package com.linkage.client;

import java.util.Map;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;

public class LoansService extends BaseServiceClient{

    public LoansService(LinkageConfiguration configuration) {
        super(configuration);
    }

    public Object preFundedrequestStore(Map<String, Object> body) {
        String url = configuration.getLoansUrl() + "/transactions/addPrefundedRequest";
        ApiResponse<Object> resp = this.networkCallInternalService(url, "post", body, null);
        return resp.getData();
    }

}
