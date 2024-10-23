package com.linkage.client;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;

import jakarta.ws.rs.core.MultivaluedHashMap;

public class WebengageService extends BaseServiceClient {

    public WebengageService(LinkageConfiguration configuration) {
        super(configuration);
    }

    public ApiResponse<Object> createBulkUser(Object body) {
        String url = configuration.getWebEngageApiUrl() + "v1/accounts/" + configuration.getWebEngageLiscenseCode()
                + "/bulk-users";

        MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle("content-type", "application/json");

        headers.putSingle("Authorization", "Bearer " + configuration.getWebEngageApiKey());

        return this.networkCallExternalService(url, "post", body, headers);
    }


    public ApiResponse<Object> pushEvent(Object body) {
        String url = configuration.getWebEngageApiUrl() + "v1/accounts/" + configuration.getWebEngageLiscenseCode()
                + "/events";

        MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle("content-type", "application/json");
        headers.putSingle("Authorization", "Bearer " + configuration.getWebEngageApiKey());

        return this.networkCallExternalService(url, "post", body, headers);
    }

}
