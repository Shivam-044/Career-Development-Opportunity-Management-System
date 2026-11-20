package com.studenttracker.util;

import java.util.Locale;

public final class Labels {

    private Labels() {
    }

    /** Turns an enum constant such as {@code FULL_TIME} into display text such as "Full time". */
    public static String of(Enum<?> value) {
        String text = value.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
