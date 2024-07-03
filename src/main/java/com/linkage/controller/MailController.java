package com.linkage.controller;

import java.io.IOException;
import java.util.Map;

import javax.mail.MessagingException;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.MailReaderService;
import com.linkage.client.MasterService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/mail")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MailController extends BaseController {

    private MailReaderService mailReaderService;
    private MasterService masterService;

    public MailController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        this.mailReaderService = new MailReaderService(null, null, null, null, configuration);
        this.masterService = new MasterService(configuration);
    }

    @POST
    @Path("/emailReader")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> emailReader(@Context HttpServletRequest request) throws MessagingException, IOException {
        return this.mailReaderService.fetchAndProcessEmail();
    }

    @POST
    @Path("/emailDataStore")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response emailDataStore(@Context HttpServletRequest request, Map<String, String> requestBody) {
        try {
            //ApiResponse<Object> dApiResponse = masterService.mailDataStore(requestBody);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse<>(true, "Data stored successfully", null))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiResponse<>(false, "Error storing data", null))
                    .build();
        }
    }
}