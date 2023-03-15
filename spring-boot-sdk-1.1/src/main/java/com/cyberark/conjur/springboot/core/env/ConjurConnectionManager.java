package com.cyberark.conjur.springboot.core.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyberark.conjur.sdk.AccessToken;
import com.cyberark.conjur.sdk.ApiClient;
import com.cyberark.conjur.sdk.Configuration;
/**
 * 
 * This is the connection creation singleton class with conjur vault by using
 * the conjur java sdk.
 *
 */
public final class ConjurConnectionManager {

	private static ConjurConnectionManager conjurConnectionInstance = null;

	private static final Logger LOGGER = LoggerFactory.getLogger(ConjurConnectionManager.class);

	// For Getting Connection with conjur vault using cyberark sdk
	private ConjurConnectionManager() {

		getConnection();

	}
	private void getConnection() { 
		try {
			ApiClient client = Configuration.getDefaultApiClient();
			AccessToken accesToken = client.getNewAccessToken();
			if (accesToken == null) {
				LOGGER.error("Access token is null, Please enter proper environment variables.");
			}
			String token = accesToken.getHeaderValue();
			client.setAccessToken(token);
			Configuration.setDefaultApiClient(client);
			LOGGER.debug("Connection with conjur is successful");
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			
		}

	}

	/**
	 * method to create instance of class and checking for multiple threads.
	 * @return unique instance of class. 
	 */
	public static ConjurConnectionManager getInstance() {
		if (conjurConnectionInstance == null) {
			synchronized (ConjurConnectionManager.class) {
				if (conjurConnectionInstance == null) {
					conjurConnectionInstance = new ConjurConnectionManager();
				}
			}
		}
		return conjurConnectionInstance;
	}
}
