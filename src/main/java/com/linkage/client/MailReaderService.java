package com.linkage.client;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.security.auth.Subject;

import org.conscrypt.io.IoUtils;
import org.json.JSONObject;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.constants.Constants.EmailKeywords;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;


public class MailReaderService extends EmailFetcher {

    private MasterService masterService;

    public MailReaderService(String host, String port, String user, String password,
            LinkageConfiguration configuration) {
        super("imap.gmail.com", "993", "qubetestemailssend@gmail.com", "vuopgzdlbsyzmwoo", configuration);
        this.masterService = new MasterService(configuration);
    }

    @Override
    public void connect() throws MessagingException {
        super.connect();
    }

    public Response fetchAndProcessEmail(Message message) throws MessagingException, IOException {
        // Message message = fetchLatestEmail();
        connect();
        Map<String, String> responseMap = new HashMap<>();
        String gcpPath = null;
        String gcpFileName = null;  
        
        try {
            String subject = fetchSubject(message);
            String body = fetchBody(message);
            String keyword = parseSubjectForKeyword(subject);

            responseMap.put(EmailKeywords.SUBJECT, subject);
            responseMap.put(EmailKeywords.BODY, body);

            // Define a map to store handlers for each keyword
            Map<String, Function<String[], Map<String, String>>> handlerMap = new HashMap<>();
            
            // Define handlers for each keyword
            handlerMap.put(EmailKeywords.QUERY_REPLY, args -> handleQueryReply(subject, body, keyword));
            handlerMap.put(EmailKeywords.SUPPORTING_DOCUMENT, args -> handleSupportingDocument());
            handlerMap.put(EmailKeywords.FINAL_BILL_AND_DISCHARGE_SUMMARY, args -> handleFinalBillAndDischargeSummary(subject, body, keyword));
            handlerMap.put(EmailKeywords.PRE_AUTH, args -> handlePreAuth(subject, body, keyword));
            handlerMap.put(EmailKeywords.CASHLESS_CREDIT_REQUEST, args -> handleCashlessCreditRequest(subject, body, keyword, message));
            handlerMap.put(EmailKeywords.ADDITIONAL_INFORMATION, args -> handleAdditionalInformation(subject, body, keyword));

            // Execute the appropriate handler based on the keyword
            Function<String[], Map<String, String>> handler = handlerMap.getOrDefault(keyword, 
                args -> {
                    Map<String, String> errorMap = new HashMap<>();
                    errorMap.put(EmailKeywords.ERROR, "No matching function found for keyword: " + keyword);
                    return errorMap;
                });
    
            // Call the handler and populate responseMap
            responseMap = handler.apply(new String[]{subject, body, keyword});
            
            String userId = responseMap.get("claim_no") != null ? responseMap.get("claim_no") : responseMap.get("policy_no");
            responseMap.put("user_id", userId);

            // Fetch and upload attachments if user_id is present
            if (message != null && userId != null) {
                Map<String, String> gcpResponse = fetchAttachments(message, userId);
                gcpPath = gcpResponse.get(EmailKeywords.GCP_PATH);
                gcpFileName = gcpResponse.get(EmailKeywords.GCP_FILE_NAME);
    
                // Include the GCP URL in the response map if successful
                if (gcpPath != null || gcpFileName != null) {
                    responseMap.put(EmailKeywords.GCP_PATH, gcpPath);
                    responseMap.put(EmailKeywords.GCP_FILE_NAME, gcpFileName);
                } else {
                    markAsUnread(message);
                    responseMap.put("error", "Failed to upload attachments to GCP");
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new ApiResponse<>(false,
                                    "Failed to process email",
                                    responseMap))
                            .build();
                }
            }
    
        } catch (Exception e) {
            // Mark email as unread and rethrow the exception
            markAsUnread(message);
            throw e;
        } 
        // finally {
        //     close();
        // }
        return Response.status(Response.Status.OK)
                .entity(new ApiResponse<>(true,
                        "Successfully processed email",
                        responseMap))
                .build();
    }
    

    private String parseSubjectForKeyword(String subject) {
        String[] keywords = EmailKeywords.keywordsList;

        String lowerCaseSubject = subject.toLowerCase();

        for (String keyword : keywords) {
            if (lowerCaseSubject.contains(keyword.toLowerCase())) {
                return keyword;
            }
        }
        return null;
    }

    private void markAsRead(Message message) throws MessagingException {
        message.setFlag(Flags.Flag.SEEN, true);
    }

    private void markAsUnread(Message message) throws MessagingException {
        message.setFlag(Flags.Flag.SEEN, false);
    }

    private Map<String, String> handleSupportingDocument() {
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message", "Handling for supporting document");
        return responseMap;
    }

    private Map<String, String> handleQueryReply(String subject, String body, String keyword) {
        Map<String,String> responseMap = extractKeywords(subject, body, keyword);
        return responseMap;
    }

    private Map<String, String> handleCashlessCreditRequest(String subject, String body, String keyword, Message message) {
        String[] bodyLines = body.split("\n");
        for (String line : bodyLines) {
            if (line.startsWith(EmailKeywords.INITIAL_CASHLESS_APPROVED)) {
                return handleInitialCashlessCreditRequest(subject, body, "initial cashless credit request");
            } else if (line.startsWith(EmailKeywords.FINAL_CASHLESS_APPROVED)) {
                return handleFinalCashlessCreditRequest(subject, body, "final cashless credit request");
            }
        }
        // If neither condition matches, return an error response
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put(EmailKeywords.ERROR, "Neither Initial nor Final Cashless Approved Amount found in body.");
        return responseMap;
    }

    private Map<String, String> handleInitialCashlessCreditRequest(String subject, String body, String keyword) {
        Map<String,String> responseMap = extractKeywords(subject, body, keyword);

        return responseMap;
    }

    private Map<String, String> handleFinalCashlessCreditRequest(String subject, String body, String keyword){
        Map<String,String> responseMap = extractKeywords(subject, body, keyword);

        return responseMap;
    }

    private Map<String, String> handleAdditionalInformation(String subject, String body, String keyword){
        Map<String,String> responseMap = extractKeywords(subject, body, keyword);

        return responseMap;
    }

    private Map<String, String> handleFinalBillAndDischargeSummary(String subject, String body, String keyword) {
        Map<String,String> responseMap = extractKeywords(subject, body, keyword);

        return responseMap;
    }
    
    private Map<String,String> handlePreAuth(String subject, String body, String keyword) {
        
        Map<String,String> responseMap = extractKeywords(subject, body, keyword);

        return responseMap;
    }

    private Map<String, String> extractKeywords(String subject, String body, String keyword) {
        Map<String, String> extractedData = new HashMap<>();

        try {
            // Will make a call to the database and pick up the matching regex for the given
            Map<String, String> emailKeyword = new HashMap<>();
            emailKeyword.put("keyword", keyword);

            ApiResponse<Object> emailTemplateRequest = this.masterService.emailTemplate(emailKeyword);
            Map<String, Object> emailTemplateResponseData = (Map<String, Object>) emailTemplateRequest.getData();
            String emailTemplate = String.valueOf(emailTemplateResponseData.get("data")); // "225";

            extractedData.put("type", keyword);
            extractedData.put("subject", subject);
            extractedData.put("body", body);

            JSONObject templateJson = new JSONObject(emailTemplate);
            JSONObject regexSubject = templateJson.getJSONObject("subject");

            for (String key : regexSubject.keySet()) {
                String regex = regexSubject.getString(key);
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(subject);

                // Attempt to find and extract data
                if (matcher.find()) {
                    String extractedValue = matcher.group(1); // Assuming single group capture
                    extractedData.put(key, extractedValue);
                } else {
                    extractedData.put(key, ""); // Handle case where regex does not match
                }
            }

            
            if (body != null) {
                JSONObject regexBody = templateJson.getJSONObject("body");
                for (String key : regexBody.keySet()) {
                    String regex = regexBody.getString(key);
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(body);

                    // Attempt to find and extract data
                    if (matcher.find()) {
                        String extractedValue = matcher.group(1); // Assuming single group capture
                        extractedData.put(key, extractedValue);
                    } else {
                        extractedData.put(key, ""); // Handle case where regex does not match
                    }
                }
            }

        } catch (Exception e) {
            extractedData.clear();
            extractedData.put("ERROR", "An error occurred while extracting keywords: " + e.getMessage());
            throw e;
        }
        return extractedData;
    }

    @Override
    public void close() throws MessagingException {
        super.close();
    }
}