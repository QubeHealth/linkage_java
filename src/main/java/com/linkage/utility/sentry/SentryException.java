package com.linkage.utility.sentry;

import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.protocol.Message;
import jakarta.ws.rs.core.Response;

public class SentryException {
    public static String userId;
    public static String apiEndpoint;

    // Capture client error
    public static void captureClientError(String errorMessage) {
        captureEvent(Response.Status.BAD_REQUEST, errorMessage, null);
    }

    // Capture service unavailable error
    public static void captureServiceUnavailable(String errorMessage) {
        captureEvent(Response.Status.SERVICE_UNAVAILABLE, errorMessage, null);
    }

    // Capture internal server error
    public static void captureServerError(String errorMessage) {
        captureEvent(Response.Status.INTERNAL_SERVER_ERROR, errorMessage, null);
    }

    // Capture an exception with additional context
    public static void captureException(Exception exception) {
        String errorMessage = "Exception: " + exception.getMessage();
        captureEvent(Response.Status.INTERNAL_SERVER_ERROR, errorMessage, exception);
    }

    public static void captureException(Object userId, String apiPath, Exception exception) {
        String errorMessage = "Exception: " + exception.getMessage();
        SentryException.userId = userId != null ? userId.toString() : null;
        SentryException.apiEndpoint = apiPath;
        captureEvent(Response.Status.INTERNAL_SERVER_ERROR, errorMessage, exception);
    }

    // Generic method to create and capture a Sentry event
    private static void captureEvent(Response.Status status, String errorMessage, Throwable throwable) {
        SentryEvent event = new SentryEvent();

        // Create a custom message
        String customMessage = "API: " + apiEndpoint + " | UserID: " + userId + " | Status: " + status.getStatusCode() + " " + status + " | Error Message: " + errorMessage;

        // Set the custom throwable if provided
        if (throwable != null) {
            event.setThrowable(new RuntimeException(customMessage, throwable));
        } else {
            event.setThrowable(new RuntimeException(customMessage));
        }

        // Set custom tags and message
        if(userId != null) {
            event.setTag("UserID", userId);
        }
        if(apiEndpoint != null) {
            event.setTag("APIEndpoint", apiEndpoint);
        }

        Message message = new Message();
        message.setMessage(customMessage);
        event.setMessage(message);

        // Send the event to Sentry
        Sentry.captureEvent(event);
    }
}