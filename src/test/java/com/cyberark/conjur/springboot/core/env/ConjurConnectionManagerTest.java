package com.cyberark.conjur.springboot.core.env;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Base64;

import com.cyberark.conjur.sdk.ApiClient;
import com.cyberark.conjur.springboot.domain.ConjurProperties;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

import com.cyberark.conjur.sdk.AccessToken;
import com.cyberark.conjur.sdk.Configuration;
import com.cyberark.conjur.springboot.core.env.ConjurConnectionManagerTest.ConjurConnectionManagerMockConfig;
import com.cyberark.conjur.springboot.processor.SpringBootConjurAutoConfiguration;

/**
 * @author bnasslahsen
 */
@TestPropertySource(properties = {
        "conjur.authenticator-id=demo-cluster",
        "conjur.jwt-token-path=/home/bnl/test",
        // Prevent CONJUR_AUTHN_CERT_SERVICE_ID / CONJUR_CLIENT_* env vars (set in
        // the cert-auth E2E Docker run) from overriding the JWT auth test path.
        // @TestPropertySource has higher priority than OS environment variables in
        // the Spring test context, so an explicit empty value here wins.
        "conjur.authn-cert-service-id=",
        "conjur.client-cert-file=",
        "conjur.client-key-file="
})
@SpringBootTest(classes = {SpringBootConjurAutoConfiguration.class, ConjurConnectionManagerMockConfig.class})
public class ConjurConnectionManagerTest {

    private static final String jsonString = "{\"token\":\"sample-token\"}";
    @Autowired
    private AccessTokenProvider accessTokenProviderMock;
    @Mock
    private Environment environment;

    @Mock
    private ConjurProperties conjurProperties;

    @Mock
    private AccessToken accessToken;

    private ConjurConnectionManager connectionManager;

    @TestConfiguration
    static class ConjurConnectionManagerMockConfig {

        @Bean
        @Primary
        public AccessTokenProvider accessTokenProviderMock() throws IOException, GeneralSecurityException {
            AccessTokenProvider accessTokenProviderMock = mock(AccessTokenProvider.class);
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            String SAMPLE_TOKEN = Base64.getEncoder().encodeToString(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
            when(accessTokenProviderMock.getJwtAccessToken(any(), any(), any())).thenReturn(AccessToken.fromEncodedToken(SAMPLE_TOKEN));
            when(accessTokenProviderMock.getCertAccessToken(any(), any(), any(), any(), any())).thenReturn(null);
            return accessTokenProviderMock;
        }

    }

    @BeforeEach
    void setUp() {
        connectionManager = new ConjurConnectionManager(accessTokenProviderMock);
        connectionManager.setEnvironment(environment);
    }

    @Test
    void testPostProcessBeanFactory_WithResilienceEnabled() {
        // Arrange
        BindResult<ConjurProperties> bindResult = mock(BindResult.class);
        when(bindResult.isBound()).thenReturn(true);
        when(bindResult.get()).thenReturn(conjurProperties);

        when(conjurProperties.getResilienceMaxAttempts()).thenReturn(3);
        when(conjurProperties.getResilienceWaitDuration()).thenReturn(Duration.ofSeconds(2));
        when(conjurProperties.getResilienceEnabled()).thenReturn(true);
        when(conjurProperties.getAccount()).thenReturn("demo-account");
        when(conjurProperties.getApplianceUrl()).thenReturn("https://conjur.example.com");

        try (MockedStatic<Binder> mockedBinder = mockStatic(Binder.class)) {
            Binder binder = mock(Binder.class);
            mockedBinder.when(() -> Binder.get(environment)).thenReturn(binder);
            when(binder.bind(eq("conjur"), eq(ConjurProperties.class))).thenReturn(bindResult);

            connectionManager.setEnvironment(environment);

            // Act
            connectionManager.postProcessBeanFactory(mock(ConfigurableListableBeanFactory.class));

            // Assert — no exception thrown, connection was initialized
        }
    }

    @Test
    void testGetConnection_setsAccessToken() throws Exception {
        ApiClient spyClient = Mockito.spy(new ApiClient());

        try (MockedStatic<Configuration> mockedConfig = mockStatic(Configuration.class)) {
            mockedConfig.when(Configuration::getDefaultApiClient).thenReturn(spyClient);

            when(conjurProperties.getAccount()).thenReturn("demo-account");
            when(conjurProperties.getApplianceUrl()).thenReturn("https://conjur.example.com");
            when(conjurProperties.getResilienceEnabled()).thenReturn(false);
            when(conjurProperties.getAuthnLogin()).thenReturn("host/app");
            when(conjurProperties.getAuthnApiKey()).thenReturn("api-key");

            AccessToken token = mock(AccessToken.class);
            when(token.getHeaderValue()).thenReturn("Bearer abc123");

            when(accessTokenProviderMock.getNewAccessToken(spyClient)).thenReturn(token);

            // Call private getConnection via reflection
            Method method = ConjurConnectionManager.class.getDeclaredMethod("getConnection", ConjurProperties.class);
            method.setAccessible(true);
            method.invoke(connectionManager, conjurProperties);


            verify(spyClient).setAccessToken("Bearer abc123");
        }
    }

    @Test
    public void testGetJwtAccessToken() throws IOException {
        // Verify that getJwtAccessToken is invoked
        verify(accessTokenProviderMock).getJwtAccessToken(any(), any(), any());
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        String SAMPLE_TOKEN = Base64.getEncoder().encodeToString(jsonObject.toString().getBytes(StandardCharsets.UTF_8));

        when(accessTokenProviderMock.getNewAccessToken(Configuration.getDefaultApiClient())).thenReturn(AccessToken.fromEncodedToken(SAMPLE_TOKEN));
    }

    @Test
    void testGetConnection_certAuth() throws Exception {
        ApiClient spyClient = Mockito.spy(new ApiClient());

        try (MockedStatic<Configuration> mockedConfig = mockStatic(Configuration.class)) {
            mockedConfig.when(Configuration::getDefaultApiClient).thenReturn(spyClient);

            when(conjurProperties.getAccount()).thenReturn("demo-account");
            when(conjurProperties.getApplianceUrl()).thenReturn("https://conjur.example.com");
            when(conjurProperties.getResilienceEnabled()).thenReturn(false);
            when(conjurProperties.getAuthnCertServiceId()).thenReturn("acme-vm");
            when(conjurProperties.getAuthnLogin()).thenReturn("host/vm-workloads/vm-01");
            when(conjurProperties.getClientCertFile()).thenReturn("/path/to/cert.pem");
            when(conjurProperties.getClientKeyFile()).thenReturn("/path/to/key.pem");

            AccessToken token = mock(AccessToken.class);
            when(token.getHeaderValue()).thenReturn("Bearer cert-token");
            when(accessTokenProviderMock.getCertAccessToken(any(), eq("acme-vm"),
                    eq("host/vm-workloads/vm-01"), eq("/path/to/cert.pem"), eq("/path/to/key.pem")))
                    .thenReturn(token);

            Method method = ConjurConnectionManager.class.getDeclaredMethod("getConnection", ConjurProperties.class);
            method.setAccessible(true);
            method.invoke(connectionManager, conjurProperties);

            verify(accessTokenProviderMock).getCertAccessToken(any(), eq("acme-vm"),
                    eq("host/vm-workloads/vm-01"), eq("/path/to/cert.pem"), eq("/path/to/key.pem"));
            verify(spyClient).setAccessToken("Bearer cert-token");
        }
    }
}
