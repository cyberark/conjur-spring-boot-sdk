
package com.cyberark.conjur.springboot.processor;

import java.lang.reflect.Field;
import java.util.List;

import javax.annotation.Nullable;

import com.cyberark.conjur.springboot.annotations.ConjurValue;
import com.cyberark.conjur.springboot.core.env.ConjurPropertySource;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;
/**
 * 
 * Annotation ConjurValues class processor.
 *
 */
public class ConjurValueClassProcessor implements BeanPostProcessor {

	private final ConjurRetrieveSecretService conjurRetrieveSecretService;

	private static Logger logger = LoggerFactory.getLogger(ConjurPropertySource.class);

	public ConjurValueClassProcessor(ConjurRetrieveSecretService conjurRetrieveSecretService) {
		this.conjurRetrieveSecretService = conjurRetrieveSecretService;
	}


	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

		Class<?> managedBeanClass = bean.getClass();

		List<Field> fieldList = FieldUtils.getFieldsListWithAnnotation(managedBeanClass, ConjurValue.class);

		for (Field field : fieldList) {
			if (field.isAnnotationPresent(ConjurValue.class)) {
				ReflectionUtils.makeAccessible(field);
				String variableId = field.getDeclaredAnnotation(ConjurValue.class).key();
				byte[] result;
				try {
					result = conjurRetrieveSecretService.retriveSingleSecretForCustomAnnotation(variableId);

					field.set(bean, result);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}

			}

		}

		return bean;
	}

	@Nullable
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
