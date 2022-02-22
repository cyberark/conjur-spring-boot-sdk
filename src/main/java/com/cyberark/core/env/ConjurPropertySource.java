package com.cyberark.core.env;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertySource;

import com.cyberark.conjur.sdk.AccessToken;
import com.cyberark.conjur.sdk.ApiClient;
import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.sdk.Configuration;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;

public class ConjurPropertySource extends PropertySource<Object> {

	private static Logger logger = LoggerFactory.getLogger(ConjurPropertySource.class);

	private String vaultInfo = "";

	private String[] vaultPath;

	private static final String kind = "variable";

	protected ConjurPropertySource(String[] vaultPath) {
		super(vaultPath + "@");
		this.vaultPath = vaultPath;

	}

	protected ConjurPropertySource(String[] vaultPath, String vaultInfo) {
		super(vaultPath + "@" + vaultInfo);
		this.vaultPath = vaultPath;
		this.vaultInfo = vaultInfo;
	}

//	static {
//
//		logger.info(System.getenv("CONJUR_AUTHN_API_KEY"));
//		logger.info(System.getenv("CONJUR_AUTHN_TOKEN_FILE"));
//
//		Map<String, String> newVars = new HashMap<String, String>();
//
//		// TODO: resolve the value of api key from file content here
//		// CONJUR_AUTHN_TOKEN_FILE
//
//		newVars.put("CONJUR_AUTHN_API_KEY", "my key from CONJUR_AUTHN_TOKEN_FILE specified location");
//
//		try {
//			set(newVars);
//		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
//			e.printStackTrace();
//		}
//
//		logger.info("%s\n", System.getenv("CONJUR_AUTHN_API_KEY"));
//	}
//
//	public static void set(Map<String, String> newenv)
//			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
//		Class[] classes = Collections.class.getDeclaredClasses();
//		Map<String, String> env = System.getenv();
//		for (Class cl : classes) {
//			if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
//				Field field = cl.getDeclaredField("m");
//				field.setAccessible(true);
//				Object obj = field.get(env);
//				Map<String, String> map = (Map<String, String>) obj;
//				map.putAll(newenv);
//			}
//		}
//	}

	@Override
	public Object getProperty(String name) {

		name = ConjurConfig.getInstance().mapProperty(name);

		

			ApiClient client = Configuration.getDefaultApiClient();
			AccessToken accesToken = client.getNewAccessToken();
			if (accesToken == null) {
				logger.error("Access token is null");
			}
			
			String accountName = System.getenv("CONJUR_ACCOUNT");
			String token = accesToken.getHeaderValue();
			client.setAccessToken(token);
			Configuration.setDefaultApiClient(client);
			//logger for connection successful
			SecretsApi api = new SecretsApi();
			String result = null;
			StringBuilder kindMull = new StringBuilder("");
			try {
				if (vaultPath.length > 1) {

					for (int i = 0; i <= vaultPath.length; i++) {
						if (i < vaultPath.length - 1) {
							kindMull.append("" + accountName + ":variable:" + vaultPath[i] + ",");
						} else if (i == vaultPath.length - 1) {
							kindMull.append("" + accountName + ":variable:" + vaultPath[i] + "");
						}
					}

					result = api.getSecrets(new String(kindMull)).toString();

				}

				else {
					result = api.getSecret(accountName, kind, vaultPath[0]+"/"+name);
					
				}
				logger.info(vaultPath[0]);
			} catch (ApiException e) {
				logger.error(e.getMessage());
			}
			return result;
	

		

	}

}
