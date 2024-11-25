package com.linkage.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.api.HspDetails;
import com.linkage.core.constants.Constants;
import com.linkage.core.validations.HspByLocation;

public class GoogleMapsService extends BaseServiceClient {

    public GoogleMapsService(LinkageConfiguration configuration) {
        super(configuration);
    }

    public HashMap<String, Object> gmbApiData(Double latitude, Double longitude, Integer radius) throws Exception {
        URL nearbySearchUrl = new URL(Constants.NEARBY_SEARCH_URL);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("includedTypes", Constants.INCLUDED_TYPES);
        requestBody.put("maxResultCount", 20);
        requestBody.put("locationRestriction", Map.of("circle", Map.of(
            "center", Map.of("latitude", latitude, "longitude", longitude),
            "radius", (radius == null ? 100 : radius)
        )));
        requestBody.put("rankPreference", "DISTANCE");

        HttpURLConnection connection = (HttpURLConnection) nearbySearchUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("X-Goog-Api-Key", configuration.getGoogleApiKey());
        connection.setRequestProperty("X-Goog-FieldMask", Constants.FIELDS_FOR_GMB);
        connection.setDoOutput(true);

        ObjectMapper objectMapper = new ObjectMapper();
        try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
            String jsonRequestBody = objectMapper.writeValueAsString(requestBody);
            outputStream.write(jsonRequestBody.getBytes(StandardCharsets.UTF_8));
        }

        Integer responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream()) {
                String response = new BufferedReader(new InputStreamReader(inputStream))
                        .lines()
                        .collect(Collectors.joining("\n"));
                return objectMapper.readValue(response, new TypeReference<HashMap<String, Object>>() {});
            }
        } else {
            try (InputStream errorStream = connection.getErrorStream()) {
                String errorResponse = new BufferedReader(new InputStreamReader(errorStream))
                        .lines()
                        .collect(Collectors.joining("\n"));
                throw new Exception("Failed with HTTP error code: " + responseCode + ", Response: " + errorResponse);
            }
        }
    }
}