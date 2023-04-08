package com.cyberark.conjur.springboot.processor;

import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.core.env.ConjurConnectionManager;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SpringBootConjurAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	ConjurValueClassProcessor conjurSecretValueClassProcessor(ConjurRetrieveSecretService conjurRetrieveSecretService) {
		return new ConjurValueClassProcessor(conjurRetrieveSecretService);
	}

	@ConditionalOnMissingBean
	@Bean
	ConjurRetrieveSecretService conjurRetrieveSecretService(SecretsApi secretsApi) {
		return new ConjurRetrieveSecretService(secretsApi);
	}

	@ConditionalOnMissingBean
	@Bean
	SecretsApi secretsApi() {
		return new SecretsApi();
	}

	@ConditionalOnMissingBean
	@Bean
	static ConjurConnectionManager conjurConnectionManager() {
		return new ConjurConnectionManager();
	}

}
