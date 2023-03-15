package com.cyberark.conjur.springboot.processor;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.constant.ConjurConstant;

public class ConjurRetrieveSecretService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConjurRetrieveSecretService.class);

	private SecretsApi secretsApi;
	

	/**
	 * This method retrieves multiple secrets for custom annotation's keys.
	 * 
	 * @param keys - query to vault.
	 * @return secrets - output from the vault.
	 * @throws ApiException - Exception thrown from conjur java sdk.
	 */
	public byte[] retriveMultipleSecretsForCustomAnnotation(String[]... keys) throws ApiException {

		Object result = null;
		secretsApi = new SecretsApi();
		StringBuilder kind = new StringBuilder("");
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
			{
			LOGGER.error("Exception : ", e.getMessage());
			}
		}
		return processMultipleSecretResult(result);

	}

	/**
	 * This method retrieves single secret for custom annotation's key value.
	 * 
	 * @param key - query to vault.
	 * @return secrets - output from the vault.
	 * @throws ApiException - Exception thrown from conjur java sdk.
	 */
	public byte[] retriveSingleSecretForCustomAnnotation(String key) throws ApiException {
		byte[] result = null;
		secretsApi = new SecretsApi();
		try {
			result = secretsApi.getSecret(ConjurConstant.CONJUR_ACCOUNT, ConjurConstant.CONJUR_KIND, key) != null
					? secretsApi.getSecret(ConjurConstant.CONJUR_ACCOUNT, ConjurConstant.CONJUR_KIND, key).getBytes()
					: null; 
		} catch (ApiException e) {
			{
			LOGGER.error("Exception : ", e.getMessage());
			}
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
