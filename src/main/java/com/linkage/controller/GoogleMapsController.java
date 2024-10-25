package com.linkage.controller;

import com.google.api.client.util.Sets;
import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.api.HspDetails;
import com.linkage.client.GoogleMapsService;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import com.linkage.core.validations.HspByLocation;
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
import jakarta.ws.rs.core.Response;

@Path("/api/hsp")
public class GoogleMapsController extends BaseController {
    public GoogleMapsService mapsService;

    public GoogleMapsController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);

        this.mapsService = new GoogleMapsService(configuration);
    }

    @POST
    @Path("/searchByLatLong")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response HspByLocation(@Context HttpServletRequest request, HspByLocation reqBody) {
        try {
            // Validate the request body
            Set<ConstraintViolation<HspByLocation>> violations = validator.validate(reqBody);
            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ApiResponse<>(false, errorMessage, null))
                        .build();
            }
    
            // Call the service to search for HSP
            ApiResponse<List<HspDetails>>  response = mapsService.searchHsp(reqBody);
            
            if (response == null || !response.getStatus()) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new ApiResponse<>(false, "Failed to fetch data from external service", null))
                        .build();
            }

            return Response.status(Response.Status.OK)
                    .entity(new ApiResponse<>(true, "Data fetched successfully", Helper.toJsonString(response.getData())))
                    .build();
        } catch (Exception e) {
            // Log the exception for debugging purposes
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiResponse<>(false, "Internal server error: " + e.getMessage(), null))
                    .build();
        }
    }

}
