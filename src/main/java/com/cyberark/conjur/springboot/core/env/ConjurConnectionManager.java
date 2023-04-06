package com.cyberark.conjur.springboot.core.env;

import org.apache.commons.lang3.StringUtils;
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

	private static final Logger logger = LoggerFactory.getLogger(ConjurConnectionManager.class);

	// For Getting Connection with conjur vault using cyberark sdk
	private ConjurConnectionManager() {

		getConnection();

	}

	private void getConnection() { 
		ApiClient client = Configuration.getDefaultApiClient();
		AccessToken accesToken = client.getNewAccessToken();
		if (accesToken == null) {
			logger.debug("Using Account: " + obfuscateString(client.getAccount()));
			logger.debug("Using ApplianceUrl: " + obfuscateString(client.getBasePath()));
			if (StringUtils.isNotEmpty( System.getenv().get("CONJUR_AUTHN_LOGIN"))) {
				logger.debug("Using AuthnLogin: " +obfuscateString(System.getenv().get("CONJUR_AUTHN_LOGIN")));
			}
			if (StringUtils.isNotEmpty( System.getenv().get("CONJUR_AUTHN_API_KEY"))) {
				logger.debug("Using Authn API Key: " +obfuscateString(System.getenv().get("CONJUR_AUTHN_API_KEY")));
			}
			if (StringUtils.isNotEmpty(System.getenv().get("CONJUR_SSL_CERTIFICATE"))) {
				logger.debug("Using SSL Cert: " +  obfuscateString(System.getenv().get("CONJUR_SSL_CERTIFICATE")));
			}
			else if (StringUtils.isNotEmpty(System.getenv().get("CONJUR_CERT_FILE"))){
				logger.debug("Using SSL Cert File: " +  obfuscateString((System.getenv().get("CONJUR_CERT_FILE"))));
			}
			throw new IllegalArgumentException("Access token is null, Please enter proper environment variables.");
		}
		String token = accesToken.getHeaderValue();
		client.setAccessToken(token);
		Configuration.setDefaultApiClient(client);
		logger.debug("Connection with conjur is successful");
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

	private String obfuscateString(String str) {
		int len = str.length();
		if (len <= 2) {
			return str;
		} else {
			char first = str.charAt(0);
			char last = str.charAt(len - 1);
			String middle = "*******";
			return first + middle + last;
		}
	}
}
