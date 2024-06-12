package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class KaleyraInviteMessageSchema {
    @NotNull(message = "Please enter a valid cashback amount")
    @Min(value = 1, message = "Cashback amount must be greater than 0")
    @JsonProperty("discount_amount")
    private Integer discountAmount;

    @NotBlank(message = "Please enter a valid first name")
    @JsonProperty("first_name")
    private String firstName;

    @NotBlank(message = "Please enter a valid mobile number")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be a valid 10-digit number and should not contain any alphabets.")
    private String mobile;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getDiscountAmount() {
        return discountAmount;
    }
    public void setDiscountAmount(Integer discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

   
}
