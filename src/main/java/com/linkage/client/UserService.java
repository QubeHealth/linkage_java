package com.linkage.client;


import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;

public class UserService extends BaseServiceClient {

    public UserService(LinkageConfiguration configuration) {
        super(configuration);
    }

    public ApiResponse<Object> getMobileNo(Long userId) {
        String url = configuration.getUserUrl() + "users/getMobileNo";

        return this.networkCallInternalService(url, "post", userId, null);
    }

}
