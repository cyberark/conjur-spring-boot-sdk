package com.cyberark.conjur.springboot.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 
 * This interface represents the custom annotation which takes the array of keys as input and 
 * returns the secret values from the conjur vault.
 *
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConjurValue {
	String key();

	String kind() default "variable";
}
