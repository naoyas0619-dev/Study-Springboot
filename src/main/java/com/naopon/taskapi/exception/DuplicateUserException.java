package com.naopon.taskapi.exception;

// Raised when attempting to create a user with an existing username.
public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException(String message) {
        super(message);
    }
}
