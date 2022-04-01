package com.cyberark.conjur.springboot.processor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cyberark.conjur.api.Conjur;

@Configuration

@ConditionalOnClass(LegacyConjurRetrieveSecretService.class)
public class LegacySpringBootConjurAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	LegacyConjurValueClassProcessor clegacyonjurSecretValueClassProcessor() {
		return new LegacyConjurValueClassProcessor();
	}

	@ConditionalOnMissingBean
	@Bean
	LegacyConjurRetrieveSecretService legacyConjurRetrieveSecretService() {
		return new LegacyConjurRetrieveSecretService();
	}

	@ConditionalOnMissingBean
	@Bean
	Conjur conjur() {
		return new Conjur();
	}

//	@ConditionalOnMissingBean
//	@Bean
//	ConjurValuesClassProcessor conjurValuesClassProcessor() {
//		return new ConjurValuesClassProcessor();
//	}

}
