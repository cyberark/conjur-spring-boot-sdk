
package com.cyberark.conjur.springboot.processor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;

import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.springboot.annotations.ConjurValue;
import com.cyberark.conjur.springboot.annotations.ConjurValues;
import com.cyberark.conjur.springboot.core.env.ConjurConfig;

/**
 * 
 * Annotation ConjurValues class processor.
 *
 */
public class ConjurValueClassProcessor implements BeanPostProcessor {

	private final ConjurRetrieveSecretService conjurRetrieveSecretService;
	
	private ConjurConfig conjurConfig;

	private static final Logger LOGGER = LoggerFactory.getLogger(ConjurValueClassProcessor.class);

	public ConjurValueClassProcessor(ConjurRetrieveSecretService conjurRetrieveSecretService,ConjurConfig conjurConfig) {
		this.conjurRetrieveSecretService = conjurRetrieveSecretService;
		this.conjurConfig= conjurConfig;

	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

		Class<?> managedBeanClass = bean.getClass();

		/*
		 * Replaced getFieldsListWithAnnotation(managedBeanClass, ConjurValue.class)
		 * with getAllFieldsList(managedBeanClass) so that it accepts both ConjurValue
		 * and ConjurValues annotaions for standalone
		 */
		List<Field> fieldList = FieldUtils.getAllFieldsList(managedBeanClass);
		
		for (Field field : fieldList) {
			if (field.isAnnotationPresent(ConjurValue.class)) {
				ReflectionUtils.makeAccessible(field);
				String credentialId = field.getDeclaredAnnotation(ConjurValue.class).key();
				String credentialToMap = conjurConfig.mapProperty(credentialId);
				if (StringUtils.isNotBlank(credentialToMap)) {
					credentialId = credentialToMap;
				}
				byte[] result;
				try {
					result = conjurRetrieveSecretService.retriveSingleSecretForCustomAnnotation(credentialId);
					try {
						if (result != null)
							field.set(bean, result);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						LOGGER.error("Error setting field value " + e.getMessage());
					}
				} catch (ApiException ex) {
					LOGGER.error(
							"Error fecting secrets using ConjurValue(single) custom annotation " + ex.getMessage());
					return ex.getMessage();
				}
			} else if (field.isAnnotationPresent(ConjurValues.class)) {
				LOGGER.info("inside else if ");

				ReflectionUtils.makeAccessible(field);
				String[] credentialsArr = field.getDeclaredAnnotation(ConjurValues.class).keys();
				List<String> credentialsList = new ArrayList<>();
				String credentialToMap = "";
				for (String key : credentialsArr) {
					credentialToMap = conjurConfig.mapProperty(key);
					if (StringUtils.isNotBlank(credentialToMap)) {
						credentialsList.add(credentialToMap);
					}
				}
				byte[] result = null;
				try {
					if (!credentialsList.isEmpty()) {
						String[] credentialArr = credentialsList.toArray(new String[0]);
						result = conjurRetrieveSecretService.retriveMultipleSecretsForCustomAnnotation(credentialArr);
						try {
							field.set(bean, result);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							LOGGER.error("Error setting field value " + e.getMessage());
						}
					}
				} catch (ApiException ex) {
					LOGGER.error("Error fecting secrets  using ConjurValues (bulk/multiple) custom annoation "
							+ ex.getMessage());
					if (ex.getCode() == 404 || ex.getMessage().equalsIgnoreCase("Not Found")) {
						if (!credentialsList.isEmpty()) {
							String[] credentialArr = credentialsList.toArray(new String[0]);
							ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
							String secretVal = "";
							if (credentialArr.length > 0) {
								try {
									result = conjurRetrieveSecretService
											.retriveSingleSecretForCustomAnnotation(credentialArr[0]);
									if (result != null) {
										outputStream.write(result);
									}
									for (int i = 1; i < credentialArr.length; i++) {
										result = conjurRetrieveSecretService
												.retriveSingleSecretForCustomAnnotation(credentialArr[i]);
										if (result != null) {
											secretVal = "," + new String(result);
											outputStream.write(secretVal.getBytes());
										}
									}
								} catch (ApiException | IOException e) {
									LOGGER.error("Error while using ConjurValues custom annoation " + ex.getMessage());
								}
							}
							byte[] finalResult = outputStream.toByteArray();
							if (finalResult.length > 0) {
								try {
									field.set(bean, finalResult);
								} catch (IllegalArgumentException | IllegalAccessException e) {
									LOGGER.error("Error while adding result for ConjurValues custom annoation "
											+ ex.getMessage());
								}
							}
						}
					}
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