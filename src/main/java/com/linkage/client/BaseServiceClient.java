package com.linkage.client;




import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiRequest;
import com.linkage.api.ApiResponse;
import com.linkage.utility.ThirdPartyAPICall;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

public abstract class BaseServiceClient {

    protected LinkageConfiguration configuration;

    protected BaseServiceClient(LinkageConfiguration configuration) {
        this.configuration = configuration;
    }

    protected ApiResponse<Object> callThirdPartyApi(String url, String method, Object requestBody,
            MultivaluedMap<String, Object> additionalHeaders) {

        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

        headers.putSingle("Authorization", configuration.getAuthorizationKey());
        headers.putSingle("X-API-KEY", configuration.getxApiKey());

        if (additionalHeaders != null) {
            headers.putAll(additionalHeaders); // Use putAll to add all headers at once
        }

        ApiRequest apiRequest = new ApiRequest(url, method, requestBody, headers);
        return ThirdPartyAPICall.thirdPartyAPICall(apiRequest);
    }
}

