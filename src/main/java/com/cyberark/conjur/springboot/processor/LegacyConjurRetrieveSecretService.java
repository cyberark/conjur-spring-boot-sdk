package com.cyberark.conjur.springboot.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyberark.conjur.api.Conjur;

public class LegacyConjurRetrieveSecretService {

	private static Logger logger = LoggerFactory.getLogger(LegacyConjurRetrieveSecretService.class);

	private Conjur conjur;

//	public byte[] retriveMultipleSecretsForCustomAnnotation(String[] keys) throws ApiException {
//		Object result = null;
//		conjur = new Conjur();
//		StringBuilder kind = new StringBuilder("");
//		for (int i = 0; i <= keys.length; i++) {
//			if (i < keys.length - 1) {
//				kind.append("" + ConjurConstant.CONJUR_ACCOUNT + ":variable:" + keys[i] + ",");
//			} else if (i == keys.length - 1) {
//				kind.append("" + ConjurConstant.CONJUR_ACCOUNT + ":variable:" + keys[i] + "");
//			}
//		}
//		try {
//			result = secretsApi.getSecrets(new String(kind));
//		} catch (ApiException e) {
//			logger.error(e.getMessage());
//		}
//		return processMultipleSecretResult(result);
//
//	}

	public byte[] retriveSingleSecretForCustomAnnotation(String key) throws Exception {
		byte[] result = null;
		Conjur conjur = new Conjur();
		 result = conjur.variables().retrieveSecret(key).getBytes();
		return result;
	}

//	private byte[] processMultipleSecretResult(Object result) {
//		Map<String, String> map = new HashMap<String, String>();
//		String[] parts = result.toString().split(",");
//		{
//			for (int j = 0; j < parts.length; j++) {
//				String[] splitted = parts[j].split("[:/=]");
//
//				for (int i = 0; i < splitted.length; i++) {
//					map.put(splitted[3], splitted[4]);
//				}
//			}
//		}
//		return map.toString().getBytes();
//	}

}