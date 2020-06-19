package com.db.cso.conjur.domain;

import lombok.Data;

import java.util.Arrays;
import java.util.stream.Collectors;

@Data
public class DapConfig {
    private String account;
    private String url;
    private String hostId;
    private String serviceId;
    private String namespace;
    private String csr;
    private String podName;
    private String podNamespace;
    private String disableCertificateValidation;
    private String apiKey;
    private String apiKeyPath;

    public String getHostIdPrefix() {
        String[] hostIdParts = this.hostId.split("/");
        try {
            return Arrays.stream(hostIdParts, 0, 4).collect(Collectors.joining("."));
        }catch(Exception e){
            return hostId;
        }
    }

    public String getHostIdSuffix() {
        String[] hostIdParts = this.hostId.split("/");
        try {
            return Arrays.stream(hostIdParts, 4, 8).collect(Collectors.joining("."));
        } catch (Exception e) {
            return hostId;
        }
    }

}
