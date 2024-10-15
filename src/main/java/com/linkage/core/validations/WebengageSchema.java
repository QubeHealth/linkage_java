package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class WebengageSchema {

    private WebengageSchema() {
    }

    @Data
    public static class UserSchema {
        @NotEmpty(message = "event name is required")
        @JsonProperty("event_name")
        private String eventName;

        @NotNull(message = "event data cannot be null")
        @JsonProperty("event_data")
        private Object eventData;
    }

    @Data
    public static class EventSchema {
        @NotEmpty(message = "event name is required")
        private String eventName;

        @NotEmpty(message = "userId is required")
        private String userId;

        @NotEmpty(message = "eventTime is required")
        private String eventTime;

        @NotNull(message = "event data cannot be null")
        private Object eventData;
    }

    @Data
    public static class UploadEmployeeSchema {
        @NotEmpty(message = "event name is required")
        @JsonProperty("event_name")
        private String eventName;

        @NotNull(message = "event data cannot be null")
        @JsonProperty("event_data")
        private Object eventData;
    }
}
