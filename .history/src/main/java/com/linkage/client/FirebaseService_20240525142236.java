package com.linkage.client;

import java.util.Map;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;

public class FirebaseService extends BaseServiceClient {
    public FirebaseService(LinkageConfiguration configuration) {
        super(configuration);
    }

    public ApiResponse<Object> getFirebaseShortUrl(Map<String, Object> requestBody) {
        String url = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key="
                + configuration.getFireBaseWebApIKey();

        return this.networkCallInternalService(url, "post", requestBody, null);
    }

}
