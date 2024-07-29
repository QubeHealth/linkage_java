package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DigitapCreditSchema {

    @NotBlank(message = "Client reference number is mandatory")
    @JsonProperty("client_ref_num")
    private String clientRefNum;

    @NotBlank(message = "Mobile number is mandatory")
    @Pattern(regexp = "^(?:\\+91\\s?|0)?[6-9]\\d{9}$", message = "Invalid mobile number")
    @JsonProperty("mobile_no")
    private String mobileNo;

    @JsonProperty("name_lookup")
    private Integer nameLookup;

    @NotBlank(message = "first_name is mandatory")
    @JsonProperty("first_name")
    private String firstName;

    @NotBlank(message = "last_name is mandatory")
    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("date_of_birth")
    private String dateOfBirth;

    @JsonProperty("email")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Invalid email address")
    private String email;

    @JsonProperty("pan")
    @Pattern(regexp = "^[A-Za-z]{5}\\d{4}[A-Za-z]{1}$", message = "Invalid PAN number")
    private String pan;

    @JsonProperty("consent_message")
    private String consentMessage;

    @JsonProperty("consent_acceptance")
    private String consentAcceptance;

    @JsonProperty("device_type")
    private String deviceType;

    @NotBlank(message = "OTP is mandatory")
    @JsonProperty("otp")
    private String otp;

    @JsonProperty("timestamp")
    private String timestamp;

    @NotBlank(message = "Device IP is mandatory")
    @JsonProperty("device_ip")
    private String deviceIp;

    @NotBlank(message = "Device ID is mandatory")
    @JsonProperty("device_id")
    private String deviceId;

}