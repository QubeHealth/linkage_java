package com.linkage.client;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.validations.GetBankDetailsByAccSchema;
import com.linkage.core.validations.GetVpaByMobileSchema;
import com.linkage.core.validations.GetVpaDetailsSchema;

import jakarta.ws.rs.core.MultivaluedHashMap;

public class BefiscService extends BaseServiceClient {

    private static final String CONSENT = "I give my consent to Bank Account Verification (Penny Less) api to check my bank details";

    public BefiscService(LinkageConfiguration configuration) {
        super(configuration);
    }

    public ApiResponse<Map<String, Object>> mobileUpiSupreme(GetVpaByMobileSchema body) {
        return callExternalService("https://mobile-lookup-supreme.befisc.com", body);
    }

    public ApiResponse<Map<String, Object>> multipleUpi(GetVpaByMobileSchema body) {
        return callExternalService("https://mobile-lookup-for-multiple-upi.befisc.com", body);
    }

    public ApiResponse<Map<String, Object>> vpaAnalysis(GetVpaDetailsSchema body) {
        return callExternalService("https://vpa-analysis.befisc.com", body);
    }

    public ApiResponse<Map<String, Object>> bankDetails(GetBankDetailsByAccSchema body) {
        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();
        header.putSingle("authkey", configuration.getBefiscAuthKey());

        Map<String, Object> reqBody = new HashMap<>();
        reqBody.put("account_no", body.getAccountNumber());
        reqBody.put("ifsc_code", body.getIfscCode());
        reqBody.put("name", ""); // optional field
        reqBody.put("consent", "Y");
        reqBody.put("consent_text", CONSENT);

        return callExternalService("https://bank-account-verification.befisc.com/penny-less", reqBody);
    }

    private ApiResponse<Map<String, Object>> callExternalService(String url, Object body) {
        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();
        header.putSingle("authkey", configuration.getBefiscAuthKey());

        try {
            ApiResponse<Object> response = this.networkCallExternalService(url, "post", body, header);
            Map<String, Object> responseData = (Map<String, Object>) response.getData();
            Boolean status = Integer.parseInt(responseData.get("status").toString()) == 1;
            String message = responseData.get("message").toString();
            Map<String, Object> data = (Map<String, Object>) responseData.get("result");
            return new ApiResponse<>(status, message, data);
        } catch (Exception e) {
            // Handle exception or log error
            System.out.println(e.getMessage());
            return new ApiResponse<>(false, "Something went wrong", null);
        }
    }

}
