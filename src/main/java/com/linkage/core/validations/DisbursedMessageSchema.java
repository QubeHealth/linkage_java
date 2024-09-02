package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DisbursedMessageSchema {
   
    @NotBlank(message = "Please enter a valid mobile number")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be a valid 10-digit number and should not contain any alphabets.")
    @JsonProperty("mobile")
    private String mobile;

    @NotBlank(message = "Please enter valid transaction id ")
    @JsonProperty("transaction_id")
    private String transactionId;

    @NotBlank(message = "Please enter valid disbursed_amount")
    @JsonProperty("disbursed_amount")
    private Integer disbursedAmount;

    @NotBlank(message = "Please enter valid hsp_name ")
    @JsonProperty("hsp_name")
    private String hspName;

    
}
