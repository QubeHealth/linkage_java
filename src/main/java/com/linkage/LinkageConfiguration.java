package com.linkage;

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
    private String messageProviderTemplateUrl;
    @NotEmpty
    private String messageProviderSendMessageUrl;
    @NotEmpty
    private String messageProviderToken;
    @NotEmpty
    private String messageProviderSource;
    @NotEmpty
    private String messageProviderChannel;
    @NotEmpty
    private String messageProviderSrcName;
    @NotEmpty
    private String befiscAuthKey;

    @NotEmpty
    private String digitapUrl;
    @NotEmpty
    private String digitapClientId;
    @NotEmpty
    private String digitapClientSecret;

    public String getxApiKey() {
        return xApiKey;
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

    public String getMessageProviderTemplateUrl() {
        return messageProviderTemplateUrl;
    }

    public void setMessageProviderTemplateUrl(String messageProviderTemplateUrl) {
        this.messageProviderTemplateUrl = messageProviderTemplateUrl;
    }

    public String getMessageProviderSendMessageUrl() {
        return messageProviderSendMessageUrl;
    }

    public void setMessageProviderSendMessageUrl(String messageProviderSendMessageUrl) {
        this.messageProviderSendMessageUrl = messageProviderSendMessageUrl;
    }
    public String getMessageProviderToken() {
        return messageProviderToken;
    }

    public void setMessageProviderToken(String messageProviderToken) {
        this.messageProviderToken = messageProviderToken;
    }

    public String getMessageProviderSource() {
        return messageProviderSource;
    }

    public void setMessageProviderSource(String messageProviderSource) {
        this.messageProviderSource = messageProviderSource;
    }

    public String getMessageProviderChannel() {
        return messageProviderChannel;
    }

    public void setMessageProviderChannel(String messageProviderChannel) {
        this.messageProviderChannel = messageProviderChannel;
    }


    public String getMessageProviderSrcName() {
        return messageProviderSrcName;
    }

    public void setMessageProviderSrcName(String messageProviderSrcName) {
        this.messageProviderSrcName = messageProviderSrcName;
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

}
