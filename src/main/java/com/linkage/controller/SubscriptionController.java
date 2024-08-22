package com.linkage.controller;

import com.linkage.LinkageConfiguration;
import com.linkage.client.FirebaseService;
import com.linkage.core.validations.GetReferalUrl;
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

public class SubscriptionController extends BaseController  {
    
    private SubscriptionService subscriptionService;

    public SubscriptionController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        this.subscriptionService = new SubscriptionService(configuration);

    }

    @POST
    @Path("/sendEmailSubscription")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendEmailSubscription(@Context HttpServletRequest request) {

        
        return null;
    }
}
