package com.cyberark.conjur.springboot.core.env;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.constant.ConjurConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

/**
 * 
 * This class resolves the secret value for give vault path at application load
 * time from the conjur vault.
 *
 */
public class ConjurPropertySource extends EnumerablePropertySource<Object> {

	private String vaultInfo = "";

	private String vaultPath = "";

	private SecretsApi secretsApi;

	private List<String> properties;

	private static final Logger LOGGER = LoggerFactory.getLogger(ConjurPropertySource.class);

	protected ConjurPropertySource(String vaultPath) {
		super(vaultPath + "@");
		this.vaultPath = vaultPath;

	}

	protected ConjurPropertySource(String vaultPath, String vaultInfo, AnnotationMetadata importingClassMetadata)
			throws ClassNotFoundException {
		super(vaultPath + "@" + vaultInfo);
		this.vaultPath = vaultPath;
		this.vaultInfo = vaultInfo;
		List<String> properties = new ArrayList<>();
		Class<?> annotatedClass = ClassUtils.forName((importingClassMetadata).getClassName(),
				getClass().getClassLoader());
		for (Field field : annotatedClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(Value.class)) {
				String value = field.getAnnotation(Value.class).value();
				properties.add(value);
			}
		}
		this.properties = properties;
	}

	@Override
	public String[] getPropertyNames() {
		return new String[0];
	}

	/**
	 * Method which resolves @value annotation queries and return result in the form
	 * of byte array.
	 */

	@Override
	public Object getProperty(String key) {
		byte[] result = null;
		if(!vaultPath.endsWith("/")) {
			this.vaultPath = vaultPath.concat("/");
		}
		if (propertyExists(key)) {
			key = ConjurConfig.getInstance().mapProperty(key);
			try {
				String account = ConjurConnectionManager.getAccount(secretsApi);
				String secretValue = secretsApi.getSecret(account, ConjurConstant.CONJUR_KIND, vaultPath + key);
				result = secretValue != null ? secretValue.getBytes() : null;
			} catch (ApiException ae) {
				LOGGER.warn("Failed to get property from Conjur for: " + key);
				LOGGER.warn("Reason: " + ae.getResponseBody());
				LOGGER.warn(ae.getMessage());
			}
		}
		return result;
	}

	/**
	 * To set the secert api value
	 * @param secretsApi
	 */
	public void setSecretsApi(SecretsApi secretsApi) {
		this.secretsApi = secretsApi;
	}

	private boolean propertyExists(String key) {
		return properties.stream().anyMatch(property -> property.contains(key));
	}
}