package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class BillRejectedSchema {


    @NotBlank(message = "Please enter a valid mobile number")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be a valid 10-digit number and should not contain any alphabets.")
    private String mobile;

    @NotBlank(message = "Please enter a valid first name")
    @JsonProperty("first_name")
    private String firstname; 

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

   
}
