package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AddFamilyMemberSchema {
            
    @NotBlank(message = "Please enter a valid mobile number")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be a valid 10-digit number and should not contain any alphabets.")
    @JsonProperty("mobile")
    private String mobile;

               
    @NotNull(message = "Please enter a valid user id")
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("primary_fname")
    private String primaryFname;

    @JsonProperty("secondary_fname")
    private String secondaryFname;

}
