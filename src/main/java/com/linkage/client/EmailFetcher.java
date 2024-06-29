package com.linkage.client;

import java.io.IOException;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import com.linkage.LinkageConfiguration;

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

        Message[] messages = inbox.getMessages();
        for (int i = 0; i < 5; i++) {
            Object content = messages[i].getContent().toString();

            String subject = messages[i].getSubject();
            String description = messages[i].getDescription();
            int size = messages[i].getSize();

            String textContent = getTextFromMimeMultipart((Multipart) content);

            System.out.println("Message " + (i + 1));
            System.out.println("Subject: " + subject);
            System.out.println("Description: " + description);
            System.out.println("Size: " + size);
            System.out.println("Content:\n" + textContent);
            System.out.println("--------------------------");
        }

        int messageCount = inbox.getMessageCount();
        if (messageCount > 0) {
        }
        return message;
    }

    private String getTextFromMimeMultipart(Multipart content) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        int count = content.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = content.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent());
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result.append(org.jsoup.Jsoup.parse(html).text()); // Using Jsoup to convert HTML to plain text
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
