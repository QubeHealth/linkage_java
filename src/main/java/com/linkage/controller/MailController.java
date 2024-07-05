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
import com.linkage.core.constants.Constants.EmailKeywords;

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
        String type = response.get(EmailKeywords.TYPE);

        // Based on the type, call different db functions using if-else conditions
        if (EmailKeywords.PRE_AUTH.equalsIgnoreCase(type)) {
            handlePreAuth(response);
        } else if (EmailKeywords.QUERY_REPLY.equalsIgnoreCase(type)) {
            handleQueryReply(response);
        } else if (EmailKeywords.FINAL_BILL_AND_DISCHARGE_SUMMARY.equalsIgnoreCase(type)) {
            handleFinalBillAndDischargeSummary(response);
        } else if (EmailKeywords.CASHLESS_CREDIT_REQUEST.equalsIgnoreCase(type)) {
            handleCashlessCreditRequest(response);
        } else if (EmailKeywords.ADDITIONAL_INFORMATION.equalsIgnoreCase(type)) {
            handleAdditionalInformation(response);
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity("Unknown type: " + type).build();
        }

        return Response.ok().entity("Processed successfully").build();
    }

    //Pre-Auth flow db calls
    private void handlePreAuth(Map<String, String> response) {

        String partneredUserId = response.get(EmailKeywords.POLICY_NO);
        String khId = response.get(EmailKeywords.TPA_DESK_ID);
        String subject = response.get(EmailKeywords.SUBJECT);
        String patientName = response.get(EmailKeywords.PATIENT_NAME);
        String gcpPath = response.get(EmailKeywords.GCP_PATH);
        String gcpFileName = response.get(EmailKeywords.GCP_FILE_NAME);

        Map<String, Object> preFundedReqMap = new HashMap<>();
        preFundedReqMap.put(EmailKeywords.USER_ID,"123");
        preFundedReqMap.put("hsp_id","123");
        preFundedReqMap.put("partnered_user_id",partneredUserId);
        preFundedReqMap.put(EmailKeywords.TPA_DESK_ID,khId);
        preFundedReqMap.put("status","PENDING");
        preFundedReqMap.put(EmailKeywords.TYPE,"TPA");
        preFundedReqMap.put("processed_at",null);
        preFundedReqMap.put("requested_amount",null);
        preFundedReqMap.put("disbursement_amount",null);
        preFundedReqMap.put(EmailKeywords.IS_ACTIVE,true);
        preFundedReqMap.put(EmailKeywords.CLAIM_NO,null);
        preFundedReqMap.put("claim_id", null);
        preFundedReqMap.put("approved_amount_initial",null);
        preFundedReqMap.put("approved_amount_final", null);
        preFundedReqMap.put("initial_request_resolved_at", null);
        preFundedReqMap.put("final_request_resolved_at", null);

        Integer preFundedRequestId = this.loansService.preFundedrequestStore(preFundedReqMap);
       
        Map<String,Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put(EmailKeywords.TYPE,"PRE AUTH");
        preFundedEmailerMap.put(EmailKeywords.SUBJECT,subject);
        preFundedEmailerMap.put(EmailKeywords.IS_ACTIVE,true);
        preFundedEmailerMap.put(EmailKeywords.CLAIM_NO,null);
        preFundedEmailerMap.put("pf_request_id", preFundedRequestId);

        Long prefundedEmail = (Long)this.masterService.prefundedEmail(preFundedEmailerMap);

        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put(EmailKeywords.TPA_DESK_ID,khId);
        emailerItems.put(EmailKeywords.CLAIM_NO, null);
        emailerItems.put("initial_amt_req",null);
        emailerItems.put("initial_amt_approved",null);
        emailerItems.put("final_adj_amt_req", null);
        emailerItems.put("final_adj_amt_approved",null);
        emailerItems.put("metadata",subject);
        emailerItems.put(EmailKeywords.PATIENT_NAME,patientName);

        Long emailItems = (Long)this.masterService.emailInsert(emailerItems);

        Map<String, Object> adjudicationDataMap = new HashMap<>();
        adjudicationDataMap.put(EmailKeywords.TPA_DESK_ID,khId);
        adjudicationDataMap.put("pf_req_id", prefundedEmail);
        adjudicationDataMap.put("requested_amount", null);
        adjudicationDataMap.put("estimated_amount", null);
        adjudicationDataMap.put("file_path", gcpPath);
        adjudicationDataMap.put("file_name", gcpFileName);
        adjudicationDataMap.put(EmailKeywords.USER_ID, partneredUserId);
        adjudicationDataMap.put("offer_id", null);
        adjudicationDataMap.put("hsp_id",123);
        adjudicationDataMap.put("document_id", null);
        adjudicationDataMap.put("associated_user_id", null);
        adjudicationDataMap.put("status", "PENDING");
        adjudicationDataMap.put("created_by", "TPA DESK");
        adjudicationDataMap.put("requested_by", null);

        Long adjudicationDataId = (Long)this.loansService.adjudicationDataStore(adjudicationDataMap);

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put("adjudication_data_id",adjudicationDataId);
        adjudicationItems.put("pf_document_id",prefundedEmail);
        adjudicationItems.put("document_url", gcpPath);
        adjudicationItems.put(EmailKeywords.IS_ACTIVE, 1);

        Long adjudicationItemsId = (Long)this.loansService.adjudicationItemsStore(adjudicationItems);

    }
    
    //Query reply flow db calls
    private void handleQueryReply(Map<String, String> response) {
        
        String claimNo = response.get(EmailKeywords.CLAIM_NO);
        String khId = response.get(EmailKeywords.TPA_DESK_ID);
        String subject = response.get(EmailKeywords.SUBJECT);
        String patientName = response.get(EmailKeywords.PATIENT_NAME);
        String body = response.get(EmailKeywords.BODY);
        String gcpPath = response.get(EmailKeywords.GCP_PATH);
        String gcpFileName = response.get(EmailKeywords.GCP_FILE_NAME);


        Map<String, Object> preFundedReqMap = new HashMap<>();
        preFundedReqMap.put(EmailKeywords.USER_ID,"123");
        preFundedReqMap.put("hsp_id","123");
        preFundedReqMap.put("partnered_user_id",null);
        preFundedReqMap.put(EmailKeywords.TPA_DESK_ID,khId);
        preFundedReqMap.put("status","PENDING");
        preFundedReqMap.put(EmailKeywords.TYPE,"TPA");
        preFundedReqMap.put("processed_at",null);
        preFundedReqMap.put("requested_amount",null);
        preFundedReqMap.put("disbursement_amount",null);
        preFundedReqMap.put(EmailKeywords.IS_ACTIVE,true);
        preFundedReqMap.put(EmailKeywords.CLAIM_NO,claimNo);
        preFundedReqMap.put("claim_id", null);
        preFundedReqMap.put("approved_amount_initial",null);
        preFundedReqMap.put("approved_amount_final", null);
        preFundedReqMap.put("initial_request_resolved_at", null);
        preFundedReqMap.put("final_request_resolved_at", null);

        Long preFundedRequest = (Long)this.loansService.preFundedrequestStore(preFundedReqMap);
       
        Map<String,Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put(EmailKeywords.TYPE,"QUERY REPLY");
        preFundedEmailerMap.put(EmailKeywords.SUBJECT,subject);
        preFundedEmailerMap.put(EmailKeywords.IS_ACTIVE,true);
        preFundedEmailerMap.put(EmailKeywords.CLAIM_NO,claimNo);
        preFundedEmailerMap.put("pf_req_id", preFundedRequest.toString());

        Long prefundedEmail = (Long)this.masterService.prefundedEmail(preFundedEmailerMap);

        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put(EmailKeywords.TPA_DESK_ID,khId);
        emailerItems.put(EmailKeywords.CLAIM_NO, claimNo);
        emailerItems.put("initial_amt_req",null);
        emailerItems.put("initial_amt_approved",null);
        emailerItems.put("final_adj_amt_req", null);
        emailerItems.put("final_adj_amt_approved",null);
        emailerItems.put("metadata",subject + body);
        emailerItems.put(EmailKeywords.PATIENT_NAME,patientName);

        Long emailItems = (Long)this.masterService.emailInsert(emailerItems);

        Map<String, Object> adjudicationDataMap = new HashMap<>();
        adjudicationDataMap.put(EmailKeywords.TPA_DESK_ID,khId);
        adjudicationDataMap.put("pf_req_id", prefundedEmail);
        adjudicationDataMap.put("requested_amount", null);
        adjudicationDataMap.put("estimated_amount", null);
        adjudicationDataMap.put("file_path", gcpPath);
        adjudicationDataMap.put("file_name", gcpFileName);
        adjudicationDataMap.put(EmailKeywords.USER_ID, null);
        adjudicationDataMap.put("offer_id", null);
        adjudicationDataMap.put("hsp_id",123);
        adjudicationDataMap.put("document_id", null);
        adjudicationDataMap.put("associated_user_id", null);
        adjudicationDataMap.put("status", "PENDING");
        adjudicationDataMap.put("created_by", "TPA DESK");
        adjudicationDataMap.put("requested_by", null);

        Long adjudicationDataId = (Long)this.loansService.adjudicationDataStore(adjudicationDataMap);

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put("adjudication_data_id",adjudicationDataId);
        adjudicationItems.put("pf_document_id",prefundedEmail);
        adjudicationItems.put("document_url", gcpPath);
        adjudicationItems.put(EmailKeywords.IS_ACTIVE, 1);

        Long adjudicationItemsId = (Long)this.loansService.adjudicationItemsStore(adjudicationItems);
    }   

    //Cashless to check Initial Or Final
    private void handleCashlessCreditRequest(Map<String, String> response) throws IOException, MessagingException {
        
        String body = response.get(EmailKeywords.BODY);
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
        String employeeCode = response.get(EmailKeywords.EMPLOYEE_CODE);
        String claimNo = response.get(EmailKeywords.CLAIM_NO);
        String initialCashlessApprovedAmount = response.get(EmailKeywords.INITIAL_CASHLESS_APPROVED_AMT);
        String initialCashlessRequestAmount = response.get(EmailKeywords.INITIAL_CASHLESS_REQUEST_AMT);
        String subject = response.get(EmailKeywords.SUBJECT);
        String body = response.get(EmailKeywords.BODY);
        String gcpPath = response.get(EmailKeywords.GCP_PATH);
        String gcpFileName = response.get(EmailKeywords.GCP_FILE_NAME);

        Map<String, Object> preFundedReqMap = new HashMap<>();
        preFundedReqMap.put(EmailKeywords.USER_ID, "123");
        preFundedReqMap.put("hsp_id", "123");
        preFundedReqMap.put("partnered_user_id", employeeCode);
        preFundedReqMap.put(EmailKeywords.TPA_DESK_ID, null);
        preFundedReqMap.put("status", "PENDING");
        preFundedReqMap.put(EmailKeywords.TYPE, "TPA");
        preFundedReqMap.put("processed_at", null);
        preFundedReqMap.put("requested_amount", null); //check wheather initial or final
        preFundedReqMap.put("disbursement_amount", null);
        preFundedReqMap.put(EmailKeywords.IS_ACTIVE, true);
        preFundedReqMap.put(EmailKeywords.CLAIM_NO, claimNo);
        preFundedReqMap.put("claim_id", null);
        preFundedReqMap.put("approved_amount_initial",initialCashlessApprovedAmount);
        preFundedReqMap.put("approved_amount_final", null);
        preFundedReqMap.put("initial_request_resolved_at", null);
        preFundedReqMap.put("final_request_resolved_at", null);
    
        Long preFundedRequest = (Long) this.loansService.preFundedrequestStore(preFundedReqMap);
    
        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put(EmailKeywords.TYPE, "INITIAL CASHLESS CREDIT REQUEST");
        preFundedEmailerMap.put(EmailKeywords.SUBJECT, subject);
        preFundedEmailerMap.put(EmailKeywords.IS_ACTIVE, true);
        preFundedEmailerMap.put(EmailKeywords.CLAIM_NO, claimNo);
        preFundedEmailerMap.put("pf_req_id", preFundedRequest.toString());
    
        Long prefundedEmail = (Long) this.masterService.prefundedEmail(preFundedEmailerMap);
    
        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put(EmailKeywords.TPA_DESK_ID, null);
        emailerItems.put(EmailKeywords.CLAIM_NO, claimNo);
        emailerItems.put("initial_amt_req", initialCashlessRequestAmount);
        emailerItems.put("initial_amt_approved", initialCashlessApprovedAmount);
        emailerItems.put("final_adj_amt_req", null);
        emailerItems.put("final_adj_amt_approved", null);
        emailerItems.put("metadata", subject + body);
        emailerItems.put(EmailKeywords.PATIENT_NAME, null);
    
        Long emailItems = (Long) this.masterService.emailInsert(emailerItems);

        Map<String, Object> adjudicationDataMap = new HashMap<>();
        adjudicationDataMap.put(EmailKeywords.TPA_DESK_ID,null);
        adjudicationDataMap.put("pf_req_id", prefundedEmail);
        adjudicationDataMap.put("requested_amount", null);
        adjudicationDataMap.put("estimated_amount", null);
        adjudicationDataMap.put("file_path", gcpPath);
        adjudicationDataMap.put("file_name", gcpFileName);
        adjudicationDataMap.put(EmailKeywords.USER_ID, null);
        adjudicationDataMap.put("offer_id", null);
        adjudicationDataMap.put("hsp_id",123);
        adjudicationDataMap.put("document_id", null); //document_id??
        adjudicationDataMap.put("associated_user_id", null);
        adjudicationDataMap.put("status", "PENDING");
        adjudicationDataMap.put("created_by", "TPA DESK");
        adjudicationDataMap.put("requested_by", null);

        Long adjudicationDataId = (Long)this.loansService.adjudicationDataStore(adjudicationDataMap);

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put("adjudication_data_id",adjudicationDataId);
        adjudicationItems.put("pf_document_id",prefundedEmail);
        adjudicationItems.put("document_url", gcpPath);
        adjudicationItems.put(EmailKeywords.IS_ACTIVE, 1);

        Long adjudicationItemsId = (Long)this.loansService.adjudicationItemsStore(adjudicationItems);
    }

    //Final Cashless Credit Request flow db calls
    private void handleFinalCashlessCreditRequest(Map<String, String> response) {
        String employeeCode = response.get(EmailKeywords.EMPLOYEE_CODE);
        String claimNo = response.get(EmailKeywords.CLAIM_NO);
        String finalCashlessApprovedAmount = response.get(EmailKeywords.INITIAL_CASHLESS_REQUEST_AMT);
        String finalCashlessRequestAmount = response.get(EmailKeywords.FINAL_CASHLESS_REQUEST_AMT);
        String subject = response.get(EmailKeywords.SUBJECT);
        String body = response.get(EmailKeywords.BODY);
        String gcpPath = response.get(EmailKeywords.GCP_PATH);
        String gcpFileName = response.get(EmailKeywords.GCP_FILE_NAME); 
    
        Map<String, Object> preFundedReqMap = new HashMap<>();
        preFundedReqMap.put(EmailKeywords.USER_ID, "123");
        preFundedReqMap.put("hsp_id", "123");
        preFundedReqMap.put("partnered_user_id", employeeCode);
        preFundedReqMap.put(EmailKeywords.TPA_DESK_ID, null);
        preFundedReqMap.put("status", "PENDING");
        preFundedReqMap.put(EmailKeywords.TYPE, "TPA");
        preFundedReqMap.put("processed_at", null);
        preFundedReqMap.put("requested_amount", null); //check wheather initial or final
        preFundedReqMap.put("disbursement_amount", null);
        preFundedReqMap.put(EmailKeywords.IS_ACTIVE, true);
        preFundedReqMap.put(EmailKeywords.CLAIM_NO, claimNo);
        preFundedReqMap.put("claim_id", null);
        preFundedReqMap.put("approved_amount_initial",null);
        preFundedReqMap.put("approved_amount_final", finalCashlessApprovedAmount);
        preFundedReqMap.put("initial_request_resolved_at", null);
        preFundedReqMap.put("final_request_resolved_at", null);
    
        Long preFundedRequest = (Long) this.loansService.preFundedrequestStore(preFundedReqMap);
    
        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put(EmailKeywords.TYPE, "FINAL CASHLESS CREDIT REQUEST");
        preFundedEmailerMap.put(EmailKeywords.SUBJECT, subject);
        preFundedEmailerMap.put(EmailKeywords.IS_ACTIVE, true);
        preFundedEmailerMap.put(EmailKeywords.CLAIM_NO, claimNo);
        preFundedEmailerMap.put("pf_req_id", preFundedRequest.toString());
    
        Long prefundedEmail = (Long) this.masterService.prefundedEmail(preFundedEmailerMap);
    
        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put(EmailKeywords.TPA_DESK_ID, null);
        emailerItems.put(EmailKeywords.CLAIM_NO, claimNo);
        emailerItems.put("initial_amt_req", null);
        emailerItems.put("initial_amt_approved", null);
        emailerItems.put("final_adj_amt_req", finalCashlessRequestAmount);
        emailerItems.put("final_adj_amt_approved", finalCashlessApprovedAmount);
        emailerItems.put("metadata", subject + body);
        emailerItems.put(EmailKeywords.PATIENT_NAME, null);
    
        Long emailItems = (Long) this.masterService.emailInsert(emailerItems);
    
        Map<String, Object> adjudicationDataMap = new HashMap<>();
        adjudicationDataMap.put(EmailKeywords.TPA_DESK_ID,null);
        adjudicationDataMap.put("pf_req_id", prefundedEmail);
        adjudicationDataMap.put("requested_amount", finalCashlessRequestAmount);
        adjudicationDataMap.put("estimated_amount", finalCashlessApprovedAmount);
        adjudicationDataMap.put("file_path", gcpPath);
        adjudicationDataMap.put("file_name", gcpFileName);
        adjudicationDataMap.put(EmailKeywords.USER_ID, null);
        adjudicationDataMap.put("offer_id", null);
        adjudicationDataMap.put("hsp_id",123);
        adjudicationDataMap.put("document_id", null);
        adjudicationDataMap.put("associated_user_id", null);
        adjudicationDataMap.put("status", "PENDING");
        adjudicationDataMap.put("created_by", "TPA DESK");
        adjudicationDataMap.put("requested_by", null);

        Long adjudicationDataId = (Long)this.loansService.adjudicationDataStore(adjudicationDataMap);

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put("adjudication_data_id",adjudicationDataId);
        adjudicationItems.put("pf_document_id",prefundedEmail);
        adjudicationItems.put("document_url", gcpPath);
        adjudicationItems.put(EmailKeywords.IS_ACTIVE, 1);

        Long adjudicationItemsId = (Long)this.loansService.adjudicationItemsStore(adjudicationItems);
    }

    //Final Bill And Discharge Summary flow db calls
    private void handleFinalBillAndDischargeSummary(Map<String, String> response) {
        String patientName = response.get(EmailKeywords.PATIENT_NAME);
        String claimNo = response.get(EmailKeywords.CLAIM_NO);
        String khId = response.get(EmailKeywords.TPA_DESK_ID);
        String subject = response.get(EmailKeywords.SUBJECT);
        String body = response.get(EmailKeywords.BODY);
        String gcpPath = response.get(EmailKeywords.GCP_PATH);
        String gcpFileName = response.get(EmailKeywords.GCP_FILE_NAME); 
    
        Map<String, Object> preFundedReqMap = new HashMap<>();
        preFundedReqMap.put(EmailKeywords.USER_ID, "123");
        preFundedReqMap.put("hsp_id", "123");
        preFundedReqMap.put("partnered_user_id", null);
        preFundedReqMap.put(EmailKeywords.TPA_DESK_ID, khId);
        preFundedReqMap.put("status", "PENDING");
        preFundedReqMap.put(EmailKeywords.TYPE, "TPA");
        preFundedReqMap.put("processed_at", null);
        preFundedReqMap.put("requested_amount", null);
        preFundedReqMap.put("disbursement_amount", null);
        preFundedReqMap.put(EmailKeywords.IS_ACTIVE, true);
        preFundedReqMap.put(EmailKeywords.CLAIM_NO, claimNo);
        preFundedReqMap.put("claim_id", null);
        preFundedReqMap.put("approved_amount_initial",null);
        preFundedReqMap.put("approved_amount_final", null);
        preFundedReqMap.put("initial_request_resolved_at", null);
        preFundedReqMap.put("final_request_resolved_at", null);

        Long preFundedRequest = (Long) this.loansService.preFundedrequestStore(preFundedReqMap);
    
        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put(EmailKeywords.TYPE, "FINAL BILL AND DISCHARGE SUMMARY");
        preFundedEmailerMap.put(EmailKeywords.SUBJECT, subject);
        preFundedEmailerMap.put(EmailKeywords.IS_ACTIVE, true);
        preFundedEmailerMap.put(EmailKeywords.CLAIM_NO, claimNo);
        preFundedEmailerMap.put("pf_req_id", preFundedRequest.toString());
    
        Long prefundedEmail = (Long) this.masterService.prefundedEmail(preFundedEmailerMap);
    
        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put(EmailKeywords.TPA_DESK_ID, khId);
        emailerItems.put(EmailKeywords.CLAIM_NO, claimNo);
        emailerItems.put("initial_amt_req", null);
        emailerItems.put("initial_amt_approved", null);
        emailerItems.put("final_adj_amt_req", null);
        emailerItems.put("final_adj_amt_approved", null);
        emailerItems.put("metadata", subject + body);
        emailerItems.put(EmailKeywords.PATIENT_NAME, patientName);
    
        Long emailItems = (Long) this.masterService.emailInsert(emailerItems);
    
        Map<String, Object> adjudicationDataMap = new HashMap<>();
        adjudicationDataMap.put(EmailKeywords.TPA_DESK_ID,khId);
        adjudicationDataMap.put("pf_req_id", prefundedEmail);
        adjudicationDataMap.put("requested_amount", null);
        adjudicationDataMap.put("estimated_amount", null);
        adjudicationDataMap.put("file_path", gcpPath);
        adjudicationDataMap.put("file_name", gcpFileName);
        adjudicationDataMap.put(EmailKeywords.USER_ID, null);
        adjudicationDataMap.put("offer_id", null);
        adjudicationDataMap.put("hsp_id",123);
        adjudicationDataMap.put("document_id", null);
        adjudicationDataMap.put("associated_user_id", null);
        adjudicationDataMap.put("status", "PENDING");
        adjudicationDataMap.put("created_by", "TPA DESK");
        adjudicationDataMap.put("requested_by", null);

        Long adjudicationDataId = (Long)this.loansService.adjudicationDataStore(adjudicationDataMap);

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put("adjudication_data_id",adjudicationDataId);
        adjudicationItems.put("pf_document_id",prefundedEmail);
        adjudicationItems.put("document_url", gcpPath);
        adjudicationItems.put(EmailKeywords.IS_ACTIVE, 1);

        Long adjudicationItemsId = (Long)this.loansService.adjudicationItemsStore(adjudicationItems);
    }

    //Additional Information flow db calls
    private void handleAdditionalInformation(Map<String, String> response) {

        //return null;

        String employeeCode = response.get(EmailKeywords.EMPLOYEE_CODE);
        String claimNo = response.get(EmailKeywords.CLAIM_NO);
        String documentRequired = response.get(EmailKeywords.DOCUMENT_REQUIRED);
        String patientName = response.get(EmailKeywords.PATIENT_NAME);
        String subject = response.get(EmailKeywords.SUBJECT);
        String body = response.get(EmailKeywords.BODY);
        String gcpPath = response.get(EmailKeywords.GCP_PATH);
        String gcpFileName = response.get(EmailKeywords.GCP_FILE_NAME); 
    
        Map<String, Object> preFundedReqMap = new HashMap<>();
        preFundedReqMap.put(EmailKeywords.USER_ID, "123");
        preFundedReqMap.put("hsp_id", "123");
        preFundedReqMap.put("partnered_user_id", employeeCode);
        preFundedReqMap.put(EmailKeywords.TPA_DESK_ID, null);
        preFundedReqMap.put("status", "PENDING");
        preFundedReqMap.put(EmailKeywords.TYPE, "TPA");
        preFundedReqMap.put("processed_at", null);
        preFundedReqMap.put("requested_amount", null);
        preFundedReqMap.put("disbursement_amount", null);
        preFundedReqMap.put(EmailKeywords.IS_ACTIVE, true);
        preFundedReqMap.put(EmailKeywords.CLAIM_NO, claimNo);
        preFundedReqMap.put("claim_id", null);
        preFundedReqMap.put("approved_amount_initial", null);
        preFundedReqMap.put("approved_amount_final", null);
        preFundedReqMap.put("initial_request_resolved_at", null);
        preFundedReqMap.put("final_request_resolved_at", null);  
    
        Long preFundedRequest = (Long) this.loansService.preFundedrequestStore(preFundedReqMap);
    
        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put(EmailKeywords.TYPE, "ADDITIONAL INFORMATION");
        preFundedEmailerMap.put(EmailKeywords.SUBJECT, subject);
        preFundedEmailerMap.put(EmailKeywords.IS_ACTIVE, true);
        preFundedEmailerMap.put(EmailKeywords.CLAIM_NO, claimNo);
        preFundedEmailerMap.put("pf_req_id", preFundedRequest.toString());
    
        Long prefundedEmail = (Long) this.masterService.prefundedEmail(preFundedEmailerMap);
    
        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put(EmailKeywords.TPA_DESK_ID, null);
        emailerItems.put(EmailKeywords.CLAIM_NO, claimNo);
        emailerItems.put("initial_amt_req", null);
        emailerItems.put("initial_amt_approved", null);
        emailerItems.put("final_adj_amt_req", null);
        emailerItems.put("final_adj_amt_approved", null);
        emailerItems.put("metadata", subject + body);
        emailerItems.put(EmailKeywords.PATIENT_NAME, patientName);
    
        Long emailItems = (Long) this.masterService.emailInsert(emailerItems);
    
        Map<String, Object> adjudicationDataMap = new HashMap<>();
        adjudicationDataMap.put(EmailKeywords.TPA_DESK_ID,null);
        adjudicationDataMap.put("pf_req_id", prefundedEmail);
        adjudicationDataMap.put("requested_amount", null);
        adjudicationDataMap.put("estimated_amount", null);
        adjudicationDataMap.put("file_path", gcpPath);
        adjudicationDataMap.put("file_name", gcpFileName);
        adjudicationDataMap.put(EmailKeywords.USER_ID, null);
        adjudicationDataMap.put("offer_id", null);
        adjudicationDataMap.put("hsp_id",123);
        adjudicationDataMap.put("document_id", null);
        adjudicationDataMap.put("associated_user_id", null);
        adjudicationDataMap.put("status", "PENDING");
        adjudicationDataMap.put("created_by", "TPA DESK");
        adjudicationDataMap.put("requested_by", null);

        Long adjudicationDataId = (Long)this.loansService.adjudicationDataStore(adjudicationDataMap);

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put("adjudication_data_id",adjudicationDataId);
        adjudicationItems.put("pf_document_id",prefundedEmail);
        adjudicationItems.put("document_url", gcpPath);
        adjudicationItems.put(EmailKeywords.IS_ACTIVE, 1);

       Long adjudicationItemsId = (Long)this.loansService.adjudicationItemsStore(adjudicationItems);

        Map<String, Object> adjudicationQuery =new HashMap<>();
        adjudicationQuery.put("adjudication_data_id", null);
        adjudicationQuery.put("remark", null);
        adjudicationQuery.put("document_url", gcpPath);
        adjudicationQuery.put(EmailKeywords.IS_ACTIVE, 1);
        adjudicationQuery.put("responded_at", null);
        adjudicationQuery.put("resolved_at", null);
        adjudicationQuery.put("status", null);

        Long adjudicationQueryId = (Long)this.loansService.adjudicationQueryStore(adjudicationQuery);
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