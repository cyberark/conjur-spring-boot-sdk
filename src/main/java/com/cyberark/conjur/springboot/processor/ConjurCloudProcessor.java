package com.cyberark.conjur.springboot.processor;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import com.cyberark.conjur.sdk.AccessToken;
import com.cyberark.conjur.sdk.ApiClient;
import com.cyberark.conjur.sdk.Configuration;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.core.env.AccessTokenProvider;

import com.cyberark.conjur.springboot.domain.ConjurProperties;
import com.cyberark.conjur.springboot.service.CustomPropertySourceChain;
import com.cyberark.conjur.springboot.service.DefaultPropertySourceChain;
import com.cyberark.conjur.springboot.service.PropertyProcessorChain;

/**
 * The ValueProcess class will be invoked on boot strap of the applicaiton and
 * will invoke the process chain based on the properties. It call the default
 * property chain if value is found or will call the Custome propertysource to
 * retrieve the value from the Conjur vault . This class in turn will invoke the
 * ConjurPropertySource to autowire the value for @Value annotation
 * 
 *
 */

public class ConjurCloudProcessor
		implements BeanPostProcessor, InitializingBean, EnvironmentAware, ApplicationContextAware {


	private ApplicationContext context;

	private ConfigurableEnvironment environment;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConjurCloudProcessor.class);
	
	/**
	 * The Access token provider.
	 */
	private final AccessTokenProvider accessTokenProvider;
	
	/**
	 * For Getting Connection with conjur vault using cyberark sdk
	 *
	 * @param accessTokenProvider the access token provider
	 */
	public ConjurCloudProcessor(AccessTokenProvider accessTokenProvider) {
		this.accessTokenProvider = accessTokenProvider;
	}

	@Autowired(required = true)
	private ConjurProperties conjurProperties;

	private PropertyProcessorChain processorChain;

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
		//initializeApiClient(conjurProperties);
		
		getConnection(conjurProperties);

		this.processorChain = new DefaultPropertySourceChain("DefaultPropertySource");
		CustomPropertySourceChain customPS = new CustomPropertySourceChain("CustomPropertySource");
		processorChain.setNextChain(customPS);
		customPS.setSecretsApi(new SecretsApi());
		environment.getPropertySources().addLast(processorChain);


	}

	@Override
	public void setEnvironment(Environment environment) {

		if (environment instanceof ConfigurableEnvironment) {

			this.environment = (ConfigurableEnvironment) environment;
			
		}

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

		this.context = applicationContext;

	}

	private void initializeApiClient(ConjurProperties conjurParam) throws IOException {

		ApiClient apiClient = Configuration.getDefaultApiClient();
		apiClient.setAccount(conjurParam.getAccount());
		apiClient.setApiKey(conjurParam.getAuthnApiKey());
		apiClient.setCertFile(conjurParam.getCertFile());

		InputStream sslInputStream = null;
		String sslCertificate = conjurProperties.getSslCertificate();
		String certFile = conjurProperties.getCertFile();
		if (StringUtils.isNotEmpty(sslCertificate)) {
			sslInputStream = new ByteArrayInputStream(sslCertificate.getBytes(StandardCharsets.UTF_8));
		} else {
			if (StringUtils.isNotEmpty(certFile))
				sslInputStream = new FileInputStream(certFile);
		}

		if (sslInputStream != null) {
			apiClient.setSslCaCert(sslInputStream);
			sslInputStream.close();
		}

	}
	
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
			String authApiKey = conjurProperties.getAuthnApiKey();

			// If jwtTokenPath and authenticatorId are present, the we assume it's JWT Authentication
			if (StringUtils.isNotEmpty(jwtTokenPath) && StringUtils.isNotEmpty(authenticatorId)) {
             LOGGER.debug("Using JWT Authentication");
				accessToken = accessTokenProvider.getJwtAccessToken(client, jwtTokenPath, authenticatorId);
			}
			else {
				if (StringUtils.isNotEmpty(authnLogin)) {
					client.setUsername(authnLogin);
				}
				if (StringUtils.isNotEmpty(authApiKey)) {
					client.setApiKey(authApiKey);
				}
				LOGGER.debug("Using API KEY Authentication");
				accessToken = accessTokenProvider.getNewAccessToken(client);
			}
			
			if (accessToken == null) {
				LOGGER.debug("Using Account: " + obfuscateString(client.getAccount()));
				LOGGER.debug("Using ApplianceUrl: " + obfuscateString(client.getBasePath()));
				if (StringUtils.isNotEmpty(authnLogin)) {
					LOGGER.debug("Using AuthnLogin: " +obfuscateString(authnLogin));
				}
				if (StringUtils.isNotEmpty(authApiKey)) {
					LOGGER.debug("Using Authn API Key: " +obfuscateString(authApiKey));
				}
				if (StringUtils.isNotEmpty(sslCertificate)) {
					LOGGER.debug("Using SSL Cert: " +  obfuscateString(sslCertificate));
				}
				else if (StringUtils.isNotEmpty(certFile)){
					LOGGER.debug("Using SSL Cert File: " +  obfuscateString(certFile));
				}
				if (StringUtils.isNotEmpty(jwtTokenPath)) {
					LOGGER.debug("Using JWT Token Path: " +obfuscateString(jwtTokenPath));
				}
				if (StringUtils.isNotEmpty(authenticatorId)) {
					LOGGER.debug("Using Authenticator ID: " +obfuscateString(authenticatorId));
				}
				LOGGER.error("Access token is null, Please enter proper environment variables.");
			}
			else {
				String token = accessToken.getHeaderValue();
				client.setAccessToken(token);
				Configuration.setDefaultApiClient(client);
				LOGGER.debug("Connection with conjur is successful");
			}
		} catch (Exception e) {
			LOGGER.error("Exception encountered {} : {}" , e.getClass(), e.getMessage());
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



}
