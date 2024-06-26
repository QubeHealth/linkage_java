package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GetVpaDetailsSchema {

    @NotBlank(message = "Please enter a valid vpa")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9._-]+$", message = "Enter a valid vpa")
    @JsonProperty("vpa")
    private String vpa;

}
