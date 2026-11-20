package com.studenttracker.fx.controller;

import com.studenttracker.enums.InterviewResult;
import com.studenttracker.fx.components.StatusBadge;
import com.studenttracker.fx.controller.dialog.InterviewDialogController;
import com.studenttracker.fx.support.FxContext;
import com.studenttracker.fx.support.Tables;
import com.studenttracker.fx.support.Toggles;
import com.studenttracker.model.Interview;
import com.studenttracker.service.InterviewService.Scope;
import com.studenttracker.util.Dates;
import java.time.LocalDateTime;
import java.util.Comparator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;

public final class InterviewsFxController extends BaseFxController {

    @FXML
    private ToggleGroup scopeGroup;
    @FXML
    private Button deleteButton;
    @FXML
    private Button editButton;
    @FXML
    private Button addButton;
    @FXML
    private TableView<Interview> table;

    private Scope scope = Scope.UPCOMING;

    public InterviewsFxController(FxContext ctx) {
        super(ctx);
    }

    @FXML
    private void initialize() {
        Tables.configure(table, "");
        table.getColumns().add(Tables.column("Date and time", 150, Interview::getScheduledAt, Dates::display));
        table.getColumns().add(Tables.text("Company", 150, Interview::getCompanyName));
        table.getColumns().add(Tables.text("Position", 180, Interview::getRoleTitle));
        table.getColumns().add(Tables.text("Round", 150, Interview::getRoundName));
        table.getColumns().add(Tables.text("Mode", 80, interview -> interview.getMode().toString()));
        table.getColumns().add(Tables.nodes("Result", 110, interview -> StatusBadge.forInterviewResult(interview.getResult()),
                Comparator.comparing(Interview::getResult)));
        table.getColumns().add(Tables.text("Feedback", 260, Interview::getFeedback));
        Tables.styleRows(table, interview -> isUpcoming(interview) ? "row-upcoming" : null);

        Toggles.segmented(scopeGroup, key -> {
            scope = Scope.valueOf(key);
            onShow();
        });
        table.getSelectionModel().selectedItemProperty().addListener((observable, previous, selected) -> updateButtons());
        table.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                editInterview();
            }
        });
        updateButtons();
    }

    private static boolean isUpcoming(Interview interview) {
        return interview.getResult() != InterviewResult.CANCELLED
                && !interview.getScheduledAt().isBefore(LocalDateTime.now());
    }

    @Override
    public void onShow() {
        Scope selected = scope;
        load(() -> services.interviews().list(selected), interviews -> {
            table.setItems(FXCollections.observableArrayList(interviews));
            Tables.setEmptyText(table, switch (selected) {
                case UPCOMING -> "No upcoming interviews. Add one here or from the Applications page.";
                case PAST -> "No past interviews yet.";
                case ALL -> "No interviews yet.";
            });
            updateButtons();
        });
    }

    private void updateButtons() {
        boolean selected = table.getSelectionModel().getSelectedItem() != null;
        editButton.setDisable(!selected);
        deleteButton.setDisable(!selected);
    }

    @FXML
    private void addInterview() {
        load(services.applications()::listActive, active -> {
            if (active.isEmpty()) {
                ctx.dialogs().info("No applications in progress",
                        "Interviews belong to an application that is still in progress. "
                                + "Mark an opportunity as applied first.");
                return;
            }
            ctx.dialogs().<Interview, InterviewDialogController>form("Add interview", "Add interview",
                    "interview-dialog.fxml", dialog -> dialog.configureNew(active, null))
                    .ifPresent(interview -> perform(() -> services.interviews().add(interview), () -> {
                        ctx.toasts().success("Interview added.");
                        onShow();
                    }, addButton));
        }, addButton);
    }

    @FXML
    private void editInterview() {
        Interview selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        ctx.dialogs().<Interview, InterviewDialogController>form("Edit interview", "Save changes",
                "interview-dialog.fxml", dialog -> dialog.configureEdit(selected))
                .ifPresent(interview -> perform(() -> services.interviews().update(interview), () -> {
                    ctx.toasts().success("Interview updated.");
                    onShow();
                }));
    }

    @FXML
    private void deleteInterview() {
        Interview selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        boolean confirmed = ctx.dialogs().confirmDestructive("Delete interview?",
                "Delete the " + selected.getRoundName() + " round for " + selected.getRoleTitle() + " at "
                        + selected.getCompanyName() + "? This can't be undone. To keep the record, edit it and set "
                        + "the result to Cancelled instead.",
                "Delete interview", "Keep interview");
        if (confirmed) {
            perform(() -> services.interviews().delete(selected), () -> {
                ctx.toasts().success("Interview deleted.");
                onShow();
            });
        }
    }
}
