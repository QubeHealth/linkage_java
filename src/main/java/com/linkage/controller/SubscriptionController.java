package com.linkage.controller;

import javax.mail.MessagingException;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.FirebaseService;
import com.linkage.core.validations.GetReferalUrl;
import com.linkage.core.validations.SubscriptionSchema;
import com.linkage.service.SubscriptionService;
import com.linkage.utility.Helper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    public Response sendEmailSubscription(@Valid SubscriptionSchema request) {

        try {
            Helper.sendEmail(configuration, request.getEmail(), "Hi", "Subscription Expired");

            return Response.status(Response.Status.OK).entity(new ApiResponse<>(true, "Email Sent Successfully", null)).build();
        } catch (MessagingException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ApiResponse<>(false, e.getMessage(), e)).build();
        }    
    }
}
