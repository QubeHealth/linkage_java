package com.linkage.service;

import com.linkage.LinkageConfiguration;
import com.linkage.client.SmsClient;

public class SmsService extends BaseService {

    private final SmsClient smsClient;

    public SmsService(LinkageConfiguration configuration) {
        super(configuration);
        this.smsClient = new SmsClient(configuration);
    }

    public String sendOtpMessage(String phoneNumber, String otp, String expiryTime) {
        // DLT Template ID and entity-specific ID from configuration
        String dltTemplateId = configuration.getSmsConfig().getDltOtpSmsTemplateId();
        String otpTemplate = "Dear User, %s is your login OTP into Qubehealth App. OTP is valid for %s mins.";
        return  smsClient.sendMessage(phoneNumber, dltTemplateId, otpTemplate, otp, "5 minutes");
    }
}