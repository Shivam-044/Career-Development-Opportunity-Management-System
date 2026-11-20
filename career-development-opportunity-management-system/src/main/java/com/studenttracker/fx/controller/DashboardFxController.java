package com.studenttracker.fx.controller;

import com.studenttracker.enums.ApplicationStatus;
import com.studenttracker.fx.components.StatCard;
import com.studenttracker.fx.components.StatusBadge;
import com.studenttracker.fx.support.FxAnimations;
import com.studenttracker.fx.support.FxContext;
import com.studenttracker.fx.support.FxFormats;
import com.studenttracker.model.DashboardSummary;
import com.studenttracker.model.Interview;
import com.studenttracker.model.Opportunity;
import com.studenttracker.model.Student;
import com.studenttracker.util.Dates;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class DashboardFxController extends BaseFxController {

    private record Snapshot(DashboardSummary summary, String studentName) {
    }

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd", Locale.ENGLISH);
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

    @FXML
    private Label greetingLabel;
    @FXML
    private Label greetingSubtitle;
    @FXML
    private Button refreshButton;
    @FXML
    private GridPane cardGrid;
    @FXML
    private StatCard savedCard;
    @FXML
    private StatCard activeCard;
    @FXML
    private StatCard interviewsCard;
    @FXML
    private StatCard offersCard;
    @FXML
    private StatCard learningCard;
    @FXML
    private StatCard completedCard;
    @FXML
    private StatCard deadlinesCard;
    @FXML
    private StatCard totalCard;
    @FXML
    private VBox statusCard;
    @FXML
    private VBox skillCard;
    @FXML
    private VBox interviewCard;
    @FXML
    private VBox deadlineCard;
    @FXML
    private PieChart statusChart;
    @FXML
    private Label statusEmpty;
    @FXML
    private Label skillPercent;
    @FXML
    private ProgressBar skillProgress;
    @FXML
    private Label skillSummary;
    @FXML
    private VBox interviewList;
    @FXML
    private Label interviewEmpty;
    @FXML
    private VBox deadlineList;
    @FXML
    private Label deadlineEmpty;

    private boolean entrancePlayed;

    public DashboardFxController(FxContext ctx) {
        super(ctx);
    }

    @FXML
    private void initialize() {
        greetingLabel.setText(greeting(null));
        greetingSubtitle.setText("Welcome to CareerForge. Here is where your preparation stands today.");
        statusChart.setStartAngle(90);
        statusChart.setClockwise(true);
        statusChart.setLegendVisible(true);
    }

    @Override
    public void onShow() {
        refresh();
    }

    @FXML
    private void refresh() {
        load(() -> new Snapshot(
                        services.dashboard().summary(),
                        services.students().findProfile().map(Student::getFullName).orElse(null)),
                this::render, refreshButton);
    }

    private void render(Snapshot snapshot) {
        DashboardSummary summary = snapshot.summary();
        greetingLabel.setText(greeting(snapshot.studentName()));

        savedCard.setValue(summary.savedOpportunities());
        activeCard.setValue(summary.activeApplications());
        interviewsCard.setValue(summary.upcomingInterviewCount());
        offersCard.setValue(summary.offers());
        learningCard.setValue(summary.skillsInProgress());
        completedCard.setValue(summary.completedSkills());
        deadlinesCard.setValue(summary.upcomingDeadlineCount());
        totalCard.setValue(summary.totalApplications());

        showStatusChart(summary);
        showSkillProgress(summary);
        showInterviews(summary.upcomingInterviews());
        showDeadlines(summary.upcomingDeadlines());

        if (!entrancePlayed) {
            entrancePlayed = true;
            FxAnimations.staggerIn(List.<Node>of(cardGrid, statusCard, skillCard, interviewCard, deadlineCard));
        }
    }

    private static String greeting(String studentName) {
        int hour = LocalDateTime.now().getHour();
        String part = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";
        String first = FxFormats.firstName(studentName);
        return first.isEmpty() ? part : part + ", " + first;
    }

    private void showStatusChart(DashboardSummary summary) {
        boolean empty = summary.totalApplications() == 0;
        statusChart.setVisible(!empty);
        statusEmpty.setVisible(empty);
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            int count = summary.applicationsByStatus().getOrDefault(status, 0);
            data.add(new PieChart.Data(status + "  " + count, count));
        }
        statusChart.setData(data);
    }

    private void showSkillProgress(DashboardSummary summary) {
        int completed = summary.completedSkills();
        int learning = summary.skillsInProgress();
        int total = completed + learning;
        double ratio = total == 0 ? 0 : (double) completed / total;
        skillPercent.setText(Math.round(ratio * 100) + "%");
        FxAnimations.animateProgress(skillProgress, ratio);
        skillSummary.setText(total == 0
                ? "No skills tracked yet. Add some on the Skills page."
                : completed + " completed and " + learning + " in progress.");
    }

    private void showInterviews(List<Interview> interviews) {
        interviewList.getChildren().clear();
        LocalDate today = LocalDate.now();
        for (Interview interview : interviews) {
            LocalDateTime when = interview.getScheduledAt();
            interviewList.getChildren().add(row(
                    when.toLocalDate(),
                    interview.getRoundName(),
                    interview.getRoleTitle() + " at " + interview.getCompanyName(),
                    new StatusBadge(FxFormats.relativeDay(when.toLocalDate(), today) + ", " + TIME.format(when), "accent")));
        }
        interviewEmpty.setVisible(interviews.isEmpty());
        interviewEmpty.setManaged(interviews.isEmpty());
    }

    private void showDeadlines(List<Opportunity> deadlines) {
        deadlineList.getChildren().clear();
        LocalDate today = LocalDate.now();
        for (Opportunity opportunity : deadlines) {
            LocalDate deadline = opportunity.getDeadline();
            boolean imminent = !deadline.isAfter(today.plusDays(2));
            deadlineList.getChildren().add(row(
                    deadline,
                    opportunity.getRoleTitle(),
                    opportunity.getCompanyName() + " - closes " + Dates.display(deadline),
                    new StatusBadge(FxFormats.relativeDay(deadline, today), imminent ? "danger" : "warning")));
        }
        deadlineEmpty.setVisible(deadlines.isEmpty());
        deadlineEmpty.setManaged(deadlines.isEmpty());
    }

    private static HBox row(LocalDate date, String title, String subtitle, Node trailing) {
        Label day = new Label(DAY.format(date));
        day.getStyleClass().add("date-chip-day");
        Label month = new Label(MONTH.format(date).toUpperCase(Locale.ROOT));
        month.getStyleClass().add("date-chip-month");
        VBox chip = new VBox(0, day, month);
        chip.setAlignment(Pos.CENTER);
        chip.getStyleClass().add("date-chip");

        Label heading = new Label(title);
        heading.getStyleClass().add("row-title");
        Label detail = new Label(subtitle);
        detail.getStyleClass().add("muted");
        VBox text = new VBox(2, heading, detail);
        HBox.setHgrow(text, Priority.ALWAYS);

        HBox row = new HBox(14, chip, text, trailing);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("list-row");
        return row;
    }
}
