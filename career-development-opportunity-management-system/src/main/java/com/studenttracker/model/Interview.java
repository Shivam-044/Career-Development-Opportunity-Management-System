package com.studenttracker.model;

import com.studenttracker.enums.InterviewMode;
import com.studenttracker.enums.InterviewResult;
import com.studenttracker.util.Validators;
import java.time.LocalDateTime;

public final class Interview {

    private Long interviewId;
    private final long applicationId;
    private String companyName;
    private String roleTitle;
    private String roundName;
    private LocalDateTime scheduledAt;
    private InterviewMode mode;
    private InterviewResult result = InterviewResult.PENDING;
    private String feedback;

    public Interview(long applicationId, String roundName, LocalDateTime scheduledAt, InterviewMode mode) {
        this.applicationId = applicationId;
        setRoundName(roundName);
        setScheduledAt(scheduledAt);
        setMode(mode);
    }

    public Long getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(Long interviewId) {
        this.interviewId = interviewId;
    }

    public long getApplicationId() {
        return applicationId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getRoleTitle() {
        return roleTitle;
    }

    public void setDisplayNames(String companyName, String roleTitle) {
        this.companyName = companyName;
        this.roleTitle = roleTitle;
    }

    public String getRoundName() {
        return roundName;
    }

    public void setRoundName(String roundName) {
        this.roundName = Validators.requireText(roundName, "Round name", 100);
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = Validators.requireNonNull(scheduledAt, "Interview date and time");
    }

    public InterviewMode getMode() {
        return mode;
    }

    public void setMode(InterviewMode mode) {
        this.mode = Validators.requireNonNull(mode, "Interview mode");
    }

    public InterviewResult getResult() {
        return result;
    }

    public void setResult(InterviewResult result) {
        this.result = Validators.requireNonNull(result, "Result");
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = Validators.optionalText(feedback, "Feedback", 2000);
    }
}
