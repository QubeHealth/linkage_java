package com.linkage.client;

import java.util.HashMap;
import java.util.Map;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;

public class FirebaseService extends BaseServiceClient {
     public FirebaseService(LinkageConfiguration configuration) {
        super(configuration);
    }
    public ApiResponse<Object> getFirebaseShortUrl(String name, String firstName, String status) {
        String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key="+configuration.getFireBaseWebApiKey();
        Map<String, Object> requestBody = new HashMap<>();
        
        return this.callThirdPartyApi(url, "post", requestBody, null);
    }

}
