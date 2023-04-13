package com.cyberark.conjur.springboot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This class resolves the secret value at application load
 * time from the conjur vault.
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
		// TODO Auto-generated method stub
		this.chain = nextChain;

	}

	@Override
	public String[] getPropertyNames() {
		// TODO Auto-generated method stub
		return new String[0];
	}

	@Override
	public Object getProperty(String name) {
		// TODO Auto-generated method stub

		Object value = null;

		if (value == null) {
			value =  this.chain.getProperty(name);

		}
        LOGGER.debug("property value : " + value);
		return value;
		
	}

}
