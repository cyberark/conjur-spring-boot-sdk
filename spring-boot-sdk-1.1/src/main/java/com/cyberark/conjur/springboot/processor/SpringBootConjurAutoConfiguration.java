package com.cyberark.conjur.springboot.processor;

import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.core.env.AccessTokenProvider;
import com.cyberark.conjur.springboot.core.env.ConjurConnectionManager;
import com.cyberark.conjur.springboot.domain.ConjurProperties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.cyberark.conjur.springboot.constant.ConjurConstant.CONJUR_PREFIX;

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
	static ConjurConnectionManager conjurConnectionManager(AccessTokenProvider accessTokenProvider) {
		return new ConjurConnectionManager(accessTokenProvider);
	}

	@ConditionalOnMissingBean
	@Bean
	AccessTokenProvider accessTokenProvider(){
		return new AccessTokenProvider();
	}

	@ConditionalOnMissingBean
	@ConfigurationProperties(prefix = CONJUR_PREFIX)
	@Bean
	ConjurProperties conjurProperties(){
		return new ConjurProperties();
	}
}