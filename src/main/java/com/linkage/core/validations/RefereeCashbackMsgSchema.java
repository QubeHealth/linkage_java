package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class RefereeCashbackMsgSchema {

    @NotNull(message = "Please enter a valid cashback amount")
    @Min(value = 1, message = "Cashback amount must be greater than 0")
    @JsonProperty("cashback_amount")
    private Integer cashbackAmt;

    @NotBlank(message = "Please enter a valid mobile number")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be a valid 10-digit number and should not contain any alphabets.")
    private String mobile;

    @NotBlank(message = "Please enter a valid compay name")
    @JsonProperty("company_name")
    private String company;

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setCashbackAmt(Integer cashbackAmt) {
        this.cashbackAmt = cashbackAmt;
    }

    public Integer getCashbackAmt() {
        return cashbackAmt;
    }
}
