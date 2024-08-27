package com.linkage.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.validations.BillRejectedSchema;
import com.linkage.core.validations.BillVerifiedMsgSchema;
import com.linkage.core.validations.CashbackTypeMessageSchema;
import com.linkage.core.validations.MessageProviderSchema;
import com.linkage.core.validations.MessageProviderSchema.SendMessageSchema;
import com.linkage.core.validations.RefereeCashbackMsgSchema;
import com.linkage.core.validations.RefereeInviteMsgSchema;
import com.linkage.utility.Helper;

import jakarta.ws.rs.core.MultivaluedHashMap;

public class MessageProviderService extends BaseServiceClient {
    private static final String REFEREE_INVITE_TEMPLATE = "qc_bth1hr_awareness_22aug2024";
    private static final String REFERER_CASHBACK_TEMPLATE = "test_template_var21aug2024";
    private static final String REFEREE_CASHBACK_TEMPLATE = "test_template_var21aug2024";
    private static final String CASHBACK_TEMPLATE = "temp_marketing_21aug2024";
    private static final String BILL_VERIFIED_TEMPLATE = "temp_marketing_21aug2024";
    private static final String BILL_PARTIAL_VERIFIED_TEMPLATE = "temp_marketing_21aug2024";
    private static final String BILL_REJECTED_TEMPLATE = "qc_bth1hr_awareness_22aug2024";

    public ApiResponse<Object> templatesData;



    public MessageProviderService(LinkageConfiguration configuration) {
        super(configuration);

        templatesData = getTemplates();

    
    }


    // referee Invite message
    public ApiResponse<Object> referreeInviteMessage(RefereeInviteMsgSchema body) {

        // Arraylist of key value pairs then added to hashmap
        SendMessageSchema parameter =  new SendMessageSchema();
        parameter.setMobile(body.getMobile());
        parameter.setElementName(REFEREE_INVITE_TEMPLATE);
        List<String> params = new ArrayList<>();
        params.add(body.getCashbackAmt().toString());
        parameter.setParams(params);

        return sendMessage(parameter);

    }

    // Referrer gets cashback after referee registers with message invite
    public ApiResponse<Object> referrerCashbackMessage(RefereeInviteMsgSchema body) {

        Integer cbReferer = Integer.parseInt(body.getCashbackAmt().toString());

        // Arraylist of key value pairs then added to hashmap
        SendMessageSchema parameter =  new SendMessageSchema();
        parameter.setMobile(body.getMobile());
        parameter.setElementName(REFERER_CASHBACK_TEMPLATE);
        List<String> params = new ArrayList<>();
        params.add(cbReferer.toString());
        parameter.setParams(params);

        return sendMessage(parameter);

    }
    public ApiResponse<Object> cashbackTypeMessage(CashbackTypeMessageSchema body){
        // Arraylist of key value pairs then added to hashmap

        SendMessageSchema parameter = new SendMessageSchema();
        parameter.setMobile(body.getMobile());
        parameter.setElementName(CASHBACK_TEMPLATE);
        // Create a list to hold the parameter values
        List<String> params = new ArrayList<>();
        
        // Add values to the list
        params.add(body.getFirstName());
        params.add(body.getCashbackAmt().toString());
        params.add(body.getOnlineStore());
        parameter.setParams(params);

        return sendMessage(parameter);
    }

    // Referee gets cashback after registering with message invite
    public ApiResponse<Object> refereeCashbackMessage(RefereeCashbackMsgSchema body) {

        Integer cbReferee = Integer.parseInt(body.getCashbackAmt().toString());

        SendMessageSchema parameter = new SendMessageSchema();
        parameter.setMobile(body.getMobile());
        parameter.setElementName(REFEREE_CASHBACK_TEMPLATE);
        // Create a list to hold the parameter values
        List<String> params = new ArrayList<>();
        
        // Add values to the list
        params.add(cbReferee.toString());
        params.add( body.getCompany());
        parameter.setParams(params);

        return sendMessage(parameter);
    }

