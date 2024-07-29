package com.linkage.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.LoansService;
import com.linkage.client.MailReaderService;
import com.linkage.client.MailWriterService;
import com.linkage.client.MasterService;
import com.linkage.client.UserService;
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
    private MailWriterService mailWriterService;
    private UserService userService;

    public MailController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        this.mailReaderService = new MailReaderService(null, null, null, null, configuration);
        this.masterService = new MasterService(configuration);
        this.loansService = new LoansService(configuration);
        this.mailWriterService = new MailWriterService(configuration);
        this.userService = new UserService(configuration);
    }

    @POST
    @Path("/markEmailUnread")
    @Consumes(MediaType.APPLICATION_JSON)
    public String markEmailUnread(@Context HttpServletRequest request, String subject) throws MessagingException {

        return this.mailWriterService.markEmailUnread(subject);
    }

    @POST
    @Path("/emailReader")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response emailReader(@Context HttpServletRequest request) {
        try {
            // Fetch and process the email
            Set<Message> msgSet = this.mailReaderService.getUnreadEmail();

            for (Message message : msgSet) {
                try {
                    Response processedMail = this.mailReaderService.fetchAndProcessEmail(message);
                    ApiResponse<Object> apiResponse = (ApiResponse<Object>) processedMail.getEntity();
                    if (apiResponse.getStatus()) {
                        // Log the error or handle as needed
                        throw new Exception("Error processing email");
                    } else {
                        logger.info("Email fetch And Process Successfully");
                    }
                    Map<String, String> responseData = (Map<String, String>) apiResponse.getData();

                    // Extract common type from the response
                    String type = responseData.get(EmailKeywords.TYPE);

                    // Map to store handlers for different types
                    Map<String, Runnable> emailType = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                    emailType.put(EmailKeywords.PRE_AUTH, () -> handlePreAuth(responseData));
                    emailType.put(EmailKeywords.QUERY_REPLY, () -> handleQueryReply(responseData));
                    emailType.put(EmailKeywords.FINAL_BILL_AND_DISCHARGE_SUMMARY, () -> handleFinalBillAndDischargeSummary(responseData));
                    emailType.put(EmailKeywords.FINAL_CASHLESS_CREDIT_REQUEST, () -> handleFinalCashlessCreditRequest(responseData));
                    emailType.put(EmailKeywords.INITIAL_CASHLESS_CREDIT_REQUEST, () -> handleInitialCashlessCreditRequest(responseData));
                    emailType.put(EmailKeywords.ADDITIONAL_INFORMATION, () -> handleAdditionalInformation(responseData));

                    // Execute handler based on the type
                    Runnable handler = emailType.get(type);
                    if (handler != null) {
                        handler.run();
                        this.mailWriterService.mailSender(message);
                    }
                } catch (Exception e) {
                    // Log the exception and continue with the next message
                    logger.error("Failed to process email with subject: {}", message.getSubject(), e);
                    this.mailWriterService.markEmailUnread(message.getSubject());
                }
            }
            return Response.ok().entity("All emails processed successfully.").build();

        } catch (Exception e) {
            // Handle any unexpected exceptions
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiResponse<Object>(false, "Error processing emails: "+ e.getMessage(), null))
                    .build();
        }
    }

    /**
     * first mail
     * sender - tpa desk
     * receiver - qube
     */
    // Pre-Auth flow db calls
    private void handlePreAuth(Map<String, String> response) {

        String partneredUserId = response.get(EmailKeywords.PARTNERED_USER_ID);
        String khId = response.get(EmailKeywords.TPA_DESK_ID);
        String subject = response.get(EmailKeywords.SUBJECT);
        String patientName = response.get(EmailKeywords.PATIENT_NAME);
        String gcpPath = response.get(EmailKeywords.GCP_PATH);
        String gcpFileName = response.get(EmailKeywords.GCP_FILE_NAME);

        ApiResponse<Object> qbUserIdRequest = this.userService.getQbUserId(partneredUserId);
        Map<String, Object> qbUserIdData = (Map<String, Object>) qbUserIdRequest.getData();
        String userId = String.valueOf(qbUserIdData.get("data"));

        Map<String, Object> preFundedReqMap = new HashMap<>();
        preFundedReqMap.put(EmailKeywords.USER_ID, userId);
        preFundedReqMap.put("hsp_id", "123");
        preFundedReqMap.put("partnered_user_id", partneredUserId);
        preFundedReqMap.put(EmailKeywords.TPA_DESK_ID, khId);
        preFundedReqMap.put(EmailKeywords.STATUS, "PENDING");
        preFundedReqMap.put(EmailKeywords.TYPE, "TPA");
        preFundedReqMap.put(EmailKeywords.IS_ACTIVE, true);

        ApiResponse<Object> preFundedRequest = this.loansService.preFundedrequestStore(preFundedReqMap);
        if (!preFundedRequest.getStatus()) {
            logger.error("preFundedRequest Data insertion failed");
        } else {
            logger.info("Data Successfully updated");
        }
        Map<String, Object> responseData = (Map<String, Object>) preFundedRequest.getData();
        String preFundedRequestId = String.valueOf(responseData.get("data"));

        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put(EmailKeywords.EMAIL_TYPE, "PRE_AUTH");
        preFundedEmailerMap.put(EmailKeywords.SUBJECT, subject);
        preFundedEmailerMap.put(EmailKeywords.IS_ACTIVE, true);
        //preFundedEmailerMap.put("partnered_claim_no", "22");
        preFundedEmailerMap.put(EmailKeywords.PF_REQUEST_ID, preFundedRequestId);
        preFundedEmailerMap.put(EmailKeywords.POLICY_NO, partneredUserId);

        ApiResponse<Object> prefundedEmailRequest = this.masterService.prefundedEmail(preFundedEmailerMap);
        if (!prefundedEmailRequest.getStatus()) {
            logger.error("prefundedEmailer Data insertion failed");
        } else {
            logger.info("prefundedEmailer Data Successfully updated");
        }
        Map<String, Object> prefundedEmailResponseData = (Map<String, Object>) prefundedEmailRequest.getData();
        String prefundedEmailId = String.valueOf(prefundedEmailResponseData.get("data")); // "225";
        

        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put(EmailKeywords.TPA_DESK_ID, khId);
        emailerItems.put(EmailKeywords.METADATA, subject);
        emailerItems.put(EmailKeywords.PATIENT_NAME, patientName);
        emailerItems.put(EmailKeywords.POLICY_NO, partneredUserId);

        ApiResponse<Object> emailItemsRequest = this.masterService.emailInsert(emailerItems);
        if (!emailItemsRequest.getStatus()) {
            logger.error("emailItems Data insertion failed");
        } else {
            logger.info("emailItems Data Successfully updated");
        }
        Map<String, Object> emailItemsResponseData = (Map<String, Object>) emailItemsRequest.getData();
        String emailItems = String.valueOf(emailItemsResponseData.get("data"));
        

        Map<String, Object> adjudicationDataMap = new HashMap<>();
        adjudicationDataMap.put(EmailKeywords.TPA_DESK_ID, khId);
        adjudicationDataMap.put(EmailKeywords.PF_REQUEST_ID, preFundedRequestId);
        adjudicationDataMap.put(EmailKeywords.FILE_PATH, gcpPath);
        adjudicationDataMap.put(EmailKeywords.FILE_NAME, gcpFileName);
        adjudicationDataMap.put(EmailKeywords.USER_ID, partneredUserId);
        adjudicationDataMap.put(EmailKeywords.HSP_ID, 123);
        adjudicationDataMap.put(EmailKeywords.STATUS, "PENDING");
        adjudicationDataMap.put("created_by", "TPA DESK");
        adjudicationDataMap.put("updated_by", "ADJUDICATOR");

        ApiResponse<Object> adjudicationData = this.loansService.adjudicationDataStore(adjudicationDataMap);
        if (!adjudicationData.getStatus()) {
            logger.error("adjudicationData insertion failed");
        } else {
            logger.info("adjudicationData Successfully updated");
        }
        Map<String, Object> adjudicationResponseData = (Map<String, Object>) adjudicationData.getData();
        String adjudicationDataId = String.valueOf(adjudicationResponseData.get("data"));
        

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put(EmailKeywords.ADJUDICATION_DATA_ID, adjudicationDataId);
        adjudicationItems.put(EmailKeywords.PF_DOCUMENT_ID, prefundedEmailId);
        adjudicationItems.put(EmailKeywords.DOCUMENT_URL, gcpFileName);
        adjudicationItems.put(EmailKeywords.IS_ACTIVE, 1);

        ApiResponse<Object> adjudicationItemsData = this.loansService.adjudicationItemsStore(adjudicationItems);
        if (!adjudicationItemsData.getStatus()) {
            logger.error("adjudicationItems Data insertion failed");
        } else {
            logger.info("adjudicationItems Data Successfully updated");
        }
        Map<String, Object> adjudicationItemsDataResponseData = (Map<String, Object>) adjudicationItemsData.getData();
        String adjudicationItemsId = String.valueOf(adjudicationItemsDataResponseData.get("data"));
        
    }

    /**
     * Reply to the query raised by Adjudicator
     * sender - Tpa desk
     * receiver - qube
     */
    // Query reply flow db calls
    private void handleQueryReply(Map<String, String> response) {

        String claimNo = response.get(EmailKeywords.CLAIM_NO);
        String khId = response.get(EmailKeywords.TPA_DESK_ID);
        String subject = response.get(EmailKeywords.SUBJECT);
        String patientName = response.get(EmailKeywords.PATIENT_NAME);
        String gcpPath = response.get(EmailKeywords.GCP_PATH);
        String gcpFileName = response.get(EmailKeywords.GCP_FILE_NAME);
        String status = "RESPONDED";

        ApiResponse<Object> getPrefundedRequestIdRequest = this.loansService.getPrefundedRequestId(claimNo);
        if (!getPrefundedRequestIdRequest.getStatus()) {
            logger.error("prefundedRequestId data not found");
        } else {
            logger.info("preFundedRequestId fetched successfully");
        }
        Map<String, Object> prefundedIdResponseData = (Map<String, Object>) getPrefundedRequestIdRequest.getData();
        String prefundedRequestId = String.valueOf(prefundedIdResponseData.get("data"));
        

        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put(EmailKeywords.TYPE, "QUERY_REPLY");
        preFundedEmailerMap.put(EmailKeywords.SUBJECT, subject);
        preFundedEmailerMap.put(EmailKeywords.IS_ACTIVE, true);
        //preFundedEmailerMap.put("partnered_claim_no", claimNo);
        preFundedEmailerMap.put(EmailKeywords.PF_REQUEST_ID, prefundedRequestId);

        ApiResponse<Object> prefundedEmailRequest = this.masterService.prefundedEmail(preFundedEmailerMap);
        if (!prefundedEmailRequest.getStatus()) {
            logger.error("prefundedEmailer Data insertion failed");
        } else {
            logger.info("prefundedEmailer Data Successfully updated");
        }
        Map<String, Object> prefundedEmailResponseData = (Map<String, Object>) prefundedEmailRequest.getData();
        String prefundedEmailId = String.valueOf(prefundedEmailResponseData.get("data"));
        

        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put(EmailKeywords.TPA_DESK_ID, khId);
        emailerItems.put(EmailKeywords.CLAIM_NO, claimNo);
        emailerItems.put(EmailKeywords.METADATA, subject);
        emailerItems.put(EmailKeywords.PATIENT_NAME, patientName);

        ApiResponse<Object> emailItemsRequest = this.masterService.emailInsert(emailerItems);
        if (!emailItemsRequest.getStatus()) {
            logger.error("emailItems Data insertion failed");
        } else {
            logger.info("emailItems Data Successfully updated");
        }
        Map<String, Object> emailItemsResponseData = (Map<String, Object>) emailItemsRequest.getData();
        String emailItems = String.valueOf(emailItemsResponseData.get("data"));
        

        ApiResponse<Object> adjudicationDataRequest = this.loansService.getAdjudicationDataId(claimNo);
        if (!adjudicationDataRequest.getStatus()) {
            logger.error("adjudicationDataId data not found");
        } else {
            logger.info("adjudicationDataId fetched successfully");
        }
        Map<String, Object> adjudicationDataResponseData = (Map<String, Object>) adjudicationDataRequest.getData();
        String adjudicationDataId = String.valueOf(adjudicationDataResponseData.get("data"));
        

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put(EmailKeywords.ADJUDICATION_DATA_ID, adjudicationDataId);
        adjudicationItems.put(EmailKeywords.PF_DOCUMENT_ID, prefundedEmailId);
        adjudicationItems.put(EmailKeywords.DOCUMENT_URL, gcpFileName);
        adjudicationItems.put(EmailKeywords.IS_ACTIVE, 1);

        ApiResponse<Object> adjudicationItemsData = this.loansService.adjudicationItemsStore(adjudicationItems);
        if (adjudicationItemsData.getStatus()) {
            logger.error("adjudicationItems Data insertion failed");
        } else {
            logger.info("adjudicationItems Data Successfully updated");
        }
        Map<String, Object> adjudicationItemsDataResponseData = (Map<String, Object>) adjudicationItemsData.getData();
        String adjudicationItemsId = String.valueOf(adjudicationItemsDataResponseData.get("data"));
        

        ApiResponse<Object> updateStatus = this.loansService.handleQueryReply(claimNo, status);
        if (!updateStatus.getStatus()) {
            logger.error("Status not updated as RESPONDED");
        } else {
            logger.info("Status updated as RESPONDED");
        }
    }

    /**
     * Pre Auth Amount Approved by the Adjudicaotr
     * sender - Adjudicator
     * receiver - qube
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
        if (!getPrefundedRequestIdRequest.getStatus()) {
            logger.error("prefundedRequestId data not found");
        } else {
            logger.info("preFundedRequestId fetched successfully");
        }
        Map<String, Object> prefundedIdResponseData = (Map<String, Object>) getPrefundedRequestIdRequest.getData();
        String prefundedRequestId = String.valueOf(prefundedIdResponseData.get("data"));
        

        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put(EmailKeywords.TYPE, "INITIAL_CASHLESS_CREDIT_REQUEST");
        preFundedEmailerMap.put(EmailKeywords.SUBJECT, subject);
        preFundedEmailerMap.put(EmailKeywords.IS_ACTIVE, true);
        //preFundedEmailerMap.put("partnered_claim_no", "22");
        preFundedEmailerMap.put(EmailKeywords.PF_REQUEST_ID, prefundedRequestId);
        preFundedEmailerMap.put(EmailKeywords.POLICY_NO, employeeCode);

        ApiResponse<Object> prefundedEmailRequest = this.masterService.prefundedEmail(preFundedEmailerMap);
        if (!prefundedEmailRequest.getStatus()) {
            logger.error("prefundedEmailer Data insertion failed");
        } else {
            logger.info("prefundedEmailer Data Successfully updated");
        }
        Map<String, Object> prefundedEmailResponseData = (Map<String, Object>) prefundedEmailRequest.getData();
        String prefundedEmailId = String.valueOf(prefundedEmailResponseData.get("data"));
        

        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put(EmailKeywords.TPA_DESK_ID, null);
        emailerItems.put(EmailKeywords.CLAIM_NO, claimNo);
        emailerItems.put("initial_amt_req", initialRequestAmount);
        emailerItems.put("initial_amt_approved", initialApprovedAmount);
        emailerItems.put(EmailKeywords.METADATA, subject + body);
        emailerItems.put(EmailKeywords.POLICY_NO, employeeCode);

        ApiResponse<Object> emailItemsRequest = this.masterService.emailInsert(emailerItems);
        if (!emailItemsRequest.getStatus()) {
            logger.error("emailItems Data insertion failed");
        } else {
            logger.info("emailItems Data Successfully updated");
        }
        Map<String, Object> emailItemsResponseData = (Map<String, Object>) emailItemsRequest.getData();
        String emailItems = String.valueOf(emailItemsResponseData.get("data"));
        

        ApiResponse<Object> adjudicationDataRequest = this.loansService.getAdjudicationDataId(claimNo);
        if (!adjudicationDataRequest.getStatus()) {
            logger.error("adjudicationDataId data not found");
        } else {
            logger.info("adjudicationDataId fetched successfully");
        }
        Map<String, Object> adjudicationDataResponseData = (Map<String, Object>) adjudicationDataRequest.getData();
        String adjudicationDataId = String.valueOf(adjudicationDataResponseData.get("data"));
        

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put(EmailKeywords.ADJUDICATION_DATA_ID, adjudicationDataId);
        adjudicationItems.put(EmailKeywords.PF_DOCUMENT_ID, prefundedEmailId);
        adjudicationItems.put(EmailKeywords.DOCUMENT_URL, gcpFileName);
        adjudicationItems.put(EmailKeywords.IS_ACTIVE, 1);

        ApiResponse<Object> adjudicationItemsData = this.loansService.adjudicationItemsStore(adjudicationItems);
        if (!adjudicationItemsData.getStatus()) {
            logger.error("adjudicationItems Data insertion failed");
        } else {
            logger.info("adjudicationItems Data Successfully updated");
        }
        Map<String, Object> adjudicationItemsDataResponseData = (Map<String, Object>) adjudicationItemsData.getData();
        String adjudicationItemsId = String.valueOf(adjudicationItemsDataResponseData.get("data"));
        

        ApiResponse<Object> checkQueryStatusData = this.loansService.checkQueryStatus(claimNo);
        Map<String, Object> checkQueryStatusResponseData = (Map<String, Object>) checkQueryStatusData.getData();
        String checkStatus = String.valueOf(checkQueryStatusResponseData.get("data"));
        if ("0".equals(checkStatus)) {
            ApiResponse<Object> updateStatusAdjudicationData = this.loansService.updateStatusAdjudicationData(claimNo, status);
        }

        ApiResponse<Object> updateInitialAmountsPrefundedData = this.loansService.updateInitialAmountsPrefunded(claimNo,
                initialRequestAmount, initialApprovedAmount);
        if (!updateInitialAmountsPrefundedData.getStatus()) {
            logger.error("Initial amount update failed");
        } else {
            logger.info("Initial amount Successfully updated");
        }

    }

    /**
     * Final Document and bills approved by Adjudicator 
     * sender - Adjudicator
     * receiver - qube
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
        if (!getPrefundedRequestIdRequest.getStatus()) {
            logger.error("prefundedRequestId data not found");
        } else {
            logger.info("preFundedRequestId fetched successfully");
        }
        Map<String, Object> prefundedIdResponseData = (Map<String, Object>) getPrefundedRequestIdRequest.getData();
        String prefundedRequestId = String.valueOf(prefundedIdResponseData.get("data"));
        

        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put(EmailKeywords.TYPE, "FINAL_CASHLESS_CREDIT_REQUEST");
        preFundedEmailerMap.put(EmailKeywords.SUBJECT, subject);
        preFundedEmailerMap.put(EmailKeywords.IS_ACTIVE, true);
        preFundedEmailerMap.put("partnered_claim_no", "22");
        preFundedEmailerMap.put("pf_request_id", prefundedRequestId);
        preFundedEmailerMap.put(EmailKeywords.POLICY_NO, employeeCode);
        preFundedEmailerMap.put(EmailKeywords.CLAIM_NO, claimNo);

        ApiResponse<Object> prefundedEmailRequest = this.masterService.prefundedEmail(preFundedEmailerMap);
        if (!prefundedEmailRequest.getStatus()) {
            logger.error("prefundedEmailer Data insertion failed");
        } else {
            logger.info("prefundedEmailer Data Successfully updated");
        }
        Map<String, Object> prefundedEmailResponseData = (Map<String, Object>) prefundedEmailRequest.getData();
        String prefundedEmailId = String.valueOf(prefundedEmailResponseData.get("data"));
        

        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put(EmailKeywords.CLAIM_NO, claimNo);
        emailerItems.put("final_adj_amt_req", finalRequestAmount);
        emailerItems.put("final_adj_amt_approved", finalApprovedAmount);
        emailerItems.put(EmailKeywords.METADATA, subject + body);
        emailerItems.put(EmailKeywords.PATIENT_NAME, patientName);
        emailerItems.put(EmailKeywords.POLICY_NO, employeeCode);

        ApiResponse<Object> emailItemsRequest = this.masterService.emailInsert(emailerItems);
        if (!emailItemsRequest.getStatus()) {
            logger.error("emailItems Data insertion failed");
        } else {
            logger.info("emailItems Data Successfully updated");
        }
        Map<String, Object> emailItemsResponseData = (Map<String, Object>) emailItemsRequest.getData();
        String emailItems = String.valueOf(emailItemsResponseData.get("data"));
        

        ApiResponse<Object> adjudicationDataRequest = this.loansService.getAdjudicationDataId(claimNo);
        if (!adjudicationDataRequest.getStatus()) {
            logger.error("adjudicationDataId data not found");
        } else {
            logger.info("adjudicationDataId fetched successfully");
        }
        Map<String, Object> adjudicationDataResponseData = (Map<String, Object>) adjudicationDataRequest.getData();
        String adjudicationDataId = String.valueOf(adjudicationDataResponseData.get("data"));
        

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put(EmailKeywords.ADJUDICATION_DATA_ID, adjudicationDataId);
        adjudicationItems.put(EmailKeywords.PF_DOCUMENT_ID, prefundedEmailId);
        adjudicationItems.put(EmailKeywords.DOCUMENT_URL, gcpFileName);
        adjudicationItems.put(EmailKeywords.IS_ACTIVE, 1);

        ApiResponse<Object> adjudicationItemsData = this.loansService.adjudicationItemsStore(adjudicationItems);
        if (!adjudicationItemsData.getStatus()) {
            logger.error("adjudicationItems Data insertion failed");
        } else {
            logger.info("adjudicationItems Data Successfully updated");
        }
        Map<String, Object> adjudicationItemsDataResponseData = (Map<String, Object>) adjudicationItemsData.getData();
        String adjudicationItemsId = String.valueOf(adjudicationItemsDataResponseData.get("data"));
        

        ApiResponse<Object> checkQueryStatusData = this.loansService.checkQueryStatus(claimNo);
        Map<String, Object> checkQueryStatusResponseData = (Map<String, Object>) checkQueryStatusData.getData();
        String checkStatus = String.valueOf(checkQueryStatusResponseData.get("data"));
        if ("0".equals(checkStatus)) {
            ApiResponse<Object> updateStatusAdjudicationData = this.loansService.updateStatusAdjudicationData(claimNo, status);
        }

        ApiResponse<Object> updateFinalAmountsPrefundedData = this.loansService.updateFinalAmountsPrefunded(claimNo, finalRequestAmount, finalApprovedAmount);
                if (!updateFinalAmountsPrefundedData.getStatus()) {
                    logger.error("Final amount update failed");
                } else {
                    logger.info("Final amount Successfully updated");
                }
    }

    /**
     * Final Document and bills sended by Tpa Desk 
     * sender - Tpa Desk
     * receiver - qube
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
        if (!getPrefundedRequestIdRequest.getStatus()) {
            logger.error("prefundedRequestId data not found");
        } else {
            logger.info("preFundedRequestId fetched successfully");
        }
        Map<String, Object> prefundedIdResponseData = (Map<String, Object>) getPrefundedRequestIdRequest.getData();
        String prefundedRequestId = String.valueOf(prefundedIdResponseData.get("data"));
        

        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put(EmailKeywords.TYPE, "FINAL_BILL_AND_DISCAHRGE_SUMMARY");
        preFundedEmailerMap.put(EmailKeywords.SUBJECT, subject);
        preFundedEmailerMap.put(EmailKeywords.IS_ACTIVE, true);
        //preFundedEmailerMap.put("partnered_claim_no", "22");
        preFundedEmailerMap.put(EmailKeywords.PF_REQUEST_ID, prefundedRequestId);

        ApiResponse<Object> prefundedEmailRequest = this.masterService.prefundedEmail(preFundedEmailerMap);
        if (!prefundedEmailRequest.getStatus()) {
            logger.error("prefundedEmailer Data insertion failed");
        } else {
            logger.info("prefundedEmailer Data Successfully updated");
        }Map<String, Object> prefundedEmailResponseData = (Map<String, Object>) prefundedEmailRequest.getData();
        String prefundedEmailId = String.valueOf(prefundedEmailResponseData.get("data"));
        

        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put(EmailKeywords.TPA_DESK_ID, khId);
        emailerItems.put(EmailKeywords.CLAIM_NO, claimNo);
        emailerItems.put(EmailKeywords.METADATA, subject + body);
        emailerItems.put(EmailKeywords.PATIENT_NAME, patientName);

        ApiResponse<Object> emailItemsRequest = this.masterService.emailInsert(emailerItems);
        if (!emailItemsRequest.getStatus()) {
            logger.error("emailItems Data insertion failed");
        } else {
            logger.info("emailItems Data Successfully updated");
        }

        Map<String, Object> emailItemsResponseData = (Map<String, Object>) emailItemsRequest.getData();
        String emailItems = String.valueOf(emailItemsResponseData.get("data"));
        
        ApiResponse<Object> adjudicationDataRequest = this.loansService.getAdjudicationDataId(claimNo);
        if (!adjudicationDataRequest.getStatus()) {
            logger.error("adjudicationDataId data not found");
        } else {
            logger.info("adjudicationDataId fetched successfully");
        }
        Map<String, Object> adjudicationDataResponseData = (Map<String, Object>) adjudicationDataRequest.getData();
        String adjudicationDataId = String.valueOf(adjudicationDataResponseData.get("data"));
        

        Map<String, Object> adjudicationItems = new HashMap<>();
        adjudicationItems.put(EmailKeywords.ADJUDICATION_DATA_ID, adjudicationDataId);
        adjudicationItems.put(EmailKeywords.PF_DOCUMENT_ID, prefundedEmailId);
        adjudicationItems.put(EmailKeywords.DOCUMENT_URL, gcpFileName);
        adjudicationItems.put(EmailKeywords.IS_ACTIVE, 1);

        ApiResponse<Object> adjudicationItemsData = this.loansService.adjudicationItemsStore(adjudicationItems);
        if (!adjudicationItemsData.getStatus()) {
            logger.error("adjudicationItems Data insertion failed");
        } else {
            logger.info("adjudicationItems Data Successfully updated");
        }
        Map<String, Object> adjudicationItemsDataResponseData = (Map<String, Object>) adjudicationItemsData.getData();
        String adjudicationItemsId = String.valueOf(adjudicationItemsDataResponseData.get("data"));
        

        ApiResponse<Object> updateStatusAdjudicationData = this.loansService.updateStatusAdjudicationData(claimNo,
                status);
                if (!updateStatusAdjudicationData.getStatus()) {
                    logger.error("Status not updated as PENDING");
                } else {
                    logger.info("Status updated as PENDING");
                }
    }

    /**
     * To raised the Document query by Adjudicator
     * sender - Adjudicator
     * receiver - qube
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
        if (!pfRequest.getStatus()) {
            logger.error("Clain No. updation Failed");
        } else {
            logger.info("Clain No. Updated");
        }
        Map<String, Object> pfRequestResponseData = (Map<String, Object>) pfRequest.getData();
        String pfRequestId = String.valueOf(pfRequestResponseData.get("data"));
        

        ApiResponse<Object> adjudicationDataRequest = this.loansService.updateStatusAdjudicationData(claimNo, "QUERY");
        if (!adjudicationDataRequest.getStatus()) {
            logger.error("adjudicationDataId data not found");
        } else {
            logger.info("adjudicationDataId fetched successfully");
        }
        Map<String, Object> adjudicationDataResponseData = (Map<String, Object>) adjudicationDataRequest.getData();
        String adjudicationDataId = String.valueOf(adjudicationDataResponseData.get("data"));
        

        Map<String, Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put(EmailKeywords.TYPE, "QUERY_RAISED");
        preFundedEmailerMap.put(EmailKeywords.SUBJECT, subject);
        preFundedEmailerMap.put(EmailKeywords.IS_ACTIVE, true);
        //preFundedEmailerMap.put("partnered_claim_no", claimNo);
        preFundedEmailerMap.put(EmailKeywords.PF_REQUEST_ID, pfRequestId);

        ApiResponse<Object> prefundedEmailRequest = this.masterService.prefundedEmail(preFundedEmailerMap);
        if (!prefundedEmailRequest.getStatus()) {
            logger.error("prefundedEmailer Data insertion failed");
        } else {
            logger.info("prefundedEmailer Data Successfully updated");
        }
        Map<String, Object> prefundedEmailResponseData = (Map<String, Object>) prefundedEmailRequest.getData();
        String prefundedEmailId = String.valueOf(prefundedEmailResponseData.get("data"));
        

        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put(EmailKeywords.TPA_DESK_ID, null);
        emailerItems.put(EmailKeywords.CLAIM_NO, claimNo);
        emailerItems.put(EmailKeywords.METADATA, body);
        emailerItems.put(EmailKeywords.PATIENT_NAME, patientName);

        ApiResponse<Object> emailItemsRequest = this.masterService.emailInsert(emailerItems);
        if (!emailItemsRequest.getStatus()) {
            logger.error("emailItems Data insertion failed");
        } else {
            logger.info("emailItems Data Successfully updated");
        }
        Map<String, Object> emailItemsResponseData = (Map<String, Object>) emailItemsRequest.getData();
        String emailItems = String.valueOf(emailItemsResponseData.get("data"));
        

        Map<String, Object> adjudicationQuery = new HashMap<>();
        adjudicationQuery.put(EmailKeywords.ADJUDICATION_DATA_ID, adjudicationDataId);
        adjudicationQuery.put("remark", documentRequired);
        adjudicationQuery.put(EmailKeywords.DOCUMENT_URL, gcpFileName);
        adjudicationQuery.put(EmailKeywords.IS_ACTIVE, 1);
        adjudicationQuery.put("responded_at", null);
        adjudicationQuery.put("resolved_at", null);
        adjudicationQuery.put(EmailKeywords.STATUS, "PENDING");

        ApiResponse<Object> adjudicationQueryData = this.loansService.adjudicationQueryStore(adjudicationQuery);
        if (!adjudicationQueryData.getStatus()) {
            logger.error("adjudicationQuery Data insertion failed");
        } else {
            logger.info("adjudicationQuery Data Successfully updated");
        }
        Map<String, Object> adjudicationQueryDataResponseData = (Map<String, Object>) adjudicationQueryData.getData();
        String adjudicationQueryId = String.valueOf(adjudicationQueryDataResponseData.get("data"));
        

    }
}