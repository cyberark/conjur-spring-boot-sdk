package com.cyberark.conjur.springboot.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * Container annotation that aggregates several {@link ConjurPropertySource}
 * annotations.
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(Registrar.class)
@Documented
public @interface ConjurPropertySources {

	ConjurPropertySource[] value();
}