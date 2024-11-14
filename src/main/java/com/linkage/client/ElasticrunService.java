package com.linkage.client;

import java.util.HashMap;
import java.util.Map;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;

import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;

public class ElasticrunService extends BaseServiceClient {

    public ElasticrunService(LinkageConfiguration configuration) {
        super(configuration);
    }

    // Method to retrieve authentication token
    public ApiResponse<Object> getToken() {
        String url = "https://auth.peoplestrong.com/auth/realms/1194/protocol/openid-connect/token";

        MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        headers.add("Authorization",
                "Basic Q2xpZW50X0ludGVncmF0aW9uXzExOTRfSFJJUzozMDJlZjgwZi0wYjczLTQxMWItOTk2OS1mYzM1MTcwOWE5NGY=");

        Form form = new Form();
        form.param("grant_type", "client_credentials");

        return this.networkCallExternalService(url, "POST", form, headers);
    }

    // Method to retrieve employee data
    public ApiResponse<Object> getEmployeeData(String token) {
        String url = "https://api.peoplestrong.com/api/integration/Outbound/ElasticRun_HRIS_Qubehealth";

        MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();

        headers.add("Authorization", "Bearer " + token);

        headers.add("Content-Type", MediaType.APPLICATION_JSON);

        headers.add("apikey", "Syg0BxAuYYXAGxNiYEdH51mkU3d6kEa8");

        Map<String, Object> reqBody = new HashMap<>();
        reqBody.put("integrationMasterName", "Qubehealth");

        return this.networkCallExternalService(url, "POST", reqBody, headers);
    }
}
