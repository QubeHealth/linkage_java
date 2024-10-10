package com.linkage.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.BbpsService;
import com.linkage.core.validations.BbpsSchema;

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

@Path("/api/bbps")
@Produces(MediaType.APPLICATION_JSON)
public class BbpsController extends BaseController {

    private final BbpsService bbpsService;

    public BbpsController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        this.bbpsService = new BbpsService(configuration);
    }

    @POST
    @Path("/billerInfoRequest")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response billerInfoRequest(@Context HttpServletRequest request, BbpsSchema.BillerInfoRequest body) {
        // Validate request
        String validationError = validateRequest(body);
        if (validationError != null) {
            return buildBadRequestResponse(validationError);
        }

        try {
            // Prepare request map
            Map<String, Object> jsonMap = Map.of("billerId", Collections.singletonList(body.getBillerId()));

            // Call external service
            Map<String, Object> bbpsResponse = bbpsService.billerInfoRequest(jsonMap);

            // Determine success or failure
            if (bbpsResponse == null) {
                return buildErrorResponse("Problem occurred while calling the API");
            }
            
            // Determine success or failure
            return handleResponse(bbpsResponse);

        } catch (JsonProcessingException e) {
            return buildErrorResponse("Error processing JSON: " + e.getMessage());
        } catch (Exception e) {
            return buildErrorResponse("An unexpected error occurred: " + e.getMessage());
        }
    }

    private Response buildErrorResponse(String errorMessage) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse<>(false, "Error occurred", errorMessage))
                .build();
    }

    private Response buildResponse(Object data, String successMessage, String failureMessage) {
        if (data == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiResponse<>(false, failureMessage, null))
                    .build();
        }
        return Response.ok(new ApiResponse<>(true, successMessage, data)).build();
    }

    private String validateRequest(Object req) {
        Set<ConstraintViolation<Object>> violations = validator.validate(req);
        if (!violations.isEmpty()) {
            return violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce((acc, msg) -> acc + "; " + msg)
                    .orElse("");
        }
        return null;
    }

    private Response buildBadRequestResponse(String errorMessage) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ApiResponse<>(false, errorMessage, null))
                .build();
    }

    @POST
    @Path("/billFetchRequest")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response billFetchRequest(@Context HttpServletRequest request, BbpsSchema.BillFetchRequest body) {
        // Validate request
        String validationError = validateRequest(body);
        if (validationError != null) {
            return buildBadRequestResponse(validationError);
        }
    
        try {
            // Prepare the request map
            Map<String, Object> map = createBillFetchRequestMap(body);
    
            // Call external service
            Map<String, Object> bbpsResponse = bbpsService.billFetchRequest(map);
            if (bbpsResponse == null) {
                return buildErrorResponse("Problem occurred while calling the API");
            }
    
            // Determine success or failure
            return handleResponse(bbpsResponse);

        } catch (JsonProcessingException e) {
            return buildErrorResponse("Error processing JSON: " + e.getMessage());
        } catch (Exception e) {
            return buildErrorResponse("An unexpected error occurred: " + e.getMessage());
        }
    }
    
    private Map<String, Object> createBillFetchRequestMap(BbpsSchema.BillFetchRequest body) {
        Map<String, Object> map = new HashMap<>();
        map.put("agentId", "CC01CC01513515340681");
        map.put("billerAdhoc", false);
    
        // Create agentDeviceInfo map
        Map<String, String> agentDeviceInfo = new HashMap<>();
        agentDeviceInfo.put("ip", "192.168.2.183");
        agentDeviceInfo.put("initChannel", "INT");
        agentDeviceInfo.put("mac", "01-23-45-67-89-ab");
        map.put("agentDeviceInfo", agentDeviceInfo);
    
        // Create customerInfo map
        Map<String, Object> customerInfo = new HashMap<>();
        customerInfo.put("customerMobile", 9892506507L);
        customerInfo.put("customerEmail", "kishor.anand@avenues.info");
        customerInfo.put("customerAdhaar", 548550008000L);
        customerInfo.put("customerPan", "");
        map.put("customerInfo", customerInfo);
    
        map.put("billerId", "HPCL00000NAT01");
    
        // Create inputParams map
        Map<String, Object> inputParams = new HashMap<>();
        List<Map<String, Object>> inputList = new ArrayList<>();
    
        // Create input parameters
        inputList.add(createInputParam("Consumer Number", 90883000));
        inputList.add(createInputParam("Distributor ID", 13645300));
    
        inputParams.put("input", inputList);
        map.put("inputParams", inputParams);
    
        return map;
    }
    
    private Map<String, Object> createInputParam(String paramName, Object paramValue) {
        Map<String, Object> param = new HashMap<>();
        param.put("paramName", paramName);
        param.put("paramValue", paramValue);
        return param;
    }

    @POST
    @Path("/billPaymentRequest")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response billPaymentRequest(@Context HttpServletRequest request, BbpsSchema.BillPaymentRequest body) {
    
        // Validate request
        String validationError = validateRequest(body);
        if (validationError != null) {
            return buildBadRequestResponse(validationError);
        }
    
        try {
            // Create the main map
            Map<String, Object> jsonMap = new HashMap<>();
    
            // Add basic fields
            jsonMap.put("billerAdhoc", "false");
            jsonMap.put("agentId", "CC01CC01513515340681");
    
            // Create agentDeviceInfo map
            Map<String, String> agentDeviceInfo = new HashMap<>();
            agentDeviceInfo.put("initChannel", "AGT");
            agentDeviceInfo.put("ip", "192.168.2.183");
            agentDeviceInfo.put("mac", "01-23-45-67-89-ab");
            jsonMap.put("agentDeviceInfo", agentDeviceInfo);
    
            // Create customerInfo map
            Map<String, String> customerInfo = new HashMap<>();
            customerInfo.put("customerMobile", "9892506507");
            customerInfo.put("customerEmail", "kishor.anand@avenues.info");
            customerInfo.put("customerAdhaar", "548550008000");
            customerInfo.put("customerPan", "");
            jsonMap.put("customerInfo", customerInfo);
    
            // Add billerId
            jsonMap.put("billerId", "HPCL00000NAT01");
    
            // Create inputParams map
            Map<String, Object> inputParams = new HashMap<>();
            List<Map<String, String>> inputList = new ArrayList<>();
    
            // Create input parameters
            inputList.add(createInputParam("Consumer Number", "90883000"));
            inputList.add(createInputParam("Distributor ID", "13645300"));
            inputParams.put("input", inputList);
            jsonMap.put("inputParams", inputParams);
    
            // Create billerResponse list
            List<Map<String, String>> billerResponseList = new ArrayList<>();
            Map<String, String> billerResponse = new HashMap<>();
            billerResponse.put("billAmount", "92300");
            billerResponse.put("billNumber", "1123314338567");
            billerResponse.put("customerName", "Ramesh Agrawal");
            billerResponse.put("dueDate", "");
            billerResponseList.add(billerResponse);
            jsonMap.put("billerResponse", billerResponseList);
    
            // Create additionalInfo map
            Map<String, Object> additionalInfo = new HashMap<>();
            List<Map<String, String>> infoList = new ArrayList<>();
            
            infoList.add(createInfo("Distributor Contact", "243306"));
            infoList.add(createInfo("Distributor Name", "Billavenue COMPANY"));
            infoList.add(createInfo("Consumer Number", "90883000"));
            infoList.add(createInfo("Consumer Address", "NA"));
            additionalInfo.put("info", infoList);
            jsonMap.put("additionalInfo", additionalInfo);
    
            // Create amountInfo map
            Map<String, String> amountInfo = new HashMap<>();
            amountInfo.put("amount", "92300");
            amountInfo.put("currency", "356");
            amountInfo.put("custConvFee", "0");
            jsonMap.put("amountInfo", amountInfo);
    
            // Create paymentMethod map
            Map<String, String> paymentMethod = new HashMap<>();
            paymentMethod.put("paymentMode", "Credit Card");
            paymentMethod.put("quickPay", "N");
            paymentMethod.put("splitPay", "N");
            jsonMap.put("paymentMethod", paymentMethod);
    
            // Create paymentInfo map
            Map<String, Object> paymentInfo = new HashMap<>();
            List<Map<String, String>> paymentInfoList = new ArrayList<>();
            
            paymentInfoList.add(createInfo("CardNum", "4111111111111111"));
            paymentInfoList.add(createInfo("AuthCode", "123456"));
            paymentInfo.put("info", paymentInfoList);
            jsonMap.put("paymentInfo", paymentInfo);
    
            // Call external service
            Map<String, Object> bbpsResponse = bbpsService.billPaymentRequest(jsonMap);
    
            // Determine success or failure
            return handleResponse(bbpsResponse);
    
        } catch (JsonProcessingException e) {
            return buildErrorResponse("Error processing JSON: " + e.getMessage());
        } catch (Exception e) {
            return buildErrorResponse("An unexpected error occurred: " + e.getMessage());
        }
    }
    
    // Helper methods to create input parameters and additional info
    private Map<String, String> createInputParam(String name, String value) {
        Map<String, String> param = new HashMap<>();
        param.put("paramName", name);
        param.put("paramValue", value);
        return param;
    }
    
    private Map<String, String> createInfo(String name, String value) {
        Map<String, String> info = new HashMap<>();
        info.put("infoName", name);
        info.put("infoValue", value);
        return info;
    }
    
    // Helper method to handle the API response
    private Response handleResponse(Map<String, Object> response) {
        if (response == null) {
            return buildErrorResponse("Problem occurred while calling the API");
        }

        String responseCode = (String) response.get("responseCode");
        if ("000".equals(responseCode)) {
            return buildResponse(response, "Biller info fetched successfully", null);
        } else if ("001".equals(responseCode) || responseCode == null) {
            return buildResponse(response, null, "Failed to fetch Biller info");
        }

        return buildResponse(null, null, "Unexpected response code: " + responseCode);
    }

    @POST
    @Path("/transactionStatusReq")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response transactionStatusReq(@Context HttpServletRequest request, BbpsSchema.TransactionStatusReq body) {
        // Validate request
        String validationError = validateRequest(body);
        if (validationError != null) {
            return buildBadRequestResponse(validationError);
        }
    
        try {
            // Prepare the transaction status request map
            Map<String, Object> transactionStatusReq = new HashMap<>();
            transactionStatusReq.put("trackType", body.getReqId() != null ? "REQUEST_ID" : "TRANS_REF_ID");
            transactionStatusReq.put("trackValue", body.getReqId() != null ? "35CHRACTERSREQUESTID123456789012345" : "CC0175192009");
    
            // Call external service
            Map<String, Object> bbpsResponse = bbpsService.transactionStatusReq(transactionStatusReq);
            
            return handleResponse(bbpsResponse);
        } catch (JsonProcessingException e) {
            return buildErrorResponse("Error processing JSON: " + e.getMessage());
        } catch (Exception e) {
            return buildErrorResponse("An unexpected error occurred: " + e.getMessage());
        }
    }    
    @POST
    @Path("/complaintRegistrationReq")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response complaintRegistrationReq(@Context HttpServletRequest request, BbpsSchema.ComplaintRegistrationReq body) {
        // Validate request
        String validationError = validateRequest(body);
        if (validationError != null) {
            return buildBadRequestResponse(validationError);
        }
    
        try {
            // Prepare the complaint registration request map
            Map<String, Object> complaintRegistrationReq = new HashMap<>();
            complaintRegistrationReq.put("complaintType", "Transaction");
            complaintRegistrationReq.put("participationType", ""); // Consider filling this if possible
            complaintRegistrationReq.put("agentId", ""); // Consider filling this if possible
            complaintRegistrationReq.put("txnRefId", "CC017B090155");
            complaintRegistrationReq.put("billerId", ""); // Consider filling this if possible
            complaintRegistrationReq.put("complaintDesc", "Complaint initiated through API");
            complaintRegistrationReq.put("servReason", ""); // Consider filling this if possible
            complaintRegistrationReq.put("complaintDisposition", "Transaction Successful, account not updated");
    
            // Call external service
            Map<String, Object> bbpsResponse = bbpsService.complaintRegistrationReq(complaintRegistrationReq);
            
            return handleResponse(bbpsResponse);
        } catch (JsonProcessingException e) {
            return buildErrorResponse("Error processing JSON: " + e.getMessage());
        } catch (Exception e) {
            return buildErrorResponse("An unexpected error occurred: " + e.getMessage());
        }
    }    
    @POST
    @Path("/billValidationRequest")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response billValidationRequest(@Context HttpServletRequest request, BbpsSchema.BillValidationRequest body) {
        
        // Validate request
        String validationError = validateRequest(body);
        if (validationError != null) {
            return buildBadRequestResponse(validationError);
        }
    
        try {
            Map<String, Object> billValidationRequest = createBillValidationRequest();
            
            // Call external service
            Map<String, Object> bbpsResponse = bbpsService.billValidationRequest(billValidationRequest);
            return handleResponse(bbpsResponse);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return buildErrorResponse("Error processing request: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return buildErrorResponse("An unexpected error occurred: " + e.getMessage());
        }
    }
    
    private Map<String, Object> createBillValidationRequest() {
        Map<String, Object> billValidationRequest = new HashMap<>();
        
        // Add agentId and billerId
        billValidationRequest.put("agentId", "CC01CC01513515340681");
        billValidationRequest.put("billerId", "OTNS00005XXZ43");
    
        // Create inputParams map
        Map<String, Object> inputParams = new HashMap<>();
        List<Map<String, String>> inputList = new ArrayList<>();
    
        // Add input parameters
        addInputParameter(inputList, "a", "10");
        addInputParameter(inputList, "a b", "20");
        addInputParameter(inputList, "a b c", "30");
        addInputParameter(inputList, "a b c d", "40");
        addInputParameter(inputList, "a b c d e", "50");
    
        // Add input list to inputParams
        inputParams.put("input", inputList);
        billValidationRequest.put("inputParams", inputParams);
    
        return billValidationRequest;
    }
    
    private void addInputParameter(List<Map<String, String>> inputList, String paramName, String paramValue) {
        Map<String, String> input = new HashMap<>();
        input.put("paramName", paramName);
        input.put("paramValue", paramValue);
        inputList.add(input);
    }    

    @POST
    @Path("/complaintTrackingReq")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response complaintTrackingReq(@Context HttpServletRequest request, BbpsSchema.ComplaintTrackingReq body) {
        // Validate request
        String validationError = validateRequest(body);
        if (validationError != null) {
            return buildBadRequestResponse(validationError);
        }
    
        try {
            // Prepare the complaint tracking request map
            Map<String, Object> complaintTrackingReq = new HashMap<>();
            complaintTrackingReq.put("complaintType", "Transaction");
            complaintTrackingReq.put("complaintId", "XD1495446616192");
    
            // Call external service
            Map<String, Object> bbpsResponse = bbpsService.complaintTrackingReq(complaintTrackingReq);
            
            return handleResponse(bbpsResponse);
        } catch (JsonProcessingException e) {
            return buildErrorResponse("Error processing JSON: " + e.getMessage());
        } catch (Exception e) {
            return buildErrorResponse("An unexpected error occurred: " + e.getMessage());
        }
    }    
}
