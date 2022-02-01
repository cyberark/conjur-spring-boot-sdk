package com.annotation.secret.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StopWatch;

import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;

public class ConjurRetrieveSecretManager {

	@Value("${account}")
	private String accountName;

	@Autowired
	public SecretsApi api;

	public String retriveMultipleSecrets(String[] keys) throws ApiException {
		Object result = null;
		StopWatch timeMeasure = new StopWatch();
		timeMeasure.start("Task");

		SecretsApi api = new SecretsApi();
		StringBuilder kind = new StringBuilder("");
		for (int i = 0; i <= keys.length; i++) {
			if (i < keys.length - 1) {
				kind.append("" + accountName + ":variable:" + keys[i] + ",");
			} else if (i == keys.length - 1) {
				kind.append("" + accountName + ":variable:" + keys[i] + "");
			}
		}
		result = api.getSecrets(new String(kind));
		timeMeasure.stop();
		System.out
				.println("Total time for execution  for multiple keys in sec -->" + timeMeasure.getTotalTimeSeconds());
		return result.toString();
	}

	public String retriveSecret(String key) throws ApiException {
		StopWatch timeMeasure = new StopWatch();
		timeMeasure.start("Task");
		String result = null;
		String kind = "variable";
		try {
			result = api.getSecret(accountName, kind, key);
		} catch (ApiException e) {
			e.printStackTrace();
		}
		timeMeasure.stop();
		System.out.println("Total time for execution  for single key in sec-->" + timeMeasure.getTotalTimeSeconds());
		return result;
	}

}
