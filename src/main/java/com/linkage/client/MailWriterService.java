package com.linkage.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.linkage.LinkageConfiguration;

public class MailWriterService extends EmailFetcher {

    public MailWriterService(LinkageConfiguration configuration) {
        super("imap.gmail.com", "993", "qubetestemailssend@gmail.com", "vuopgzdlbsyzmwoo", configuration);
    }

    public String mailSender() {
        try {
            // Fetch the latest email
            connect();
            Message mailMessage = fetchLatestEmail();
            String subject = fetchSubject(mailMessage);
            String body = fetchBody(mailMessage);
            Object content = mailMessage.getContent();

            String keyword = parseSubjectForKeyword(subject);

            String recipient = null;

            if ("supporting document".equalsIgnoreCase(keyword) || "query reply".equalsIgnoreCase(keyword)
                    || "addtional information".equalsIgnoreCase(keyword)) {
                recipient = "tmt.9@qubehealth.com";
            } else if ("pre auth".equalsIgnoreCase(keyword) || "cashless credit request".equalsIgnoreCase(keyword)
                    || "final bill and discharge summary".equalsIgnoreCase(keyword)) {
                recipient = "tmt.8@qubehealth.com";
            }

            // Sender's email ID
            String from = "qubetestemailssend@gmail.com";

            // Assuming you are sending email from a SMTP server
            String host = "smtp.gmail.com";

            // Get system properties
            Properties properties = System.getProperties();

            // Setup mail server
            properties.setProperty("mail.smtp.host", host);
            properties.setProperty("mail.smtp.port", "587"); // Change port if needed
            properties.setProperty("mail.smtp.auth", "true");
            properties.setProperty("mail.smtp.starttls.enable", "true");

            // Get the default Session object.
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("qubetestemailssend@gmail.com", "vuopgzdlbsyzmwoo");
                }
            });

            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field
            message.setFrom(new InternetAddress(from));

            // Set To: header field
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

            // Set Subject: header field
            message.setSubject(subject);

            // Now set the actual message
            message.setText(body);

            MimeMultipart multipart = (MimeMultipart) content;
            message.setContent(multipart);

            // Send message
            Transport.send(message);
            System.out.println("Sent message successfully...");
            close();
            return "success";
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            return "failure";
        }
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
}
