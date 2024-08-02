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
    private String loansUrl;
    @NotEmpty
    private String masterUrl;
    @NotEmpty
    private String userJavaUrl;
    @NotEmpty
    private String mailHost;
    @NotEmpty
    private String mailPort;
    @NotEmpty
    private String mailId;
    @NotEmpty
    private String passkey;
    @NotEmpty
    private String adjudicatorMail;
    @NotEmpty
    private String tpaMail;
    @NotEmpty
    private String mailWriterPort;

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

    public String getMasterurl() {
        return masterUrl;
    }
    public void setMasterUrl(String masterUrl) {
        this.masterUrl = masterUrl;
    }

    public String getLoansUrl() {
        return loansUrl;
    }
    public void setLoansurl(String loansUrl) {
        this.loansUrl = loansUrl;
    }

    public String getUserJavaUrl(){
        return userJavaUrl;
    }
    public void SetUserJavaUrl(String userJavaUrl) {
        this.userJavaUrl = userJavaUrl;
    }
    
    public String getMailHost(){
        return mailHost;
    }
    public void setMailHost(String mailHost){
        this.mailHost = mailHost;
    }

    public String getMailPort() {
        return mailPort;
    }
    public void setMailPort(String mailPort) {
        this.mailPort = mailPort;
    }

    public String getMailId() {
        return mailId;
    }
    public void setMailId(String mailId) {
        this.mailId = mailId;
    }

    public String getPasskey() {
        return passkey;
    }
    public void setPasskey(String passkey) {
        this.passkey = passkey;
    }

    public String getTpaMail() {
        return tpaMail;
    }
    public void setTpaMail(String tpaMail) {
        this.tpaMail = tpaMail;
    }

    public String getAdjudicatorMail() {
        return adjudicatorMail;
    }
    public void setAdjudicatorMail(String adjudicatorMail) {
        this.adjudicatorMail = adjudicatorMail;
    }

    public String getMailWriterPort() {
        return mailWriterPort;
    }
    public void setMailWriterPort(String mailWriterPort) {
        this.mailWriterPort = mailWriterPort;
    }
}
