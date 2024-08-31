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
    @Path("/commumnicationRefund")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public  ApiResponse<Object>  commumnicationRefund(SmsSchema.CommumnicationRefund body) {
         Set<ConstraintViolation<SmsSchema.CommumnicationRefund>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }

        ApiResponse<Object> userRes = this.userService.getMobileNo(body.getUserID());
        String mobileNo = (String) userRes.getData();

        body.setMobile(mobileNo);
        
        ApiResponse<Object> result = null;
        if (body.getStatus().equals("payment_pending")){

            result = this.smsService.paymentPending(body);

        }else if(body.getStatus().equals("payment_failed")){

            result = this.smsService.paymentFailed(body);

        }
        return result;
    }

    
}

