package com.linkage.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.api.services.storage.Storage.AnywhereCaches.List;
import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.validations.GetVpaByMobileSchema;
import com.linkage.core.validations.RefereeCashbackMsgSchema;
import com.linkage.core.validations.RefereeInviteMsgSchema;
import com.linkage.utility.Helper;

import jakarta.ws.rs.core.MultivaluedHashMap;

public class WatiService extends BaseServiceClient {
    private String refereeInviteTemplate = "qp_cashback_referal_invite24may2024";
    private String referrerCashbackTemplate = "qp_cashback_referrer_24may2024";
    private String refereeCashbackTemplate = "qp_cashback_referree_24may2024";

    public WatiService(LinkageConfiguration configuration) {
        super(configuration);
    }

    public ApiResponse<Object> referreeInviteMessage(RefereeInviteMsgSchema body) {

        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();

        header.putSingle("Authorization", configuration.getWatiToken());
        String url = configuration.getWatiUrl() + body.getMobile();
        
        //logger.info("mobile upi supreme response {}", Helper.toJsonString(response));

        //Arraylist of key value pairs then added to hashmap
        Map<String, String> parameter = new HashMap<>();
        parameter.put("name", "cashback_amount");
        parameter.put("value", body.getCashbackAmt().toString());
        ArrayList<Map<String, String>> parameters = new ArrayList<>();
        parameters.add(parameter);
        Map<String, Object> mainMap = new HashMap<>();
        mainMap.put("template_name", refereeInviteTemplate);
        mainMap.put("broadcast_name", "test referal");
        mainMap.put("parameters", parameters);

       return this.networkCallExternalService(url, "POST", mainMap, header);


    }

    public ApiResponse<Object> referrerCashbackMessage(RefereeInviteMsgSchema body)
    {
        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();

        header.putSingle("Authorization", configuration.getWatiToken());
        String url = configuration.getWatiUrl() + body.getMobile();
        
        //Arraylist of key value pairs then added to hashmap
        Map<String, String> parameter = new HashMap<>();
        parameter.put("name", "cashback_amount");
        parameter.put("value", body.getCashbackAmt().toString());
        ArrayList<Map<String, String>> parameters = new ArrayList<>();
        parameters.add(parameter);
        Map<String, Object> mainMap = new HashMap<>();
        mainMap.put("template_name", referrerCashbackTemplate);
        mainMap.put("broadcast_name", "test referal");
        mainMap.put("parameters", parameters);

       return this.networkCallExternalService(url, "POST", mainMap, header);

    }
    public ApiResponse<Object> refereeCashbackMessage(RefereeCashbackMsgSchema body)
    {
        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();

        header.putSingle("Authorization", configuration.getWatiToken());
        String url = configuration.getWatiUrl() + body.getMobile();
        
        //Arraylist of key value pairs then added to hashmap
        Map<String, String> parameter = new HashMap<>();
        parameter.put("name", "cashback_amount");
        parameter.put("value", body.getCashbackAmt().toString());
        Map<String, String> parameter2 = new HashMap<>();
        parameter.put("name", "company_name");
        parameter.put("value", body.getCompany().toString());
        ArrayList<Map<String, String>> parameters = new ArrayList<>();
        parameters.add(parameter);
        parameters.add(parameter2);
        Map<String, Object> mainMap = new HashMap<>();
        mainMap.put("template_name", refereeCashbackTemplate);
        mainMap.put("broadcast_name", "test referal");
        mainMap.put("parameters", parameters);

       return this.networkCallExternalService(url, "POST", mainMap, header);

    }

}