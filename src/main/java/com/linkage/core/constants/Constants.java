package com.linkage.core.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {
    public static final Map<String, String> SUBSCRIPTION_EXPIRED_EMAIL;
    
    public static final String NEARBY_SEARCH_URL = "https://places.googleapis.com/v1/places:searchNearby";

    static {
        Map<String, String> emailMap = new HashMap<>();
        emailMap.put("SUBJECT", "Urgent: Your Qube App Subscription is About to Expire – Don’t Miss Out on Continued Savings!");
        SUBSCRIPTION_EXPIRED_EMAIL = Collections.unmodifiableMap(emailMap);
    }

    public static final List<String> INCLUDED_TYPES = List.of(
        "chiropractor", "dental_clinic", "dentist", "doctor", "drugstore",
        "hospital", "massage", "medical_lab", "pharmacy", "physiotherapist",
        "sauna", "skin_care_clinic", "spa", "tanning_studio",
        "wellness_center", "yoga_studio"
    );

    public static final String FIELDS_FOR_GMB = "places.accessibilityOptions,places.addressComponents,places.adrFormatAddress,places.attributions,places.businessStatus,places.containingPlaces,places.displayName,places.formattedAddress,places.googleMapsLinks,places.googleMapsUri,places.iconBackgroundColor,places.iconMaskBaseUri,places.id,places.location,places.name,places.photos,places.plusCode,places.primaryType,places.primaryTypeDisplayName,places.pureServiceAreaBusiness,places.shortFormattedAddress,places.subDestinations,places.types,places.utcOffsetMinutes,places.viewport,places.currentOpeningHours,places.currentSecondaryOpeningHours,places.internationalPhoneNumber,places.nationalPhoneNumber,places.priceLevel,places.priceRange,places.rating,places.regularOpeningHours,places.regularSecondaryOpeningHours,places.userRatingCount,places.websiteUri";

}