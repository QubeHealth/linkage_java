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

import org.json.JSONObject;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.constants.Constants.EmailKeywords;


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

    public Map<String, String> fetchAndProcessEmail(Message message) throws MessagingException, IOException {
        connect();
        // Message message = fetchLatestEmail();
        Map<String, String> responseMap = new HashMap<>();
        String gcpPath = null;
        String gcpFileName = null;  
        
        try {
            String subject = fetchSubject(message);
            String body = fetchBody(message);
            String keyword = parseSubjectForKeyword(subject);
    
            // Define a map to store handlers for each keyword
            Map<String, Function<String[], Map<String, String>>> handlerMap = new HashMap<>();
            
            // Define handlers for each keyword
            handlerMap.put(EmailKeywords.QUERY_REPLY, args -> handleQueryReply(args[0], args[1]));
            handlerMap.put(EmailKeywords.SUPPORTING_DOCUMENT, args -> handleSupportingDocument());
            handlerMap.put(EmailKeywords.FINAL_BILL_AND_DISCHARGE_SUMMARY, args -> handleFinalBillAndDischargeSummary(args[0], args[1]));
            handlerMap.put(EmailKeywords.PRE_AUTH, args -> handlePreAuth(args[0]));
            handlerMap.put(EmailKeywords.CASHLESS_CREDIT_REQUEST, args -> handleCashlessCreditRequest(args[0], args[1], message));
            handlerMap.put(EmailKeywords.ADDITIONAL_INFORMATION, args -> handleAdditionalInformation(args[0], args[1], message));
    
            // Execute the appropriate handler based on the keyword
            Function<String[], Map<String, String>> handler = handlerMap.getOrDefault(keyword, 
                args -> {
                    Map<String, String> errorMap = new HashMap<>();
                    errorMap.put(EmailKeywords.ERROR, "No matching function found for keyword: " + keyword);
                    return errorMap;
                });
    
            // Call the handler and populate responseMap
            responseMap = handler.apply(new String[]{subject, body});

            // Fetch and upload attachments if user_id is present
            if (message != null && responseMap.containsKey("user_id")) {
                String userId = responseMap.get("user_id");
                Map<String, String> gcpResponse = fetchAttachments(message, userId);
                gcpPath = gcpResponse.get(EmailKeywords.GCP_PATH);
                gcpFileName = gcpResponse.get(EmailKeywords.GCP_FILE_NAME);
    
                // Include the GCP URL in the response map if successful
                if (gcpPath != null || gcpFileName != null) {
                    responseMap.put(EmailKeywords.GCP_PATH, gcpPath);
                    responseMap.put(EmailKeywords.GCP_FILE_NAME, gcpFileName);
                    responseMap.put(EmailKeywords.SUBJECT, subject);
                    responseMap.put(EmailKeywords.BODY, body);
                    markAsRead(message);
                } else {
                    markAsUnread(message);
                    responseMap.put("error", "Failed to upload attachments to GCP");
                    return responseMap;
                }
            }
    
        } catch (Exception e) {
            // Mark email as unread and rethrow the exception
            markAsUnread(message);
            throw e;
        } finally {
            close();
        }
        
        return responseMap;
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

    private Map<String, String> handleQueryReply(String subject, String body) {
        String khId = null;
        String claimNo = null;
        String patientName = null;

        String[] parts = subject.split("\\s+");

        for (int i = 0; i < parts.length; i++) {
            if (parts[i].startsWith("KH")) { //use regular expression
                khId = parts[i].trim();
            } else if (parts[i].startsWith("CL-")) {
                claimNo = parts[i].substring(3).trim();
            } else if (patientName == null && parts.length > i + 1 && parts[i].equalsIgnoreCase("For")) {
                patientName = parts[i + 1] + " " + parts[i + 2];
            } else if (patientName == null && parts.length > i  && parts[i].equalsIgnoreCase("For")) {
                patientName = parts[i + 1];
            }
        }

        Map<String, String> responseMap = new HashMap<>();

        if (khId != null && claimNo != null && patientName != null) {
            responseMap.put(EmailKeywords.TYPE, "QUERY REPLY");
            responseMap.put(EmailKeywords.PATIENT_NAME, patientName);
            responseMap.put(EmailKeywords.TPA_DESK_ID, khId);
            responseMap.put(EmailKeywords.CLAIM_NO, claimNo);
            responseMap.put(EmailKeywords.BODY, body);
            responseMap.put(EmailKeywords.SUBJECT, subject);
            responseMap.put(EmailKeywords.USER_ID, claimNo);
        } else {
            responseMap.put(EmailKeywords.ERROR, "Unable to extract name, kh_id, and cl_no from subject: " + subject);
        }

        return responseMap;
    }

    private Map<String, String> handleCashlessCreditRequest(String subject, String body, Message message) {
        String[] bodyLines = body.split("\n");
        for (String line : bodyLines) {
            if (line.startsWith(EmailKeywords.INITIAL_CASHLESS_APPROVED)) {
                return handleInitialCashlessCreditRequest(subject, body, message);
            } else if (line.startsWith(EmailKeywords.FINAL_CASHLESS_APPROVED)) {
                return handleFinalCashlessCreditRequest(subject, body, message);
            }
        }
        // If neither condition matches, return an error response
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put(EmailKeywords.ERROR, "Neither Initial nor Final Cashless Approved Amount found in body.");
        return responseMap;
    }

    private Map<String, String> handleInitialCashlessCreditRequest(String subject, String body, Message message) {
        String employeeName = null;
        String employeeCode = null;
        String claimNo = null;
        String initialCashlessApprovedAmount = null;
        String initialCashlessRequestAmount = null;

        // Extract approved ID from subject
        String[] subjectParts = subject.split("\\s+");
        for (int i = 0; i < subjectParts.length; i++) {
            if (subjectParts[i].equalsIgnoreCase("approved")) {
                employeeCode = subjectParts[i + 1].replace(",", "").trim();
            }
        }

        // Extract details from body
        String[] bodyLines = body.split("\n");
        for (String line : bodyLines) {
            line = line.trim();
            if (line.startsWith("Emloyee Name:-")) {
                employeeName = line.substring("Emloyee Name:-".length()).trim();
            } else if (line.startsWith("Employee Code:-")) {
                employeeCode = line.substring("Employee Code:-".length()).trim();
            } else if (line.startsWith("Claim No:-")) {
                claimNo = line.substring("Claim No:-".length()).trim();
            } else if (line.startsWith(EmailKeywords.INITIAL_CASHLESS_APPROVED)) {
                initialCashlessApprovedAmount = line.substring("Initial Cashless Approved Amount:-".length()).trim();
            } else if (line.startsWith("Cashless Request Amount:-")) {
                initialCashlessRequestAmount = line.substring("Cashless Request Amount:-".length()).trim();
            }
        }

        Map<String, String> responseMap = new HashMap<>();

        if (employeeName != null && employeeCode != null && claimNo != null && initialCashlessApprovedAmount != null
                && initialCashlessRequestAmount != null) {
            responseMap.put(EmailKeywords.TYPE, "CASHLESS CREDIT REQUEST");
            responseMap.put(EmailKeywords.EMPLOYEE_NAME, employeeName);
            responseMap.put(EmailKeywords.EMPLOYEE_CODE, employeeCode);
            responseMap.put(EmailKeywords.CLAIM_NO, claimNo);
            responseMap.put(EmailKeywords.INITIAL_CASHLESS_APPROVED_AMT, initialCashlessApprovedAmount);
            responseMap.put(EmailKeywords.INITIAL_CASHLESS_REQUEST_AMT, initialCashlessRequestAmount);
            responseMap.put(EmailKeywords.SUBJECT, subject);
            responseMap.put(EmailKeywords.BODY, body);
            responseMap.put(EmailKeywords.USER_ID, claimNo);

        } else {
            responseMap.put(EmailKeywords.ERROR, "Unable to extract all required details from subject and body.");
        }

        return responseMap;
    }

    private Map<String, String> handleFinalCashlessCreditRequest(String subject, String body, Message message){
        String employeeName = null;
        String employeeCode = null;
        String claimNo = null;
        String finalApprovedAmount = null;
        String finalCashlessRequestAmount = null;

        // Extract approved ID from subject
        String[] subjectParts = subject.split("\\s+");
        for (int i = 0; i < subjectParts.length; i++) {
            if (subjectParts[i].equalsIgnoreCase("approved")) {
                employeeCode = subjectParts[i + 1].replace(",", "").trim();
            }
        }

        // Extract details from body
        String[] bodyLines = body.split("\n");
        for (String line : bodyLines) {
            line = line.trim();
            if (line.startsWith("Emloyee Name:-")) {
                employeeName = line.substring("Emloyee Name:-".length()).trim();
            } else if (line.startsWith("Employee Code:-")) {
                employeeCode = line.substring("Employee Code:-".length()).trim();
            } else if (line.startsWith("Claim No:-")) {
                claimNo = line.substring("Claim No:-".length()).trim();
            } else if (line.startsWith(EmailKeywords.FINAL_CASHLESS_APPROVED)) {
                finalApprovedAmount = line.substring(EmailKeywords.FINAL_CASHLESS_APPROVED.length()).trim();
            } else if (line.startsWith("Cashless Request Amount:-")) {
                finalCashlessRequestAmount = line.substring("Cashless Request Amount:-".length()).trim();
            }
        }

        Map<String, String> responseMap = new HashMap<>();

        if (employeeName != null && employeeCode != null && claimNo != null && finalApprovedAmount != null
                && finalCashlessRequestAmount != null) {
            responseMap.put(EmailKeywords.TYPE, "CASHLESS CREDIT REQUEST");
            responseMap.put(EmailKeywords.EMPLOYEE_NAME, employeeName);
            responseMap.put(EmailKeywords.EMPLOYEE_CODE, employeeCode);
            responseMap.put(EmailKeywords.CLAIM_NO, claimNo);
            responseMap.put(EmailKeywords.FINAL_CASHLESS_APPROVED_AMT, finalApprovedAmount);
            responseMap.put(EmailKeywords.FINAL_CASHLESS_REQUEST_AMT, finalCashlessRequestAmount);
            responseMap.put(EmailKeywords.SUBJECT, subject);
            responseMap.put(EmailKeywords.BODY, body);
            responseMap.put(EmailKeywords.USER_ID, claimNo);

        } else {
            responseMap.put(EmailKeywords.ERROR, "Unable to extract all required details from subject and body.");
        }

        return responseMap;
    }

    private Map<String, String> handleAdditionalInformation(String subject, String body, Message message){
        String employeeCode = null;
        String claimNo = null;
        String documentRequired = null;
        String patientName = null;

        // Extract approved_id from subject
        String[] subjectParts = subject.split("\\s+");
        for (int i = 0; i < subjectParts.length; i++) {
            if (subjectParts[i].matches("\\d+")) {
                employeeCode = subjectParts[i].trim();
                break;
            }
        }

        // Extract information from body
        String[] bodyLines = body.split("\n");
        for (String line : bodyLines) {
            if (line.contains("Your Employee code No:-")) {
                employeeCode = line.split("Your Employee code No:-")[1].trim();
                employeeCode = employeeCode.split("\\s+")[0].trim();
            } else if (line.contains("Your claim intimation No:-")) {
                claimNo = line.split("Your claim intimation No:-")[1].trim();
            } else if (line.contains("Kindly provide :-")) {
                documentRequired = line.split("Kindly provide :-")[1].trim();
            } else if (line.contains("Patient Name:-")) {
                patientName = line.split("Patient Name:-")[1].trim();
                // patientName = patientName.split("\\s+")[0].trim();
            }
        }

        Map<String, String> responseMap = new HashMap<>();

        if (employeeCode != null && claimNo != null) {
            responseMap.put(EmailKeywords.TYPE, "Addtional Information");
            responseMap.put(EmailKeywords.EMPLOYEE_CODE, employeeCode);
            responseMap.put(EmailKeywords.CLAIM_NO, claimNo);
            responseMap.put(EmailKeywords.DOCUMENT_REQUIRED, documentRequired);
            responseMap.put(EmailKeywords.PATIENT_NAME, patientName);
            responseMap.put(EmailKeywords.USER_ID, claimNo);
        } else {
            responseMap.put(EmailKeywords.ERROR, "Unable to extract all required details from subject and body.");
        }

        return responseMap;
    }

    private Map<String, String> handleFinalBillAndDischargeSummary(String subject, String body) {
        String khId = null;
        String claimNo = null;
        String patientName = null;

        String[] parts = subject.split("\\s+");

        for (int i = 0; i < parts.length; i++) {
            if (parts[i].startsWith("KH")) {
                khId = parts[i].trim();
            } else if (parts[i].startsWith("CLAIM") && i < parts.length - 1 && parts[i + 1].startsWith("NO-")) {
                if (i + 2 < parts.length) {
                    claimNo = parts[i + 2].trim();
                }
            } else if (patientName == null && parts.length > i + 1 && parts[i].equalsIgnoreCase("For")) {
                patientName = parts[i + 1] + " " + parts[i + 2];
            }
        }

        Map<String, String> responseMap = new HashMap<>();

        if (khId != null && claimNo != null && patientName != null) {
            responseMap.put(EmailKeywords.TYPE, "Final Bill And Discharge Summary");
            responseMap.put(EmailKeywords.PATIENT_NAME, patientName);
            responseMap.put(EmailKeywords.TPA_DESK_ID, khId);
            responseMap.put(EmailKeywords.CLAIM_NO, claimNo);
            responseMap.put(EmailKeywords.SUBJECT, subject);
            responseMap.put(EmailKeywords.BODY, body);
            responseMap.put(EmailKeywords.USER_ID, claimNo);
        } else {
            responseMap.put(EmailKeywords.ERROR, "Unable to extract name, kh_id, and claim_no from subject: " + subject);
        }

        return responseMap;
    }

    private Map<String,String> handlePreAuth(String subject) {
        String khId = null;
        String partneredUserId = null;
        String patientName = null;
        String[] parts = subject.split("\\s+");

        // Extraction of data values from the subject of pre-auth email
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].startsWith("KH")) {
                khId = parts[i].trim();
            } else if (parts[i].equalsIgnoreCase("Policy") && i < parts.length - 1
                    && parts[i + 1].equalsIgnoreCase("No:")) {
                if (i + 2 < parts.length) {
                    partneredUserId = parts[i + 2].trim();
                }
            } else if (patientName == null && parts.length > i + 1 && parts[i].equalsIgnoreCase("For")) {
                patientName = parts[i + 1] + " " + parts[i + 2];
            }
        }

        // Storing the data in a hashmap
        Map<String, String> responseMap = new HashMap<>();

        if (khId != null && partneredUserId != null && patientName != null) {
            responseMap.put(EmailKeywords.TYPE, "PRE AUTH");
            responseMap.put(EmailKeywords.PATIENT_NAME, patientName);
            responseMap.put(EmailKeywords.TPA_DESK_ID, khId);
            responseMap.put(EmailKeywords.POLICY_NO, partneredUserId);
            responseMap.put(EmailKeywords.SUBJECT,subject);
            responseMap.put(EmailKeywords.USER_ID, partneredUserId);
        } else {
            return Collections.emptyMap();
        }

        return responseMap;
    }

    private Map<String, String> extractKeywords(String subject, String body, String keyword) {

        //Will make a call to the database and pick up the matching regex for the given keyword
        Map<String, String> emailKeyword = new HashMap<>();
        emailKeyword.put("keyword", keyword);

        ApiResponse<Object> emailTemplateRequest = this.masterService.emailTemplate(emailKeyword);
       Map<String, Object> emailTemplateResponseData = (Map<String, Object>) emailTemplateRequest.getData();
       String emailTemplate = String.valueOf(emailTemplateResponseData.get("data"));  //"225";

        String regex = null;
        
        JSONObject regexJson = new JSONObject(emailTemplate);

        Map<String, String> extractedData = new HashMap<>();

        for (String key : regexJson.keySet()) {
            regex = regexJson.getString(key);
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
        return extractedData;
    }

    @Override
    public void close() throws MessagingException {
        super.close();
    }
}