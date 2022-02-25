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

public class ConjurPropertySource
//extends PropertySource<Object> {
//consider the following alternative if miss rates are excessive
		extends EnumerablePropertySource<Object> {

	private String vaultInfo = "";

	private String vaultPath = "";

	private SecretsApi secretsApi;

	private static Logger logger = LoggerFactory.getLogger(ConjurPropertySource.class);

	static {

		// a hack to support seeding environment for the file based api token support in
		// downstream java

		Map<String, String> conjurParameters = new HashMap<String, String>();
		String apiKey = "";

		try (BufferedReader br = new BufferedReader(new FileReader(System.getenv("CONJUR_AUTHN_TOKEN_FILE")))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			apiKey = sb.toString();
			logger.info("apiKey-->" + apiKey);
		} catch (Exception e1) {
			e1.printStackTrace();

		}

		conjurParameters.put("CONJUR_AUTHN_API_KEY", apiKey.trim());
		try {
	//		loadEnvironmentParameters(conjurParameters);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public static void loadEnvironmentParameters(Map<String, String> newenv)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Class[] classes = Collections.class.getDeclaredClasses();
		Map<String, String> env = System.getenv();
		for (Class cl : classes) {
			if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
				Field field = cl.getDeclaredField("m");
				field.setAccessible(false);
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

	@Override
	public Object getProperty(String key) {

		key = ConjurConfig.getInstance().mapProperty(key);

		ConjurConnectionManager.getInstance();
		if (null == secretsApi) {
			secretsApi = new SecretsApi();
		}
		String result = null;
		try {
			result = secretsApi.getSecret(ConjurConstant.CONJUR_ACCOUNT, ConjurConstant.CONJUR_KIND, vaultPath + key);

		} catch (ApiException e) {

		}
		return result;

	}

}