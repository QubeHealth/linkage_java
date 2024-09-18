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
public class AhcAppointmentReportSchema {

    @NotBlank(message = "Please enter a valid mobile number")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be a valid 10-digit number and should not contain any alphabets.")
    @JsonProperty("mobile")
    private String mobile;

    @NotBlank(message = "Please enter valid first name ")
    @JsonProperty("first_name")
    private String firstName;

    @NotBlank(message = "Please enter valid appointment_date")
    @JsonProperty("appointment_date")
    private String appointmentDate;

    @NotBlank(message = "Please enter valid report_path")
    @JsonProperty("report_path")
    private String reportPath;

}