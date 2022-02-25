package com.cyberark.conjur.springboot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ConjurPropertySources.class)
@Import(Registrar.class)
public @interface ConjurPropertySource {
	
	String name() default "";
	String[] value();
	boolean ignoreResourceNotFound() default false;
}