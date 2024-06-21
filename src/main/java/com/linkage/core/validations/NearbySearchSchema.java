package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NearbySearchSchema {
    
    @NotBlank(message = "Latitude is required")
    @JsonProperty("latitude")
    private String latitude;

    @NotBlank(message = "Longitude code is required")
    @JsonProperty("longitude")
    private String longitude;

    @NotBlank(message = "radius is required")
    @JsonProperty("radius")
    private String radius;

    @JsonProperty("keyword")
    private String keyword;

}
