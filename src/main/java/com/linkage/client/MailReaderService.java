package com.linkage.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.linkage.LinkageConfiguration;
import com.linkage.core.constants.Constants;

public class MailReaderService extends EmailFetcher {

    public MailReaderService(String host, String port, String user, String password,
            LinkageConfiguration configuration) {
        super("imap.gmail.com", "993", "qubetestemailssend@gmail.com", "vuopgzdlbsyzmwoo", configuration);
    }

    public void connect() throws MessagingException {
        super.connect();
    }

    public Map<String, String> fetchAndProcessEmail() throws MessagingException, IOException {
        connect();
        Message message = fetchLatestEmail();
        Map<String, String> responseMap = new HashMap<>();
        try {
            String subject = fetchSubject(message);
            String body = fetchBody(message);

            String keyword = parseSubjectForKeyword(subject);

            if (keyword == null) {
                // Mark email as unread
                markAsUnread(message);
                responseMap = new HashMap<>();
                responseMap.put("error", "No matching function found for keyword: " + keyword);
            } else if ("query reply".equalsIgnoreCase(keyword)) {
                responseMap = handleQueryReply(subject, body);
            } else if ("supporting document".equalsIgnoreCase(keyword)) {
                responseMap = handleSupportingDocument();
            } else if ("final bill and discharge summary".equalsIgnoreCase(keyword)) {
                responseMap = handleFinalBillAndDischargeSummary(subject, body);
            } else if ("pre auth".equalsIgnoreCase(keyword)) {
                responseMap = handlePreAuth(subject);
            } else if ("cashless credit request".equalsIgnoreCase(keyword)) {
                responseMap = handleCashlessCreditRequest(subject, body, message);
            } else if ("additional information".equalsIgnoreCase(keyword)) {
                responseMap = handleAddtionalInformation(subject, body, message);
            } else {
                responseMap = new HashMap<>();
                responseMap.put("error", "No matching function found for keyword: " + keyword);
            }

            markAsRead(message);

        } catch (Exception e) {
            // Mark email as unread
            markAsUnread(message);
            throw e;
        } finally {
            close();
        }
        return responseMap;
    }

    private String parseSubjectForKeyword(String subject) {
        String[] keywords = { "supporting document", "query reply", "final bill and discharge summary", "pre auth",
                "cashless credit request", "addtional information" };
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
            if (parts[i].startsWith("KH")) {
                khId = parts[i].trim();
            } else if (parts[i].startsWith("CL-")) {
                claimNo = parts[i].trim();
            } else if (patientName == null && parts.length > i + 1 && parts[i].equalsIgnoreCase("For")) {
                patientName = parts[i + 1] + " " + parts[i + 2];
            } else if (patientName == null && parts.length > i  && parts[i].equalsIgnoreCase("For")) {
                patientName = parts[i + 1];
            }
        }

        Map<String, String> responseMap = new HashMap<>();

        if (khId != null && claimNo != null && patientName != null) {
            responseMap.put("type", "QUERY REPLY");
            responseMap.put("patient_name", patientName);
            responseMap.put("tpa_desk_id", khId);
            responseMap.put("claim_no", claimNo);
            responseMap.put("body", body);
            responseMap.put("subject", subject);
        } else {
            responseMap.put("error", "Unable to extract name, kh_id, and cl_no from subject: " + subject);
        }

