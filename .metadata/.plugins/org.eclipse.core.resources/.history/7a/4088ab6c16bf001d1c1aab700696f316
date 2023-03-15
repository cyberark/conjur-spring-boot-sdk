package com.cyberark.conjur.springboot.processor;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.core.env.ConjurPropertySource;
import com.cyberark.conjur.springboot.domain.ConjurProperties;

/**
 * 
 * SpringBootConjurAutoConfiguration class attempts to automatically configure spring-boot-conjur Application.
 *
 */

@Configuration
public class SpringBootConjurAutoConfiguration {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootConjurAutoConfiguration.class);
	
	/**
	 * 
	 * @ConditionalOnMissingBean annotation is used to load a bean only if a given bean is missing.
	 * @return - conjurRetrieveSecretService instance of class ConjurRetrieveSecretService.
	 */
	
	
	@ConditionalOnMissingBean
	@Bean
	ConjurRetrieveSecretService conjurRetrieveSecretService() {
		LOGGER.debug("Creating ConjurRetrieveSecretService instance");
		return new ConjurRetrieveSecretService();
		
	}

	@ConditionalOnMissingBean
	@Bean
	ConjurValueClassProcessor conjurSecretValueClassProcessor() {
//		LOGGER.debug("Creating ConjurSecretValueClassProcessor instance");
		return new ConjurValueClassProcessor();
	}

	
	
	@ConditionalOnMissingBean
	@Bean
	ConjurValuesClassProcessor conjurValuesClassProcessor() {
//		LOGGER.debug("Creating ConjurValueClassProcessor instance");
		return new ConjurValuesClassProcessor();
	}
	


	@ConditionalOnMissingBean
	@Bean
	SecretsApi secretsApi() {
		return new SecretsApi();
	}
	
	@ConditionalOnMissingBean
	@Bean
	ConjurProperties conjurAuthParam() {
		return new ConjurProperties();
	}
	
	/**
	 * 
	 * @return conjurValueProcessor instance of class ConjurValueProcessor.
	 */
	
	//@ConditionalOnExpression(value="${conjur.cloud.enabled}")
	@ConditionalOnMissingBean(ConjurPropertySource.class)
	@Bean
	 ConjurValueProcessor conjurValueProcessor()
	{
		LOGGER.debug("Creating ConjurValueProcessor instance");
		return new ConjurValueProcessor();
	}

}
