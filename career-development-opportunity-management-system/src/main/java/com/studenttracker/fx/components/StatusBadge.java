package com.studenttracker.fx.components;

import com.studenttracker.enums.ApplicationStatus;
import com.studenttracker.enums.InterviewResult;
import com.studenttracker.enums.OpportunityStatus;
import com.studenttracker.enums.OpportunityType;
import com.studenttracker.enums.SkillStatus;
import javafx.scene.control.Label;

/** A small rounded pill that colours a status. The variant maps to a {@code badge-*} style class. */
public class StatusBadge extends Label {

    public StatusBadge() {
        getStyleClass().add("badge");
    }

    public StatusBadge(String text, String variant) {
        this();
        setText(text);
        setVariant(variant);
    }

    public final void setVariant(String variant) {
        getStyleClass().removeIf(name -> name.startsWith("badge-"));
        getStyleClass().add("badge-" + variant);
    }

    public static StatusBadge forApplication(ApplicationStatus status) {
        String variant = switch (status) {
            case APPLIED -> "info";
            case ASSESSMENT -> "warning";
            case INTERVIEW -> "accent";
            case OFFER -> "success";
            case REJECTED -> "danger";
            case WITHDRAWN -> "muted";
        };
        return new StatusBadge(status.toString(), variant);
    }

    public static StatusBadge forOpportunity(OpportunityStatus status) {
        String variant = switch (status) {
            case OPEN -> "success";
            case CLOSED -> "warning";
            case ARCHIVED -> "muted";
        };
        return new StatusBadge(status.toString(), variant);
    }

    public static StatusBadge forOpportunityType(OpportunityType type) {
        return new StatusBadge(type.toString(), type == OpportunityType.INTERNSHIP ? "info" : "accent");
    }

    public static StatusBadge forInterviewResult(InterviewResult result) {
        String variant = switch (result) {
            case PENDING -> "info";
            case PASSED -> "success";
            case FAILED -> "danger";
            case CANCELLED -> "muted";
        };
        return new StatusBadge(result.toString(), variant);
    }

    public static StatusBadge forSkillStatus(SkillStatus status) {
        return new StatusBadge(status.toString(), status == SkillStatus.COMPLETED ? "success" : "accent");
    }
}
