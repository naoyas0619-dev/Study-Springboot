package com.naopon.taskapi.common;

// Common JSON shape used when returning errors to the client.
public class ErrorResponse {

    private String message;

    // Stores the message that will be returned in the HTTP response body.
    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
