package com.cyberark.conjur.springboot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
/**
 * 
 * This interface represents Custom annotation, which receives the array of different vault paths.    
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(Registrar.class)
public @interface ConjurPropertySources {

	ConjurPropertySource[] value();
}