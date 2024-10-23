package com.linkage.utility;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
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
import com.linkage.LinkageConfiguration;

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
        ZonedDateTime nowIST = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (format != null && !format.isBlank()) {
            formatter = DateTimeFormatter.ofPattern(format);
        }

        return nowIST.format(formatter);

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


    public static String convertToUrlEncoded(Map<String, String> params) {
        // Convert the map to a URL-encoded string
        StringBuilder encoded = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            try {
                encoded.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                       .append("=")
                       .append(URLEncoder.encode(entry.getValue(), "UTF-8"))
                       .append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();  // Handle the encoding exception appropriately
            }
        }
        // Remove the trailing "&"
        if (encoded.length() > 0) {
            encoded.setLength(encoded.length() - 1);
        }
        return encoded.toString();
    }
  
    public static boolean sendEmail(LinkageConfiguration configuration, String to, String subject, String body) {
        try {
            // SMTP server information
            String host = configuration.getEmailHost();
            final String username = configuration.getEmailSmtp();
            final String password = configuration.getEmailPassword();
    
            // Set properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", "587"); // Replace with your SMTP port
    
            // Get the Session object
            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
    
            // Create a default MimeMessage object
            Message message = new MimeMessage(session);
    
            // Set From: header field
            message.setFrom(new InternetAddress(configuration.getEmailSmtp()));
    
            // Set To: header field
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
    
            // Set Subject: header field
            message.setSubject(subject);
    
            // Set the actual message
            message.setText(body);
    
            // Send the message
            Transport.send(message);
    
            // If everything went well, return true
            return true;
        } catch (MessagingException e) {
            // Log the exception (optional)
            e.printStackTrace();
    
            // If there was an error, return false
            return false;
        }
    }

    public static String convertJsonToString(Object jsonNode) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String downloadXmlAsString(String urlString) {
        try {
            // Create a URL object from the string
            URL url = new URL(urlString);

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Check the response code (200 means OK)
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Create an InputStream to read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();

                // Read the response line by line and append to content
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                 // Close the BufferedReader
                in.close();
                // Return the content as a string
                return content.toString();
            } else {
                System.out.println("Failed to download XML. HTTP response code: " + responseCode);
                return "";
            }
        } catch (Exception e) {
            System.out.println("Error while downloading xml " + e.getMessage());
            return "";
        }
    }

    // to extract values based on field mappings from a Map
    public static Map<String, Object> getMappedValuesFromMap(Map<String, Object> rootMap, Map<String, Object> fieldMappings) {
        Map<String, Object> resultMap = new HashMap<>();
    
        // Iterate over the field mappings
        for (Map.Entry<String, Object> entry : fieldMappings.entrySet()) {
            String outputField = entry.getKey();
            String keyPath = entry.getValue().toString();
    
            // Support concatenation of multiple fields using "+"
            if (keyPath.contains("+")) {
                String[] keyParts = keyPath.split("\\+");
                StringBuilder concatenatedValue = new StringBuilder();
    
                for (String part : keyParts) {
                    Object value = getValueFromMap(rootMap, part.trim());
                    if (value != null) {
                        if (concatenatedValue.length() > 0) {
                            concatenatedValue.append(" "); // Add a space between concatenated parts
                        }
                        concatenatedValue.append(value.toString()); // Convert the object to string before concatenation
                    }
                }
    
                resultMap.put(outputField, concatenatedValue.length() > 0 ? concatenatedValue.toString() : null);
            } else {
                // Get the value based on the key path
                Object value = getValueFromMap(rootMap, keyPath);
                resultMap.put(outputField, value);
            }
        }
    
        return resultMap;
    }

    // To get value based on key path from a Map
    private static Object getValueFromMap(Map<String, Object> rootMap, String keyPath) {
        String[] keys = keyPath.split("\\.");
    
        Object current = rootMap;
        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(key);
    
                // If the field does not exist or is null, return null
                if (current == null) {
                    return null;
                }
            } else {
                return null; // If we hit a non-map object before the key is resolved
            }
        }
    
        return current; // Return the object, which could be of any type
    }

}
