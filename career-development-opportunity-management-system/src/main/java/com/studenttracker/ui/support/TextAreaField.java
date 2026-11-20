package com.studenttracker.ui.support;

import java.awt.KeyboardFocusManager;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** Multi-line text input that wraps lines and lets Tab move focus instead of typing a tab character. */
public final class TextAreaField extends JScrollPane {

    private static final long serialVersionUID = 1L;

    private final JTextArea area;

    public TextAreaField(int rows, int columns) {
        this.area = new JTextArea(rows, columns);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        for (int direction : new int[] {
                KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS}) {
            area.setFocusTraversalKeys(direction,
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalKeys(direction));
        }
        setViewportView(area);
    }

    public String getText() {
        return area.getText();
    }

    public void setText(String text) {
        area.setText(text == null ? "" : text);
        area.setCaretPosition(0);
    }
}
