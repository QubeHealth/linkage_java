package com.linkage.controller;

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

    public MailController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
    }

    @POST
    @Path("/mailSender")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    //ReferrrInviteMsgSchema must be changed to mail schema
    public Response referrerCashbackMessage(@Context HttpServletRequest request) throws MessagingException {
    
        //tester email id for now
        String winclientEmailId = "qubetestemailssend@gmail.com";
        String adjudicatorEmailId = "tmt.8@qubehealth.com";
        String host = "smtp.gmail.com";

        Properties properties = System.getProperties();

        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", "587"); // Change port if needed
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("qubetestemailssend@gmail.com", "vuopgzdlbsyzmwoo");
            }
        });
        // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field
            message.setFrom(new InternetAddress(winclientEmailId));

            // Set To: header field
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(adjudicatorEmailId));

            // Set Subject: header field
            message.setSubject("This is the Subject Line!");

            // Now set the actual message
            message.setText("This is the actual message body.");

            // Send message
            Transport.send(message);

            System.out.println("Sent message successfully...");

        return Response.status(Response.Status.OK).entity(new ApiResponse<>(true, "success", null)).build();
    }
}