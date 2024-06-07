package com.linkage.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.validations.BillRejectedSchema;
import com.linkage.core.validations.BillVerifiedMsgSchema;
import com.linkage.core.validations.CashbackTypeMessageSchema;
import com.linkage.core.validations.RefereeCashbackMsgSchema;
import com.linkage.core.validations.RefereeInviteMsgSchema;


import jakarta.ws.rs.core.MultivaluedHashMap;

public class WatiService extends BaseServiceClient {
    private static final String REFEREE_INVITE_TEMPLATE = "qp_cashback_referal_invite24may2024";
    private static final String REFERER_CASHBACK_TEMPLATE = "qp_cashback_referrer_24may2024";
    private static final String REFEREE_CASHBACK_TEMPLATE = "qp_cashback_referree_24may2024";
    private static final String BILL_VERIFIED_TEMPLATE = "qp_ubv_24may2024";
    private static final String BILL_PARTIAL_VERIFIED_TEMPLATE = "qp_ubpv_24may2024";
    private static final String BILL_REJECTED_TEMPLATE = "qp_ubr_new24may2024";
    private static final String APP_REVIEWER_TEMPLATE = "cashback_appreviews_05june2024";

    public WatiService(LinkageConfiguration configuration) {
        super(configuration);
    }

    // referee Invite message
    public ApiResponse<Object> referreeInviteMessage(RefereeInviteMsgSchema body) {

        // Arraylist of key value pairs then added to hashmap
        Map<String, String> parameter = new HashMap<>();
        parameter.put("name", "cashback_amount");
        parameter.put("value", body.getCashbackAmt().toString());

        return sendMessage(body.getMobile(), REFEREE_INVITE_TEMPLATE, "REFEREE_INVITE", List.of(parameter));

    }

    // Referrer gets cashback after referee registers with message invite
    public ApiResponse<Object> referrerCashbackMessage(RefereeInviteMsgSchema body) {

        // Arraylist of key value pairs then added to hashmap
        Map<String, String> parameter = new HashMap<>();
        parameter.put("name", "cashback_amount");
        parameter.put("value", body.getCashbackAmt().toString());

        return sendMessage(body.getMobile(), REFERER_CASHBACK_TEMPLATE, "REFERER_CASHBACK", List.of(parameter));

    }
    public ApiResponse<Object> cashbackTypeMessage(CashbackTypeMessageSchema body){
        // Arraylist of key value pairs then added to hashmap
        Map<String, String> parameter1 = new HashMap<>();
        parameter1.put("name", "first_name");
        parameter1.put("value", body.getFirstName());
        Map<String, String> parameter2 = new HashMap<>();
        parameter2.put("name", "cashback_amount");
        parameter2.put("value", body.getCashbackAmt().toString());
        Map<String, String> parameter3 = new HashMap<>();
        parameter3.put("name", "online_store");
        parameter3.put("value", body.getOnlineStore());

        return sendMessage(body.getMobile(), APP_REVIEWER_TEMPLATE, "APP_REVIEWER", List.of(parameter1,parameter2,parameter3));
    }

    // Referee gets cashback after registering with message invite
    public ApiResponse<Object> refereeCashbackMessage(RefereeCashbackMsgSchema body) {

        // Arraylist of key value pairs then added to hashmap
        Map<String, String> parameter1 = new HashMap<>();
        parameter1.put("name", "cashback_amount");
        parameter1.put("value", body.getCashbackAmt().toString());
        Map<String, String> parameter2 = new HashMap<>();
        parameter2.put("name", "company_name");
        parameter2.put("value", body.getCompany());

        return sendMessage(body.getMobile(), REFEREE_CASHBACK_TEMPLATE, "REFEREE_CASHBACK",
                List.of(parameter1, parameter2));
    }

    // User uploads bill for cashback, this is the case when bill is verified
    public ApiResponse<Object> billVerifiedMessage(BillVerifiedMsgSchema body) {

        Map<String, String> parameter1 = new HashMap<>();
        parameter1.put("name", "cashback_amount");
        parameter1.put("value", body.getCashbackAmt().toString());
        Map<String, String> parameter2 = new HashMap<>();
        parameter2.put("name", "first_name");
        parameter2.put("value", body.getFirstname());

        String templateName = body.getBillStatus().equals("APPROVED") ? BILL_VERIFIED_TEMPLATE
                : BILL_PARTIAL_VERIFIED_TEMPLATE;

        return sendMessage(body.getMobile(), templateName, "BILL_REJECTED", List.of(parameter1, parameter2));

    }

    // bill rejected no cashback
    public ApiResponse<Object> billRejected(BillRejectedSchema body) {

        Map<String, String> parameter = new HashMap<>();
        parameter.put("name", "first_name");
        parameter.put("value", body.getFirstname());

        return sendMessage(body.getMobile(), BILL_REJECTED_TEMPLATE, "BILL_REJECTED", List.of(parameter));

    }

    private ApiResponse<Object> sendMessage(String mobile, String templateName, String broadCastName,
            List<Map<String, String>> paramList) {
        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();

        header.putSingle("Authorization", configuration.getWatiToken());
        String url = configuration.getWatiUrl() + mobile;

        Map<String, Object> mainMap = new HashMap<>();
        mainMap.put("template_name", templateName);
        mainMap.put("broadcast_name", broadCastName);
        mainMap.put("parameters", paramList);

        return this.networkCallExternalService(url, "POST", mainMap, header);
    }

}