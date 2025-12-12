package com.cyberark.conjur.springboot.processor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.springboot.ConjurPluginTests;
import com.cyberark.conjur.springboot.core.env.ConjurConfig;


@SpringBootTest
@ContextConfiguration(classes = {ConjurValueClassProcessorTest.TestConfig.class})
public class ConjurValueClassProcessorTest {

    //private static ConjurConfig conjurConfig = new ConjurConfig();
    @Configuration
    static class TestConfig {
        @Bean
        public ConjurConfig conjurConfig() {
            // Either return the real one or mock if needed
            return Mockito.mock(ConjurConfig.class);
        }

        @Bean
        public ConjurRetrieveSecretService conjurRetrieveSecretService() {
            return Mockito.mock(ConjurRetrieveSecretService.class);
        }

        @Bean
        public ConjurValueClassProcessor conjurValueClassProcessor(ConjurRetrieveSecretService conjurRetrieveSecretService, ConjurConfig conjurConfig) {  // Inject ConjurConfig properly
            return new ConjurValueClassProcessor(conjurRetrieveSecretService, conjurConfig);
        }

        @Bean
        public ConjurPluginTests conjurPluginTests() {
            return new ConjurPluginTests();
        }
    }

    @Autowired
    private ConjurValueClassProcessor conjurValueClassProcessor;

    @Autowired
    private ConjurPluginTests test;

    @Autowired
    private ConjurConfig conjurConfig;

    @Autowired
    private ConjurRetrieveSecretService conjurRetrieveSecretService;

    @BeforeEach
    void setUp() throws ApiException {

        test.setDbPasswordMap("test-password".getBytes());
        test.setDbSecretsMap("test-username,test-password,test-url".getBytes());

        // Mock secrets
        when(conjurRetrieveSecretService.retriveSingleSecretForCustomAnnotation("username")).thenReturn("mock-username".getBytes());
        when(conjurRetrieveSecretService.retriveSingleSecretForCustomAnnotation("password")).thenReturn("mock-password".getBytes());
        when(conjurRetrieveSecretService.retriveSingleSecretForCustomAnnotation("url")).thenReturn("mock-url".getBytes());


        when(conjurRetrieveSecretService.retriveMultipleSecretsForCustomAnnotation(argThat(arr -> arr != null && Arrays.asList(arr).containsAll(Arrays.asList("username", "password", "url"))))).thenReturn("username=mock-username,password=mock-password,url=mock-url".getBytes());

        // Mock ConjurConfig mapping
        when(conjurConfig.mapProperty(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }


    @Test
    void testFallbackToRetrieveSecretsForCredentialWhenBulkFailsWith404() throws ApiException {
        test.setDdbUserNameMap("test-username".getBytes());
        // Trigger ApiException for bulk call
        when(conjurRetrieveSecretService.retriveMultipleSecretsForCustomAnnotation(new String[]{"username", "password", "url"})).thenThrow(new ApiException(404, "Not Found"));

        // Fallback will call single secret retrieval
        when(conjurRetrieveSecretService.retriveSingleSecretForCustomAnnotation("username")).thenReturn("fallback-username".getBytes());
        when(conjurRetrieveSecretService.retriveSingleSecretForCustomAnnotation("password")).thenReturn("fallback-password".getBytes());
        when(conjurRetrieveSecretService.retriveSingleSecretForCustomAnnotation("url")).thenReturn("fallback-url".getBytes());

        Object result = conjurValueClassProcessor.postProcessBeforeInitialization(test, "testBean");

        assertEquals("fallback-username", new String(test.getDdbUserNameMap()));
        assertEquals("fallback-password", new String(test.getDbPasswordMap()));
        String secretMap = new String(test.getDbSecretsMap());

        assertTrue(secretMap.contains("username=fallback-username"));
        assertTrue(secretMap.contains("password=fallback-password"));
        assertTrue(secretMap.contains("url=fallback-url"));
    }

    @Test
    void testFieldWithoutAnnotationIsIgnored() {
        test.setDdbUserNameMap("test-username".getBytes());
        Object result = conjurValueClassProcessor.postProcessBeforeInitialization(new Object(), "testBean");
        assertEquals(Object.class, result.getClass());
    }

    @Test
    void testSingleSecretReturnsNull() throws Exception {
        test.setDdbUserNameMap(null);
        when(conjurRetrieveSecretService.retriveSingleSecretForCustomAnnotation("username")).thenReturn(null);

        Object result = conjurValueClassProcessor.postProcessBeforeInitialization(test, "testBean");

        assertNull(test.getDdbUserNameMap());
    }

    @Test
    void testEmptyKeysInConjurValuesAnnotation() {
        ConjurPluginTests bean = new ConjurPluginTests() {
            @com.cyberark.conjur.springboot.annotations.ConjurValues(keys = {})
            private byte[] emptyKeysField;
        };

        Object result = conjurValueClassProcessor.postProcessBeforeInitialization(bean, "testBean");
        // Nothing to assert as field is inaccessible; no exception is success
        assertEquals(bean.getClass(), result.getClass());
    }

    @Test
    void testIOExceptionDuringFallbackHandling() throws Exception {
        // Throw 404 to trigger fallback
        when(conjurRetrieveSecretService.retriveMultipleSecretsForCustomAnnotation(new String[]{"username", "password", "url"})).thenThrow(new ApiException(404, "Not Found"));

        // Return null from fallback retrieval to simulate write error
        when(conjurRetrieveSecretService.retriveSingleSecretForCustomAnnotation("username")).thenReturn(null);
        when(conjurRetrieveSecretService.retriveSingleSecretForCustomAnnotation("password")).thenReturn(null);
        when(conjurRetrieveSecretService.retriveSingleSecretForCustomAnnotation("url")).thenReturn(null);

        Object result = conjurValueClassProcessor.postProcessBeforeInitialization(test, "testBean");

        // Even with IO exception, bean should still return
        assertEquals(test, result);
    }

    @Test
    void testPartialFallbackWithNullSecrets() throws Exception {
        when(conjurRetrieveSecretService.retriveMultipleSecretsForCustomAnnotation(new String[]{"username", "password", "url"})).thenThrow(new ApiException(404, "Not Found"));

        when(conjurRetrieveSecretService.retriveSingleSecretForCustomAnnotation("username")).thenReturn("partial-user".getBytes());
        when(conjurRetrieveSecretService.retriveSingleSecretForCustomAnnotation("password")).thenReturn(null); // null password
        when(conjurRetrieveSecretService.retriveSingleSecretForCustomAnnotation("url")).thenReturn("partial-url".getBytes());

        Object result = conjurValueClassProcessor.postProcessBeforeInitialization(test, "testBean");

        String secretMap = new String(test.getDbSecretsMap());
        assertTrue(secretMap.contains("username=partial-user"));
        assertTrue(secretMap.contains("url=partial-url"));
    }

    @Test
    void postProcessBeforeInitializationTest() {
        Object result = conjurValueClassProcessor.postProcessBeforeInitialization(test, "testBean");
        assertEquals(test, result);

        // Assert fields are populated
        assertEquals("mock-username", new String(test.getDdbUserNameMap()));
        assertEquals("mock-password", new String(test.getDbPasswordMap()));
        String secretMap = new String(test.getDbSecretsMap());

        // Validate at least contains expected keys
        assertTrue(secretMap.contains("username=mock-username"));
        assertTrue(secretMap.contains("password=mock-password"));
        assertTrue(secretMap.contains("url=mock-url"));
    }
}
