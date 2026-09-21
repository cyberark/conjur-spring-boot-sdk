package com.cyberark.conjur.springboot.core.env;

import static com.cyberark.conjur.springboot.constant.ConjurConstant.CONJUR_PREFIX;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

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

import com.cyberark.conjur.sdk.AccessToken;
import com.cyberark.conjur.sdk.ApiClient;
import com.cyberark.conjur.sdk.Configuration;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.constant.ConjurConstant;
import com.cyberark.conjur.springboot.domain.ConjurProperties;
import com.cyberark.conjur.springboot.util.ConjurResilientAuthenticator;

/**
 * This is the connection creation singleton class with conjur vault by using
 * the conjur java sdk.
 */
public class ConjurConnectionManager implements EnvironmentAware, BeanFactoryPostProcessor {

  /**
   * The Access token provider.
   */
  private final AccessTokenProvider accessTokenProvider;
  private ConjurResilientAuthenticator resilientAuthenticator;


  /**
   * The Environment.
   */
  private Environment environment;

  /**
   * The constant logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ConjurConnectionManager.class);

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
      ConjurProperties conjurProperties = result.get();
      initializeResilientAuthenticator(conjurProperties);
      this.getConnection(conjurProperties);
    }
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  private void initializeResilientAuthenticator(ConjurProperties conjurProperties) {
    int maxAttempts = conjurProperties.getResilienceMaxAttempts();
    Duration waitDuration = conjurProperties.getResilienceWaitDuration();
    this.resilientAuthenticator = new ConjurResilientAuthenticator(maxAttempts, waitDuration);
  }

  /**
   * Gets connection.
   *
   * @param conjurProperties the conjur properties
   */
  private void getConnection(ConjurProperties conjurProperties) {
    try {
      // The client connection values can be filled automatically through environment
      // variables
      // But if the client connection information come from a configuration file:
      // application.yml for example
      // We will need to set the client properties values to the ApiClient, because
      // during
      // postProcessBeanFactory lifecyle, properties as environment variables might
      // not be set yet.
      ApiClient client = Configuration.getDefaultApiClient();
      client.setAccount(conjurProperties.getAccount());
      client.setBasePath(conjurProperties.getApplianceUrl());

      // Setting up telemetry headers
      client.setIntegrationName(conjurProperties.getIntegrationName());
      client.setIntegrationType(conjurProperties.getIntegrationType());
      client.setIntegrationVersion(conjurProperties.getIntegrationVersion());
      client.setVendorName(conjurProperties.getVendorName());

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
        client.setSslCaCert(sslInputStream);
        sslInputStream.close();
      }

      String authTokenFile = conjurProperties.getAuthTokenFile();
      if (StringUtils.isNotEmpty(authTokenFile)) {
        String apiKey = new String(Files.readAllBytes(Paths.get(authTokenFile)));
        client.setApiKey(apiKey);
      }

      AccessToken accessToken;
      String jwtTokenPath = conjurProperties.getJwtTokenPath();
      String authenticatorId = conjurProperties.getAuthenticatorId();
      String authnLogin = conjurProperties.getAuthnLogin();
      String authApiKey = conjurProperties.getAuthnApiKey();
      String authnCertServiceId = conjurProperties.getAuthnCertServiceId();
      String clientCertFile = conjurProperties.getClientCertFile();
      String clientKeyFile = conjurProperties.getClientKeyFile();

      // If jwtTokenPath and authenticatorId are present, the we assume it's JWT
      // Authentication
      boolean resilienceEnabled = conjurProperties.getResilienceEnabled();

      if (StringUtils.isNotEmpty(authnCertServiceId)) {
        // ── authn-cert (mTLS / client certificate) authentication ──
        LOGGER.debug("Using Certificate (authn-cert) Authentication");
        if (resilienceEnabled && resilientAuthenticator != null) {
          LOGGER.debug("Using resilient authentication with retries (cert)");
          final String _authnCertServiceId = authnCertServiceId;
          final String _authnLogin = authnLogin;
          final String _clientCertFile = clientCertFile;
          final String _clientKeyFile = clientKeyFile;
          accessToken = resilientAuthenticator.execute(() -> {
            try {
              return accessTokenProvider.getCertAccessToken(client, _authnCertServiceId, _authnLogin,
                  _clientCertFile, _clientKeyFile);
            } catch (IOException | java.security.GeneralSecurityException e) {
              LOGGER.error("Failed to get cert access token in resilient auth: {}", e.getMessage(), e);
              throw new RuntimeException(e);
            }
          });
        } else {
          try {
            accessToken = accessTokenProvider.getCertAccessToken(client, authnCertServiceId, authnLogin,
                clientCertFile, clientKeyFile);
          } catch (IOException | java.security.GeneralSecurityException e) {
            LOGGER.error("Failed to get cert access token: {}", e.getMessage(), e);
            accessToken = null;
          }
        }
      } else if (resilienceEnabled && resilientAuthenticator != null) {
        LOGGER.debug("Using resilient authentication with retries");
        if (StringUtils.isNotEmpty(jwtTokenPath) && StringUtils.isNotEmpty(authenticatorId)) {
          accessToken = resilientAuthenticator.execute(() -> {
            try {
              return accessTokenProvider.getJwtAccessToken(client, jwtTokenPath, authenticatorId);
            } catch (IOException e) {
              LOGGER.error("Failed to get JWT access token in resilient auth: {}", e.getMessage(), e);
              throw new RuntimeException(e);
            }
          });
        } else {
          if (StringUtils.isNotEmpty(authnLogin)) {
            client.setUsername(authnLogin);
          }
          if (StringUtils.isNotEmpty(authApiKey)) {
            client.setApiKey(authApiKey);
          }
          accessToken = resilientAuthenticator.execute(() ->
            accessTokenProvider.getNewAccessToken(client)
          );
        }
      } else {
        // Existing behavior without resilience
        if (StringUtils.isNotEmpty(jwtTokenPath) && StringUtils.isNotEmpty(authenticatorId)) {
          LOGGER.debug("Using JWT Authentication");
          try {
            accessToken = accessTokenProvider.getJwtAccessToken(client, jwtTokenPath, authenticatorId);
          } catch (IOException e) {
            LOGGER.error("Failed to get JWT access token: {}", e.getMessage(), e);
            accessToken = null;
          }
        } else {
          if (StringUtils.isNotEmpty(authnLogin)) {
            client.setUsername(authnLogin);
          }
          if (StringUtils.isNotEmpty(authApiKey)) {
            client.setApiKey(authApiKey);
          }
          LOGGER.debug("Using API KEY Authentication");
          accessToken = accessTokenProvider.getNewAccessToken(client);
        }
      }


      if (accessToken == null) {
        LOGGER.debug("Using Account: " + obfuscateString(client.getAccount()));
        LOGGER.debug("Using ApplianceUrl: " + obfuscateString(client.getBasePath()));
        if (StringUtils.isNotEmpty(authnLogin)) {
          LOGGER.debug("Using AuthnLogin: " + obfuscateString(authnLogin));
        }
        if (StringUtils.isNotEmpty(authApiKey)) {
          LOGGER.debug("Using Authn API Key: " + obfuscateString(authApiKey));
        }
        if (StringUtils.isNotEmpty(sslCertificate)) {
          LOGGER.debug("Using SSL Cert: " + obfuscateString(sslCertificate));
        } else if (StringUtils.isNotEmpty(certFile)) {
          LOGGER.debug("Using SSL Cert File: " + obfuscateString(certFile));
        }
        if (StringUtils.isNotEmpty(jwtTokenPath)) {
          LOGGER.debug("Using JWT Token Path: " + obfuscateString(jwtTokenPath));
        }
        if (StringUtils.isNotEmpty(authenticatorId)) {
          LOGGER.debug("Using Authenticator ID: " + obfuscateString(authenticatorId));
        }
        if (StringUtils.isNotEmpty(authnCertServiceId)) {
          LOGGER.debug("Using Authn Cert Service ID: " + obfuscateString(authnCertServiceId));
        }
        if (StringUtils.isNotEmpty(clientCertFile)) {
          LOGGER.debug("Using Client Cert File: " + obfuscateString(clientCertFile));
        }
        if (StringUtils.isNotEmpty(clientKeyFile)) {
          LOGGER.debug("Using Client Key File: " + obfuscateString(clientKeyFile));
        }
        LOGGER.error("Access token is null, Please enter proper environment variables.");
      } else {
        String token = accessToken.getHeaderValue();
        client.setAccessToken(token);
        Configuration.setDefaultApiClient(client);
        LOGGER.debug("Connection with conjur is successful");
      }
    } catch (Exception e) {
      LOGGER.error("Exception encountered {} : {}", e.getClass(), e.getMessage());
    }
  }

  /**
   * Obfuscate string string.
   *
   * @param str the str
   * @return the string
   */
  private String obfuscateString(String str) {
    if (StringUtils.isNoneEmpty(str) && str.length() > 2) {
      int len = str.length();
      char first = str.charAt(0);
      char last = str.charAt(len - 1);
      String middle = "*******";
      return first + middle + last;

    }
    return str;
  }

  /**
   * Get account string.
   *
   * @param secretsApi the secrets api
   * @return the string
   */
  public static String getAccount(SecretsApi secretsApi) {
    ApiClient apiClient = secretsApi.getApiClient();
    return (apiClient != null) ? apiClient.getAccount() : ConjurConstant.CONJUR_ACCOUNT;
  }

}
