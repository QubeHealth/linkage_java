package com.linkage.controller;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;

import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/webhook")
@Produces(MediaType.APPLICATION_JSON)
public class WebhookController extends BaseController {

    public WebhookController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
    }

    @POST
    @Path("/eRupee")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response eRupeeData(Object body) {

        logger.info("E-RUPEE DATA {}\n", body);

        return Response.status(Response.Status.OK).entity(new ApiResponse<>(true, "success", null)).build();
    }

}
