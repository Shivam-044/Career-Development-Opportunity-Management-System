package com.studenttracker.fx.support;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

public final class FxFormats {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private FxFormats() {
    }

    /** Shows dates as "13 Nov 2026" and accepts that format or ISO (2026-11-13) when typed. */
    public static void configure(DatePicker picker) {
        picker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : DATE.format(date);
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.isBlank()) {
                    return null;
                }
                String value = text.trim();
                try {
                    return LocalDate.parse(value, DATE);
                } catch (DateTimeParseException first) {
                    try {
                        return LocalDate.parse(value);
                    } catch (DateTimeParseException second) {
                        return null;
                    }
                }
            }
        });
        picker.setPromptText("dd Mon yyyy");
    }

    public static String initials(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase(Locale.ROOT);
    }

    public static String firstName(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        return name.trim().split("\\s+")[0];
    }

    /** "Today", "Tomorrow", "In 3 days", "2 days ago". */
    public static String relativeDay(LocalDate date, LocalDate today) {
        long days = ChronoUnit.DAYS.between(today, date);
        if (days == 0) {
            return "Today";
        }
        if (days == 1) {
            return "Tomorrow";
        }
        if (days == -1) {
            return "Yesterday";
        }
        return days > 0 ? "In " + days + " days" : Math.abs(days) + " days ago";
    }
}
