package com.linkage.client;

import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.validations.ErupeeSchema.VoucherRequest;
import com.linkage.utility.Helper;

import java.nio.charset.StandardCharsets;
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

            System.out.println(body);

            return this.networkCallExternalService(url, "POST", body, header);

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
        request.put("service", "CreateVouchers");
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


    public static String decrypt(String encryptedData, String encryptedKey, String clientPrivateKeyPath) throws Exception {

        // 1. Get the IV
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        byte[] iv = Arrays.copyOfRange(decodedData, 0, 16);
        byte[] encryptedResponse = Arrays.copyOfRange(decodedData, 16, decodedData.length);

        // 2. Decrypt the session key
        byte[] privateKeyBytes = readFile(clientPrivateKeyPath);
        PrivateKey clientPrivateKey = getPrivateKeyFromPKCS12(privateKeyBytes);
        Cipher keyCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        keyCipher.init(Cipher.DECRYPT_MODE, clientPrivateKey);
        byte[] decodedKey = keyCipher.doFinal(Base64.getDecoder().decode(encryptedKey));

        // 3. Decrypt the response
        SecretKeySpec sessionKeySpec = new SecretKeySpec(decodedKey, "AES");
        Cipher responseCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        responseCipher.init(Cipher.DECRYPT_MODE, sessionKeySpec, new IvParameterSpec(iv));
        byte[] decryptedResponse = responseCipher.doFinal(encryptedResponse);

        // 4. Skip the IV and return the decrypted response
        return new String(decryptedResponse, StandardCharsets.UTF_8);
    }

    private static byte[] readFile(String path) throws Exception {
        // Replace with your logic to read the file from the specified path
        // This example assumes the file is a byte array
        return new byte[0]; // Placeholder, replace with actual file reading logic
    }

    private static PrivateKey getPrivateKeyFromPKCS12(byte[] keystoreBytes) throws Exception {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keystoreBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
}
