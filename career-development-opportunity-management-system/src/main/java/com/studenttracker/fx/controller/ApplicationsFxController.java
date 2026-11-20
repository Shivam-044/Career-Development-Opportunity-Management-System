package com.studenttracker.fx.controller;

import com.studenttracker.enums.ApplicationStatus;
import com.studenttracker.fx.components.StatusBadge;
import com.studenttracker.fx.controller.dialog.InterviewDialogController;
import com.studenttracker.fx.controller.dialog.NotesDialogController;
import com.studenttracker.fx.controller.dialog.StatusDialogController;
import com.studenttracker.fx.navigation.Searchable;
import com.studenttracker.fx.support.Combos;
import com.studenttracker.fx.support.FxContext;
import com.studenttracker.fx.support.Tables;
import com.studenttracker.model.Interview;
import com.studenttracker.model.JobApplication;
import com.studenttracker.service.InterviewService;
import com.studenttracker.util.Dates;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class ApplicationsFxController extends BaseFxController implements Searchable {

    private static final String EMPTY = "No applications yet. Mark an opportunity as applied and it will show up here.";

    @FXML
    private ComboBox<ApplicationStatus> statusFilter;
    @FXML
    private Button notesButton;
    @FXML
    private Button interviewButton;
    @FXML
    private Button correctButton;
    @FXML
    private Button statusButton;
    @FXML
    private TableView<JobApplication> table;
    @FXML
    private Label detailEmpty;
    @FXML
    private VBox detailContent;
    @FXML
    private Label detailRole;
    @FXML
    private Label detailCompany;
    @FXML
    private HBox detailStatusHolder;
    @FXML
    private Label detailApplied;
    @FXML
    private Label detailNotes;
    @FXML
    private VBox detailInterviews;

    private String keyword = "";
    private Long detailApplicationId;

    public ApplicationsFxController(FxContext ctx) {
        super(ctx);
    }

    @FXML
    private void initialize() {
        Tables.configure(table, EMPTY);
        table.getColumns().add(Tables.text("Company", 160, JobApplication::getCompanyName));
        table.getColumns().add(Tables.text("Role", 200, JobApplication::getRoleTitle));
        table.getColumns().add(Tables.column("Applied on", 105, JobApplication::getAppliedOn, Dates::display));
        table.getColumns().add(Tables.nodes("Status", 120,
                application -> StatusBadge.forApplication(application.getStatus()),
                Comparator.comparing(JobApplication::getStatus)));
        table.getColumns().add(Tables.text("Notes", 240, JobApplication::getNotes));

        Combos.fillOptional(statusFilter, ApplicationStatus.values(), "All statuses");
        statusFilter.valueProperty().addListener((observable, previous, current) -> reload());
        table.getSelectionModel().selectedItemProperty().addListener((observable, previous, selected) -> {
            updateButtons();
            showDetails(selected);
        });
        updateButtons();
        showDetails(null);
    }

    @Override
    public String searchPrompt() {
        return "Search company or role";
    }

    @Override
    public void applySearch(String text) {
        keyword = text == null ? "" : text;
        reload();
    }

    @Override
    public void onShow() {
        reload();
    }

    private void reload() {
        String search = keyword;
        ApplicationStatus status = statusFilter.getValue();
        load(() -> services.applications().search(search, status), applications -> {
            table.setItems(FXCollections.observableArrayList(applications));
            Tables.setEmptyText(table, search.isEmpty() && status == null ? EMPTY : "No applications match.");
            updateButtons();
            showDetails(null);
        });
    }

    private void updateButtons() {
        boolean selected = table.getSelectionModel().getSelectedItem() != null;
        notesButton.setDisable(!selected);
        interviewButton.setDisable(!selected);
        correctButton.setDisable(!selected);
        statusButton.setDisable(!selected);
    }

    // ---------------------------------------------------------------- details

    private void showDetails(JobApplication application) {
        boolean present = application != null;
        detailEmpty.setVisible(!present);
        detailEmpty.setManaged(!present);
        detailContent.setVisible(present);
        detailContent.setManaged(present);
        detailApplicationId = present ? application.getApplicationId() : null;
        if (!present) {
            return;
        }
        detailRole.setText(application.getRoleTitle());
        detailCompany.setText(application.getCompanyName());
        detailStatusHolder.getChildren().setAll(StatusBadge.forApplication(application.getStatus()));
        detailApplied.setText(Dates.display(application.getAppliedOn()));
        detailNotes.setText(application.getNotes() == null ? "No notes yet." : application.getNotes());
        detailInterviews.getChildren().setAll(muted("Loading..."));

        long id = application.getApplicationId();
        ctx.tasks().runHandling(
                () -> services.interviews().list(InterviewService.Scope.ALL).stream()
                        .filter(interview -> interview.getApplicationId() == id)
                        .sorted(Comparator.comparing(Interview::getScheduledAt))
                        .toList(),
                interviews -> {
                    if (detailApplicationId != null && detailApplicationId == id) {
                        showInterviews(interviews);
                    }
                },
                error -> detailInterviews.getChildren().setAll(muted("Interviews couldn't be loaded.")));
    }

    private void showInterviews(List<Interview> interviews) {
        detailInterviews.getChildren().clear();
        if (interviews.isEmpty()) {
            detailInterviews.getChildren().add(muted("No interview rounds yet."));
            return;
        }
        for (Interview interview : interviews) {
            Label name = new Label(interview.getRoundName());
            name.getStyleClass().add("row-title");
            Label when = new Label(Dates.display(interview.getScheduledAt()) + "  |  " + interview.getMode());
            when.getStyleClass().add("muted");
            VBox text = new VBox(2, name, when);
            HBox.setHgrow(text, Priority.ALWAYS);
            HBox row = new HBox(10, text, StatusBadge.forInterviewResult(interview.getResult()));
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("list-row");
            detailInterviews.getChildren().add(row);
        }
    }

    private static Label muted(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted");
        return label;
    }

    // ---------------------------------------------------------------- actions

    @FXML
    private void updateStatus() {
        JobApplication application = table.getSelectionModel().getSelectedItem();
        if (application == null) {
            return;
        }
        Set<ApplicationStatus> next = application.getStatus().nextStatuses();
        if (next.isEmpty()) {
            ctx.dialogs().info("Status is final", "An application marked " + application.getStatus()
                    + " can't move on. If it was set by mistake, use Correct status.");
            return;
        }
        ctx.dialogs().<ApplicationStatus, StatusDialogController>form("Update status", "Update status",
                "status-dialog.fxml", dialog -> dialog.configure(
                        application.describe() + " (currently " + application.getStatus() + ")",
                        "Only the next steps in the workflow are listed.", next))
                .ifPresent(target -> perform(() -> services.applications().changeStatus(application, target), () -> {
                    ctx.toasts().success("Moved to " + target + ".");
                    reload();
                }, statusButton));
    }

    @FXML
    private void correctStatus() {
        JobApplication application = table.getSelectionModel().getSelectedItem();
        if (application == null) {
            return;
        }
        Set<ApplicationStatus> others = EnumSet.complementOf(EnumSet.of(application.getStatus()));
        ctx.dialogs().<ApplicationStatus, StatusDialogController>form("Correct status", "Correct status",
                "status-dialog.fxml", dialog -> dialog.configure(
                        application.describe() + " (currently " + application.getStatus() + ")",
                        "Use this only to fix a mistake. It skips the normal workflow rules.", others))
                .ifPresent(target -> perform(() -> services.applications().correctStatus(application, target), () -> {
                    ctx.toasts().success("Status corrected to " + target + ".");
                    reload();
                }, correctButton));
    }

    @FXML
    private void editNotes() {
        JobApplication application = table.getSelectionModel().getSelectedItem();
        if (application == null) {
            return;
        }
        ctx.dialogs().<String, NotesDialogController>form("Application notes", "Save notes", "notes-dialog.fxml",
                dialog -> dialog.configure(application.describe(), application.getNotes()))
                .ifPresent(notes -> perform(() -> services.applications().updateNotes(application, notes), () -> {
                    ctx.toasts().success("Notes saved.");
                    reload();
                }, notesButton));
    }

    @FXML
    private void addInterview() {
        JobApplication application = table.getSelectionModel().getSelectedItem();
        if (application == null) {
            return;
        }
        ctx.dialogs().<Interview, InterviewDialogController>form("Add interview", "Add interview",
                "interview-dialog.fxml", dialog -> dialog.configureNew(List.of(application), application))
                .ifPresent(interview -> perform(() -> services.interviews().add(interview), () -> {
                    ctx.toasts().success("Interview added.");
                    reload();
                }, interviewButton));
    }

}
