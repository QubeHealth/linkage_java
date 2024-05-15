package com.linkage.api;

import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

public class ApiRequest {
    protected String url;
    protected String method;
    protected Object body;
    protected MultivaluedMap<String, Object> headers;

    // Constructor
    public ApiRequest(String url, String method, Object body, MultivaluedMap<String, Object> headers) {
        this.url = url;
        this.method = method;
        this.body = body;
        this.headers = headers;

    }

    // Getters and setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public MultivaluedMap<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Object headers) {
        this.headers = convertToMultivaluedMap(headers);
    }

    private static MultivaluedMap<String, Object> convertToMultivaluedMap(Object headers) {
        MultivaluedMap<String, Object> multivaluedMap = new MultivaluedHashMap<>();
        if (headers instanceof Map<?, ?>) {
            Map<?, ?> rawHeadersMap = (Map<?, ?>) headers;
            for (Map.Entry<?, ?> entry : rawHeadersMap.entrySet()) {
                if (entry.getKey() instanceof String && entry.getValue() instanceof List<?>) {
                    String key = (String) entry.getKey();
                    List<?> value = (List<?>) entry.getValue();
                    multivaluedMap.addAll(key, value);
                }
            }
        }
        return multivaluedMap;
    }
}
