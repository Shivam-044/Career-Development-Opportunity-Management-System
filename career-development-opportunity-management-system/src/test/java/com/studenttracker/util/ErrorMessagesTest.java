package com.studenttracker.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.studenttracker.exception.DataAccessException;
import com.studenttracker.exception.DuplicateApplicationException;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.util.ErrorMessages.Message;
import com.studenttracker.util.ErrorMessages.Severity;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.junit.jupiter.api.Test;

class ErrorMessagesTest {

    @Test
    void validationProblemsAreWarningsShownAsWritten() {
        Message message = ErrorMessages.describe(new ValidationException("Role title is required."));

        assertEquals("Check your entry", message.title());
        assertEquals("Role title is required.", message.text());
        assertEquals(Severity.WARNING, message.severity());
    }

    @Test
    void duplicateApplicationsHaveTheirOwnTitle() {
        Message message = ErrorMessages.describe(new DuplicateApplicationException("Already applied."));

        assertEquals("Already applied", message.title());
        assertEquals(Severity.WARNING, message.severity());
    }

    @Test
    void databaseProblemsIncludeTheCauseAndWhatToDoNext() {
        Message message = ErrorMessages.describe(
                new DataAccessException("Couldn't load companies.", new RuntimeException("Communications link failure")));

        assertEquals("Database problem", message.title());
        assertEquals(Severity.ERROR, message.severity());
        assertTrue(message.text().contains("Couldn't load companies."));
        assertTrue(message.text().contains("Communications link failure"));
        assertTrue(message.text().contains("MySQL is running"));
    }

    @Test
    void databaseProblemsWithoutACauseStillExplainThemselves() {
        Message message = ErrorMessages.describe(new DataAccessException("Database settings are missing."));

        assertTrue(message.text().startsWith("Database settings are missing."));
        assertTrue(!message.text().contains("Details:"));
    }

    @Test
    void fileProblemsMentionTheUsualCause() {
        Message message = ErrorMessages.describe(new UncheckedIOException(new IOException("denied")));

        assertEquals("Couldn't write the file", message.title());
        assertTrue(message.text().contains("Excel"));
    }

    @Test
    void anythingElseIsReportedAsUnexpected() {
        Message message = ErrorMessages.describe(new IllegalStateException("boom"));

        assertEquals("Something went wrong", message.title());
        assertTrue(message.text().contains("boom"));
    }

    @Test
    void aMissingExceptionDoesNotCrashTheHandler() {
        assertEquals("Something went wrong", ErrorMessages.describe(null).title());
    }
}
