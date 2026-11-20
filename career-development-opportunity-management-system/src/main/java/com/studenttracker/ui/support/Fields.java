package com.studenttracker.ui.support;

import com.studenttracker.exception.ValidationException;
import com.studenttracker.util.Dates;
import java.awt.Component;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

/** Factory and parsing helpers for form controls. */
public final class Fields {

    private Fields() {
    }

    public static JTextField text(int columns) {
        return new JTextField(columns);
    }

    public static JSpinner dateSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, Dates.DATE_PATTERN));
        return spinner;
    }

    public static JSpinner dateTimeSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
        spinner.setEditor(new JSpinner.DateEditor(spinner, Dates.DATE_TIME_PATTERN));
        return spinner;
    }

    /** A combo box with a leading "no selection" entry, which reads back as {@code null}. */
    public static <E> JComboBox<E> optionalCombo(E[] values, String emptyLabel) {
        JComboBox<E> combo = new JComboBox<>();
        combo.addItem(null);
        for (E value : values) {
            combo.addItem(value);
        }
        combo.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focused) {
                return super.getListCellRendererComponent(
                        list, value == null ? emptyLabel : value, index, selected, focused);
            }
        });
        combo.setSelectedIndex(0);
        return combo;
    }

    public static BigDecimal parseScore(String text, String field) {
        String value = text == null ? "" : text.trim();
        if (value.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            throw new ValidationException(field + " must be a number such as 8.25.");
        }
    }

    public static Integer parseWholeNumber(String text, String field) {
        String value = text == null ? "" : text.trim();
        if (value.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new ValidationException(field + " must be a whole number.");
        }
    }
}
