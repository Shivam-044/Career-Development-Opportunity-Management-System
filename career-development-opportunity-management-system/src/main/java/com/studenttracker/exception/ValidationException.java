package com.studenttracker.exception;

/** Thrown when user input or a business rule check fails. The message is written for the end user. */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
