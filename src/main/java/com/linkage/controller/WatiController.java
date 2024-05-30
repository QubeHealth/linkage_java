package com.linkage.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.BefiscService;
import com.linkage.client.WatiService;
import com.linkage.core.validations.GetVpaByMobileSchema;
import com.linkage.core.validations.RefereeCashbackMsgSchema;
import com.linkage.core.validations.RefereeInviteMsgSchema;
import com.linkage.utility.Helper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

@Path("/api/wati")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class WatiController extends BaseController {
    private WatiService watiService;
    public WatiController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator); 
        watiService = new WatiService(configuration);
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
        ApiResponse<Object> watiResponse = this.watiService.referreeInviteMessage(body);
        if(!watiResponse.getStatus())
        {
            watiResponse.setMessage("Message failed to deliver");
            return watiResponse;
        }
        watiResponse.setMessage("Message delivered successfully");
        watiResponse.setData(null);
        return watiResponse; 
    

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
        ApiResponse<Object> watiResponse = this.watiService.referrerCashbackMessage(body);
        if(!watiResponse.getStatus())
        {
            watiResponse.setMessage("Message failed to deliver");
            return watiResponse;
        }
        watiResponse.setMessage("Message delivered successfully");
        watiResponse.setData(null);
        return watiResponse; 
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
        ApiResponse<Object> watiResponse = this.watiService.refereeCashbackMessage(body);
        if(!watiResponse.getStatus())
        {
            watiResponse.setMessage("Message failed to deliver");
            return watiResponse;
        }
        watiResponse.setMessage("Message delivered successfully");
        watiResponse.setData(null);
        return watiResponse; 
    }
    
}