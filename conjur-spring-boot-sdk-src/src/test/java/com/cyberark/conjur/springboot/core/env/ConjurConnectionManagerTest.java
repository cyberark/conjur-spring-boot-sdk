package com.cyberark.conjur.springboot.core.env;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;

import com.cyberark.conjur.sdk.AccessToken;
import com.cyberark.conjur.sdk.Configuration;
import com.cyberark.conjur.sdk.auth.ApiKeyAuth;
import com.cyberark.conjur.springboot.core.env.ConjurConnectionManagerTest.ConjurConnectionManagerMockConfig;
import com.cyberark.conjur.springboot.processor.SpringBootConjurAutoConfiguration;

/**
 * @author bnasslahsen
 */
@TestPropertySource(properties = { "conjur.authenticator-id=demo-cluster", "conjur.jwt-token-path=/home/bnl/test" })
@SpringBootTest(classes = { SpringBootConjurAutoConfiguration.class, ConjurConnectionManagerMockConfig.class })
public class ConjurConnectionManagerTest {

	private static final String SAMPLE_TOKEN = "eyJwcm90ZWN0ZWQiOiJleUpoYkdjaU9pSmpiMjVxZFhJdWIzSm5MM05zYjNOcGJHOHZkaklpTENKcmFXUWlPaUl3WkdZeE1tRmxOMlJoTWpZd01tWmhaR000WmpOak5XVTJNVGRrWkdJeVptTTFPV013WmpZelkyVmlaV0V6WW1RM09UVXdNakkyTm1VM05UUTJZamswSW4wPSIsInBheWxvYWQiOiJleUp6ZFdJaU9pSm9iM04wTDJSaGRHRXZZbTVzTDI5amNDMWhjSEJ6TDNONWMzUmxiVHB6WlhKMmFXTmxZV05qYjNWdWREcGlibXd0WkdWdGJ5MWhjSEF0Ym5NNlpHVnRieTFoY0hBdGFuZDBMWE5oSWl3aVpYaHdJam94Tmpnd09UWXlNall4TENKcFlYUWlPakUyT0RBNU5qRTNPREY5Iiwic2lnbmF0dXJlIjoiaFotVnluSDJMSWQ4TVBIUHI3cWlsYTdvdm9hTmpFZUVWWHg5UDhkbGVfZk10VTNJYXpfTkw0YXYySUtmMkhfR0xXRHFpc3MyRUpHMzFvLVVhMWR6OVkwQmRkVU41VG9GVS1zWEN2VTN5ei16alVIdE15cXRFanBlQTJGNHpQRXFTTklOMjVPUm9nY0xkZHpBbWlRMndZenM0M1ZQWFZqeUZELXd0YUNJRlNsTS1hNFlQTTR2T2NhVDBkaWRHU1hJWEk5bHI2cGRLbHRYMDg0N2dENkxDczhUVWN4TmpYSnJxbEhwNkNjT0F0S0JNbm1tZ1A3QWtlODk4Y1lLY0NxdUUtaDJIanFUdzBFRHRnWVU0Sl9LWTZvWmp6cnJsUGpLYW5EbmNBcWNnZnZ1TF9iZFRqeUQzRVJwb1pkMVBMTTBoVm5jWlVaZWx2RXpFYTV4RXlNcE9pTXFPbERhdzk0V2U3SFlpTTV4bUV1dVkycnBsZFFiVmVwV21lVkRSa052In0=";

	@Autowired
	private AccessTokenProvider accessTokenProviderMock;

	@TestConfiguration
	static class ConjurConnectionManagerMockConfig {

		@Bean
		@Primary
		public AccessTokenProvider accessTokenProviderMock() throws IOException {
			AccessTokenProvider accessTokenProviderMock = mock(AccessTokenProvider.class);
			when(accessTokenProviderMock.getJwtAccessToken(any(), any(), any()))
					.thenReturn(AccessToken.fromEncodedToken(SAMPLE_TOKEN));
			return accessTokenProviderMock;
		}

	}

	@Test
	public void testGetJwtAccessToken() throws IOException {
		// Verify that getJwtAccessToken is invoked
		verify(accessTokenProviderMock).getJwtAccessToken(any(), any(), any());
		// Check that the client has now the new Access Token
		ApiKeyAuth apiKeyAuth = ((ApiKeyAuth) Configuration.getDefaultApiClient().getAuthentications()
				.get("conjurAuth"));
		assertTrue(apiKeyAuth.getApiKey().contains(SAMPLE_TOKEN));
	}

}
