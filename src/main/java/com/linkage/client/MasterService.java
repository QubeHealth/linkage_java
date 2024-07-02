package com.linkage.client;

import java.util.Map;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;

public class MasterService extends BaseServiceClient{

    public MasterService(LinkageConfiguration configuration) {
        super(configuration);
    }

    public ApiResponse<Object> mailDataStore(Map<String, String> body) {

        String url = configuration.getMasterurl() + "/prefunded/emailInsert";
        return this.networkCallInternalService(url, "post", body, null);
    }

}
