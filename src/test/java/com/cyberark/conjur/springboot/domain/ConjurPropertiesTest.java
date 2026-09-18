package com.cyberark.conjur.springboot.domain;

import java.lang.reflect.Field;
import java.time.Duration;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.cyberark.conjur.sdk.ApiClient;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.processor.SpringBootConjurAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author bnasslahsen
 */
@SpringBootTest(classes = SpringBootConjurAutoConfiguration.class)
//@TestPropertySource(properties = { "conjur.authn-api-key="+TEST_KEY })
public class ConjurPropertiesTest {

    public static final String TEST_KEY = System.getenv("CONJUR_AUTHN_API_KEY");

    @Autowired
    private SecretsApi secretsApi;

    @Test
    public void testPropertyTest() throws IllegalAccessException {
        Assumptions.assumeTrue(System.getenv("CONJUR_AUTHN_CERT_SERVICE_ID") == null,
                "Skipping testPropertyTest in cert-auth mode: apiKey is not set when using mTLS");
        Field apiKeyField = FieldUtils.getDeclaredField(ApiClient.class, "apiKey", true);
        String apiKey = (String) apiKeyField.get(secretsApi.getApiClient());
        assertNotNull(apiKey);
    }

    @Test
    public void testSettersandGetters() {
        ConjurProperties props = new ConjurProperties();

        props.setAccount("myConjurAccount");
        props.setApplianceUrl("https://proxy");
        props.setAuthnLogin("host/sprinboot-demo");
        props.setAuthTokenFile("users/file/token-api");
        props.setCertFile("cert-file");
        props.setSslCertificate("ssl-certificate");
        props.setScanAllValues(true);
        props.setResilienceEnabled(true);
        props.setResilienceMaxAttempts(6);
        Duration tenMillis = Duration.ofMillis(10);
        props.setResilienceWaitDuration(tenMillis);

        assertAll(
            () -> assertEquals("myConjurAccount", props.getAccount()),
            () -> assertEquals("https://proxy", props.getApplianceUrl()),
            () -> assertEquals("host/sprinboot-demo", props.getAuthnLogin()),
            () -> assertTrue(props.isScanAllValues()),
            () -> assertEquals("users/file/token-api", props.getAuthTokenFile()),
            () -> assertEquals("cert-file", props.getCertFile()),
            () -> assertEquals("ssl-certificate", props.getSslCertificate()),
            () -> assertTrue(props.getResilienceEnabled()),
            () -> assertEquals(6, props.getResilienceMaxAttempts()),
            () -> assertEquals(tenMillis, props.getResilienceWaitDuration())


        );


    }

    @Test
    public void testToStringIncludesAllProperties() {
        ConjurProperties props = new ConjurProperties();

        // Set representative sample values
        props.setAccount("my-account");
        props.setApplianceUrl("https://conjur.myorg.com");
        props.setAuthnLogin("host/my-app");
        props.setScanAllValues(true);
        props.setMappingPath("/etc/conjur/mappings.yml");

        String toStringOutput = props.toString();

        // Basic assertions to ensure fields appear in toString output
        assertAll(
            () -> assertTrue(toStringOutput.contains("my-account")),
            () -> assertTrue(toStringOutput.contains("https://conjur.myorg.com")),
            () -> assertTrue(toStringOutput.contains("host/my-app")),
            () -> assertTrue(toStringOutput.contains("true")),
            () -> assertTrue(toStringOutput.contains(props.getIntegrationName())),
            () -> assertTrue(toStringOutput.contains(props.getIntegrationType())),
            () -> assertTrue(toStringOutput.contains(props.getVendorName()))
        );
    }
}
