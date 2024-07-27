package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

public class ErupeeSchema {

    private ErupeeSchema() {
    }

    @Data
    public static class VoucherRequest {
        private String merchantId;
        private String merchantTranId;
        private String subMerchantId;
        private String beneficiaryID;
        private String mobileNumber;
        private String beneficiaryName;
        private String amount;
        private String expiry;
        private String purposeCode;
        private String mcc;
        @JsonProperty("VoucherRedemptionType")
        private String VoucherRedemptionType;
        @JsonProperty("PayerVA")
        private String PayerVA;
        private String type;
    }

    @Data
    public static class VoucherResponse {
        private String expiryDate;
        private Long merchantId;
        private String success;
        private Long response;
        private Float amount;
        private String umn;
        private String message;
        private String uuid;
        private String merchantTranId;
        private String status;
    }

    @Data
    public static class RedemptionCallback {
        private Long merchantId;
        private Long subMerchantId;
        private Long terminalId;
        private Long bankRRN;
        private String merchantTranId;
        private String payerName;
        private Long payerMobile;
        private String payerVA;
        private Float payerAmount;
        private String txnStatus;
        private String txnInitDate;
        private String txnCompletionDate;
        private String umn;
    }
}
