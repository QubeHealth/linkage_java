package com.linkage.controller;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.FirebaseService;
import com.linkage.core.validations.GetReferalUrl;
import com.linkage.core.validations.SubscriptionSchema;
import com.linkage.service.SubscriptionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/subscription")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SubscriptionController extends BaseController  {
    
    private SubscriptionService subscriptionService;

    public SubscriptionController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        this.subscriptionService = new SubscriptionService(configuration);

    }

    @POST
    @Path("/sendSubscriptionExpiryEmail")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendEmailSubscription(SubscriptionSchema request) {

        
        return Response.status(Response.Status.OK).entity(new ApiResponse<>(true, "Email Sent Successfully", null)).build();
    }
}
