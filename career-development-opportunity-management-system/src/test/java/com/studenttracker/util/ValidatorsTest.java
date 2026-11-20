package com.studenttracker.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.studenttracker.exception.ValidationException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ValidatorsTest {

    @Test
    void trimsRequiredText() {
        assertEquals("Java", Validators.requireText("  Java  ", "Skill", 20));
    }

    @Test
    void rejectsTextThatIsTooLong() {
        assertThrows(ValidationException.class, () -> Validators.requireText("abcdef", "Skill", 5));
    }

    @Test
    void optionalTextTurnsBlankIntoNull() {
        assertNull(Validators.optionalText("   ", "Notes", 10));
    }

    @Test
    void acceptsBoundaryScores() {
        assertEquals(new BigDecimal("0.00"), Validators.optionalScore(BigDecimal.ZERO, "CGPA"));
        assertEquals(new BigDecimal("10.00"), Validators.optionalScore(BigDecimal.TEN, "CGPA"));
    }

    @Test
    void rejectsScoresOutsideTheRange() {
        assertThrows(ValidationException.class, () -> Validators.optionalScore(new BigDecimal("10.01"), "CGPA"));
    }

    @Test
    void requiresAHostInWebAddresses() {
        assertThrows(ValidationException.class, () -> Validators.optionalHttpUrl("https://", "URL", 100));
    }
}
