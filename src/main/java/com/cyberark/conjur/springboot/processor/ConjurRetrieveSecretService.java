package com.cyberark.conjur.springboot.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.constant.ConjurConstant;

public class ConjurRetrieveSecretService {

	private static Logger logger = LoggerFactory.getLogger(ConjurRetrieveSecretService.class);

	private SecretsApi secretsApi ;
	

	public String retriveMultipleSecretsForCustomAnnotation(String[] keys) throws ApiException {

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
		result = secretsApi.getSecrets(new String(kind));
		return result.toString();
	}

	public String retriveSingleSecretForCustomAnnotation(String key) throws ApiException {
		String result = null;
		secretsApi = new SecretsApi();
		try {
			result = secretsApi.getSecret(ConjurConstant.CONJUR_ACCOUNT, ConjurConstant.CONJUR_KIND, key);
		} catch (ApiException e) {
			logger.error(e.getMessage());
		}
		return result;
	}

}