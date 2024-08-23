package com.linkage.controller;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
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
        Boolean sendSubscriptionEmailRes = Helper.sendEmail(configuration, request.getEmail(), "Hi",
                "Subscription Expired");
        if (!sendSubscriptionEmailRes) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiResponse<>(false, "Failed to send email", null)).build();
        }

        return Response.status(Response.Status.NO_CONTENT).entity(new ApiResponse<>(true, "Email Sent Successfully", null))
                .build();
    }
}
