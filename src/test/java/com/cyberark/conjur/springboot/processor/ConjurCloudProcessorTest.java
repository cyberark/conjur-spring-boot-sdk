package com.cyberark.conjur.springboot.processor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

import com.cyberark.conjur.sdk.endpoint.SecretsApi;

@SpringBootTest(classes = { SpringBootConjurAutoConfiguration.class,ConjurCloudProcessorTest.class })
public class ConjurCloudProcessorTest {

	@MockBean
	private SecretsApi secretsApi;


	@Configuration
	class PropertySourceConfiguration {

		@Value("${dbpassWord}")
		private byte[] dbpassWord;

		@Value("${dbuserName}")
		private byte[] dbuserName;

		@Value("${key}")
		private byte[] key;
	}

}
