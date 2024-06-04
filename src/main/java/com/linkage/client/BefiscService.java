package com.linkage.client;

import java.util.Map;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
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
}
