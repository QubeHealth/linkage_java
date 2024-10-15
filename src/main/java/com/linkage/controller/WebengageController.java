package com.linkage.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.protobuf.Api;
import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.WebengageService;
import com.linkage.core.validations.GetBankDetailsByAccSchema;
import com.linkage.core.validations.GetVpaByMobileSchema;
import com.linkage.core.validations.WebengageSchema;
import com.linkage.utility.Helper;

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

        // if (!userUpload.equals(body.getEventName())) {
        //     return new ApiResponse<>(false, "Invalid event name", null);
        // }

        return this.webengageService.createBulkUser(Map.of("users", body.getEventData()));

    }

    @POST
    @Path("/addEvent")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> addEvent(WebengageSchema.EventSchema body) {

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

    @POST
    @Path("/uploadEmployeeData")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> uploadEmployeeData(@Context HttpServletRequest request,
            WebengageSchema.UploadEmployeeSchema body) {
        Set<ConstraintViolation<WebengageSchema.UploadEmployeeSchema>> violations = validator
                .validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }

        try {
            if (!userUpload.equals(body.getEventName())) {
                return new ApiResponse<>(false, "Invalid event name", null);
            }

            ApiResponse<Object>  res = this.webengageService.pushEvent(body);
            if(res == null || !res.getStatus()) {
                return new ApiResponse<Object>(false, res != null ? res.getMessage() : "Error occurred", null);
            }

            ArrayList<HashMap<String, Object>> users = (ArrayList<HashMap<String, Object>>) body.getEventData();
            for(HashMap<String, Object> user : users) {
            WebengageSchema.EventSchema event = new WebengageSchema.EventSchema();
                event.setEventName(body.getEventName());
                event.setEventTime(Helper.getCurrentTimeForWebEngage());
                event.setUserId(user.get("userId").toString());
                event.setEventData(new HashMap<>());

                ApiResponse<Object> triggerEvent = addEvent(event);
                if(triggerEvent == null || !triggerEvent.getStatus()) {
                    return new ApiResponse<Object>(false, triggerEvent != null ? triggerEvent.getMessage() : "Error occurred", null);
                }
            }

            return new ApiResponse<Object>(true, "Employees Uploaded Successfully", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<Object>(false, e.getMessage(), null);
        }
    }
}