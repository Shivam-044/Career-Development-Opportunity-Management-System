package com.studenttracker.fx.support;

import com.studenttracker.enums.ApplicationStatus;
import com.studenttracker.enums.InterviewResult;

/** Chart colours. The same in both themes, chosen to stay legible on dark and light surfaces. */
public final class Palette {

    private Palette() {
    }

    public static String applicationStatus(ApplicationStatus status) {
        return switch (status) {
            case APPLIED -> "#4CA9D8";
            case ASSESSMENT -> "#F5B94A";
            case INTERVIEW -> "#7667F8";
            case OFFER -> "#29C9A5";
            case REJECTED -> "#F26B7A";
            case WITHDRAWN -> "#8B96AE";
        };
    }

    public static String interviewResult(InterviewResult result) {
        return switch (result) {
            case PENDING -> "#4CA9D8";
            case PASSED -> "#29C9A5";
            case FAILED -> "#F26B7A";
            case CANCELLED -> "#8B96AE";
        };
    }
}
