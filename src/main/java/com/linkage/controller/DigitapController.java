package com.linkage.controller;

import java.net.InetAddress;
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

            // Retrieve the client IP address
            String clientIp = request.getHeader("X-Forwarded-For");

            if (clientIp != null) {
                // X-Forwarded-For may contain a comma-separated list of IPs
                clientIp = clientIp.split(",")[0];
            }

            if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
                clientIp = request.getHeader("X-Real-IP");
            }
            if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
                clientIp = request.getRemoteAddr();
            }

            System.out.println("\n\n IP ADDDRESS => " + clientIp);

            InetAddress myIP = InetAddress.getLocalHost();
            String ipv4Address = myIP.getHostAddress();
            body.setDeviceIp(ipv4Address);

            ApiResponse<Object> digitapResponse = this.digitapService.getCreditReport(body);

            if (!digitapResponse.getStatus()) {
                return response(Response.Status.FORBIDDEN, digitapResponse);
            }

            Map<String, Object> creditResponse = (Map<String, Object>) digitapResponse.getData();

            if (creditResponse.get("result_code") != null && !creditResponse.get("result_code").equals(101)) {
                return response(Response.Status.FORBIDDEN,
                        new ApiResponse<>(false, creditResponse.get("message").toString(), creditResponse));

            }

            Map<String, Object> report = (Map<String, Object>) creditResponse.get("result");

            String xmlReport = Helper.jsonToXML(Helper.toJsonString(report.get("result_json")));

            creditResponse.put("result", xmlReport);

            return response(Response.Status.OK,
                    new ApiResponse<>(true, "Bureau fetch success", creditResponse));

        } catch (Exception e) {
            return response(Response.Status.INTERNAL_SERVER_ERROR,
                    new ApiResponse<>(false, e.getMessage(), e));
        }

    }
}