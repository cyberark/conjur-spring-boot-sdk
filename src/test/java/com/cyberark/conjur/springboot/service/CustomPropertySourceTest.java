package com.cyberark.conjur.springboot.service;


import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.constant.ConjurConstant;
import com.cyberark.conjur.springboot.core.env.ConjurConfig;
import com.cyberark.conjur.springboot.core.env.ConjurConnectionManager;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootTest(classes = {CustomPropertySourceTest.class})
public class CustomPropertySourceTest {

    private CustomPropertySourceChain customChain;
    private SecretsApi secretsApi;
    private ConjurConfig conjurConfig;
    private final String account = "dev";

    private MockedStatic<ConjurConnectionManager> conjurConnectionManagerMock;

    @BeforeEach
    public void setup() {
        conjurConfig = mock(ConjurConfig.class);
        secretsApi = mock(SecretsApi.class);
        customChain = new CustomPropertySourceChain("test");
        customChain.setConjurConfig(conjurConfig);
        customChain.setSecretsApi(secretsApi);
        when(conjurConfig.getResilienceEnabled()).thenReturn(false);
        when(conjurConfig.getResilienceMaxAttempts()).thenReturn(3);
        when(conjurConfig.getResilienceWaitDuration()).thenReturn(Duration.ofMillis(50));

        conjurConnectionManagerMock = mockStatic(ConjurConnectionManager.class);
        conjurConnectionManagerMock.when(() -> ConjurConnectionManager.getAccount(secretsApi)).thenReturn("dev");
    }

    @AfterEach
    void tearDown() {
        if (conjurConnectionManagerMock != null) {
            conjurConnectionManagerMock.close(); // Always close static mocks
        }
    }

    @Test
    void testGetPropertySingleSecretSuccess() throws Exception {
        String rawKey = "path/to/secret";
        String mappedKey = "path/to/secret";
        String secretValue = "mySecret";

        when(conjurConfig.mapProperty(rawKey)).thenReturn(mappedKey);
        when(secretsApi.getSecret(account, "variable", mappedKey)).thenReturn(secretValue);

        Object result = customChain.getProperty(rawKey);

        assertNotNull(result);
        assertEquals(secretValue, new String((byte[]) result, StandardCharsets.UTF_8));
    }

    @Test
    void testGetPropertyBulkSecretSuccess() throws Exception {
        String rawKey = "path/to/secret1,path/to/secret2";
        String mappedKey1 = "path/to/secret1";
        String mappedKey2 = "path/to/secret2";
        //  Workaround to prevent NPE due to initial mapProperty call on full key
        when(conjurConfig.mapProperty("path/to/secret1,path/to/secret2")).thenReturn("path/to/secret1,path/to/secret2");

        when(conjurConfig.mapProperty("path/to/secret1")).thenReturn(mappedKey1);
        when(conjurConfig.mapProperty("path/to/secret2")).thenReturn(mappedKey2);

        Map<String, String> secretMap = new HashMap<>();
        secretMap.put(account + "/variable/" + mappedKey1, "val1");
        secretMap.put(account + "/variable/" + mappedKey2, "val2");

        when(secretsApi.getSecrets("dev:variable:path/to/secret1,dev:variable:path/to/secret2")).thenReturn(secretMap);

        Object result = customChain.getProperty(rawKey);
        String resultStr = new String((byte[]) result, StandardCharsets.UTF_8);

        assertTrue(resultStr.contains("secret1=val1"));
        assertTrue(resultStr.contains("secret2=val2"));
    }

