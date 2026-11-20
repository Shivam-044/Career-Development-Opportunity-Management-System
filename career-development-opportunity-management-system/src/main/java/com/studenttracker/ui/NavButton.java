package com.studenttracker.ui;

import com.studenttracker.ui.support.Theme;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JToggleButton;

/** Sidebar navigation entry with a rounded highlight for the active and hovered states. */
final class NavButton extends JToggleButton {

    private static final long serialVersionUID = 1L;

    NavButton(String text) {
        super(text);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setHorizontalAlignment(LEFT);
        setForeground(Theme.SIDEBAR_TEXT);
        setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        setAlignmentX(LEFT_ALIGNMENT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (isSelected()) {
            g2.setColor(Theme.ACCENT);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        } else if (getModel().isRollover()) {
            g2.setColor(Theme.SIDEBAR_HOVER);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
