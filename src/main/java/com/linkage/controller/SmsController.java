package com.linkage.controller;

import java.util.Set;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.SmsService;
import com.linkage.client.UserService;
import com.linkage.core.validations.SmsSchema;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/sms")
@Produces(MediaType.APPLICATION_JSON)
public class SmsController extends BaseController {

    private SmsService smsService;
    private UserService userService;

    public SmsController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        this.smsService = new SmsService(configuration);
        this.userService = new UserService(configuration);
    }

    @POST
    @Path("/paymentStatus")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> paymentStatus(SmsSchema.PaymentStatus body) {
        Set<ConstraintViolation<SmsSchema.PaymentStatus>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }

        ApiResponse<Object> result = this.userService.getMobileNo(body.getUserID());
        if (!result.getStatus()) {
            return new ApiResponse<>(false, "User mobile not found", result);
        }

        String mobileNo = (String) result.getData();
        body.setMobile(mobileNo);

        return this.smsService.sendPaymentSms(body);

    }

}
