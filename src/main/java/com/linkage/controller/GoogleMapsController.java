package com.linkage.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.GoogleMapsService;
import com.linkage.core.validations.TextSearchSchema;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/googlemaps")
@Produces(MediaType.APPLICATION_JSON)
public class GoogleMapsController extends BaseController {

    private GoogleMapsService googleMapsService;

    public GoogleMapsController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        googleMapsService = new GoogleMapsService(configuration);
        this.googleMapsService = new GoogleMapsService(configuration);
    }

    private Response response(Response.Status status, Object data) {
        return Response.status(status).entity(data).build();
    }

    @POST
    @Path("/textSearchid")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response textSearchId(@Context HttpServletRequest request,
            TextSearchSchema body) {
        // Validating the schema
        Set<ConstraintViolation<TextSearchSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse<>(false, errorMessage, null))
                    .build();
        }

        // now make the call to the third party API;
        Map<String, Object> receiver = new HashMap<>();
        receiver = (Map<String, Object>) this.googleMapsService.textSearchid(body);
        return Response.status(Response.Status.OK)
                .entity(new ApiResponse<>(true, "Data fetched successfully", receiver))
                .build();
    }

    @POST
    @Path("/textSearch")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response textSearchMain(@Context HttpServletRequest request,
            TextSearchSchema body) {
        // Validating the schema
        Set<ConstraintViolation<TextSearchSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse<>(false, errorMessage, null))
                    .build();
        }

        // now make the call to the third party API;
        Map<String, Object> receiver = new HashMap<>();
        receiver = (Map<String, Object>) this.googleMapsService.textSearch(body);
        return Response.status(Response.Status.OK)
                .entity(new ApiResponse<>(true, "Data fetched successfully", receiver))
                .build();
    }

}