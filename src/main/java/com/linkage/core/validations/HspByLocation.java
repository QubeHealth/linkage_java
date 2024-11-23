package com.linkage.core.validations;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class HspByLocation {
  
   @NotNull(message = "Latitude is required")
   @JsonProperty("latitude")
   private Double latitude;

   @NotNull(message = "Longitude code is required")
   @JsonProperty("longitude")
   private Double longitude;

   @JsonProperty("radius")
   private Integer radius;
}


