package com.linkage.controller;

import java.util.Map;
import java.util.Set;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.validations.GetReferalUrl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

public class FirebaseController extends BaseController {

    protected FirebaseController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);

    }

    @POST
    @Path("/getReferralUrl")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Map<String, Object>> getReferalUrl(@Context HttpServletRequest request,
            GetReferalUrl body) {
        Set<ConstraintViolation<GetReferalUrl>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }

        Map<String, Object> requestBody = Map.of(
                "dynamicLinkInfo", Map.of(
                        "domainUriPrefix", "https://qubehealth.page.link",
                        "link", "https://www.qubehealth.com/cd=dkkd",
                        "androidInfo", Map.of(
                                "androidPackageName", "com.qubehealth"),
                        "iosInfo", Map.of(
                                "iosBundleId", "com.qubehealth")));

        return null;
    }

}
