package com.linkage.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.HrmsService;

import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/hrms")
public class HrmsController extends BaseController {

    private final HrmsService hrmsService;

    public HrmsController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        this.hrmsService = new HrmsService(configuration);
    }

    private Response buildResponse(Response.Status status, ApiResponse<?> apiResponse) {
        return Response.status(status).entity(apiResponse).build();
    }

    @SuppressWarnings("unchecked")
    private Optional<String> getAccessToken() {
        // Retrieve token from hrmsService
        ApiResponse<Object> tokenResponse = hrmsService.getToken();
        if (tokenResponse == null || tokenResponse.getData() == null) {
            return Optional.empty();
        }

        // Extract access_token
        Map<String, Object> responseBody = (Map<String, Object>) tokenResponse.getData();
        return Optional.ofNullable((String) responseBody.get("access_token"));
    }

    private ApiResponse<Object> fetchEmployeeData(String accessToken) {
        // Fetch employee data using the access token
        return hrmsService.getEmployeeData(accessToken);
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("/getEmployeeData")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getEmployeeData() {
        try {
            // Get access token
            Optional<String> accessTokenOpt = getAccessToken();

            if (!accessTokenOpt.isPresent()) {
                return buildResponse(Response.Status.INTERNAL_SERVER_ERROR,
                        new ApiResponse<>(false, "Access token not found or invalid", null));
            }

            String accessToken = accessTokenOpt.get();
            System.out.println("Access Token: " + accessToken);

            // Fetch employee data
            ApiResponse<Object> employeeData = fetchEmployeeData(accessToken);

            if (employeeData != null && employeeData.getData() instanceof Map) {
                Map<String, Object> dataMap = (Map<String, Object>) employeeData.getData();
                Map<String, Object> root = (Map<String, Object>) dataMap.get("root");
                Map<String, Object> employeeMaster = (Map<String, Object>) root.get("EmployeeMaster");
                List<Map<String, Object>> employeeMasterData = (List<Map<String, Object>>) employeeMaster
                        .get("EmployeeMasterData");

                return buildResponse(Response.Status.OK,
                        new ApiResponse<>(true, "Employee data fetched successfully", employeeMasterData));
            } else {
                return buildResponse(Response.Status.INTERNAL_SERVER_ERROR,
                        new ApiResponse<>(false, "Failed to fetch employee data", null));
            }

        } catch (Exception e) {
            // Log the exception (replace with proper logger in real use case)
            e.printStackTrace();
            return buildResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    new ApiResponse<>(false, "An error occurred while processing the request", null));
        }
    }
}
