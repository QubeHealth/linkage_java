package com.linkage.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipException;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import org.threeten.bp.ZonedDateTime;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.DigitapService;
import com.linkage.core.validations.DigitapSchema.GetCreditBureau;
import com.linkage.core.validations.DigitapSchema.SendAadharOtp;
import com.linkage.core.validations.DigitapSchema.VerifyAadharOtp;
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
    public Response sendAadharOtp(@Context HttpServletRequest request, SendAadharOtp body) {

        // Validate the request body
        Set<ConstraintViolation<SendAadharOtp>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            return response(Response.Status.BAD_REQUEST, new ApiResponse<>(false, errorMessage, null));
        }

        // Call the service to send the OTP
        ApiResponse<Object> digitapResponse = this.digitapService.sendAadharOtp(body);
        if (!digitapResponse.getStatus()) {
            return response(Response.Status.INTERNAL_SERVER_ERROR, digitapResponse);
        }

        // Cast the response data to the appropriate type
        Map<String, Object> digitapData = (Map<String, Object>) digitapResponse.getData();
        // Store the result in Redis
        RedisClient.set(body.getUniqueId(), Helper.toJsonString(digitapData));

        // Return success response
        return response(Response.Status.OK,
                new ApiResponse<>(true, "Send OTP success", Map.of("access_key", body.getUniqueId())));
    }

    @POST
    @Path("/verifyAadharOtp")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verifyAadharOtp(@Context HttpServletRequest request, VerifyAadharOtp body) {

        // Validate the request body
        Set<ConstraintViolation<VerifyAadharOtp>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            return response(Response.Status.BAD_REQUEST, new ApiResponse<>(false, errorMessage, null));
        }

        Map<String, Object> digitapData = Helper.jsonStringToMap(RedisClient.get(body.getAccessKey()));
        if (digitapData.isEmpty()) {
            return response(Response.Status.INTERNAL_SERVER_ERROR,
                    new ApiResponse<>(false, "No data found for access key", null));
        }

        Map<String, Object> model = (Map<String, Object>) digitapData.get("model");

        Map<String, Object> map = new HashMap<>();
        map.put("shareCode", "1234");
        map.put("otp", body.getOtp());
        map.put("transactionId", model.get("transactionId"));
        map.put("codeVerifier", model.get("codeVerifier"));
        map.put("fwdp", model.get("fwdp"));
        map.put("validateXml", true);

        // Call the service to send the OTP
        ApiResponse<Object> digitapResponse = this.digitapService.verifyAadharOtp(map);
        if (!digitapResponse.getStatus()) {
            return response(Response.Status.INTERNAL_SERVER_ERROR, digitapResponse);
        }

        // Cast the response data to the appropriate type
        Map<String, Object> digitapRes = (Map<String, Object>) digitapResponse.getData();

        Helper.toJsonString(digitapRes);
        // Return success response
        return response(Response.Status.OK,
                new ApiResponse<>(true, "aadhar verification success", digitapResponse.getData()));
    }

    public void extractAadhar(String fileUrl) {
        String downloadLocation = "downloaded.zip"; // Local path for the downloaded file
        String password = "yourPassword"; // Password for the ZIP file

        try {
            // Step 1: Download the ZIP file from the URL
            downloadFile(fileUrl, downloadLocation);

            // Step 2: Extract the password-protected ZIP file contents
            extractZipFile(downloadLocation, password);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Function to download file from a URL
    private static void downloadFile(String fileUrl, String destination) throws IOException {
        try (InputStream in = new URL(fileUrl).openStream()) {
            Files.copy(in, Paths.get(destination));
            System.out.println("File downloaded successfully: " + destination);
        }
    }

    // Function to extract a password-protected ZIP file using Zip4j
    private static void extractZipFile(String zipFilePath, String password) throws IOException, ZipException {
        ZipFile zipFile = new ZipFile(zipFilePath);

        if (zipFile.isEncrypted()) {
            zipFile.setPassword(password.toCharArray());
        }

        // Extract all files from the ZIP to the "extracted_files" folder
        String extractFolder = "extracted_files";
        zipFile.extractAll(extractFolder);

        System.out.println("Extraction complete.");

        // Step 3: Find and print XML file content
        File folder = new File(extractFolder);
        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".xml")) {
                System.out.println("XML File found: " + file.getName());
                printXmlFileContent(file);
            }
        }

        zipFile.close();
    }

    // Function to print the content of an XML file
    private static void printXmlFileContent(File xmlFile) throws IOException {
        System.out.println("Reading XML file: " + xmlFile.getName());
        try (BufferedReader reader = new BufferedReader(new FileReader(xmlFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}