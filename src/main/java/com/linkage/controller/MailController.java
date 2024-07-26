package com.linkage.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.mail.Message;
import javax.mail.MessagingException;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.LoansService;
import com.linkage.client.MailReaderService;
import com.linkage.client.MailWriterService;
import com.linkage.client.MasterService;
import com.linkage.core.constants.Constants.EmailKeywords;
import com.linkage.core.constants.Constants.NotificationKeywords;
import com.linkage.utility.Helper;
import com.linkage.utility.sqs.ExecutionsConstants;
import com.linkage.utility.sqs.Producer;
import com.linkage.utility.sqs.QueueConstants;

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
    private MailWriterService mailWriterService;

    public MailController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        this.mailReaderService = new MailReaderService(null, null, null, null, configuration);
        this.masterService = new MasterService(configuration);
        this.loansService = new LoansService(configuration);
        this.mailWriterService = new MailWriterService(configuration);
    }

    @POST
    @Path("/markEmailUnread")
    @Consumes(MediaType.APPLICATION_JSON)
    public String markEmailUnread(@Context HttpServletRequest request, String subject) throws MessagingException {

        String response = this.mailWriterService.markEmailUnread(subject);

        return response;
    }

    @POST
    @Path("/emailReader")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response emailReader(@Context HttpServletRequest request) throws MessagingException, IOException {
        try {
            // Fetch and process the email
            List<Message> msgList = this.mailReaderService.fetchLatestEmail();

            for (Message message : msgList) {
                try {
                    Response processedMail = this.mailReaderService.fetchAndProcessEmail(message);
                    ApiResponse<Object> apiResponse = (ApiResponse<Object>) processedMail.getEntity();
                    if (processedMail.getStatus() == 0) {
                        // Log the error or handle as needed
                        throw new Exception("Error processing email");
                    } else {
                        System.out.println("Email fetch And Process Successfully");
                    }
                    Map<String, String> responseData = (Map<String, String>) apiResponse.getData();

                    // Extract common type from the response
                    String type = String.valueOf(responseData.get(EmailKeywords.TYPE));

                    // Map to store handlers for different types
                    Map<String, Runnable> emailType = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                    emailType.put(EmailKeywords.PRE_AUTH, () -> handlePreAuth(responseData));
                    emailType.put(EmailKeywords.QUERY_REPLY, () -> handleQueryReply(responseData));
                    emailType.put(EmailKeywords.FINAL_BILL_AND_DISCHARGE_SUMMARY,
                            () -> handleFinalBillAndDischargeSummary(responseData));
                    // emailType.put(EmailKeywords.CASHLESS_CREDIT_REQUEST,
                    //         () -> handleCashlessCreditRequest(responseData));
                    emailType.put("final cashless credit request",
                            () -> handleFinalCashlessCreditRequest(responseData));
                    emailType.put("initial cashless credit request",
                            () -> handleInitialCashlessCreditRequest(responseData));
                    emailType.put(EmailKeywords.ADDITIONAL_INFORMATION,
                            () -> handleAdditionalInformation(responseData));

                    // Execute handler based on the type
                    Runnable handler = emailType.get(type);
                    if (handler != null) {
                        handler.run();
                        this.mailWriterService.mailSender(message);
                        // return Response.ok().entity("mail Sender Response").build();
                    }
                } catch (Exception e) {
                    // Log the exception and continue with the next message
                    this.mailWriterService.markEmailUnread(message.getSubject());
                    // return
                    // Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error");

                }
            }
            return Response.ok().entity("All emails processed successfully.").build();

        } catch (Exception e) {
            // Handle any unexpected exceptions
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error processing emails: " + e.getMessage())
                    .build();
        }
    }

    // Pre-Auth flow db calls

    /**
     * first mail
     * sender - tpa desk
     * receiver - qube
     * 
     * @param response
     */
    private void handlePreAuth(Map<String, String> response) {

        String partneredUserId = response.get(EmailKeywords.POLICY_NO);
        String khId = response.get(EmailKeywords.TPA_DESK_ID);
        String subject = response.get(EmailKeywords.SUBJECT);
        String patientName = response.get(EmailKeywords.PATIENT_NAME);
        String gcpPath = response.get(EmailKeywords.GCP_PATH);
        String gcpFileName = response.get(EmailKeywords.GCP_FILE_NAME);
        //String uniqueId = UUID.randomUUID().toString();

        

        Map<String, Object> preFundedReqMap = new HashMap<>();
        preFundedReqMap.put(EmailKeywords.USER_ID, "123");
        preFundedReqMap.put("hsp_id", "123");
        preFundedReqMap.put("partnered_user_id", partneredUserId);
        preFundedReqMap.put(EmailKeywords.TPA_DESK_ID, khId);
        preFundedReqMap.put("status", "PENDING");
        preFundedReqMap.put(EmailKeywords.TYPE, "TPA");
        preFundedReqMap.put("processed_at", null);
        preFundedReqMap.put("requested_amount", null);
        preFundedReqMap.put("disbursement_amount", null);
        preFundedReqMap.put(EmailKeywords.IS_ACTIVE, true);
        preFundedReqMap.put(EmailKeywords.CLAIM_NO, null);
        preFundedReqMap.put("claim_id", null); /// can pass uniqueId
        preFundedReqMap.put("approved_amount_initial", null);
        preFundedReqMap.put("approved_amount_final", null);
        preFundedReqMap.put("initial_request_resolved_at", null);
        preFundedReqMap.put("final_request_resolved_at", null);


        ApiResponse<Object> preFundedRequest = this.loansService.preFundedrequestStore(preFundedReqMap);
        Map<String, Object> responseData = (Map<String, Object>) preFundedRequest.getData();
        String preFundedRequestId = String.valueOf(responseData.get("data"));
        if (preFundedRequest.getStatus() == false) {
            logger.error("preFundedRequest returned null response data");
        } else {
            logger.info("PreFunded Request db added at {}", preFundedRequestId);
        }

        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put(EmailKeywords.EMAIL_TYPE, "PRE AUTH");
        preFundedEmailerMap.put(EmailKeywords.SUBJECT, subject);
        preFundedEmailerMap.put(EmailKeywords.IS_ACTIVE, true);
        preFundedEmailerMap.put("partnered_claim_no", "22");
        preFundedEmailerMap.put("pf_request_id", preFundedRequestId);
        preFundedEmailerMap.put("policy_no", partneredUserId);

        // Long prefundedEmail =
        // (Long)this.masterService.prefundedEmail(preFundedEmailerMap);

        ApiResponse<Object> prefundedEmailRequest = this.masterService.prefundedEmail(preFundedEmailerMap);
        Map<String, Object> prefundedEmailResponseData = (Map<String, Object>) prefundedEmailRequest.getData();
        String prefundedEmailId = String.valueOf(prefundedEmailResponseData.get("data")); // "225";
        if (prefundedEmailRequest.getStatus() == false) {
            logger.error("prefundedEmailId returned null response data");
        } else {
            logger.info("prefunded Emailers db added at {}", prefundedEmailId);
        }

        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put(EmailKeywords.TPA_DESK_ID, khId);
        emailerItems.put("claim_no", null);
        emailerItems.put("initial_amt_req", null);
        emailerItems.put("initial_amt_approved", null);
        emailerItems.put("final_adj_amt_req", null);
        emailerItems.put("final_adj_amt_approved", null);
        emailerItems.put("metadata", subject);
        emailerItems.put(EmailKeywords.PATIENT_NAME, patientName);
        emailerItems.put("policy_no", partneredUserId);

        // Long emailItems = (Long)this.masterService.emailInsert(emailerItems);

        ApiResponse<Object> emailItemsRequest = this.masterService.emailInsert(emailerItems);
        Map<String, Object> emailItemsResponseData = (Map<String, Object>) emailItemsRequest.getData();
        String emailItems = String.valueOf(emailItemsResponseData.get("data"));
        if (emailItemsRequest.getStatus() == false) {
            logger.error("emailItems returned null response data");
        } else {
            logger.info("Response data emailItems {}", emailItems);
        }

        Map<String, Object> adjudicationDataMap = new HashMap<>();
        adjudicationDataMap.put(EmailKeywords.TPA_DESK_ID, khId);
        adjudicationDataMap.put("pf_req_id", preFundedRequestId);
        adjudicationDataMap.put("requested_amount", null);
        adjudicationDataMap.put("estimated_amount", null);
        adjudicationDataMap.put("file_path", gcpPath);
        adjudicationDataMap.put("file_name", gcpFileName);
        adjudicationDataMap.put(EmailKeywords.USER_ID, partneredUserId);
        adjudicationDataMap.put("offer_id", null);
        adjudicationDataMap.put("hsp_id", 123);
        adjudicationDataMap.put("document_id", null);
        adjudicationDataMap.put("associated_user_id", null);
        adjudicationDataMap.put("status", "PENDING");
        adjudicationDataMap.put("created_by", "TPA DESK");
        adjudicationDataMap.put("requested_by", null);
        adjudicationDataMap.put("updated_by", "ADJUDICATOR");

        ApiResponse<Object> adjudicationData = this.loansService.adjudicationDataStore(adjudicationDataMap);
        Map<String, Object> adjudicationResponseData = (Map<String, Object>) adjudicationData.getData();
        String adjudicationDataId = String.valueOf(adjudicationResponseData.get("data"));
        if (adjudicationData.getStatus() == false) {
            logger.error("adjudicationDataId returned null response data");
        } else {
            logger.info("Response data adjudicationDataId", adjudicationDataId);
        }

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put("adjudication_data_id", adjudicationDataId);
        adjudicationItems.put("pf_document_id", prefundedEmailId);
        adjudicationItems.put("document_url", gcpFileName);
        adjudicationItems.put(EmailKeywords.IS_ACTIVE, 1);

        // Long adjudicationItemsId =
        // (Long)this.loansService.adjudicationItemsStore(adjudicationItems);

        ApiResponse<Object> adjudicationItemsData = this.loansService.adjudicationItemsStore(adjudicationItems);
        Map<String, Object> adjudicationItemsDataResponseData = (Map<String, Object>) adjudicationItemsData.getData();
        String adjudicationItemsId = String.valueOf(adjudicationItemsDataResponseData.get("data"));
        if (adjudicationItemsData.getStatus() == false) {
            logger.error("adjudicationItemsId returned null response data");
        } else {
            logger.info("Response data adjudicationItemsId {}", adjudicationItemsId);
        }

        Map<String, Object> sendNotification = new HashMap<>();
        sendNotification.put("policy_no", partneredUserId);
        sendNotification.put(NotificationKeywords.TYPE, NotificationKeywords.PRE_AUTH_MESSAGE);
        sendNotification.put(NotificationKeywords.USER_ID, NotificationKeywords.USER_ID_VALUE);
        try {
            Producer.addInQueue(QueueConstants.LINKAGE.exchange, ExecutionsConstants.PREFUNDED_NOTIFICATON.key,Helper.convertMapToJsonString(sendNotification));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Reply to the query raised by Adjudicator
     * sender - Tpa desk
     * receiver - qube
     * 
     * @param response
     */
    // Query reply flow db calls
    private void handleQueryReply(Map<String, String> response) {

        String claimNo = response.get(EmailKeywords.CLAIM_NO);
        String khId = response.get(EmailKeywords.TPA_DESK_ID);
        String subject = response.get(EmailKeywords.SUBJECT);
        String patientName = response.get(EmailKeywords.PATIENT_NAME);
        String body = response.get(EmailKeywords.BODY);
        String gcpPath = response.get(EmailKeywords.GCP_PATH);
        String gcpFileName = response.get(EmailKeywords.GCP_FILE_NAME);
        String status = "RESPONDED";

        ApiResponse<Object> getPrefundedRequestIdRequest = this.loansService.getPrefundedRequestId(claimNo);
        Map<String, Object> prefundedIdResponseData = (Map<String, Object>) getPrefundedRequestIdRequest.getData();
        String prefundedRequestId = String.valueOf(prefundedIdResponseData.get("data"));
        if (prefundedRequestId == null) {
            logger.error("preFundedRequest returned null response data");
        } else {
            logger.info("Response data preFundedRequestId", prefundedRequestId);
        }

        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put(EmailKeywords.TYPE, "QUERY REPLY");
        preFundedEmailerMap.put(EmailKeywords.SUBJECT, subject);
        preFundedEmailerMap.put(EmailKeywords.IS_ACTIVE, true);
        preFundedEmailerMap.put("partnered_claim_no", claimNo);
        preFundedEmailerMap.put("pf_request_id", prefundedRequestId);
        preFundedEmailerMap.put("policy_no", null);

        // Long prefundedEmail =
        // (Long)this.masterService.prefundedEmail(preFundedEmailerMap);

        ApiResponse<Object> prefundedEmailRequest = this.masterService.prefundedEmail(preFundedEmailerMap);
        Map<String, Object> prefundedEmailResponseData = (Map<String, Object>) prefundedEmailRequest.getData();
        String prefundedEmailId = String.valueOf(prefundedEmailResponseData.get("data")); // "225";
        if (prefundedEmailId == null) {
            logger.error("prefundedEmailId returned null response data");
        } else {
            logger.info("Response data prefundedEmailId", prefundedEmailId);
        }

        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put(EmailKeywords.TPA_DESK_ID, khId);
        emailerItems.put("claim_no", claimNo);
        emailerItems.put("initial_amt_req", null);
        emailerItems.put("initial_amt_approved", null);
        emailerItems.put("final_adj_amt_req", null);
        emailerItems.put("final_adj_amt_approved", null);
        emailerItems.put("metadata", subject);
        emailerItems.put(EmailKeywords.PATIENT_NAME, patientName);
        emailerItems.put("policy_no", null);

        // Long emailItems = (Long)this.masterService.emailInsert(emailerItems);

        ApiResponse<Object> emailItemsRequest = this.masterService.emailInsert(emailerItems);
        Map<String, Object> emailItemsResponseData = (Map<String, Object>) emailItemsRequest.getData();
        String emailItems = String.valueOf(emailItemsResponseData.get("data"));
        if (emailItems == null) {
            logger.error("emailItems returned null response data");
        } else {
            logger.info("Response data emailItems", emailItems);
        }

        ApiResponse<Object> adjudicationDataRequest = this.loansService.getAdjudicationDataId(claimNo);
        Map<String, Object> adjudicationDataResponseData = (Map<String, Object>) adjudicationDataRequest.getData();
        String adjudicationDataId = String.valueOf(adjudicationDataResponseData.get("data"));
        if (adjudicationDataId == null) {
            logger.error("adjudicationDataId returned null response data");
        } else {
            logger.info("Response data adjudicationDataId", adjudicationDataId);
        }

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put("adjudication_data_id", adjudicationDataId);
        adjudicationItems.put("pf_document_id", prefundedEmailId);
        adjudicationItems.put("document_url", gcpFileName);
        adjudicationItems.put(EmailKeywords.IS_ACTIVE, 1);

        // Long adjudicationItemsId =
        // (Long)this.loansService.adjudicationItemsStore(adjudicationItems);

        ApiResponse<Object> adjudicationItemsData = this.loansService.adjudicationItemsStore(adjudicationItems);
        Map<String, Object> adjudicationItemsDataResponseData = (Map<String, Object>) adjudicationItemsData.getData();
        String adjudicationItemsId = String.valueOf(adjudicationItemsDataResponseData.get("data"));
        if (adjudicationItemsId == null) {
            logger.error("adjudicationItemsId returned null response data");
        } else {
            logger.info("Response data adjudicationItemsId", adjudicationItemsId);
        }

        ApiResponse<Object> updateStatus = this.loansService.handleQueryReply(claimNo, status);

        Map<String, Object> sendNotification = new HashMap<>();
        sendNotification.put("claim_no", claimNo);
        sendNotification.put(NotificationKeywords.TYPE, NotificationKeywords.REPLY_QUERY_ADJ);
        sendNotification.put(NotificationKeywords.USER_ID, NotificationKeywords.USER_ID_VALUE);
        try {
            Producer.addInQueue(QueueConstants.LINKAGE.exchange, ExecutionsConstants.PREFUNDED_NOTIFICATON.key,Helper.convertMapToJsonString(sendNotification));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    // // Cashless to check Initial Or Final
    // private void handleCashlessCreditRequest(Map<String, String> response) {

    //     String body = response.get(EmailKeywords.BODY);
    //     String[] bodyLines = body.split("\n");
    //     for (String line : bodyLines) {
    //         if (line.startsWith("Initial Cashless Approved Amount:-")) {
    //             handleInitialCashlessCreditRequest(response);
    //             return;
    //         } else if (line.startsWith("Final Cashless Approved Amount:-")) {
    //             handleFinalCashlessCreditRequest(response);
    //             return;
    //         }
    //     }

    //     // If neither condition matches, handle error case
    //     Map<String, Object> errorResponseMap = new HashMap<>();
    //     errorResponseMap.put("error", "Neither Initial nor Final Cashless Approved Amount found in body.");

    // }

    /**
     * Pre Auth Amount Approved by the Adjudicaotr
     * sender - Adjudicator
     * receiver - qube
     * 
     * @param response
     */
    // Initial Cashless Credit Request flow db calls
    private void handleInitialCashlessCreditRequest(Map<String, String> response) {
        String employeeCode = response.get(EmailKeywords.EMPLOYEE_CODE);
        String claimNo = response.get(EmailKeywords.CLAIM_NO);
        String initialApprovedAmount = response.get("initial_cashless_approved_amount");
        String initialRequestAmount = response.get("initial_cashless_request_amount");
        String subject = response.get(EmailKeywords.SUBJECT);
        String body = response.get(EmailKeywords.BODY);
        String gcpPath = response.get(EmailKeywords.GCP_PATH);
        String gcpFileName = response.get(EmailKeywords.GCP_FILE_NAME);
        String status = "APPROVED";

        ApiResponse<Object> getPrefundedRequestIdRequest = this.loansService.getPrefundedRequestId(claimNo);
        Map<String, Object> prefundedIdResponseData = (Map<String, Object>) getPrefundedRequestIdRequest.getData();
        String prefundedRequestId = String.valueOf(prefundedIdResponseData.get("data"));
        if (prefundedRequestId == null) {
            logger.error("prefundedRequest returned null response data");
        } else {
            logger.info("Response data prefundedRequestId", prefundedRequestId);
        }

        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put(EmailKeywords.TYPE, "CASHLESS CREDIT REQUEST");
        preFundedEmailerMap.put(EmailKeywords.SUBJECT, subject);
        preFundedEmailerMap.put(EmailKeywords.IS_ACTIVE, true);
        preFundedEmailerMap.put("partnered_claim_no", "22");
        preFundedEmailerMap.put("pf_request_id", prefundedRequestId);
        preFundedEmailerMap.put("policy_no", employeeCode);

        // Long prefundedEmail =
        // (Long)this.masterService.prefundedEmail(preFundedEmailerMap);

        ApiResponse<Object> prefundedEmailRequest = this.masterService.prefundedEmail(preFundedEmailerMap);
        Map<String, Object> prefundedEmailResponseData = (Map<String, Object>) prefundedEmailRequest.getData();
        String prefundedEmailId = String.valueOf(prefundedEmailResponseData.get("data")); // "225";
        if (prefundedEmailId == null) {
            logger.error("prefundedEmailId returned null response data");
        } else {
            logger.info("Response data prefundedEmailId", prefundedEmailId);
        }

        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put(EmailKeywords.TPA_DESK_ID, null);
        emailerItems.put("claim_no", claimNo);
        emailerItems.put("initial_amt_req", initialRequestAmount);
        emailerItems.put("initial_amt_approved", initialApprovedAmount);
        emailerItems.put("final_adj_amt_req", null);
        emailerItems.put("final_adj_amt_approved", null);
        emailerItems.put("metadata", subject + body);
        emailerItems.put(EmailKeywords.PATIENT_NAME, null);
        emailerItems.put("policy_no", employeeCode);

        // Long emailItems = (Long)this.masterService.emailInsert(emailerItems);

        ApiResponse<Object> emailItemsRequest = this.masterService.emailInsert(emailerItems);
        Map<String, Object> emailItemsResponseData = (Map<String, Object>) emailItemsRequest.getData();
        String emailItems = String.valueOf(emailItemsResponseData.get("data"));
        if (emailItems == null) {
            logger.error("emailItems returned null response data");
        } else {
            logger.info("Response data emailItems", emailItems);
        }

        ApiResponse<Object> adjudicationDataRequest = this.loansService.getAdjudicationDataId(claimNo);
        Map<String, Object> adjudicationDataResponseData = (Map<String, Object>) adjudicationDataRequest.getData();
        String adjudicationDataId = String.valueOf(adjudicationDataResponseData.get("data"));
        if (adjudicationDataId == null) {
            logger.error("adjudicationDataId returned null response data");
        } else {
            logger.info("Response data adjudicationDataId", adjudicationDataId);
        }

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put("adjudication_data_id", adjudicationDataId);
        adjudicationItems.put("pf_document_id", prefundedEmailId);
        adjudicationItems.put("document_url", gcpFileName);
        adjudicationItems.put(EmailKeywords.IS_ACTIVE, 1);

        // Long adjudicationItemsId =
        // (Long)this.loansService.adjudicationItemsStore(adjudicationItems);

        ApiResponse<Object> adjudicationItemsData = this.loansService.adjudicationItemsStore(adjudicationItems);
        Map<String, Object> adjudicationItemsDataResponseData = (Map<String, Object>) adjudicationItemsData.getData();
        String adjudicationItemsId = String.valueOf(adjudicationItemsDataResponseData.get("data"));
        if (adjudicationItemsId == null) {
            logger.error("adjudicationItemsId returned null response data");
        } else {
            logger.info("Response data adjudicationItemsId", adjudicationItemsId);
        }

        ApiResponse<Object> checkQueryStatusData = this.loansService.checkQueryStatus(claimNo);
        Map<String, Object> checkQueryStatusResponseData = (Map<String, Object>) checkQueryStatusData.getData();
        String checkStatus = String.valueOf(checkQueryStatusResponseData.get("data"));
        if ("0".equals(checkStatus)) {
            ApiResponse<Object> updateStatusAdjudicationData = this.loansService.updateStatusAdjudicationData(claimNo,
                    status);
        }

        ApiResponse<Object> updateInitialAmountsPrefundedData = this.loansService.updateInitialAmountsPrefunded(claimNo,
                initialRequestAmount, initialApprovedAmount);
        if (updateInitialAmountsPrefundedData == null) {
            logger.error("update Initial Amounts Prefunded Data returned null response data");
        } else {
            logger.info("Response data update Initial Amounts Prefunded Data");
        }

        Map<String, Object> sendNotification = new HashMap<>();
        sendNotification.put("claim_no", claimNo);
        sendNotification.put("requested_amount", initialRequestAmount);
        sendNotification.put("approved_amount", initialApprovedAmount);
        sendNotification.put(NotificationKeywords.TYPE, NotificationKeywords.PRE_AUTH_AMOUNT_APPROVED);
        sendNotification.put(NotificationKeywords.USER_ID, NotificationKeywords.USER_ID_VALUE);
        try {
            Producer.addInQueue(QueueConstants.LINKAGE.exchange, ExecutionsConstants.PREFUNDED_NOTIFICATON.key,Helper.convertMapToJsonString(sendNotification));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Final Document and bills approved by Adjudicator 
     * sender - Adjudicator
     * receiver - qube
     * 
     * @param response
     */
    // Final Cashless Credit Request flow db calls
    private void handleFinalCashlessCreditRequest(Map<String, String> response) {
        String employeeCode = response.get(EmailKeywords.EMPLOYEE_CODE);
        String claimNo = response.get(EmailKeywords.CLAIM_NO);
        String finalApprovedAmount = response.get(EmailKeywords.FINAL_CASHLESS_APPROVED_AMT);
        String finalRequestAmount = response.get(EmailKeywords.FINAL_CASHLESS_REQUEST_AMT);
        String subject = response.get(EmailKeywords.SUBJECT);
        String body = response.get(EmailKeywords.BODY);
        String gcpPath = response.get(EmailKeywords.GCP_PATH);
        String gcpFileName = response.get(EmailKeywords.GCP_FILE_NAME);
        String status = "APPROVED";
        String patientName = response.get(EmailKeywords.PATIENT_NAME);

        ApiResponse<Object> getPrefundedRequestIdRequest = this.loansService.getPrefundedRequestId(claimNo);
        Map<String, Object> prefundedIdResponseData = (Map<String, Object>) getPrefundedRequestIdRequest.getData();
        String prefundedRequestId = String.valueOf(prefundedIdResponseData.get("data"));
        if (prefundedRequestId == null) {
            logger.error("prefundedRequest returned null response data");
        } else {
            logger.info("Response data prefundedRequestId", prefundedRequestId);
        }

        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put(EmailKeywords.TYPE, "CASHLESS CREDIT REQUEST");
        preFundedEmailerMap.put(EmailKeywords.SUBJECT, subject);
        preFundedEmailerMap.put(EmailKeywords.IS_ACTIVE, true);
        preFundedEmailerMap.put("partnered_claim_no", "22");
        preFundedEmailerMap.put("pf_request_id", prefundedRequestId);
        preFundedEmailerMap.put("policy_no", employeeCode);
        preFundedEmailerMap.put("claim_no", claimNo);

        // Long prefundedEmail =
        // (Long)this.masterService.prefundedEmail(preFundedEmailerMap);

        ApiResponse<Object> prefundedEmailRequest = this.masterService.prefundedEmail(preFundedEmailerMap);
        Map<String, Object> prefundedEmailResponseData = (Map<String, Object>) prefundedEmailRequest.getData();
        String prefundedEmailId = String.valueOf(prefundedEmailResponseData.get("data")); // "225";
        if (prefundedEmailId == null) {
            logger.error("prefundedEmailId returned null response data");
        } else {
            logger.info("Response data prefundedEmailId", prefundedEmailId);
        }

        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put(EmailKeywords.TPA_DESK_ID, null);
        emailerItems.put("claim_no", claimNo);
        emailerItems.put("initial_amt_req", null);
        emailerItems.put("initial_amt_approved", null);
        emailerItems.put("final_adj_amt_req", finalRequestAmount);
        emailerItems.put("final_adj_amt_approved", finalApprovedAmount);
        emailerItems.put("metadata", subject + body);
        emailerItems.put(EmailKeywords.PATIENT_NAME, patientName);
        emailerItems.put("policy_no", employeeCode);

        // Long emailItems = (Long)this.masterService.emailInsert(emailerItems);

        ApiResponse<Object> emailItemsRequest = this.masterService.emailInsert(emailerItems);
        Map<String, Object> emailItemsResponseData = (Map<String, Object>) emailItemsRequest.getData();
        String emailItems = String.valueOf(emailItemsResponseData.get("data"));
        if (emailItems == null) {
            logger.error("emailItems returned null response data");
        } else {
            logger.info("Response data emailItems", emailItems);
        }

        ApiResponse<Object> adjudicationDataRequest = this.loansService.getAdjudicationDataId(claimNo);
        Map<String, Object> adjudicationDataResponseData = (Map<String, Object>) adjudicationDataRequest.getData();
        String adjudicationDataId = String.valueOf(adjudicationDataResponseData.get("data"));
        if (adjudicationDataId == null) {
            logger.error("adjudicationDataId returned null response data");
        } else {
            logger.info("Response data adjudicationDataId", adjudicationDataId);
        }

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put("adjudication_data_id", adjudicationDataId);
        adjudicationItems.put("pf_document_id", prefundedEmailId);
        adjudicationItems.put("document_url", gcpFileName);
        adjudicationItems.put(EmailKeywords.IS_ACTIVE, 1);

        // Long adjudicationItemsId =
        // (Long)this.loansService.adjudicationItemsStore(adjudicationItems);

        ApiResponse<Object> adjudicationItemsData = this.loansService.adjudicationItemsStore(adjudicationItems);
        Map<String, Object> adjudicationItemsDataResponseData = (Map<String, Object>) adjudicationItemsData.getData();
        String adjudicationItemsId = String.valueOf(adjudicationItemsDataResponseData.get("data"));
        if (adjudicationItemsId == null) {
            logger.error("adjudicationItemsId returned null response data");
        } else {
            logger.info("Response data adjudicationItemsId", adjudicationItemsId);
        }

        ApiResponse<Object> checkQueryStatusData = this.loansService.checkQueryStatus(claimNo);
        Map<String, Object> checkQueryStatusResponseData = (Map<String, Object>) checkQueryStatusData.getData();
        String checkStatus = String.valueOf(checkQueryStatusResponseData.get("data"));
        if ("0".equals(checkStatus)) {
            ApiResponse<Object> updateStatusAdjudicationData = this.loansService.updateStatusAdjudicationData(claimNo,
                    status);
        }

        ApiResponse<Object> updateFinalAmountsPrefundedData = this.loansService.updateFinalAmountsPrefunded(claimNo,
                finalRequestAmount, finalApprovedAmount);

        Map<String, Object> sendNotification = new HashMap<>();
        sendNotification.put("claim_no", claimNo);
        sendNotification.put("approved_amount", finalApprovedAmount);
        sendNotification.put(NotificationKeywords.TYPE, NotificationKeywords.FINAL_AMOUNT_APPROVED);
        sendNotification.put(NotificationKeywords.USER_ID, NotificationKeywords.USER_ID_VALUE);
        try {
            Producer.addInQueue(QueueConstants.LINKAGE.exchange, ExecutionsConstants.PREFUNDED_NOTIFICATON.key,Helper.convertMapToJsonString(sendNotification));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
    }

    /**
     * Final Document and bills sended by Tpa Desk 
     * sender - Tpa Desk
     * receiver - qube
     * 
     * @param response
     */
    // Final Bill And Discharge Summary flow db calls
    private void handleFinalBillAndDischargeSummary(Map<String, String> response) {
        String patientName = response.get(EmailKeywords.PATIENT_NAME);
        String claimNo = response.get(EmailKeywords.CLAIM_NO);
        String khId = response.get(EmailKeywords.TPA_DESK_ID);
        String subject = response.get(EmailKeywords.SUBJECT);
        String body = response.get(EmailKeywords.BODY);
        String gcpPath = response.get(EmailKeywords.GCP_PATH);
        String gcpFileName = response.get(EmailKeywords.GCP_FILE_NAME);
        String status = "PENDING";

        ApiResponse<Object> getPrefundedRequestIdRequest = this.loansService.getPrefundedRequestId(claimNo);
        Map<String, Object> prefundedIdResponseData = (Map<String, Object>) getPrefundedRequestIdRequest.getData();
        String prefundedRequestId = String.valueOf(prefundedIdResponseData.get("data"));
        if (prefundedRequestId == null) {
            logger.error("prefundedRequest returned null response data");
        } else {
            logger.info("Response data prefundedRequestId", prefundedRequestId);
        }

        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put(EmailKeywords.TYPE, "FINAL BILL AND DISCAHRGE SUMMARY");
        preFundedEmailerMap.put(EmailKeywords.SUBJECT, subject);
        preFundedEmailerMap.put(EmailKeywords.IS_ACTIVE, true);
        preFundedEmailerMap.put("partnered_claim_no", "22");
        preFundedEmailerMap.put("pf_request_id", prefundedRequestId);
        preFundedEmailerMap.put("policy_no", null);

        // Long prefundedEmail =
        // (Long)this.masterService.prefundedEmail(preFundedEmailerMap);

        ApiResponse<Object> prefundedEmailRequest = this.masterService.prefundedEmail(preFundedEmailerMap);
        Map<String, Object> prefundedEmailResponseData = (Map<String, Object>) prefundedEmailRequest.getData();
        String prefundedEmailId = String.valueOf(prefundedEmailResponseData.get("data")); // "225";
        if (prefundedEmailId == null) {
            logger.error("prefundedEmailId returned null response data");
        } else {
            logger.info("Response data prefundedEmailId", prefundedEmailId);
        }

        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put(EmailKeywords.TPA_DESK_ID, khId);
        emailerItems.put("claim_no", claimNo);
        emailerItems.put("initial_amt_req", null);
        emailerItems.put("initial_amt_approved", null);
        emailerItems.put("final_adj_amt_req", null);
        emailerItems.put("final_adj_amt_approved", null);
        emailerItems.put("metadata", subject + body);
        emailerItems.put(EmailKeywords.PATIENT_NAME, patientName);
        emailerItems.put("policy_no", null);

        // Long emailItems = (Long)this.masterService.emailInsert(emailerItems);

        ApiResponse<Object> emailItemsRequest = this.masterService.emailInsert(emailerItems);
        Map<String, Object> emailItemsResponseData = (Map<String, Object>) emailItemsRequest.getData();
        String emailItems = String.valueOf(emailItemsResponseData.get("data"));
        if (emailItems == null) {
            logger.error("emailItems returned null response data");
        } else {
            logger.info("Response data emailItems", emailItems);
        }

        ApiResponse<Object> adjudicationDataRequest = this.loansService.getAdjudicationDataId(claimNo);
        Map<String, Object> adjudicationDataResponseData = (Map<String, Object>) adjudicationDataRequest.getData();
        String adjudicationDataId = String.valueOf(adjudicationDataResponseData.get("data"));
        if (adjudicationDataId == null) {
            logger.error("adjudicationDataId returned null response data");
        } else {
            logger.info("Response data adjudicationDataId", adjudicationDataId);
        }

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put("adjudication_data_id", adjudicationDataId);
        adjudicationItems.put("pf_document_id", prefundedEmailId);
        adjudicationItems.put("document_url", gcpFileName);
        adjudicationItems.put(EmailKeywords.IS_ACTIVE, 1);

        // Long adjudicationItemsId =
        // (Long)this.loansService.adjudicationItemsStore(adjudicationItems);

        ApiResponse<Object> adjudicationItemsData = this.loansService.adjudicationItemsStore(adjudicationItems);
        Map<String, Object> adjudicationItemsDataResponseData = (Map<String, Object>) adjudicationItemsData.getData();
        String adjudicationItemsId = String.valueOf(adjudicationItemsDataResponseData.get("data"));
        if (adjudicationItemsId == null) {
            logger.error("adjudicationItemsId returned null response data");
        } else {
            logger.info("Response data adjudicationItemsId", adjudicationItemsId);
        }

        ApiResponse<Object> updateStatusAdjudicationData = this.loansService.updateStatusAdjudicationData(claimNo,
                status);

        Map<String, Object> sendNotification = new HashMap<>();
        sendNotification.put("claim_no", claimNo);
        sendNotification.put(NotificationKeywords.TYPE, NotificationKeywords.FINAL_DOCUMENT_SENT);
        sendNotification.put(NotificationKeywords.USER_ID, NotificationKeywords.USER_ID_VALUE);
        try {
            Producer.addInQueue(QueueConstants.LINKAGE.exchange, ExecutionsConstants.PREFUNDED_NOTIFICATON.key,Helper.convertMapToJsonString(sendNotification));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * To raised the Document query by Adjudicator
     * sender - Adjudicator
     * receiver - qube
     * 
     * @param response
     */
    // Additional Information flow db calls
    private void handleAdditionalInformation(Map<String, String> response) {

        String employeeCode = response.get(EmailKeywords.EMPLOYEE_CODE);
        String claimNo = response.get(EmailKeywords.CLAIM_NO);
        String documentRequired = response.get(EmailKeywords.DOCUMENT_REQUIRED);
        String patientName = response.get(EmailKeywords.PATIENT_NAME);
        String subject = response.get(EmailKeywords.SUBJECT);
        String body = response.get(EmailKeywords.BODY);
        String gcpPath = response.get(EmailKeywords.GCP_PATH);
        String gcpFileName = response.get(EmailKeywords.GCP_FILE_NAME);

        ApiResponse<Object> pfRequest = this.loansService.updateClaimNo(employeeCode, claimNo);
        Map<String, Object> pfRequestResponseData = (Map<String, Object>) pfRequest.getData();
        String pfRequestId = String.valueOf(pfRequestResponseData.get("data"));
        if (pfRequest.getStatus() == false) {
            logger.error("Clain No. updation Failed");
        } else {
            logger.info("Clain No. Updated at pfRequestId {}", pfRequestId);
        }

        ApiResponse<Object> adjudicationDataRequest = this.loansService.updateStatusAdjudicationData(claimNo, "QUERY");
        Map<String, Object> adjudicationDataResponseData = (Map<String, Object>) adjudicationDataRequest.getData();
        String adjudicationDataId = String.valueOf(adjudicationDataResponseData.get("data"));
        if (adjudicationDataRequest.getStatus() == false) {
            logger.error("Adjudication Data db Status update failed");
        } else {
            logger.info("Adjudication Data db Status update at id {}", adjudicationDataId);
        }

        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put(EmailKeywords.TYPE, "ADDTIONAL INFORMATION");
        preFundedEmailerMap.put(EmailKeywords.SUBJECT, subject);
        preFundedEmailerMap.put(EmailKeywords.IS_ACTIVE, true);
        preFundedEmailerMap.put("partnered_claim_no", claimNo);
        preFundedEmailerMap.put("pf_request_id", pfRequestId);

        ApiResponse<Object> prefundedEmailRequest = this.masterService.prefundedEmail(preFundedEmailerMap);
        Map<String, Object> prefundedEmailResponseData = (Map<String, Object>) prefundedEmailRequest.getData();
        String prefundedEmailId = String.valueOf(prefundedEmailResponseData.get("data")); // "225";
        if (prefundedEmailId == null) {
            logger.error("prefunded Email tbl update failed");
        } else {
            logger.info("prefunded Email tbl upadted at id {}", prefundedEmailId);
        }

        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put(EmailKeywords.TPA_DESK_ID, null);
        emailerItems.put("claim_no", claimNo);
        emailerItems.put("initial_amt_req", null);
        emailerItems.put("initial_amt_approved", null);
        emailerItems.put("final_adj_amt_req", null);
        emailerItems.put("final_adj_amt_approved", null);
        emailerItems.put("metadata", subject + body);
        emailerItems.put(EmailKeywords.PATIENT_NAME, patientName);

        ApiResponse<Object> emailItemsRequest = this.masterService.emailInsert(emailerItems);
        Map<String, Object> emailItemsResponseData = (Map<String, Object>) emailItemsRequest.getData();
        String emailItems = String.valueOf(emailItemsResponseData.get("data"));
        if (emailItemsRequest.getStatus() == false) {
            logger.error("email Items tbl update failed");
        } else {
            logger.info("email Items tbl upadted at id {}", emailItems);
        }

        Map<String, Object> adjudicationQuery = new HashMap<>();
        adjudicationQuery.put("adjudication_data_id", adjudicationDataId);
        adjudicationQuery.put("remark", documentRequired);
        adjudicationQuery.put("document_url", gcpFileName);
        adjudicationQuery.put(EmailKeywords.IS_ACTIVE, 1);
        adjudicationQuery.put("responded_at", null);
        adjudicationQuery.put("resolved_at", null);
        adjudicationQuery.put("status", "PENDING");

        ApiResponse<Object> adjudicationQueryData = this.loansService.adjudicationQueryStore(adjudicationQuery);
        Map<String, Object> adjudicationQueryDataResponseData = (Map<String, Object>) adjudicationQueryData.getData();
        String adjudicationQueryId = String.valueOf(adjudicationQueryDataResponseData.get("data"));
        if (adjudicationQueryData.getStatus() == false) {
            logger.error("adjudication Query tbl update failed");
        } else {
            logger.info("adjudication Query tbl upadted at id {}", adjudicationQueryId);
        }

        Map<String, Object> sendNotification = new HashMap<>();
        sendNotification.put("claim_no", claimNo);
        sendNotification.put("document_required", documentRequired);
        sendNotification.put(NotificationKeywords.TYPE, NotificationKeywords.RAISE_QUERY);
        sendNotification.put(NotificationKeywords.USER_ID, NotificationKeywords.USER_ID_VALUE);
        try {
            Producer.addInQueue(QueueConstants.LINKAGE.exchange, ExecutionsConstants.PREFUNDED_NOTIFICATON.key,Helper.convertMapToJsonString(sendNotification));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }







    // Pre-Auth flow db calls
    private void handlePreAuthcall(Map<String, String> response) {

        String partneredUserId = response.get(EmailKeywords.POLICY_NO);
        String khId = response.get(EmailKeywords.TPA_DESK_ID);
        String subject = response.get(EmailKeywords.SUBJECT);
        String patientName = response.get(EmailKeywords.PATIENT_NAME);
        String gcpPath = response.get(EmailKeywords.GCP_PATH);
        String gcpFileName = response.get(EmailKeywords.GCP_FILE_NAME);
        String uniqueId = UUID.randomUUID().toString();

        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put(EmailKeywords.EMAIL_TYPE, "PRE AUTH");
        preFundedEmailerMap.put(EmailKeywords.SUBJECT, subject);
        preFundedEmailerMap.put(EmailKeywords.IS_ACTIVE, true);
        preFundedEmailerMap.put("partnered_claim_no", "22");
        preFundedEmailerMap.put("pf_request_id", uniqueId);
        preFundedEmailerMap.put("policy_no", partneredUserId);

        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put(EmailKeywords.TPA_DESK_ID, khId);
        emailerItems.put("metadata", subject);
        emailerItems.put(EmailKeywords.PATIENT_NAME, patientName);
        emailerItems.put("policy_no", partneredUserId);

        Map<String, Object> masterEmailerData = new HashMap<>();
        masterEmailerData.putAll(preFundedEmailerMap);
        masterEmailerData.putAll(emailerItems);

        ApiResponse<Object> insertEmailMaster = this.masterService.prefundedEmailFull(masterEmailerData);

        Map<String, Object> preFundedReqMap = new HashMap<>();
        preFundedReqMap.put(EmailKeywords.USER_ID, "123");
        preFundedReqMap.put("hsp_id", "123");
        preFundedReqMap.put("partnered_user_id", partneredUserId);
        preFundedReqMap.put(EmailKeywords.TPA_DESK_ID, khId);
        preFundedReqMap.put("status", "PENDING");
        preFundedReqMap.put(EmailKeywords.TYPE, "TPA");
        preFundedReqMap.put(EmailKeywords.IS_ACTIVE, true);
        preFundedReqMap.put("claim_id",uniqueId); /// can pass uniqueId
        preFundedReqMap.put("unique_id",uniqueId);
        
        Map<String, Object> adjudicationDataMap = new HashMap<>();
        adjudicationDataMap.put(EmailKeywords.TPA_DESK_ID, khId);
        adjudicationDataMap.put("pf_req_id", uniqueId);
        adjudicationDataMap.put("file_path", gcpPath);
        adjudicationDataMap.put("file_name", gcpFileName);
        adjudicationDataMap.put(EmailKeywords.USER_ID, partneredUserId);
        adjudicationDataMap.put("hsp_id", 123);
        adjudicationDataMap.put("status", "PENDING");
        adjudicationDataMap.put("created_by", "TPA DESK");
        adjudicationDataMap.put("updated_by", "ADJUDICATOR");

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put("adjudication_data_id", uniqueId);
        adjudicationItems.put("pf_document_id", uniqueId);
        adjudicationItems.put("document_url", gcpFileName);
        adjudicationItems.put(EmailKeywords.IS_ACTIVE, 1);

        Map<String, Object> emailerData = new HashMap<>();
        emailerData.putAll(preFundedReqMap);
        emailerData.putAll(adjudicationDataMap);
        emailerData.putAll(adjudicationItems);

        // ApiResponse<Object> insertEmailDataData = this.loansService.insertEmailData(emailerData);
        
        ApiResponse<Object> insertEmailDataData = this.loansService.preAuthCall(emailerData);

    }

    private void handleAdditionalInformationcall(Map<String, String> response) {
        
        String claimNo = response.get(EmailKeywords.CLAIM_NO);
        String documentRequired = response.get(EmailKeywords.DOCUMENT_REQUIRED);
        String patientName = response.get(EmailKeywords.PATIENT_NAME);
        String subject = response.get(EmailKeywords.SUBJECT);
        String body = response.get(EmailKeywords.BODY);
        String gcpPath = response.get(EmailKeywords.GCP_PATH);
        String gcpFileName = response.get(EmailKeywords.GCP_FILE_NAME);

        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put(EmailKeywords.TYPE, "ADDTIONAL INFORMATION");
        preFundedEmailerMap.put(EmailKeywords.SUBJECT, subject);
        preFundedEmailerMap.put(EmailKeywords.IS_ACTIVE, true);
        preFundedEmailerMap.put("partnered_claim_no", claimNo);
        preFundedEmailerMap.put("pf_request_id", null); //pf_request_id updated in loan service

        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put("claim_no", claimNo);
        emailerItems.put("metadata", subject + body);
        emailerItems.put(EmailKeywords.PATIENT_NAME, patientName);

        Map<String, Object> masterEmailerData = new HashMap<>();
        masterEmailerData.putAll(preFundedEmailerMap);
        masterEmailerData.putAll(emailerItems);

        ApiResponse<Object> insertEmailMaster = this.masterService.prefundedEmailFull(masterEmailerData);

        Map<String, Object> adjudicationQuery = new HashMap<>();
        adjudicationQuery.put("adjudication_data_id", null); // adjudicationDataId updated in loan service
        adjudicationQuery.put("remark", documentRequired);
        adjudicationQuery.put("document_url", gcpFileName);
        adjudicationQuery.put(EmailKeywords.IS_ACTIVE, true);
        adjudicationQuery.put("status", "PENDING");

        ApiResponse<Object> insertEmailDataData = this.loansService.preAuthCall(adjudicationQuery);
    }

    private void handleQueryReplyCall(Map<String, String> response) {

        String claimNo = response.get(EmailKeywords.CLAIM_NO);
        String khId = response.get(EmailKeywords.TPA_DESK_ID);
        String subject = response.get(EmailKeywords.SUBJECT);
        String patientName = response.get(EmailKeywords.PATIENT_NAME);
        String body = response.get(EmailKeywords.BODY);
        String gcpPath = response.get(EmailKeywords.GCP_PATH);
        String gcpFileName = response.get(EmailKeywords.GCP_FILE_NAME);
        String status = "RESPONDED";

        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put(EmailKeywords.TYPE, "QUERY REPLY");
        preFundedEmailerMap.put(EmailKeywords.SUBJECT, subject);
        preFundedEmailerMap.put(EmailKeywords.IS_ACTIVE, true);
        preFundedEmailerMap.put("partnered_claim_no", claimNo);
        preFundedEmailerMap.put("pf_request_id", null); //update in loan service 
        preFundedEmailerMap.put("policy_no", null);

        ApiResponse<Object> prefundedEmailRequest = this.masterService.prefundedEmail(preFundedEmailerMap);
        Map<String, Object> prefundedEmailResponseData = (Map<String, Object>) prefundedEmailRequest.getData();
        String prefundedEmailId = String.valueOf(prefundedEmailResponseData.get("data")); // "225";

        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put(EmailKeywords.TPA_DESK_ID, khId);
        emailerItems.put("claim_no", claimNo);
        emailerItems.put("initial_amt_req", null);
        emailerItems.put("initial_amt_approved", null);
        emailerItems.put("final_adj_amt_req", null);
        emailerItems.put("final_adj_amt_approved", null);
        emailerItems.put("metadata", subject);
        emailerItems.put(EmailKeywords.PATIENT_NAME, patientName);
        emailerItems.put("policy_no", null);

        // Long emailItems = (Long)this.masterService.emailInsert(emailerItems);

        ApiResponse<Object> emailItemsRequest = this.masterService.emailInsert(emailerItems);
        Map<String, Object> emailItemsResponseData = (Map<String, Object>) emailItemsRequest.getData();
        String emailItems = String.valueOf(emailItemsResponseData.get("data"));
        if (emailItems == null) {
            logger.error("emailItems returned null response data");
        } else {
            logger.info("Response data emailItems", emailItems);
        }

        ApiResponse<Object> adjudicationDataRequest = this.loansService.getAdjudicationDataId(claimNo);
        Map<String, Object> adjudicationDataResponseData = (Map<String, Object>) adjudicationDataRequest.getData();
        String adjudicationDataId = String.valueOf(adjudicationDataResponseData.get("data"));

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put("adjudication_data_id", adjudicationDataId);
        adjudicationItems.put("pf_document_id", prefundedEmailId);
        adjudicationItems.put("document_url", gcpPath);
        adjudicationItems.put(EmailKeywords.IS_ACTIVE, 1);

        // Long adjudicationItemsId =
        // (Long)this.loansService.adjudicationItemsStore(adjudicationItems);

        ApiResponse<Object> adjudicationItemsData = this.loansService.adjudicationItemsStore(adjudicationItems);
        Map<String, Object> adjudicationItemsDataResponseData = (Map<String, Object>) adjudicationItemsData.getData();
        String adjudicationItemsId = String.valueOf(adjudicationItemsDataResponseData.get("data"));
        if (adjudicationItemsId == null) {
            logger.error("adjudicationItemsId returned null response data");
        } else {
            logger.info("Response data adjudicationItemsId", adjudicationItemsId);
        }

        this.loansService.handleQueryReply(claimNo, status);

    }

}