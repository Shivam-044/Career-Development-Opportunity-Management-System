package com.studenttracker.fx.controller.dialog;

import com.studenttracker.fx.support.DialogForm;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public final class NotesDialogController implements DialogForm<String> {

    @FXML
    private Label subjectLabel;
    @FXML
    private TextArea notesArea;

    public void configure(String subject, String currentNotes) {
        subjectLabel.setText(subject);
        notesArea.setText(currentNotes);
    }

    @Override
    public String collect() {
        return notesArea.getText();
    }

    @Override
    public void focusFirstField() {
        notesArea.requestFocus();
        notesArea.end();
    }
}