        return responseMap;
    }

    private Map<String, String> handleCashlessCreditRequest(String subject, String body, Message message)
            throws IOException, MessagingException {
        String[] bodyLines = body.split("\n");
        for (String line : bodyLines) {
            if (line.startsWith("Initial Cashless Approved Amount:-")) {
                return handleInitialCashlessCreditRequest(subject, body, message);
            } else if (line.startsWith("Final Cashless Approved Amount:-")) {
                return handleFinalCashlessCreditRequest(subject, body, message);
            }
        }
        // If neither condition matches, return an error response
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("error", "Neither Initial nor Final Cashless Approved Amount found in body.");
        return responseMap;
    }

    private Map<String, String> handleInitialCashlessCreditRequest(String subject, String body, Message message)
            throws IOException, MessagingException {
        String employeeName = null;
        String employeeCode = null;
        String claimNo = null;
        String initialCashlessApprovedAmount = null;
        String initialCashlessRequestAmount = null;

        fetchAttachments(message, employeeCode);
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
            } else if (line.startsWith("Initial Cashless Approved Amount:-")) {
                initialCashlessApprovedAmount = line.substring("Initial Cashless Approved Amount:-".length()).trim();
            } else if (line.startsWith("Cashless Request Amount:-")) {
                initialCashlessRequestAmount = line.substring("Cashless Request Amount:-".length()).trim();
            }
        }

        Map<String, String> responseMap = new HashMap<>();

        if (employeeName != null && employeeCode != null && claimNo != null && initialCashlessApprovedAmount != null
                && initialCashlessRequestAmount != null) {
            responseMap.put("type", "cashless credit request");
            responseMap.put("employee_name", employeeName);
            responseMap.put("employee_code", employeeCode);
            responseMap.put("claim_no", claimNo);
            responseMap.put("initial_cashless_approved_amount", initialCashlessApprovedAmount);
            responseMap.put("initial_cashless_request_amount", initialCashlessRequestAmount);
            responseMap.put("subject", subject);
            responseMap.put("body", body);

        } else {
            responseMap.put("error", "Unable to extract all required details from subject and body.");
        }

        return responseMap;
    }

    private Map<String, String> handleFinalCashlessCreditRequest(String subject, String body, Message message)
            throws IOException, MessagingException {
        String employeeName = null;
        String employeeCode = null;
        String claimNo = null;
        String finalApprovedAmount = null;
        String finalCashlessRequestAmount = null;

        fetchAttachments(message, employeeCode);
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
            } else if (line.startsWith("Final Cashless Approved Amount:-")) {
                finalApprovedAmount = line.substring("Final Cashless Approved Amount:-".length()).trim();
            } else if (line.startsWith("Cashless Request Amount:-")) {
                finalCashlessRequestAmount = line.substring("Cashless Request Amount:-".length()).trim();
            }
        }

        Map<String, String> responseMap = new HashMap<>();

        if (employeeName != null && employeeCode != null && claimNo != null && finalApprovedAmount != null
                && finalCashlessRequestAmount != null) {
            responseMap.put("type", "CASHLESS CREDIT REQUEST");
            responseMap.put("employee_name", employeeName);
            responseMap.put("employee_code", employeeCode);
            responseMap.put("claim_no", claimNo);
            responseMap.put("final_cashless_approved_amount", finalApprovedAmount);
            responseMap.put("final_cashless_request_amount", finalCashlessRequestAmount);
            responseMap.put("subject", subject);
            responseMap.put("body", body);

        } else {
            responseMap.put("error", "Unable to extract all required details from subject and body.");
        }

        return responseMap;
    }
 
    
    private Map<String, String> handleAddtionalInformation(String subject, String body, Message message)
            throws IOException, MessagingException {
        String employeeCode = null;
        String claimNo = null;
        String documentRequired = null;
        String patientName = null;

        fetchAttachments(message, employeeCode);

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
            responseMap.put("type", "Additional Information");
            responseMap.put("employee_code", employeeCode);
            responseMap.put("claim_no", claimNo);
            responseMap.put("document_required", documentRequired);
            responseMap.put("patient_name", patientName);
        } else {
            responseMap.put("error", "Unable to extract all required details from subject and body.");
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
            responseMap.put("type", "Final Bill And Discharge Summary");
            responseMap.put("patient_name", patientName);
            responseMap.put("kh_id", khId);
            responseMap.put("claim_no", claimNo);
            responseMap.put("subject", subject);
            responseMap.put("body", body);
        } else {
            responseMap.put("error", "Unable to extract name, kh_id, and claim_no from subject: " + subject);
        }

        return responseMap;
    }

    private Map<String,String> handlePreAuth(String subject) {
        String khId = null;
        String policyNo = null;
        String patientName = null;

        String[] parts = subject.split("\\s+");

        // Extraction of data values from the subject of pre-auth email
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].startsWith("KH")) {
                khId = parts[i].trim();
            } else if (parts[i].equalsIgnoreCase("Policy") && i < parts.length - 1
                    && parts[i + 1].equalsIgnoreCase("No:")) {
                if (i + 2 < parts.length) {
                    policyNo = parts[i + 2].trim();
                }
            } else if (patientName == null && parts.length > i + 1 && parts[i].equalsIgnoreCase("For")) {
                patientName = parts[i + 1] + " " + parts[i + 2];
            }
        }

        // Storing the data in a hashmap
        Map<String, String> responseMap = new HashMap<>();

        if (khId != null && policyNo != null && patientName != null) {
            responseMap.put("patient_name", patientName);
            responseMap.put("kh_id", khId);
            responseMap.put("policy_no", policyNo);
            responseMap.put("subject",subject);
        } else {
            return null;
        }

        return responseMap;
    }

    @Override
    public void close() throws MessagingException {
        super.close();
    }
}



