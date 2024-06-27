package com.linkage.client;

import java.util.HashMap;
import java.util.Map;

import com.linkage.LinkageConfiguration;
import com.linkage.core.validations.TextSearchSchema;

import jakarta.ws.rs.core.MultivaluedHashMap;

public class GoogleMapsService extends BaseServiceClient {
    public GoogleMapsService(LinkageConfiguration configuration) {
        super(configuration);
    }
//Does the existential search via the id
    public Object textSearchid(TextSearchSchema search) {

        Map<String, String> center = new HashMap<>();
        center.put("latitude", search.getLatitude());
        center.put("longitude", search.getLongitude());

        Map<String, Object> circle = new HashMap<>();
        circle.put("circle", center);
        circle.put("radius",search.getRadius());

        Map<String, Object> locationBias = new HashMap<>();
        locationBias.put("locationBias", circle);
        locationBias.put("textQuery",search.getKeyword());

        MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle("X-Goog-FieldMask", "places.id");
        headers.putSingle("X-Goog-Api-Key", configuration.getxGoogApiKey());

        return networkCallExternalService("https://places.googleapis.com/v1/places:searchText", "post", locationBias,
                headers);
    }
//Does the actual search 
    public Object textSearch(TextSearchSchema search) {

        Map<String, String> center = new HashMap<>();
        center.put("latitude", search.getLatitude());
        center.put("longitude", search.getLongitude());

        Map<String, Object> circle = new HashMap<>();
        circle.put("circle", center);
        circle.put("radius",search.getRadius());

        Map<String, Object> locationBias = new HashMap<>();
        locationBias.put("locationBias", circle);
        locationBias.put("textQuery",search.getKeyword());

        MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle("X-Goog-FieldMask", "places.primaryTypeDisplayName,places.displayName,places.addressComponents,places.nationalPhoneNumber,places.websiteUri,places.location,places.types");
        headers.putSingle("X-Goog-Api-Key", configuration.getxGoogApiKey());

        return networkCallExternalService("https://places.googleapis.com/v1/places:searchText", "post", locationBias,
                headers);
    }
}
