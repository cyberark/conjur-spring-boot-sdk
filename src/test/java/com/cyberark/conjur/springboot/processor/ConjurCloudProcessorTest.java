package com.cyberark.conjur.springboot.processor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import com.cyberark.conjur.sdk.AccessToken;
import com.cyberark.conjur.sdk.Configuration;
import com.cyberark.conjur.sdk.auth.ApiKeyAuth;
import com.cyberark.conjur.springboot.core.env.AccessTokenProvider;



@TestPropertySource(properties = {
        "conjur.authenticator-id=demo-cluster",
        "conjur.jwt-token-path=/home/bnl/test",
        // Prevent CONJUR_AUTHN_CERT_SERVICE_ID / CONJUR_CLIENT_* env vars (set in
        // the cert-auth E2E Docker run) from overriding the JWT auth test path.
        // @TestPropertySource has higher priority than OS environment variables in
        // the Spring test context, so explicit empty values here win.
        "conjur.authn-cert-service-id=",
        "conjur.client-cert-file=",
        "conjur.client-key-file="
})
@SpringBootTest(classes = { SpringBootConjurAutoConfiguration.class, com.cyberark.conjur.springboot.processor.ConjurCloudProcessorTest.ConjurCloudProcessorMockConfig.class })
public class ConjurCloudProcessorTest {


    private static final String jsonString = "{\"token\":\"sample-token\"}";

	@Autowired
	private AccessTokenProvider accessTokenProviderMock;

	@TestConfiguration
	static class ConjurCloudProcessorMockConfig {

		@Bean

		@Primary
		public AccessTokenProvider accessTokenProviderMock() throws IOException {
			AccessTokenProvider accessTokenProviderMock = mock(AccessTokenProvider.class);
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            String SAMPLE_TOKEN = Base64.getEncoder().encodeToString(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
			when(accessTokenProviderMock.getJwtAccessToken(any(), any(), any()))
					.thenReturn(AccessToken.fromEncodedToken(SAMPLE_TOKEN));
			return accessTokenProviderMock;
		}

	}

    @Test
    public void testGetJwtAccessToken() throws IOException {
        // Verify that getJwtAccessToken is invoked
        verify(accessTokenProviderMock).getJwtAccessToken(any(), any(), any());

        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        String SAMPLE_TOKEN = Base64.getEncoder().encodeToString(jsonObject.toString().getBytes(StandardCharsets.UTF_8));

        when(accessTokenProviderMock.getNewAccessToken(Configuration.getDefaultApiClient()))
            .thenReturn(AccessToken.fromEncodedToken(SAMPLE_TOKEN));

    }



}
