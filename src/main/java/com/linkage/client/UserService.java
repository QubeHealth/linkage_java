package com.linkage.client;


import java.util.Map;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;

public class UserService extends BaseServiceClient {

    public UserService(LinkageConfiguration configuration) {
        super(configuration);
    }

    public ApiResponse<Object> getMobileNo(Long userId) {
        String url = configuration.getUserJavaUrl() + "auth/getMobileNo";

        return this.networkCallInternalService(url, "post", Map.of("user_id",userId), null);
    }

}
