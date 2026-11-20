package com.studenttracker.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public final class Dates {

    public static final String DATE_PATTERN = "dd MMM yyyy";
    public static final String DATE_TIME_PATTERN = "dd MMM yyyy, HH:mm";

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern(DATE_PATTERN, Locale.ENGLISH);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN, Locale.ENGLISH);

    private Dates() {
    }

    public static String display(LocalDate date) {
        return date == null ? "" : DATE.format(date);
    }

    public static String display(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME.format(dateTime);
    }

    public static Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static Date toDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().withSecond(0).withNano(0);
    }
}
