package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

public class EmailModel {

    @Data
    public static class sendAdBannerEmailReq {

        @JsonProperty("hr_name")
        @NotEmpty(message = "HR Name is required")
        private String hrName;

        @JsonProperty("product_name")
        @NotEmpty(message = "Product Name is required")
        private String productName;

        @JsonProperty("cluster_name")
        @NotEmpty(message = "Cluster Name is required")
        private String clusterName;

        @JsonProperty("trigger_date_time")
        @NotEmpty(message = "Trigger Date Time is required")
        private String triggerDateTime;
    }
}
