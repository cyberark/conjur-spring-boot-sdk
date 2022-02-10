package com.annotation.config.processor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.annotation.secret.service.ConjurConnectionManager;
import com.annotation.secret.service.ConjurRetrieveSecretManager;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;

@Configuration
@ConditionalOnClass(ConjurRetrieveSecretManager.class)
public class SpringBootConjurAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	ConjurRetrieveSecretManager conjurRetrieveSecretManager() {
		return new ConjurRetrieveSecretManager();
	}

	@ConditionalOnMissingBean
	@Bean
	ConjurValueClassProcessor conjurValueClassProcessor() {
		return new ConjurValueClassProcessor();
	}

	@ConditionalOnMissingBean
	@Bean
	ConjurConnectionManager conjurConnectionManager() {
		return new ConjurConnectionManager();
	}

	@ConditionalOnMissingBean
	@Bean
	SecretsApi secretApi() {
		return new SecretsApi();
	}

	@ConditionalOnMissingBean
	@Bean
	ConjurValuesClassProcessor conjurValuesClassProcessor() {
		return new ConjurValuesClassProcessor();
	}

//	@ConditionalOnMissingBean
//	@Bean
//	MyProperty myProperty() {
//		return new MyProperty();
//	}

}
