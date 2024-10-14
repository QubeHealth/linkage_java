package com.linkage.utility;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(ThirdPartyAPICall.class);

    public static ApiResponse<Object> thirdPartyAPICall(ApiRequest request) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(request.getUrl());

        // Handle GET requests with query parameters
        if (request.getMethod().equalsIgnoreCase("GET")) {
            target = appendQueryParams(target, (Map<String, Object>) request.getBody());
        }

        Invocation.Builder builder = target
                .request(MediaType.APPLICATION_XML) // Expect XML responses
                .headers(request.getHeaders());

        Response response;
        if (request.getMethod().equalsIgnoreCase("GET")) {
            response = builder.get();
        } else {
            response = builder.post(Entity.json(request.getBody())); // Fallback to JSON
            if (request.getHeaders().get("Content-Type") != null) {
                if (request.getHeaders().get("Content-Type").get(0).equals(MediaType.APPLICATION_FORM_URLENCODED)) {
                    response = builder.post(Entity.entity(request.getBody(), MediaType.APPLICATION_FORM_URLENCODED));
                } else if (request.getHeaders().get("Content-Type").get(0).equals(MediaType.TEXT_PLAIN)) {
                    response = builder.post(Entity.entity(request.getBody(), MediaType.TEXT_PLAIN)); // Send XML
                }
            }
        }

        logger.info("\n\nThird party api request => {}", Helper.toJsonString(request));

        logger.info("\n\nThird party api response => {}", response);

        Object responseBody;
        String contentType = response.getMediaType().toString(); // Get the content type as a string

        if (contentType.equals(MediaType.APPLICATION_XML) || contentType.equals(MediaType.TEXT_XML)) {
            // Read the response as a String for XML
            responseBody = response.readEntity(String.class);
        } else if (contentType.startsWith("text/html")) {
            // Read the response as a String for HTML
            responseBody = response.readEntity(String.class);
        } else {
            // Read the response as a Map<String, Object> for JSON or other types
            responseBody = response.readEntity(new GenericType<Map<String, Object>>() {});
        }

        System.out.println("Response status => " + response.toString());
        logger.info("\n\nThird party api response => {}", responseBody);
        
        boolean status = Response.Status.OK.getStatusCode() <= response.getStatus() && response.getStatus() < 300;
        String message = status ? "success" : "failed";

        client.close();

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
