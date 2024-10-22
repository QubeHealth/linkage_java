package com.linkage.client;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;

import com.linkage.core.validations.DigitapSchema.GetCreditBureau;
import com.linkage.core.validations.DigitapSchema.SendAadharOtp;
import com.linkage.core.validations.DigitapSchema.VerifyAadharOtp;
import com.linkage.utility.Helper;

import jakarta.ws.rs.core.MultivaluedHashMap;

public class DigitapService extends BaseServiceClient {

    public DigitapService(LinkageConfiguration configuration) {
        super(configuration);
    }

    private static final String CONSENT_MESSAGE = "I hereby authorize Experian to pull my credit report for the Credit History";
    private static final String DEVICE_TYPE = "mobile";
    private static final String DEVICE_ID = "350123451234560";
    private static final Integer REPORT_TYPE = 1; // for xml report

    public ApiResponse<Object> getCreditReport(GetCreditBureau body) {

        String timeStamp = Helper.getCurrentDate("ddMMyyyy-HH:mm:ss");
        body.setTimestamp(timeStamp);
        body.setReportType(REPORT_TYPE);
        body.setConsentMessage(CONSENT_MESSAGE);
        body.setDeviceType(DEVICE_TYPE);
        body.setConsentAcceptance("yes");
        body.setNameLookup(0);
        // if device id is not passed the set the static device id given by digitap doc
        if (body.getDeviceId() == null || body.getDeviceId().length() != 15) {
            body.setDeviceId(DEVICE_ID);
        }
        String authString = configuration.getDigitapClientId() + ":" + configuration.getDigitapClientSecret();

        // Encode the string in Base64
        String encodedAuthString = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));

        logger.info(encodedAuthString);

        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();
        header.putSingle("content-type", "application/json");
        header.putSingle("Authorization", "Basic " + encodedAuthString);

        final String url = configuration.getDigitapUrl() + "credit_analytics/request";

        return this.networkCallExternalService(url, "POST", body, header);

    }

    public ApiResponse<Object> sendAadharOtp(SendAadharOtp body) {

        Map<String, String> reqBody = new HashMap<>();
        reqBody.put("uniqueId", body.getUniqueId());
        reqBody.put("uid", body.getAadharNumber());

        // Encode the string in Base64
        String authString = configuration.getDigitapClientId() + ":" + configuration.getDigitapClientSecret();
        String encodedAuthString = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));

        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();
        header.putSingle("content-type", "application/json");
        header.putSingle("authorization", encodedAuthString);

        final String url = configuration.getDigitapKycUrl() + "ent/v3/kyc/intiate-kyc-auto";

        return this.networkCallExternalService(url, "POST", reqBody, header);

    }

    public ApiResponse<Object> verifyAadharOtp(Map<String,Object> body) {


        // Encode the string in Base64
        String authString = configuration.getDigitapClientId() + ":" + configuration.getDigitapClientSecret();
        String encodedAuthString = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));

        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();
        header.putSingle("content-type", "application/json");
        header.putSingle("authorization", encodedAuthString);

        final String url = configuration.getDigitapKycUrl() + "ent/v3/kyc/submit-otp";

        return this.networkCallExternalService(url, "POST", body, header);

    }

}