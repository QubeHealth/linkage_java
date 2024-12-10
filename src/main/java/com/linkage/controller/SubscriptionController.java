package com.linkage.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.validations.SubscriptionSchema;
import com.linkage.utility.Helper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/subscription")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SubscriptionController extends BaseController {

    public SubscriptionController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);

    }

    @POST
    @Path("/sendSubscriptionExpiryEmail")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendEmailSubscription(List<SubscriptionSchema> request) {
        Set<ConstraintViolation<List<SubscriptionSchema>>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse<>(false, errorMessage, null))
                    .build();
        }

        List<ApiResponse<Object>> responses = new ArrayList<ApiResponse<Object>>();

        Boolean success = false;

        for (SubscriptionSchema email : request) {

            String emailSubject = email.getEmployeeName() + " is requesting Qube Renewal!";
            String emailBody = "Dear " + email.getHrName() + ",<br><br>" +
            email.getEmployeeName() + " from your team has requested a renewal of their Qube Subscription.<br>" +
            "There might be many others who are concerned about their subscription expiry. Please reach out to your contact at QubeHealth to get this resolved soon.<br><br>" +
            "Thanks,<br>" +
            "QubeHealth Subscription Team";

            Boolean sendSubscriptionEmailRes = Helper.sendEmail(configuration, email.getHrEmail(), emailSubject,
            emailBody);
            if(!sendSubscriptionEmailRes) {
                responses.add(new ApiResponse<Object>(false, "Failed to send subscription expiry email", null));
            } else {
                responses.add(new ApiResponse<Object>(true, "Subscription expiry email sent to " + email.getHrEmail(), null));
                success = true;
            }
        }

        if(!success) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ApiResponse<>(false, "Failed to send subscription expiry emails", responses))
                   .build();
        }

        return Response.status(Response.Status.OK).entity(new ApiResponse<>(true, "Email sent successfully", responses))
                .build();
    }
}
