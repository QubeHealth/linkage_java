package com.linkage.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.validations.AdjudicationStatusMessageSchema;
import com.linkage.core.validations.AhcAppointmentReportSchema;
import com.linkage.core.validations.AhcBookConfirmSchema;
import com.linkage.core.validations.BillRejectedSchema;
import com.linkage.core.validations.BillVerifiedMsgSchema;
import com.linkage.core.validations.CashbackTypeMessageSchema;
import com.linkage.core.validations.CreditAssignedSchema;
import com.linkage.core.validations.DisbursedMessageSchema;
import com.linkage.core.validations.MessageProviderSchema;
import com.linkage.core.validations.MessageProviderSchema.SendMessageSchema;
import com.linkage.core.validations.RefereeCashbackMsgSchema;
import com.linkage.core.validations.RefereeInviteMsgSchema;
import com.linkage.utility.Helper;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;

public class MessageProviderService extends BaseServiceClient {
    private static final String REFEREE_INVITE_TEMPLATE = "qp_cashback_referal_invite24may2024";
    private static final String REFERER_CASHBACK_TEMPLATE = "qp_cashback_referrer_24may2024";
    private static final String REFEREE_CASHBACK_TEMPLATE = "qp_cashback_referree_24may2024";
    private static final String CASHBACK_TEMPLATE = "qp_ubv_24may2024";
    private static final String BILL_VERIFIED_TEMPLATE = "qp_ubv_24may2024";
    private static final String BILL_PARTIAL_VERIFIED_TEMPLATE = "qp_ubr_new24may2024";
    private static final String BILL_REJECTED_TEMPLATE = "qp_ubr_new24may2024";

    private static final String ADJUDICATION_APPROVED = "ncif_adjudication_2_22nov2023";
    private static final String ADJUDICATION_INPROGRESS = "ncif_adjudication_1_22nov2023";
    private static final String ADJUDICATION_REJECTED = "ncif_adjudication_3_22nov2023";

    private static final String AHC_APPOINTMENT_REPORT = "ahc_appt_report_8_sept_2023";// "ahc_appointment_report_8_sept_2023";
    private static final String AHC_APPOINTMENT_CONFIRM = "ahc_appointment_confirmation_7_sept_2023";

    private static final String CREDIT_ASSIGNED = "qc_limit_assigned_22nov2023";
    private static final String DISBURSEMENT_SUCCESS = "qc_disbursement_successful_22nov2023";
    private static final String ALLOWED_TO_CREDIT_LIMIT = "qc_elign_29Mar2024";

    public ApiResponse<Object> templatesData;

    public MessageProviderService(LinkageConfiguration configuration) {
        super(configuration);
        this.templatesData = getTemplates();
    }

    // referee Invite message
    public ApiResponse<Object> referreeInviteMessage(RefereeInviteMsgSchema body) {

        // Arraylist of key value pairs then added to hashmap
        SendMessageSchema parameter = new SendMessageSchema();
        parameter.setMobile(body.getMobile());
        parameter.setElementName(REFEREE_INVITE_TEMPLATE);
        List<String> params = new ArrayList<>();
        params.add(body.getCashbackAmt().toString());
        parameter.setParams(params);
        return sendMessage(parameter);

    }

