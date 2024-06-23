package com.linkage.core.validations;

import lombok.Data;

public class ErupeeSchema {

    private ErupeeSchema() {
    }

    @Data
    public static class VoucherRequest {
        private Long merchantId;
        private String merchantTranId;
        private Long subMerchantId;
        private String beneficiaryID;
        private Long mobileNumber;
        private String beneficiaryName;
        private Float amount;
        private String expiry;
        private String purposeCode;
        private Integer mcc;
        private String voucherRedemptionType;
        private String payerVA;
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
