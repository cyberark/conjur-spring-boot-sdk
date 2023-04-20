package com.cyberark.conjur.springboot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This class resolves the secret value at application load time from the conjur
 * vault.
 *
 */

public class DefaultPropertySourceChain extends PropertyProcessorChain {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPropertySourceChain.class);

	private PropertyProcessorChain chain;

	public DefaultPropertySourceChain(String name) {

		super("defaultPropertySource");
		LOGGER.debug("Calling Defaultropertysource Chain");

	}

	@Override
	public void setNextChain(PropertyProcessorChain nextChain) {

		this.chain = nextChain;

	}

	@Override
	public String[] getPropertyNames() {

		return new String[0];
	}

	@Override
	public Object getProperty(String name) {

		Object value = null;

		if (value == null) {
			value = this.chain.getProperty(name);

		}

		return value;

	}

}
