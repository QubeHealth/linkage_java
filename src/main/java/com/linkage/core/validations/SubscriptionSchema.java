package com.linkage.core.validations;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionSchema {
    
    @JsonProperty("email")
    @NotEmpty(message = "Email is required")
    private ArrayList<String> email;
}
