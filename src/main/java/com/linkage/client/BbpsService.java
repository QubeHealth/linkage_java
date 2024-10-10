package com.linkage.client;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.constants.Constants;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;

public class BbpsService extends BaseServiceClient {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public BbpsService(LinkageConfiguration configuration) {
        super(configuration);
    }

    // Unified method to handle requests
    private ApiResponse<Object> processRequest(String endpoint, Map<String, Object> body) throws Exception {
        String url = buildApiUrlWithParameters(endpoint);
        MultivaluedHashMap<String, Object> header = createHeader();
        String encryptedRequest = encryptMapToJson(body, configuration.getBbpsEncryptionKey(), Constants.BbpsConstants.ALGORITHM);
        return this.networkCallExternalService(url, "POST", encryptedRequest, header);
    }

    private String buildApiUrlWithParameters(String endpoint) throws Exception {
        Map<String, String> apiParameters = prepareApiParameters(configuration.getBbpsAccessCode(), configuration.getBbpsInstituteId());
        return buildApiUrl(configuration.getBbpsUrl() + endpoint, apiParameters);
    }

    private MultivaluedHashMap<String, Object> createHeader() {
        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();
        header.putSingle("Content-Type", MediaType.TEXT_PLAIN);
        return header;
    }

    // API Request Methods
    public ApiResponse<Object> billerInfoRequest(Map<String, Object> body) throws Exception {
        return processRequest("extMdmCntrl/mdmRequestNew/xml", body);
    }

    public ApiResponse<Object> billFetchRequest(Map<String, Object> body) throws Exception {
        return processRequest("extBillCntrl/billFetchRequest/xml", body);
    }

    public ApiResponse<Object> billPaymentRequest(Map<String, Object> body) throws Exception {
        return processRequest("extBillPayCntrl/billPayRequest/xml", body);
    }

    public ApiResponse<Object> complaintRegistrationReq(Map<String, Object> body) throws Exception {
        return processRequest("extComplaints/register/xml", body);
    }

    public ApiResponse<Object> transactionStatusReq(Map<String, Object> body) throws Exception {
        return processRequest("transactionStatus/fetchInfo/xml", body);
    }

    public ApiResponse<Object> billValidationRequest(Map<String, Object> body) throws Exception {
        return processRequest("extBillValCntrl/billValidationRequest/xml", body);
    }

    public ApiResponse<Object> complaintTrackingReq(Map<String, Object> body) throws Exception {
        return processRequest("extComplaints/track/xml", body);
    }

    // Helper Methods
    private Map<String, String> prepareApiParameters(String accessCode, String instituteId) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("accessCode", accessCode);
        parameters.put("requestId", generateRequestId());
        parameters.put("ver", Constants.BbpsConstants.VERSION);
        parameters.put("instituteId", instituteId);
        return parameters;
    }

    private String generateRequestId() {
        StringBuilder randomChars = new StringBuilder(Constants.BbpsConstants.RANDOM_CHAR_LENGTH);
        
        // Generate random alphanumeric characters
        for (int i = 0; i < Constants.BbpsConstants.RANDOM_CHAR_LENGTH; i++) {
            int index = secureRandom.nextInt(Constants.BbpsConstants.ALPHANUMERIC.length());
            randomChars.append(Constants.BbpsConstants.ALPHANUMERIC.charAt(index));
        }

        // Append date and time components
        LocalDateTime now = LocalDateTime.now();
        return String.format("%s%d%03d%02d%02d", randomChars, now.getYear() % 10, now.getDayOfYear(), now.getHour(), now.getMinute());
    }

    private String buildApiUrl(String baseUrl, Map<String, String> parameters) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(baseUrl).append("?");
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            urlBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                      .append("=")
                      .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                      .append("&");
        }
        // Remove trailing '&'
        urlBuilder.setLength(urlBuilder.length() - 1);
        return urlBuilder.toString();
    }

    public String encryptMapToJson(Map<String, Object> map, String hexKey, String algorithm) throws Exception {
        // Convert hex string to byte array
        byte[] encryptionKey = hexStringToByteArray(hexKey);
        
        // Validate key size
        if (encryptionKey.length != 16) {
            throw new IllegalArgumentException("Encryption key must be 16 bytes (128 bits) for AES-128.");
        }

        // Convert Map to JSON string
        String jsonString = objectMapper.writeValueAsString(map);

        // Initialize cipher and secret key
        SecretKeySpec secretKey = new SecretKeySpec(encryptionKey, algorithm);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // Encrypt and encode to Base64
        byte[] encryptedBytes = cipher.doFinal(jsonString.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
