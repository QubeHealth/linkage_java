package com.linkage;

import io.dropwizard.core.Configuration;
import jakarta.validation.constraints.NotEmpty;

public class LinkageConfiguration extends Configuration {
    
    private String jwtTokenSignature;
    
    private String authorizationKey;
    
    private String xApiKey;
    
    private String firebaseWebApiKey;
    
    private String watiUrl;
    
    private String watiToken;
    
    private String befiscAuthKey;

    private String loansUrl;

    @NotEmpty
    private String masterUrl;

    private String userJavaUrl;

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
}
