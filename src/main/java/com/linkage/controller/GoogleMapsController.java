package com.linkage.controller;

import com.google.api.client.util.Sets;
import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.api.HspDetails;
import com.linkage.client.GoogleMapsService;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import com.linkage.core.validations.HspByLocation;
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
import jakarta.ws.rs.core.Response;

@Path("/api/hsp")
public class GoogleMapsController extends BaseController {
    public GoogleMapsService mapsService;

    public GoogleMapsController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);

        this.mapsService = new GoogleMapsService(configuration);
    }

    @POST
    @Path("/nearbySearch")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response nearbySearch(@Context HttpServletRequest request, HspByLocation reqBody) {

        Set<ConstraintViolation<HspByLocation>> violations = validator
                .validate(reqBody);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(new ApiResponse<>(false, errorMessage, null)).build();
        }

        try {

            HashMap<String, Object> gmbApiData = mapsService.gmbApiData(reqBody.getLatitude(), reqBody.getLongitude(), reqBody.getRadius());
            if(gmbApiData == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ApiResponse<>(false, "Error Occurred. Please try again later.", null)).build();
            }

            return Response.status(Response.Status.OK).entity(new ApiResponse<>(true, "Success", gmbApiData)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ApiResponse<>(false, "Error Occurred. Please try again later.", null)).build();
        }
    }
}
