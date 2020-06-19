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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import static com.db.cso.conjur.util.StringUtil.isEmpty;

public class ApiKeyBasedAuthenticator implements ConjurAuthenticator {
    @Override
    public String authenticate(DapConfig dapConfig) throws AuthenticationException {
        String apiKey = getApiKey(dapConfig);
        if(isEmpty(apiKey))
            return null;

        try {
            HttpPost httpPost = new HttpPost(dapConfig.getUrl() + "/authn/" + dapConfig.getAccount() + "/"
                    + dapConfig.getHostId() + "/authenticate");
            httpPost.setEntity(new StringEntity(apiKey));

            HttpUtil httpUtil = new HttpUtil();
            try (CloseableHttpClient httpClient = httpUtil.createHttpClient(dapConfig.getDisableCertificateValidation());
                 CloseableHttpResponse response = httpClient.execute(httpPost)) {

                HttpEntity responseEntity = response.getEntity();
                String res = EntityUtils.toString(responseEntity);
                return Base64.getEncoder().encodeToString(res.getBytes());
            }
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
    }

    private String getApiKey(DapConfig dapConfig) {
        if (!isEmpty(dapConfig.getApiKeyPath())) {
            try {
                dapConfig.setApiKey(Files.readAllLines(Paths.get(dapConfig.getApiKeyPath())).get(0));
                return dapConfig.getApiKey();
            } catch (IOException e) {
                System.out.println("apiKeyPath specified, but unable to read the specified file at "
                        + dapConfig.getApiKeyPath());
                return null;
            }
        } else {
            return dapConfig.getApiKey();
        }
    }
}
