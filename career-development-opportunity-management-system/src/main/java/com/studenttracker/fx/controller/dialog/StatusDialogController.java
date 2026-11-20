package com.studenttracker.fx.controller.dialog;

import com.studenttracker.enums.ApplicationStatus;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.fx.support.DialogForm;
import java.util.Collection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

/** Lets the user pick a new application status from the options the caller allows. */
public final class StatusDialogController implements DialogForm<ApplicationStatus> {

    @FXML
    private Label contextLabel;
    @FXML
    private ComboBox<ApplicationStatus> statusCombo;
    @FXML
    private Label explanationLabel;

    public void configure(String context, String explanation, Collection<ApplicationStatus> options) {
        contextLabel.setText(context);
        explanationLabel.setText(explanation);
        statusCombo.setItems(FXCollections.observableArrayList(options));
        statusCombo.getSelectionModel().selectFirst();
    }

    @Override
    public ApplicationStatus collect() {
        ApplicationStatus selected = statusCombo.getValue();
        if (selected == null) {
            throw new ValidationException("Choose a status.");
        }
        return selected;
    }

    @Override
    public void focusFirstField() {
        statusCombo.requestFocus();
    }
}
