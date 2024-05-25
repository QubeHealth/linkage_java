package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public class GetReferalUrl {

    @NotBlank(message = "Please enter a valid referal code")
    @JsonProperty("program_id")
    private String referCode;

    public String getReferCode() {
        return referCode;
    }

    public void setReferCode(String referCode) {
        this.referCode = referCode;
    }
}
