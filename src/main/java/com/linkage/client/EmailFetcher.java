package com.linkage.client;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import com.linkage.core.validations.EmailSchema;

import javax.mail.*;

import com.linkage.LinkageConfiguration;

public abstract class EmailFetcher extends BaseServiceClient{
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

    public Message fetchLatestEmail() throws MessagingException {
        EmailSchema latestEmail = null;

        // Set up properties for the email session
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");

        // Create the email session
        Session emailSession = Session.getDefaultInstance(properties);
        Store store = emailSession.getStore();
        store.connect(host, user, password);

        // Open the inbox folder
        Folder emailFolder = store.getFolder("INBOX");
        emailFolder.open(Folder.READ_ONLY);

        Message message = null;
        // Fetch the latest message from the inbox
        int messageCount = emailFolder.getMessageCount();
        if (messageCount > 0) {
            message = emailFolder.getMessage(messageCount);
            latestEmail = new EmailSchema();
        }
        return message;
    }

    public abstract String fetchSubject(Message message) throws MessagingException;

    public abstract String fetchBody(Message message) throws Exception;

    public abstract void fetchAttachments(Message message, String saveDirectory) throws Exception;

    public void close() throws MessagingException {
        if (inbox != null && inbox.isOpen()) {
            inbox.close(false);
        }
        if (store != null) {
            store.close();
        }
    }
}