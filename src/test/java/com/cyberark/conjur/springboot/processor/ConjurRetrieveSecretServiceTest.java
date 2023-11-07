package com.cyberark.conjur.springboot.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.annotations.ConjurValue;
import com.cyberark.conjur.springboot.domain.ConjurProperties;

/**
 * @author bnasslahsen
 */
@SpringBootTest(classes = { ConjurRetrieveSecretServiceTest.class, SpringBootConjurAutoConfiguration.class,com.cyberark.conjur.springboot.processor.ConjurRetrieveSecretServiceTest.ConjurPropertySourceConfiguration.class })
@RunWith(MockitoJUnitRunner.class)
public class ConjurRetrieveSecretServiceTest {

	@MockBean
	private SecretsApi secretsApiMock;

	@InjectMocks
	public ConjurProperties conjurConfig;

	public ConjurRetrieveSecretService conjurService;

	@Test
	public void testGetSecretCallsCount() throws ApiException {
		// Verify the number of times the method was called
		verify(secretsApiMock, times(1)).getSecret(any(), any(), any());
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
