package com.cyberark.conjur.springboot.domain;

import static org.junit.Assert.assertNotNull;
import java.lang.reflect.Field;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.cyberark.conjur.sdk.ApiClient;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.processor.SpringBootConjurAutoConfiguration;

/**
 * @author bnasslahsen
 */
@SpringBootTest(classes =SpringBootConjurAutoConfiguration.class)
//@TestPropertySource(properties = { "conjur.authn-api-key="+TEST_KEY })
public class ConjurPropertiesTest {
	
	public static final String TEST_KEY = System.getenv("CONJUR_AUTHN_API_KEY");

	@Autowired
	private SecretsApi secretsApi;
	
	@Test
	public void testPropertyTest() throws IllegalAccessException {
		Field apiKeyField = FieldUtils.getDeclaredField(ApiClient.class, "apiKey", true);
		String apiKey = (String) apiKeyField.get(secretsApi.getApiClient());
		assertNotNull(apiKey);
	}
}