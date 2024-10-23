package com.linkage.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.VendorService;
import com.linkage.core.validations.VendorSchema.SendAadharOtp;
import com.linkage.core.validations.VendorSchema.VerifyAadharOtp;
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

@Path("/api/vendor")
@Produces(MediaType.APPLICATION_JSON)
public class VendorController extends BaseController {

    private VendorService vendorService;

    public VendorController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);

        this.vendorService = new VendorService(configuration);
    }

    private Response response(Response.Status status, Object data) {
        return Response.status(status).entity(data).build();
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("/sendAadharOtp")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendAadharOtp(@Context HttpServletRequest request, SendAadharOtp body) {

        // Validate the request body
        final Set<ConstraintViolation<SendAadharOtp>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            final String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            return response(Response.Status.BAD_REQUEST, new ApiResponse<>(false, errorMessage, null));
        }

        // Call the service to send the OTP
        final ApiResponse<Object> sendAadharOtpResponse = this.vendorService.sendAadharOtp(body);
        if (!sendAadharOtpResponse.getStatus()) {
            return response(Response.Status.INTERNAL_SERVER_ERROR, sendAadharOtpResponse);
        }

        // Cast the response data to the appropriate type
        final Map<String, Object> sendAadharOtpData = (Map<String, Object>) sendAadharOtpResponse.getData();
        if (sendAadharOtpData == null) {
            return response(Response.Status.EXPECTATION_FAILED,
            new ApiResponse<>(false, "Send OTP failed", null));
        }
        final Map<String, Object> sendAadharOtpResult = (Map<String, Object>) sendAadharOtpData.get("result");
        if (sendAadharOtpResult == null) {
            return response(Response.Status.EXPECTATION_FAILED,
            new ApiResponse<>(false, "Send OTP failed", sendAadharOtpData));
        }
        // Return success response
        return response(Response.Status.OK,
                new ApiResponse<>(true, "Send OTP success", Map.of("access_key", sendAadharOtpResult.get("requestId"))));
    }
    
    @SuppressWarnings("unchecked")
    @POST
    @Path("/verifyAadharOtp")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verifyAadharOtp(@Context HttpServletRequest request, VerifyAadharOtp body) {

        // Validate the request body
        final Set<ConstraintViolation<VerifyAadharOtp>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            return response(Response.Status.BAD_REQUEST, new ApiResponse<>(false, errorMessage, null));
        }

        final Map<String, Object> vendorRequest = new HashMap<>();
        vendorRequest.put("aadhaar", body.getAadharNumber());
        vendorRequest.put("referenceId", body.getAccessKey());
        vendorRequest.put("otp", body.getOtp());

        // Call the service to send the OTP
        final ApiResponse<Object> vendorResponse = this.vendorService.verifyAadharOtp(vendorRequest);
        if (!vendorResponse.getStatus()) {
            return response(Response.Status.INTERNAL_SERVER_ERROR, vendorResponse);
        }
        // Cast the response data to the appropriate type
        final Map<String, Object> vendorResponseData = (Map<String, Object>) vendorResponse.getData();
        if (vendorResponseData == null) {
             // Return success response
            return response(Response.Status.EXPECTATION_FAILED,
        new ApiResponse<>(false, "Aadhar verification failed", null));
        }

        final Map<String,Object> vendorResultData = (Map<String, Object> ) vendorResponseData;

        Map<String, Object> clientRequiredData = new HashMap<>();
        clientRequiredData.put("file", "result.xml_file");
        clientRequiredData.put("address", "result.splitAddress");
        clientRequiredData.put("image", "result.image");
        clientRequiredData.put("zip", "result.pincode");
        Map<String, Object> mappedValues = Helper.getMappedValuesFromMap(vendorResultData, clientRequiredData);
        // Return success response
        return response(Response.Status.OK,
                new ApiResponse<>(true, "aadhar verification success", mappedValues));
    }


}