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
	public static final String CONJUR_APIKEY_ERROR="Please provide Conjur Authn Token file or else api Key in environment Variable";

	
	/**
	 * Not Found message.
	 */
	public static final String NOT_FOUND = "notFound";

}
