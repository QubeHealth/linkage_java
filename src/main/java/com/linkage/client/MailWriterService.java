package com.linkage.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SubjectTerm;

import com.linkage.LinkageConfiguration;
import com.linkage.core.constants.Constants.EmailKeywords;

public class MailWriterService extends EmailFetcher {

    public MailWriterService(LinkageConfiguration configuration) {
        super(configuration);
    }

    public void mailSender(Message sendMessage) throws MessagingException {
        try {
            // Fetch the latest email
            connect();
            MimeMessage mailMessage = (MimeMessage) sendMessage;
            String subject = fetchSubject(mailMessage);

            String keyword = parseSubjectForKeyword(subject);

            String recipient = null;

            if (EmailKeywords.adjudicatorKeywords.contains(keyword.toLowerCase())) {
                recipient = configuration.getAdjudicatorMail();
            } else if (EmailKeywords.tpaKeywords.contains(keyword.toLowerCase())) {
                recipient = configuration.getTpaMail();
            }

            // Sender's email ID
            String from = configuration.getMailId();

            // Assuming you are sending email from a SMTP server
            String host = configuration.getMailHost();

            // Get system properties
            Properties properties = System.getProperties();

            // Setup mail server
            properties.setProperty("mail.smtp.host", host);
            properties.setProperty("mail.smtp.port", configuration.getMailWriterPort()); // Change port if needed
            properties.setProperty("mail.smtp.auth", "true");
            properties.setProperty("mail.smtp.starttls.enable", "true");

            // Get the default Session object.
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(configuration.getMailId(), configuration.getPasskey());
                }
            });

            //Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            //Set From: header field
            message.setFrom(new InternetAddress(from));

            //Set To: header field
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

            // Set Subject: header field
            message.setSubject(subject);

            // Now set the actual message
            message.setContent(mailMessage.getContent(), mailMessage.getContentType());

            // Send message
            Transport.send(message);
            logger.info("Sent message successfully...");
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        } finally {
            close(); // Ensure close is called in all cases
        }
    }

    private String parseSubjectForKeyword(String subject) {
        String[] keywords = EmailKeywords.keywordsArray;
        String lowerCaseSubject = subject.toLowerCase();

        for (String keyword : keywords) {
            if (lowerCaseSubject.contains(keyword.toLowerCase())) {
                return keyword;
            }
        }
        return null;
    }

    public String markEmailUnread(String subject) throws MessagingException {
        try {
            connect(); // Ensure inbox is initialized
    
            if (inbox == null || !inbox.isOpen()) {
                throw new MessagingException("INBOX folder is not initialized or not open");
            }
    
            Message[] messages = inbox.search(new SubjectTerm(subject));
    
            for (int i = messages.length - 1; i >= 0; i--) {
                Message message = messages[i];
                message.setFlag(Flags.Flag.SEEN, false); // Mark message as unread
                return "Email marked as unread"; // Exit method after marking the first matching message
            }

            
            return "Email not found"; // If no message with the specified subject is found
        } catch (MessagingException e) {
            e.printStackTrace();
            return "Failure: " + e.getMessage(); // Handle and log the exception
        } finally {
            close();
        }
    }
    
}
