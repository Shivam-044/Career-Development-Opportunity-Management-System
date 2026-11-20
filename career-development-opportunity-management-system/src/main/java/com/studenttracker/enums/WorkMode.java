package com.studenttracker.enums;

import com.studenttracker.util.Labels;

public enum WorkMode {
    ONSITE, HYBRID, REMOTE;

    @Override
    public String toString() {
        return Labels.of(this);
    }
}
