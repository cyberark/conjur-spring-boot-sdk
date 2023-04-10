package com.cyberark.conjur.springboot.processor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cyberark.conjur.sdk.endpoint.SecretsApi;

@Configuration
public class SpringBootConjurAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	ConjurValueClassProcessor conjurSecretValueClassProcessor() {
		return new ConjurValueClassProcessor();
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
	ConjurValuesClassProcessor conjurValuesClassProcessor() {
		return new ConjurValuesClassProcessor();
	}

}
