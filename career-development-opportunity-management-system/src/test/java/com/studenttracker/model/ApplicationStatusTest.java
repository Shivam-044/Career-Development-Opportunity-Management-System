package com.studenttracker.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.studenttracker.enums.ApplicationStatus;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;

class ApplicationStatusTest {

    @Test
    void appliedCanMoveToEveryLaterStage() {
        assertEquals(EnumSet.of(ApplicationStatus.ASSESSMENT, ApplicationStatus.INTERVIEW,
                ApplicationStatus.OFFER, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
                ApplicationStatus.APPLIED.nextStatuses());
    }

    @Test
    void interviewCannotGoBackToAssessment() {
        assertFalse(ApplicationStatus.INTERVIEW.canMoveTo(ApplicationStatus.ASSESSMENT));
    }

    @Test
    void offerRejectedAndWithdrawnAreFinal() {
        assertTrue(ApplicationStatus.OFFER.isTerminal());
        assertTrue(ApplicationStatus.REJECTED.isTerminal());
        assertTrue(ApplicationStatus.WITHDRAWN.isTerminal());
        assertFalse(ApplicationStatus.REJECTED.canMoveTo(ApplicationStatus.OFFER));
    }

    @Test
    void onlyInProgressStatusesAreActive() {
        assertTrue(ApplicationStatus.APPLIED.isActive());
        assertTrue(ApplicationStatus.ASSESSMENT.isActive());
        assertTrue(ApplicationStatus.INTERVIEW.isActive());
        assertFalse(ApplicationStatus.OFFER.isActive());
    }

    @Test
    void displayTextIsReadable() {
        assertEquals("Applied", ApplicationStatus.APPLIED.toString());
    }
}
