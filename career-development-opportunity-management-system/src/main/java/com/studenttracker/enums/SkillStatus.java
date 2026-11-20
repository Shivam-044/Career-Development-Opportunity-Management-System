package com.studenttracker.enums;

import com.studenttracker.util.Labels;

public enum SkillStatus {
    LEARNING, COMPLETED;

    @Override
    public String toString() {
        return Labels.of(this);
    }
}
