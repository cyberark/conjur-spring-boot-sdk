package com.cyberark.conjur.springboot.constant;

/**
 * 
 * Conjur Spring boot plugin constant file.
 *
 */
public class ConjurConstant {

	/**
	 * Generic extension for properties define at conjur.properties.
	 */
	public static final String CONJUR_MAPPING = "conjur.mapping.";

	/**
	 * Conjur_Kind.
	 */
	public static final String CONJUR_KIND = "variable";

	/**
	 * Custom property file name.
	 */
	public static final String CONJUR_PROPERTIES = "/conjur.properties";

	/**
	 * Environment variable.
	 */
	public static final String CONJUR_ACCOUNT = System.getenv("CONJUR_ACCOUNT");

	/**
	 * Error Message.
	 */
	public static final String CONJUR_APIKEY_ERROR = "Please provide Conjur Authn Token file or else api Key in environment Variable";

	/**
	 * Not Found message.
	 */
	public static final String NOT_FOUND = "notFound";

	/**
	 * The constant CONJUR_PREFIX.
	 */
	public static final String CONJUR_PREFIX = "conjur";
	/**
	 * The constant SPRING_VAR
	 */
	public static final String SPRING_VAR = "spring";
	/**
	 * The constant SERVER_VAR
	 */
	public static final String SERVER_VAR = "server";
	/**
	 * The constant ERROR
	 */
	public static final String ERROR = "error";
	/**
	 * The constant SPRING_UTIL
	 */
	public static final String SPRING_UTIL = "SPRING_UTIL";

	/**
	 * The constant ACTUATOR_PREFIX.
	 */
	public static final String ACTUATOR_PREFIX = "management";

	/**
	 * The constant LOGGING_PREFIX.
	 */
	public static final String LOGGING_PREFIX = "logging";

	/**
	 * The constant KUBERNETES_PREFIX.
	 */
	public static final String KUBERNETES_PREFIX = "kubernetes";
	
}
