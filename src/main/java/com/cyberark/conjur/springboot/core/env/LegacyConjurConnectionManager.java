package com.cyberark.conjur.springboot.core.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyberark.conjur.api.Conjur;
import com.cyberark.conjur.api.Token;

public final class LegacyConjurConnectionManager {

	private static LegacyConjurConnectionManager conjurConnectionInstance = null;

	private static Logger logger = LoggerFactory.getLogger(LegacyConjurConnectionManager.class);

	// For Getting Connection with conjur vault using cyberark sdk
	private LegacyConjurConnectionManager() {

		getConnection();

	}

	private void getConnection() {
		try {
			Token token = Token.fromEnv();
			Conjur conjur = new Conjur(token);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}

	// Static method to create instance of class and checking for multiple threads
	public static LegacyConjurConnectionManager getInstance() {
		if (conjurConnectionInstance == null) {
			synchronized (LegacyConjurConnectionManager.class) {
				if (conjurConnectionInstance == null) {
					conjurConnectionInstance = new LegacyConjurConnectionManager();
				}
			}
		}
		return conjurConnectionInstance;
	}
}
