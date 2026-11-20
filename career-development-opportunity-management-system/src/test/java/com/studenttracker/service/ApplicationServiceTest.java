package com.studenttracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.studenttracker.enums.ApplicationStatus;
import com.studenttracker.enums.OpportunityStatus;
import com.studenttracker.exception.DuplicateApplicationException;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.Company;
import com.studenttracker.model.JobApplication;
import com.studenttracker.model.Opportunity;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApplicationServiceTest {

    private InMemoryStudentDao students;
    private InMemoryOpportunityDao opportunities;
    private InMemoryApplicationDao applications;
    private ApplicationService service;
    private Opportunity openRole;

    @BeforeEach
    void setUp() {
        students = new InMemoryStudentDao();
        opportunities = new InMemoryOpportunityDao();
        applications = new InMemoryApplicationDao();
        service = new ApplicationService(applications, opportunities, students, TestData.CLOCK);

        TestData.student(students, "8.00");
        Company company = TestData.company(new InMemoryCompanyDao(), "Brightwave");
        openRole = TestData.opportunity(opportunities, company, "Java Intern", TestData.TODAY.plusDays(5));
    }

    @Test
    void appliesToAnOpenOpportunity() {
        JobApplication application = service.apply(openRole.getOpportunityId(), TestData.TODAY, "Referred");

        assertEquals(ApplicationStatus.APPLIED, application.getStatus());
        assertEquals("Referred", application.getNotes());
        assertTrue(applications.exists(1L, openRole.getOpportunityId()));
    }

    @Test
    void defaultsTheAppliedDateToToday() {
        JobApplication application = service.apply(openRole.getOpportunityId(), null, null);

        assertEquals(TestData.TODAY, application.getAppliedOn());
    }

    @Test
    void rejectsASecondApplicationToTheSameOpportunity() {
        service.apply(openRole.getOpportunityId(), TestData.TODAY, null);

        assertThrows(DuplicateApplicationException.class,
                () -> service.apply(openRole.getOpportunityId(), TestData.TODAY, null));
    }

    @Test
    void rejectsClosedListings() {
        openRole.setStatus(OpportunityStatus.CLOSED);

        assertThrows(ValidationException.class,
                () -> service.apply(openRole.getOpportunityId(), TestData.TODAY, null));
    }

    @Test
    void rejectsListingsPastTheirDeadline() {
        openRole.setDeadline(TestData.TODAY.minusDays(1));

        assertThrows(ValidationException.class,
                () -> service.apply(openRole.getOpportunityId(), TestData.TODAY, null));
    }

    @Test
    void rejectsFutureAppliedDates() {
        assertThrows(ValidationException.class,
                () -> service.apply(openRole.getOpportunityId(), TestData.TODAY.plusDays(1), null));
    }

    @Test
    void requiresAProfile() {
        ApplicationService withoutProfile = new ApplicationService(
                applications, opportunities, new InMemoryStudentDao(), TestData.CLOCK);

        assertThrows(ValidationException.class,
                () -> withoutProfile.apply(openRole.getOpportunityId(), TestData.TODAY, null));
    }

    @Test
    void allowsForwardStatusTransitions() {
        JobApplication application = service.apply(openRole.getOpportunityId(), TestData.TODAY, null);

        service.changeStatus(application, ApplicationStatus.INTERVIEW);

        assertEquals(ApplicationStatus.INTERVIEW, applications.findById(application.getApplicationId()).get().getStatus());
    }

    @Test
    void rejectsTransitionsOutOfAFinalStatus() {
        JobApplication application = service.apply(openRole.getOpportunityId(), TestData.TODAY, null);
        service.changeStatus(application, ApplicationStatus.REJECTED);

        assertThrows(ValidationException.class, () -> service.changeStatus(application, ApplicationStatus.OFFER));
        assertEquals(ApplicationStatus.REJECTED, application.getStatus());
    }

    @Test
    void rejectsMovingBackwards() {
        JobApplication application = service.apply(openRole.getOpportunityId(), TestData.TODAY, null);
        service.changeStatus(application, ApplicationStatus.INTERVIEW);

        assertThrows(ValidationException.class, () -> service.changeStatus(application, ApplicationStatus.APPLIED));
    }

    @Test
    void correctStatusBypassesTheWorkflow() {
        JobApplication application = service.apply(openRole.getOpportunityId(), TestData.TODAY, null);
        service.changeStatus(application, ApplicationStatus.REJECTED);

        service.correctStatus(application, ApplicationStatus.INTERVIEW);

        assertEquals(ApplicationStatus.INTERVIEW, application.getStatus());
    }

    @Test
    void warnsWhenCgpaIsBelowTheMinimum() {
        openRole.setMinimumCgpa(new BigDecimal("8.50"));

        assertTrue(service.minimumCgpaWarning(openRole.getOpportunityId()).isPresent());
    }

    @Test
    void doesNotWarnWhenCgpaMeetsTheMinimum() {
        openRole.setMinimumCgpa(new BigDecimal("8.00"));

        assertFalse(service.minimumCgpaWarning(openRole.getOpportunityId()).isPresent());
    }

    @Test
    void listsOnlyApplicationsInProgress() {
        JobApplication first = service.apply(openRole.getOpportunityId(), TestData.TODAY, null);
        Company other = TestData.company(new InMemoryCompanyDao(), "Kestrel");
        Opportunity second = TestData.opportunity(opportunities, other, "Analyst", null);
        JobApplication rejected = service.apply(second.getOpportunityId(), TestData.TODAY, null);
        service.changeStatus(rejected, ApplicationStatus.REJECTED);

        assertEquals(1, service.listActive().size());
        assertEquals(first.getApplicationId(), service.listActive().get(0).getApplicationId());
    }
}
