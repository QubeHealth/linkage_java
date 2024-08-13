package com.linkage.client;

import java.util.Map;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;

public class ImsNwService extends BaseServiceClient {

    public ImsNwService(LinkageConfiguration configuration) {
        super(configuration);
    }


    public ApiResponse<Object> redeemVoucher(Map<String, Object> requestBody) {

        String url = configuration.getImsUrl() + "erupee/redeemVoucher";

        return this.networkCallInternalService(url, "post", requestBody, null);
    }

}


