package com.studenttracker.ui.support;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.JPanel;

/** A row with controls on the left (search, filters) and actions on the right. */
public final class Toolbar extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

    public Toolbar() {
        super(new BorderLayout());
        setOpaque(false);
        left.setOpaque(false);
        right.setOpaque(false);
        add(left, BorderLayout.WEST);
        add(right, BorderLayout.EAST);
    }

    public Toolbar left(Component... items) {
        for (Component item : items) {
            left.add(item);
        }
        return this;
    }

    public Toolbar right(Component... items) {
        for (Component item : items) {
            right.add(item);
        }
        return this;
    }
}
