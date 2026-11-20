package com.studenttracker.ui.dialogs;

import com.studenttracker.exception.ValidationException;
import com.studenttracker.ui.support.FormPanel;
import com.studenttracker.ui.support.Theme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Base for modal data-entry dialogs. Subclasses add their fields to {@link #form()},
 * call {@link #finishLayout(Component)} and implement {@link #collect()}. Validation
 * messages appear inside the dialog so the user can correct the entry without losing it.
 */
abstract class FormDialog<T> extends JDialog {

    private static final long serialVersionUID = 1L;

    private final FormPanel form = new FormPanel();
    private final JLabel messageLabel = new JLabel(" ");
    private final JButton submitButton;
    private transient T result;

    protected FormDialog(Component owner, String title, String submitLabel) {
        super(owner == null ? null : SwingUtilities.getWindowAncestor(owner), title, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        submitButton = Theme.primaryButton(submitLabel);
        submitButton.addActionListener(event -> submit());
    }

    protected final FormPanel form() {
        return form;
    }

    protected abstract T collect();

    protected final void finishLayout(Component owner) {
        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));
        content.add(form, BorderLayout.CENTER);

        messageLabel.setForeground(Theme.DANGER);
        JButton cancel = Theme.secondaryButton("Cancel");
        cancel.addActionListener(event -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(cancel);
        buttons.add(submitButton);

        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.add(messageLabel, BorderLayout.CENTER);
        footer.add(buttons, BorderLayout.EAST);
        content.add(footer, BorderLayout.SOUTH);

        setContentPane(content);
        getRootPane().setDefaultButton(submitButton);
        getRootPane().registerKeyboardAction(event -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(owner);
    }

    private void submit() {
        try {
            result = collect();
            dispose();
        } catch (ValidationException ex) {
            messageLabel.setText("<html>" + ex.getMessage() + "</html>");
            pack();
        }
    }

    /** Shows the dialog and waits; empty if the user cancelled. */
    public final Optional<T> showDialog() {
        setVisible(true);
        return Optional.ofNullable(result);
    }

}
