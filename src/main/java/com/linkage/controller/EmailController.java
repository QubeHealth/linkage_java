package com.linkage.controller;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        if(req == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiResponse<>(false, "Request body is required", null))
                    .build();
        }

        Set<ConstraintViolation<EmailModel.sendAdBannerEmailReq>> violations = validator.validate(req);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiResponse<>(false, errorMessage, null))
                    .build();
        }

        try {
            // Parse the original date string into LocalDateTime
            DateTimeFormatter originalFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(req.getTriggerDateTime(), originalFormat);

            // Format the date to the desired format: "MMM d, yyyy 'at' hh:mm a"
            DateTimeFormatter desiredFormat = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
            String formattedDate = dateTime.format(desiredFormat);

            // Format the product name to title case: "Product Name"
            String formattedProductName = Arrays.stream(req.getProductName().split("_"))
                 .map(word -> word.charAt(0) + word.substring(1).toLowerCase()) // Capitalize first letter
                 .collect(Collectors.joining(" "));

            String emailSubject = formattedProductName + " Interested: " + req.getHrName() + " - " + req.getClusterName();
            String emailBody = "Hi Shweta,<br><br>" +
            "This is an auto-triggered mail by our Qubehealth System basis on the interest shown by " + req.getHrName() + " of \"Cluster Dashboard\".<br>" +
            "The interest shown is for our " + formattedProductName + " Product on " + formattedDate + ".<br>" +
            "<br>" +
            "Requesting to contact HR to take this ahead.<br>" +
            "HR Contact Number -<br>" +
            "HR Full Name -<br>" +
            "Designation -<br>" +
            "Cluster Dashboard Role - <br>" +
            "<br><br>" +
            "Thanks,<br>" +
            "Qubehealth";

            Boolean sendAdBannerEmailRes = Helper.sendEmail(configuration, "noel.pinto@qubehealth.com", emailSubject, emailBody);
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
