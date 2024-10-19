package com.linkage.controller;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.DigitapService;
import com.linkage.core.validations.DigitapSchema.GetCreditBureau;
import com.linkage.core.validations.DigitapSchema.SendAadharOtp;
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

@Path("/api/digitap")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class DigitapController extends BaseController {

    private DigitapService digitapService;

    public DigitapController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        this.digitapService = new DigitapService(configuration);
    }

    private Response response(Response.Status status, Object data) {
        return Response.status(status).entity(data).build();
    }

    @POST
    @Path("/getCreditBureau")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response referreeInviteMessage(@Context HttpServletRequest request,
            GetCreditBureau body) {

        Set<ConstraintViolation<GetCreditBureau>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return response(Response.Status.BAD_REQUEST, new ApiResponse<>(false, errorMessage, null));
        }

        try {

            if (body.getDeviceIp() == null || body.getDeviceIp().isBlank() || body.getDeviceIp().contains("0.0.0.0")
                    || Arrays.asList("127.0.0.0", "127.0.0.1", "127.0.1.1").contains(body.getDeviceIp())) {
                InetAddress myIP = InetAddress.getLocalHost();
                String ipv4Address = myIP.getHostAddress();
                body.setDeviceIp(ipv4Address);
            }

            ApiResponse<Object> digitapResponse = this.digitapService.getCreditReport(body);

            if (!digitapResponse.getStatus()) {
                return response(Response.Status.FORBIDDEN, digitapResponse);
            }

            Map<String, Object> creditResponse = (Map<String, Object>) digitapResponse.getData();

            if (creditResponse.get("result_code") != null && !creditResponse.get("result_code").equals(101)) {
                if (creditResponse.get("result_code").equals(102)) {
                    return response(Response.Status.OK,
                            new ApiResponse<>(true, creditResponse.get("message").toString(), creditResponse));
                }
                return response(Response.Status.FORBIDDEN,
                        new ApiResponse<>(false, creditResponse.get("message").toString(), creditResponse));

            }

            Map<String, Object> report = (Map<String, Object>) creditResponse.get("result");

            String xmlReport = Helper.downloadXmlAsString(report.getOrDefault("result_xml", "").toString());

            creditResponse.put("result", xmlReport);

            return response(Response.Status.OK,
                    new ApiResponse<>(true, "Bureau fetch success", creditResponse));

        } catch (Exception e) {
            return response(Response.Status.INTERNAL_SERVER_ERROR,
                    new ApiResponse<>(false, e.getMessage(), e));
        }

    }

    @POST
    @Path("/sendAadharOtp")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendAadharOtp(@Context HttpServletRequest request,
            SendAadharOtp body) {

        Set<ConstraintViolation<SendAadharOtp>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return response(Response.Status.BAD_REQUEST, new ApiResponse<>(false, errorMessage, null));
        }

        ApiResponse<Object> digitapResponse = this.digitapService.sendAadharOtp(body);

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("code", "200");
        jsonMap.put("msg", "success");

        // Create the "uidaiResponse" map
        Map<String, Object> uidaiResponseMap = new HashMap<>();
        uidaiResponseMap.put("message", "OTP Generation Successful");
        uidaiResponseMap.put("sessionActive", "true");
        uidaiResponseMap.put("status", "true");

        // Create the "model" map
        Map<String, Object> modelMap = new HashMap<>();
        modelMap.put("uidaiResponse", uidaiResponseMap);
        modelMap.put("transactionId", "953226395193673817");
        modelMap.put("fwdp",
                "8PZmrVbW0BdICB1lTlXi_lQ2hNEJA2MHxPwBFRgQ34-Qn6nEU_V84_nB6Xhy7JioKQROdagXMYH20nNPQJjQ3HVie5M3t1SdP1TiTiO9GeSdjZhTBBIwmLJm4g6i9HiNVZ9AXNe4lkDZB-TbkhGiCcuvoJXP9c1v5n90VvcAuwvCcjhsxKIOf8l7uhcnQ0rrHw");
        modelMap.put("codeVerifier",
                "mN1h17vnLnmkf4XKq8xB1mWIacEBVqyLOB0Weo8WRiO1UHpUlnoksx7V9d7Gs1vqi5rhXSh9DbtdcuuDsTG6nx84VAf5VbqWlozQO7ISTmGD96aRo4pQH0lB88bnTLev");

        jsonMap.put("model", modelMap);

        digitapResponse.setStatus(true);
        digitapResponse.setData(jsonMap);

        if (!digitapResponse.getStatus()) {
            return response(Response.Status.FORBIDDEN, digitapResponse);
        }

        String redisKey = body.getUserId() + Helper.getCurrentDate("yyyyMMddHHmmss");

        System.out.println(redisKey);

        RedisClient.set(redisKey, Helper.toJsonString(modelMap));

        Map<String, Object> res = Helper.jsonStringToMap(RedisClient.get(redisKey));

        System.out.println(res);
        return response(Response.Status.OK, digitapResponse);

    }
}