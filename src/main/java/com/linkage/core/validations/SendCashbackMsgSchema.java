package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class SendCashbackMsgSchema {

    @NotNull(message = "Please enter a valid cashback amount to be given to the referer")
    @Min(value = 1, message = "Cashback amount must be greater than 0")
    @JsonProperty("cashback_amount_referer")
    private Integer cashbackAmtReferer;

    @NotNull(message = "Please enter a valid cashback amount to be given to the referee")
    @Min(value = 1, message = "Cashback amount must be greater than 0")
    @JsonProperty("cashback_amount_referee")
    private Integer cashbackAmtReferee;

    @NotBlank(message = "Please enter a valid mobile number of the referer")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be a valid 10-digit number and should not contain any alphabets.")
    @JsonProperty("mobile_referer")
    private String mobileReferer;

    @NotBlank(message = "Please enter a valid mobile number of the referee")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be a valid 10-digit number and should not contain any alphabets.")
    @JsonProperty("mobile_referee")
    private String mobileReferee;

    @NotBlank(message = "Please enter a valid compay name")
    @JsonProperty("company_name")
    private String company;

    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        this.company = company;
    }
    public Integer getCashbackAmtReferee() {
        return cashbackAmtReferee;
    }
    public void setCashbackAmtReferee(Integer cashbackAmtReferee) {
        this.cashbackAmtReferee = cashbackAmtReferee;
    }
    public Integer getCashbackAmtReferer() {
        return cashbackAmtReferer;
    }
    public void setCashbackAmtReferer(Integer cashbackAmtReferer) {
        this.cashbackAmtReferer = cashbackAmtReferer;
    }
    public void setMobileReferee(String mobileReferee) {
        this.mobileReferee = mobileReferee;
    }
    public String getMobileReferee() {
        return mobileReferee;
    }
    public void setMobileReferer(String mobileReferer) {
        this.mobileReferer = mobileReferer;
    }
    public String getMobileReferer() {
        return mobileReferer;
    }


}
