package com.cyberark.conjur.springboot.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyberark.conjur.springboot.constant.ConjurConstant;
import com.cyberark.conjur.springboot.domain.ConjurProperties;
import com.cyberark.conjur.springboot.util.ConjurPropertySourceUtil;

public class ConjurAuthConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConjurAuthConfiguration.class);
	
	private static ConjurPropertySourceUtil conjurPSUtil = new ConjurPropertySourceUtil();

	public void getSysAuthParam() {
		
		String authTokenFile = System.getenv("CONJUR_AUTHN_TOKEN_FILE");

		String authApiKey = System.getenv("CONJUR_AUTHN_API_KEY");
		
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
				LOGGER.error(e1.getMessage());
				;

			}

			conjurParameters.put("CONJUR_AUTHN_API_KEY", new String(apiKey).trim());
			apiKey = null;
			try {
				conjurPSUtil.loadEnvironmentParameters(conjurParameters);
			} catch (Exception e) {
				{
					LOGGER.error(e.getMessage());
				}
			}

		} else if (authApiKey == null && authTokenFile == null) {
			LOGGER.error(ConjurConstant.CONJUR_APIKEY_ERROR);

		}

	}

	public void getPropAuthParam(ConjurProperties conjurParam) {
		String authApiKey = null;// System.getenv("CONJUR_AUTHN_API_KEY");

		String authTokenFile = conjurParam.getAuthTokenFile();

		LOGGER.info("Auth Token file path>>" + authTokenFile);

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
				e1.printStackTrace();

			}

			conjurParameters.put("CONJUR_AUTHN_API_KEY", new String(apiKey).trim());
			conjurParameters.put("CONJUR_ACCOUNT", conjurParam.getAccount());
			conjurParameters.put("CONJUR_APPLIANCE_URL", conjurParam.getApplianceUrl());
			conjurParameters.put("CONJUR_AUTHN_LOGIN", conjurParam.getAuthnLogin());
			conjurParameters.put("CONJUR_CERT_FILE", conjurParam.getCertFile());
			conjurParameters.put("CONJUR_SSL_CERTIFICATE", conjurParam.getSslCertificate());
			conjurParameters.put("CONJUR_AUTHN_TOKEN_FILE", conjurParam.getAuthTokenFile());

			apiKey = null;
			try {
				conjurPSUtil.loadEnvironmentParameters(conjurParameters);
			} catch (Exception e) {

				e.printStackTrace();

			}

		} else if (authApiKey == null && authTokenFile == null) {
			LOGGER.error(ConjurConstant.CONJUR_APIKEY_ERROR);

		}

	}

}
