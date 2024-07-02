package com.linkage.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

import org.jsoup.Jsoup;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.utility.GcpFileUpload;
import com.linkage.utility.Helper;

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
        }

        return message;
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

    public String fetchAttachments(Message message, String userId) throws IOException, MessagingException {
        Object content = message.getContent();
        String gcpPath = null;
        String gcpUrl = null;

        if (content instanceof MimeMultipart) {
            MimeMultipart multipart = (MimeMultipart) content;
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    String documentId = UUID.randomUUID().toString();
                    String fileName = Helper.md5Encryption(userId) + "/estimation/" + documentId + ".pdf";

                    String contentType = bodyPart.getContentType();
                    InputStream fileContent = bodyPart.getInputStream();

                  ApiResponse<String> gcpRes =   GcpFileUpload.uploadEmailAttachments(contentType, fileName, fileContent.readAllBytes(), contentType, true);
                        gcpUrl = gcpRes.getData();
                       // gcpPath = GcpFileUpload.getSignedUrl(contentType,gcpUrl);


                    // if (fileName != null && !fileName.isEmpty()) {
                    //     String filePath = "C:\\Users\\ADMIN\\Downloads" + File.separator + fileName;

                    //     try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                    //         bodyPart.getInputStream().transferTo(outputStream);
                    //     } catch (IOException | MessagingException e) {
                    //         e.printStackTrace();
                    //         throw new IOException("Error downloading attachment: " + fileName, e);
                    //     }
                    // }
                }
            }
        }
        return gcpUrl;
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
