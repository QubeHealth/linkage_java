package com.linkage.core.validations;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class BbpsSchema {
    
    private BbpsSchema(){

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillerInfoRequest{

        @NotBlank(message = "Please enter a valid biller Id")
        @JsonProperty("biller_id")
        private String billerId;

    }

    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class BillFetchRequest{
        @NotBlank(message = "Please enter a valid biller Id")
        @JsonProperty("biller_id")
        private String billerId;
    }


    public class BillPaymentRequest {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class TransactionStatusReq {

        @NotBlank(message = "Please enter a valid biller Id")
        @JsonProperty("req_id")
        private String reqId;
    }


    public class ComplaintRegistrationReq {
    }


    public class ComplaintTrackingReq {
    }


    public class BillValidationRequest {
    }

}
