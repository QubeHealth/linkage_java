package com.linkage.controller;

import java.util.List;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.validations.SubscriptionSchema;

import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/email")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmailController extends BaseController {
    public EmailController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);

    }

    @POST
    @Path("/sendAdBannerEmail")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendAdBannerEmail() {

        try {
            

            return Response.status(Response.Status.OK).entity(new ApiResponse<>(true, "Email sent successfully", null)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ApiResponse<>(false, "Error sending email", null)).build();
        }

    }
}
