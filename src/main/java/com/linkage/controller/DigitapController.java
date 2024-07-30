package com.linkage.controller;

import java.util.Map;
import java.util.Set;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.DigitapService;
import com.linkage.core.validations.DigitapSchema.GetCreditBureau;
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

@Path("/api/digitap")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class DigitapController extends BaseController {

    private DigitapService digitapService;

    public DigitapController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        this.digitapService = new DigitapService(configuration);
    }

    @POST
    @Path("/getCreditBureau")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> referreeInviteMessage(@Context HttpServletRequest request,
            GetCreditBureau body) {
        Set<ConstraintViolation<GetCreditBureau>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }

        ApiResponse<Object> digitapResponse = this.digitapService.getCreditReport(body);

        if (!digitapResponse.getStatus()) {
            return digitapResponse;
        }

        Map<String, Object> creditResponse = (Map<String, Object>) digitapResponse.getData();

        if (creditResponse.get("result_code") != null && !creditResponse.get("result_code").equals(101)) {
            return new ApiResponse<>(false, creditResponse.get("message").toString(), creditResponse);
        }

        Map<String, Object> report = (Map<String, Object>) creditResponse.get("result");

        String xmlReport = Helper.jsonToXML(Helper.toJsonString(report.get("result_json")));

        creditResponse.put("result", xmlReport);

        return new ApiResponse<>(true, "Bureau fetch success", creditResponse);

    }
}