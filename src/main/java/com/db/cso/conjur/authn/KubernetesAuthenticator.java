package com.db.cso.conjur.authn;

import com.db.cso.conjur.domain.DapConfig;
import com.db.cso.conjur.exception.AuthenticationException;
import com.db.cso.conjur.util.HttpUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.openssl.MiscPEMGenerator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemWriter;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static com.db.cso.conjur.util.StringUtil.isEmpty;

public class KubernetesAuthenticator implements ConjurAuthenticator {

    private static final String SSL_CERT_INJECTION_PATH = "/etc/conjur/ssl/client.pem";

    @Override
    public String authenticate(DapConfig dapConfig) throws AuthenticationException {
        try {
            if (isEmpty(dapConfig.getServiceId()) || isEmpty(dapConfig.getPodName()) || isEmpty(dapConfig.getPodNamespace())) {
                System.out.println("mandatory inputs for kubernetes authentication not present. Please specify a serviceId, pod-name and pod-namespace.");
                return null;
            }
            KeyPair keypair = generateKeyPair();
            dapConfig.setCsr(generateCSR(keypair, dapConfig));
            login(dapConfig);
            return getToken(dapConfig, keypair, readCert());
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
    }

    private String getToken(DapConfig dapConfig, KeyPair keypair, String cert) throws IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, KeyStoreException, KeyManagementException {

        HttpPost httpPost = new HttpPost(dapConfig.getUrl() + "/api/authn-k8s/" + dapConfig.getServiceId() + "/"
                + dapConfig.getAccount() + "/" + URLEncoder.encode(dapConfig.getHostId(), "UTF-8") + "/authenticate");

        httpPost.setHeader("Content-Type", "text/plain");

        HttpUtil httpUtil = new HttpUtil();
        try (CloseableHttpClient httpClient = httpUtil.createHttpClientForMutualTLS(dapConfig.getDisableCertificateValidation()
                , keypair.getPrivate(), cert);
             CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity responseEntity = response.getEntity();
            return Base64.getEncoder().encodeToString(EntityUtils.toString(responseEntity, StandardCharsets.UTF_8).getBytes());
        }
    }

    private String readCert() throws IOException, InterruptedException {
        int retryCount = 0;
        int maxRetry = 10;
        while (!Paths.get(SSL_CERT_INJECTION_PATH).toFile().exists() && retryCount < maxRetry) {
            Thread.sleep(1000);
            retryCount++;
        }
        return Files.readAllLines(Paths.get(SSL_CERT_INJECTION_PATH)).stream().collect(Collectors.joining(System.lineSeparator()));
    }

    private String login(DapConfig dapConfig) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        HttpPost httpPost = new HttpPost(dapConfig.getUrl() + "/api/authn-k8s/" + dapConfig.getServiceId() + "/inject_client_cert");
        httpPost.setHeader("Content-Type", "text/plain");
        httpPost.setHeader("Host-Id-Prefix", dapConfig.getHostIdPrefix());
        httpPost.setEntity(new StringEntity(dapConfig.getCsr()));

        HttpUtil httpUtil = new HttpUtil();
        try (CloseableHttpClient httpClient = httpUtil.createHttpClient(dapConfig.getDisableCertificateValidation());
             CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity responseEntity = response.getEntity();
            return Base64.getEncoder().encodeToString(EntityUtils.toString(responseEntity).getBytes());
        }
    }

    private String generateCSR(KeyPair keypair, DapConfig dapConfig) throws OperatorCreationException, IOException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());


        X500Principal subject = new X500Principal("CN=" + dapConfig.getHostIdSuffix());
        PKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(
                subject, keypair.getPublic());

        String podSpiffeUrl = "spiffe://cluster.local/namespace/" + dapConfig.getPodNamespace() + "/podname/" + dapConfig.getPodName();

        GeneralName san = new GeneralName(GeneralName.uniformResourceIdentifier, podSpiffeUrl);
        List<GeneralName> subjectAltNames = new ArrayList<>();
        subjectAltNames.add(san);
        GeneralNames subjectAltName = new GeneralNames(subjectAltNames.toArray(new GeneralName[0]));
        ExtensionsGenerator extGen = new ExtensionsGenerator();
        extGen.addExtension(Extension.subjectAlternativeName, false, subjectAltName.toASN1Primitive());
        csrBuilder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, extGen.generate());

        PKCS10CertificationRequest csr = csrBuilder.build(new JcaContentSignerBuilder("SHA256withRSA").
                build(keypair.getPrivate()));

        StringWriter stringWriter = new StringWriter();
        PemWriter pemWriter = new PemWriter(stringWriter);
        PemObjectGenerator objGen = new MiscPEMGenerator(csr);
        pemWriter.writeObject(objGen);
        pemWriter.close();

        System.out.println("csr :" + stringWriter.toString());
        return stringWriter.toString();
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.genKeyPair();
    }

}
