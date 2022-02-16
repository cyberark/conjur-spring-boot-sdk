package com.cyberark.core.env;

import org.springframework.core.env.PropertySource;

import com.cyberark.conjur.sdk.AccessToken;
import com.cyberark.conjur.sdk.ApiClient;
import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.sdk.Configuration;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;

public class ConjurPropertySource extends PropertySource<Object> {

	private String vaultInfo = "";

	private String[] vaultPath;

	protected ConjurPropertySource(String[] vaultPath) {
		super(vaultPath + "@");
		this.vaultPath = vaultPath;

	}

	protected ConjurPropertySource(String[] vaultPath, String vaultInfo) {
		super(vaultPath + "@" + vaultInfo);
		this.vaultPath = vaultPath;
		this.vaultInfo = vaultInfo;
	}

	@Override
	public Object getProperty(String name) {

		name = ConjurConfig.getInstance().mapProperty(name);

		if ("secretValue".equals(name.trim())) {

			ApiClient client = Configuration.getDefaultApiClient();
			AccessToken accesToken = client.getNewAccessToken();
			if (accesToken == null) {
				System.out.println("Access token is null");
				System.exit(1);
			}
			String accountName = System.getenv("CONJUR_ACCOUNT");
			String token = accesToken.getHeaderValue();
			client.setAccessToken(token);
			Configuration.setDefaultApiClient(client);
			SecretsApi api = new SecretsApi();
			String result = null;
			String kind = "variable";
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
					result = api.getSecret(accountName, kind, vaultPath[0]);
				}

			} catch (ApiException e) {
				e.printStackTrace();
			}
			return result;
		}

		return null;

	}

}
