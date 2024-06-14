package com.cyberark.conjur.springboot.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.constant.ConjurConstant;
import com.cyberark.conjur.springboot.core.env.ConjurConfig;
import com.cyberark.conjur.springboot.core.env.ConjurConnectionManager;
import com.google.gson.Gson;

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
	
	private ConjurConfig conjurConfig;
	
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
	
	public void setConjurConfig(ConjurConfig conjurConfig) {
		this.conjurConfig = conjurConfig;
	}
	
	@Override
	public Object getProperty(String key) {
		StringBuilder kind = new StringBuilder();
		Gson gson = new Gson();
		Object secretValue = null;
		
		List<Object> list = new ArrayList<Object>();
		key = conjurConfig.mapProperty(key);
		if (!(key.startsWith(ConjurConstant.SPRING_VAR)) && !(key.startsWith(ConjurConstant.SERVER_VAR))
				&& !(key.startsWith(ConjurConstant.ERROR)) && !(key.startsWith(ConjurConstant.SPRING_UTIL))
				&& !(key.startsWith(ConjurConstant.CONJUR_PREFIX)) && !(key.startsWith(ConjurConstant.ACTUATOR_PREFIX))
				&& !(key.startsWith(ConjurConstant.LOGGING_PREFIX))
				&& !(key.startsWith(ConjurConstant.KUBERNETES_PREFIX))) {
			String account = ConjurConnectionManager.getAccount(secretsApi);

			/*
			 * Included the below code for Bulk Retrieval using @Value annotation to check
			 * if there are more than one key is being fetched using , separated value
			 */
			if (key.contains(",")) {
				String[] keys = key.split(",");
				String credentialId = "";
				if (keys.length > 0) {
					credentialId = key = conjurConfig.mapProperty(keys[0]);
					kind.append(account + ":variable:" + credentialId); 
					for (int i = 1; i < keys.length; i++) {
						credentialId = conjurConfig.mapProperty(keys[i]);
						kind.append("," + account+ ":variable:" + keys[i]);
					}
				}
				try {
					secretValue = gson.toJson(secretsApi.getSecrets(new String(kind)), Object.class);
				} catch (ApiException ex) {
					LOGGER.error("Status code CustomPropery: " + ex.getCode());
					LOGGER.error("Reason: " + ex.getResponseBody());
					LOGGER.error(ex.getMessage());
					if(ex.getCode() == 404 || ex.getMessage().equalsIgnoreCase("Not Found")) {
					for (int i = 0; i < keys.length; i++) {
						try {
							credentialId = conjurConfig.mapProperty(keys[i]);
							secretValue = secretsApi.getSecret(account, ConjurConstant.CONJUR_KIND, credentialId);
							if (secretValue != null) {
								list.add(secretValue);
							}
						} catch (ApiException e) {
							LOGGER.error("Status code CustomPropery: " + ex.getCode());
							LOGGER.error("Reason: " + ex.getResponseBody());
							LOGGER.error(ex.getMessage());
						}
					}
					secretValue = gson.toJson(list.toArray(new Object[list.size()]), Object.class);
				}
			}
			}else {
				try {
					secretValue = secretsApi.getSecret(account, ConjurConstant.CONJUR_KIND, key);
				} catch (ApiException ex) {
					LOGGER.error("Status code: " + ex.getCode());
					LOGGER.error("Reason: " + ex.getResponseBody());
					LOGGER.error(ex.getMessage());
				}
			}

		}

		return secretValue;
	}

}
