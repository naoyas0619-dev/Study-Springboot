package com.naopon.taskapi.exception;

// Raised when authentication endpoints are being called too frequently.
public class TooManyRequestsException extends RuntimeException {

    public TooManyRequestsException(String message) {
        super(message);
    }
}
