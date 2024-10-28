package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public class OtpSmsSchema {

    @JsonProperty("phone_number")
    @NotEmpty(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotEmpty(message = "OTP is required")
    private String otp;

    @JsonProperty("expiry_time")
    @NotEmpty(message = "Expiry time is required")
    private String expiryTime;

    // Getters and Setters
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(String expiryTime) {
        this.expiryTime = expiryTime;
    }
}