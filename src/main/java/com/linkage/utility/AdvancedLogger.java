package com.linkage.utility;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    private static volatile String externalLoggingUrl; // Thread-safe URL for logging
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final String HOST_NAME = "advanced-host";
    private static final String SERVICE_PREFIX = "LINKAGE_SERVICE_";

    private AdvancedLogger() {
        // Private constructor to prevent instantiation
    }

    /**
     * Initializes the logger with an external logging URL.
     * 
     * @param loggingUrl The URL for the external logging system.
     */
    public static void initialize(String loggingUrl) {
        if (loggingUrl == null || loggingUrl.isEmpty()) {
            throw new IllegalArgumentException("Logging URL cannot be null or empty.");
        }
        externalLoggingUrl = loggingUrl;
        logInfo("Initialization", "AdvancedLogger initialized with URL: " + loggingUrl);
    }

    /**
     * Logs an INFO level message.
     * 
     * @param shortMessage A short summary of the log.
     * @param fullMessage  Detailed log message.
     */
    public static void logInfo(String shortMessage, String fullMessage) {
        LOGGER.info("[INFO] {}", shortMessage);
        sendToExternalSystemAsync("INFO", SERVICE_PREFIX + shortMessage, fullMessage, 6);
    }

    /**
     * Logs a WARN level message.
     * 
     * @param shortMessage A short summary of the log.
     * @param fullMessage  Detailed log message.
     */
    public static void logWarn(String shortMessage, String fullMessage) {
        LOGGER.warn("[WARN] {}", shortMessage);
        sendToExternalSystemAsync("WARN", SERVICE_PREFIX + shortMessage, fullMessage, 4);
    }

    /**
     * Logs an ERROR level message.
     * 
     * @param shortMessage A short summary of the log.
     * @param fullMessage  Detailed log message.
     */
    public static void logError(String shortMessage, String fullMessage) {
        LOGGER.error("[ERROR] {}", shortMessage);
        sendToExternalSystemAsync("ERROR", SERVICE_PREFIX + shortMessage, fullMessage, 3);
    }

    private static void sendToExternalSystemAsync(String severity, String shortMessage, String fullMessage, int level) {
        EXECUTOR.submit(() -> sendToExternalSystem(severity, shortMessage, fullMessage, level));
    }

    private static void sendToExternalSystem(String severity, String shortMessage, String fullMessage, int level) {
        if (externalLoggingUrl == null || externalLoggingUrl.isEmpty()) {
            LOGGER.error("External logging URL is not set. Unable to send log.");
            return;
        }

        try {
            // Convert current time to IST and calculate timestamp
            ZonedDateTime istTime = Instant.now().atZone(ZoneId.of("Asia/Kolkata"));
            double istTimestamp = istTime.toEpochSecond() + (istTime.getNano() / 1_000_000_000.0);

            // Create JSON payload
            String jsonPayload = String.format(
                    "{ \"version\": \"1.1\", \"host\": \"%s\", \"short_message\": \"[%s] %s\", \"full_message\": \"%s\", \"timestamp\": %.3f, \"level\": %d }",
                    HOST_NAME, severity, shortMessage, fullMessage, istTimestamp, level);

            // Build and execute HTTP request
            RequestBody requestBody = RequestBody.create(jsonPayload, JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(externalLoggingUrl)
                    .post(requestBody)
                    .build();

            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    LOGGER.error("Failed to send log. Response Code: {}", response.code());
                } else {
                    LOGGER.info("Log sent successfully. Response Code: {}", response.code());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while sending log: {}", e.getMessage(), e);
        }
    }

    /**
     * Shuts down the ExecutorService to release resources.
     */
    public static void shutdown() {
        LOGGER.info("Shutting down AdvancedLogger...");
        EXECUTOR.shutdown();
    }
}