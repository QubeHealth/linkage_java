package com.linkage.client;

import java.util.HashMap;
import java.util.Map;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.validations.GetBankDetailsByAccSchema;
import com.linkage.core.validations.GetNameByVpaSchema;
import com.linkage.core.validations.GetVpaByMobileSchema;
import com.linkage.utility.Helper;

import jakarta.ws.rs.core.MultivaluedHashMap;

public class BefiscService extends BaseServiceClient {

    public BefiscService(LinkageConfiguration configuration) {
        super(configuration);
    }

    public ApiResponse<Map<String, Object>> mobileUpiSupreme(GetVpaByMobileSchema body) {

        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();

        header.putSingle("authkey", "ODWPKDP73RC9937");

        ApiResponse<Object> response = this.networkCallExternalService("https://mobile-lookup-supreme.befisc.com",
                "post", body, header);

        logger.info("mobile upi supreme response {}", Helper.toJsonString(response));

        Map<String, Object> responseData = (Map<String, Object>) response.getData();

        Boolean status = Integer.parseInt(responseData.get("status").toString()) == 1;
        String message = responseData.get("message").toString();
        Map<String, Object> data = (Map<String, Object>) responseData.get("result");

        return new ApiResponse<>(status, message, data);

    }

    public ApiResponse<Map<String, Object>> multipleUpi(GetVpaByMobileSchema body) {
        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();
        header.putSingle("authkey", "ODWPKDP73RC9937");
        ApiResponse<Object> response = this.networkCallExternalService("https://mobile-lookup-for-multiple-upi.befisc.com",
        "post", body, header);
        Map<String, Object> responseData = (Map<String, Object>) response.getData();
        Boolean status = Integer.parseInt(responseData.get("status").toString()) == 1;
        String message = responseData.get("message").toString();
        Map<String, Object> data = (Map<String, Object>) responseData.get("result");

        return new ApiResponse<>(status, message, data);
    }

    public ApiResponse<Map<String,Object>> vpaAnalysis(GetNameByVpaSchema body){

        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();
        header.putSingle("authkey", "ODWPKDP73RC9937");
        ApiResponse<Object> response = this.networkCallExternalService("https://vpa-analysis.befisc.com",
        "post", body, header);
        Map<String, Object> responseData = (Map<String, Object>) response.getData();
        Boolean status = Integer.parseInt(responseData.get("status").toString()) == 1;
        String message = responseData.get("message").toString();
        Map<String, Object> data = (Map<String, Object>) responseData.get("result");

        return new ApiResponse<>(status, message, data);

    }

   public ApiResponse<Map<String,Object>> bankDetails(GetBankDetailsByAccSchema body){

        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();
        header.putSingle("authkey", "ODWPKDP73RC9937");

        Map<String, Object> reqBody = new HashMap();
        reqBody.put("account_no",body.getAccountNumber());
        reqBody.put("ifsc_code", body.getIfsc_code());
        reqBody.put("name", "Steve");
        reqBody.put("consent", "Y");
        reqBody.put("consent_text","I give my consent to Bank Account Verification (Penny Less) api to check my bank details");


        ApiResponse<Object> response = this.networkCallExternalService("https://bank-account-verification.befisc.com/penny-less",
        "post", reqBody, header);

        logger.info("mobile upi supreme response {}", Helper.toJsonString(response));

        Map<String, Object> responseData = (Map<String, Object>) response.getData();
        Boolean status = Integer.parseInt(responseData.get("status").toString()) == 1;
        String message = responseData.get("message").toString();
        Map<String, Object> data = (Map<String, Object>) responseData.get("result");

        return new ApiResponse<>(status, message, data);

    }
}

