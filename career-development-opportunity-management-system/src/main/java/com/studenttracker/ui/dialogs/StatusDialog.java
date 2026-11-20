package com.studenttracker.ui.dialogs;

import com.studenttracker.enums.ApplicationStatus;
import com.studenttracker.ui.support.Theme;
import java.awt.Component;
import java.util.Collection;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/** Lets the user pick a new application status from the given options. */
public final class StatusDialog extends FormDialog<ApplicationStatus> {

    private static final long serialVersionUID = 1L;

    private final JComboBox<ApplicationStatus> choices;

    public StatusDialog(Component owner, String title, String explanation, String currentLabel,
                        Collection<ApplicationStatus> options) {
        super(owner, title, "Update status");
        choices = new JComboBox<>(options.toArray(new ApplicationStatus[0]));
        choices.setSelectedIndex(0);

        JLabel note = new JLabel("<html><body style='width: 280px'>" + explanation + "</body></html>");
        note.setForeground(Theme.MUTED);
        form().addRow("Application", new JLabel(currentLabel));
        form().addRow("New status", choices, true);
        form().addRow("", note);
        finishLayout(owner);
    }

    @Override
    protected ApplicationStatus collect() {
        return (ApplicationStatus) choices.getSelectedItem();
    }
}
