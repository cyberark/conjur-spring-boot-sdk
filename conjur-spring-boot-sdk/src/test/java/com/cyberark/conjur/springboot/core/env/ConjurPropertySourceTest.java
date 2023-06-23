package com.cyberark.conjur.springboot.core.env;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.annotations.ConjurPropertySource;
import com.cyberark.conjur.springboot.core.env.ConjurPropertySourceTest.ConjurPropertySourceConfiguration;
import com.cyberark.conjur.springboot.processor.SpringBootConjurAutoConfiguration;

/**
 * @author bnasslahsen
 */
@SpringBootTest(classes = { SpringBootConjurAutoConfiguration.class, ConjurPropertySourceConfiguration.class, ConjurPropertySourceTest.class})
public class ConjurPropertySourceTest {

	@MockBean
	private SecretsApi secretsApi;

	/*@Test
	public void testGetSecretCallsCount() throws ApiException {
		// Verify the number of times the method was called
		verify(secretsApi, times(3)).getSecret(any(), any(),
				any());
	}*/

	@ConjurPropertySource("db/")
	@Configuration
	class ConjurPropertySourceConfiguration {

		@Value("${dbpassWord}")
		private byte[] dbpassWord;

		@Value("${dbuserName}")
		private byte[] dbuserName;

		@Value("${key}")
		private byte[] key;
	}

}
