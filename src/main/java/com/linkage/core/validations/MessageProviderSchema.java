package com.linkage.core.validations;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

public class MessageProviderSchema {

    private MessageProviderSchema() {
    }

    @Data
    public static class SendMessageSchema {
        @JsonProperty("mobile")
        private String mobile;

        @JsonProperty("element_name")
        private String elementName;

        @JsonProperty("params")
        private List<String> params;
    }
}