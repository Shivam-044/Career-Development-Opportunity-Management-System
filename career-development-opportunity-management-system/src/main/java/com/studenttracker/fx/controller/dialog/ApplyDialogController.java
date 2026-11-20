package com.studenttracker.fx.controller.dialog;

import com.studenttracker.exception.ValidationException;
import com.studenttracker.fx.support.DialogForm;
import com.studenttracker.fx.support.FxFormats;
import com.studenttracker.model.Opportunity;
import java.time.LocalDate;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/** Asks for the date and notes when marking an opportunity as applied. */
public final class ApplyDialogController implements DialogForm<ApplyDialogController.Details> {

    public record Details(LocalDate appliedOn, String notes) {
    }

    @FXML
    private Label opportunityLabel;
    @FXML
    private DatePicker appliedOnPicker;
    @FXML
    private TextArea notesArea;

    @FXML
    private void initialize() {
        FxFormats.configure(appliedOnPicker);
        appliedOnPicker.setValue(LocalDate.now());
    }

    public void configure(Opportunity opportunity) {
        opportunityLabel.setText(opportunity.getRoleTitle() + " at " + opportunity.getCompanyName());
    }

    @Override
    public Details collect() {
        LocalDate date = appliedOnPicker.getValue();
        if (date == null) {
            throw new ValidationException("Applied date is required.");
        }
        return new Details(date, notesArea.getText());
    }

    @Override
    public void focusFirstField() {
        appliedOnPicker.requestFocus();
    }
}
