package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

public class VendorSchema {
    

    @Data
    public static class SendAadharOtp {

        @JsonProperty("aadhar_number")
        @NotEmpty(message = "aadhar number is required")
        private String aadharNumber;

    }

    @Data
    public static class VerifyAadharOtp {

        @JsonProperty("aadhar_number")
        @NotEmpty(message = "aadhar number is required")
        private String aadharNumber;

        @JsonProperty("otp")
        @NotEmpty(message = "otp is required")
        private String otp;

        @JsonProperty("access_key")
        @NotEmpty(message = "access key is required")
        private String accessKey;
        
    }

}
