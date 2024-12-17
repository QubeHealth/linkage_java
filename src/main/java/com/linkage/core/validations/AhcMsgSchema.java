package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AhcMsgSchema {
    @NotBlank(message = "Please enter a valid mobile number")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be a valid 10-digit number and should not contain any alphabets.")
    private String mobile;

    @JsonProperty("template_id")
    @NotEmpty(message = "Template is required")
    private String templateId;

    @NotBlank(message = "Please enter valid first name ")
    @JsonProperty("first_name")
    private String firstName;

    private String address;

    @JsonProperty("collection_type")
    private String collectionType;

    private String lab;
    
    @JsonProperty("booking_date")
    private String bookingDate;

    @JsonProperty("booking_time")
    public String bookingTime;

}