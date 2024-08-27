package com.linkage.controller;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.MessageProviderService;
import com.linkage.core.validations.BillRejectedSchema;
import com.linkage.core.validations.BillVerifiedMsgSchema;
import com.linkage.core.validations.CashbackTypeMessageSchema;
import com.linkage.core.validations.RefereeCashbackMsgSchema;
import com.linkage.core.validations.RefereeInviteMsgSchema;
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

@Path("/api/messenger")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class MessageProviderController extends BaseController {
    private MessageProviderService messageProviderService;

    public ApiResponse<Object> templatesData;

    public MessageProviderController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        messageProviderService = new MessageProviderService(configuration);
        templatesData =  messageProviderService.getTemplates();
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
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (response.get("status").equals("submitted")) {
            messageProviderResponse.setMessage("Message failed to deliver");
            return messageProviderResponse;
        }
        messageProviderResponse.setMessage("Message delivered successfully");
        messageProviderResponse.setData(null);
        return messageProviderResponse;

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
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (response.get("status").equals("submitted")) {
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
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (response.get("status").equals("submitted")) {
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
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (response.get("status").equals("submitted")) {
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
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (response.get("status").equals("submitted")) {
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
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (response.get("status").equals("submitted")) {
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
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) messageProviderResponse.getData();
        if (response.get("status").equals("submitted")) {
            messageProviderResponse.setMessage("Message failed to deliver");
            return messageProviderResponse;
        }

        messageProviderResponse = this.messageProviderService.referrerCashbackMessage(refererBody);
        @SuppressWarnings("unchecked")
        Map<String, Object> res = (Map<String, Object>) messageProviderResponse.getData();
        if (res.get("status").equals("submitted")) {
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


}