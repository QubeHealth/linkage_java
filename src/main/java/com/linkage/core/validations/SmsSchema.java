package com.linkage.core.validations;

import lombok.Data;
public class SmsSchema {
    private SmsSchema(){

    }
    @Data
    public static class PaymentStatus {
        private String status;
        private Long userId;
        private String transactionId;
        private String type;
        private String mobile;

    }
}
