
package com.cyberark.conjur.springboot.processor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nullable;

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

	private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	private static final Logger LOGGER = LoggerFactory.getLogger(ConjurValueClassProcessor.class);

	public ConjurValueClassProcessor(ConjurRetrieveSecretService conjurRetrieveSecretService,
			ConjurConfig conjurConfig) {
		this.conjurRetrieveSecretService = conjurRetrieveSecretService;
		this.conjurConfig = conjurConfig;
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

			String credentialId;
			String credentialToMap;
			List<String> credentialsList = new ArrayList<>();

			if (field.isAnnotationPresent(ConjurValue.class)) {
				ReflectionUtils.makeAccessible(field);
				credentialId = field.getDeclaredAnnotation(ConjurValue.class).key();
				credentialToMap = conjurConfig.mapProperty(credentialId);
				if (StringUtils.isNotBlank(credentialToMap)) {
					credentialId = credentialToMap;
				}
				byte[] result;
				try {
					result = conjurRetrieveSecretService.retriveSingleSecretForCustomAnnotation(credentialId);
					setField(field, bean, result);

				} catch (ApiException ex) {
					LOGGER.warn("Data Not found for Single Retrieval for key : " + ex.getMessage());
				}
			} else if (field.isAnnotationPresent(ConjurValues.class)) {
				ReflectionUtils.makeAccessible(field);
				String[] credentialsArr = field.getDeclaredAnnotation(ConjurValues.class).keys();
				for (String key : credentialsArr) {
					credentialToMap = conjurConfig.mapProperty(key);
					if (StringUtils.isNotBlank(credentialToMap)) {
						credentialsList.add(credentialToMap);
					}
				}
				byte[] result;
				try {
					if (!credentialsList.isEmpty()) {
						String[] credentialArr = credentialsList.toArray(new String[0]);
						result = conjurRetrieveSecretService.retriveMultipleSecretsForCustomAnnotation(credentialArr);
						setField(field, bean, result);
					}
				} catch (ApiException ex) {
					if (ex.getCode() == 404 || ex.getMessage().equalsIgnoreCase("Not Found")) {
						try {
							byte[] finalResult = retrieveSecretsForCredential(credentialsList).toByteArray();
							if (finalResult.length > 0) {
								setField(field, bean, finalResult);

							}
							outputStream.close();
						} catch (IOException ioe) {
							LOGGER.error("Error while closing  ConjurValues outputStream " + ioe.getMessage());
						}
					}
				}

			}
		}
		return bean;
	}

	@Override
	@Nullable
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	private void setField(Field field, Object bean, byte[] finalResult) {
		try {
			if (finalResult != null)
				field.set(bean, finalResult);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			LOGGER.error("Error setting field value " + e.getMessage());
		}
	}

	private ByteArrayOutputStream retrieveSecretsForCredential(List<String> credentialsList) {
		byte[] result;
		if (!credentialsList.isEmpty()) {
			String secretVal = "";
			boolean firstValue = false;
			String keyValuePair;
			int counter = 0;
			try {
				for (String value : credentialsList) {
					result = conjurRetrieveSecretService.retriveSingleSecretForCustomAnnotation(value);
					String[] tmp2 = value.split("/");
					String key = tmp2[tmp2.length - 1];

					if (result != null) {
						if (!firstValue && !(outputStream.size() > 0)) {
							keyValuePair = key + "=" + new String(result);
							outputStream.write(keyValuePair.getBytes());
							firstValue = true;
						} else {
							secretVal = "," + key + "=" + new String(result);
							outputStream.write(secretVal.getBytes());
						}

					}
					counter++;
				}

			} catch (ApiException ae) {
				LOGGER.warn("Data Not found for Multiple/Bulk Retrieval for key(s)" + ae.getMessage());
				List<String> processList = new ArrayList<>();
				processList = credentialsList.subList(counter + 1, credentialsList.size());
				retrieveSecretsForCredential(processList);
			} catch (IOException ioe) {
				LOGGER.error("Error while processing ConjurValues outputStream " + ioe.getMessage());
				try {
					outputStream.close();
				} catch (IOException e) {
					LOGGER.error("Error while closing  ConjurValues outputStream " + e.getMessage());
				}
			}
		}
		return outputStream;
	}

}
