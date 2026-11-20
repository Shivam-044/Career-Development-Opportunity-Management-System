package com.studenttracker.ui.support;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

/** Colours, fonts and small factory methods so every screen looks consistent. */
public final class Theme {

    public static final Color ACCENT = new Color(0x2563EB);
    public static final Color ACCENT_HOVER = new Color(0x1D4ED8);
    public static final Color SIDEBAR = new Color(0x111827);
    public static final Color SIDEBAR_HOVER = new Color(0x1F2937);
    public static final Color SIDEBAR_TEXT = new Color(0xE5E7EB);
    public static final Color SIDEBAR_MUTED = new Color(0x9CA3AF);
    public static final Color PAGE = new Color(0xF3F4F6);
    public static final Color SURFACE = Color.WHITE;
    public static final Color BORDER = new Color(0xE5E7EB);
    public static final Color TEXT = new Color(0x111827);
    public static final Color MUTED = new Color(0x6B7280);
    public static final Color SELECTION = new Color(0xDBEAFE);
    public static final Color DANGER = new Color(0xB91C1C);
    public static final Color SUCCESS = new Color(0x15803D);

    private Theme() {
    }

    public static Font font(int style, float size) {
        Font base = UIManager.getFont("Label.font");
        if (base == null) {
            base = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        }
        return base.deriveFont(style, size);
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.BOLD, 24f));
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel subtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.PLAIN, 13f));
        label.setForeground(MUTED);
        return label;
    }

    public static JLabel sectionHeading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.BOLD, 15f));
        label.setForeground(TEXT);
        return label;
    }

    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(ACCENT);
        button.setForeground(Color.WHITE);
        button.setFont(font(Font.BOLD, 13f));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static Border cardBorder(int padding) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(padding, padding, padding, padding));
    }

    public static JPanel card(int padding) {
        JPanel panel = new JPanel();
        panel.setBackground(SURFACE);
        panel.setBorder(cardBorder(padding));
        return panel;
    }
}
