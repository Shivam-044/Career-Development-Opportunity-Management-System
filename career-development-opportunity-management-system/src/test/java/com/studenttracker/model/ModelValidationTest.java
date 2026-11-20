package com.studenttracker.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.studenttracker.enums.ApplicationStatus;
import com.studenttracker.enums.InterviewMode;
import com.studenttracker.enums.OpportunityType;
import com.studenttracker.exception.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ModelValidationTest {

    @Test
    void studentRejectsACgpaAboveTen() {
        Student student = new Student("Asha Verma");

        assertThrows(ValidationException.class, () -> student.setCgpa(new BigDecimal("11.2")));
    }

    @Test
    void studentRejectsANegativeCgpa() {
        Student student = new Student("Asha Verma");

        assertThrows(ValidationException.class, () -> student.setCgpa(new BigDecimal("-0.1")));
    }

    @Test
    void studentKeepsCgpaToTwoDecimals() {
        Student student = new Student("Asha Verma");

        student.setCgpa(new BigDecimal("8.256"));

        assertEquals(new BigDecimal("8.26"), student.getCgpa());
    }

    @Test
    void studentRequiresAName() {
        assertThrows(ValidationException.class, () -> new Student("   "));
    }

    @Test
    void studentRejectsAnImplausibleGraduationYear() {
        Student student = new Student("Asha Verma");

        assertThrows(ValidationException.class, () -> student.setGraduationYear(1850));
    }

    @Test
    void studentRejectsAMalformedEmail() {
        Student student = new Student("Asha Verma");

        assertThrows(ValidationException.class, () -> student.setEmail("asha.example.com"));
    }

    @Test
    void blankOptionalFieldsBecomeNull() {
        Student student = new Student("Asha Verma");

        student.setEmail("  ");

        assertNull(student.getEmail());
    }

    @Test
    void opportunityRequiresARoleTitle() {
        assertThrows(ValidationException.class, () -> new Opportunity(1, "", OpportunityType.INTERNSHIP));
    }

    @Test
    void opportunityAcceptsOnlyWebAddresses() {
        Opportunity opportunity = new Opportunity(1, "Intern", OpportunityType.INTERNSHIP);

        opportunity.setSourceUrl("https://example.com/jobs/1");
        assertThrows(ValidationException.class, () -> opportunity.setSourceUrl("ftp://example.com/jobs"));
        assertThrows(ValidationException.class, () -> opportunity.setSourceUrl("not a url"));
    }

    @Test
    void interviewRequiresADateAndTime() {
        assertThrows(ValidationException.class, () -> new Interview(1, "Technical", null, InterviewMode.ONLINE));
    }

    @Test
    void applicationRequiresADate() {
        assertThrows(ValidationException.class, () -> new JobApplication(1, 1, null));
    }

    @Test
    void newApplicationsStartAsApplied() {
        JobApplication application = new JobApplication(1, 1, LocalDate.of(2026, 11, 1));

        assertEquals(ApplicationStatus.APPLIED, application.getStatus());
        assertEquals(LocalDate.of(2026, 11, 1), application.getAppliedOn());
    }
}
