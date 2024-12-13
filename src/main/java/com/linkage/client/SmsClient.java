package com.linkage.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.linkage.LinkageConfiguration;

public class SmsClient {

    private final LinkageConfiguration configuration;

    public SmsClient(LinkageConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Sends a customized message using a template.
     * 
     * @param phoneNumber   Recipient's phone number
     * @param dltTemplateId DLT template ID
     * @param messageTemplate Template for the message with placeholders
     * @param messageParams Parameters to replace placeholders in the template
     * @return Response from the SMS API
     */
    public String sendMessage(String phoneNumber, String dltTemplateId, String messageTemplate, String... messageParams) {
        try {
            String endpoint = configuration.getSmsConfig().getGupshupEndpoint();

            // Format the message using the template and provided parameters
            String message = String.format(messageTemplate, (Object[]) messageParams);

            // Prepare request parameters
            String params = "method=" + URLEncoder.encode("SendMessage", "UTF-8") +
                    "&send_to=" + URLEncoder.encode(phoneNumber, "UTF-8") +
                    "&msg=" + URLEncoder.encode(message, "UTF-8") +
                    "&msg_type=" + URLEncoder.encode("TEXT", "UTF-8") +
                    "&userid=" + URLEncoder.encode(configuration.getSmsConfig().getGupshupUserId(), "UTF-8") +
                    "&auth_scheme=" + URLEncoder.encode("plain", "UTF-8") +
                    "&password=" + URLEncoder.encode(configuration.getSmsConfig().getGupshupPassword(), "UTF-8") +
                    "&v=" + URLEncoder.encode("1.1", "UTF-8") +
                    "&format=" + URLEncoder.encode("json", "UTF-8") +
                    "&principalEntityId=" + URLEncoder.encode(configuration.getSmsConfig().getDltPrincipalEntityId(), "UTF-8") +
                    "&dltTemplateId=" + URLEncoder.encode(dltTemplateId, "UTF-8");

            // Set up the HTTP connection
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            // Send request
            try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
                out.writeBytes(params);
                out.flush();
            }

            // Read response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return response.toString();

        } catch (Exception e) {
            SentryException.captureException(e);
            e.printStackTrace();
            return "Failed to send message: " + e.getMessage();
        }
    }
}