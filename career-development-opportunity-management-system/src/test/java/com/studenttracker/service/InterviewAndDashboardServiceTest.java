package com.studenttracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.studenttracker.enums.ApplicationStatus;
import com.studenttracker.enums.InterviewMode;
import com.studenttracker.enums.InterviewResult;
import com.studenttracker.enums.OpportunityStatus;
import com.studenttracker.enums.Proficiency;
import com.studenttracker.enums.SkillStatus;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.Company;
import com.studenttracker.model.DashboardSummary;
import com.studenttracker.model.Interview;
import com.studenttracker.model.JobApplication;
import com.studenttracker.model.Opportunity;
import com.studenttracker.model.StudentSkill;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InterviewAndDashboardServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 13, 9, 0);

    private InMemoryStudentDao students;
    private InMemorySkillDao skills;
    private InMemoryOpportunityDao opportunities;
    private InMemoryApplicationDao applications;
    private InMemoryInterviewDao interviewDao;
    private ApplicationService applicationService;
    private InterviewService interviews;
    private DashboardService dashboard;
    private JobApplication application;

    @BeforeEach
    void setUp() {
        students = new InMemoryStudentDao();
        skills = new InMemorySkillDao();
        opportunities = new InMemoryOpportunityDao();
        applications = new InMemoryApplicationDao();
        interviewDao = new InMemoryInterviewDao();
        applicationService = new ApplicationService(applications, opportunities, students, TestData.CLOCK);
        interviews = new InterviewService(interviewDao, applications, TestData.CLOCK);
        dashboard = new DashboardService(students, skills, opportunities, applications, interviewDao, TestData.CLOCK);

        TestData.student(students, "8.00");
        Company company = TestData.company(new InMemoryCompanyDao(), "Brightwave");
        Opportunity role = TestData.opportunity(opportunities, company, "Java Intern", TestData.TODAY.plusDays(3));
        application = applicationService.apply(role.getOpportunityId(), TestData.TODAY, null);
    }

    private Interview round(String name, LocalDateTime when) {
        return new Interview(application.getApplicationId(), name, when, InterviewMode.ONLINE);
    }

    @Test
    void addsAnInterviewToAnActiveApplication() {
        Interview saved = interviews.add(round("Technical", NOW.plusDays(1)));

        assertEquals(1L, saved.getInterviewId());
    }

    @Test
    void rejectsInterviewsOnFinishedApplications() {
        applicationService.changeStatus(application, ApplicationStatus.REJECTED);

        assertThrows(ValidationException.class, () -> interviews.add(round("Technical", NOW.plusDays(1))));
    }

    @Test
    void splitsInterviewsIntoUpcomingAndPast() {
        interviews.add(round("Screening", NOW.minusDays(2)));
        interviews.add(round("Technical", NOW.plusDays(1)));
        Interview cancelled = interviews.add(round("HR", NOW.plusDays(2)));
        cancelled.setResult(InterviewResult.CANCELLED);
        interviews.update(cancelled);

        assertEquals(1, interviews.list(InterviewService.Scope.UPCOMING).size());
        assertEquals(2, interviews.list(InterviewService.Scope.PAST).size());
        assertEquals(3, interviews.list(InterviewService.Scope.ALL).size());
    }

    @Test
    void dashboardCountsMatchTheUnderlyingRecords() {
        interviews.add(round("Technical", NOW.plusDays(1)));
        interviews.add(round("Screening", NOW.minusDays(1)));
        skills.insert(skill("Java", SkillStatus.LEARNING));
        skills.insert(skill("Git", SkillStatus.COMPLETED));
        skills.insert(skill("SQL", SkillStatus.COMPLETED));

        DashboardSummary summary = dashboard.summary();

        assertEquals(1, summary.savedOpportunities());
        assertEquals(1, summary.activeApplications());
        assertEquals(1, summary.upcomingInterviewCount());
        assertEquals(0, summary.offers());
        assertEquals(1, summary.skillsInProgress());
        assertEquals(2, summary.completedSkills());
        assertEquals(1, summary.upcomingDeadlineCount());
        assertEquals(1, summary.totalApplications());
    }

    @Test
    void offersAreCountedSeparatelyFromActiveApplications() {
        applicationService.changeStatus(application, ApplicationStatus.OFFER);

        DashboardSummary summary = dashboard.summary();

        assertEquals(0, summary.activeApplications());
        assertEquals(1, summary.offers());
    }

    @Test
    void archivedOpportunitiesAreNotCountedAsSaved() {
        opportunities.updateStatus(1, OpportunityStatus.ARCHIVED);

        assertEquals(0, dashboard.summary().savedOpportunities());
    }

    private StudentSkill skill(String name, SkillStatus status) {
        StudentSkill skill = new StudentSkill(name, Proficiency.INTERMEDIATE, status);
        skill.setStudentId(1L);
        return skill;
    }
}
