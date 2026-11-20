package com.studenttracker.ui.support;

import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Text field that calls back shortly after the user stops typing. */
public final class SearchField extends JTextField {

    private static final long serialVersionUID = 1L;
    private static final int DELAY_MILLIS = 250;

    public SearchField(String placeholder, Runnable onChange) {
        super(22);
        putClientProperty("JTextField.placeholderText", placeholder);
        putClientProperty("JTextField.showClearButton", true);
        setToolTipText(placeholder);

        Timer debounce = new Timer(DELAY_MILLIS, event -> onChange.run());
        debounce.setRepeats(false);
        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                debounce.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                debounce.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                debounce.restart();
            }
        });
    }

    public String query() {
        return getText().trim();
    }
}
