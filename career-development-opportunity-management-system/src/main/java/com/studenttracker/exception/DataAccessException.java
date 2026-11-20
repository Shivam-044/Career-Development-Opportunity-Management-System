package com.studenttracker.exception;

/** Wraps failures from the persistence layer so callers never deal with {@link java.sql.SQLException}. */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
