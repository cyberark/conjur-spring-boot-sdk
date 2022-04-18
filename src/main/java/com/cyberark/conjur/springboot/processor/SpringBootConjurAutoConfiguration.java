package com.cyberark.conjur.springboot.processor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cyberark.conjur.api.Conjur;

@Configuration

@ConditionalOnClass(ConjurRetrieveSecretService.class)
public class SpringBootConjurAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	ConjurValueClassProcessor legacyconjurSecretValueClassProcessor() {
		return new ConjurValueClassProcessor();
	}

	@ConditionalOnMissingBean
	@Bean
	ConjurRetrieveSecretService conjurRetrieveSecretService() {
		return new ConjurRetrieveSecretService();
	}

	@ConditionalOnMissingBean
	@Bean
	Conjur conjur() {
		return new Conjur();
	}

	@ConditionalOnMissingBean
	@Bean
	ConjurValuesClassProcessor conjurValuesClassProcessor() {
		return new ConjurValuesClassProcessor();
	}

}
