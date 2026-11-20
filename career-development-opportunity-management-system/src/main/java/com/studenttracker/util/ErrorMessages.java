package com.studenttracker.util;

import com.studenttracker.exception.DataAccessException;
import com.studenttracker.exception.DuplicateApplicationException;
import com.studenttracker.exception.ValidationException;
import java.io.UncheckedIOException;

/** Turns exceptions into text a student can act on. Used by the JavaFX interface. */
public final class ErrorMessages {

    public enum Severity {
        WARNING, ERROR
    }

    public record Message(String title, String text, Severity severity) {
    }

    private static final String DATABASE_HINT =
            "Check that MySQL is running and that the connection settings are correct, then try again.";

    private ErrorMessages() {
    }

    public static Message describe(Throwable failure) {
        if (failure instanceof ValidationException) {
            return new Message("Check your entry", failure.getMessage(), Severity.WARNING);
        }
        if (failure instanceof DuplicateApplicationException) {
            return new Message("Already applied", failure.getMessage(), Severity.WARNING);
        }
        if (failure instanceof DataAccessException) {
            StringBuilder text = new StringBuilder(failure.getMessage());
            Throwable cause = failure.getCause();
            if (cause != null && cause.getMessage() != null) {
                text.append("\n\nDetails: ").append(cause.getMessage());
            }
            text.append("\n\n").append(DATABASE_HINT);
            return new Message("Database problem", text.toString(), Severity.ERROR);
        }
        if (failure instanceof UncheckedIOException) {
            return new Message("Couldn't write the file",
                    "The file couldn't be saved. Check that the folder exists, that you can write to it, "
                            + "and that the file isn't open in another program such as Excel.",
                    Severity.ERROR);
        }
        String reason = failure == null || failure.getMessage() == null ? "" : " (" + failure.getMessage() + ")";
        return new Message("Something went wrong",
                "The app ran into an unexpected problem" + reason
                        + ". Try again, and restart the app if it keeps happening.",
                Severity.ERROR);
    }
}
