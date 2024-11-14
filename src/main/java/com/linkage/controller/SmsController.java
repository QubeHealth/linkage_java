package com.linkage.controller;

import java.util.Set;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.SmsClient;
import com.linkage.client.SmsService;
import com.linkage.client.UserService;
import com.linkage.core.validations.OtpSmsSchema;
import com.linkage.core.validations.SmsSchema;

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

@Path("/api/sms")
public class SmsController extends BaseController {

    private final SmsClient smsClient;
    private SmsService smsService;
    private UserService userService;

    public SmsController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        this.smsClient = new SmsClient(configuration);
        this.smsService = new SmsService(configuration);
        this.userService = new UserService(configuration);
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
        String response = smsClient.sendMessage(
                phoneNumber,
                dltTemplateId,
                "%s is your OTP to log in to QubeHealth App (Valid only for %s Mins.) \n%s",
                otp,
                expiryTime,
                configuration.appSignature()
        );

        // Check response and create ApiResponse object
        boolean isSuccess = response.contains("success"); // Simplified success check
        ApiResponse<String> apiResponse = new ApiResponse<>(isSuccess,
                isSuccess ? "OTP sent successfully" : "Failed to send OTP", response);

        // Return response with appropriate HTTP status
        return Response.status(isSuccess ? Response.Status.OK : Response.Status.INTERNAL_SERVER_ERROR)
                .entity(apiResponse)
                .build();
    }

    @POST
    @Path("/paymentStatus")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> paymentStatus(SmsSchema.PaymentStatus body) {
        // Validate the input body
        Set<ConstraintViolation<SmsSchema.PaymentStatus>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }
    
        // Retrieve user mobile number
        ApiResponse<Object> result = this.userService.getMobileNo(body.getUserID());
        if (!result.getStatus()) {
            return new ApiResponse<>(false, "User mobile not found", result);
        }
    
        String mobileNo = (String) result.getData();
        body.setMobile(mobileNo);
    
        // Initialize variables for message content and template ID
        String message;
        String dltTemplateId;
    
        // Construct payment status message and select appropriate template ID
        if ("payment_pending".equals(body.getStatus())) {
            message = String.format(
                "Payment Pending:\nQubePay Payment Pending Txn. ID %s. Please check the app after 30 mins",
                body.getTransactionId()
            );
            dltTemplateId = configuration.getKayeraPaymentPendingTemplateId(); 
        } else if ("payment_failed".equals(body.getStatus())) {
            message = String.format(
                "Payment Failed: \nOh! Your payment via QubePay Txn. ID %s FAILED. Don't worry, any amount debited from %s will be REFUNDED within 7 Business days.",
                body.getTransactionId(),
                body.getType()
            );
            dltTemplateId = configuration.getKayeraPaymentFailedTemplateId(); 
        } else {
            return new ApiResponse<>(false, "Invalid payment status", null);
        }
    
        // Send the payment status message using the selected template ID
        String response = smsClient.sendMessage(mobileNo, dltTemplateId, message);
    
        // Check the response from the SMS client if needed and act accordingly
        if (response == null || response.isEmpty()) {
            return new ApiResponse<>(false, "Failed to send SMS", null);
        }
    
        return new ApiResponse<>(true, "Payment status message sent successfully", null);
    }
}
