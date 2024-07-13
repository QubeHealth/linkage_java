package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CashbackTypeMessageSchema {
    @NotBlank(message = "Please enter a valid mobile number")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be a valid 10-digit number and should not contain any alphabets.")
    private String mobile;

    @NotBlank(message = "Please enter a valid first name")
    @JsonProperty("first_name")
    private String first_name;

    @NotNull(message = "Please enter a valid cashback amount")
    @Min(value = 1, message = "Cashback amount must be greater than 0")
    @JsonProperty("cashback_amount")
    private Integer cashback_amount;

    @NotBlank(message = "Please enter a valid store name")
    @JsonProperty("online_store")
    private String online_store;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getFirstName() {
        return first_name;
    }

    public void setFirstName(String company) {
        this.first_name = company;
    }
    public void setCashbackAmt(Integer cashback_amount) {
        this.cashback_amount = cashback_amount;
    }

    public Integer getCashbackAmt() {
        return cashback_amount;
    }

    public void setOnlineStore(String onlineStore) {
        this.online_store = onlineStore;
    }

    public String getOnlineStore() {
        return online_store;
    }


    
    

}