    // Referrer gets cashback after referee registers with message invite
    public ApiResponse<Object> referrerCashbackMessage(RefereeInviteMsgSchema body) {

        // Arraylist of key value pairs then added to hashmap
        SendMessageSchema parameter =  new SendMessageSchema();
        parameter.setMobile(body.getMobile());
        parameter.setElementName(REFERER_CASHBACK_TEMPLATE);
        List<String> params = new ArrayList<>();
        params.add(body.getCashbackAmt().toString());
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

        SendMessageSchema parameter = new SendMessageSchema();
        parameter.setMobile(body.getMobile());
        parameter.setElementName(REFEREE_CASHBACK_TEMPLATE);
        // Create a list to hold the parameter values
        List<String> params = new ArrayList<>();
        
        // Add values to the list
        params.add(body.getCashbackAmt().toString());
        params.add(body.getCompany());
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

    // Send Adjudication
    public ApiResponse<Object> sendAdjudicationMessage(AdjudicationStatusMessageSchema body) {

        SendMessageSchema parameter = new SendMessageSchema();
        parameter.setMobile(body.getMobile());    
        // Create a list to hold the parameter values
        List<String> params = new ArrayList<>();
        
        // Add values to the list
        params.add(body.getFirstName());
        params.add(body.getStatus().toString());
        params.add(body.getMobile());
        parameter.setParams(params);
        if (body.getStatus().toLowerCase().equals("approved") ){
            parameter.setElementName(ADJUDICATION_APPROVED);
        } else if (body.getStatus().toLowerCase().equals("rejected") ){
            parameter.setElementName(ADJUDICATION_REJECTED);
        } else {
            parameter.setElementName(ADJUDICATION_INPROGRESS);
        }
        return sendMessage(parameter);

    }

    // Appointment Confirmed
    public ApiResponse<Object> appointmentConfirmed(AhcBookConfirmSchema body) {

        SendMessageSchema parameter = new SendMessageSchema();
        parameter.setMobile(body.getMobile());    
        // Create a list to hold the parameter values
        List<String> params = new ArrayList<>();
        
        // Add values to the list
        params.add(body.getFirstName().toString());
        params.add(body.getDiagnosticsAddress());
        params.add(body.getAppointmentDate());
        params.add(body.getAppointmentTime());
        parameter.setParams(params);
        parameter.setElementName(AHC_APPOINTMENT_CONFIRM);
        return sendMessage(parameter);

    }

    // Appointment Confirmed
    public ApiResponse<Object> ahcReportMessage(AhcAppointmentReportSchema body) {

        SendMessageSchema parameter = new SendMessageSchema();
        parameter.setMobile(body.getMobile());    
        // Create a list to hold the parameter values
        List<String> params = new ArrayList<>();
        // Add values to the list
        params.add(body.getFirstName().toString());
        params.add(body.getAppointmentDate());
        parameter.setParams(params);
        parameter.setElementName(AHC_APPOINTMENT_REPORT);
        return sendMessage(parameter);

    }

    // Credit Assigned
    public ApiResponse<Object> creditAssigned(CreditAssignedSchema body) {

        SendMessageSchema parameter = new SendMessageSchema();
        parameter.setMobile(body.getMobile());    
        // Create a list to hold the parameter values
        List<String> params = new ArrayList<>();
        // Add values to the list
        params.add(body.getFirstName().toString());
        params.add("qubehealth");
        params.add(body.getCreditAssigned());
        parameter.setParams(params);
        parameter.setElementName(CREDIT_ASSIGNED);
        return sendMessage(parameter);

    }

    // Disbursement Succesful
    public ApiResponse<Object> disbursementMessage(DisbursedMessageSchema body) {

        SendMessageSchema parameter = new SendMessageSchema();
        parameter.setMobile(body.getMobile());    
        // Create a list to hold the parameter values
        List<String> params = new ArrayList<>();
        // Add values to the list
        params.add(body.getDisbursedAmount().toString());
        params.add(body.getHspName());
        params.add(body.getTransactionId());
        params.add(new Date().toString());
        parameter.setParams(params);
        parameter.setElementName(DISBURSEMENT_SUCCESS);
        return sendMessage(parameter);

    }
    
    // Send Credit Limit Request
    public ApiResponse<Object> allowedToRequestCredit(CreditAssignedSchema body) {
        SendMessageSchema parameter = new SendMessageSchema();
        parameter.setMobile(body.getMobile());    
        // Create a list to hold the parameter values
        List<String> params = new ArrayList<>();
        // Add values to the list
        params.add(body.getFirstName().toString());
        parameter.setParams(params);
        parameter.setElementName(ALLOWED_TO_CREDIT_LIMIT);
        return sendMessage(parameter);
    }

    public ApiResponse<Object> getTemplates() {
        final String providerToken = configuration.getMessageProviderToken();
        final String providerBaseUrl = configuration.getMessageProviderTemplateUrl();
        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();
        header.putSingle("apikey", providerToken);
        return this.networkCallExternalService(providerBaseUrl, "get", null, header);
    }
    
    public ApiResponse<Object> sendMessage(MessageProviderSchema.SendMessageSchema parameter) {

        @SuppressWarnings("unchecked")
        final Map<String, Object> response = (Map<String, Object>) templatesData.getData();
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("templates");
    
        final Map<String, Object> extractedTemplateData = extractAppIdForElementName(data, parameter.getElementName());

        // Get Template Object
        final Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("id", extractedTemplateData.get("id"));
        templateParams.put("params", parameter.getParams());
        final JSONObject templateObject =  new JSONObject(templateParams);
    
        // Send Params
        final Map<String, String> params = new HashMap<>();
        params.put("channel", configuration.getMessageProviderChannel());
        params.put("source", configuration.getMessageProviderSource());
        params.put("destination", parameter.getMobile());
        if (!extractedTemplateData.get("templateType").toString().equalsIgnoreCase("TEXT")) {
            final JSONObject selectedTemplateData = new JSONObject(extractedTemplateData.get("containerMeta").toString());
            final JSONObject imageObject = new JSONObject();
            imageObject.put("id", selectedTemplateData.getString("mediaId").toString());
            imageObject.put("type", "image");
            final JSONObject messagObject = new JSONObject();
            messagObject.put("image", imageObject.toString());
        }
        params.put("template", templateObject.toString());
        final String urlEncodedString = Helper.convertToUrlEncoded(params);
        final MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();
        header.putSingle("apikey", configuration.getMessageProviderToken());
        header.putSingle("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
        
        return this.networkCallExternalService(configuration.getMessageProviderSendMessageUrl(), "post", urlEncodedString, header);
    }
    



    private static Map<String, Object> extractAppIdForElementName(List<Map<String, Object>> data, String targetElementName) {
        try {
            // Use Java Streams to process the list of Maps
            return data.stream()
                .filter(map -> targetElementName.equals(map.get("elementName")))
                .map(map ->  map)
                .findFirst()
                .orElse(null); // Return the first matching id or null if none found
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exceptions (e.g., logging)
            return null;
        }
    }
}