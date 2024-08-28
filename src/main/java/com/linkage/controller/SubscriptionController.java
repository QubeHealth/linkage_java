package com.linkage.controller;

import java.util.ArrayList;
import java.util.List;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.constants.Constants;
import com.linkage.core.validations.SubscriptionSchema;
import com.linkage.utility.Helper;

import jakarta.validation.Valid;
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
    public Response sendEmailSubscription(@Valid SubscriptionSchema request) {

        List<ApiResponse<Object>> responses = new ArrayList<ApiResponse<Object>>();

        Boolean success = false;

        for (String email : request.getEmail()) {
            Boolean sendSubscriptionEmailRes = Helper.sendEmail(configuration, "noelpinto47@gmail.com", Constants.SUBSCRIPTION_EXPIRED_EMAIL.get("SUBJECT"),
            Constants.SUBSCRIPTION_EXPIRED_EMAIL.get("BODY"));
            if(!sendSubscriptionEmailRes) {
                responses.add(new ApiResponse<Object>(false, "Failed to send subscription expiry email", null));
            } else {
                responses.add(new ApiResponse<Object>(true, "Subscription expiry email sent to " + email, null));
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
