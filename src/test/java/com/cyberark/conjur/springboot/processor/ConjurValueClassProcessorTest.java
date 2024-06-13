package com.cyberark.conjur.springboot.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.springboot.ConjurPluginTests;
import com.cyberark.conjur.springboot.core.env.ConjurConfig;

@SpringBootTest
@ContextConfiguration(classes = { ConjurValueClassProcessorTest.TestConfig.class })
class ConjurValueClassProcessorTest {
	
	private static ConjurConfig conjurConfig = new ConjurConfig();
	@Configuration
	static class TestConfig {
		@Bean
		public ConjurRetrieveSecretService conjurRetrieveSecretService() {
			return Mockito.mock(ConjurRetrieveSecretService.class);
		}

		@Bean
		public ConjurValueClassProcessor conjurValueClassProcessor(
				ConjurRetrieveSecretService conjurRetrieveSecretService) {
			return new ConjurValueClassProcessor(conjurRetrieveSecretService,conjurConfig);
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
	private ConjurRetrieveSecretService conjurRetrieveSecretService;

	@BeforeEach
	void setUp() throws ApiException {

		MockitoAnnotations.openMocks(this);

		test.setDdbUserNameMap("test-username".getBytes());
		test.setDbPasswordMap("test-password".getBytes());
		test.setDbSecretsMap("test-username,test-password,test-url".getBytes());

		// Ensure that the ConjurRetrieveSecretService is properly mocked if necessary
		Mockito.when(conjurRetrieveSecretService.retriveSingleSecretForCustomAnnotation(Mockito.anyString()))
				.thenReturn("test-secret".getBytes());
	}

	@Test
	void postProcessBeforeInitializationTest() {
		Object result = conjurValueClassProcessor.postProcessBeforeInitialization(test, "testBean");
		assertEquals(test, result);
	}
}
