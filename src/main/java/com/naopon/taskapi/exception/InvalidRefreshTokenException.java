package com.naopon.taskapi.exception;

// Raised when a refresh token is missing, expired, revoked, or unknown.
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
