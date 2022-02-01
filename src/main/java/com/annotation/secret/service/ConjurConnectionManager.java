package com.annotation.secret.service;

import java.io.ByteArrayInputStream;

import org.springframework.beans.factory.annotation.Value;

import com.cyberark.conjur.sdk.AccessToken;
import com.cyberark.conjur.sdk.ApiClient;
import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.sdk.Configuration;

public class ConjurConnectionManager {

	@Value("${account}")
	String account;

	@Value("${userName}")
	String userName;

	@Value("${sslCert}")
	String sslCert;

	@Value("${basepath}")
	String basepath;

	@Value("${apiKey}")
	String apiKey;

	public String connectionConfiguration() throws ApiException {
		ApiClient client = Configuration.getDefaultApiClient();
		client.setAccount(account);
		client.setApiKey(apiKey);
		client.setBasePath(basepath);
		client.setSslCaCert(new ByteArrayInputStream(sslCert.getBytes()));
		client.setUsername(userName);

		AccessToken accesToken = client.getNewAccessToken();

		if (accesToken == null) {
			System.out.println("Access token is null");
			System.exit(1);
		}

		String token = accesToken.getHeaderValue();
		client.setAccessToken(token);
		Configuration.setDefaultApiClient(client);
		return null;
	}

}
