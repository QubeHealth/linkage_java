package com.linkage;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.core.Configuration;
import jakarta.validation.constraints.NotEmpty;

public class LinkageConfiguration extends Configuration {
    @NotEmpty
    private String jwtTokenSignature;
    @NotEmpty
    private String authorizationKey;
    @NotEmpty
    private String xApiKey;
    @NotEmpty
    private String firebaseWebApiKey;
    @NotEmpty
    private String watiUrl;
    @NotEmpty
    private String watiToken;
    @NotEmpty
    private String befiscAuthKey;

    @NotEmpty
    private String digitapUrl;
    @NotEmpty
    private String digitapClientId;
    @NotEmpty
    private String digitapClientSecret;
    @NotEmpty
    private String hereApiKey;

    @NotEmpty
    private String appSignature;

 

    public String appSignature() {
        return xApiKey;
    }
    public void setAppSignature(String appSignature) {
        this.appSignature = appSignature;
    }

    public String getxApiKey() {
        return appSignature;
    }

    public void setxApiKey(String xApiKey) {
        this.xApiKey = xApiKey;
    }

    public String getJwtTokenSignature() {
        return jwtTokenSignature;
    }

    public void setJwtTokenSignature(String jwtTokenSignature) {
        this.jwtTokenSignature = jwtTokenSignature;
    }

    public String getAuthorizationKey() {
        return authorizationKey;
    }

    public void setAuthorizationKey(String authorizationKey) {
        this.authorizationKey = authorizationKey;
    }

    public String getFirebaseWebApiKey() {
        return firebaseWebApiKey;
    }

    public void setFirebaseWebApiKey(String firebaseWebApiKey) {
        this.firebaseWebApiKey = firebaseWebApiKey;
    }

    public String getBefiscAuthKey() {
        return befiscAuthKey;
    }

    public void setBefiscAuthKey(String befiscAuthKey) {
        this.befiscAuthKey = befiscAuthKey;
    }

    public String getWatiUrl() {
        return watiUrl;
    }

    public void setWatiUrl(String watiUrl) {
        this.watiUrl = watiUrl;
    }

    public String getWatiToken() {
        return watiToken;
    }

    public void setWatiToken(String watiToken) {
        this.watiToken = watiToken;
    }

    public String getDigitapClientId() {
        return digitapClientId;
    }

    public void setDigitapClientId(String digitapClientId) {
        this.digitapClientId = digitapClientId;
    }

    public String getDigitapClientSecret() {
        return digitapClientSecret;
    }

    public void setDigitapClientSecret(String digitapClientSecret) {
        this.digitapClientSecret = digitapClientSecret;
    }

    public String getDigitapUrl() {
        return digitapUrl;
    }

    public void setDigitapUrl(String digitapUrl) {
        this.digitapUrl = digitapUrl;
    }

    public String getHereApiKey() {
        return hereApiKey;

    }

    public void setHereApiKey(String hereApiKey) {
        this.hereApiKey=hereApiKey;
    }

    @JsonProperty("sms")
    private SmsConfig smsConfig;

    public SmsConfig getSmsConfig() {
        return smsConfig;
    }

    public static class SmsConfig {
        @NotEmpty
        private String dltOtpSmsTemplateId;

        @NotEmpty
        private String gupshupEndpoint;

        @NotEmpty
        private String dltPrincipalEntityId;

        @NotEmpty
        private String gupshupUserId;

        @NotEmpty
        private String gupshupPassword;

        // Getters
        public String getDltOtpSmsTemplateId() {
            return dltOtpSmsTemplateId;
        }

        public String getGupshupEndpoint() {
            return gupshupEndpoint;
        }
        
        public String getDltPrincipalEntityId() {
            return dltPrincipalEntityId;
        }

        public String getGupshupUserId() {
            return gupshupUserId;
        }

        public String getGupshupPassword() {
            return gupshupPassword;
        }
    }
}
