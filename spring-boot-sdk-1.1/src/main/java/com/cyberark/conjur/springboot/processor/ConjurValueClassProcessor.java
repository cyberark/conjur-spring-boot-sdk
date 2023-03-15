
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

import com.cyberark.conjur.springboot.annotations.ConjurValue;
import com.cyberark.conjur.springboot.core.env.ConjurConnectionManager;
import com.cyberark.conjur.springboot.core.env.ConjurPropertySource;


/**
 * 
 * Annotation ConjurValues class processor.
 *
 */
@Configuration
public class ConjurValueClassProcessor implements BeanPostProcessor {

	@Autowired
	ConjurRetrieveSecretService conjurRetrieveSecretService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConjurPropertySource.class);
	

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		
		LOGGER.debug("Start : postProcessBeforeInitialization" + conjurRetrieveSecretService);

		Class<?> managedBeanClass = bean.getClass();
		ConjurConnectionManager.getInstance();

		List<Field> fieldList = FieldUtils.getFieldsListWithAnnotation(managedBeanClass, ConjurValue.class);

		for (Field field : fieldList) {
			if (field.isAnnotationPresent(ConjurValue.class)) {
				ReflectionUtils.makeAccessible(field);
				String variableId = field.getDeclaredAnnotation(ConjurValue.class).key();
				byte[] result = null;
				try {
					result = conjurRetrieveSecretService.retriveSingleSecretForCustomAnnotation(variableId);

					field.set(bean, result);
				} catch (Exception e) { 
					{
					LOGGER.error("Exception : ", e.getMessage());
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
		return null;
	}

}
