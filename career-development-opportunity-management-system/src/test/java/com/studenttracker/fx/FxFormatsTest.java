package com.studenttracker.fx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.studenttracker.fx.support.FxFormats;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class FxFormatsTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 16);

    @Test
    void initialsUseTheFirstAndLastName() {
        assertEquals("AS", FxFormats.initials("Aarav Kumar Sharma"));
        assertEquals("M", FxFormats.initials("madonna"));
        assertEquals("?", FxFormats.initials("  "));
        assertEquals("?", FxFormats.initials(null));
    }

    @Test
    void firstNameIsTheFirstWord() {
        assertEquals("Aarav", FxFormats.firstName("  Aarav Sharma "));
        assertEquals("", FxFormats.firstName(null));
    }

    @Test
    void relativeDaysReadNaturally() {
        assertEquals("Today", FxFormats.relativeDay(TODAY, TODAY));
        assertEquals("Tomorrow", FxFormats.relativeDay(TODAY.plusDays(1), TODAY));
        assertEquals("Yesterday", FxFormats.relativeDay(TODAY.minusDays(1), TODAY));
        assertEquals("In 5 days", FxFormats.relativeDay(TODAY.plusDays(5), TODAY));
        assertEquals("3 days ago", FxFormats.relativeDay(TODAY.minusDays(3), TODAY));
    }
}
