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
    private String befiscSendAadharOtpUrl;
    @NotEmpty
    private String befiscVerifyAadharOtpUrl;

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
    private String userJavaUrl;

    @NotEmpty
    private String emailSmtp;
    @NotEmpty
    private String emailPassword;
    @NotEmpty
    private String emailHost;

    @NotEmpty
    private String googleApiKey;

    @NotEmpty
    private String webEngageLiscenseCode;
    @NotEmpty
    private String webEngageApiKey;
    @NotEmpty
    private String webEngageApiUrl;

    @NotEmpty
    private String appSignature;

 

    @NotEmpty
    private String hrmsTokenApiUrl;

    @NotEmpty
    private String hrmsEmployeeApiUrl;

    @NotEmpty
    private String hrmsTokenAuthorization;

    @NotEmpty
    private String hrmsEmployeeApiKey;

    public String getHrmsEmployeeApiKey() {
        return hrmsEmployeeApiKey;
    }

    public String getHrmsTokenAuthorization() {
        return hrmsTokenAuthorization;
    }

    public String getHrmsEmployeeApiUrl() {
        return hrmsEmployeeApiUrl;
    }

    public String getHrmsTokenApiUrl() {
        return hrmsTokenApiUrl;
    }


    public String getxApiKey() {
        return xApiKey;
    
    }
    public void setAppSignature(String appSignature) {
        this.appSignature = appSignature;
    }

    public String appSignature() {
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

    public String getBefiscSendAadharOtpUrl() {
        return befiscSendAadharOtpUrl;
    }

    public void setBefiscSendAadharOtpUrl(String befiscSendAadharOtpUrl) {
        this.befiscSendAadharOtpUrl = befiscSendAadharOtpUrl;
    }

    public String getBefiscVerifyAadharOtpUrl() {
        return befiscVerifyAadharOtpUrl;
    }

    public void setBefiscVerifyAadharOtpUrl(String befiscVerifyAadharOtpUrl) {
        this.befiscVerifyAadharOtpUrl = befiscVerifyAadharOtpUrl;
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
        this.hereApiKey = hereApiKey;
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
    public String getGoogleApiKey() {
        return googleApiKey;
    }

    public void setGoogleApiKey(String googleApiKey) {
        this.googleApiKey = googleApiKey;
    }

    public String getKaleyraBaseUrl() {
        return kaleyraBaseUrl;

    }

    public void setKaleyraBaseUrl(String kaleyraBaseUrl) {
        this.kaleyraBaseUrl = kaleyraBaseUrl;
    }

    public String getKaleyraApiKey() {
        return kaleyraApiKey;

    }

    public void setKaleyraApiKey(String kaleyraApiKey) {
        this.kaleyraApiKey = kaleyraApiKey;
    }

    public String getKaleyraSid() {
        return kaleyraSid;

    }

    public void setKaleyraSid(String kaleyraSid) {
        this.kaleyraSid = kaleyraSid;
    }

    public String getKaleyraSenderId() {
        return kaleyraSenderId;

    }

    public void setkaleyraSenderId(String kaleyraSenderId) {
        this.kaleyraSenderId = kaleyraSenderId;
    }

    public String getKayeraPaymentPendingTemplateId() {
        return kayeraPaymentPendingTemplateId;

    }

    public void setKayeraPaymentPendingTemplateId(String kayeraPaymentPendingTemplateId) {
        this.kayeraPaymentPendingTemplateId = kayeraPaymentPendingTemplateId;
    }

    public String getKayeraPaymentFailedTemplateId() {
        return kayeraPaymentFailedTemplateId;

    }

    public void setKayeraPaymentFailedTemplateId(String kayeraPaymentFailedTemplateId) {
        this.kayeraPaymentFailedTemplateId = kayeraPaymentFailedTemplateId;
    }

    public String getUserJavaUrl() {
        return userJavaUrl;
    }

    public void setUserJavaUrl(String userJavaUrl) {
        this.userJavaUrl = userJavaUrl;
    }

    public String getWebEngageApiKey() {
        return webEngageApiKey;
    }

    public String getWebEngageApiUrl() {
        return webEngageApiUrl;
    }

    public String getWebEngageLiscenseCode() {
        return webEngageLiscenseCode;
    }

    public void setWebEngageApiKey(String webEngageApiKey) {
        this.webEngageApiKey = webEngageApiKey;
    }

    public void setWebEngageApiUrl(String webEngageApiUrl) {
        this.webEngageApiUrl = webEngageApiUrl;
    }

    public void setWebEngageLiscenseCode(String webEngageLiscenseCode) {
        this.webEngageLiscenseCode = webEngageLiscenseCode;
    }

}
