package com.cyberark.conjur.springboot.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.constant.ConjurConstant;
import com.cyberark.conjur.springboot.core.env.ConjurConfig;
import com.cyberark.conjur.springboot.core.env.ConjurConnectionManager;
import com.google.gson.Gson;

@SpringBootTest(classes = {CustomPropertySourceTest.class})
@RunWith(MockitoJUnitRunner.class)
public class CustomPropertySourceTest {
	
	private SecretsApi secretsApi=mock(SecretsApi.class);
	
	private ConjurConfig conjurConfig = new ConjurConfig();
	
	ConjurConnectionManager conjurConnectionManager=mock(ConjurConnectionManager.class);
	
	String key=conjurConfig.mapProperty(toString());
	
	@Test
	public void getPropertyTest() throws  ApiException {
		String account = ConjurConnectionManager.getAccount(secretsApi);
		//to test the multiple keys using @Value
		StringBuilder kind = new StringBuilder();
		Object secretValue = null;
		if(key.contains(",")) {
			String[] keys = key.split(",");
			if (keys.length > 0) {
				kind.append(account + ":variable:" +  keys[0]); 
				for (int i = 1; i < keys.length; i++) {
					kind.append("," + account+ ":variable:" + keys[i]);
				}
			}
			try (MockedStatic<Object> getBatchSecretsMockStatic = mockStatic(Object.class)){
			     
			    Gson gson = mock(Gson.class);
				secretValue = secretsApi.getSecrets(new String(kind));
				Object valObject = gson.toJson(secretValue, Object.class);
				secretValue = valObject;
				getBatchSecretsMockStatic.when(() -> gson.toJson(secretsApi.getSecrets(new String(kind)), Object.class)).thenReturn(secretValue);
				assertEquals(getBatchSecretsMockStatic, secretValue);
			}
		}
			else {
				try (MockedStatic<Object> getSecretValMockStatic = mockStatic(Object.class))  {
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
}
