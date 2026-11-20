package com.studenttracker.enums;

import com.studenttracker.util.Labels;

public enum OpportunityType {
    INTERNSHIP, FULL_TIME;

    @Override
    public String toString() {
        return Labels.of(this);
    }
}
