package com.studenttracker.ui.support;

import com.studenttracker.util.Dates;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.Date;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;

/** A date picker that can also be left empty, for dates that are not always known. */
public final class OptionalDateField extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JCheckBox enabled = new JCheckBox("Set date");
    private final JSpinner spinner = Fields.dateSpinner();

    public OptionalDateField() {
        super(new FlowLayout(FlowLayout.LEFT, 8, 0));
        setOpaque(false);
        spinner.setEnabled(false);
        enabled.setOpaque(false);
        enabled.addActionListener(event -> spinner.setEnabled(enabled.isSelected()));
        add(enabled);
        add(spinner);
    }

    /** The chosen date, or {@code null} when none is set. */
    public LocalDate getDate() {
        return enabled.isSelected() ? Dates.toLocalDate((Date) spinner.getValue()) : null;
    }

    public void setDate(LocalDate date) {
        enabled.setSelected(date != null);
        spinner.setEnabled(date != null);
        if (date != null) {
            spinner.setValue(Dates.toDate(date));
        }
    }
}
