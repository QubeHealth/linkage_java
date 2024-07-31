package com.linkage.utility;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONObject;
import org.json.XML;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public final class Helper {
    private Helper() {

    }

    private static byte[] getEncryptDecryptKey() {
        String keyString = "DocumentKey";
        byte[] keyBuf = keyString.getBytes(StandardCharsets.UTF_8);
        int paddingLength = 16 - keyBuf.length;
        byte[] paddingBuf = new byte[paddingLength];
        Arrays.fill(paddingBuf, (byte) 0);
        return Arrays.copyOf(concatByteArrays(keyBuf, paddingBuf), 16);
    }

    private static byte[] concatByteArrays(byte[]... arrays) {
        int totalLength = 0;
        for (byte[] array : arrays) {
            totalLength += array.length;
        }

        byte[] result = new byte[totalLength];
        int currentIndex = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, currentIndex, array.length);
            currentIndex += array.length;
        }
        return result;
    }

    public static String decryptData(String userId, String data) {
        if (userId == null || data == null) {
            return null;
        }

        byte[] decryptionKey = getEncryptDecryptKey();
        byte[] iv = concatByteArrays("123".getBytes(StandardCharsets.UTF_8), userId.getBytes(StandardCharsets.UTF_8),
                "456".getBytes(StandardCharsets.UTF_8));
        try {
            Cipher decipher = Cipher.getInstance("AES/CTR/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(decryptionKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            decipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decodedData = Base64.getDecoder().decode(data);
            byte[] decrypted = decipher.doFinal(decodedData);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encryptData(String userId, String data) {
        try {
            byte[] encryptionKey = getEncryptDecryptKey();
            byte[] iv = concatByteArrays("123".getBytes(StandardCharsets.UTF_8),
                    userId.getBytes(StandardCharsets.UTF_8),
                    "456".getBytes(StandardCharsets.UTF_8));
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec secretKey = new SecretKeySpec(encryptionKey, "AES");

            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());

            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String parseXmlContent(String xmlContent) {
        return StringEscapeUtils.unescapeXml(xmlContent);
    }

    private static Map<String, Object> jsonToMap(JSONObject jsonObject) {
        Map<String, Object> map = new HashMap<>();
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                value = jsonToMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static Map<String, Object> xmlToJson(String xmlData) {
        try {
            // Convert XML to JSON
            JSONObject json = XML.toJSONObject(xmlData);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("status", true);
            resultMap.put("message", "Xml to json formatted successfully");
            resultMap.put("data", jsonToMap(json));
            return resultMap;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("status", false);
            resultMap.put("message", "Xml to json formatter failed");
            resultMap.put("data", null);
            return resultMap;
        }
    }

    public static List<Object> getDataFromMap(Object object, List<String> keys) {
        Set<Object> result = new HashSet<>();

        iter(object, keys, result);

        return new ArrayList<>(result);
    }

    private static void iter(Object obj, List<String> keys, Set<Object> results) {
        if (!(obj instanceof Map)) {
            return;
        }

        Map<String, Object> map = (Map<String, Object>) obj;
        for (String key : keys) {
            Object value = map.get(key);
            if (value instanceof Object && !value.toString().isEmpty()) {
                results.add(value);
            }
        }
        map.values().stream().filter(Objects::nonNull).forEach(v -> iter(v, keys, results));
    }

    public static String formatDate(String date, String currentFormat, String convertToFormat) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(currentFormat));
        return localDate.format(DateTimeFormatter.ofPattern(convertToFormat));
    }

    public static String getCurrentDate(String format) {
        LocalDateTime currentDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (format != null && !format.isBlank()) {
            formatter = DateTimeFormatter.ofPattern(format);
        }

        return currentDate.format(formatter);

    }

    public static String toJsonString(Object obj) {
        // ObjectMapper instance
        ObjectMapper mapper = new ObjectMapper();

        // Convert object to JSON string
        String jsonString;
        try {
            jsonString = mapper.writeValueAsString(obj);
            return jsonString;
        } catch (JsonProcessingException e) {
            System.out.println("\nJSON conversion failed => " + e.getMessage());
            return "";
        }

    }

    public static String md5Encryption(String input) {
        return DigestUtils.md5Hex(input);
    }

    public static boolean isValidUrl(String urlString) {
        try {
            // Attempt to create a URL object
            new URL(urlString);
            return true; // If no exception is thrown, URL is valid
        } catch (Exception e) {
            return false; // If an exception is caught, URL is not valid
        }
    }

    public static String jsonToXML(String json) {

        ObjectMapper jsonMapper = new ObjectMapper();
        XmlMapper xmlMapper = new XmlMapper();

        try {
            JsonNode jsonNode = jsonMapper.readTree(json);

            String xml = xmlMapper.writeValueAsString(jsonNode);

            String regex = "<ObjectNode>(.*?)</ObjectNode>";
            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(xml);

            if (matcher.find()) {
                return matcher.group(1);
            }

            return xml;

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}
