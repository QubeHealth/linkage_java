package com.linkage.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.validations.KaleyraInviteMessageSchema;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Request;

public class KaleyraService extends BaseServiceClient {

    private static final String KALEYRA_INVITE_TEMPLATE = "1107171756597399694";
    private static final String KALEYRA_SENDER = "QBHLTH";
 

    public KaleyraService(LinkageConfiguration configuration) {
        super(configuration);
    }
    
    // public ApiResponse<Object> kaleyraInviteMessage(KaleyraInviteMessageSchema body){
    //     // Arraylist of key value pairs then added to hashmap
    //     Map<String, String> parameter1 = new HashMap<>();
    //     parameter1.put("name", "first_name");
    //     parameter1.put("value", body.getFirstName());
        
    //     Map<String, String> parameter2 = new HashMap<>();
    //     parameter2.put("name", "discountAmt");
    //     parameter2.put("value", body.getCashbackAmt().toString());
        
    //     Map<String, String> parameter3 = new HashMap<>();
    //     parameter3.put("name", "mobile");
    //     parameter3.put("value", body.getMobile());

    //     return sendMessage(body.getMobile(), KALEYRA_INVITE_TEMPLATE, "APP_REVIEWER", List.of(parameter1,parameter2,parameter3));
    // }

    public ApiResponse<Object> kaleyraInviteMessage(KaleyraInviteMessageSchema body){
        String mobile = body.getMobile();
        String firstName = body.getFirstName();
        String DiscountAmt = String.valueOf(body.getDiscountAmount());

        // Construct the template body with placeholders
        String templateBody = "Hi, {firstName} has just added you to their QubeHealth Account. Get {DiscountAmt}% Cashback on All your Medical Bill Payments using QubePay. Just Download the App NOW!";
        templateBody = templateBody.replace("{firstName}", firstName);
        templateBody = templateBody.replace("{DiscountAmt}", DiscountAmt);

        String url = configuration.getKaleyraUrl() + configuration.getKaleyraSID() + "/messages";

        //HashMap for Url
        Map<String, String> messageAttributes = new HashMap<>();
        messageAttributes.put("to", "+91"+ mobile);
        messageAttributes.put("type", "OTP");
        messageAttributes.put("sender", KALEYRA_SENDER);
        messageAttributes.put("body", templateBody);
        messageAttributes.put("template_id", KALEYRA_INVITE_TEMPLATE);
        
        //header
        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();
        header.putSingle("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        header.putSingle("api-key", configuration.getKaleyraApiKey());



       
        return this.networkCallExternalService(url, "POST", messageAttributes, header);

    }
    


    // private ApiResponse<Object> sendMessage(String mobile, String templateName, String broadCastName,
    //         List<Map<String, String>> paramList) {
    //     MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();

    //     header.putSingle("Authorization", configuration.getKaleyraToken());
    //     String url = configuration.getKaleyraUrl() + mobile;
        
    //     //message body
    //     String templateBody = "Hi, {first_name} has just added you to their QubeHealth Account. Get {discountAmt}% Cashback on All your Medical Bill Payments using QubePay. Just Download the App NOW!";

    //     templateBody = templateBody.replace("{first_name}", firstName);
    //     templateBody = templateBody.replace("{discountAmt}", discountAmt);

    //     // for (Map<String, String> param : paramList) {
    //     //     templateBody = templateBody.replace("{" + param.get("name") + "}", param.get("value"));
    //     // }

    //     return this.networkCallExternalService(url, "POST", templateBody, header);
    // }
}
