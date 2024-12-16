package com.linkage.utility;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
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

 

    @SuppressWarnings("unchecked")
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
            if (request.getHeaders() != null && request.getHeaders().get("Content-Type") != null
                    && request.getHeaders().get("Content-Type").get(0).equals(MediaType.APPLICATION_FORM_URLENCODED)) {
                response = builder.post(Entity.entity(request.getBody(), MediaType.APPLICATION_FORM_URLENCODED));
            } else {
                response = builder.post(Entity.json(request.getBody()));
            }
        }

        AdvancedLogger.logInfo("\n\nThird party api request => {}", Helper.toJsonString(request));
        // Use GenericType to specify the type parameter for readEntity method
        String contentType = response.getHeaderString("Content-Type");
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseBody = null;
        if (contentType != null && contentType.contains("application/json")) {
            // If the response is JSON, read it into a Map
            responseBody = response.readEntity(new GenericType<Map<String, Object>>() {
            });
        } else if (contentType != null && contentType.contains("text/html")) {
            // If the response is HTML, read it as a String
            String responseBodyStr = response.readEntity(String.class);
            AdvancedLogger.logInfo("Received HTML response instead of JSON: {}", responseBodyStr);
            // You can decide to return this error or handle it differently
            try {
                responseBody = objectMapper.readValue(responseBodyStr, Map.class); // Parse the string to a Map
            } catch (Exception e) {
             AdvancedLogger.logInfo("Failed to parse the response body to JSON", e.toString());
            }
        } else if (contentType != null && contentType.contains("text/plain")) {
            // If the response is HTML, read it as a String
            String responseBodyStr = response.readEntity(String.class);
             AdvancedLogger.logInfo("Received TEXT/PLAIN response instead of JSON: {}", responseBodyStr);
            // You can decide to return this error or handle it differently
            try {
                responseBody = objectMapper.readValue(responseBodyStr, Map.class); // Parse the string to a Map
            } catch (Exception e) {
                AdvancedLogger.logInfo("Failed to parse the response body to JSON", e.toString());
            }
        }

        else {
            responseBody = response.readEntity(new GenericType<Map<String, Object>>() {
            });
        }
   
        AdvancedLogger.logInfo("\n\nThird party api response => {}", responseBody.toString());
        boolean status = false;
        if (Response.Status.OK.getStatusCode() <= response.getStatus() && response.getStatus() < 300) {
            status = true;
        }
        String message = status ? "success" : "failed";

        client.close();
        AdvancedLogger.logInfo(" EXTERNAL_PARTY_LOG  \t\t" + "URL:\n"+ request.getUrl() +"REQUEST \n->"+request.getBody(),"RESPONSE -->\n "+ responseBody.toString());
        return new ApiResponse<>(status, message, responseBody);
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
