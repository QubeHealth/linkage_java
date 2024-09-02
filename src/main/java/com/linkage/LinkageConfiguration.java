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
    private String kaleyraBaseUrl;
    @NotEmpty
    private String kaleyraApiKey;
    @NotEmpty
    private String kaleyraSid;
    @NotEmpty
    private String kaleyraSenderId;
    @NotEmpty
    private String kayeraPaymentPendingTemplateId;
    @NotEmpty
    private String kayeraPaymentFailedTemplateId;

    @NotEmpty
    private String userUrl;

    @NotEmpty
    private String emailSmtp;
    @NotEmpty
    private String emailPassword;
    @NotEmpty
    private String emailHost;

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

    public String getEmailSmtp() {
        return emailSmtp;
    }

    public void setEmailSmtp(String emailSmtp) {
        this.emailSmtp = emailSmtp;
    }

    public String getEmailPassword() {
        return emailPassword;
    }

    public void setEmailPassword(String emailPassword) {
        this.emailPassword = emailPassword;
    }

    public String getEmailHost() {
        return emailHost;
    }

    public void setEmailHost(String emailHost) {
        this.emailHost = emailHost;
    }

    public String getHereApiKey() {
        return hereApiKey;

    }

    public void setHereApiKey(String hereApiKey) {
        this.hereApiKey=hereApiKey;
    }
    public String getKaleyraBaseUrl() {
        return kaleyraBaseUrl;

    }
    public void setKaleyraBaseUrl(String kaleyraBaseUrl) {
        this.kaleyraBaseUrl=kaleyraBaseUrl;
    }
    public String getKaleyraApiKey() {
        return kaleyraApiKey;

    }
    public void setKaleyraApiKey(String kaleyraApiKey) {
        this.kaleyraApiKey=kaleyraApiKey;
    }
    public String getKaleyraSid() {
        return kaleyraSid;

    }
    public void setKaleyraSid(String kaleyraSid) {
        this.kaleyraSid=kaleyraSid;
    }
    public String getKaleyraSenderId() {
        return kaleyraSenderId;

    }
    public void setkaleyraSenderId(String kaleyraSenderId) {
        this.kaleyraSenderId=kaleyraSenderId;
    }
    public String getKayeraPaymentPendingTemplateId() {
        return kayeraPaymentPendingTemplateId;

    }
    public void setKayeraPaymentPendingTemplateId(String kayeraPaymentPendingTemplateId) {
        this.kayeraPaymentPendingTemplateId=kayeraPaymentPendingTemplateId;
    }
    public String getKayeraPaymentFailedTemplateId() {
        return kayeraPaymentFailedTemplateId;

    }
    public void setKayeraPaymentFailedTemplateId(String kayeraPaymentFailedTemplateId) {
        this.kayeraPaymentFailedTemplateId=kayeraPaymentFailedTemplateId;
    }

    public String getUserUrl() {
        return userUrl;

    }
    public void setUserUrl(String userUrl) {
        this.userUrl=userUrl;
    }
}
