package com.cyberark.conjur.springboot.processor;

import static com.cyberark.conjur.springboot.constant.ConjurConstant.CONJUR_PREFIX;
import static com.cyberark.conjur.springboot.constant.ConjurConstant.CONJUR_SCAN_ALL_VALUES;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.core.env.AccessTokenProvider;
import com.cyberark.conjur.springboot.core.env.ConjurConfig;
import com.cyberark.conjur.springboot.core.env.ConjurConnectionManager;
import com.cyberark.conjur.springboot.core.env.ConjurPropertySource;
import com.cyberark.conjur.springboot.domain.ConjurProperties;
	

@Configuration(proxyBeanMethods = false)
public class SpringBootConjurAutoConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootConjurAutoConfiguration.class);

	@ConditionalOnMissingBean
	@Bean
	ConjurValueClassProcessor conjurSecretValueClassProcessor(ConjurRetrieveSecretService conjurRetrieveSecretService, ConjurConfig conjurConfig) {
		return new ConjurValueClassProcessor(conjurRetrieveSecretService,conjurConfig);
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
	AccessTokenProvider accessTokenProvider() {
		return new AccessTokenProvider();
	}

	@ConditionalOnMissingBean
	@ConfigurationProperties(prefix = CONJUR_PREFIX)
	@Bean
	ConjurProperties conjurProperties() {
		return new ConjurProperties();
	}

	@ConditionalOnMissingBean(ConjurPropertySource.class)
	@ConditionalOnProperty(name = CONJUR_SCAN_ALL_VALUES)
	@Bean
	static ConjurCloudProcessor conjurCloudProcessor(SecretsApi secretsApi, ConjurConfig conjurConfig) {

		LOGGER.info("Creating ConjurCloudProcessor instance");

		return new ConjurCloudProcessor(secretsApi, conjurConfig);
	}
	
	@ConditionalOnMissingBean
	@Bean
	static ConjurConfig conjurConfig() {
		return new ConjurConfig();
	}

}