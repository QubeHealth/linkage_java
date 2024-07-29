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
    public RabbitMqConfig getRabbitMqConfig() {
        return rabbitMqConfig;
    }

    @JsonProperty("rabbitmq")
    public void setRabbitMqConfig(RabbitMqConfig rabbitMqConfig) {
        this.rabbitMqConfig = rabbitMqConfig;
    }

    public static class RabbitMqConfig {
        @NotEmpty
        @JsonProperty("userName")
        private String userName;

        @NotEmpty
        @JsonProperty("password")
        private String password;

        @NotEmpty
        @JsonProperty("virtualHost")
        private String virtualHost;

        @NotEmpty
        @JsonProperty("hostName")
        private String hostName;

        @JsonProperty("portNumber")
        private int portNumber;

        @JsonProperty("env")
        private String env;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getVirtualHost() {
            return virtualHost;
        }

        public void setVirtualHost(String virtualHost) {
            this.virtualHost = virtualHost;
        }

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public int getPortNumber() {
            return portNumber;
        }

        public void setPortNumber(int portNumber) {
            this.portNumber = portNumber;
        }

        public String getEnv() {
            return env;
        }

        public void setEnv(String env) {
            this.env = env;
        }
    }

    public String getUserJavaUrl(){
        return userJavaUrl;
    }
    public void SetUserJavaUrl(String userJavaUrl) {
        this.userJavaUrl = userJavaUrl;
    }
}
