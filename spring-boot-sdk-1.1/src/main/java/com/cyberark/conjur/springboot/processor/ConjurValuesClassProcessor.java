package com.cyberark.conjur.springboot.processor;

import java.lang.reflect.Field;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import com.cyberark.conjur.springboot.annotations.ConjurValues;
import com.cyberark.conjur.springboot.core.env.ConjurConnectionManager;
import com.cyberark.conjur.springboot.core.env.ConjurPropertySource;

/**
 * 
 * Custom annotation ConjurValues class processor.
 *
 */
@Configuration
public class ConjurValuesClassProcessor implements BeanPostProcessor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConjurPropertySource.class);

	@Autowired
	ConjurRetrieveSecretService conjurRetrieveSecretService;


	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		
		LOGGER.debug("Start : postProcessBeforeInitialization" + conjurRetrieveSecretService);

		Class<?> managedBeanClass = bean.getClass();

		List<Field> fieldList = FieldUtils.getFieldsListWithAnnotation(managedBeanClass, ConjurValues.class);

		ConjurConnectionManager.getInstance();

		for (Field field : fieldList) {
			if (field.isAnnotationPresent(ConjurValues.class)) {
				ReflectionUtils.makeAccessible(field);
				String[] variableId = field.getDeclaredAnnotation(ConjurValues.class).keys();
				byte[] result = null;
				try {
					result = conjurRetrieveSecretService.retriveMultipleSecretsForCustomAnnotation(variableId);

					field.set(bean, result);

				} catch (Exception e1) {
					{
					LOGGER.error("Exception : ", e1.getMessage());
					}
				}

			}
		}
		
		{
			LOGGER.debug("End  : postProcessBeforeInitialization ");
		}

		return bean;
	}

	@Nullable
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		// TODO Auto-generated method stub
		return null;
	}
}