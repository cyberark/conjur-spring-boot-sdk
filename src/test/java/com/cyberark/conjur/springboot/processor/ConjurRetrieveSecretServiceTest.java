package com.cyberark.conjur.springboot.processor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cyberark.conjur.springboot.constant.ConjurConstant;
import com.cyberark.conjur.springboot.core.env.ConjurConfig;
import com.cyberark.conjur.springboot.core.env.ConjurConnectionManager;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.annotations.ConjurValue;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bnasslahsen
 */
@SpringBootTest(classes = {ConjurRetrieveSecretServiceTest.class, SpringBootConjurAutoConfiguration.class, com.cyberark.conjur.springboot.processor.ConjurRetrieveSecretServiceTest.ConjurPropertySourceConfiguration.class})
public class ConjurRetrieveSecretServiceTest {

    @MockitoBean
    private SecretsApi secretsApiMock;

    @MockitoBean
    private ConjurConfig conjurConfig;


    public ConjurRetrieveSecretService service;
    private final String account = "dev";

    //	@Test
//	public void testGetSecretCallsCount() throws ApiException {
//		// Verify the number of times the method was called
//		verify(secretsApiMock, times(1)).getSecret(any(), any(), any());
//	}
    @BeforeEach
    void setUp() throws IllegalAccessException, NoSuchFieldException {
        service = new ConjurRetrieveSecretService(secretsApiMock);
        Field configField = ConjurRetrieveSecretService.class.getDeclaredField("conjurConfig");
        configField.setAccessible(true);
        configField.set(service, conjurConfig);

        when(conjurConfig.getResilienceEnabled()).thenReturn(false);
        when(conjurConfig.getResilienceMaxAttempts()).thenReturn(3);
        when(conjurConfig.getResilienceWaitDuration()).thenReturn(Duration.ofMillis(50));
    }

    @Test
    void testRetriveSingleSecretSuccess() throws Exception {
        String key = "db/password";
        byte[] expected = "admin".getBytes(StandardCharsets.UTF_8);

        try (MockedStatic<ConjurConnectionManager> mockStatic = mockStatic(ConjurConnectionManager.class)) {
            mockStatic.when(() -> ConjurConnectionManager.getAccount(secretsApiMock)).thenReturn(account); // "dev" or whatever you defined

            when(secretsApiMock.getSecret(account, ConjurConstant.CONJUR_KIND, key)).thenReturn("admin");

            byte[] result = service.retriveSingleSecretForCustomAnnotation(key);
            assertArrayEquals(expected, result);
        }
    }

    @Test
    void testRetriveSingleSecretReturnsNullAndThrows404() {
        String key = "missing/key";

        try {
            when(secretsApiMock.getSecret(account, ConjurConstant.CONJUR_KIND, key)).thenReturn(null);
            service.retriveSingleSecretForCustomAnnotation(key);
            fail("Expected ApiException");
        } catch (ApiException e) {
            assertEquals(404, e.getCode());
            assertTrue(e.getMessage().contains("Secret not found"));
        } catch (Exception e) {
            fail("Expected ApiException, but got: " + e);
        }
    }

    @Test
    void testRetriveSingleSecretThrowsWrappedException() throws Exception {
        String key = "error/key";

        try (MockedStatic<ConjurConnectionManager> mockStatic = mockStatic(ConjurConnectionManager.class)) {
            mockStatic.when(() -> ConjurConnectionManager.getAccount(secretsApiMock)).thenReturn(account);

            // Throw runtime exception from API call
            when(secretsApiMock.getSecret(account, ConjurConstant.CONJUR_KIND, key)).thenThrow(new RuntimeException("network failure"));

            service.retriveSingleSecretForCustomAnnotation(key);
            fail("Expected ApiException");
        } catch (ApiException e) {
            assertEquals(500, e.getCode());
            assertTrue(e.getMessage().contains("Unexpected error"));
        } catch (Exception e) {
            fail("Expected ApiException, but got: " + e);
        }
    }

    @Test
    void testRetriveMultipleSecretsSuccess() throws Exception {
        String[] keys = new String[]{"db/username", "db/password"};

        // Prepare expected secret map
        Map<String, String> secrets = new HashMap<>();
        secrets.put("dev/variable/db/username", "admin");
        secrets.put("dev/variable/db/password", "admin123");

        // Expected values in result
        String expectedResult1 = "username=admin";
        String expectedResult2 = "password=admin123";

        // Gson instance
        Gson gson = new Gson();

        try (MockedStatic<ConjurConnectionManager> mockStatic = mockStatic(ConjurConnectionManager.class)) {
            // Mock static call to get account
            mockStatic.when(() -> ConjurConnectionManager.getAccount(secretsApiMock)).thenReturn("dev");

            // Mock secret fetch
            when(secretsApiMock.getSecrets("dev:variable:db/username,dev:variable:db/password")).thenReturn(secrets);

            // Execute
            byte[] result = service.retriveMultipleSecretsForCustomAnnotation(keys);
            String resultStr = new String(result, StandardCharsets.UTF_8);

            // Assert
            assertTrue(resultStr.contains(expectedResult1));
            assertTrue(resultStr.contains(expectedResult2));
        }
    }

    @Test
    void testRetriveMultipleSecretsEmptyArray() throws Exception {
        byte[] result = service.retriveMultipleSecretsForCustomAnnotation(new String[0]);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testRetriveMultipleSecretsThrowsApiException() throws Exception {
        String[] keys = new String[]{"db/username"};

        when(secretsApiMock.getSecrets(any())).thenThrow(new ApiException(500, "internal error"));

        ApiException thrown = assertThrows(ApiException.class, () -> service.retriveMultipleSecretsForCustomAnnotation(keys));

        assertEquals(500, thrown.getCode());
    }


    @TestConfiguration
    static class SecretApiMockConfig {

        @Bean
        @Primary
        public SecretsApi secretsApiMock() throws ApiException {
            // secret Api call
            SecretsApi secretsApi = mock(SecretsApi.class);
            when(secretsApi.getSecret(any(), any(), any())).thenReturn("secret");
            return secretsApi;
        }

    }

    @Configuration
    class ConjurPropertySourceConfiguration {

        @ConjurValue(key = "db/dbpassWord")
        private byte[] dbPassword;

        public byte[] getDbPassword() {
            return dbPassword;
        }
    }
}
