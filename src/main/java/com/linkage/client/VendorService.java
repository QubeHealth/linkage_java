package com.linkage.client;

import java.util.HashMap;
import java.util.Map;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.validations.DigitapSchema.SendAadharOtp;

import jakarta.ws.rs.core.MultivaluedHashMap;

public class VendorService extends BaseServiceClient {

    public VendorService(LinkageConfiguration configuration) {
        super(configuration);
    }

    public ApiResponse<Object> sendAadharOtp(SendAadharOtp body) {

        Map<String, String> reqBody = new HashMap<>();
        reqBody.put("aadhaar", body.getAadharNumber());
        // Encode the string in Base64
        String authString = configuration.getBefiscAuthKey();
        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();
        header.putSingle("content-type", "application/json");
        header.putSingle("authkey", authString);
        
        final String url = configuration.getBefiscSendAadharOtpUrl();
        return this.networkCallExternalService(url, "POST", reqBody, header);

    }

    public ApiResponse<Object> verifyAadharOtp(Map<String,Object> body) {

        // Encode the string in Base64
        String authString = configuration.getBefiscAuthKey();
        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();
        header.putSingle("content-type", "application/json");
        header.putSingle("authkey", authString);
        final String url = configuration.getBefiscVerifyAadharOtpUrl();
        return this.networkCallExternalService(url, "POST", body, header);

    }
}
