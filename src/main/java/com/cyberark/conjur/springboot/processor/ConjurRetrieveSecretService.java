package com.cyberark.conjur.springboot.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.constant.ConjurConstant;

public class ConjurRetrieveSecretService {

	private static Logger logger = LoggerFactory.getLogger(ConjurRetrieveSecretService.class);

	@Autowired
	public SecretsApi api;

	public String retriveMultipleSecretsForCustomAnnotation(String[] keys) throws ApiException {

		Object result = null;

		SecretsApi api = new SecretsApi();
		StringBuilder kind = new StringBuilder("");
		for (int i = 0; i <= keys.length; i++) {
			if (i < keys.length - 1) {
				kind.append("" + ConjurConstant.CONJUR_ACCOUNT + ":variable:" + keys[i] + ",");
			} else if (i == keys.length - 1) {
				kind.append("" + ConjurConstant.CONJUR_ACCOUNT + ":variable:" + keys[i] + "");
			}
		}
		result = api.getSecrets(new String(kind));
		return result.toString();
	}

	public String retriveSingleSecretForCustomAnnotation(String key) throws ApiException {
		String result = null;

		try {
			result = api.getSecret(ConjurConstant.CONJUR_ACCOUNT, ConjurConstant.CONJUR_KIND, key);
		} catch (ApiException e) {
			logger.error(e.getMessage());
		}
		return result;
	}

}