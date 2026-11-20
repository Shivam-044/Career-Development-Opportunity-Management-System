package com.studenttracker.model;

import com.studenttracker.enums.ApplicationStatus;
import com.studenttracker.util.Validators;
import java.time.LocalDate;

public final class JobApplication {

    private Long applicationId;
    private final long studentId;
    private final long opportunityId;
    private String companyName;
    private String roleTitle;
    private ApplicationStatus status = ApplicationStatus.APPLIED;
    private LocalDate appliedOn;
    private String notes;

    public JobApplication(long studentId, long opportunityId, LocalDate appliedOn) {
        this.studentId = studentId;
        this.opportunityId = opportunityId;
        setAppliedOn(appliedOn);
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public long getStudentId() {
        return studentId;
    }

    public long getOpportunityId() {
        return opportunityId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getRoleTitle() {
        return roleTitle;
    }

    /** Sets the labels shown in tables; these come from the opportunity and are not stored on the application. */
    public void setDisplayNames(String companyName, String roleTitle) {
        this.companyName = companyName;
        this.roleTitle = roleTitle;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = Validators.requireNonNull(status, "Status");
    }

    public LocalDate getAppliedOn() {
        return appliedOn;
    }

    public void setAppliedOn(LocalDate appliedOn) {
        this.appliedOn = Validators.requireNonNull(appliedOn, "Applied date");
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = Validators.optionalText(notes, "Notes", 2000);
    }

    public String describe() {
        return roleTitle + " at " + companyName;
    }
}
