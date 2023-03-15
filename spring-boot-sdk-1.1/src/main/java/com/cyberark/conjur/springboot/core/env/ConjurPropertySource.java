package com.cyberark.conjur.springboot.core.env;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.EnumerablePropertySource;

import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.constant.ConjurConstant;

/**
 * 
 * This class resolves the secret value for give vault path at application load
 * time from the conjur vault.
 *
 */
public class ConjurPropertySource
//extends PropertySource<Object> {
//consider the following alternative if miss rates are excessive
		extends EnumerablePropertySource<Object> {

	private String vaultInfo = "";

	private String vaultPath = "";

	private SecretsApi secretsApi;

	private static String authTokenFile = System.getenv("CONJUR_AUTHN_TOKEN_FILE");

	private static String authApiKey = System.getenv("CONJUR_AUTHN_API_KEY");

	private static final Logger LOGGER = LoggerFactory.getLogger(ConjurPropertySource.class);
	/**
	 * a hack to support seeding environment for the file based api token support in
	 * downstream java
	 */
	static {

		// a hack to support seeding environment for the file based api token support in
		// downstream java
		if (authTokenFile != null) {
			Map<String, String> conjurParameters = new HashMap<String, String>();
			byte[] apiKey = null;
			try (BufferedReader br = new BufferedReader(new FileReader(authTokenFile))) {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();

				while (line != null) {
					sb.append(line);
					sb.append(System.lineSeparator());
					line = br.readLine();
				}
				apiKey = sb.toString().getBytes();
			} catch (Exception e1) {
				LOGGER.error(e1.getMessage());;

			}

			conjurParameters.put("CONJUR_AUTHN_API_KEY", new String(apiKey).trim());
			apiKey = null;
			try {
				loadEnvironmentParameters(conjurParameters);
			} catch (Exception e) {
				{
				LOGGER.error(e.getMessage());
				}
			}

		} else if (authApiKey == null && authTokenFile == null)
		{
			LOGGER.error(ConjurConstant.CONJUR_APIKEY_ERROR);

		}
	}

	/**
	 * Sets the external environment variable.
	 * 
	 * @param newenv - setting for API_KEY
	 * @throws NoSuchFieldException     -- class doesn't have a field of a specified
	 *                                  name
	 * @throws SecurityException        --indicate a security violation.
	 * @throws IllegalArgumentException -- a method has been passed an illegal or
	 *                                  inappropriate argument.
	 * @throws IllegalAccessException   -- excuting method does not have access to
	 *                                  the definition of the specified class,
	 *                                  field, method or constructor.
	 */
	public static void loadEnvironmentParameters(Map<String, String> newenv)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Class[] classes = Collections.class.getDeclaredClasses();
		Map<String, String> env = System.getenv();
		for (Class cl : classes) {
			if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
				Field field = cl.getDeclaredField("m");
				field.setAccessible(true);
				Object obj = field.get(env);
				Map<String, String> map = (Map<String, String>) obj;
				map.putAll(newenv);

			}
		}
	}

	protected ConjurPropertySource(String vaultPath) {
		super(vaultPath + "@");
		this.vaultPath = vaultPath;

	}

	protected ConjurPropertySource(String vaultPath, String vaultInfo) {
		super(vaultPath + "@" + vaultInfo);
		this.vaultPath = vaultPath;
		this.vaultInfo = vaultInfo;
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

		String secretValue = null;
//		key = ConjurConfig.getInstance().mapProperty(key);
		ConjurConnectionManager.getInstance();
		if (null == secretsApi) {
			secretsApi = new SecretsApi();
		}
		byte[] result = null;
		try {
			secretValue = secretsApi.getSecret(ConjurConstant.CONJUR_ACCOUNT, ConjurConstant.CONJUR_KIND,
					vaultPath + key);
			result = secretValue != null ? secretValue.getBytes() : null;

		} catch (ApiException ae) {
		}
		return result;

	}

}