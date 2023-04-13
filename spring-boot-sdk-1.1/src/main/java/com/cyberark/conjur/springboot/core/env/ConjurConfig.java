package com.cyberark.conjur.springboot.core.env;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.cyberark.conjur.springboot.constant.ConjurConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This class loads the external configured conjur.properties file and resolves
 * the keys values defined in properties file.
 *
 */
public class ConjurConfig {

	private static final Properties props = new Properties();

	private static final ConjurConfig uniqueInstance = new ConjurConfig();

	private static final Logger logger = LoggerFactory.getLogger(ConjurConfig.class);

	private ConjurConfig() {

		InputStream propsFile = ConjurConfig.class.getResourceAsStream(ConjurConstant.CONJUR_PROPERTIES);

		if (propsFile != null) {
			try {
				props.load(propsFile);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			} finally {
				try {
					propsFile.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

	}

	/**
	 * 
	 * @return unique instance of class.
	 */
	public static ConjurConfig getInstance() {
		return uniqueInstance;
	}

	/**
	 * 
	 * @param name - key define at given property file.
	 * @return - corresponding value of key defined at given property file.
	 */
	public String mapProperty(String name) {
		String mapped = props.getProperty(ConjurConstant.CONJUR_MAPPING + name);

		return mapped != null ? mapped : name;
	}
}
