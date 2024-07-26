package com.linkage.client;

import java.util.Map;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;

public class UserService extends BaseServiceClient {

    public UserService(LinkageConfiguration configuration) {
        super(configuration);
    }
    
    public ApiResponse<Object> getQbUserId(String partneredUserId) {

        String url = configuration.getUserJavaUrl() + "/emailerUser/getQbUserId";
        return this.networkCallInternalService(url, "post", partneredUserId, null);
    }
}
