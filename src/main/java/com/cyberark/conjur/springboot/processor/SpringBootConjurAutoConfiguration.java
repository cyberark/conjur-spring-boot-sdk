package com.cyberark.conjur.springboot.processor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cyberark.conjur.sdk.AccessToken;
import com.cyberark.conjur.sdk.ApiClient;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration(proxyBeanMethods = false)
public class SpringBootConjurAutoConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(SpringBootConjurAutoConfiguration.class);
	
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
	SecretsApi secretsApi(ApiClient apiClient) {
		return new SecretsApi(apiClient);
	}
	
	@ConditionalOnMissingBean
	@Bean
	ApiClient apiClient(){
		ApiClient client = com.cyberark.conjur.sdk.Configuration.getDefaultApiClient();
		try {
			AccessToken accesToken = client.getNewAccessToken();
			if (accesToken == null) {
				logger.debug("Using Account: " + obfuscateString(client.getAccount()));
				logger.debug("Using ApplianceUrl: " + obfuscateString(client.getBasePath()));
				if (StringUtils.isNotEmpty( System.getenv().get("CONJUR_AUTHN_LOGIN"))) {
					logger.debug("Using AuthnLogin: " +obfuscateString(System.getenv().get("CONJUR_AUTHN_LOGIN")));
				}
				if (StringUtils.isNotEmpty( System.getenv().get("CONJUR_AUTHN_API_KEY"))) {
					logger.debug("Using Authn API Key: " +obfuscateString(System.getenv().get("CONJUR_AUTHN_API_KEY")));
				}
				if (StringUtils.isNotEmpty(System.getenv().get("CONJUR_SSL_CERTIFICATE"))) {
					logger.debug("Using SSL Cert: " +  obfuscateString(System.getenv().get("CONJUR_SSL_CERTIFICATE")));
				}
				else if (StringUtils.isNotEmpty(System.getenv().get("CONJUR_CERT_FILE"))){
					logger.debug("Using SSL Cert File: " +  obfuscateString((System.getenv().get("CONJUR_CERT_FILE"))));
				}
				logger.error("Access token is null, Please enter proper environment variables.");
			}
			String token = accesToken.getHeaderValue();
			client.setAccessToken(token);
			com.cyberark.conjur.sdk.Configuration.setDefaultApiClient(client);
			logger.debug("Connection with conjur is successful");
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return client;
	}
	
	@ConditionalOnMissingBean
	@Bean
	ConjurValuesClassProcessor conjurValuesClassProcessor(ConjurRetrieveSecretService conjurRetrieveSecretService) {
		return new ConjurValuesClassProcessor(conjurRetrieveSecretService);
	}

	private String obfuscateString(String str) {
		int len = str.length();
		if (len <= 2) {
			return str;
		} else {
			char first = str.charAt(0);
			char last = str.charAt(len - 1);
			String middle = "*******";
			return first + middle + last;
		}
	}
}
