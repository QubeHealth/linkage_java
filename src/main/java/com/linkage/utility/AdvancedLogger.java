package com.linkage.utility;



import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdvancedLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdvancedLogger.class);

    private static String externalLoggingUrl; // URL to be set dynamically
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private AdvancedLogger() {
        // Private constructor to prevent instantiation
    }

    public static void initialize(String loggingUrl) {
        externalLoggingUrl = loggingUrl;
        logInfo("AdvancedLogger Initialization", "AdvancedLogger initialized with URL: " + loggingUrl);
    }

    public static void logInfo(String shortMessage, String fullMessage) {
        LOGGER.info("[INFO] {}",  shortMessage);
        sendToExternalSystemAsync("INFO", "LINKAGE_SERVICE_" +shortMessage, fullMessage, 6);
    }

    public static void logWarn(String shortMessage, String fullMessage) {
        LOGGER.warn("[WARN] {}", shortMessage);
        sendToExternalSystemAsync("WARN","LINKAGE_SERVICE_"+ shortMessage, fullMessage, 4);
    }

    public static void logError(String shortMessage, String fullMessage) {
        LOGGER.error("[ERROR] {}", shortMessage);
        sendToExternalSystemAsync("ERROR","LINKAGE_SERVICE_"+ shortMessage, fullMessage, 3);
    }

    private static void sendToExternalSystemAsync(String severity, String shortMessage, String fullMessage, int level) {
        EXECUTOR.submit(() -> sendToExternalSystem(severity, shortMessage, fullMessage, level));
    }

    private static void sendToExternalSystem(String severity, String shortMessage, String fullMessage, int level) {
        if (externalLoggingUrl == null || externalLoggingUrl.isEmpty()) {
            LOGGER.error("External logging URL is not set.");
            return;
        }

        String jsonPayload = String.format(
            "{ \"version\": \"1.1\", \"host\": \"%s\", \"short_message\": \"[%s] %s\", \"full_message\": \"%s\", \"timestamp\": %.3f, \"level\": %d }",
            "advanced-host", severity, shortMessage, fullMessage, System.currentTimeMillis() / 1000.0, level
        );

        RequestBody requestBody = RequestBody.create(jsonPayload, JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(externalLoggingUrl)
                .post(requestBody)
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                LOGGER.error("Failed to send log to external logging system. Response Code: {}", response.code());
            } else {
                LOGGER.info("Log successfully sent to external logging system. Response Code: {}", response.code());
            }
        } catch (Exception e) {
            LOGGER.error("Error while sending log to external logging system: {}", e.getMessage());
        }
    }
}