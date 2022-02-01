package com.annotation.config.processor;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

import com.annotation.config.annotations.ConjurValue;
import com.annotation.config.annotations.ConjurValues;
import com.annotation.secret.service.ConjurConnectionManager;
import com.annotation.secret.service.ConjurRetrieveSecretManager;
import com.cyberark.conjur.sdk.ApiException;


@Configuration
public class ConjurValueClassProcessor implements BeanPostProcessor {

	@Autowired
	ConjurRetrieveSecretManager conjurRetrieveSecretManager;

	@Autowired
	private  ConjurConnectionManager conjurConnectionCall;


	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

		
		Class<?> managedBeanClass = bean.getClass();

		List<Field> fieldList = FieldUtils.getFieldsListWithAnnotation(managedBeanClass, ConjurValue.class);
		try {
			conjurConnectionCall.connectionConfiguration();
		} catch (ApiException e2) {
			e2.printStackTrace();
		}
		for (Field field : fieldList) {
			if (field.isAnnotationPresent(ConjurValue.class)) {
				ReflectionUtils.makeAccessible(field);
				String variableId = field.getDeclaredAnnotation(ConjurValue.class).key();
				String result = null;
				try {
					result = conjurRetrieveSecretManager.retriveSecret(variableId);
				} catch (ApiException e1) {
					e1.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					field.set(bean, result);
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				}

			}
		}

		return bean;
	}

}