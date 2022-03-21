package com.cyberark.conjur.springboot.constant;
/**
 * 
 * Conjur Spring boot plugin constant file.
 *
 */
public class ConjurConstant {

	public static final String CONJUR_MAPPING = "conjur.mapping.";

	public static final String CONJUR_KIND = "variable";

	public static final String CONJUR_PROPERTIES = "/conjur.properties";

	public static final String CONJUR_ACCOUNT = System.getenv("CONJUR_ACCOUNT");
	
	public static final String CONJUR_APIKEY_ERROR="Please provide Conjur Authn Token file or else api Key in environment Variable";

	public static final String NOT_FOUND = "notFound";

}
