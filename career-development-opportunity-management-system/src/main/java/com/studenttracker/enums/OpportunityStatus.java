package com.studenttracker.enums;

import com.studenttracker.util.Labels;

public enum OpportunityStatus {
    OPEN, CLOSED, ARCHIVED;

    @Override
    public String toString() {
        return Labels.of(this);
    }
}
