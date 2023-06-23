package com.cyberark.conjur.springboot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.constant.ConjurConstant;
import com.cyberark.conjur.springboot.core.env.ConjurConfig;
import com.cyberark.conjur.springboot.core.env.ConjurConnectionManager;

/**
 * 
 * This custom class resolves the secret value at application load time from the
 * conjur vault.
 *
 */

public class CustomPropertySourceChain extends PropertyProcessorChain {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomPropertySourceChain.class);

	private PropertyProcessorChain chain;

	private SecretsApi secretsApi;

	public CustomPropertySourceChain(String name) {
		super("customPropertySource");
	
			LOGGER.debug("Calling CustomPropertysource Chain ");
		
	}

	@Override
	public void setNextChain(PropertyProcessorChain nextChain) {
		this.chain = nextChain;

	}

	public void setSecretsApi(SecretsApi secretsApi) {
		this.secretsApi = secretsApi;
	}

	@Override
	public String[] getPropertyNames() {

		return new String[0];
	}

	@Override
	public Object getProperty(String key) {

		byte[] result = null;

		key = ConjurConfig.getInstance().mapProperty(key);

		if (!(key.startsWith(ConjurConstant.SPRING_VAR)) && !(key.startsWith(ConjurConstant.SERVER_VAR))
				&& !(key.startsWith(ConjurConstant.ERROR)) && !(key.startsWith(ConjurConstant.SPRING_UTIL))
				&& !(key.startsWith(ConjurConstant.CONJUR_PREFIX)) && !(key.startsWith(ConjurConstant.ACTUATOR_PREFIX))
				&& !(key.startsWith(ConjurConstant.LOGGING_PREFIX)) && !(key.startsWith(ConjurConstant.KUBERNETES_PREFIX))) {
			try {
				String account = ConjurConnectionManager.getAccount(secretsApi);
				String secretValue = secretsApi.getSecret(account, ConjurConstant.CONJUR_KIND, key);
				result = secretValue != null ? secretValue.getBytes() : null;
			} catch (ApiException ae) {
				logger.warn("Failed to get property from Conjur for: " + key);
				logger.warn("Reason: " + ae.getResponseBody());
				logger.warn(ae.getMessage());

			}
		}
		return result;

	}
}
