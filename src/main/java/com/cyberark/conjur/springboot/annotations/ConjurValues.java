package com.cyberark.conjur.springboot.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation providing a convenient and declarative mechanism for adding a
 * {@link ConjurValues} to Spring Boot Application.
 *
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.FIELD)
public @interface ConjurValues {

	/**
	 * Indicates the complete path for multiple keys.
	 * 
	 */
	String[] keys();

}
