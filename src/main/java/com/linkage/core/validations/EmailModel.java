package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

public class EmailModel {

    @Data
    public static class sendAdBannerEmailReq {

        @JsonProperty("hr_name")
        private String hrName;

        @JsonProperty("product_name")
        private String productName;

        @JsonProperty("cluster_name")
        private String clusterName;

        @JsonProperty("trigger_date_time")
        private String triggerDateTime;
    }
}
