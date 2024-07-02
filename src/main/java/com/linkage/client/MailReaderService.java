package com.linkage.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;
import javax.mail.Part;

import org.json.JSONObject;
import org.jsoup.Jsoup;

import com.linkage.LinkageConfiguration;

public class MailReaderService extends EmailFetcher {
   
    
    public MailReaderService(String host, String port, String user, String password, LinkageConfiguration configuration) {
        super("imap.gmail.com", "993", "qubetestemailssend@gmail.com", "vuopgzdlbsyzmwoo", configuration);
    }

    public void connect() throws MessagingException {
        super.connect();
    }

    @Override
    public String fetchSubject(Message message) throws MessagingException {
        return message.getSubject();
    }

    @Override
    public String fetchBody(Message message) throws IOException, MessagingException {
        Object content = message.getContent();
        String textContent = "";
            if (content instanceof String) {
                textContent = (String) content;
            } else if (content instanceof Multipart) {
                textContent = getTextFromMimeMultipart((Multipart) content);
            }
        return textContent;
    }

    @Override
    public void fetchAttachments(Message message) throws IOException, MessagingException {
        Object content = message.getContent();

        if (content instanceof MimeMultipart) {
            MimeMultipart multipart = (MimeMultipart) content;
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    String fileName = bodyPart.getFileName();
                    if (fileName != null && !fileName.isEmpty()) {
                        String filePath = "C:\\Users\\ADMIN\\Downloads" + File.separator + fileName;

                        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                            bodyPart.getInputStream().transferTo(outputStream);
                        } catch (IOException | MessagingException e) {
                            e.printStackTrace();
                            throw new IOException("Error downloading attachment: " + fileName, e);
                        }

                        System.out.println("Attachment downloaded");
                    }
                }
            }
        }
    }

    private String getTextFromMimeMultipart(Multipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent());
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result.append(Jsoup.parse(html).text()); // Using Jsoup to convert HTML to plain text
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    public String fetchAndProcessEmail() throws MessagingException, IOException {
        connect();
        Message message = fetchLatestEmail();
        String subject = fetchSubject(message);
        String body = fetchBody(message);

        String keyword = parseSubjectForKeyword(subject);

        if ("query reply".equalsIgnoreCase(keyword)) {
            return handleQueryReply(subject, body); // Comment: Handles 'query reply' emails
        } else if ("supporting document".equalsIgnoreCase(keyword)) {
            return handleSupportingDocument(); // Comment: Handles 'supporting document' emails
        } else if ("final bill and discharge summary".equalsIgnoreCase(keyword)) {
            return handleFinalBillAndDischargeSummary(subject, body); // Comment: Handles 'final bill and discharge summary' emails
        } else if ("pre auth".equalsIgnoreCase(keyword)) {
            return handlePreAuth(subject); // Comment: Handles 'pre auth' emails
        } else if ("cashless credit request".equalsIgnoreCase(keyword)) {
            return handleCashlessCreditRequest(subject,body,message);
        } else if ("addtional information".equalsIgnoreCase(keyword)) {
            return handleAddtionalInformation(subject, body, message);
        } else {
            return "No matching function found for keyword: " + keyword;
        }
    }

    private String parseSubjectForKeyword(String subject) {
        String[] keywords = { "supporting document", "query reply", "final bill and discharge summary", "pre auth","cashless credit request", "addtional information" };
        String lowerCaseSubject = subject.toLowerCase();

        for (String keyword : keywords) {
            if (lowerCaseSubject.contains(keyword.toLowerCase())) {
                return keyword;
            }
        }
        return null;
    }

    private String handleSupportingDocument() {
        JSONObject json = new JSONObject();
        json.put("message", "Handling for supporting document");
        return json.toString();
    }

    private String handleQueryReply(String subject, String body) {
        String khiladiId = null;
        String clNo = null;
        String name = null;

        String[] parts = subject.split("\\s+");

        for (int i = 0; i < parts.length; i++) {
            if (parts[i].startsWith("KH")) {
                khiladiId = parts[i].trim();
            } else if (parts[i].startsWith("CL-")) {
                clNo = parts[i].trim();
            } else if (name == null && parts.length > i + 1 && parts[i].equalsIgnoreCase("For")) {
                name = parts[i + 1] + " " + parts[i + 2];
            }
        }

        if (khiladiId != null && clNo != null && name != null) {
            JSONObject json = new JSONObject();
            json.put("Type", "query reply");
            json.put("name", name);
            json.put("khiladi_id", khiladiId);
            json.put("cl_no", clNo);
            json.put("body", body);
            return json.toString();
        } else {
            JSONObject errorJson = new JSONObject();
            errorJson.put("error", "Unable to extract name, khiladi_id, and cl_no from subject: " + subject);
            return errorJson.toString();
        }
    }

    private String handleCashlessCreditRequest(String subject, String body, Message message) throws IOException, MessagingException {
        String employeeName = null;
        String employeeCode = null;
        String claimNo = null;
        String finalApprovedAmount = null;
        String cashlessRequestAmount = null;
    
        fetchAttachments(message);
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
                cashlessRequestAmount = line.substring("Cashless Request Amount:-".length()).trim();
            }
        }
    
        if (employeeName != null && employeeCode != null && claimNo != null && finalApprovedAmount != null && cashlessRequestAmount != null) {
            JSONObject json = new JSONObject();
            json.put("Type", "cashless credit request");
            json.put("employee_name", employeeName);
            json.put("employee_code", employeeCode);
            json.put("claim_no", claimNo);
            json.put("final_cashless_approved_amount", finalApprovedAmount);
            json.put("cashless_request_amount", cashlessRequestAmount);
            return json.toString();
        } else {
            JSONObject errorJson = new JSONObject();
            errorJson.put("error", "Unable to extract all required details from subject and body.");
            return errorJson.toString();
        }
    }

    private String handleAddtionalInformation(String subject, String body, Message message) throws IOException, MessagingException {
        String employeeCode = null;
        String claimNo = null;
        String documentRequired = null;
        String patientName = null;

        fetchAttachments(message); 

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
                //patientName = patientName.split("\\s+")[0].trim();
            }
        }

        if (employeeCode != null && claimNo != null) {
            JSONObject json = new JSONObject();
            json.put("Type", "Addtional Information");
            json.put("employee_code", employeeCode);
            json.put("claim_no", claimNo);
            json.put("document_required", documentRequired);
            json.put("patient_name", patientName);
            return json.toString();
        } else {
            JSONObject errorJson = new JSONObject();
            errorJson.put("error", "Unable to extract all required details from subject and body.");
            return errorJson.toString();
        }
    }

    private String handleFinalBillAndDischargeSummary(String subject, String body) {
        String khiladiId = null;
        String claimNo = null;
        String name = null;

        String[] parts = subject.split("\\s+");

        for (int i = 0; i < parts.length; i++) {
            if (parts[i].startsWith("KH")) {
                khiladiId = parts[i].trim();
            } else if (parts[i].startsWith("CLAIM") && i < parts.length - 1 && parts[i + 1].startsWith("NO-")) {
                if (i + 2 < parts.length) {
                    claimNo = parts[i + 2].trim();
                }
            } else if (name == null && parts.length > i + 1 && parts[i].equalsIgnoreCase("For")) {
                name = parts[i + 1] + " " + parts[i + 2];
            }
        }

        if (khiladiId != null && claimNo != null && name != null) {
            JSONObject json = new JSONObject();
            json.put("Type", "Final Bill And Discharge Summary");
            json.put("name", name);
            json.put("khiladi_id", khiladiId);
            json.put("claim_no", claimNo);
            json.put("body", body); // Assuming 'body' needs to be included here
            return json.toString();
        } else {
            JSONObject errorJson = new JSONObject();
            errorJson.put("error", "Unable to extract name, khiladi_id, and claim_no from subject: " + subject);
            return errorJson.toString();
        }
    }

    private String handlePreAuth(String subject) {
        String khiladiId = null;
        String policyNo = null;
        String name = null;

        String[] parts = subject.split("\\s+");

        for (int i = 0; i < parts.length; i++) {
            if (parts[i].startsWith("KH")) {
                khiladiId = parts[i].trim();
            } else if (parts[i].equalsIgnoreCase("Policy") && i < parts.length - 1 && parts[i + 1].equalsIgnoreCase("No:")) {
                if (i + 2 < parts.length) {
                    policyNo = parts[i + 2].trim();
                }
            } else if (name == null && parts.length > i + 1 && parts[i].equalsIgnoreCase("For")) {
                name = parts[i + 1] + " " + parts[i + 2];
            }
        }

        if (khiladiId != null && policyNo != null && name != null) {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("khiladi_id", khiladiId);
            json.put("policy_no", policyNo);
            return json.toString();
        } else {
            JSONObject errorJson = new JSONObject();
            errorJson.put("error", "Unable to extract name, khiladi_id, and policy_no from subject: " + subject);
            return errorJson.toString();
        }
    }

    @Override
    public void close() throws MessagingException {
        super.close();
    }
}
