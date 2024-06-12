package com.linkage.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.linkage.client.KaleyraService;
import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.validations.KaleyraInviteMessageSchema;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;


@Path("/api/kaleyra")
@Produces(MediaType.APPLICATION_JSON)
public class KaleyraController extends BaseController {

    private KaleyraService kaleyraService;

    public KaleyraController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        this.kaleyraService = new KaleyraService(configuration);
    }

    @POST
    @Path("/kaleyraInviteMessage")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> KaleyraInviteMessage(@Context HttpServletRequest request,
            KaleyraInviteMessageSchema body) {
        
        Set<ConstraintViolation<KaleyraInviteMessageSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }

        ApiResponse<Object> kaleyraResponse = this.kaleyraService.kaleyraInviteMessage(body);
        if (!kaleyraResponse.getStatus()) {
            kaleyraResponse.setMessage("Message failed to deliver");
            return kaleyraResponse;
        }
        kaleyraResponse.setMessage("Message delivered successfully");
        kaleyraResponse.setData(null);
        return kaleyraResponse;

    }
    
}
