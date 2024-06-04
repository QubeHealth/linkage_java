package com.linkage.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.BefiscService;
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
    public ApiResponse<Map<String, Object>> getVpaByMobile(@Context HttpServletRequest request,
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
    public ApiResponse<Map<String,Object>> getMultipleUpi(@Context HttpServletRequest request,
            GetVpaByMobileSchema body){
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
    public ApiResponse<Map<String,Object>> getNameByVpa(@Context HttpServletRequest request,
            GetNameByVpaSchema body){
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


   

}
