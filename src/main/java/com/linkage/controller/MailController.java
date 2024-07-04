package com.linkage.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.commons.lang3.ObjectUtils.Null;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.LoansService;
import com.linkage.client.MailReaderService;
import com.linkage.client.MailWriterService;
import com.linkage.client.MasterService;
import com.linkage.core.constants.Constants;
import com.linkage.core.constants.Constants.EmailSubjectKeywords;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/mail")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MailController extends BaseController {

    private MailReaderService mailReaderService;
    private MasterService masterService;
    private LoansService loansService;

    public MailController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        this.mailReaderService = new MailReaderService(null, null, null, null, configuration);
        this.masterService = new MasterService(configuration);
        this.loansService = new LoansService(configuration);
    }

    @POST
    @Path("/emailReader")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response emailReader(@Context HttpServletRequest request) throws MessagingException, IOException {
        // Fetch and process the email
        Map<String, String> response = this.mailReaderService.fetchAndProcessEmail();

        // Extract common details from the response
        String type = response.get("type");

        // Based on the type, call different db functions using if-else conditions
        if (EmailSubjectKeywords.PRE_AUTH.equalsIgnoreCase(type)) {
            handlePreAuth(response);
        } else if (EmailSubjectKeywords.QUERY_REPLY.equalsIgnoreCase(type)) {
            handleQueryReply(response);
        } else if (EmailSubjectKeywords.FINAL_BILL_AND_DISCHARGE_SUMMARY.equalsIgnoreCase(type)) {
            handleFinalBillAndDischargeSummary(response);
        } else if (EmailSubjectKeywords.CASHLESS_CREDIT_REQUEST.equalsIgnoreCase(type)) {
            handleCashlessCreditRequest(response);
        } else if (EmailSubjectKeywords.ADDITIONAL_INFORMATION.equalsIgnoreCase(type)) {
            handleAdditionalInformation(response);
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity("Unknown type: " + type).build();
        }

        return Response.ok().entity("Processed successfully").build();
    }

    //Pre-Auth flow db calls
    private void handlePreAuth(Map<String, String> response) {

        String policyNo = response.get("policy_no");
        String khId = response.get("kh_id");
        String subject = response.get("subject");
        String patientName = response.get("patient_name");
        String gcpPath = response.get("gcp_path");
        String gcpFileName = response.get("gcp_file_name");

        Map<String, Object> preFundedReqMap = new HashMap<>();
        preFundedReqMap.put("user_id","123");
        preFundedReqMap.put("hsp_id","123");
        preFundedReqMap.put("partnered_user_id",policyNo);
        preFundedReqMap.put("tpa_desk_id",khId);
        preFundedReqMap.put("status","PENDING");
        preFundedReqMap.put("type","TPA");
        preFundedReqMap.put("processed_at",null);
        preFundedReqMap.put("requested_amount",null);
        preFundedReqMap.put("disbursement_amount",null);
        preFundedReqMap.put("is_active",true);
        preFundedReqMap.put("claim_no",null);

        Long preFundedRequest = (Long)this.loansService.preFundedrequestStore(preFundedReqMap);
       
        Map<String,Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put("type","PRE AUTH");
        preFundedEmailerMap.put("subject",subject);
        preFundedEmailerMap.put("is_active",true);
        preFundedEmailerMap.put("claim_no",null);
        preFundedEmailerMap.put("pf_req_id", preFundedRequest.toString());

        Long prefundedEmail = (Long)this.masterService.prefundedEmail(preFundedEmailerMap);

        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put("tpa_desk_id",khId);
        emailerItems.put("claim_no", null);
        emailerItems.put("initial_amt_req",null);
        emailerItems.put("initial_amt_approved",null);
        emailerItems.put("final_adj_amt_req", null);
        emailerItems.put("final_adj_amt_approved",null);
        emailerItems.put("metadata",subject);
        emailerItems.put("patient_name",patientName);

        Long emailItems = (Long)this.masterService.emailInsert(emailerItems);

        Map<String, Object> adjudicationDataMap = new HashMap<>();
        adjudicationDataMap.put("tpa_desk_id",khId);
        adjudicationDataMap.put("pf_req_id", prefundedEmail);
        adjudicationDataMap.put("requested_amount", null);
        adjudicationDataMap.put("estimated_amount", null);
        adjudicationDataMap.put("file_path", gcpPath);
        adjudicationDataMap.put("file_name", gcpFileName);
        adjudicationDataMap.put("user_id", policyNo);
        adjudicationDataMap.put("offer_id", null);
        adjudicationDataMap.put("hsp_id",123);
        adjudicationDataMap.put("document_id", null);
        adjudicationDataMap.put("associated_user_id", null);
        adjudicationDataMap.put("status", "PENDING");
        adjudicationDataMap.put("created_by", "TPA DESK");
        adjudicationDataMap.put("requested_by", null);

        Long adjudicationDataId = (Long)this.loansService.adjudicationDataStore(adjudicationDataMap);

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put("adjduciation_data_id",adjudicationDataId);
        adjudicationItems.put("pf_document_id",prefundedEmail);
        adjudicationItems.put("document_url", gcpPath);
        adjudicationItems.put("is_active", 1);

        Long adjudicationItemsId = (Long)this.loansService.adjudicationItemsStore(adjudicationItems);

    }
    
    //Query reply flow db calls
    private void handleQueryReply(Map<String, String> response) {
        
        String claimNo = response.get("claim_no");
        String khId = response.get("tpa_desk_id");
        String subject = response.get("subject");
        String patientName = response.get("patient_name");
        String body = response.get("body");
        String gcpPath = response.get("gcp_path");
        String gcpFileName = response.get("gcp_file_name");


        Map<String, Object> preFundedReqMap = new HashMap<>();
        preFundedReqMap.put("user_id","123");
        preFundedReqMap.put("hsp_id","123");
        preFundedReqMap.put("partnered_user_id",null);
        preFundedReqMap.put("tpa_desk_id",khId);
        preFundedReqMap.put("status","PENDING");
        preFundedReqMap.put("type","TPA");
        preFundedReqMap.put("processed_at",null);
        preFundedReqMap.put("requested_amount",null);
        preFundedReqMap.put("disbursement_amount",null);
        preFundedReqMap.put("is_active",true);
        preFundedReqMap.put("claim_no",claimNo);

        Long preFundedRequest = (Long)this.loansService.preFundedrequestStore(preFundedReqMap);
       
        Map<String,Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put("type","QUERY REPLY");
        preFundedEmailerMap.put("subject",subject);
        preFundedEmailerMap.put("is_active",true);
        preFundedEmailerMap.put("claim_no",claimNo);
        preFundedEmailerMap.put("pf_req_id", preFundedRequest.toString());

        Long prefundedEmail = (Long)this.masterService.prefundedEmail(preFundedEmailerMap);

        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put("tpa_desk_id",khId);
        emailerItems.put("claim_no", claimNo);
        emailerItems.put("initial_amt_req",null);
        emailerItems.put("initial_amt_approved",null);
        emailerItems.put("final_adj_amt_req", null);
        emailerItems.put("final_adj_amt_approved",null);
        emailerItems.put("metadata",subject + body);
        emailerItems.put("patient_name",patientName);

        Long emailItems = (Long)this.masterService.emailInsert(emailerItems);

        Map<String, Object> adjudicationDataMap = new HashMap<>();
        adjudicationDataMap.put("tpa_desk_id",khId);
        adjudicationDataMap.put("pf_req_id", prefundedEmail);
        adjudicationDataMap.put("requested_amount", null);
        adjudicationDataMap.put("estimated_amount", null);
        adjudicationDataMap.put("file_path", gcpPath);
        adjudicationDataMap.put("file_name", gcpFileName);
        adjudicationDataMap.put("user_id", null);
        adjudicationDataMap.put("offer_id", null);
        adjudicationDataMap.put("hsp_id",123);
        adjudicationDataMap.put("document_id", null);
        adjudicationDataMap.put("associated_user_id", null);
        adjudicationDataMap.put("status", "PENDING");
        adjudicationDataMap.put("created_by", "TPA DESK");
        adjudicationDataMap.put("requested_by", null);

        Long adjudicationDataId = (Long)this.loansService.adjudicationDataStore(adjudicationDataMap);

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put("adjduciation_data_id",adjudicationDataId);
        adjudicationItems.put("pf_document_id",prefundedEmail);
        adjudicationItems.put("document_url", gcpPath);
        adjudicationItems.put("is_active", 1);

        Long adjudicationItemsId = (Long)this.loansService.adjudicationItemsStore(adjudicationItems);
    }   

    //Cashless to check Initial Or Final
    private void handleCashlessCreditRequest(Map<String, String> response) throws IOException, MessagingException {
        
        String body = response.get("body");
        String[] bodyLines = body.split("\n");
        for (String line : bodyLines) {
            if (line.startsWith("Initial Cashless Approved Amount:-")) {
                handleInitialCashlessCreditRequest(response);
                return;
            } else if (line.startsWith("Final Cashless Approved Amount:-")) {
                handleFinalCashlessCreditRequest(response);
                return;
            }
        }
    
        // If neither condition matches, handle error case
        Map<String, Object> errorResponseMap = new HashMap<>();
        errorResponseMap.put("error", "Neither Initial nor Final Cashless Approved Amount found in body.");

    }

    //Initial Cashless Credit Request flow db calls
    private void handleInitialCashlessCreditRequest(Map<String, String> response) {
        String employeeCode = response.get("employee_code");
        String claimNo = response.get("claim_no");
        String initialCashlessApprovedAmount = response.get("initial_cashless_approved_amount");
        String initialCashlessRequestAmount = response.get("initial_cashless_request_amount");
        String subject = response.get("subject");
        String body = response.get("body");
        String gcpPath = response.get("gcp_path");
        String gcpFileName = response.get("gcp_file_name");

        Map<String, Object> preFundedReqMap = new HashMap<>();
        preFundedReqMap.put("user_id", "123");
        preFundedReqMap.put("hsp_id", "123");
        preFundedReqMap.put("partnered_user_id", employeeCode);
        preFundedReqMap.put("tpa_desk_id", null);
        preFundedReqMap.put("status", "PENDING");
        preFundedReqMap.put("type", "TPA");
        preFundedReqMap.put("processed_at", null);
        preFundedReqMap.put("requested_amount", null); //check wheather initial or final
        preFundedReqMap.put("disbursement_amount", null);
        preFundedReqMap.put("is_active", true);
        preFundedReqMap.put("claim_no", claimNo);
    
        Long preFundedRequest = (Long) this.loansService.preFundedrequestStore(preFundedReqMap);
    
        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put("type", "INITIAL CASHLESS CREDIT REQUEST");
        preFundedEmailerMap.put("subject", subject);
        preFundedEmailerMap.put("is_active", true);
        preFundedEmailerMap.put("claim_no", claimNo);
        preFundedEmailerMap.put("pf_req_id", preFundedRequest.toString());
    
        Long prefundedEmail = (Long) this.masterService.prefundedEmail(preFundedEmailerMap);
    
        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put("tpa_desk_id", null);
        emailerItems.put("claim_no", claimNo);
        emailerItems.put("initial_amt_req", initialCashlessRequestAmount);
        emailerItems.put("initial_amt_approved", initialCashlessApprovedAmount);
        emailerItems.put("final_adj_amt_req", null);
        emailerItems.put("final_adj_amt_approved", null);
        emailerItems.put("metadata", subject + body);
        emailerItems.put("patient_name", null);
    
        Long emailItems = (Long) this.masterService.emailInsert(emailerItems);

        Map<String, Object> adjudicationDataMap = new HashMap<>();
        adjudicationDataMap.put("tpa_desk_id",null);
        adjudicationDataMap.put("pf_req_id", prefundedEmail);
        adjudicationDataMap.put("requested_amount", null);
        adjudicationDataMap.put("estimated_amount", null);
        adjudicationDataMap.put("file_path", gcpPath);
        adjudicationDataMap.put("file_name", gcpFileName);
        adjudicationDataMap.put("user_id", null);
        adjudicationDataMap.put("offer_id", null);
        adjudicationDataMap.put("hsp_id",123);
        adjudicationDataMap.put("document_id", null); //document_id??
        adjudicationDataMap.put("associated_user_id", null);
        adjudicationDataMap.put("status", "PENDING");
        adjudicationDataMap.put("created_by", "TPA DESK");
        adjudicationDataMap.put("requested_by", null);

        Long adjudicationDataId = (Long)this.loansService.adjudicationDataStore(adjudicationDataMap);

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put("adjduciation_data_id",adjudicationDataId);
        adjudicationItems.put("pf_document_id",prefundedEmail);
        adjudicationItems.put("document_url", gcpPath);
        adjudicationItems.put("is_active", 1);

        Long adjudicationItemsId = (Long)this.loansService.adjudicationItemsStore(adjudicationItems);
    }

    //Final Cashless Credit Request flow db calls
    private void handleFinalCashlessCreditRequest(Map<String, String> response) {
        String employeeCode = response.get("employee_code");
        String claimNo = response.get("claim_no");
        String finalCashlessApprovedAmount = response.get("final_cashless_approved_amount");
        String finalCashlessRequestAmount = response.get("final_cashless_request_amount");
        String subject = response.get("subject");
        String body = response.get("body");
        String gcpPath = response.get("gcp_path");
        String gcpFileName = response.get("gcp_file_name"); 
    
        Map<String, Object> preFundedReqMap = new HashMap<>();
        preFundedReqMap.put("user_id", "123");
        preFundedReqMap.put("hsp_id", "123");
        preFundedReqMap.put("partnered_user_id", employeeCode);
        preFundedReqMap.put("tpa_desk_id", null);
        preFundedReqMap.put("status", "PENDING");
        preFundedReqMap.put("type", "TPA");
        preFundedReqMap.put("processed_at", null);
        preFundedReqMap.put("requested_amount", null); //check wheather initial or final
        preFundedReqMap.put("disbursement_amount", null);
        preFundedReqMap.put("is_active", true);
        preFundedReqMap.put("claim_no", claimNo);
    
        Long preFundedRequest = (Long) this.loansService.preFundedrequestStore(preFundedReqMap);
    
        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put("type", "FINAL CASHLESS CREDIT REQUEST");
        preFundedEmailerMap.put("subject", subject);
        preFundedEmailerMap.put("is_active", true);
        preFundedEmailerMap.put("claim_no", claimNo);
        preFundedEmailerMap.put("pf_req_id", preFundedRequest.toString());
    
        Long prefundedEmail = (Long) this.masterService.prefundedEmail(preFundedEmailerMap);
    
        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put("tpa_desk_id", null);
        emailerItems.put("claim_no", claimNo);
        emailerItems.put("initial_amt_req", null);
        emailerItems.put("initial_amt_approved", null);
        emailerItems.put("final_adj_amt_req", finalCashlessRequestAmount);
        emailerItems.put("final_adj_amt_approved", finalCashlessApprovedAmount);
        emailerItems.put("metadata", subject + body);
        emailerItems.put("patient_name", null);
    
        Long emailItems = (Long) this.masterService.emailInsert(emailerItems);
    
        Map<String, Object> adjudicationDataMap = new HashMap<>();
        adjudicationDataMap.put("tpa_desk_id",null);
        adjudicationDataMap.put("pf_req_id", prefundedEmail);
        adjudicationDataMap.put("requested_amount", finalCashlessRequestAmount);
        adjudicationDataMap.put("estimated_amount", finalCashlessApprovedAmount);
        adjudicationDataMap.put("file_path", gcpPath);
        adjudicationDataMap.put("file_name", gcpFileName);
        adjudicationDataMap.put("user_id", null);
        adjudicationDataMap.put("offer_id", null);
        adjudicationDataMap.put("hsp_id",123);
        adjudicationDataMap.put("document_id", null);
        adjudicationDataMap.put("associated_user_id", null);
        adjudicationDataMap.put("status", "PENDING");
        adjudicationDataMap.put("created_by", "TPA DESK");
        adjudicationDataMap.put("requested_by", null);

        Long adjudicationDataId = (Long)this.loansService.adjudicationDataStore(adjudicationDataMap);

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put("adjduciation_data_id",adjudicationDataId);
        adjudicationItems.put("pf_document_id",prefundedEmail);
        adjudicationItems.put("document_url", gcpPath);
        adjudicationItems.put("is_active", 1);

        Long adjudicationItemsId = (Long)this.loansService.adjudicationItemsStore(adjudicationItems);
    }

    //Final Bill And Discharge Summary flow db calls
    private void handleFinalBillAndDischargeSummary(Map<String, String> response) {
        String patientName = response.get("patient_name");
        String claimNo = response.get("claim_no");
        String khId = response.get("kh_id");
        String subject = response.get("subject");
        String body = response.get("body");
        String gcpPath = response.get("gcp_path");
        String gcpFileName = response.get("gcp_file_name"); 
    
        Map<String, Object> preFundedReqMap = new HashMap<>();
        preFundedReqMap.put("user_id", "123");
        preFundedReqMap.put("hsp_id", "123");
        preFundedReqMap.put("partnered_user_id", null);
        preFundedReqMap.put("tpa_desk_id", khId);
        preFundedReqMap.put("status", "PENDING");
        preFundedReqMap.put("type", "TPA");
        preFundedReqMap.put("processed_at", null);
        preFundedReqMap.put("requested_amount", null);
        preFundedReqMap.put("disbursement_amount", null);
        preFundedReqMap.put("is_active", true);
        preFundedReqMap.put("claim_no", claimNo);
    
        Long preFundedRequest = (Long) this.loansService.preFundedrequestStore(preFundedReqMap);
    
        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put("type", "FINAL BILL AND DISCHARGE SUMMARY");
        preFundedEmailerMap.put("subject", subject);
        preFundedEmailerMap.put("is_active", true);
        preFundedEmailerMap.put("claim_no", claimNo);
        preFundedEmailerMap.put("pf_req_id", preFundedRequest.toString());
    
        Long prefundedEmail = (Long) this.masterService.prefundedEmail(preFundedEmailerMap);
    
        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put("tpa_desk_id", khId);
        emailerItems.put("claim_no", claimNo);
        emailerItems.put("initial_amt_req", null);
        emailerItems.put("initial_amt_approved", null);
        emailerItems.put("final_adj_amt_req", null);
        emailerItems.put("final_adj_amt_approved", null);
        emailerItems.put("metadata", subject + body);
        emailerItems.put("patient_name", patientName);
    
        Long emailItems = (Long) this.masterService.emailInsert(emailerItems);
    
        Map<String, Object> adjudicationDataMap = new HashMap<>();
        adjudicationDataMap.put("tpa_desk_id",khId);
        adjudicationDataMap.put("pf_req_id", prefundedEmail);
        adjudicationDataMap.put("requested_amount", null);
        adjudicationDataMap.put("estimated_amount", null);
        adjudicationDataMap.put("file_path", gcpPath);
        adjudicationDataMap.put("file_name", gcpFileName);
        adjudicationDataMap.put("user_id", null);
        adjudicationDataMap.put("offer_id", null);
        adjudicationDataMap.put("hsp_id",123);
        adjudicationDataMap.put("document_id", null);
        adjudicationDataMap.put("associated_user_id", null);
        adjudicationDataMap.put("status", "PENDING");
        adjudicationDataMap.put("created_by", "TPA DESK");
        adjudicationDataMap.put("requested_by", null);

        Long adjudicationDataId = (Long)this.loansService.adjudicationDataStore(adjudicationDataMap);

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put("adjduciation_data_id",adjudicationDataId);
        adjudicationItems.put("pf_document_id",prefundedEmail);
        adjudicationItems.put("document_url", gcpPath);
        adjudicationItems.put("is_active", 1);

        Long adjudicationItemsId = (Long)this.loansService.adjudicationItemsStore(adjudicationItems);
    }

    //Additional Information flow db calls
    private String handleAdditionalInformation(Map<String, String> response) {

        return null;

        // String employeeCode = response.get("employee_code");
        // String claimNo = response.get("claim_no");
        // String documentRequired = response.get("document_required");
        // String patientName = response.get("patient_name");
        // String subject = response.get("subject");
        // String body = response.get("body");
        // String gcpPath = response.get("gcp_path");
        // String gcpFileName = response.get("gcp_file_name"); 
    
        // Map<String, Object> preFundedReqMap = new HashMap<>();
        // preFundedReqMap.put("user_id", "123");
        // preFundedReqMap.put("hsp_id", "123");
        // preFundedReqMap.put("partnered_user_id", employeeCode);
        // preFundedReqMap.put("tpa_desk_id", null);
        // preFundedReqMap.put("status", "PENDING");
        // preFundedReqMap.put("type", "TPA");
        // preFundedReqMap.put("processed_at", null);
        // preFundedReqMap.put("requested_amount", null);
        // preFundedReqMap.put("disbursement_amount", null);
        // preFundedReqMap.put("is_active", true);
        // preFundedReqMap.put("claim_no", claimNo);
    
        // Long preFundedRequest = (Long) this.loansService.preFundedrequestStore(preFundedReqMap);
    
        // Map<String, Object> preFundedEmailerMap = new HashMap<>();
        // preFundedEmailerMap.put("type", "ADDITIONAL INFORMATION");
        // preFundedEmailerMap.put("subject", subject);
        // preFundedEmailerMap.put("is_active", true);
        // preFundedEmailerMap.put("claim_no", claimNo);
        // preFundedEmailerMap.put("pf_req_id", preFundedRequest.toString());
    
        // Long prefundedEmail = (Long) this.masterService.prefundedEmail(preFundedEmailerMap);
    
        // Map<String, Object> emailerItems = new HashMap<>();
        // emailerItems.put("tpa_desk_id", null);
        // emailerItems.put("claim_no", claimNo);
        // emailerItems.put("initial_amt_req", null);
        // emailerItems.put("initial_amt_approved", null);
        // emailerItems.put("final_adj_amt_req", null);
        // emailerItems.put("final_adj_amt_approved", null);
        // emailerItems.put("metadata", subject + body);
        // emailerItems.put("patient_name", patientName);
    
        // Long emailItems = (Long) this.masterService.emailInsert(emailerItems);
    
        // Map<String, Object> adjudicationDataMap = new HashMap<>();
        // adjudicationDataMap.put("tpa_desk_id",null);
        // adjudicationDataMap.put("pf_req_id", prefundedEmail);
        // adjudicationDataMap.put("requested_amount", null);
        // adjudicationDataMap.put("estimated_amount", null);
        // adjudicationDataMap.put("file_path", gcpPath);
        // adjudicationDataMap.put("file_name", gcpFileName);
        // adjudicationDataMap.put("user_id", null);
        // adjudicationDataMap.put("offer_id", null);
        // adjudicationDataMap.put("hsp_id",123);
        // adjudicationDataMap.put("document_id", null);
        // adjudicationDataMap.put("associated_user_id", null);
        // adjudicationDataMap.put("status", "PENDING");
        // adjudicationDataMap.put("created_by", "TPA DESK");
        // adjudicationDataMap.put("requested_by", null);

        // Long adjudicationDataId = (Long)this.loansService.adjudicationDataStore(adjudicationDataMap);

        // Map<String, Object> adjudicationItems = new HashMap<>();
        // adjudicationItems.put("adjduciation_data_id",adjudicationDataId);
        // adjudicationItems.put("pf_document_id",prefundedEmail);
        // adjudicationItems.put("document_url", gcpPath);
        // adjudicationItems.put("is_active", 1);

       // Long adjudicationItemsId = (Long)this.loansService.adjudicationItemsStore(adjudicationItems);
    }
}

// @POST
// @Path("/emailDataStore")
// @Consumes(MediaType.APPLICATION_JSON)
// public Response emailDataStore(@Context HttpServletRequest request,
// Map<String, String> requestBody) {
// try {
// //ApiResponse<Object> dApiResponse =
// masterService.mailDataStore(requestBody);
// return Response.status(Response.Status.OK)
// .entity(new ApiResponse<>(true, "Data stored successfully", null))
// .build();
// } catch (Exception e) {
// e.printStackTrace();
// return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
// .entity(new ApiResponse<>(false, "Error storing data", null))
// .build();
// }
// }