package com.linkage.client;

import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.validations.ErupeeSchema.VoucherRequest;
import com.linkage.core.validations.ErupeeSchema.VoucherStatus;
import com.linkage.utility.Helper;

import java.io.FileInputStream;
import java.security.*;
import jakarta.ws.rs.core.MultivaluedHashMap;

public class ErupeeService extends BaseServiceClient {

    public ErupeeService(LinkageConfiguration configuration) {
        super(configuration);
    }

    public ApiResponse<Object> creatVoucher(VoucherRequest request) {
        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();

        header.putSingle("apiKey", "ZceJ7Ggva6Sy3h7D8PSxdWSeIvgE4ICg");
        String url = "https://apibankingonesandbox.icicibank.com/api/MerchantAPI/UPI2/v1/CreateVouchers";

        try {

            String body = Helper.toJsonString(request);
            System.out.println(body);

            body = encryptRequest(body);

            ApiResponse<Object> res = this.networkCallExternalService(url, "POST", body, header);

            Map<String, Object> data = (Map<String, Object>) res.getData();

            String encryptedKey = data.get("encryptedKey").toString();

            String decrptedRes = decrypt(data.get("encryptedData").toString(), encryptedKey);
            ObjectMapper objectMapper = new ObjectMapper();

            HashMap<String, Object> map = objectMapper.readValue(decrptedRes,
                    new TypeReference<HashMap<String, Object>>() {
                    });

            return new ApiResponse<>(true, "success", map);

        } catch (Exception e) {
            return new ApiResponse<>(false, e.getMessage(), null);
        }

    }

    private static final String RSA_ECB_PKCS1 = "RSA/ECB/PKCS1Padding";
    private static final String AES_CBC_PKCS5 = "AES/CBC/PKCS5Padding";
    private static final int SESSION_KEY_LENGTH = 16; // 16 bytes (128 bits)
    private static final String ICICI_PUBLIC_KEY = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAsIwVStQi6aSMLBZu3vhafOR5NTMNp+TXPwyk/6VoaSQfDnZaSQPYhdt4a8X215KwXwpIL1eBJOH2NW8jp5AO4WauHWEwEggJvPaC8FgzZtDhjYexOk+/yaDbY7U9BofJSU76VIBxRoN7YmAknAKrpfn0ukXPPuUx5Ny/cy85nunqo5M8Acf2VVwSGZQMBZFSm3yxYOdS4laDlM+s1w+5wLDMjYSgIMm76rpVdO3hs2n2dSAYM6XMOaqNDwHdZk6n8lPgivYVXjTz7KU9eqkFnecWvn2ugRI7hgrplZxS020k0QBeYd0AH7zJZKS3Xo5VycL01UO/WYOQvB7v8lge7TiQZ3CCrnuykqcJ/r5DMLO/cKQAeZi+LQ95FQg39joO8G7bfO7+a3Gs8Re3mRW7AA8x1aEn7XZMOUu4l4IfNvwh20V4cz3xvGXdr9ZLFvgX5593MxCDBjkiaynzG8gmLVTIoaItPy+khwO/vjfWka0L3yvT3l55R4H/KRKxlHaY58HVdLbuWrUoH/4gbkYFYFC+rejBW5wbE0FJmWIkEXLKsTlXcsn6eAzi4BRxidQ/4rIEf8qWpSFzJobivBnWe4bpBA19g3N47PDpD5xS6uj7ODSBhEn22UnsiDaGV+RhsXYA/xqaJCjB6+W7CN00Lowr87sUoT4VAK8wrOk4D5sCAwEAAQ==";

