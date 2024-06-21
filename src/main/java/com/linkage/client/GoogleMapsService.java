package com.linkage.client;

import java.util.HashMap;
import java.util.Map;

import com.linkage.LinkageConfiguration;
import com.linkage.core.validations.NearbySearchSchema;

import jakarta.ws.rs.core.MultivaluedHashMap;

public class GoogleMapsService extends BaseServiceClient {
    public GoogleMapsService(LinkageConfiguration configuration) {
        super(configuration);
    }

    public Object nearbySearch(NearbySearchSchema search) {

        Map<String, String> center = new HashMap<>();
        center.put("latitude", search.getLatitude());
        center.put("longitude", search.getLongitude());

        Map<String, Object> location = new HashMap<>();
        location.put("circle", center);
        location.put("radius", search.getRadius());

        Map<String, Object> locationRestrictions = new HashMap<>();
        locationRestrictions.put("locationRestriction", location);

        String[] includedTypes = {"dental_clinic", "dentist", "doctor", "drugstore", "hospital", "medical_lab", "pharmacy", "physiotherapist"};
        locationRestrictions.put("includedTypes",includedTypes);

        MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle("X-Goog-FieldMask", "places.displayName");
        headers.putSingle("X-Goog-Api-Key", configuration.getxGoogApiKey());

        return networkCallExternalService("https://places.googleapis.com/v1/places:searchNearby", "post", locationRestrictions,
                headers);

    }
}
