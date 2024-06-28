package com.linkage.controller;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.MailReaderService;
import com.linkage.core.validations.EmailSchema;
import com.linkage.core.validations.RefereeInviteMsgSchema;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
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

    public MailController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        this.mailReaderService = new MailReaderService(null, null, null, null, configuration);
    }

    @POST
    @Path("/preAuthRequest")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String preAuthRequest(@Context HttpServletRequest request) throws MessagingException, IOException
    {
        this.mailReaderService.connect();
        Message getter = this.mailReaderService.fetchLatestEmail();
        return this.mailReaderService.fetchSubject(getter);
    }
}
