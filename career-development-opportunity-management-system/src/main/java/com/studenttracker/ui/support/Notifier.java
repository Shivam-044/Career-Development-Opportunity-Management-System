package com.studenttracker.ui.support;

import com.studenttracker.exception.DataAccessException;
import com.studenttracker.exception.DuplicateApplicationException;
import com.studenttracker.exception.ValidationException;
import java.awt.Component;
import java.io.UncheckedIOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/** Dialogs for feedback. Messages say what happened and what the user can do about it. */
public final class Notifier {

    private static final Logger LOG = Logger.getLogger(Notifier.class.getName());
    private static final String DATABASE_HINT =
            "Check that MySQL is running and that the connection settings are correct, then try again.";

    private Notifier() {
    }

    public static void error(Component parent, Throwable failure) {
        if (failure instanceof ValidationException) {
            show(parent, "Check your entry", failure.getMessage(), JOptionPane.WARNING_MESSAGE);
        } else if (failure instanceof DuplicateApplicationException) {
            show(parent, "Already applied", failure.getMessage(), JOptionPane.WARNING_MESSAGE);
        } else if (failure instanceof DataAccessException) {
            LOG.log(Level.WARNING, failure.getMessage(), failure);
            StringBuilder message = new StringBuilder(failure.getMessage());
            if (failure.getCause() != null && failure.getCause().getMessage() != null) {
                message.append("\n\nDetails: ").append(failure.getCause().getMessage());
            }
            message.append("\n\n").append(DATABASE_HINT);
            show(parent, "Database problem", message.toString(), JOptionPane.ERROR_MESSAGE);
        } else if (failure instanceof UncheckedIOException) {
            LOG.log(Level.WARNING, "File operation failed", failure);
            show(parent, "Couldn't write the file",
                    "The file couldn't be saved. Check that the folder exists, that you can write to it, "
                            + "and that the file isn't open in another program such as Excel.",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            LOG.log(Level.SEVERE, "Unexpected failure", failure);
            String reason = failure == null || failure.getMessage() == null ? "" : " (" + failure.getMessage() + ")";
            show(parent, "Something went wrong",
                    "The app ran into an unexpected problem" + reason + ". Try again, and restart the app if it keeps happening.",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void info(Component parent, String title, String message) {
        show(parent, title, message, JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String title, String message,
                                  String confirmLabel, String cancelLabel) {
        return choose(parent, title, message, confirmLabel, cancelLabel) == 0;
    }

    /** Shows one button per label and returns the index of the one pressed, or -1 if the dialog was closed. */
    public static int choose(Component parent, String title, String message, String... labels) {
        return JOptionPane.showOptionDialog(parent, wrap(message), title, JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, labels, labels[labels.length - 1]);
    }

    private static void show(Component parent, String title, String message, int type) {
        JOptionPane.showMessageDialog(parent, wrap(message), title, type);
    }

    private static String wrap(String message) {
        String escaped = message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\n", "<br>");
        return "<html><body style='width: 340px'>" + escaped + "</body></html>";
    }
}
