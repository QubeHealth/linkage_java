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
    private String firebaseWebAPIKey;


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
    public String  getFireBaseWebApiKey() {
        return fireBaseWebApiKey;
    }
    public void setFireBaseWebApiKey(String fireBaseWebApiKey) {
        this. fireBaseWebApiKey= fireBaseWebApiKey;
    }
   
}
