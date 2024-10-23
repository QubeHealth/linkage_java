package com.linkage.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.BefiscService;
import com.linkage.core.constants.HspKeywords;
import com.linkage.core.validations.DigitapSchema.SendAadharOtp;
import com.linkage.core.validations.DigitapSchema.VerifyAadharOtp;
import com.linkage.core.validations.GetBankDetailsByAccSchema;
import com.linkage.core.validations.GetVpaByMobileSchema;
import com.linkage.core.validations.GetVpaDetailsSchema;
import com.linkage.utility.Helper;
import com.linkage.utility.RedisClient;

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

@Path("/api/befisc")
@Produces(MediaType.APPLICATION_JSON)
public class BefiscController extends BaseController {

    private BefiscService befiscService;

    public BefiscController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);

        this.befiscService = new BefiscService(configuration);
    }

    private Response response(Response.Status status, Object data) {
        return Response.status(status).entity(data).build();
    }


    @POST
    @Path("/getVpaByMobile")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Map<String, Object>> getVpaByMobile(
            GetVpaByMobileSchema body) {
        Set<ConstraintViolation<GetVpaByMobileSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }
        ApiResponse<Map<String, Object>> result = this.befiscService.mobileUpiSupreme(body);
        logger.info("BEFISC RESPONSE : {}", result.getData());

        if (!result.getStatus()) {
            return result;
        }

        List<Object> vpa = Helper.getDataFromMap(result.getData(), Arrays.asList("vpa"));
        List<Object> name = Helper.getDataFromMap(result.getData(), Arrays.asList("name"));
        List<Object> accountName = Helper.getDataFromMap(result.getData(), Arrays.asList("account_holder_name"));
        List<Object> entityType = Helper.getDataFromMap(result.getData(), Arrays.asList("entity_type"));


        Map<String, Object> data = new HashMap<>();
        data.put("vpa", vpa.isEmpty() ? null : vpa.get(0));
        data.put("merchant_name", name.isEmpty() ? null : name.get(0));
        data.put("bank_account_name", accountName.isEmpty() ? null : accountName.get(0));

        Map<String, Object> validation = hspValidationCheck(data.get("merchant_name").toString());
        if (Boolean.FALSE.equals(validation.get("valid_hsp"))) {
            validation = hspValidationCheck(data.get("bank_account_name").toString());
        }

        data.put("status", ((boolean) validation.get("valid_hsp")) ? "VALID_HSP" : "INVALID_HSP");
        data.put("keyword", validation.get("keyword"));
        data.put("account_type", entityType.isEmpty() ? null : entityType.get(0));


        result.setData(data);

        return result;
    }

    @POST
    @Path("/getMultipleUpi")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Map<String, Object>> getMultipleUpi(
            GetVpaByMobileSchema body) {
        Set<ConstraintViolation<GetVpaByMobileSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }
        ApiResponse<Map<String, Object>> result = this.befiscService.multipleUpi(body);
        logger.info("BEFISC RESPONSE : {}", result.getData());

        List<Object> upiIds = Helper.getDataFromMap(result.getData(), Arrays.asList("upi"));
        List<Object> name = Helper.getDataFromMap(result.getData(), Arrays.asList("name"));

        Map<String, Object> data = new HashMap<>();
        data.put("upi", upiIds.isEmpty() ? null : upiIds.get(0));
        data.put("name", name.isEmpty() ? null : name.get(0));
        result.setData(data);

        return result;

    }

    @POST
    @Path("/getVpaDetails")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Map<String, Object>> getNameByVpa(
            GetVpaDetailsSchema body) {
        Set<ConstraintViolation<GetVpaDetailsSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }
        ApiResponse<Map<String, Object>> result = this.befiscService.vpaAnalysis(body);

        if (!result.getStatus()) {
            return result;
        }

        List<Object> vpa = Helper.getDataFromMap(result.getData(), Arrays.asList("vpa"));
        List<Object> name = Helper.getDataFromMap(result.getData(), Arrays.asList("name"));
        List<Object> accountName = Helper.getDataFromMap(result.getData(), Arrays.asList("account_holder_name"));
        List<Object> entityType = Helper.getDataFromMap(result.getData(), Arrays.asList("entity_type"));

        Map<String, Object> data = new HashMap<>();
        data.put("vpa", vpa.isEmpty() ? null : vpa.get(0));
        data.put("merchant_name", name.isEmpty() ? null : name.get(0));
        data.put("bank_account_name", accountName.isEmpty() ? null : accountName.get(0));
        data.put("account_type", entityType.isEmpty() ? null : entityType.get(0));

        Map<String, Object> validation = hspValidationCheck(data.get("merchant_name").toString());
        if (Boolean.FALSE.equals(validation.get("valid_hsp"))) {
            validation = hspValidationCheck(data.get("bank_account_name").toString());
        }

        data.put("status", ((boolean) validation.get("valid_hsp")) ? "VALID_HSP" : "INVALID_HSP");
        data.put("keyword", validation.get("keyword"));
        result.setData(data);

        return result;

    }

    @POST
    @Path("/getBankDetails")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Map<String, Object>> getBankDetailsByAcc(@Context HttpServletRequest request,
            GetBankDetailsByAccSchema body) {
        Set<ConstraintViolation<GetBankDetailsByAccSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }

        ApiResponse<Map<String, Object>> result = this.befiscService.bankDetails(body);

        if (!result.getStatus()) {
            return result;
        }

        List<Object> accountStatus = Helper.getDataFromMap(result.getData(), Arrays.asList("account_status"));
        if (!accountStatus.get(0).toString().equals("SUCCESS")) {
            result.setStatus(false);
            result.setMessage("Invalid Bank details");
            result.setData(null);
            return result;
        }

        List<Object> accountHolderName = Helper.getDataFromMap(result.getData(), Arrays.asList("registered_name"));
        String name = accountHolderName.get(0) != null ? accountHolderName.get(0).toString() : "";

        Map<String, Object> validation = hspValidationCheck(name);

        Map<String, Object> data = new HashMap<>();
        data.put("status", ((boolean) validation.get("valid_hsp")) ? "VALID_HSP" : "INVALID_HSP");
        data.put("keyword", validation.get("keyword"));
        data.put("bank_account_name", name);
        data.put("account_number", body.getAccountNumber());
        data.put("ifsc_code", body.getIfscCode());

        result.setData(data);

        return result;

    }

    public Map<String, Object> hspValidationCheck(String hspName) {

        List<String> hspKeywords = HspKeywords.hspKeys();
        String key = "";
        Boolean valid = false;

        for (String keyword : hspKeywords) {
            if (hspName.toLowerCase().contains(keyword.toLowerCase())) {
                key = keyword;
                valid = true;
                break;
            }
        }
        return Map.of("valid_hsp", valid, "keyword", key);
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
        final ApiResponse<Object> sendAadharOtpResponse = this.befiscService.sendAadharOtp(body);
        if (!sendAadharOtpResponse.getStatus()) {
            return response(Response.Status.INTERNAL_SERVER_ERROR, sendAadharOtpResponse);
        }

        // Cast the response data to the appropriate type
        final Map<String, Object> sendAadharOtpData = (Map<String, Object>) sendAadharOtpResponse.getData();
        if (sendAadharOtpData == null) {
            return response(Response.Status.OK,
            new ApiResponse<>(false, "Send OTP failed", null));
        }
        // Store the result in Redis
        RedisClient.set(body.getUniqueId(), Helper.toJsonString(sendAadharOtpData));
        final Map<String, Object> sendAadharOtpResult = (Map<String, Object>) sendAadharOtpData.get("result");
        if (sendAadharOtpResult == null) {
            return response(Response.Status.OK,
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

        final Map<String, Object> befiscRequest = new HashMap<>();
        befiscRequest.put("aadhaar", body.getAadharNumber());
        befiscRequest.put("referenceId", body.getAccessKey());
        befiscRequest.put("otp", body.getOtp());

        // Call the service to send the OTP
        final ApiResponse<Object> befiscResponse = this.befiscService.verifyAadharOtp(befiscRequest);
        if (!befiscResponse.getStatus()) {
            return response(Response.Status.INTERNAL_SERVER_ERROR, befiscResponse);
        }
        // Cast the response data to the appropriate type
        final Map<String, Object> befiscResponseData = (Map<String, Object>) befiscResponse.getData();
        if (befiscResponseData == null) {
             // Return success response
            return response(Response.Status.EXPECTATION_FAILED,
        new ApiResponse<>(false, "Aadhar verification failed", null));
        }

        final Map<String,Object> befiscResultData = (Map<String, Object> ) befiscResponseData;

        Map<String, Object> clientRequiredData = new HashMap<>();
        clientRequiredData.put("file", "result.xml_file");
        clientRequiredData.put("address", "result.splitAddress");
        clientRequiredData.put("image", "result.image");
        clientRequiredData.put("zip", "result.pincode");
        Map<String, Object> mappedValues = Helper.getMappedValuesFromMap(befiscResultData, clientRequiredData);
        // Return success response
        return response(Response.Status.OK,
                new ApiResponse<>(true, "aadhar verification success", mappedValues));
    }


}