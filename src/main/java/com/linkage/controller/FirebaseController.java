package com.linkage.controller;

import java.util.Map;
import java.util.Set;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.FirebaseService;

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
@Path("/api/firebase")
public class FirebaseController extends BaseController {
    private FirebaseService firebaseService;

    public FirebaseController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        this.firebaseService = new FirebaseService(configuration);

    }

    @POST
    @Path("/getReferralUrl")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getReferalUrl(@Context HttpServletRequest request
        ) {
        // Set<ConstraintViolation<GetReferalUrl>> violations = validator.validate(body);
        // if (!violations.isEmpty()) {
        //     // Construct error message from violations
        //     String errorMessage = violations.stream()
        //             .map(ConstraintViolation::getMessage)
        //             .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
        //     return Response.status(Response.Status.OK)
        //     .entity(new ApiResponse<>(false, errorMessage, null))
        //     .build();
        // }

        Map<String, Object> requestBody = Map.of(
                "dynamicLinkInfo", Map.of(
                        "domainUriPrefix", "https://qubehealth.page.link",
                        "link", "https://www.qubehealth.com?referralCode=" ,
                        "androidInfo", Map.of(
                                "androidPackageName", "com.qubehealth"),
                        "iosInfo", Map.of(
                                "iosBundleId", "com.qubehealth")));

        ApiResponse<Object> dApiResponse = firebaseService.getFirebaseShortUrl(requestBody);

        return Response.status(Response.Status.OK)
        .entity(new ApiResponse<>(true, "Data fetched successfully", dApiResponse.getData()))
        .build();

    }}


