package com.linkage.core.validations;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class HspByLocation {
  
   @NotBlank(message = "Latitude is required")
   @JsonProperty("latitude")
   private String latitude;

   @NotBlank(message = "Longitude code is required")
   @JsonProperty("longitude")
   private String longitude;

}


