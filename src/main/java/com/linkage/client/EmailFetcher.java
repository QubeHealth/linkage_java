package com.linkage.client;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

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
        int messageCount = inbox.getMessageCount();

        if (messageCount > 0) {
            message = inbox.getMessage(messageCount);
        }
        
        return message;
    }

    
    public abstract String fetchSubject(Message message) throws MessagingException;

    public abstract String fetchBody(Message message) throws IOException, MessagingException;

    public abstract void fetchAttachments(Message message) throws IOException, MessagingException;

    public void close() throws MessagingException {
        if (inbox != null && inbox.isOpen()) {
            inbox.close(false);
        }
        if (store != null) {
            store.close();
        }
    }
}
