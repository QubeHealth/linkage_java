package com.linkage.client;

import java.util.HashMap;
import java.util.Map;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.validations.SmsSchema;

import jakarta.ws.rs.core.MultivaluedHashMap;

public class SmsService extends BaseServiceClient {

    public SmsService(LinkageConfiguration configuration) {
        super(configuration);
    }

    private MultivaluedHashMap<String, Object> prepareHeaders(String apiKey) {
        MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle("api-key", apiKey);
        headers.putSingle("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        return headers;
    }

    private Map<String, Object> prepareRequestBody(String mobile, String type, String messageBody, String templateId) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("to", "+91" + mobile);
        requestBody.put("type", type);
        requestBody.put("sender", configuration.getKaleyraSenderId());
        requestBody.put("body", messageBody);
        requestBody.put("template_id", templateId);
        return requestBody;
    }

    public ApiResponse<Object> sendPaymentSms(SmsSchema.PaymentStatus body) {
        final String URL = configuration.getKaleyraBaseUrl() + "/v1/" + configuration.getKaleyraSid() + "/messages";
        String templateId = configuration.getKayeraPaymentPendingTemplateId();
        final String TYPE = "TXN";

        MultivaluedHashMap<String, Object> headers = prepareHeaders(configuration.getKaleyraApiKey());

        String message = "";

        if (body.getStatus().equals("payment_pending")) {
            message = String.format(
                    "Payment Pending:\nQubePay Payment Pending Txn. ID %s. Please check the app after 30 mins",
                    body.getTransactionId());
        } else if (body.getStatus().equals("payment_failed")) {
            templateId = configuration.getKayeraPaymentFailedTemplateId();
            message = String.format(
                    "Payment Failed: \nOh! Your payment via QubePay Txn. ID %s FAILED. Don't worry, any amount debited from %s will be REFUNDED within 7 Business days.",
                    body.getTransactionId(), body.getType());
        } else {
            return new ApiResponse<Object>(false, "Invalid payment status", null);
        }

        Map<String, Object> requestBody = prepareRequestBody(body.getMobile(), TYPE, message, templateId);

        return networkCallExternalService(URL, "post", requestBody, headers);
    }
}
