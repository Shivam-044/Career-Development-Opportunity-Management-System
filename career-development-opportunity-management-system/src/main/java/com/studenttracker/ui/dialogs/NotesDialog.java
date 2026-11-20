package com.studenttracker.ui.dialogs;

import com.studenttracker.ui.support.TextAreaField;
import java.awt.Component;
import javax.swing.JLabel;

public final class NotesDialog extends FormDialog<String> {

    private static final long serialVersionUID = 1L;

    private final TextAreaField notes = new TextAreaField(6, 30);

    public NotesDialog(Component owner, String subject, String currentNotes) {
        super(owner, "Application notes", "Save notes");
        notes.setText(currentNotes);
        form().addRow("Application", new JLabel(subject));
        form().addRow("Notes", notes);
        finishLayout(owner);
    }

    @Override
    protected String collect() {
        return notes.getText();
    }
}
