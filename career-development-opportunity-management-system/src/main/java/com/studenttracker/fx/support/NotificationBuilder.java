package com.studenttracker.fx.support;

import com.studenttracker.model.DashboardSummary;
import com.studenttracker.model.Interview;
import com.studenttracker.model.Opportunity;
import com.studenttracker.util.Dates;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Turns the dashboard summary into reminder entries: interviews in the next seven days and deadlines
 * closing in the next seven days. Pure logic with no JavaFX types, so it is easy to test.
 */
public final class NotificationBuilder {

    public enum Kind {
        INTERVIEW, DEADLINE
    }

    /** One reminder. The key changes when an interview is rescheduled or a deadline moves, so it reappears. */
    public record Item(String key, Kind kind, String title, String detail, LocalDateTime when) {
    }

    private static final int WINDOW_DAYS = 7;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private NotificationBuilder() {
    }

    public static List<Item> build(DashboardSummary summary, LocalDateTime now) {
        List<Item> items = new ArrayList<>();
        LocalDate today = now.toLocalDate();
        LocalDateTime limit = now.plusDays(WINDOW_DAYS);

        for (Interview interview : summary.upcomingInterviews()) {
            if (interview.getScheduledAt().isAfter(limit)) {
                continue;
            }
            LocalDate day = interview.getScheduledAt().toLocalDate();
            items.add(new Item(
                    "interview:" + interview.getInterviewId() + ":" + interview.getScheduledAt(),
                    Kind.INTERVIEW,
                    interview.getRoundName() + " at " + interview.getCompanyName(),
                    FxFormats.relativeDay(day, today) + ", " + TIME.format(interview.getScheduledAt())
                            + " (" + interview.getMode() + ")",
                    interview.getScheduledAt()));
        }

        for (Opportunity opportunity : summary.upcomingDeadlines()) {
            LocalDate deadline = opportunity.getDeadline();
            String when = deadline.equals(today) ? "Closes today"
                    : deadline.equals(today.plusDays(1)) ? "Closes tomorrow"
                    : "Closes " + Dates.display(deadline);
            items.add(new Item(
                    "deadline:" + opportunity.getOpportunityId() + ":" + deadline,
                    Kind.DEADLINE,
                    opportunity.getRoleTitle() + " at " + opportunity.getCompanyName(),
                    when,
                    deadline.atTime(LocalTime.of(23, 59))));
        }

        items.sort(Comparator.comparing(Item::when));
        return items;
    }
}
