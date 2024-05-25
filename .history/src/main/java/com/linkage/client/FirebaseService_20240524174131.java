package com.linkage.client;

import java.util.Map;

import com.linkage.api.ApiResponse;

import jakarta.ws.rs.core.MultivaluedHashMap;

public class FirebaseService {
    public ApiResponse<Map<String, Object>>getFirbaseUrl() {

        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();
    
    
        Map<String, Object> body = Map.of(
            "dynamicLinkInfo", Map.of(
                "domainUriPrefix", "https://qubehealth.page.link",
                "link", "https://www.qubehealth.com/cd=dkkd",
                "androidInfo", Map.of(
                    "androidPackageName", "com.qubehealth"
                ),
                "iosInfo", Map.of(
                    "iosBundleId", "com.qubehealth"
                )
            )
        );
    
        ApiResponse<Object> response = this.networkCallExternalService(
            "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=YOUR_API_KEY",
            "post",
            body,
            header
        );
    
        Map<String, Object> responseData = (Map<String, Object>) response.getData();
        Boolean status = Integer.parseInt(responseData.get("status").toString()) == 1;
        String message = responseData.get("message").toString();
        Map<String, Object> data = (Map<String, Object>) responseData.get("result");
    
        return new ApiResponse<>(status, message, data);
    }
}
