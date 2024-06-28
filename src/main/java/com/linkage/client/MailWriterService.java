package com.linkage.client;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.linkage.LinkageConfiguration;

public class MailWriterService extends BaseServiceClient {
   
    public MailWriterService(LinkageConfiguration configuration) {
        super(configuration);
    }

    public void mailSender(){
        // Recipient's email ID
        String to = "";

        // Sender's email ID
        String from = "";

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
        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("", "");
            }
        });

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field
            message.setFrom(new InternetAddress(from));

            // Set To: header field
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject("This is the Subject Line!");

            // Now set the actual message
            message.setText("This is the actual message body.");

            // Send message
            Transport.send(message);
            System.out.println("Sent message successfully...");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
    



}