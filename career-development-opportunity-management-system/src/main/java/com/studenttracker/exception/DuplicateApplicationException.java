package com.studenttracker.exception;

/** Thrown when the student has already applied to the same opportunity. */
public class DuplicateApplicationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateApplicationException(String message) {
        super(message);
    }

    public DuplicateApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
