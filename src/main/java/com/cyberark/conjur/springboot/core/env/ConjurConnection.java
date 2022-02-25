package com.cyberark.conjur.springboot.core.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyberark.conjur.sdk.AccessToken;
import com.cyberark.conjur.sdk.ApiClient;
import com.cyberark.conjur.sdk.Configuration;

public class ConjurConnection {

	private static ConjurConnection conjurConnectionInstance = null;

	private static Logger logger = LoggerFactory.getLogger(ConjurConnection.class);

	// For Getting Connection with conjur vault using cyberark sdk
	private ConjurConnection() {

		getConnection();

	}

	private void getConnection() {
		try {
			ApiClient client = Configuration.getDefaultApiClient();
			AccessToken accesToken = client.getNewAccessToken();
			if (accesToken == null) {
				logger.error("Access token is null, Please enter proper environment variables.");
			}
			String token = accesToken.getHeaderValue();
			client.setAccessToken(token);
			Configuration.setDefaultApiClient(client);
			logger.debug("Connection with conjur is successful");
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}

	// Static method to create instance of class and checking for multiple threads
	public static ConjurConnection getInstance() {
		if (conjurConnectionInstance == null) {
			synchronized (ConjurConnection.class) {
				if (conjurConnectionInstance == null) {
					conjurConnectionInstance = new ConjurConnection();
				}
			}
		}
		return conjurConnectionInstance;
	}
}
