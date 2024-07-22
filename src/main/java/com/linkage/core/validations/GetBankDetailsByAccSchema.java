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
public class GetBankDetailsByAccSchema {

    @NotBlank(message = "Please enter a valid Bank Account Number")
    @Pattern(regexp = "^\\d{6,}$", message = "Invalid Account Number")
    @JsonProperty("account_number")
    private String accountNumber;

    @NotBlank(message = "Please enter a valid IFSC Code")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC Code")
    @JsonProperty("ifsc_code")
    private String ifscCode;

}
