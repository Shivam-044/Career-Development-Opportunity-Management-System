package com.studenttracker.fx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.studenttracker.enums.ApplicationStatus;
import com.studenttracker.enums.InterviewMode;
import com.studenttracker.enums.OpportunityType;
import com.studenttracker.fx.support.NotificationBuilder;
import com.studenttracker.fx.support.NotificationBuilder.Item;
import com.studenttracker.fx.support.NotificationBuilder.Kind;
import com.studenttracker.model.DashboardSummary;
import com.studenttracker.model.Interview;
import com.studenttracker.model.Opportunity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import org.junit.jupiter.api.Test;

class NotificationBuilderTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 16, 9, 0);

    private static Interview interview(long id, String round, LocalDateTime when) {
        Interview interview = new Interview(1, round, when, InterviewMode.ONLINE);
        interview.setInterviewId(id);
        interview.setDisplayNames("Brightwave", "Java Intern");
        return interview;
    }

    private static Opportunity opportunity(long id, String role, LocalDate deadline) {
        Opportunity opportunity = new Opportunity(1, role, OpportunityType.INTERNSHIP);
        opportunity.setOpportunityId(id);
        opportunity.setCompanyName("Kestrel");
        opportunity.setDeadline(deadline);
        return opportunity;
    }

    private static DashboardSummary summary(List<Interview> interviews, List<Opportunity> deadlines) {
        return new DashboardSummary(0, 0, interviews.size(), 0, 0, 0, deadlines.size(),
                new EnumMap<>(ApplicationStatus.class), interviews, deadlines);
    }

    @Test
    void listsInterviewsAndDeadlinesInChronologicalOrder() {
        List<Item> items = NotificationBuilder.build(summary(
                List.of(interview(1, "Technical", NOW.plusDays(2).withHour(15))),
                List.of(opportunity(7, "Data Analyst", NOW.toLocalDate().plusDays(1)))), NOW);

        assertEquals(2, items.size());
        assertEquals(Kind.DEADLINE, items.get(0).kind());
        assertEquals(Kind.INTERVIEW, items.get(1).kind());
    }

    @Test
    void ignoresInterviewsMoreThanAWeekAway() {
        List<Item> items = NotificationBuilder.build(summary(
                List.of(interview(1, "Far away", NOW.plusDays(8))), List.of()), NOW);

        assertTrue(items.isEmpty());
    }

    @Test
    void describesWhenAnInterviewHappens() {
        List<Item> items = NotificationBuilder.build(summary(
                List.of(interview(1, "Technical", NOW.plusDays(1).withHour(15).withMinute(30))), List.of()), NOW);

        assertEquals("Technical at Brightwave", items.get(0).title());
        assertEquals("Tomorrow, 15:30 (Online)", items.get(0).detail());
    }

    @Test
    void describesDeadlinesClosingTodayAndTomorrow() {
        List<Item> items = NotificationBuilder.build(summary(List.of(), List.of(
                opportunity(1, "Today role", NOW.toLocalDate()),
                opportunity(2, "Tomorrow role", NOW.toLocalDate().plusDays(1)),
                opportunity(3, "Later role", NOW.toLocalDate().plusDays(4)))), NOW);

        assertEquals("Closes today", items.get(0).detail());
        assertEquals("Closes tomorrow", items.get(1).detail());
        assertEquals("Closes 20 Nov 2026", items.get(2).detail());
    }

    @Test
    void reschedulingAnInterviewProducesANewKey() {
        String before = NotificationBuilder.build(summary(
                List.of(interview(5, "Technical", NOW.plusDays(1))), List.of()), NOW).get(0).key();
        String after = NotificationBuilder.build(summary(
                List.of(interview(5, "Technical", NOW.plusDays(2))), List.of()), NOW).get(0).key();

        assertFalse(before.equals(after));
    }

    @Test
    void anEmptySummaryHasNoReminders() {
        assertTrue(NotificationBuilder.build(summary(List.of(), List.of()), NOW).isEmpty());
    }
}
