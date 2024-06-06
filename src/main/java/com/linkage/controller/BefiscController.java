package com.linkage.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.BefiscService;
import com.linkage.core.constants.HspKeywords;
import com.linkage.core.validations.GetBankDetailsByAccSchema;
import com.linkage.core.validations.GetNameByVpaSchema;
import com.linkage.core.validations.GetVpaByMobileSchema;
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

@Path("/api/befisc")
@Produces(MediaType.APPLICATION_JSON)
public class BefiscController extends BaseController {

    private BefiscService befiscService;

    public BefiscController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);

        this.befiscService = new BefiscService(configuration);
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

        List<Object> vpa = Helper.getDataFromMap(result.getData(), Arrays.asList("vpa"));
        List<Object> name = Helper.getDataFromMap(result.getData(), Arrays.asList("name"));
        List<Object> accountName = Helper.getDataFromMap(result.getData(), Arrays.asList("account_holder_name"));

        Map<String, Object> data = new HashMap<>();
        data.put("vpa", vpa.isEmpty() ? null : vpa.get(0));
        data.put("name", name.isEmpty() ? null : name.get(0));
        data.put("accountName", accountName.isEmpty() ? null : accountName.get(0));
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
    @Path("/getNameByVpa")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Map<String, Object>> getNameByVpa(
            GetNameByVpaSchema body) {
        Set<ConstraintViolation<GetNameByVpaSchema>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }
        ApiResponse<Map<String, Object>> result = this.befiscService.vpaAnalysis(body);
        List<Object> name = Helper.getDataFromMap(result.getData(), Arrays.asList("name"));
        List<Object> accountName = Helper.getDataFromMap(result.getData(), Arrays.asList("account_holder_name"));

        Map<String, Object> data = new HashMap<>();
        data.put("name", name.isEmpty() ? null : name.get(0));
        data.put("accountName", accountName.isEmpty() ? null : accountName.get(0));
        result.setData(data);

        return result;

    }

    @POST
    @Path("/getBankDetailsByAcc")
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

        return result;
    }

    @POST
    @Path("/validateName")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Map<String, Object>> validateName(
            GetVpaByMobileSchema body) {

        // Creating a HashMap for storing the UPI ids linked to the mobile number passed
        // in the schema
        Map<String, Object> data = new HashMap();
        data = getMultipleUpi(body).getData();

        // Moving those UPI ids into an arrayList so that they can be iterated through
        ArrayList<String> x = (ArrayList<String>) data.get("upi");

        // Calling the arrayList with hspKeywords
        List<String> iterator = HspKeywords.hspKeys();

        // iterating through each UPI id
        for (int i = 0; i < x.size(); i++) {

            x.get(i);

            // Getting the names that are associated with the UPI id
            GetNameByVpaSchema temp = new GetNameByVpaSchema();
            temp.setVpa(x.get(i));

            // moving the names into a hashmap
            Map<String, Object> names = new HashMap();
            names = getNameByVpa(temp).getData();

            // From a hashmap to two individual Strings
            String account_name = names.get("accountName").toString();
            String name = names.get("name").toString();

            // Iterating through the arrayList for each hspKeyword
            for (int j = 0; j < iterator.size(); j++) {
                if (account_name.indexOf(iterator.get(j)) != -1) {

                    // Creating a hashMap with the requested data since the keyword exists in the
                    // bank_account_name
                    Map<String, Object> requestedData = new HashMap<>();
                    requestedData.put("vpa", temp.getVpa());
                    requestedData.put("bank_account_name", account_name);
                    requestedData.put("keyword_hit", iterator.get(j));
                    requestedData.put("name", name);
                    ApiResponse success = new ApiResponse<Map<String, Object>>(true,
                            "The given name has been validated with keyword " + iterator.get(j), requestedData);

                    return success;
                }
                if (name.indexOf(iterator.get(j)) != -1) {

                    // Creating a hashMap with the requested data since the keyword exists in the
                    // name
                    Map<String, Object> requestedData = new HashMap<>();
                    requestedData.put("vpa", temp.getVpa());
                    requestedData.put("bank_account_name", account_name);
                    requestedData.put("keyword_hit", iterator.get(j));
                    requestedData.put("name", name);
                    ApiResponse success = new ApiResponse<Map<String, Object>>(true,
                            "The given name has been validated with keyword " + iterator.get(j), requestedData);

                    return success;
                }
            }

        }
        // If the name is not found to be a part of either name of account name, then we
        // reutrn the failure message
        ApiResponse failure = new ApiResponse<Map<String, Object>>(false, "The given name has not been validated",
                null);
        return failure;
    }

}