package com.linkage.controller;

import java.util.Map;
import java.util.Set;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.WebengageService;
import com.linkage.core.validations.WebengageSchema;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

@Path("/api/webengage")
@Produces(MediaType.APPLICATION_JSON)
public class WebengageController extends BaseController {

    private WebengageService webengageService;
    private String userUpload = "User_Uploaded";

    public WebengageController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        this.webengageService = new WebengageService(configuration);
    }

    @POST
    @Path("/createUser")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> createUser(@Context HttpServletRequest request, WebengageSchema.UserSchema body) {

        Set<ConstraintViolation<WebengageSchema.UserSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }

        if (!userUpload.equals(body.getEventName())) {
            return new ApiResponse<>(false, "Invalid event name", null);
        }

        return this.webengageService.createBulkUser(Map.of("users", body.getEventBody()));

    }

    @POST
    @Path("/addEvent")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> addEvent(@Context HttpServletRequest request, WebengageSchema.EventSchema body) {

        Set<ConstraintViolation<WebengageSchema.EventSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }

        return this.webengageService.pushEvent(body);

    }

}