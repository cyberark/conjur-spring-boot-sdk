package com.cyberark.conjur.springboot.util;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConjurPropertySourceUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConjurPropertySourceUtil.class);
	
	public  void loadEnvironmentParameters(Map<String, String> newenv)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		LOGGER.info("Start loadEnvironment()");
		
		
		Class<?>[] classes = Collections.class.getDeclaredClasses();
		
		Map<String, String> env = System.getenv();
		for (Class<?> cl : classes) {
			if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
				Field field = cl.getDeclaredField("m");
				field.setAccessible(true);
				Object obj = field.get(env);
				
				@SuppressWarnings("unchecked")
				Map<String, String> map = (Map<String, String>) obj;
				map.putAll(newenv);
				
			}
		}
	}

}
