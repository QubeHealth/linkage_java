package com.linkage.client;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;

import java.io.IOException;
import com.linkage.LinkageConfiguration;
import org.json.JSONObject;

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
        if (content instanceof String) {
            System.out.println("Email content is plain text.");
            return (String) content; // Handle plain text emails directly
        } else if (content instanceof Multipart) {
            System.out.println("Email content is multipart.");
            Multipart multipart = (Multipart) content;
            StringBuilder text = new StringBuilder();
            handleMultipart(multipart, text);
            return text.toString(); // Trim to remove any leading or trailing whitespace
        }
        System.out.println("Email content is of an unknown type.");
        return null;
    }

    private void handleMultipart(Multipart multipart, StringBuilder text) throws MessagingException, IOException {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            String contentType = bodyPart.getContentType();
            
            System.out.println("Processing part with content type: " + contentType);
            
            // Handle plain text parts
            if (contentType.startsWith("text/plain")) {
                text.append(bodyPart.getContent().toString().trim()).append("\n"); // Append and trim to remove extra whitespace
            }
        }
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
        } else {
            JSONObject errorJson = new JSONObject();
            errorJson.put("error", "No matching function found for keyword: " + keyword);
            return errorJson.toString();
        }
    }

    private String parseSubjectForKeyword(String subject) {
        String[] keywords = { "supporting document", "query reply", "final bill and discharge summary", "pre auth" };
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
    public void fetchAttachments(Message message, String saveDirectory) {
        
    }
}
