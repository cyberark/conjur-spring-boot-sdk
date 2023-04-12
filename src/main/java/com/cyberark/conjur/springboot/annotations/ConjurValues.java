package com.cyberark.conjur.springboot.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The @ConjurValues annotation is a Conjur native annotation intended for new Spring Boot applications.
 * Injecting the annotation into your Spring Boot code allows you to retrieve multiple secrets from the Conjur Vault by passing the complete path 
 * to multiple keys in comma separated format.
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.FIELD)
public @interface ConjurValues {
	/**
	 * Indicates the complete path for multiple keys.
	 * @return - An array of keys.
	 */
	String[] keys();
}
