package com.cyberark.conjur.springboot.processor;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.constant.ConjurConstant;
import com.cyberark.conjur.springboot.core.env.ConjurConnectionManager;

public class ConjurRetrieveSecretService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConjurRetrieveSecretService.class);

	private final SecretsApi secretsApi;

	public ConjurRetrieveSecretService(SecretsApi secretsApi) {
		this.secretsApi = secretsApi;
	}

	/**
	 * This method retrieves multiple secrets for custom annotation's keys.
	 *
	 * @param keys - query to vault.
	 * @return secrets - output from the vault.
	 */
	public byte[] retriveMultipleSecretsForCustomAnnotation(String[] keys) {

		Object result = null;
		StringBuilder kind = new StringBuilder();

		for (int i = 0; i <= keys.length; i++) {
			if (i < keys.length - 1) {
				kind.append("" + ConjurConstant.CONJUR_ACCOUNT + ":variable:" + keys[i] + ",");
			} else if (i == keys.length - 1) {
				kind.append("" + ConjurConstant.CONJUR_ACCOUNT + ":variable:" + keys[i] + "");
			}
		}
		try {
			result = secretsApi.getSecrets(new String(kind));
		} catch (ApiException e) {
			LOGGER.error("Status code: " + e.getCode());
			LOGGER.error("Reason: " + e.getResponseBody());
			LOGGER.error(e.getMessage());
		}
		return processMultipleSecretResult(result);

	}

	/**
	 * This method retrieves single secret for custom annotation's key value.
	 * 
	 * @param key - query to vault.
	 * @return secrets - output from the vault.
	 */
	public byte[] retriveSingleSecretForCustomAnnotation(String key) {
		byte[] result = null;
		try {
			String account = ConjurConnectionManager.getAccount(secretsApi);
			String secret = secretsApi.getSecret(account, ConjurConstant.CONJUR_KIND, key);
			result = secret != null
					? secret.getBytes()
					: null;
		} catch (ApiException e) {
			LOGGER.error("Status code: " + e.getCode());
			LOGGER.error("Reason: " + e.getResponseBody());
			LOGGER.error(e.getMessage());
		}
		return result;
	}

	private byte[] processMultipleSecretResult(Object result) {
		Map<String, String> map = new HashMap<String, String>();
		String[] parts = result.toString().split(",");
		{
			for (int j = 0; j < parts.length; j++) {
				String[] splitted = parts[j].split("[:/=]");

				for (int i = 0; i < splitted.length; i++) {
					map.put(splitted[3], splitted[4]);
				}
			}
		}
		return map.toString().getBytes();
	}

}
