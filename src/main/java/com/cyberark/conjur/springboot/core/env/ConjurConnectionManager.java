package com.cyberark.conjur.springboot.core.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyberark.conjur.api.Conjur;
import com.cyberark.conjur.api.Token;

/**
 * 
 * This is the connection creation singleton class with conjur vault by using
 * the conjur api java/legacy java client.
 *
 */
public final class ConjurConnectionManager {

	private static ConjurConnectionManager conjurConnectionInstance = null;

	private static Logger logger = LoggerFactory.getLogger(ConjurConnectionManager.class);

	private ConjurConnectionManager() {

		getConnection();

	}

	private void getConnection() {
		try {
			Token token = Token.fromEnv();
			new Conjur(token);
			logger.info("Connection with Conjur is successful");
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}

	/**
	 * method to create instance of class and checking for multiple threads.
	 * 
	 * @return unique instance of class.
	 */
	public static synchronized ConjurConnectionManager getInstance() {
		if (conjurConnectionInstance == null) {

			conjurConnectionInstance = new ConjurConnectionManager();
		}

		return conjurConnectionInstance;
	}
}
