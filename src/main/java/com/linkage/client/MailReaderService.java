package com.linkage.client;

import java.io.IOException;

import javax.mail.Message;
import javax.mail.MessagingException;

import com.linkage.LinkageConfiguration;
import com.linkage.core.validations.EmailSchema;

public class MailReaderService extends EmailFetcher {
   
    public MailReaderService(String host, String port, String user, String password,LinkageConfiguration configuration) {
        //super(host,port,user,password,configuration);
        super("imap.gmail.com","993","qubetestemailssend@gmail.com","vuopgzdlbsyzmwoo",configuration);
    }

    public void connect() throws MessagingException{
        super.connect();
    }

    public String fetchSubject(Message message) throws MessagingException {
        return message.getSubject();
    }

    public String fetchBody(Message message){
        return null;
    }
    public void fetchAttachments(Message message, String saveDirectory){
        return;
    }


}
