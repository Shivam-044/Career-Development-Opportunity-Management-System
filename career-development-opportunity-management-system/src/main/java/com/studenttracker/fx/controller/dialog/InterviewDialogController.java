package com.studenttracker.fx.controller.dialog;

import com.studenttracker.enums.InterviewMode;
import com.studenttracker.enums.InterviewResult;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.fx.support.Combos;
import com.studenttracker.fx.support.DialogForm;
import com.studenttracker.fx.support.FxFormats;
import com.studenttracker.model.Interview;
import com.studenttracker.model.JobApplication;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

public final class InterviewDialogController implements DialogForm<Interview> {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("H:mm");

    @FXML
    private ComboBox<JobApplication> applicationCombo;
    @FXML
    private Label applicationLabel;
    @FXML
    private TextField roundField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField timeField;
    @FXML
    private ComboBox<InterviewMode> modeCombo;
    @FXML
    private ComboBox<InterviewResult> resultCombo;
    @FXML
    private TextArea feedbackArea;

    private Interview editing;

    @FXML
    private void initialize() {
        Combos.fill(modeCombo, InterviewMode.values());
        Combos.fill(resultCombo, InterviewResult.values());
        modeCombo.getSelectionModel().select(InterviewMode.ONLINE);
        resultCombo.getSelectionModel().select(InterviewResult.PENDING);
        FxFormats.configure(datePicker);
        datePicker.setValue(LocalDate.now());
        timeField.setText(TIME.format(LocalTime.now().plusHours(1).withMinute(0)));
        applicationCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(JobApplication application) {
                return application == null ? ""
                        : application.describe() + " (" + application.getStatus() + ")";
            }

            @Override
            public JobApplication fromString(String text) {
                return null;
            }
        });
    }

    /** Prepares the dialog for a new interview; {@code preselected} fixes the application, or is null to choose. */
    public void configureNew(List<JobApplication> applications, JobApplication preselected) {
        applicationCombo.setItems(FXCollections.observableArrayList(applications));
        if (preselected != null) {
            applicationCombo.getSelectionModel().select(preselected);
            applicationCombo.setDisable(true);
        }
    }

    public void configureEdit(Interview interview) {
        this.editing = interview;
        applicationCombo.setVisible(false);
        applicationCombo.setManaged(false);
        applicationLabel.setText(interview.getRoleTitle() + " at " + interview.getCompanyName());
        applicationLabel.setVisible(true);
        applicationLabel.setManaged(true);
        roundField.setText(interview.getRoundName());
        datePicker.setValue(interview.getScheduledAt().toLocalDate());
        timeField.setText(TIME.format(interview.getScheduledAt().toLocalTime()));
        modeCombo.getSelectionModel().select(interview.getMode());
        resultCombo.getSelectionModel().select(interview.getResult());
        feedbackArea.setText(interview.getFeedback());
    }

    @Override
    public Interview collect() {
        long applicationId;
        if (editing != null) {
            applicationId = editing.getApplicationId();
        } else {
            JobApplication selected = applicationCombo.getValue();
            if (selected == null) {
                throw new ValidationException("Application is required.");
            }
            applicationId = selected.getApplicationId();
        }
        LocalDate date = datePicker.getValue();
        if (date == null) {
            throw new ValidationException("Interview date is required.");
        }
        Interview interview = new Interview(applicationId, roundField.getText(),
                LocalDateTime.of(date, parseTime(timeField.getText())), modeCombo.getValue());
        interview.setResult(resultCombo.getValue());
        interview.setFeedback(feedbackArea.getText());
        if (editing != null) {
            interview.setInterviewId(editing.getInterviewId());
            interview.setDisplayNames(editing.getCompanyName(), editing.getRoleTitle());
        }
        return interview;
    }

    @Override
    public void focusFirstField() {
        if (editing == null && !applicationCombo.isDisabled()) {
            applicationCombo.requestFocus();
        } else {
            roundField.requestFocus();
        }
    }

    private static LocalTime parseTime(String text) {
        try {
            return LocalTime.parse(text == null ? "" : text.trim(), TIME);
        } catch (DateTimeParseException ex) {
            throw new ValidationException("Time must look like 14:30 (24-hour clock).");
        }
    }
}
