package com.linkage.controller;

import java.util.Set;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.validations.EmailModel;
import com.linkage.utility.Helper;

import jakarta.validation.ConstraintViolation;
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
    public Response sendAdBannerEmail(EmailModel.sendAdBannerEmailReq req) {
        Set<ConstraintViolation<EmailModel.sendAdBannerEmailReq>> violations = validator.validate(req);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse<>(false, errorMessage, null))
                    .build();
        }

        try {
            
            String emailSubject = req.getProductName() + " Interested: " + req.getHrName() + " - " + req.getClusterName();
            String emailBody = "Hi Shweta, \n\n"+
            "This is an auto-triggered mail by our Qubehealth System basis on the interest shown by " + req.getHrName() + " of “Cluster Dashboard“.\n" + //
            "The interest shown is for our " + req.getProductName() + " Product on May 12, 2024 at 03.05 pm.\n" + //
            "\n" + //
            "Requesting to contact HR to take this ahead.\n" + //
            "HR Contact Number -\n" + //
            "HR Full Name -\n" + //
            "Designation -\n" + //
            "Cluster Dashboard Role - \n" + //
            "\n\n" + //
            "Thanks,\n" + //
            "Qubehealth";

            Boolean sendAdBannerEmailRes = Helper.sendEmail(configuration, "noelpinto47@gmail.com", emailSubject, emailBody);
            if(!sendAdBannerEmailRes) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ApiResponse<>(false, "Error sending email", null)).build();
            }

            return Response.status(Response.Status.OK).entity(new ApiResponse<>(true, "Email sent successfully", null)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ApiResponse<>(false, "Error sending email", null)).build();
        }

    }
}
