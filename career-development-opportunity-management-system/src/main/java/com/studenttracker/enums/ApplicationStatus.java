package com.studenttracker.enums;

import com.studenttracker.util.Labels;
import java.util.EnumSet;
import java.util.Set;

public enum ApplicationStatus {
    APPLIED,
    ASSESSMENT,
    INTERVIEW,
    OFFER,
    REJECTED,
    WITHDRAWN;

    /** The statuses an application in this status may normally move to. */
    public Set<ApplicationStatus> nextStatuses() {
        return switch (this) {
            case APPLIED -> EnumSet.of(ASSESSMENT, INTERVIEW, OFFER, REJECTED, WITHDRAWN);
            case ASSESSMENT -> EnumSet.of(INTERVIEW, OFFER, REJECTED, WITHDRAWN);
            case INTERVIEW -> EnumSet.of(OFFER, REJECTED, WITHDRAWN);
            case OFFER, REJECTED, WITHDRAWN -> EnumSet.noneOf(ApplicationStatus.class);
        };
    }

    public boolean canMoveTo(ApplicationStatus target) {
        return nextStatuses().contains(target);
    }

    public boolean isTerminal() {
        return nextStatuses().isEmpty();
    }

    /** True while the application is still in progress (applied, assessment or interview). */
    public boolean isActive() {
        return this == APPLIED || this == ASSESSMENT || this == INTERVIEW;
    }

    @Override
    public String toString() {
        return Labels.of(this);
    }
}
