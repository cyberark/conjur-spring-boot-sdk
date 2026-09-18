package com.cyberark.conjur.springboot.domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Conjur properties.
 */
public class ConjurProperties{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConjurProperties.class);
	
	// telemetry headers
	private String integrationName = System.getenv().getOrDefault("INTEGRATION_NAME", "SecretsManagerSpringBoot SDK");
    private String integrationType = System.getenv().getOrDefault("INTEGRATION_TYPE", "cybr-secretsmanager-springboot");
    private String integrationVersion = getSDKVersion();  // Fetch from VERSION file
    private String vendorName = System.getenv().getOrDefault("VENDOR_NAME", "CyberArk");
    
	private Map<String, String> mapping = new HashMap<>();

    public Map<String, String> getMapping() {
		return mapping;
	}

	public void setMapping(Map<String, String> mapping) {
		this.mapping = mapping;
	}
    
    /**
     * Gets the integration name used for telemetry.
     * 
     * @return the integration name
     */
    public String getIntegrationName() {
        return integrationName;
    }

    /**
     * Gets the integration type used for telemetry.
     * 
     * @return the integration type
     */
    public String getIntegrationType() {
        return integrationType;
    }

    /**
     * Gets the integration version used for telemetry.
     * 
     * @return the integration version
     */
    public String getIntegrationVersion() {
        return integrationVersion;
    }

    /**
     * Gets the vendor name used for telemetry.
     * 
     * @return the vendor name.
     */
    public String getVendorName() {
        return vendorName;
    }
    
    // Method to get the SDK version from the CHANGELOG file   
    public static String getSDKVersion() {
        final String fallbackVersion = "unset";
        String changelogFilePath = "/CHANGELOG.md";
        Pattern versionPattern = Pattern.compile("## \\[([\\d]+(?:\\.[\\d]+)*)\\]");

        InputStream inputStream = ConjurProperties.class.getResourceAsStream(changelogFilePath);
        
        if (inputStream == null) {
            LOGGER.warn("CHANGELOG.md file not found.");
            return fallbackVersion;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = versionPattern.matcher(line);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Error reading CHANGELOG.md from the JAR.", e);
        }

        return fallbackVersion;
    	}
    
	/**
	 * The Account, can be injected with CONJUR_ACCOUNT environment variable
	 * or spring boot property: conjur.account
	 */
	private String account;

	/**
	 * The Appliance url can be injected with CONJUR_APPLIANCE_URL environment variable
	 * or spring boot property: conjur.appliance-url
	 */
	private String applianceUrl;

	/**
	 * The Auth token file.
	 */
	private String authTokenFile;

	/**
	 * The Api key can be injected with CONJUR_AUTHN_API_KEY environment variable
	 * or spring boot property: conjur.api-key
	 */
	private String authnApiKey;

	/**
	 * The Authn login.can be injected with CONJUR_AUTHN_LOGIN environment variable
	 * or spring boot property: conjur.authn-login
	 */
	private String authnLogin;

	/**
	 * The Cert file. can be injected with CONJUR_CERT_FILE environment variable
	 * or spring boot property: conjur.cert-file
	 */
	private String certFile;

	/**
	 * The Ssl certificate. can be injected with CONJUR_SSL_CERTIFICATE environment variable
	 * or spring boot property: conjur.ssl-certificate
	 */
	private String sslCertificate;

	/**
	 * The Jwt token path.
	 */
	private String jwtTokenPath;

	/**
	 * The Authn-JWT authenticatorID.
	 */
	private String authenticatorId;

	/**
	 * The authn-cert service ID for client-certificate (mTLS) authentication.
	 * Can be injected with CONJUR_AUTHN_CERT_SERVICE_ID environment variable
	 * or spring boot property: conjur.authn-cert-service-id
	 * Example value: "acme-vm"
	 */
	private String authnCertServiceId;

	/**
	 * Path to the PEM-encoded X.509 client certificate file used for authn-cert (mTLS) authentication.
	 * Can be injected with CONJUR_CLIENT_CERT_FILE environment variable
	 * or spring boot property: conjur.client-cert-file
	 */
	private String clientCertFile;

	/**
	 * Path to the PEM-encoded PKCS#8 private key file used for authn-cert (mTLS) authentication.
	 * Must use "BEGIN PRIVATE KEY" PEM header (PKCS#8 format).
	 * To convert a PKCS#1 key: openssl pkcs8 -topk8 -nocrypt -in key.pem -out key-pkcs8.pem
	 * Can be injected with CONJUR_CLIENT_KEY_FILE environment variable
	 * or spring boot property: conjur.client-key-file
	 */
	private String clientKeyFile;
	
	/**
	 * The Scan all values.
	 */
	private boolean scanAllValues;
	
	/**
	 * The Conjur mapping path.
	 */
	private String mappingPath;

	/**
	 * Can be injected with the CONJUR_RESILIENCE_ENABLED environment variable
	 * or the Spring Boot property: conjur.resilience-enabled
	 */
	private Boolean resilienceEnabled;

	/**
	 * Can be injected with the CONJUR_RESILIENCE_MAX_ATTEMPTS environment variable
	 * or the Spring Boot property: conjur.resilience-max-attempts
	 */
	private Integer resilienceMaxAttempts;

	/**
	 * Can be injected with the CONJUR_RESILIENCE_WAIT_DURATION environment variable
	 * or the Spring Boot property: conjur.resilience-wait-duration
	 */
	private Duration resilienceWaitDuration;

	/**
	 * Gets account.
	 *
	 * @return the account
	 */
	public String getAccount() {
		return account;
	}

	/**
	 * Sets account.
	 *
	 * @param account the account
	 */
	public void setAccount(String account) {
		this.account = account;
	}

	/**
	 * Gets appliance url.
	 *
	 * @return the appliance url
	 */
	public String getApplianceUrl() {
		return applianceUrl;
	}

	/**
	 * Sets appliance url.
	 *
	 * @param applianceUrl the appliance url
	 */
	public void setApplianceUrl(String applianceUrl) {
		this.applianceUrl = applianceUrl;
	}

	/**
	 * Gets auth token file.
	 *
	 * @return the auth token file
	 */
	public String getAuthTokenFile() {
		return authTokenFile;
	}

	/**
	 * Sets auth token file.
	 *
	 * @param authToken the auth token
	 */
	public void setAuthTokenFile(String authToken) {
		this.authTokenFile = authToken;
	}

	/**
	 * Gets authn login.
	 *
	 * @return the authn login
	 */
	public String getAuthnLogin() {
		return authnLogin;
	}

	/**
	 * Sets authn login.
	 *
	 * @param authnLogin the authn login
	 */
	public void setAuthnLogin(String authnLogin) {
		this.authnLogin = authnLogin;
	}

	/**
	 * Gets cert file.
	 *
	 * @return the cert file
	 */
	public String getCertFile() {
		return certFile;
	}

	/**
	 * Sets cert file.
	 *
	 * @param certFile the cert file
	 */
	public void setCertFile(String certFile) {
		this.certFile = certFile;
	}

	/**
	 * Gets ssl certificate.
	 *
	 * @return the ssl certificate
	 */
	public String getSslCertificate() {
		return sslCertificate;
	}

	/**
	 * Sets ssl certificate.
	 *
	 * @param sslCertificate the ssl certificate
	 */
	public void setSslCertificate(String sslCertificate) {
		this.sslCertificate = sslCertificate;
	}

	/**
	 * Gets auth api key.
	 *
	 * @return the auth api key
	 */
	public String getAuthnApiKey() {
		return authnApiKey;
	}

	/**
	 * Sets auth api key.
	 *
	 * @param authnApiKey the auth api key
	 */
	public void setAuthnApiKey(String authnApiKey) {
		this.authnApiKey = authnApiKey;
	}

	/**
	 * Gets jwt token path.
	 *
	 * @return the jwt token path
	 */
	public String getJwtTokenPath() {
		return jwtTokenPath;
	}

	/**
	 * Sets jwt token path.
	 *
	 * @param jwtTokenPath the jwt token path
	 */
	public void setJwtTokenPath(String jwtTokenPath) {
		this.jwtTokenPath = jwtTokenPath;
	}

	/**
	 * Gets authenticator id.
	 *
	 * @return the authenticator id
	 */
	public String getAuthenticatorId() {
		return authenticatorId;
	}

	/**
	 * Sets authenticator id.
	 *
	 * @param authenticatorId the authenticator id
	 */
	public void setAuthenticatorId(String authenticatorId) {
		this.authenticatorId = authenticatorId;
	}

	/**
	 * Gets the authn-cert service ID.
	 *
	 * @return the authn-cert service ID
	 */
	public String getAuthnCertServiceId() {
		return authnCertServiceId;
	}

	/**
	 * Sets the authn-cert service ID.
	 *
	 * @param authnCertServiceId the authn-cert service ID
	 */
	public void setAuthnCertServiceId(String authnCertServiceId) {
		this.authnCertServiceId = authnCertServiceId;
	}

	/**
	 * Gets the path to the PEM client certificate file.
	 *
	 * @return the client cert file path
	 */
	public String getClientCertFile() {
		return clientCertFile;
	}

	/**
	 * Sets the path to the PEM client certificate file.
	 *
	 * @param clientCertFile the client cert file path
	 */
	public void setClientCertFile(String clientCertFile) {
		this.clientCertFile = clientCertFile;
	}

	/**
	 * Gets the path to the PEM PKCS#8 private key file.
	 *
	 * @return the client key file path
	 */
	public String getClientKeyFile() {
		return clientKeyFile;
	}

	/**
	 * Sets the path to the PEM PKCS#8 private key file.
	 *
	 * @param clientKeyFile the client key file path
	 */
	public void setClientKeyFile(String clientKeyFile) {
		this.clientKeyFile = clientKeyFile;
	}
	
	/**
	 * Is scan all values boolean.
	 *
	 * @return the boolean
	 */
	public boolean isScanAllValues() {
		return scanAllValues;
	}

	/**
	 * Sets scan all values.
	 *
	 * @param scanAllValues the scan all values
	 */
	public void setScanAllValues(boolean scanAllValues) {
		this.scanAllValues = scanAllValues;
	}

	/**
	 * Gets conjur mapping path.
	 *
	 * @return the conjur mapping path
	 */
	public String getMappingPath() {
		return mappingPath;
	}

	/**
	 * Sets conjur mapping path.
	 *
	 * @param mappingPath the conjur mapping path
	 */
	public void setMappingPath(String mappingPath) {
		this.mappingPath = mappingPath;
	}
	
	/**
	 * Gets the resilience enabled value.
	 *
	 * @return the resilience enabled value
	 */
	public Boolean getResilienceEnabled() {
	    return resilienceEnabled != null ? resilienceEnabled : false;
	}

	/**
	 * Sets resilience enabled value.
	 *
	 * @param resilienceEnabled resilience enabled value
	 */
	public void setResilienceEnabled(Boolean resilienceEnabled) {
		this.resilienceEnabled = resilienceEnabled;
	}

	/**
	 * Gets the resilience max attempts.
	 *
	 * @return the resilience max attempts
	 */
	public int getResilienceMaxAttempts() {
	    return resilienceMaxAttempts != null ? resilienceMaxAttempts : 3;
	}

	/**
	 * Sets the resilience max attempts.
	 *
	 * @param resilienceMaxAttempts the resilience max attempts
	 */
	public void setResilienceMaxAttempts(Integer resilienceMaxAttempts) {
	    this.resilienceMaxAttempts = resilienceMaxAttempts;
	}

	/**
	 * Gets the resilience wait duration.
	 *
	 * @return the resilience wait duration
	 */
	public Duration getResilienceWaitDuration() {
	    return resilienceWaitDuration != null ? resilienceWaitDuration : Duration.ofMillis(500);
	}

	/**
	 * Sets the resilience wait duration.
	 *
	 * @param resilienceWaitDuration the resilience wait duration
	 */
	public void setResilienceWaitDuration(Duration resilienceWaitDuration) {
		this.resilienceWaitDuration = resilienceWaitDuration;
	}

	@Override
	public String toString() {
		return "ConjurProperties{" +
				"account='" + account + '\'' +
				", applianceUrl='" + applianceUrl + '\'' +
				", authnLogin='" + authnLogin + '\'' +
				", scanAllValues=" + scanAllValues +
				", integrationName='" + integrationName + '\'' +
                ", integrationVersion='" + integrationVersion + '\'' +
                ", vendorName='" + vendorName + '\'' +
                ", integrationType='" + integrationType + '\'' +
				'}';
	}
}