    public static String encryptRequest(String payload) throws Exception {

        // 1. Generate random session key
        byte[] sessionKey = generateRandomBytes(SESSION_KEY_LENGTH);

        // 2. Encrypt session key with RSA and encode with Base64
        byte[] encryptedKey = encryptRSA(sessionKey, ICICI_PUBLIC_KEY);
        String encodedEncryptedKey = Base64.getEncoder().encodeToString(encryptedKey);

        // 3. Generate random initialization vector (IV) for AES
        byte[] iv = generateRandomBytes(Cipher.getInstance(AES_CBC_PKCS5).getBlockSize());

        // 4. Encrypt payload with AES using session key and IV
        byte[] encryptedData = encryptAES(payload.getBytes(), sessionKey, iv);

        // 4a. Option 1: Send Base64 encoded IV in separate field
        String encodedIv = Base64.getEncoder().encodeToString(iv);

        // 5. Build the complete request object
        JSONObject request = new JSONObject();
        request.put("requestId", UUID.randomUUID());
        request.put("service", "AccountCreation");
        request.put("encryptedKey", encodedEncryptedKey);
        request.put("oaepHashingAlgorithm", "NONE");
        request.put("iv", encodedIv);
        request.put("encryptedData", Base64.getEncoder().encodeToString(encryptedData));

        return request.toString();
    }

    private static byte[] generateRandomBytes(int size) throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        byte[] bytes = new byte[size];
        random.nextBytes(bytes);
        return bytes;
    }

    private static byte[] encryptRSA(byte[] data, String publicKey) throws Exception {

        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS1);
        cipher.init(Cipher.ENCRYPT_MODE, KeyFactory.getInstance("RSA").generatePublic(publicKeySpec));
        return cipher.doFinal(data);
    }

    private static byte[] encryptAES(byte[] data, byte[] key, byte[] iv) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        return cipher.doFinal(data);
    }

    public static String decrypt(String base64EncryptedData, String base64EncryptedKey) throws Exception {

        PrivateKey clientPrivateKey = getPrivateKeyFromP12();

        // Decode the base64 encoded encrypted key
        byte[] encryptedKey = Base64.getDecoder().decode(base64EncryptedKey);

        // Decrypt the session key using RSA
        byte[] sessionKeyBytes = decryptRSA(encryptedKey, clientPrivateKey);

        // Decode the base64 encoded encrypted data
        byte[] encryptedDataBytes = Base64.getDecoder().decode(base64EncryptedData);

        // Extract the IV (first 16 bytes) and the encrypted response (rest of the
        // bytes)
        byte[] iv = new byte[16];
        byte[] encryptedResponse = new byte[encryptedDataBytes.length - 16];
        System.arraycopy(encryptedDataBytes, 0, iv, 0, 16);
        System.arraycopy(encryptedDataBytes, 16, encryptedResponse, 0, encryptedDataBytes.length - 16);

        // Decrypt the response using AES
        byte[] responseBytes = decryptAES(encryptedResponse, sessionKeyBytes, iv);

        // Convert the decrypted response to a string (if it is text)
        String response = new String(responseBytes);

        System.out.println("Decrypted Response: " + response);

        return response;
    }

    private static byte[] decryptRSA(byte[] encryptedData, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedData);
    }

    private static byte[] decryptAES(byte[] encryptedData, byte[] key, byte[] iv) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        return cipher.doFinal(encryptedData);
    }

    private static PrivateKey getPrivateKeyFromP12() throws Exception {
        String filePath = "sslPrivateKey.p12";
        String password = "";
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(filePath)) {
            keyStore.load(fis, password.toCharArray());
        }
        String alias = keyStore.aliases().nextElement();
        return (PrivateKey) keyStore.getKey(alias, password.toCharArray());
    }

    public ApiResponse<Object> voucherStatus(VoucherStatus request) {
        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();

        header.putSingle("apiKey", "ZceJ7Ggva6Sy3h7D8PSxdWSeIvgE4ICg");
        String url = "https://apibankingonesandbox.icicibank.com/api/MerchantAPI/UPI2/v1/TransactionStatusByCriteria";

        try {

            String body = Helper.toJsonString(request);
            System.out.println(body);

            body = encryptRequest(body);

            ApiResponse<Object> res = this.networkCallExternalService(url, "POST", body, header);

            Map<String, Object> data = (Map<String, Object>) res.getData();

            String encryptedKey = data.get("encryptedKey").toString();

            String decrptedRes = decrypt(data.get("encryptedData").toString(), encryptedKey);
            ObjectMapper objectMapper = new ObjectMapper();

            HashMap<String, Object> map = objectMapper.readValue(decrptedRes,
                    new TypeReference<HashMap<String, Object>>() {
                    });

            return new ApiResponse<>(true, "success", map);

        } catch (Exception e) {
            return new ApiResponse<>(false, e.getMessage(), null);
        }

    }
}
