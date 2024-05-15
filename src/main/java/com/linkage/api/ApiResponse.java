package com.linkage.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiResponse<T> {
    protected boolean status;
    protected String message;
    protected T data;

    public ApiResponse(boolean status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    @JsonProperty
    public boolean getStatus() {
        return status;
    }

    @JsonProperty
    public String getMessage() {
        return message;
    }

    @JsonProperty
    public T getData() {
        return data;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }
}