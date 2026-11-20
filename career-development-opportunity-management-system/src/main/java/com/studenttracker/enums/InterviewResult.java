package com.studenttracker.enums;

import com.studenttracker.util.Labels;

public enum InterviewResult {
    PENDING, PASSED, FAILED, CANCELLED;

    @Override
    public String toString() {
        return Labels.of(this);
    }
}
