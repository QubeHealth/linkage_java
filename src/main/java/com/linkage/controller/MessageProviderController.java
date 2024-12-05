package com.linkage.controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.MessageProviderService;
import com.linkage.client.SmsClient;
import com.linkage.client.UserService;
import com.linkage.core.validations.AddFamilyMemberSchema;
import com.linkage.core.validations.AdjudicationStatusMessageSchema;
import com.linkage.core.validations.AhcAppointmentReportSchema;
import com.linkage.core.validations.AhcBookConfirmSchema;
import com.linkage.core.validations.BillRejectedSchema;
import com.linkage.core.validations.BillVerifiedMsgSchema;
import com.linkage.core.validations.CashbackTypeMessageSchema;
import com.linkage.core.validations.CreditAssignedSchema;
import com.linkage.core.validations.DisbursedMessageSchema;
import com.linkage.core.validations.DynamicMessageSchema;
import com.linkage.core.validations.MessageProviderSchema.SendMessageSchema;
import com.linkage.core.validations.NewUserOnboardingSchema;
import com.linkage.core.validations.RefereeCashbackMsgSchema;
import com.linkage.core.validations.RefereeInviteMsgSchema;
import com.linkage.core.validations.RepeatUserRetentionSchema;
import com.linkage.core.validations.SendCashbackMsgSchema;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Path("/api/messenger")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class MessageProviderController extends BaseController {
    private final SmsClient smsClient;
    private MessageProviderService messageProviderService;
    private UserService userService;

    public MessageProviderController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        messageProviderService = new MessageProviderService(configuration);
        this.smsClient = new SmsClient(configuration);
        this.userService = new UserService(configuration);

    }

    @POST
    @Path("/refereeInviteMessage")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> referreeInviteMessage(@Context HttpServletRequest request,
            RefereeInviteMsgSchema body) {
        Set<ConstraintViolation<RefereeInviteMsgSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }

        ApiResponse<Object> messageProviderResponse = this.messageProviderService.referreeInviteMessage(body);
        if (messageProviderResponse.getData() == null) {
            return messageProviderResponse;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        return new ApiResponse<Object>(true, null, response);

    }

    @POST
    @Path("/referrerCashbackMessage")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> referrerCashbackMessage(@Context HttpServletRequest request,
            RefereeInviteMsgSchema body) {
        Set<ConstraintViolation<RefereeInviteMsgSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }
        ApiResponse<Object> messageProviderResponse = this.messageProviderService.referrerCashbackMessage(body);
        if (messageProviderResponse.getData() == null) {
            return messageProviderResponse;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (!response.get("status").equals("submitted")) {
            messageProviderResponse.setMessage("Message failed to deliver");
            return messageProviderResponse;
        }
        messageProviderResponse.setMessage("Message delivered successfully");
        messageProviderResponse.setData(null);
        return messageProviderResponse;
    }

    @POST
    @Path("/refereeCashbackMessage")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> refereeCashbackMessage(@Context HttpServletRequest request,
            RefereeCashbackMsgSchema body) {
        Set<ConstraintViolation<RefereeCashbackMsgSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }
        ApiResponse<Object> messageProviderResponse = this.messageProviderService.refereeCashbackMessage(body);
        if (messageProviderResponse.getData() == null) {
            return messageProviderResponse;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (!response.get("status").equals("submitted")) {
            messageProviderResponse.setMessage("Message failed to deliver");
            return messageProviderResponse;
        }
        messageProviderResponse.setMessage("Message delivered successfully");
        messageProviderResponse.setData(null);
        return messageProviderResponse;
    }

    @POST
    @Path("/billVerifiedMessage")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> billVerifiedMessage(@Context HttpServletRequest request,
            BillVerifiedMsgSchema body) {
        Set<ConstraintViolation<BillVerifiedMsgSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }
        ApiResponse<Object> messageProviderResponse = this.messageProviderService.billVerifiedMessage(body);
        if (messageProviderResponse.getData() == null) {
            return messageProviderResponse;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (!response.get("status").equals("submitted")) {
            messageProviderResponse.setMessage("Message failed to deliver");
            return messageProviderResponse;
        }
        messageProviderResponse.setMessage("Message delivered successfully");
        messageProviderResponse.setData(null);
        return messageProviderResponse;
    }

    @POST
    @Path("/billRejectedMessage")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> billRejected(@Context HttpServletRequest request,
            BillRejectedSchema body) {
        Set<ConstraintViolation<BillRejectedSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }
        ApiResponse<Object> messageProviderResponse = this.messageProviderService.billRejected(body);
        if (messageProviderResponse.getData() == null) {
            return messageProviderResponse;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (!response.get("status").equals("submitted")) {
            messageProviderResponse.setMessage("Message failed to deliver");
            return messageProviderResponse;
        }
        messageProviderResponse.setMessage("Message delivered successfully");
        messageProviderResponse.setData(null);
        return messageProviderResponse;
    }

    @POST
    @Path("/cashbackTypeMessage")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object>cashbackTypeMessage(@Context HttpServletRequest request,
            CashbackTypeMessageSchema body) {
        Set<ConstraintViolation<CashbackTypeMessageSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }
        ApiResponse<Object> messageProviderResponse = this.messageProviderService.cashbackTypeMessage(body);
        if (messageProviderResponse.getData() == null) {
            return messageProviderResponse;
        }
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (!response.get("status").equals("submitted")) {
            messageProviderResponse.setMessage("Message failed to deliver");
            return messageProviderResponse;
        }
        messageProviderResponse.setMessage("Message delivered successfully");
        messageProviderResponse.setData(null);
        return messageProviderResponse;
    }

    @POST
    @Path("/sendCashbackMessage")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> sendCashbackMessage(SendCashbackMsgSchema body) {
        Set<ConstraintViolation<SendCashbackMsgSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }
        RefereeCashbackMsgSchema refereeBody = new RefereeCashbackMsgSchema();
        refereeBody.setCashbackAmt(body.getCashbackAmtReferee());
        refereeBody.setCompany(body.getCompany());
        refereeBody.setMobile(body.getMobileReferee());

        RefereeInviteMsgSchema refererBody = new RefereeInviteMsgSchema();
        refererBody.setCashbackAmt(body.getCashbackAmtReferer());
        refererBody.setMobile(body.getMobileReferer());

        ApiResponse<Object> messageProviderResponse = this.messageProviderService.refereeCashbackMessage(refereeBody);
        if (messageProviderResponse.getData() == null) {
            return messageProviderResponse;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (!response.get("status").equals("submitted")) {
            messageProviderResponse.setMessage("Message failed to deliver");
            return messageProviderResponse;
        }

        messageProviderResponse = this.messageProviderService.referrerCashbackMessage(refererBody);
        if (messageProviderResponse.getData() == null) {
            return messageProviderResponse;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> res = (Map<String, Object>) messageProviderResponse.getData();
        if (!res.get("status").equals("submitted")) {
            messageProviderResponse.setMessage("Message failed to deliver");
            return messageProviderResponse;
        }

        messageProviderResponse.setMessage("Message delivered successfully");
        return messageProviderResponse;
    }

    @POST
    @Path("/userAdjudicationStatusMsg")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> userAdjudicationStatusMsg(AdjudicationStatusMessageSchema body) {
        Set<ConstraintViolation<AdjudicationStatusMessageSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }
        ApiResponse<Object> messageProviderResponse = this.messageProviderService.sendAdjudicationMessage(body);
        if (messageProviderResponse.getData() == null) {
            return messageProviderResponse;
        }
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (!response.get("status").equals("submitted")) {
            messageProviderResponse.setMessage("Message failed to deliver");
            return messageProviderResponse;
        }
        messageProviderResponse.setMessage("Message delivered successfully");
        messageProviderResponse.setData(null);
        return messageProviderResponse;
    }

    @POST
    @Path("/userConfirmAhc")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> userConfirmAhc(AhcBookConfirmSchema body) {
        Set<ConstraintViolation<AhcBookConfirmSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }
        ApiResponse<Object> messageProviderResponse = this.messageProviderService.appointmentConfirmed(body);
        if (messageProviderResponse.getData() == null) {
            return messageProviderResponse;
        }
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (!response.get("status").equals("submitted")) {
            messageProviderResponse.setMessage("Message failed to deliver");
            return messageProviderResponse;
        }
        messageProviderResponse.setMessage("Message delivered successfully");
        messageProviderResponse.setData(null);
        return messageProviderResponse;
    }
    
    @POST
    @Path("/userAhcReport")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> userAhcReport(AhcAppointmentReportSchema body) {
        Set<ConstraintViolation<AhcAppointmentReportSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }
        ApiResponse<Object> messageProviderResponse = this.messageProviderService.ahcReportMessage(body);
        if (messageProviderResponse.getData() == null) {
            return messageProviderResponse;
        }
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (!response.get("status").equals("submitted")) {
            messageProviderResponse.setMessage("Message failed to deliver");
            return messageProviderResponse;
        }
        messageProviderResponse.setMessage("Message delivered successfully");
        messageProviderResponse.setData(null);
        return messageProviderResponse;
    }
    
    
    @POST
    @Path("/sendCreditApprovedMsg")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> sendCreditApprovedMsg(CreditAssignedSchema body) {
        Set<ConstraintViolation<CreditAssignedSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }
        ApiResponse<Object> messageProviderResponse = this.messageProviderService.creditAssigned(body);
        if (messageProviderResponse.getData() == null) {
            return messageProviderResponse;
        }
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (!response.get("status").equals("submitted")) {
            messageProviderResponse.setMessage("Message failed to deliver");
            return messageProviderResponse;
        }
        messageProviderResponse.setMessage("Message delivered successfully");
        messageProviderResponse.setData(null);
        return messageProviderResponse;
    }
        
    
    @POST
    @Path("/sendDisbursementMsg")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> sendDisbursementMsg(DisbursedMessageSchema body) {
        Set<ConstraintViolation<DisbursedMessageSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }
        ApiResponse<Object> messageProviderResponse = this.messageProviderService.disbursementMessage(body);
        if (messageProviderResponse.getData() == null) {
            return messageProviderResponse;
        }
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (!response.get("status").equals("submitted")) {
            messageProviderResponse.setMessage("Message failed to deliver");
            return messageProviderResponse;
        }
        messageProviderResponse.setMessage("Message delivered successfully");
        messageProviderResponse.setData(null);
        return messageProviderResponse;
    }

    @POST
    @Path("/requestToCreditLimitMsg")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> requestToCreditLimitMsg(CreditAssignedSchema body) {
        Set<ConstraintViolation<CreditAssignedSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }
        ApiResponse<Object> messageProviderResponse = this.messageProviderService.allowedToRequestCredit(body);
        if (messageProviderResponse.getData() == null) {
            return messageProviderResponse;
        }
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (!response.get("status").equals("submitted")) {
            messageProviderResponse.setMessage("Message failed to deliver");
            return messageProviderResponse;
        }
        messageProviderResponse.setMessage("Message delivered successfully");
        messageProviderResponse.setData(null);
        return messageProviderResponse;
    }

    @POST
    @Path("/newUserOnboarding")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> newUserOnboarding(NewUserOnboardingSchema body) {
        Set<ConstraintViolation<NewUserOnboardingSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }
        ApiResponse<Object> messageProviderResponse = this.messageProviderService.newUserOnboarding(body);
        if (messageProviderResponse.getData() == null) {
            return messageProviderResponse;
        }
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (!response.get("status").equals("submitted")) {
            messageProviderResponse.setMessage("Message failed to deliver");
            return messageProviderResponse;
        }
        messageProviderResponse.setMessage("Message delivered successfully");
        messageProviderResponse.setData(null);
        return messageProviderResponse;
    }
    
    @POST
    @Path("/retentionOfRepeatUser")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> repeatUserRetention(RepeatUserRetentionSchema body) {
        Set<ConstraintViolation<RepeatUserRetentionSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }
        ApiResponse<Object> messageProviderResponse = this.messageProviderService.repeatUserRetention(body);
        if (messageProviderResponse.getData() == null) {
            return messageProviderResponse;
        }
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (!response.get("status").equals("submitted")) {
            messageProviderResponse.setMessage("Message failed to deliver");
            return messageProviderResponse;
        }
        messageProviderResponse.setMessage("Message delivered successfully");
        messageProviderResponse.setData(null);
        return messageProviderResponse;
    }
    
        
    @SuppressWarnings("unchecked")
    @POST
    @Path("/addFamilyMember")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> addFamilyMember(AddFamilyMemberSchema body) {
        Set<ConstraintViolation<AddFamilyMemberSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }
        ApiResponse<Object> messageProviderResponse = this.messageProviderService.addFamilyMember(body);
        if (messageProviderResponse.getData() == null) {
            return messageProviderResponse;
        }
        
        String smsResponse = smsClient.sendMessage(
                body.getMobile(),
                configuration.getSmsConfig().getAddFamilyTemplateId(),
                "Hi, %s has just added you to their QubeHealth Account. Get %s%% Cashback on All your Medical Bill Payments using QubePay. Just Download the App NOW!",
                body.getPrimaryFname(),
                body.getCashback() 
        );

        boolean isSuccess = smsResponse.contains("success"); // Simplified success check

        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (!response.get("status").equals("submitted") || !isSuccess) {
            messageProviderResponse.setMessage("Message failed to deliver");
            return messageProviderResponse;
        }
        messageProviderResponse.setMessage("Message delivered successfully");
        messageProviderResponse.setData(null);
        return messageProviderResponse;
        
    }

    @POST
    @Path("/getTemplates")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> getTemplates(@Context HttpServletRequest request){

        ApiResponse<Object>  result = this.messageProviderService.getTemplates();
        if (!result.getStatus()) {
            result.setMessage("Failed to fetch the templates");
            return result;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) result.getData();
        @SuppressWarnings("rawtypes")
        ArrayList data =(ArrayList) response.get("templates");
        result.setData(data);

        return result;

    }


    @POST
    @Path("/dynamicWsapMessage")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> dynamicWsapMessage(@Context HttpServletRequest request,
            DynamicMessageSchema body) {
        Set<ConstraintViolation<DynamicMessageSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }

        ApiResponse<Object> messageProviderResponse = this.messageProviderService.dynamicWsapMessage(body);
        if (messageProviderResponse.getData() == null) {
            return messageProviderResponse;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        return new ApiResponse<Object>(true, null, response);

    }

    @POST
    @Path("/sendUserReport")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public ApiResponse<Object> sendUserReport(
        @Context HttpServletRequest request,
        @FormDataParam("report") FormDataBodyPart report,
        @FormDataParam("first_name") String firstName,
        @FormDataParam("email") String email,
        @FormDataParam("mobile") String mobile,
        @FormDataParam("appointment_date") String appointmentDate,
        @FormDataParam("file_url") String fileUrl
    ) {
        if (report == null || (mobile == null && email == null) || fileUrl == null || firstName == null || appointmentDate == null) {
            return new ApiResponse<Object>(false, "Invalid Request", null);
        }

        try {

            // Email subject and body
            String emailSubject = "Health Checkup Report!";
            String emailBody = "Hi " + firstName + ",<br><br>" +
                "Please find the report of your Health Checkup conducted on " + appointmentDate + ".<br>" +
                "Please find the report attached to this email.<br><br>" +
                "Thanks,<br>" +
                "QubeHealth Team";

            if(email != null) {
                // Send email
                ApiResponse<Object> sendEmailRes = this.messageProviderService.sendEmailWithAttachment(
                                                        email,
                                                        emailSubject,
                                                        emailBody,
                                                        report.getEntityAs(InputStream.class),
                                                        report.getContentDisposition().getFileName(),
                                                        report.getMediaType().toString(),
                                                        configuration
                                                    );
                if(sendEmailRes == null || !sendEmailRes.getStatus()) {
                    return sendEmailRes;
                }
            }

            // Send Whatsapp message
            ApiResponse<Object> sendWhatsappMessageRes = this.messageProviderService.sendWhatsappMessageWithAttachment(
                                                    fileUrl,
                                                    mobile,
                                                    firstName,
                                                    appointmentDate,
                                                    null,
                                                    null,
                                                    null,
                                                    "REPORT"
                                                );
            if(sendWhatsappMessageRes == null || !sendWhatsappMessageRes.getStatus()) {
                return sendWhatsappMessageRes;
            }

            return new ApiResponse<Object>(true, "Success", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<Object>(false, "Something went wrong", null);
        }
    }

    @POST
    @Path("/userConfirmAhcNew")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public ApiResponse<Object> userConfirmAhcNew(
        @Context HttpServletRequest request,
        @FormDataParam("voucher") FormDataBodyPart voucher,
        @FormDataParam("voucher_url") String voucherUrl,
        @FormDataParam("first_name") String firstName,
        @FormDataParam("email") String email,
        @FormDataParam("mobile") String mobile,
        @FormDataParam("appointment_date") String appointmentDate,
        @FormDataParam("appointment_time") String appointmentTime,
        @FormDataParam("diagnostics_address") String diagnosticsAddress
    ) {

        try {

            Boolean isVoucherSent = false;

            // Send email
            if(email != null) {
                /**Send Email */
                String emailSubject = "Health Checkup Confirmed!";
                String emailBody = "Hi " + firstName + ",<br><br>" +
                "Please find the details of your appointment below:<br>" +
                "Diagnostic Center Address: " + diagnosticsAddress + "<br>" +
                "Date: " + appointmentDate + "<br>" +
                "Time: " + appointmentTime + "<br><br>" +
                "<strong>Note:</strong><br>" +
                "1. Show the attached PDF at the Diagnostic Center.<br>" +
                "2. Set a reminder and do not be late.<br>" +
                "3. Avoid eating for 10 to 12 hours before the day of your test.<br>" +
                "4. Avoid drinking juices, tea, or coffee before your test.<br><br>" +
                "Please find the voucher attached to this email.<br><br>" +
                "Thanks,<br>" +
                "QubeHealth Team";

                ApiResponse<Object> sendEmailRes = this.messageProviderService.sendEmailWithAttachment(
                    email,
                    emailSubject,
                    emailBody,
                    voucher.getEntityAs(InputStream.class),
                    voucher.getContentDisposition().getFileName(),
                    "application/pdf",
                    configuration
                );
                if(sendEmailRes == null || !sendEmailRes.getStatus()) {
                    isVoucherSent = false;
                } else {
                    isVoucherSent = true;
                }
            }

            // Send whatsapp message
            if(mobile != null) {
                ApiResponse<Object> sendWhatsappMessageRes = this.messageProviderService.sendWhatsappMessageWithAttachment(
                    voucherUrl,
                    mobile,
                    firstName,
                    appointmentDate,
                    appointmentTime,
                    diagnosticsAddress,
                    voucher.getContentDisposition().getFileName(),
                    "VOUCHER"
                );
                if(sendWhatsappMessageRes == null || !sendWhatsappMessageRes.getStatus()) {
                    isVoucherSent = false;
                } else {
                    isVoucherSent = true;
                }
            }

            if(isVoucherSent) {
                return new ApiResponse<Object>(true, "Success", null);
            } else {
                return new ApiResponse<Object>(false, "Unable to send vouchers", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Error Occurred. Unable to send vouchers", null);
        }
    }
}