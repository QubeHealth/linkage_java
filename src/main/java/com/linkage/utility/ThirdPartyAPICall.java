package com.linkage.utility;

import java.util.Map;

import com.linkage.api.ApiRequest;
import com.linkage.api.ApiResponse;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public final class ThirdPartyAPICall {

    private ThirdPartyAPICall() {
    }

    public static ApiResponse<Object> thirdPartyAPICall(ApiRequest request) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(request.getUrl());

        if (request.getMethod().equalsIgnoreCase("GET")) {
            target = appendQueryParams(target, (Map<String, Object>) request.getBody());
        }

        Invocation.Builder builder = target
                .request(MediaType.APPLICATION_JSON) // Explicitly specify JSON media type
                .headers(request.getHeaders());

        Response response;
        if (request.getMethod().equalsIgnoreCase("GET")) {
            response = builder.get();
        } else {
            response = builder.post(Entity.json(request.getBody()));
        }

        System.out.println("\nThird party api request => " + Helper.toJsonString(request) + "\n");
        // Use GenericType to specify the type parameter for readEntity method
        Map<String, Object> responseBody = response.readEntity(new GenericType<Map<String, Object>>() {
        });

        System.out.println("\nThird party api response => " + Helper.toJsonString(responseBody) + "\n");

        boolean status = (boolean) responseBody.getOrDefault("status", false);
        String message = status ? "success" : "failed";

        if (responseBody.containsKey("errorMessage")) {
            message = responseBody.get("errorMessage").toString();
        } else if (responseBody.containsKey("errors")) {
            message = responseBody.get("errors").toString();
        } else if (responseBody.containsKey("message")) {
            message = responseBody.get("message") != null ? responseBody.get("message").toString() : message;
        }

        Object data = responseBody.containsKey("data") ? responseBody.get("data") : null;

        client.close();

        return new ApiResponse<>(status, message, data);
    }

    private static WebTarget appendQueryParams(WebTarget target, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return target;
        }
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            target = target.queryParam(entry.getKey(), entry.getValue());
        }
        return target;
    }

}
