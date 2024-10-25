package com.linkage.controller;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.SmsClient;
import com.linkage.core.validations.OtpSmsSchema;
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
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Set;

@Path("/api/sms")
public class SmsController extends BaseController {

    private final SmsClient smsClient;

    public SmsController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        this.smsClient = new SmsClient(configuration);
    }

    @POST
    @Path("/sendOtp")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendOtp(@Context HttpServletRequest request, OtpSmsSchema reqBody) {
        // Validate request body
        Set<ConstraintViolation<OtpSmsSchema>> violations = validator.validate(reqBody);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiResponse<>(false, errorMessage, null))
                    .build();
        }

        // Retrieve parameters for sending OTP
        String phoneNumber = reqBody.getPhoneNumber();
        String otp = reqBody.getOtp();
        String expiryTime = reqBody.getExpiryTime();
        String dltTemplateId = configuration.getSmsConfig().getDltOtpSmsTemplateId();
        
        // Send OTP message
        String response = smsClient.sendMessage(phoneNumber, dltTemplateId, 
                "Dear User, %s is your login OTP into Qubehealth App. OTP is valid for %s mins.", 
                otp, expiryTime);

        // Check response and create ApiResponse object
        boolean isSuccess = response.contains("success"); // Simplified success check
        ApiResponse<String> apiResponse = new ApiResponse<>(isSuccess, 
                isSuccess ? "OTP sent successfully" : "Failed to send OTP", response);

        // Return response with appropriate HTTP status
        return Response.status(isSuccess ? Response.Status.OK : Response.Status.INTERNAL_SERVER_ERROR)
                .entity(apiResponse)
                .build();
    }
}