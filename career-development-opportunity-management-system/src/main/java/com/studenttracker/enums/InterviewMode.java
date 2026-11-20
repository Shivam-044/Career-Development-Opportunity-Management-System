package com.studenttracker.enums;

import com.studenttracker.util.Labels;

public enum InterviewMode {
    ONLINE, OFFLINE, PHONE;

    @Override
    public String toString() {
        return Labels.of(this);
    }
}
