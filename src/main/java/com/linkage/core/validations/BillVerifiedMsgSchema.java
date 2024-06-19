package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class BillVerifiedMsgSchema {

    @NotNull(message = "Please enter a valid cashback amount")
    @Min(value = 1, message = "Cashback amount must be greater than 0")
    @JsonProperty("cashback_amount")
    private Long cashbackAmt;

    @NotBlank(message = "Please enter a valid mobile number")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be a valid 10-digit number and should not contain any alphabets.")
    private String mobile;

    @NotBlank(message = "Please enter a valid first name")
    @JsonProperty("first_name")
    private String firstname;

    @NotBlank(message = "Please enter status of bill approval(APPROVED or PARTIALLY_APPROVED)")
    @JsonProperty("approval_status")
    private String status;

    public String getBillStatus() {
        return status;
    }

    public void setBillStatus(String status) {
        this.status = status;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String company) {
        this.firstname = company;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setCashbackAmt(Long cashbackAmt) {
        this.cashbackAmt = cashbackAmt;
    }

    public Long getCashbackAmt() {
        return cashbackAmt;
    }

    // Custom validation to ensure status is either "APPROVED" or
    // "PARTIALLY_APPROVED"
    @AssertTrue(message = "Status must be either 'APPROVED' or 'PARTIALLY_APPROVED'")
    private boolean isValidStatus() {
        return "APPROVED".equals(status) || "PARTIALLY_APPROVED".equals(status);
    }
}
