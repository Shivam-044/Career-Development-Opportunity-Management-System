package com.studenttracker.enums;

import com.studenttracker.util.Labels;

public enum Proficiency {
    BEGINNER, INTERMEDIATE, ADVANCED;

    @Override
    public String toString() {
        return Labels.of(this);
    }
}
