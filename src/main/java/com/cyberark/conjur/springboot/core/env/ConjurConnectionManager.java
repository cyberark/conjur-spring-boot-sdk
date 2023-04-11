package com.cyberark.conjur.springboot.core.env;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.cyberark.conjur.sdk.AccessToken;
import com.cyberark.conjur.sdk.ApiClient;
import com.cyberark.conjur.sdk.Configuration;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.constant.ConjurConstant;
import com.cyberark.conjur.springboot.domain.ConjurProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import static com.cyberark.conjur.springboot.constant.ConjurConstant.CONJUR_PREFIX;

/**
 *
 * This is the connection creation singleton class with conjur vault by using
 * the conjur java sdk.
 *
 */
public class ConjurConnectionManager implements EnvironmentAware, BeanFactoryPostProcessor {

	/**
	 * The Access token provider.
	 */
	private final AccessTokenProvider accessTokenProvider;

	/**
	 * The Environment.
	 */
	private Environment environment;

	/**
	 * The constant logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(ConjurConnectionManager.class);

	/**
	 * For Getting Connection with conjur vault using cyberark sdk
	 *
	 * @param accessTokenProvider the access token provider
	 */
	public ConjurConnectionManager(AccessTokenProvider accessTokenProvider) {
		this.accessTokenProvider = accessTokenProvider;
	}
	
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		final BindResult<ConjurProperties> result = Binder.get(environment).bind(CONJUR_PREFIX, ConjurProperties.class);
		if (result.isBound()) {
			this.getConnection(result.get());
		}
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	/**
	 * Gets connection.
	 *
	 * @param conjurProperties the conjur properties
	 */
	private void getConnection(ConjurProperties conjurProperties) {
		try {
			// The client connection values can be filled automatically through environment variables
			// But if the client connection information come from a configuration file: application.yml for example
			// We will need to set the client properties values to the ApiClient, because during
			// postProcessBeanFactory lifecyle, properties as environment variables might not be set yet.
			ApiClient client = Configuration.getDefaultApiClient();
			client.setAccount(conjurProperties.getAccount());
			client.setBasePath(conjurProperties.getApplianceUrl());
					
			InputStream sslInputStream = null;
			String sslCertificate = conjurProperties.getSslCertificate();
			String certFile = conjurProperties.getCertFile();
			if (StringUtils.isNotEmpty(sslCertificate)) {
				sslInputStream = new ByteArrayInputStream(sslCertificate.getBytes(StandardCharsets.UTF_8));
			}
			else {
				if (StringUtils.isNotEmpty(certFile))
					sslInputStream = new FileInputStream(certFile);
			}

			if (sslInputStream != null) {
				client.setSslCaCert(sslInputStream);
				sslInputStream.close();
			}

			String authTokenFile = conjurProperties.getAuthTokenFile();
			if (StringUtils.isNotEmpty(authTokenFile)) {
				String apiKey = Files.readString(Paths.get(authTokenFile));
				client.setApiKey(apiKey);
			}
			
			AccessToken accessToken;
			String jwtTokenPath = conjurProperties.getJwtTokenPath();
			String authenticatorId = conjurProperties.getAuthenticatorId();
			String authnLogin = conjurProperties.getAuthnLogin();
			String authApiKey = conjurProperties.getAuthApiKey();

			// If jwtTokenPath and authenticatorId are present, the we assume it's JWT Authentication
			if (StringUtils.isNotEmpty(jwtTokenPath) && StringUtils.isNotEmpty(authenticatorId)) {
				logger.debug("Using JWT Authentication");
				accessToken = accessTokenProvider.getJwtAccessToken(client, jwtTokenPath, authenticatorId);
			}
			else {
				if (StringUtils.isNotEmpty(authnLogin)) {
					client.setUsername(authnLogin);
				}
				if (StringUtils.isNotEmpty(authApiKey)) {
					client.setApiKey(authApiKey);
				}
				logger.debug("Using API KEY Authentication");
				accessToken = accessTokenProvider.getNewAccessToken(client);
			}
			
			if (accessToken == null) {
				logger.debug("Using Account: " + obfuscateString(client.getAccount()));
				logger.debug("Using ApplianceUrl: " + obfuscateString(client.getBasePath()));
				if (StringUtils.isNotEmpty(authnLogin)) {
					logger.debug("Using AuthnLogin: " +obfuscateString(authnLogin));
				}
				if (StringUtils.isNotEmpty(authApiKey)) {
					logger.debug("Using Authn API Key: " +obfuscateString(authApiKey));
				}
				if (StringUtils.isNotEmpty(sslCertificate)) {
					logger.debug("Using SSL Cert: " +  obfuscateString(sslCertificate));
				}
				else if (StringUtils.isNotEmpty(certFile)){
					logger.debug("Using SSL Cert File: " +  obfuscateString(certFile));
				}
				if (StringUtils.isNotEmpty(jwtTokenPath)) {
					logger.debug("Using JWT Token Path: " +obfuscateString(jwtTokenPath));
				}
				if (StringUtils.isNotEmpty(authenticatorId)) {
					logger.debug("Using Authenticator ID: " +obfuscateString(authenticatorId));
				}
				logger.error("Access token is null, Please enter proper environment variables.");
			}
			else {
				String token = accessToken.getHeaderValue();
				client.setAccessToken(token);
				Configuration.setDefaultApiClient(client);
				logger.debug("Connection with conjur is successful");
			}
		} catch (Exception e) {
			logger.error("Exception encountered {} : {}" , e.getClass(), e.getMessage());
		}
	}

	/**
	 * Obfuscate string string.
	 *
	 * @param str the str
	 * @return the string
	 */
	private String obfuscateString(String str) {
		int len = str.length();
		if (len <= 2) {
			return str;
		} else {
			char first = str.charAt(0);
			char last = str.charAt(len - 1);
			String middle = "*******";
			return first + middle + last;
		}
	}

	/**
	 * Gets file content as string. 
	 * Works for java 8 and higher versions
	 *
	 * @param path the path
	 * @param encoding the encoding
	 * @return the file content as string
	 * @throws IOException the io exception
	 */
	private String getFileContentAsString(String path, Charset encoding)
			throws IOException
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	/**
	 * Get account string.
	 *
	 * @param secretsApi the secrets api
	 * @return the string
	 */
	public static String getAccount(SecretsApi secretsApi){
		ApiClient apiClient = secretsApi.getApiClient();
		return (apiClient != null ) ? apiClient.getAccount() : ConjurConstant.CONJUR_ACCOUNT;
	}

}
