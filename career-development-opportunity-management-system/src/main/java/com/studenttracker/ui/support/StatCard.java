package com.studenttracker.ui.support;

import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** A single number with a caption, used on the dashboard. */
public final class StatCard extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JLabel value = new JLabel("-");

    public StatCard(String caption) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.SURFACE);
        setBorder(Theme.cardBorder(16));

        value.setFont(Theme.font(Font.BOLD, 30f));
        value.setForeground(Theme.TEXT);
        value.setAlignmentX(LEFT_ALIGNMENT);

        JLabel label = new JLabel(caption);
        label.setFont(Theme.font(Font.PLAIN, 12f));
        label.setForeground(Theme.MUTED);
        label.setAlignmentX(LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        add(value);
        add(label);
    }

    public void setValue(int number) {
        value.setText(Integer.toString(number));
    }
}
