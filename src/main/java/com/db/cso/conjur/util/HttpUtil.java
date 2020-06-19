package com.db.cso.conjur.util;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.StringReader;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class HttpUtil {

    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String KEY_ALIAS = "ssl_key";


    public CloseableHttpClient createHttpClient(String disableCertificateCheck) throws KeyStoreException
            , NoSuchAlgorithmException, KeyManagementException {
        if (Boolean.valueOf(disableCertificateCheck)) {
            TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
                    NoopHostnameVerifier.INSTANCE);

            return HttpClients.custom().setSSLSocketFactory(sslsf)
                    .setDefaultRequestConfig(requestConfig()).build();

        }
        return HttpClients.createDefault();

    }

    public CloseableHttpClient createHttpClientForMutualTLS(String disableCertificateCheck, PrivateKey privateKey
            , String sslCert) throws UnrecoverableEntryException, NoSuchAlgorithmException
            , KeyStoreException, KeyManagementException, CertificateException, IOException {

        String keyPass = "canb3r3pl@ced";
        if (Boolean.valueOf(disableCertificateCheck)) {
            TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
            SSLContext sslContext = SSLContexts.custom()
                    .loadKeyMaterial(
                            createKeyStore(privateKey, sslCert, keyPass),
                            keyPass.toCharArray(), (map, socket) -> KEY_ALIAS

                    )
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
                    NoopHostnameVerifier.INSTANCE);

            return HttpClients.custom().setSSLSocketFactory(sslsf)
                    .setDefaultRequestConfig(requestConfig()).build();

        } else {
            SSLContext sslContext = SSLContexts.custom()
                    .loadKeyMaterial(
                            createKeyStore(privateKey, sslCert, keyPass),
                            keyPass.toCharArray(), (map, socket) -> KEY_ALIAS

                    )
                    .build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
                    NoopHostnameVerifier.INSTANCE);

            return HttpClients.custom().setSSLSocketFactory(sslsf)
                    .setDefaultRequestConfig(requestConfig()).build();
        }
    }

    private static RequestConfig requestConfig() {
        int timeout = 5000;
        return RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
    }

    private static java.security.KeyStore createKeyStore(PrivateKey privateKey, String cert, String keyPass)
            throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {

        PEMParser parser = new PEMParser(new StringReader(cert));
        java.security.KeyStore ks = java.security.KeyStore.getInstance(KEYSTORE_TYPE);
        ks.load(null);

        List<X509Certificate> certificates = new ArrayList<>();

        X509Certificate certificate;
        while ((certificate = parseCert(parser)) != null) {
            certificates.add(certificate);
        }

        ks.setKeyEntry(KEY_ALIAS, privateKey, keyPass.toCharArray(), certificates.toArray(new X509Certificate[]{}));

        parser.close();
        return ks;
    }

    private static X509Certificate parseCert(PEMParser parser) throws IOException, CertificateException {
        X509CertificateHolder certHolder = (X509CertificateHolder) parser.readObject();
        if (certHolder == null) {
            return null;
        }
        return new JcaX509CertificateConverter().getCertificate(certHolder);
    }

}
