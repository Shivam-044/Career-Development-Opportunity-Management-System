package com.studenttracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.studenttracker.enums.OpportunityStatus;
import com.studenttracker.enums.OpportunityType;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.Company;
import com.studenttracker.model.Opportunity;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OpportunityServiceTest {

    private InMemoryCompanyDao companies;
    private InMemoryOpportunityDao opportunities;
    private OpportunityService service;
    private Company company;

    @BeforeEach
    void setUp() {
        companies = new InMemoryCompanyDao();
        opportunities = new InMemoryOpportunityDao();
        service = new OpportunityService(opportunities, companies, TestData.CLOCK);
        company = TestData.company(companies, "Brightwave");
    }

    private Opportunity draft(String role) {
        return new Opportunity(company.getCompanyId(), role, OpportunityType.INTERNSHIP);
    }

    @Test
    void savesANewOpportunity() {
        Opportunity saved = service.save(draft("Java Intern"), false);

        assertEquals(1L, saved.getOpportunityId());
    }

    @Test
    void rejectsAnOpenListingWithAPastDeadline() {
        Opportunity opportunity = draft("Java Intern");
        opportunity.setDeadline(TestData.TODAY.minusDays(2));

        assertThrows(ValidationException.class, () -> service.save(opportunity, false));
    }

    @Test
    void keepsAPastDeadlineListingOpenWhenTheUserInsists() {
        Opportunity opportunity = draft("Java Intern");
        opportunity.setDeadline(TestData.TODAY.minusDays(2));

        service.save(opportunity, true);

        assertEquals(OpportunityStatus.OPEN, opportunities.findById(1).get().getStatus());
    }

    @Test
    void acceptsAPastDeadlineWhenTheListingIsClosed() {
        Opportunity opportunity = draft("Java Intern");
        opportunity.setDeadline(TestData.TODAY.minusDays(2));
        opportunity.setStatus(OpportunityStatus.CLOSED);

        service.save(opportunity, false);

        assertFalse(service.hasPassedDeadline(opportunity));
    }

    @Test
    void aDeadlineTodayIsStillOpen() {
        Opportunity opportunity = draft("Java Intern");
        opportunity.setDeadline(TestData.TODAY);

        assertFalse(service.hasPassedDeadline(opportunity));
    }

    @Test
    void rejectsNewOpportunitiesForAnArchivedCompany() {
        companies.setArchived(company.getCompanyId(), true);

        assertThrows(ValidationException.class, () -> service.save(draft("Java Intern"), false));
    }

    @Test
    void rejectsAnUnknownCompany() {
        Opportunity orphan = new Opportunity(999, "Java Intern", OpportunityType.FULL_TIME);

        assertThrows(ValidationException.class, () -> service.save(orphan, false));
    }

    @Test
    void cannotReopenAListingWhoseDeadlineHasPassed() {
        Opportunity opportunity = draft("Java Intern");
        opportunity.setDeadline(TestData.TODAY.minusDays(1));
        opportunity.setStatus(OpportunityStatus.CLOSED);
        service.save(opportunity, false);

        assertThrows(ValidationException.class, () -> service.changeStatus(opportunity, OpportunityStatus.OPEN));
    }

    @Test
    void archivesAListing() {
        Opportunity saved = service.save(draft("Java Intern"), false);

        service.changeStatus(saved, OpportunityStatus.ARCHIVED);

        assertEquals(OpportunityStatus.ARCHIVED, opportunities.findById(saved.getOpportunityId()).get().getStatus());
    }

    @Test
    void normalisesRequiredSkillsWithoutDuplicates() {
        Opportunity opportunity = draft("Java Intern");

        opportunity.setRequiredSkills(List.of(" Java ", "java", "SQL", "", "  "));

        assertEquals(List.of("Java", "SQL"), opportunity.getRequiredSkills());
        assertFalse(opportunity.isDeadlinePassed(TestData.TODAY));
    }
}
