package com.cyberark.conjur.springboot.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * The @ConjurPropertySources annotation allows you to specify the root of a single or multiple policies to look up. 
 * The Spring Boot Plugin routes the look up to Conjur through the Conjur Spring Boot SDK and a REST API that we expose.
 * Using @ConjurPropertySources in conjunction with @Configuration classes is required.
 * The names of secrets, passwords, and user IDs all remain as originally specified.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(Registrar.class)
@Documented
public @interface ConjurPropertySources {

	ConjurPropertySource[] value();
}
