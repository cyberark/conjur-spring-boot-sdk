package com.cyberark.conjur.springboot.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
/**
 * 
 * This interface represents the custom annotation which takes the key as input and 
 * returns the secret value from the conjur vault.
 *
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.FIELD)
public @interface ConjurValues {

	String[] keys();

}
