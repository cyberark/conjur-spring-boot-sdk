package com.cyberark.conjur.springboot.processor;

import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.annotations.ConjurValue;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author bnasslahsen
 */
@SpringBootTest
public class ConjurRetrieveSecretServiceTest {

	@Autowired
	private SecretsApi secretsApiMock;

	@Test
	public void testGetSecretCallsCount() throws ApiException {
		// Verify the number of times the method was called
		verify(secretsApiMock, times(1)).getSecret(any(),any(),any());
	}

	@TestConfiguration
	public static class SecretApiMockConfig {

		@Bean
		@Primary
		public SecretsApi secretsApiMock() throws ApiException {
			SecretsApi secretsApi = mock(SecretsApi.class);
			when(secretsApi.getSecret(any(),any(),any())).thenReturn("secret");
			return secretsApi;
		}

	}

	@SpringBootApplication
	static class ConjurPropertySourceTestApp {

		@Configuration
		class ConjurPropertySourceConfiguration {

			@ConjurValue(key = "db/dbpassWord")
			private byte[] dbpassWord;
		}
	}
}
