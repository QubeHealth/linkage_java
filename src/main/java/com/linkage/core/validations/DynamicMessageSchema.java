package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class DynamicMessageSchema {
    @NotBlank(message = "Please enter a valid mobile number")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be a valid 10-digit number and should not contain any alphabets.")
    private String mobile;

    @JsonProperty("template_id")
    @NotEmpty(message = "Template is required")
    private String templateId;

    @NotBlank(message = "Please enter valid first name ")
    @JsonProperty("first_name")
    private String firstName;

    public String getMobile() {
        return mobile;
    }

    public String getTemplateId(){
        return templateId;
    }

    public String getFirstname() {
        return firstName;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

}
