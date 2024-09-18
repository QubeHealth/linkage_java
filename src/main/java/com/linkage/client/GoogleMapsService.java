package com.linkage.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.api.HspDetails;
import com.linkage.core.validations.HspByLocation;

public class GoogleMapsService extends BaseServiceClient {

    public GoogleMapsService(LinkageConfiguration configuration) {
        super(configuration);
    }

    public ApiResponse<List<HspDetails>> searchHsp(HspByLocation input) {
        String latitude = input.getLatitude();
        String longitude = input.getLongitude();
    
        // Google Places API base URL
        String googleApiUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    
        // Parameters
        String location = "location=" + latitude + "," + longitude;
        String radius = "&radius=10"; // Adjusted to 5000 meters for a broader search
        String types = "&type=dental_clinic,dentist,doctor,drugstore,hospital,medical_lab,pharmacy,physiotherapist,spa";
        String apiKey = "&key=" + configuration.getGoogleApiKey();
    
        // Construct the full URL for Nearby Search
        String appendedUrl = googleApiUrl + location + radius + types + apiKey;
    
        // Make the network call to Nearby Search API
        ApiResponse nearbySearchResponse = networkCallExternalService(appendedUrl, "get", null, null);
    
        // Parse the response from the Nearby Search API
        List<HspDetails> placeDetailsList = new ArrayList<>();
        if (nearbySearchResponse.getStatus()) {
            Map<String, Object> responseData = (Map<String, Object>) nearbySearchResponse.getData();
            List<Map<String, Object>> places = (List<Map<String, Object>>) responseData.get("results");
    
            Set<String> allowedTypes = new HashSet<>(Arrays.asList(
                "dental_clinic", "health", "dentist", "doctor", "drugstore", "hospital", "medical_lab", "pharmacy", "physiotherapist", "spa"
            ));
    
            for (Map<String, Object> place : places) {
                List<String> placeTypes = (List<String>) place.get("types");
                boolean matchFound = false;
                for (String type : placeTypes) {
                    if (allowedTypes.contains(type)) {
                        matchFound = true;
                        break;
                    }
                }
    
                if (matchFound) {
                    String placeId = (String) place.get("place_id");
                    if (placeId != null) {
                        // Construct the Place Details API URL
                        String placeDetailsUrl = "https://maps.googleapis.com/maps/api/place/details/json?";
                        String placeDetailsParams = "place_id=" + placeId +
                                "&fields=name,formatted_phone_number,formatted_address,international_phone_number,geometry,address_component" +
                                "&key=" + configuration.getGoogleApiKey();
                        String placeDetailsAppendedUrl = placeDetailsUrl + placeDetailsParams;
    
                        // Make the network call to the Place Details API
                        ApiResponse placeDetailsResponse = networkCallExternalService(placeDetailsAppendedUrl, "get", null, null);
    
                        if (placeDetailsResponse.getStatus()) {
                            Map<String, Object> placeDetailsData = (Map<String, Object>) placeDetailsResponse.getData();
                            Map<String, Object> result = (Map<String, Object>) placeDetailsData.get("result");
    
                            String name = (String) result.get("name");
                            String internationalPhone = (String) result.get("international_phone_number");
                            String phoneNumber = (String) result.get("formatted_phone_number");
                            String address = (String) result.get("formatted_address");
    
                            Map<String, Object> geometry = (Map<String, Object>) result.get("geometry");
                            Map<String, Object> loc = (Map<String, Object>) geometry.get("location");
                            Double lat = (Double) loc.get("lat");
                            Double lon = (Double) loc.get("lng");
    
                            String pincode = "";
                            String state = "";
                            String country = "";
                            List<Map<String, Object>> addressComponents = (List<Map<String, Object>>) result.get("address_components");
    
                            for (Map<String, Object> component : addressComponents) {
                                List<String> typesList = (List<String>) component.get("types");
                                if (typesList.contains("postal_code")) {
                                    pincode = (String) component.get("long_name");
                                } else if (typesList.contains("administrative_area_level_1")) {
                                    state = (String) component.get("long_name");
                                } else if (typesList.contains("country")) {
                                    country = (String) component.get("long_name");
                                }
                            }
    
                            if (internationalPhone != null) {
                                phoneNumber = internationalPhone + (phoneNumber != null ? ", " + phoneNumber : "");
                            }
    
                            // Create HspDetails object with types
                            HspDetails details = new HspDetails(name, phoneNumber, address, lat, lon, pincode, state, country, placeTypes);
                            placeDetailsList.add(details);
                        }
                    }
                }
            }
        }
    
        return new ApiResponse<>(true, "Data fetched successfully", placeDetailsList);
    }
}