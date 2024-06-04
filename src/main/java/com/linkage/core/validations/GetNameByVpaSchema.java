package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


@Data
public class GetNameByVpaSchema {

    @NotBlank(message = "Please enter a valid vpa")
    @JsonProperty("vpa")
    private String vpa;

}
