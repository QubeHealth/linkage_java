package com.linkage.controller;

import java.io.IOException;
import java.util.Map;

import javax.mail.MessagingException;
import com.linkage.LinkageConfiguration;
import com.linkage.client.MailReaderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

@Path("/api/mail")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MailController extends BaseController {

    private MailReaderService mailReaderService;

    public MailController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        this.mailReaderService = new MailReaderService(null, null, null, null, configuration);
    }

    @POST
    @Path("/preAuthRequest")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> preAuthRequest(@Context HttpServletRequest request) throws MessagingException, IOException {
        return this.mailReaderService.fetchAndProcessEmail();
    }
}
