package com.studenttracker.util;

import com.studenttracker.exception.ValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/** Shared input checks. Every method returns the cleaned value or throws {@link ValidationException}. */
public final class Validators {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final BigDecimal MAX_SCORE = BigDecimal.TEN;

    private Validators() {
    }

    public static String requireText(String value, String field, int maxLength) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            throw new ValidationException(field + " is required.");
        }
        checkLength(text, field, maxLength);
        return text;
    }

    /** Returns the trimmed text, or {@code null} when the value is blank. */
    public static String optionalText(String value, String field, int maxLength) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            return null;
        }
        checkLength(text, field, maxLength);
        return text;
    }

    public static String optionalEmail(String value, String field, int maxLength) {
        String text = optionalText(value, field, maxLength);
        if (text != null && !EMAIL.matcher(text).matches()) {
            throw new ValidationException(field + " must look like name@example.com.");
        }
        return text;
    }

    public static String optionalHttpUrl(String value, String field, int maxLength) {
        String text = optionalText(value, field, maxLength);
        if (text == null) {
            return null;
        }
        String message = field + " must be a full web address starting with http:// or https://.";
        try {
            URI uri = new URI(text);
            String scheme = uri.getScheme();
            boolean web = "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
            if (!web || uri.getHost() == null) {
                throw new ValidationException(message);
            }
        } catch (URISyntaxException ex) {
            throw new ValidationException(message);
        }
        return text;
    }

    /** Accepts a value from 0.00 to 10.00 and normalises it to two decimal places. */
    public static BigDecimal optionalScore(BigDecimal value, String field) {
        if (value == null) {
            return null;
        }
        if (value.signum() < 0 || value.compareTo(MAX_SCORE) > 0) {
            throw new ValidationException(field + " must be between 0.00 and 10.00.");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static Integer optionalRange(Integer value, String field, int min, int max) {
        if (value != null && (value < min || value > max)) {
            throw new ValidationException(field + " must be between " + min + " and " + max + ".");
        }
        return value;
    }

    public static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new ValidationException(field + " is required.");
        }
        return value;
    }

    private static void checkLength(String text, String field, int maxLength) {
        if (text.length() > maxLength) {
            throw new ValidationException(field + " can't be longer than " + maxLength + " characters.");
        }
    }
}
