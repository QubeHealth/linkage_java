package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class RefereeInviteMsgSchema {

    @NotNull(message = "Please enter a valid cashback amount")
    @JsonProperty("cashback_amount")
    private Float cashbackAmt;

    @NotBlank(message = "Please enter a valid mobile number")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be a valid 10-digit number and should not contain any alphabets.")
    private String mobile;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setCashbackAmt(Float cashbackAmt) {
        this.cashbackAmt = cashbackAmt;
    }

    public Float getCashbackAmt() {
        return cashbackAmt;
    }
}
