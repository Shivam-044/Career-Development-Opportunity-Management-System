package com.studenttracker.ui.support;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Two-column label and field layout. Required fields get an asterisk. */
public final class FormPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private int row;

    public FormPanel() {
        super(new GridBagLayout());
        setOpaque(false);
    }

    public void addRow(String label, JComponent field) {
        addRow(label, field, false);
    }

    public void addRow(String label, JComponent field, boolean required) {
        JLabel caption = new JLabel(required ? label + " *" : label);
        caption.setForeground(Theme.TEXT);

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        labelConstraints.insets = new Insets(9, 0, 5, 16);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        fieldConstraints.insets = new Insets(4, 0, 4, 0);

        add(caption, labelConstraints);
        add(field, fieldConstraints);
        row++;
    }
}
