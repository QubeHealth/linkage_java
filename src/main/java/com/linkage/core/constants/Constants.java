package com.linkage.core.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final Map<String, String> SUBSCRIPTION_EXPIRED_EMAIL;
    
    static {
        Map<String, String> emailMap = new HashMap<>();
        emailMap.put("SUBJECT", "Urgent: Your Qube App Subscription is About to Expire – Don’t Miss Out on Continued Savings!");
        SUBSCRIPTION_EXPIRED_EMAIL = Collections.unmodifiableMap(emailMap);
    }
}