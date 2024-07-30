package com.linkage.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import org.jsoup.Jsoup;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.utility.GcpFileUpload;
import com.linkage.utility.Helper;

public abstract class EmailFetcher extends BaseServiceClient {
    protected Store store;
    protected Folder inbox;

    protected EmailFetcher(LinkageConfiguration configuration) {
        super(configuration);
    }

    protected void connect() throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imap.host", configuration.getMailHost());
        properties.put("mail.imap.port", configuration.getMailPort());
        properties.put("mail.imap.ssl.enable", "true");

        Session emailSession = Session.getDefaultInstance(properties);
        store = emailSession.getStore("imaps");
        store.connect(configuration.getMailHost(), configuration.getMailId(), configuration.getPasskey());
        inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
    }

    public Set<Message> getUnreadEmail() throws MessagingException {
        connect();
        Set<Message> unreadMessages = new HashSet<>();

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);

        // Use FlagTerm to search for unread messages
        FlagTerm unreadFlagTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
        Message[] messages = inbox.search(unreadFlagTerm);

        // Iterate over messages and store them in the list in the order they are fetched
        for (Message message : messages) {
                unreadMessages.add(message);
                message.setFlag(Flags.Flag.SEEN, true); // Mark message as read
        }
        return unreadMessages;
    }

    public String fetchSubject(Message message) throws MessagingException {
        return message.getSubject();
    }

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

    public Map<String,String> fetchAttachments(Message message, String userId) throws Exception {
        Object content = message.getContent();
        String gcpUrl = null;
        String gcpFileName = null;
        Map<String,String> gcpResponse = new HashMap<>();

        if (content instanceof MimeMultipart) {
            MimeMultipart multipart = (MimeMultipart) content;
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    String documentId = UUID.randomUUID().toString();
                    gcpFileName = Helper.md5Encryption(userId) + "/estimation/" + documentId + ".pdf";
                    String contentType = "APPLICATION/PDF";
                    InputStream fileContent = bodyPart.getInputStream();

                    ApiResponse<String> gcpUploadResponse =   GcpFileUpload.uploadEmailAttachments(GcpFileUpload.USER_DATA_BUCKET, gcpFileName, fileContent.readAllBytes(), contentType, true);
                    if (!gcpUploadResponse.getStatus()){
                        throw new Exception("Attachments failed to upload");
                    }
                    logger.info("Attachments uploaded successfully");
                    gcpUrl = gcpUploadResponse.getData();
                }
            }
        }
        gcpResponse.put("gcp_path", gcpUrl);
        gcpResponse.put("gcp_file_name", gcpFileName);

        return gcpResponse;
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

    public void close() throws MessagingException {
        if (inbox != null && inbox.isOpen()) {
            inbox.close(false);
        }
        if (store != null) {
            store.close();
        }
    }
}