    @Test
    void testGetPropertyThrowsIllegalStateOnApiException() throws Exception {
        String key = "throw/key";
        String mappedKey = "throw/key";
        when(conjurConfig.getResilienceEnabled()).thenReturn(true);
        when(conjurConfig.mapProperty(key)).thenReturn(mappedKey);
        when(secretsApi.getSecret(account, "variable", mappedKey)).thenThrow(new ApiException(500, "Internal Error"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> customChain.getProperty(key));
        assertTrue(ex.getMessage().contains("Failed to fetch secret"));
    }


    @Test
    void getPropertyTest() throws ApiException {
        //ConjurConnectionManager conjurConnectionManager = mock(ConjurConnectionManager.class);
        String key = "db/password";
        String mappedKey = "mapped/password";
        when(conjurConfig.mapProperty(key)).thenReturn(mappedKey);
        String account = ConjurConnectionManager.getAccount(secretsApi);
        //to test the multiple keys using @Value
        StringBuilder kind = new StringBuilder();
        Object secretValue = null;
        if (key.contains(",")) {
            String[] keys = key.split(",");
            if (keys.length > 0) {
                kind.append(account + ":variable:" + keys[0]);
                for (int i = 1; i < keys.length; i++) {
                    kind.append("," + account + ":variable:" + keys[i]);
                }
            }
            try (MockedStatic<Object> getBatchSecretsMockStatic = mockStatic(Object.class)) {

                Gson gson = mock(Gson.class);
                secretValue = secretsApi.getSecrets(new String(kind));
                Object valObject = gson.toJson(secretValue, Object.class);
                secretValue = valObject;
                getBatchSecretsMockStatic.when(() -> gson.toJson(secretsApi.getSecrets(new String(kind)), Object.class)).thenReturn(secretValue);
                assertEquals(getBatchSecretsMockStatic, secretValue);
            }
        } else {
            try (MockedStatic<Object> getSecretValMockStatic = mockStatic(Object.class)) {
                secretValue = secretsApi.getSecret(account, ConjurConstant.CONJUR_KIND, key);
                getSecretValMockStatic.when(() -> secretsApi.getSecret(account, ConjurConstant.CONJUR_KIND, key)).thenReturn(secretValue);
                assertEquals(secretsApi.getSecret(account, ConjurConstant.CONJUR_KIND, key), secretValue);
            }
        }

        @Configuration
        class CustomPropertySourceConfiguration {

            @Value("${dbpassWord}")
            private byte[] dbpassWord;

            @Value("${key}")
            private byte[] key;
        }

    }

    @Test
    void testNullKey() {
        when(conjurConfig.mapProperty(null)).thenReturn(null);
        assertThrows(NullPointerException.class, () -> customChain.getProperty(null));
    }

    @Test
    void testSetNextChain() {
        PropertyProcessorChain next = mock(PropertyProcessorChain.class);
        customChain.setNextChain(next);
        assertNotNull(next); // just to avoid IDE warning — there's no real effect here
    }

    @Test
    void testSecretRetrievalSingleFails() throws Exception {
        String key = "db/secret";
        String mappedKey = "mapped/secret";
        String account = "failAccount";

        when(conjurConfig.mapProperty(key)).thenReturn(mappedKey);
        when(secretsApi.getSecret(account, ConjurConstant.CONJUR_KIND, mappedKey)).thenThrow(new ApiException(500, "Internal Error"));
        Object result = customChain.getProperty(key);
        assertNull(result, "Should return null on ApiException");
    }


    @Test
    void testSecretRetrievalWithNonFetchableKeys() {
        for (String prefix : Arrays.asList(ConjurConstant.SPRING_VAR, ConjurConstant.SERVER_VAR, ConjurConstant.ERROR, ConjurConstant.SPRING_UTIL, ConjurConstant.CONJUR_PREFIX, ConjurConstant.ACTUATOR_PREFIX, ConjurConstant.LOGGING_PREFIX, ConjurConstant.KUBERNETES_PREFIX)) {

            String ignoredKey = prefix + "ignored.property";
            when(conjurConfig.mapProperty(ignoredKey)).thenReturn(ignoredKey);

            Object result = customChain.getProperty(ignoredKey);
            assertNull(result, "Should return null for ignored key prefix: " + prefix);
        }
    }

    @Test
    void testGetPropertyNamesReturnsEmptyArray() {
        String[] propertyNames = customChain.getPropertyNames();
        assertNotNull(propertyNames, "Property names array should not be null");
        assertEquals(0, propertyNames.length);
    }


}
