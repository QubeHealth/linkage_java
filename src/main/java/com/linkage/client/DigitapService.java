package com.linkage.client;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.validations.DigitapCreditSchema;
import com.linkage.utility.Helper;

import jakarta.ws.rs.core.MultivaluedHashMap;

public class DigitapService extends BaseServiceClient {

    public DigitapService(LinkageConfiguration configuration) {
        super(configuration);
    }

    private static final String CONSENT_MESSAGE = "I hereby authorize Experian to pull my credit report for the Credit History";
    private static final String DEVICE_TYPE = "mobile";

    public ApiResponse<Object> getCreditReport(DigitapCreditSchema body) {

        String timeStamp = Helper.getCurrentDate("ddMMyyyy-HH:mm:ss");
        body.setTimestamp(timeStamp);

        body.setConsentMessage(CONSENT_MESSAGE);
        body.setDeviceType(DEVICE_TYPE);
        body.setConsentAcceptance("yes");
        body.setNameLookup(0);

        String authString = configuration.getDigitapClientId() + ":" + configuration.getDigitapClientSecret();

        // Encode the string in Base64
        String encodedAuthString = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));

        logger.info(encodedAuthString);

        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();
        header.putSingle("content-type", "application/json");
        header.putSingle("Authorization", "Basic " + encodedAuthString);

        final String url = configuration.getDigitapUrl() + "credit_analytics/request";

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Map<String, Object> report = objectMapper.readValue(configuration.getCreditReport(),
                    new TypeReference<Map<String, Object>>() {
                    });

            return new ApiResponse<>(true, "success",
                    report);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ApiResponse<>(false, "Experian pull failed", e);
        }

        // return this.networkCallExternalService(url, "POST", body, header);

    }

}
