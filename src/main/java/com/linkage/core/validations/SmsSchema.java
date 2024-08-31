package com.linkage.core.validations;

import lombok.Data;
public class SmsSchema {
    private SmsSchema(){

    }
    @Data
    public static class CommumnicationRefund {
        private String status;
        private Long userID;
        private String transactionId;
        private String type;
        private String mobile;

    }
}
