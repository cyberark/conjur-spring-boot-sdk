package com.cyberark.conjur.springboot.core.env;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.KeyManagerFactory;

import com.cyberark.conjur.sdk.AccessToken;
import com.cyberark.conjur.sdk.ApiClient;
import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.sdk.ApiResponse;
import com.cyberark.conjur.sdk.endpoint.AuthenticationApi;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Access token provider.
 *
 * @author bnasslahsen
 */
public class AccessTokenProvider {

	private static Logger logger = LoggerFactory.getLogger(AccessTokenProvider.class);

	/**
	 * Get new access token access token.
	 *
	 * @param conjurClient the conjur client
	 * @return the access token
	 */
	public AccessToken getNewAccessToken (ApiClient conjurClient){
		return conjurClient.getNewAccessToken();
	}

	/**
	 * Gets jwt access token.
	 *
	 * @param conjurClient the conjur client
	 * @param jwtTokenPath the jwt token path
	 * @param authenticatorId the authenticator id
	 * @return the jwt access token
	 * @throws IOException the io exception
	 */
	public AccessToken getJwtAccessToken(ApiClient conjurClient, String jwtTokenPath, String authenticatorId) throws IOException {
		AccessToken accessToken = null;
		AuthenticationApi apiInstance = new AuthenticationApi(conjurClient);
		String xRequestId = UUID.randomUUID().toString();
		String jwt = new String(Files.readAllBytes(Paths.get(jwtTokenPath)));
		try {
			String accessTokenStr = apiInstance.getAccessTokenViaJWT(conjurClient.getAccount(), authenticatorId, xRequestId, jwt);
			accessToken = AccessToken.fromEncodedToken(Base64.getEncoder().encodeToString(accessTokenStr.getBytes(StandardCharsets.UTF_8)));
		}
		catch (ApiException e) {
			logger.error("Status code: " + e.getCode());
			logger.error("Reason: " + e.getResponseBody());
			logger.error(e.getMessage());
		}
		return accessToken;
	}

	/**
	 * Gets a Conjur access token using client certificate (authn-cert / mTLS) authentication.
	 * <p>
	 * The method configures mutual TLS on the provided {@link ApiClient} by loading the client
	 * certificate and private key, then POSTs to
	 * {@code /authn-cert/{serviceId}/{account}[/{workloadId}]/authenticate}.
	 * <p>
	 * The private key must be in PKCS#8 PEM format (PEM header: {@code BEGIN PRIVATE KEY}).
	 * To convert a legacy PKCS#1 key:
	 * <pre>{@code openssl pkcs8 -topk8 -nocrypt -in key.pem -out key-pkcs8.pem}</pre>
	 * Supported key algorithms: RSA, EC.
	 *
	 * @param conjurClient   the Conjur API client
	 * @param serviceId      the authn-cert authenticator service ID (e.g. {@code "acme-vm"})
	 * @param workloadId     the workload/host ID to embed in the URL path (URL-encoded automatically),
	 *                       e.g. {@code "host/vm-workloads/vm-01"}; pass {@code null} or empty for
	 *                       SPIFFE host-mode where the host is derived from the certificate SAN
	 * @param clientCertFile path to the PEM-encoded X.509 client certificate file
	 * @param clientKeyFile  path to the PEM-encoded PKCS#8 private key file
	 * @return the access token, or {@code null} if authentication fails
	 * @throws IOException              if the certificate or key file cannot be read
	 * @throws GeneralSecurityException if the certificate or key cannot be loaded or parsed
	 */
	public AccessToken getCertAccessToken(ApiClient conjurClient, String serviceId, String workloadId,
			String clientCertFile, String clientKeyFile) throws IOException, GeneralSecurityException {

		X509Certificate clientCert;
		byte[] certBytes = Files.readAllBytes(Paths.get(clientCertFile));
		try (ByteArrayInputStream certStream = new ByteArrayInputStream(certBytes)) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			clientCert = (X509Certificate) cf.generateCertificate(certStream);
		}
		PrivateKey privateKey = loadPrivateKeyFromPem(clientKeyFile);
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(null, null);
		char[] emptyPassword = new char[0];
		keyStore.setKeyEntry("client", privateKey, emptyPassword, new Certificate[]{clientCert});
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(keyStore, emptyPassword);
		conjurClient.setKeyManagers(kmf.getKeyManagers());

		//    request mode: /authn-cert/{serviceId}/{account}/{workloadId}/authenticate
		//    SPIFFE mode:  /authn-cert/{serviceId}/{account}/authenticate
		String path;
		if (workloadId != null && !workloadId.isEmpty()) {
			path = "/authn-cert/" + conjurClient.escapeString(serviceId)
					+ "/" + conjurClient.escapeString(conjurClient.getAccount())
					+ "/" + conjurClient.escapeString(workloadId)
					+ "/authenticate";
		} else {
			path = "/authn-cert/" + conjurClient.escapeString(serviceId)
					+ "/" + conjurClient.escapeString(conjurClient.getAccount())
					+ "/authenticate";
		}

		Map<String, String> headerParams = new HashMap<>();
		headerParams.put("Accept-Encoding", "base64");

		try {
			okhttp3.Call call = conjurClient.buildCall(path, "POST",
					new ArrayList<>(), new ArrayList<>(),
					null, headerParams, new HashMap<>(), new HashMap<>(),
					new String[]{}, null);
			ApiResponse<String> response = conjurClient.execute(call, new TypeToken<String>(){}.getType());
			return AccessToken.fromEncodedToken(response.getData());
		} catch (ApiException e) {
			logger.error("Certificate authentication failed - status code: {}", e.getCode());
			logger.error("Reason: {}", e.getResponseBody());
			logger.error(e.getMessage());
			return null;
		}
	}

	/**
	 * Loads a {@link PrivateKey} from a PKCS#8 PEM file.
	 * Supports RSA and EC key types.
	 * The PEM file must use a {@code BEGIN PRIVATE KEY} header.
	 *
	 * @param keyFilePath path to the PEM file
	 * @return the loaded {@link PrivateKey}
	 * @throws IOException              if the file cannot be read
	 * @throws GeneralSecurityException if the key cannot be parsed or the algorithm is unsupported
	 */
	PrivateKey loadPrivateKeyFromPem(String keyFilePath) throws IOException, GeneralSecurityException {
		String pem = new String(Files.readAllBytes(Paths.get(keyFilePath)), StandardCharsets.UTF_8);
		String base64Key = pem
				.replaceAll("-----BEGIN.*?-----", "")
				.replaceAll("-----END.*?-----", "")
				.replaceAll("\\s+", "");

		byte[] keyDer = Base64.getDecoder().decode(base64Key);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyDer);

		for (String algorithm : new String[]{"RSA", "EC"}) {
			try {
				return KeyFactory.getInstance(algorithm).generatePrivate(keySpec);
			} catch (InvalidKeySpecException ignored) {
				// try next algorithm
			}
		}
		throw new GeneralSecurityException(
				"Cannot load private key from '" + keyFilePath + "'. "
				+ "Key must be in PKCS#8 PEM format (-----BEGIN PRIVATE KEY-----). " // gitleaks:allow
				+ "To convert a PKCS#1 key: openssl pkcs8 -topk8 -nocrypt -in key.pem -out key-pkcs8.pem");
	}
}
