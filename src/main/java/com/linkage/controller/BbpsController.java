package com.linkage.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.BbpsService;
import com.linkage.core.validations.BbpsSchema;
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
            ApiResponse<Object> bbpsResponse = bbpsService.billerInfoRequest(jsonMap);
            return handleResponse(bbpsResponse);
        } catch (JsonProcessingException e) {
            return buildErrorResponse("Error processing JSON: " + e.getMessage());
        } catch (Exception e) {
            return buildErrorResponse("An unexpected error occurred: " + e.getMessage());
        }
    }

    private Response handleResponse(ApiResponse<Object> response) {
        if (isResponseError(response)) {
            return buildResponse(null, null, "Failed to fetch Biller Information");
        }
        return buildResponse(response.getData(), "Biller Info fetched successfully", null);
    }

    private boolean isResponseError(ApiResponse<Object> response) {
        return response.getData() == null ||
               response.getData().toString().contains("Unauthorized Access Detected") ||
               response.getData().toString().contains("<responseCode>001</responseCode>");
    }

    private Response buildErrorResponse(String errorMessage) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiResponse<>(false, "Error Occurred", errorMessage))
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

    
    // @POST
    // @Path("/billFetchRequest")
    // @Produces(MediaType.APPLICATION_JSON)
    // @Consumes(MediaType.APPLICATION_JSON)
    // public Response billFetchRequest(@Context HttpServletRequest request, BbpsSchema.BillFetchRequest body) {

    //     // Validate request
    //     String validationError = validateRequest(body);
    //     if (validationError != null) {
    //         return buildBadRequestResponse(validationError);
    //     }

    //     try {

    //         ObjectMapper objectMapper = new ObjectMapper();

    //         // Create a Map to represent the JSON object
    //         Map<String, Object> jsonObject = new HashMap<>();
    //         jsonObject.put("agentId", "CC01CC02INT000000001");

    //         Map<String, String> agentDeviceInfo = new HashMap<>();
    //         agentDeviceInfo.put("ip", "192.168.2.183");
    //         agentDeviceInfo.put("initChannel", "INT");
    //         agentDeviceInfo.put("mac", "01-23-45-67-89-ab");
    //         jsonObject.put("agentDeviceInfo", agentDeviceInfo);

    //         Map<String, String> customerInfo = new HashMap<>();
    //         customerInfo.put("customerMobile", "9999999999");
    //         customerInfo.put("customerEmail", "customer@email.com");
    //         customerInfo.put("customerAdhaar", "545450308010");
    //         customerInfo.put("customerPan", "HJAUI4333H");
    //         jsonObject.put("customerInfo", customerInfo);

    //         jsonObject.put("billerId", "RAJI00000MANVN");

    //         Map<String, Map<String, String>> inputParams = new HashMap<>();
    //         Map<String, String> input = new HashMap<>();
    //         input.put("paramName", "UserName");
    //         input.put("paramValue", "rushal1022");
    //         inputParams.put("input", input);
    //         jsonObject.put("inputParams", inputParams);

    //         // Convert the Map to JSON string
    //         String jsonString = objectMapper.writeValueAsString(jsonObject);
    //         // Convert request body to JSON, then to XML
    //         String xmlRequest = Helper.convertJsonToXML(jsonString, "billFetchRequest");

    //         // Call external service
    //         ApiResponse<Object> bppsResponse = bbpsService.billFetchRequest(xmlRequest);
    //         if (bppsResponse.getData() == null) {
    //             return buildResponse(null, null, "Failed to fetch the bill");
    //         }

    //         // Process XML response to JSON and map
    //         Map<String, Object> resultMap = processXmlResponse(bppsResponse.getData().toString());
    //         return buildResponse(resultMap, "Bill fetched successfully", null);

    //     } catch (JsonProcessingException e) {
    //         e.printStackTrace();
    //         return buildErrorResponse(e.getMessage());
    //     }
    // }

    // @POST
    // @Path("/billPaymentRequest")
    // @Produces(MediaType.APPLICATION_JSON)
    // @Consumes(MediaType.APPLICATION_JSON)
    // public Response billPaymentRequest(@Context HttpServletRequest request, BbpsSchema.BillPaymentRequest body) {

    //     // Validate request
    //     String validationError = validateRequest(body);
    //     if (validationError != null) {
    //         return buildBadRequestResponse(validationError);
    //     }

    //     try {

    //         ObjectMapper objectMapper = new ObjectMapper();

    //         Map<String, Object> billPaymentRequest = new HashMap<>();
    //         billPaymentRequest.put("agentId", "CC01CC01513515340681");
    //         billPaymentRequest.put("billerAdhoc", true);

    //         // Agent device info
    //         Map<String, String> agentDeviceInfo = new HashMap<>();
    //         agentDeviceInfo.put("ip", "192.168.2.73");
    //         agentDeviceInfo.put("initChannel", "AGT");
    //         agentDeviceInfo.put("mac", "01-23-45-67-89-ab");
    //         billPaymentRequest.put("agentDeviceInfo", agentDeviceInfo);

    //         // Customer info
    //         Map<String, String> customerInfo = new HashMap<>();
    //         customerInfo.put("customerMobile", "9898990084");
    //         customerInfo.put("customerEmail", "");
    //         customerInfo.put("customerAdhaar", "");
    //         customerInfo.put("customerPan", "");
    //         billPaymentRequest.put("customerInfo", customerInfo);

    //         billPaymentRequest.put("billerId", "OTME00005XXZ43");

    //         // Input parameters
    //         Map<String, Object> inputParams = new HashMap<>();
    //         List<Map<String, String>> inputs = List.of(
    //                 createInput("a", "10"),
    //                 createInput("a b", "20"),
    //                 createInput("a b c", "30"),
    //                 createInput("a b c d", "40"),
    //                 createInput("a b c d e", "50"));
    //         inputParams.put("input", inputs);
    //         billPaymentRequest.put("inputParams", inputParams);

    //         // Biller response
    //         Map<String, Object> billerResponse = new HashMap<>();
    //         billerResponse.put("billAmount", 100000);
    //         billerResponse.put("billDate", "2015-06-14");
    //         billerResponse.put("billNumber", "12303");
    //         billerResponse.put("billPeriod", "June");
    //         billerResponse.put("customerName", "BBPS");
    //         billerResponse.put("dueDate", "2015-06-20");

    //         List<Map<String, Object>> amountOptions = List.of(
    //                 createAmountOption("Late Payment Fee", 40),
    //                 createAmountOption("Fixed Charges", 50),
    //                 createAmountOption("Additional Charges", 60));
    //         billerResponse.put("amountOptions", Map.of("option", amountOptions));
    //         billPaymentRequest.put("billerResponse", billerResponse);

    //         // Additional info
    //         Map<String, Object> additionalInfo = new HashMap<>();
    //         List<Map<String, String>> additionalInfos = List.of(
    //                 createInfo("a", "10"),
    //                 createInfo("a b", "20"),
    //                 createInfo("a b c", "30"),
    //                 createInfo("a b c d", "40"));
    //         additionalInfo.put("info", additionalInfos);
    //         billPaymentRequest.put("additionalInfo", additionalInfo);

    //         // Amount info
    //         Map<String, Object> amountInfo = new HashMap<>();
    //         amountInfo.put("amount", 100000);
    //         amountInfo.put("currency", "356");
    //         amountInfo.put("custConvFee", 0);
    //         amountInfo.put("amountTags", "");
    //         billPaymentRequest.put("amountInfo", amountInfo);

    //         // Payment method
    //         Map<String, Object> paymentMethod = new HashMap<>();
    //         paymentMethod.put("paymentMode", "Cash");
    //         paymentMethod.put("quickPay", "N");
    //         paymentMethod.put("splitPay", "N");
    //         billPaymentRequest.put("paymentMethod", paymentMethod);

    //         // Payment info
    //         Map<String, Object> paymentInfo = new HashMap<>();
    //         List<Map<String, String>> paymentInfos = List.of(
    //                 createInfo("Remarks", "Received"));
    //         paymentInfo.put("info", paymentInfos);
    //         billPaymentRequest.put("paymentInfo", paymentInfo);

    //         // Convert the Map to JSON string
    //         String jsonString = objectMapper.writeValueAsString(billPaymentRequest);
    //         // Convert request body to JSON, then to XML
    //         String xmlRequest = Helper.convertJsonToXML(jsonString, "billPaymentRequest");

    //         // Call external service
    //         ApiResponse<Object> bppsResponse = bbpsService.billPaymentRequest(xmlRequest);
    //         if (bppsResponse.getData() == null) {
    //             return buildResponse(null, null, "Failed to fetch the bill");
    //         }

    //         // Process XML response to JSON and map
    //         Map<String, Object> resultMap = processXmlResponse(bppsResponse.getData().toString());
    //         return buildResponse(resultMap, "Bill fetched successfully", null);

    //     } catch (JsonProcessingException e) {
    //         e.printStackTrace();
    //         return buildErrorResponse(e.getMessage());
    //     }
    // }

    // @POST
    // @Path("/transactionStatusReq")
    // @Produces(MediaType.APPLICATION_JSON)
    // @Consumes(MediaType.APPLICATION_JSON)
    // public Response transactionStatusReq(@Context HttpServletRequest request, BbpsSchema.TransactionStatusReq body) {

    //     // Validate request
    //     String validationError = validateRequest(body);
    //     if (validationError != null) {
    //         return buildBadRequestResponse(validationError);
    //     }

    //     try {

    //         ObjectMapper objectMapper = new ObjectMapper();

    //         Map<String, Object> transactionStatusReq = new HashMap<>();

    //         if (body.getReqId() != null) {
    //             // When req_id is true
    //             transactionStatusReq.put("trackType", "REQUEST_ID");
    //             transactionStatusReq.put("trackValue", "35CHRACTERSREQUESTID123456789012345");
    //         } else {
    //             // When req_id is false
    //             transactionStatusReq.put("trackType", "TRANS_REF_ID");
    //             transactionStatusReq.put("trackValue", "CC0175192009");
    //         }

    //         // Convert the Map to JSON string
    //         String jsonString = objectMapper.writeValueAsString(transactionStatusReq);
    //         // Convert request body to JSON, then to XML
    //         String xmlRequest = Helper.convertJsonToXML(jsonString, "transactionStatusReq");

    //         // Call external service
    //         ApiResponse<Object> bppsResponse = bbpsService.transactionStatusReq(xmlRequest);
    //         if (bppsResponse.getData() == null) {
    //             return buildResponse(null, null, "Failed to fetch the bill");
    //         }

    //         // Process XML response to JSON and map
    //         Map<String, Object> resultMap = processXmlResponse(bppsResponse.getData().toString());
    //         return buildResponse(resultMap, "Bill fetched successfully", null);

    //     } catch (JsonProcessingException e) {
    //         e.printStackTrace();
    //         return buildErrorResponse(e.getMessage());
    //     }
    // }

    // @POST
    // @Path("/complaintRegistrationReq")
    // @Produces(MediaType.APPLICATION_JSON)
    // @Consumes(MediaType.APPLICATION_JSON)
    // public Response complaintRegistrationReq(@Context HttpServletRequest request,
    //         BbpsSchema.ComplaintRegistrationReq body) {

    //     // Validate request
    //     String validationError = validateRequest(body);
    //     if (validationError != null) {
    //         return buildBadRequestResponse(validationError);
    //     }

    //     try {

    //         ObjectMapper objectMapper = new ObjectMapper();

    //         Map<String, Object> complaintRegistrationReq = new HashMap<>();
    //         complaintRegistrationReq.put("complaintType", "Transaction");
    //         complaintRegistrationReq.put("participationType", "");
    //         complaintRegistrationReq.put("agentId", "");
    //         complaintRegistrationReq.put("txnRefId", "CC017B090155");
    //         complaintRegistrationReq.put("billerId", "");
    //         complaintRegistrationReq.put("complaintDesc", "Complaint initiated through API");
    //         complaintRegistrationReq.put("servReason", "");
    //         complaintRegistrationReq.put("complaintDisposition", "Transaction Successful, account not updated");

    //         // Convert the Map to JSON string
    //         String jsonString = objectMapper.writeValueAsString(complaintRegistrationReq);
    //         // Convert request body to JSON, then to XML
    //         String xmlRequest = Helper.convertJsonToXML(jsonString, "complaintRegistrationReq");

    //         // Call external service
    //         ApiResponse<Object> bppsResponse = bbpsService.complaintRegistrationReq(xmlRequest);
    //         if (bppsResponse.getData() == null) {
    //             return buildResponse(null, null, "Failed to fetch the bill");
    //         }

    //         // Process XML response to JSON and map
    //         Map<String, Object> resultMap = processXmlResponse(bppsResponse.getData().toString());
    //         return buildResponse(resultMap, "Bill fetched successfully", null);

    //     } catch (JsonProcessingException e) {
    //         e.printStackTrace();
    //         return buildErrorResponse(e.getMessage());
    //     }
    // }

    // @POST
    // @Path("/billValidationRequest")
    // @Produces(MediaType.APPLICATION_JSON)
    // @Consumes(MediaType.APPLICATION_JSON)
    // public Response billValidationRequest(@Context HttpServletRequest request, BbpsSchema.BillValidationRequest body) {

    //     // Validate request
    //     String validationError = validateRequest(body);
    //     if (validationError != null) {
    //         return buildBadRequestResponse(validationError);
    //     }

    //     try {

    //         ObjectMapper objectMapper = new ObjectMapper();

    //         List<Map<String, String>> inputs = new ArrayList<>();

    //         // Adding inputs to the list
    //         inputs.add(createInput("a", "10"));
    //         inputs.add(createInput("a b", "20"));
    //         inputs.add(createInput("a b c", "30"));
    //         inputs.add(createInput("a b c d", "40"));
    //         inputs.add(createInput("a b c d e", "50"));

    //         // Create the inputParams map
    //         Map<String, Object> inputParams = new HashMap<>();
    //         inputParams.put("input", inputs);

    //         // Create the billValidationRequest map
    //         Map<String, Object> billValidationRequest = new HashMap<>();
    //         billValidationRequest.put("agentId", "CC01CC01513515340681");
    //         billValidationRequest.put("billerId", "OTNS00005XXZ43");
    //         billValidationRequest.put("inputParams", inputParams);

    //         // Convert the Map to JSON string
    //         String jsonString = objectMapper.writeValueAsString(billValidationRequest);
    //         // Convert request body to JSON, then to XML
    //         String xmlRequest = Helper.convertJsonToXML(jsonString, "billValidationRequest");

    //         // Call external service
    //         ApiResponse<Object> bppsResponse = bbpsService.billValidationRequest(xmlRequest);
    //         if (bppsResponse.getData() == null) {
    //             return buildResponse(null, null, "Failed to fetch the bill");
    //         }

    //         // Process XML response to JSON and map
    //         Map<String, Object> resultMap = processXmlResponse(bppsResponse.getData().toString());
    //         return buildResponse(resultMap, "Bill fetched successfully", null);

    //     } catch (JsonProcessingException e) {
    //         e.printStackTrace();
    //         return buildErrorResponse(e.getMessage());
    //     }
    // }

    // @POST
    // @Path("/complaintTrackingReq")
    // @Produces(MediaType.APPLICATION_JSON)
    // @Consumes(MediaType.APPLICATION_JSON)
    // public Response complaintTrackingReq(@Context HttpServletRequest request, BbpsSchema.ComplaintTrackingReq body) {

    //     // Validate request
    //     String validationError = validateRequest(body);
    //     if (validationError != null) {
    //         return buildBadRequestResponse(validationError);
    //     }

    //     try {

    //         ObjectMapper objectMapper = new ObjectMapper();

    //         Map<String, Object> complaintTrackingReq = new HashMap<>();
    //         complaintTrackingReq.put("complaintType", "Transaction");
    //         complaintTrackingReq.put("complaintId", "XD1495446616192");
    //         // Convert the Map to JSON string
    //         String jsonString = objectMapper.writeValueAsString(complaintTrackingReq);
    //         // Convert request body to JSON, then to XML
    //         String xmlRequest = Helper.convertJsonToXML(jsonString, "complaintTrackingReq");

    //         // Call external service
    //         ApiResponse<Object> bppsResponse = bbpsService.complaintTrackingReq(xmlRequest);
    //         if (bppsResponse.getData() == null) {
    //             return buildResponse(null, null, "Failed to fetch the bill");
    //         }

    //         // Process XML response to JSON and map
    //         Map<String, Object> resultMap = processXmlResponse(bppsResponse.getData().toString());
    //         return buildResponse(resultMap, "Bill fetched successfully", null);

    //     } catch (JsonProcessingException e) {
    //         e.printStackTrace();
    //         return buildErrorResponse(e.getMessage());
    //     }
    // }

}
