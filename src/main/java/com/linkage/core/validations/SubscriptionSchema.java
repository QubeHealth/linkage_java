package com.linkage.core.validations;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionSchema {

    @JsonProperty("hr_name")
    @NotEmpty(message = "HR name is required")
    private String hrName;

    @JsonProperty("employee_name")
    @NotEmpty(message = "Employee name is required")
    private String employeeName;

    @JsonProperty("hr_email")
    @NotEmpty(message = "Email is required")
    private String hrEmail;
}
