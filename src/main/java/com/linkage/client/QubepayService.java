package com.linkage.client;

import java.util.HashMap;
import java.util.Map;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;

public class QubepayService extends BaseServiceClient {
    
        public QubepayService(LinkageConfiguration configuration) {
        super(configuration);
    }

     public ApiResponse<Object> getUserCashbackPercentage(Long user_id) {

        Map<String, Long> reqBody = new HashMap<>();
        reqBody.put("user_id", user_id);

        final String url = configuration.getQubepayUrl() + "user/getUserCashbackPercentage";
        return this.networkCallInternalService(url, "POST", reqBody, null);

    }
}
