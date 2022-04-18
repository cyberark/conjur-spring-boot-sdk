package com.cyberark.conjur.springboot.constant;

/**
 * 
 * Conjur Spring boot api plugin constant file.
 *
 */
public class ConjurConstant {

	public static final String CONJUR_MAPPING = "conjur.mapping.";

	public static final String CONJUR_KIND = "variable";

	public static final String CONJUR_PROPERTIES = "/conjur.properties";

	public static final String CONJUR_ACCOUNT = System.getenv("CONJUR_ACCOUNT");
	
	public static final String NOT_FOUND = "notFound";
	
	public static final String VALUE = "value";
	
	public static final String NAME = "name";


	private ConjurConstant() {
		super();
	}
	
}
