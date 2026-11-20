package com.studenttracker.ui.dialogs;

import com.studenttracker.model.Opportunity;
import com.studenttracker.ui.support.Fields;
import com.studenttracker.ui.support.TextAreaField;
import com.studenttracker.util.Dates;
import java.awt.Component;
import java.time.LocalDate;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JSpinner;

/** Asks for the date and notes when marking an opportunity as applied. */
public final class ApplyDialog extends FormDialog<ApplyDialog.Details> {

    private static final long serialVersionUID = 1L;

    public record Details(LocalDate appliedOn, String notes) {
    }

    private final JSpinner appliedOn = Fields.dateSpinner();
    private final TextAreaField notes = new TextAreaField(3, 24);

    public ApplyDialog(Component owner, Opportunity opportunity) {
        super(owner, "Mark as applied", "Mark as applied");
        form().addRow("Opportunity", new JLabel(opportunity.getRoleTitle() + " at " + opportunity.getCompanyName()));
        form().addRow("Applied on", appliedOn, true);
        form().addRow("Notes", notes);
        finishLayout(owner);
    }

    @Override
    protected Details collect() {
        return new Details(Dates.toLocalDate((Date) appliedOn.getValue()), notes.getText());
    }
}
