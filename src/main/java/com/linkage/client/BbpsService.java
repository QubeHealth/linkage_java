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
    private  Map<String, Object> processRequest(String endpoint, Map<String, Object> body) throws Exception {
        String url = buildApiUrlWithParameters(endpoint);
        MultivaluedHashMap<String, Object> header = createHeader();
        String encryptedRequest = encryptMapToJson(body, configuration.getBbpsEncryptionKey(), Constants.BbpsConstants.ALGORITHM);

        ApiResponse<Object> result = this.networkCallExternalService(url, "POST", encryptedRequest, header);
        if (result.getData() == null) {
            return null;
        }

        Map<String, Object> apiData = decryptJsonToMap(result.getData().toString(), configuration.getBbpsEncryptionKey(), Constants.BbpsConstants.ALGORITHM);
        return apiData;
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
    public  Map<String, Object> billerInfoRequest(Map<String, Object> body) throws Exception {
        return processRequest("extMdmCntrl/mdmRequestNew/xml", body);
    }

    public  Map<String, Object> billFetchRequest(Map<String, Object> body) throws Exception {
        return processRequest("extBillCntrl/billFetchRequest/xml", body);
    }

    public  Map<String, Object> billPaymentRequest(Map<String, Object> body) throws Exception {
        return processRequest("extBillPayCntrl/billPayRequest/xml", body);
    }

    public  Map<String, Object> complaintRegistrationReq(Map<String, Object> body) throws Exception {
        return processRequest("extComplaints/register/xml", body);
    }

    public  Map<String, Object> transactionStatusReq(Map<String, Object> body) throws Exception {
        return processRequest("transactionStatus/fetchInfo/xml", body);
    }

    public  Map<String, Object> billValidationRequest(Map<String, Object> body) throws Exception {
        return processRequest("extBillValCntrl/billValidationRequest/xml", body);
    }

    public  Map<String, Object> complaintTrackingReq(Map<String, Object> body) throws Exception {
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

    @SuppressWarnings("unchecked")
    public Map<String, Object> decryptJsonToMap(String encryptedData, String hexKey, String algorithm) {
        try {
            byte[] encryptionKey = hexStringToByteArray(hexKey);
    
            // Validate key size
            if (encryptionKey.length != 16) {
                throw new IllegalArgumentException("Encryption key must be 16 bytes (128 bits) for AES-128.");
            }
    
            // Decode the Base64 encoded string
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
    
            // Initialize cipher and secret key
            SecretKeySpec secretKey = new SecretKeySpec(encryptionKey, algorithm);
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
    
            // Decrypt the bytes
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
    
            // Convert bytes to JSON string
            String jsonString = new String(decryptedBytes, StandardCharsets.UTF_8);
    
            // Convert JSON string back to Map
            return objectMapper.readValue(jsonString, Map.class);
        } catch (Exception e) {
            // Log the exception if needed
            // e.g., logger.error("Decryption failed: {}", e.getMessage());
            return null; // Return null on any error
        }
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
