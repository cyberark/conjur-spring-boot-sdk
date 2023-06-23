package com.cyberark.conjur.springboot.core.env;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import com.cyberark.conjur.sdk.AccessToken;
import com.cyberark.conjur.sdk.ApiClient;
import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.sdk.endpoint.AuthenticationApi;
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
}
