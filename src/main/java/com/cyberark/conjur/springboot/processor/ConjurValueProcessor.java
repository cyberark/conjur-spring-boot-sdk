package com.cyberark.conjur.springboot.processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import com.cyberark.conjur.springboot.constant.ConjurConstant;
import com.cyberark.conjur.springboot.domain.ConjurProperties;
import com.cyberark.conjur.springboot.service.CustomPropertySourceChain;
import com.cyberark.conjur.springboot.service.DefaultPropertySourceChain;
import com.cyberark.conjur.springboot.service.PropertyProcessorChain;



/**
 * The ValueProcess class will be invoked on boot strap of the applicaiton and
 * will invoke the process chain based on the properties. It call the default
 * property chain if value is found or will call the Custome propertysource to
 * retrieve the value from the Conjur vault . This class in turn will invoke the
 * ConjurPropertySource to autowire the value for @Value annotation
 * 
 *
 */

@Configuration

public class ConjurValueProcessor implements BeanPostProcessor, InitializingBean, EnvironmentAware, ApplicationContextAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConjurValueProcessor.class);

	private ApplicationContext context;

	private ConfigurableEnvironment environment;

	@Autowired
	private ConjurProperties conjurProperties;
	

	private PropertyProcessorChain processorChain;
	
   

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		{

		LOGGER.info("Start : afterPropertiesSet" + conjurProperties);}
		
		this.processorChain = new DefaultPropertySourceChain("DefaultPropertySource");
		CustomPropertySourceChain customPS = new CustomPropertySourceChain("CustomPropertySource");
		processorChain.setNextChain(customPS);
		
		environment.getPropertySources().addLast(processorChain);
		
		LOGGER.info("End :afterPropertiesSet" + environment.getPropertySources());
		

	}

	@Override
	public void setEnvironment(Environment environment) {

		if (environment instanceof ConfigurableEnvironment) {

			this.environment = (ConfigurableEnvironment) environment;
			{
			LOGGER.debug("Available environment>>>" + environment);
			}
		}

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

		this.context = applicationContext;

	}
	
	


}
