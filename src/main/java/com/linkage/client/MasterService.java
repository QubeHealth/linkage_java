package com.linkage.client;

import java.util.Map;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;

public class MasterService extends BaseServiceClient{

    public MasterService(LinkageConfiguration configuration) {
        super(configuration);
    }

    public ApiResponse<Object> prefundedEmail(Map<String, Object> body) {

        String url = configuration.getMasterurl() + "/prefunded/prefundedEmailer";
        ApiResponse<Object> predundedEmail = this.networkCallInternalService(url, "post", body, null);
        return predundedEmail;
    }

    public ApiResponse<Object> emailInsert(Map<String, Object> body){
        String url = configuration.getMasterurl() + "/prefunded/emailItems";
        ApiResponse<Object> predundedEmail = this.networkCallInternalService(url, "post", body, null);
        return predundedEmail;
    }

}
