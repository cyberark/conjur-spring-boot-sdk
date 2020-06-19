package com.db.cso.conjur.spring;

import com.db.cso.conjur.authn.ApiKeyBasedAuthenticator;
import com.db.cso.conjur.authn.ConjurAuthenticator;
import com.db.cso.conjur.authn.KubernetesAuthenticator;
import com.db.cso.conjur.domain.DapConfig;
import com.db.cso.conjur.util.HttpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.db.cso.conjur.util.StringUtil.isEmpty;

public class ConjurLookup {

    private HttpUtil httpUtil;
    private DapConfig dapConfig;

    public ConjurLookup(ConfigurableEnvironment environment) {
        dapConfig = createConfig(environment);
        httpUtil = new HttpUtil();
    }

    public Map<String, Object> fetchSecrets() {

        if (isEmpty(dapConfig.getUrl()) || isEmpty(dapConfig.getAccount()) || isEmpty(dapConfig.getHostId())
                || isEmpty(dapConfig.getNamespace())) {
            System.out.println("Mandatory inputs not provided for Conjur look-up. Values will not be fetched from Conjur.");
            return null;
        }

        try {
            ConjurAuthenticator authenticator = getAuthenticator();
            String token = authenticator.authenticate(dapConfig);
            System.out.println("token got :" + token);
            if (isEmpty(token)) {
                System.out.println("Conjur authentication failed. Please validate the input specified.");
                return null;
            }

            String secretResources = getSecretResources(token);
            if (secretResources.isEmpty()) {
                System.out.println("found no variables to make available. please validate the namespace provided" +
                        " and ensure the account used to authenticate has access to this namespace.");
                return null;
            }
            String secrets = getSecretValues(secretResources, token);
            String keyTokenizer = dapConfig.getAccount() + ":variable:" + dapConfig.getNamespace() + "/";
            Map<String, Object> mapWithVariableAndValue = new HashMap<>();
            toMap(secrets).entrySet().stream().forEach(map -> mapWithVariableAndValue.put(map.getKey().split(keyTokenizer)[1], map.getValue()));
            return mapWithVariableAndValue;
        } catch (Exception e) {
            //TODO only log this. dont break the application startup
            e.printStackTrace();
        }
        return null;
    }

    private String getSecretValues(String secretResources, String token) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        HttpGet get = new HttpGet(dapConfig.getUrl() + "/secrets?variable_ids=" + secretResources);
        get.setHeader("Authorization", "Token token=\"" + token + "\"");
        get.setHeader("Accept", "application/json");
        try (CloseableHttpClient httpClient = httpUtil.createHttpClient(dapConfig.getDisableCertificateValidation());
             CloseableHttpResponse response = httpClient.execute(get);) {
            HttpEntity responseEntity = response.getEntity();
            String secrets = EntityUtils.toString(responseEntity);
            return secrets;
        }
    }

    private String getSecretResources(String token) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        HttpGet get = new HttpGet(dapConfig.getUrl() + "/resources/" + dapConfig.getAccount() + "?kind=variable&search=" + dapConfig.getNamespace());
        get.setHeader("Authorization", "Token token=\"" + token + "\"");
        get.setHeader("Accept", "application/json");
        try (CloseableHttpClient httpClient = httpUtil.createHttpClient(dapConfig.getDisableCertificateValidation());
             CloseableHttpResponse response = httpClient.execute(get)) {
            HttpEntity responseEntity = response.getEntity();
            String jsonResponse = EntityUtils.toString(responseEntity);
            System.out.println("retrieved the following entries :" + jsonResponse);
            return toList(jsonResponse).stream().map(a -> a.get("id")).map(a -> a.toString()).collect(Collectors.joining(","));
        }
    }


    private List<Map<String, Object>> toList(String responseBodyInJSON) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(responseBodyInJSON, new TypeReference<List<Map<String, Object>>>() {
        });
    }

    private Map<String, Object> toMap(String responseBodyInJSON) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(responseBodyInJSON, Map.class);
    }

    private ConjurAuthenticator getAuthenticator() {
        if (!isEmpty(dapConfig.getApiKey()) || !isEmpty(dapConfig.getApiKeyPath())) {
            System.out.println("using api based authenticator");
            return new ApiKeyBasedAuthenticator();
        }
        System.out.println("using kubernetes authenticator");
        return new KubernetesAuthenticator();

    }

    private DapConfig createConfig(ConfigurableEnvironment environment) {
        DapConfig dapConfig = new DapConfig();
        dapConfig.setUrl(environment.getProperty("conjur.spring.url"));
        dapConfig.setAccount(environment.getProperty("conjur.spring.account"));
        dapConfig.setHostId(environment.getProperty("conjur.spring.login"));
        dapConfig.setNamespace(environment.getProperty("conjur.spring.namespace"));
        dapConfig.setApiKey(environment.getProperty("conjur.spring.apiKey"));
        dapConfig.setApiKeyPath(environment.getProperty("conjur.spring.apiKeyPath"));
        dapConfig.setDisableCertificateValidation(environment.getProperty("conjur.spring.disableCertificateCheck"));
        dapConfig.setPodName(environment.getProperty("MY_POD_NAME"));
        dapConfig.setPodNamespace(environment.getProperty("MY_POD_NAMESPACE"));
        dapConfig.setServiceId(environment.getProperty("conjur.spring.serviceId"));
        System.out.println("dap config :" + dapConfig);
        return dapConfig;
    }
}
