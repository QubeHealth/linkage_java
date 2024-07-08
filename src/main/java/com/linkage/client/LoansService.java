package com.linkage.client;

import java.util.HashMap;
import java.util.Map;

import com.google.rpc.Help;
import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.utility.Helper;

public class LoansService extends BaseServiceClient{

    public LoansService(LinkageConfiguration configuration) {
        super(configuration);
    }

    public ApiResponse<Object> preFundedrequestStore(Map<String, Object> body) {
        String url = configuration.getLoansUrl() + "/transactions/addPrefundedRequest";
        ApiResponse<Object> resp = this.networkCallInternalService(url, "post", body, null);
        return resp;
    }

    public ApiResponse<Object> adjudicationDataStore(Map<String, Object> body) {
        String url = configuration.getLoansUrl() + "/transactions/addAdjudicationData";
        ApiResponse<Object> resp = this.networkCallInternalService(url, "post", body, null);
        return resp;
    }

    public ApiResponse<Object> adjudicationItemsStore(Map<String, Object> body) {
        String url = configuration.getLoansUrl() + "/transactions/addAdjudicationItems";
        ApiResponse<Object> resp = this.networkCallInternalService(url, "post", body, null);
        return resp;
       
    }

    public ApiResponse<Object> adjudicationQueryStore(Map<String, Object> body) {
        String url = configuration.getLoansUrl() + "/transactions/addAdjudicationQuery";
        ApiResponse<Object> resp = this.networkCallInternalService(url, "post", body, null);
        return resp;
    }

    public ApiResponse<Object> updateClaimNo(String partneredUserId, String claimNo) {
        String url = configuration.getLoansUrl() + "/transactions/updateClaimNo";
        Map<String, Object> body= new HashMap<>();
        body.put("partnered_user_id",partneredUserId);
        body.put("claim_no", claimNo);
        ApiResponse<Object> resp = this.networkCallInternalService(url, "post", body, null);
        return resp;
    }

    public ApiResponse<Object> updateStatusAdjudicationData(String claimNo, String status) {
        String url = configuration.getLoansUrl() + "/transactions/updateStatusAdjudicationData";
        Map<String, Object> body= new HashMap<>();
        body.put("claim_no",claimNo);
        body.put("status", status);
        ApiResponse<Object> resp = this.networkCallInternalService(url, "post", body, null);
        return resp;
    }

    public ApiResponse<Object> handleQueryReply(String claimNo, String status) {
        String url = configuration.getLoansUrl() + "/transactions/handleQueryReply";
        Map<String, Object> body= new HashMap<>();
        body.put("claim_no",claimNo);
        body.put("status", status);
        ApiResponse<Object> resp = this.networkCallInternalService(url, "post", body, null);
        return resp;
    }

    public ApiResponse<Object> getAdjudicationDataId(String claimNo) {
        String url = configuration.getLoansUrl() + "/transactions/getAdjudicationDataId";
        Map<String, Object> body= new HashMap<>();
        body.put("claim_no",claimNo);
        ApiResponse<Object> resp = this.networkCallInternalService(url, "post", body, null);
        return resp;
    }

    public ApiResponse<Object> getPrefundedRequestId(String claimNo) {
        String url = configuration.getLoansUrl() + "/transactions/getPrefundedRequestId";
        Map<String, Object> body= new HashMap<>();
        body.put("claim_no",claimNo);
        ApiResponse<Object> resp = this.networkCallInternalService(url, "post", body, null);
        return resp;
    }

    public ApiResponse<Object> checkQueryStatus(String claimNo) {
        String url = configuration.getLoansUrl() + "/transactions/checkForQuery";
        Map<String, Object> body= new HashMap<>();
        body.put("claim_no",claimNo);
        ApiResponse<Object> resp = this.networkCallInternalService(url, "post", body, null);
        return resp;
    }

    public ApiResponse<Object> updateInitialAmountsPrefunded(String claimNo, String requestedAmountInitial, String approvedAmountInitial) {
        String url = configuration.getLoansUrl() + "/transactions/checkForQuery";
        Map<String, Object> body= new HashMap<>();
        body.put("claim_no",claimNo);
        body.put("claim_no",requestedAmountInitial);
        body.put("claim_no",approvedAmountInitial);
        ApiResponse<Object> resp = this.networkCallInternalService(url, "post", body, null);
        return resp;
    }
}
