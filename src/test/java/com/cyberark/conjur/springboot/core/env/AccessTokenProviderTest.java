package com.cyberark.conjur.springboot.core.env;

import com.cyberark.conjur.sdk.AccessToken;
import com.cyberark.conjur.sdk.ApiClient;
import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.sdk.ApiResponse;
import com.cyberark.conjur.sdk.endpoint.AuthenticationApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AccessTokenProviderTest {


    private AccessTokenProvider accessTokenProvider;

    @BeforeEach
    void setUp() {
        accessTokenProvider = new AccessTokenProvider();
    }

    @Test
    void testGetNewAccessToken() {
        ApiClient apiClient = mock(ApiClient.class);
        AccessToken mockToken = mock(AccessToken.class);
        when(apiClient.getNewAccessToken()).thenReturn(mockToken);

        AccessToken result = accessTokenProvider.getNewAccessToken(apiClient);

        assertSame(mockToken, result);
        verify(apiClient).getNewAccessToken();
    }

    @Test
    void testGetJwtAccessToken_FileNotFound() {
        ApiClient apiClient = mock(ApiClient.class);
        String invalidPath = "invalid/file/path.jwt";

        assertThrows(IOException.class, () -> accessTokenProvider.getJwtAccessToken(apiClient, invalidPath, "authn-jwt-id"));
    }

    @Test
    void testGetJwtAccessToken_Success(@TempDir Path tempDir) throws Exception {
        // Create dummy JWT file
        String jwtContent = "dummy-jwt-token";
        Path jwtFile = tempDir.resolve("jwt.txt");
        Files.write(jwtFile, jwtContent.getBytes(StandardCharsets.UTF_8));

        // Mock ApiClient
        ApiClient apiClient = mock(ApiClient.class);
        when(apiClient.getAccount()).thenReturn("my-account");

        // Mock AuthenticationApi to return fake token JSON
        AuthenticationApi authApiMock = mock(AuthenticationApi.class);
        String tokenJson = "{\"protected\":\"abc\",\"payload\":\"xyz\",\"signature\":\"123\"}";
        when(authApiMock.getAccessTokenViaJWT(eq("my-account"), eq("authn-jwt-id"), eq("api"), eq(jwtContent))).thenReturn(tokenJson);

        // Mock AuthenticationApi constructor using mockito-inline
        try (MockedConstruction<AuthenticationApi> mocked = mockConstruction(AuthenticationApi.class, (mock, context) -> {
            when(mock.getAccessTokenViaJWT(anyString(), anyString(), anyString(), eq(jwtContent))).thenReturn(tokenJson);
        })) {

            AccessTokenProvider provider = new AccessTokenProvider();

            AccessToken result = provider.getJwtAccessToken(apiClient, jwtFile.toString(), "authn-jwt-id");

            assertNotNull(result);
            String expectedHeader = AccessToken.fromEncodedToken(Base64.getEncoder().encodeToString(tokenJson.getBytes(StandardCharsets.UTF_8))).getHeaderValue();
            assertEquals(expectedHeader, result.getHeaderValue());
        }
    }

    @Test
    void testGetJwtAccessToken_ApiExceptionCaught(@TempDir Path tempDir) throws Exception {
        // Prepare fake JWT file
        String jwt = "dummy-jwt-token";
        Path jwtFile = tempDir.resolve("jwt.txt");
        Files.write(jwtFile, jwt.getBytes(StandardCharsets.UTF_8));

        // Mock ApiClient
        ApiClient apiClientMock = mock(ApiClient.class);
        when(apiClientMock.getAccount()).thenReturn("my-account");

        try (MockedConstruction<AuthenticationApi> mocked = mockConstruction(AuthenticationApi.class, (mock, context) -> {
            when(mock.getAccessTokenViaJWT(any(), any(), any(), any())).thenThrow(new ApiException(403, "Forbidden", null, "{\"error\": \"unauthorized\"}"));
        })) {

            AccessTokenProvider provider = new AccessTokenProvider();

            AccessToken token = provider.getJwtAccessToken(apiClientMock, jwtFile.toString(), "authn-jwt-id");

            // Assert: Should return null due to exception
            assertNull(token);
        }
    }

    @Test
    void testLoadPrivateKeyFromPem_rsa(@TempDir Path tempDir) throws Exception {
        Path keyFile = createRsaKeyFile(tempDir, "key.pem");

        AccessTokenProvider provider = new AccessTokenProvider();
        PrivateKey pk = provider.loadPrivateKeyFromPem(keyFile.toString());

        assertNotNull(pk);
        assertEquals("RSA", pk.getAlgorithm());
    }

    @Test
    void testLoadPrivateKeyFromPem_ec(@TempDir Path tempDir) throws Exception {
        Path keyFile = createEcKeyFile(tempDir, "key-ec.pem");

        AccessTokenProvider provider = new AccessTokenProvider();
        PrivateKey pk = provider.loadPrivateKeyFromPem(keyFile.toString());

        assertNotNull(pk);
        assertEquals("EC", pk.getAlgorithm());
    }

    @Test
    void testLoadPrivateKeyFromPem_fileNotFound() {
        AccessTokenProvider provider = new AccessTokenProvider();
        assertThrows(IOException.class, () -> provider.loadPrivateKeyFromPem("nonexistent/key.pem"));
    }

    @Test
    void testLoadPrivateKeyFromPem_invalidKeyBytes(@TempDir Path tempDir) throws Exception {
        Path keyFile = tempDir.resolve("bad-key.pem");
        Files.write(keyFile, ("-----BEGIN PRIVATE KEY-----\n" // gitleaks:allow
                + Base64.getEncoder().encodeToString("not-a-real-key".getBytes()) + "\n"
                + "-----END PRIVATE KEY-----").getBytes(StandardCharsets.UTF_8));

        AccessTokenProvider provider = new AccessTokenProvider();
        assertThrows(java.security.GeneralSecurityException.class,
                () -> provider.loadPrivateKeyFromPem(keyFile.toString()));
    }

    @Test
    void testGetCertAccessToken_certFileNotFound(@TempDir Path tempDir) throws Exception {
        ApiClient mockClient = mock(ApiClient.class);
        Path keyFile = createRsaKeyFile(tempDir, "key.pem");

        assertThrows(IOException.class, () ->
                accessTokenProvider.getCertAccessToken(mockClient, "acme-vm", "host/workloads/vm-01",
                        "nonexistent/cert.pem", keyFile.toString()));
    }

    @Test
    void testGetCertAccessToken_keyFileNotFound(@TempDir Path tempDir) throws Exception {
        ApiClient mockClient = mock(ApiClient.class);
        Path[] certAndKey = buildCertAndKeyFiles(tempDir);
        Path certFile = certAndKey[0];

        assertThrows(IOException.class, () ->
                accessTokenProvider.getCertAccessToken(mockClient, "acme-vm", "host/workloads/vm-01",
                        certFile.toString(), "nonexistent/key.pem"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetCertAccessToken_successWithWorkloadId(@TempDir Path tempDir) throws Exception {
        Path[] certAndKey = buildCertAndKeyFiles(tempDir);
        Path certFile = certAndKey[0];
        Path keyFile  = certAndKey[1];

        ApiClient mockClient = mock(ApiClient.class);
        when(mockClient.getAccount()).thenReturn("myaccount");
        when(mockClient.escapeString(anyString())).thenAnswer(inv -> inv.getArgument(0, String.class));
        when(mockClient.setKeyManagers(any())).thenReturn(mockClient);

        okhttp3.Call mockCall = mock(okhttp3.Call.class);
        when(mockClient.buildCall(anyString(), eq("POST"), any(), any(), isNull(), any(), any(), any(),
                any(), isNull())).thenReturn(mockCall);
        String base64Token = Base64.getEncoder().encodeToString(
                "{\"protected\":\"p\",\"payload\":\"q\",\"signature\":\"s\"}".getBytes());
        ApiResponse<String> fakeResponse = new ApiResponse<>(200, null, base64Token);
        when(mockClient.execute(eq(mockCall), any())).thenAnswer(inv -> fakeResponse);

        AccessToken result = accessTokenProvider.getCertAccessToken(
                mockClient, "acme-vm", "host/workloads/vm-01",
                certFile.toString(), keyFile.toString());

        assertNotNull(result);
        verify(mockClient).buildCall(
                contains("/authn-cert/acme-vm/myaccount/host/workloads/vm-01/authenticate"),
                eq("POST"), any(), any(), isNull(), any(), any(), any(), any(), isNull());
        verify(mockClient).setKeyManagers(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetCertAccessToken_spiffeMode_noWorkloadId(@TempDir Path tempDir) throws Exception {
        Path[] certAndKey = buildCertAndKeyFiles(tempDir);

        ApiClient mockClient = mock(ApiClient.class);
        when(mockClient.getAccount()).thenReturn("myaccount");
        when(mockClient.escapeString(anyString())).thenAnswer(inv -> inv.getArgument(0, String.class));
        when(mockClient.setKeyManagers(any())).thenReturn(mockClient);

        okhttp3.Call mockCall = mock(okhttp3.Call.class);
        when(mockClient.buildCall(anyString(), eq("POST"), any(), any(), isNull(), any(), any(), any(),
                any(), isNull())).thenReturn(mockCall);
        String base64Token = Base64.getEncoder().encodeToString(
                "{\"protected\":\"p\",\"payload\":\"q\",\"signature\":\"s\"}".getBytes());
        when(mockClient.execute(eq(mockCall), any()))
                .thenAnswer(inv -> new ApiResponse<>(200, null, base64Token));

        accessTokenProvider.getCertAccessToken(
                mockClient, "acme-vm", "",
                certAndKey[0].toString(), certAndKey[1].toString());

        verify(mockClient).buildCall(
                contains("/authn-cert/acme-vm/myaccount/authenticate"),
                eq("POST"), any(), any(), isNull(), any(), any(), any(), any(), isNull());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetCertAccessToken_apiException_returnsNull(@TempDir Path tempDir) throws Exception {
        Path[] certAndKey = buildCertAndKeyFiles(tempDir);

        ApiClient mockClient = mock(ApiClient.class);
        when(mockClient.getAccount()).thenReturn("myaccount");
        when(mockClient.escapeString(anyString())).thenAnswer(inv -> inv.getArgument(0, String.class));
        when(mockClient.setKeyManagers(any())).thenReturn(mockClient);

        okhttp3.Call mockCall = mock(okhttp3.Call.class);
        when(mockClient.buildCall(anyString(), eq("POST"), any(), any(), isNull(), any(), any(), any(),
                any(), isNull())).thenReturn(mockCall);
        when(mockClient.execute(eq(mockCall), any()))
                .thenThrow(new ApiException(401, "Unauthorized", null, "bad cert"));

        AccessToken result = accessTokenProvider.getCertAccessToken(
                mockClient, "acme-vm", "host/vm",
                certAndKey[0].toString(), certAndKey[1].toString());

        assertNull(result);
    }

    /** Generates an RSA 1024-bit PKCS#8 PEM key file for tests. */
    private Path createRsaKeyFile(Path dir, String filename) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024, new SecureRandom());
        KeyPair kp = kpg.generateKeyPair();
        return writePkcs8KeyFile(dir, filename, kp.getPrivate());
    }

    /** Generates an EC (P-256) PKCS#8 PEM key file for tests. */
    private Path createEcKeyFile(Path dir, String filename) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(256, new SecureRandom());
        KeyPair kp = kpg.generateKeyPair();
        return writePkcs8KeyFile(dir, filename, kp.getPrivate());
    }

    private Path writePkcs8KeyFile(Path dir, String filename, PrivateKey key) throws IOException {
        String pem = "-----BEGIN PRIVATE KEY-----\n" // gitleaks:allow
                + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8)).encodeToString(key.getEncoded())
                + "\n-----END PRIVATE KEY-----";
        Path file = dir.resolve(filename);
        Files.write(file, pem.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    /**
     * Generates a matched RSA cert + key pair using the JDK's internal
     * {@code sun.security.tools.keytool.CertAndKeyGen} via reflection.
     * <p>
     * The generated cert and key are internally consistent (cert is signed by
     * the generated private key). If the JVM restricts access to that class,
     * the calling test is skipped via {@code Assumptions.assumeTrue}.
     *
     * @return [certFile, keyFile] as a two-element array
     */
    private Path[] buildCertAndKeyFiles(Path dir) throws Exception {
        try {
            Class<?> certAndKeyGenClass = Class.forName("sun.security.tools.keytool.CertAndKeyGen");
            Object certGen = certAndKeyGenClass
                    .getDeclaredConstructor(String.class, String.class)
                    .newInstance("RSA", "SHA256WithRSA");
            certAndKeyGenClass.getMethod("generate", int.class).invoke(certGen, 1024);

            Class<?> x500NameClass = Class.forName("sun.security.x509.X500Name");
            Object x500Name = x500NameClass.getDeclaredConstructor(String.class).newInstance("CN=test");
            long validSeconds = 365L * 24 * 3600;

            java.security.cert.X509Certificate cert =
                    (java.security.cert.X509Certificate) certAndKeyGenClass
                            .getMethod("getSelfCertificate", x500NameClass, long.class)
                            .invoke(certGen, x500Name, validSeconds);

            PrivateKey privateKey = (PrivateKey) certAndKeyGenClass
                    .getMethod("getPrivateKey")
                    .invoke(certGen);

            String certPem = "-----BEGIN CERTIFICATE-----\n"
                    + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8))
                            .encodeToString(cert.getEncoded())
                    + "\n-----END CERTIFICATE-----\n";
            Path certFile = dir.resolve("test-cert.pem");
            Files.write(certFile, certPem.getBytes(StandardCharsets.UTF_8));

            Path keyFile = writePkcs8KeyFile(dir, "test-key.pem", privateKey);
            return new Path[]{certFile, keyFile};

        } catch (Exception e) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false,
                    "sun.security cert-generation API not accessible — skipping: " + e.getMessage());
            return null;
        }
    }
}

