package com.linkage.client;

import java.io.IOException;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import com.linkage.LinkageConfiguration;
import org.jsoup.Jsoup;

public abstract class EmailFetcher extends BaseServiceClient {
    protected String host;
    protected String port;
    protected String user;
    protected String password;
    protected Store store;
    protected Folder inbox;

    public EmailFetcher(String host, String port, String user, String password, LinkageConfiguration configuration) {
        super(configuration);
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public void connect() throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", port);
        properties.put("mail.imap.ssl.enable", "true");

        Session emailSession = Session.getDefaultInstance(properties);
        store = emailSession.getStore("imaps");
        store.connect(host, user, password);
        inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
    }

    public Message fetchLatestEmail() throws MessagingException, IOException {
        Message message = null;
        int messageCount = inbox.getMessageCount();

        if (messageCount > 0) {
            message = inbox.getMessage(messageCount);
            Object content = message.getContent();

            String subject = message.getSubject();
            String description = message.getDescription();
            int size = message.getSize();

            String textContent = "";
            if (content instanceof String) {
                textContent = (String) content;
            } else if (content instanceof Multipart) {
                textContent = getTextFromMimeMultipart((Multipart) content);
            }

            System.out.println("Latest Message");
            System.out.println("Subject: " + subject);
            System.out.println("Description: " + description);
            System.out.println("Size: " + size);
            System.out.println("Content:\n" + textContent);
            System.out.println("--------------------------");
        }
        return message;
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

    public abstract String fetchSubject(Message message) throws MessagingException;

    public abstract String fetchBody(Message message) throws IOException, MessagingException;

    public abstract void fetchAttachments(Message message, String saveDirectory);

    public void close() throws MessagingException {
        if (inbox != null && inbox.isOpen()) {
            inbox.close(false);
        }
        if (store != null) {
            store.close();
        }
    }
}
