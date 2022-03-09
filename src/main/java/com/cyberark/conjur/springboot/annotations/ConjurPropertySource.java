package com.cyberark.conjur.springboot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
/**
 * 
 * This interface represents Custom annotation, which receives the array of different vault paths    
 * from the users in the variable defined below with name value.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ConjurPropertySources.class)
@Import(Registrar.class)
public @interface ConjurPropertySource {
	
	String name() default "";
	String[] value();
	boolean ignoreResourceNotFound() default false;
}