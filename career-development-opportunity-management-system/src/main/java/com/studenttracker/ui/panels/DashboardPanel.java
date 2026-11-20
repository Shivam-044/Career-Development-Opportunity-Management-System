package com.studenttracker.ui.panels;

import com.studenttracker.config.Services;
import com.studenttracker.model.DashboardSummary;
import com.studenttracker.model.Interview;
import com.studenttracker.model.Opportunity;
import com.studenttracker.service.DashboardService;
import com.studenttracker.ui.support.DataTable;
import com.studenttracker.ui.support.ListTableModel.Column;
import com.studenttracker.ui.support.StatCard;
import com.studenttracker.ui.support.Theme;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;

public final class DashboardPanel extends FeaturePanel {

    private static final long serialVersionUID = 1L;

    private final transient DashboardService service;

    private final StatCard saved = new StatCard("Saved opportunities");
    private final StatCard active = new StatCard("Active applications");
    private final StatCard interviewCount = new StatCard("Upcoming interviews");
    private final StatCard offers = new StatCard("Offers");
    private final StatCard learning = new StatCard("Skills in progress");
    private final StatCard completed = new StatCard("Completed skills");
    private final StatCard deadlines = new StatCard("Deadlines in 7 days");

    private final DataTable<Interview> interviews = new DataTable<>(List.of(
            new Column<Interview>("Company", String.class, Interview::getCompanyName, 120),
            new Column<Interview>("Round", String.class, Interview::getRoundName, 120),
            new Column<Interview>("When", LocalDateTime.class, Interview::getScheduledAt, 130)),
            "No interviews scheduled.");
    private final DataTable<Opportunity> closing = new DataTable<>(List.of(
            new Column<Opportunity>("Company", String.class, Opportunity::getCompanyName, 120),
            new Column<Opportunity>("Role", String.class, Opportunity::getRoleTitle, 140),
            new Column<Opportunity>("Closes", String.class, opportunity -> closesIn(opportunity.getDeadline()), 90)),
            "No deadlines in the next 7 days.");

    public DashboardPanel(Services services) {
        super("Dashboard", "Where your placement preparation stands today.");
        this.service = services.dashboard();

        JPanel firstRow = new JPanel(new GridLayout(1, 4, 12, 0));
        firstRow.setOpaque(false);
        firstRow.add(saved);
        firstRow.add(active);
        firstRow.add(interviewCount);
        firstRow.add(offers);

        JPanel secondRow = new JPanel(new GridLayout(1, 4, 12, 0));
        secondRow.setOpaque(false);
        secondRow.add(learning);
        secondRow.add(completed);
        secondRow.add(deadlines);
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        secondRow.add(filler);

        JPanel cards = new JPanel(new GridLayout(2, 1, 0, 12));
        cards.setOpaque(false);
        cards.add(firstRow);
        cards.add(secondRow);

        JPanel lists = new JPanel(new GridLayout(1, 2, 16, 0));
        lists.setOpaque(false);
        lists.add(section("Upcoming interviews", interviews));
        lists.add(section("Closing soon", closing));

        JButton refresh = Theme.secondaryButton("Refresh");
        refresh.addActionListener(event -> onShow());
        JPanel refreshRow = new JPanel(new BorderLayout());
        refreshRow.setOpaque(false);
        refreshRow.add(refresh, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(refreshRow, BorderLayout.NORTH);
        top.add(cards, BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setOpaque(false);
        body.add(top, BorderLayout.NORTH);
        body.add(lists, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    private static JPanel section(String heading, DataTable<?> table) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.add(Theme.sectionHeading(heading), BorderLayout.NORTH);
        panel.add(table, BorderLayout.CENTER);
        return panel;
    }

    private static String closesIn(LocalDate deadline) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
        if (days <= 0) {
            return "Today";
        }
        return days == 1 ? "Tomorrow" : "In " + days + " days";
    }

    @Override
    public void onShow() {
        load(service::summary, this::show);
    }

    private void show(DashboardSummary summary) {
        saved.setValue(summary.savedOpportunities());
        active.setValue(summary.activeApplications());
        interviewCount.setValue(summary.upcomingInterviewCount());
        offers.setValue(summary.offers());
        learning.setValue(summary.skillsInProgress());
        completed.setValue(summary.completedSkills());
        deadlines.setValue(summary.upcomingDeadlineCount());
        interviews.setRows(summary.upcomingInterviews());
        closing.setRows(summary.upcomingDeadlines());
    }
}
