package com.naopon.taskapi.exception;

// Custom exception used when the requested task does not exist.
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
