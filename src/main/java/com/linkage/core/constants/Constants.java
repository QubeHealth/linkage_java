package com.linkage.core.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final Map<String, String> SUBSCRIPTION_EXPIRED_EMAIL;
    
    static {
        Map<String, String> emailMap = new HashMap<>();
        emailMap.put("SUBJECT", "Urgent: Your Qube App Subscription is About to Expire – Don’t Miss Out on Continued Savings!");
        emailMap.put("BODY", "Dear [HR Manager's Name], \n\n" +
        "Don’t miss this!! \n\n" +
        "[30] employees from [ION Exchange] already requested to renew their Qube App subscription. You can see the details in the file attached. \n\n" +
        "We are writing to inform you that you don’t miss out on these significant savings and the continued support your team has come to rely on. Look at the benefits and savings your employees have been enjoying. [Link to CD]\n\n" +
        "Renew your subscription today to keep these benefits flowing.\n\n" +
        "For assistance with the renewal process or any questions, please contact your Qube SPOC now.\n\n\n" +
        "Team QubeHealth."
        );
        SUBSCRIPTION_EXPIRED_EMAIL = Collections.unmodifiableMap(emailMap);
    }
}