    // User uploads bill for cashback, this is the case when bill is verified
    public ApiResponse<Object> billVerifiedMessage(BillVerifiedMsgSchema body) {

        SendMessageSchema parameter = new SendMessageSchema();
        parameter.setMobile(body.getMobile());    
        // Create a list to hold the parameter values
        List<String> params = new ArrayList<>();
        
        // Add values to the list
        params.add(body.getCashbackAmt().toString());
        params.add(  body.getFirstname());
        parameter.setParams(params);
        if (body.getBillStatus().equals("APPROVED") ){
            parameter.setElementName(BILL_VERIFIED_TEMPLATE);
        }else{
            parameter.setElementName(BILL_PARTIAL_VERIFIED_TEMPLATE);
        }
        return sendMessage(parameter);

    }

    // bill rejected no cashback
    public ApiResponse<Object> billRejected(BillRejectedSchema body) {

        SendMessageSchema parameter = new SendMessageSchema();
        parameter.setMobile(body.getMobile());
        parameter.setElementName(BILL_REJECTED_TEMPLATE);
        // Create a list to hold the parameter values
        List<String> params = new ArrayList<>();
        
        // Add values to the list
        params.add(  body.getFirstname());
        parameter.setParams(params);
        return sendMessage(parameter);

    }

    // private ApiResponse<Object> sendMessage(String mobile, String templateName, String broadCastName,
    //         List<Map<String, String>> paramList) {
    //     MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();

    //     header.putSingle("Authorization", configuration.getMessageProviderToken());
    //     String url = configuration.getMessageProviderUrl();

    //     Map<String, Object> mainMap = new HashMap<>();
    //     mainMap.put("source", "91" + configuration.getMessageProviderSource());
    //     mainMap.put("channel", configuration.getMessageProviderChannel());
    //     mainMap.put("destination", "91" + mobile);

    //     Map<String, String> messageMap = new HashMap<>();
    //     messageMap.put("type", "text");
    //     messageMap.put("text", "text");
    //     mainMap.put("message",messageMap);
    //     mainMap.put("parameters", paramList);

    //     header.putSingle("apikey", "null");



    //     return this.networkCallExternalService(url, "POST", mainMap, header);
    // }

    public ApiResponse<Object>  getTemplates() {
        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();
        header.putSingle("apikey", "mmvjw44hmoihd5v3pilrubbhdtjinfm5");
        String url = "https://api.gupshup.io/sm/api/v1/template/list/X4YjYkuCjDy6rd9z3l3lb0rV";
        
        return this.networkCallExternalService(url, "get", null, header);

    }

    
    public ApiResponse<Object> sendMessage(MessageProviderSchema.SendMessageSchema parameter) {

        ApiResponse<Object> result = getTemplates();
        if (!result.getStatus()) {
            result.setMessage("Failed to fetch the templates");
            return result;
        }
    
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) result.getData();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("templates");
    
        String templateId = extractAppIdForElementName(data, parameter.getElementName());
    
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put("id", templateId);
        jsonObject.put("params", parameter.getParams());
    
        Map<String, String> params = new HashMap<>();
        params.put("channel", "whatsapp");
        params.put("source", "917208024110");
        params.put("destination", "91" + parameter.getMobile());
        params.put("message", "{\"type\":\"text\",\"image\":{\"link\":\"\"}}");
        params.put("src.name", "X4YjYkuCjDy6rd9z3l3lb0rV");
        params.put("template", new HashMap<>(jsonObject).toString());
    
        String urlEncodedString = Helper.convertToUrlEncoded(params);
    
        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();
        header.putSingle("apikey", "mmvjw44hmoihd5v3pilrubbhdtjinfm5");
        header.putSingle("Content-Type", "application/x-www-form-urlencoded");
    
        String url = "https://api.gupshup.io/wa/api/v1/msg";
    
        return this.networkCallExternalService(url, "post", urlEncodedString, header);
    }
    
    private static String extractAppIdForElementName(List<Map<String, Object>> data, String targetElementName) {
        try {
            // Use Java Streams to process the list of Maps
            return data.stream()
                .filter(map -> targetElementName.equals(map.get("elementName")))
                .map(map -> (String) map.get("id"))
                .findFirst()
                .orElse(null); // Return the first matching id or null if none found
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exceptions (e.g., logging)
            return null;
        }
    }
}