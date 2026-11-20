package com.studenttracker.model;

import com.studenttracker.enums.OpportunityStatus;
import com.studenttracker.enums.OpportunityType;
import com.studenttracker.enums.WorkMode;
import com.studenttracker.util.Validators;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Opportunity {

    private Long opportunityId;
    private long companyId;
    private String companyName;
    private String roleTitle;
    private OpportunityType type;
    private String location;
    private WorkMode workMode;
    private String description;
    private String sourceUrl;
    private BigDecimal minimumCgpa;
    private LocalDate deadline;
    private OpportunityStatus status = OpportunityStatus.OPEN;
    private List<String> requiredSkills = List.of();

    public Opportunity(long companyId, String roleTitle, OpportunityType type) {
        this.companyId = companyId;
        setRoleTitle(roleTitle);
        setType(type);
    }

    public Long getOpportunityId() {
        return opportunityId;
    }

    public void setOpportunityId(Long opportunityId) {
        this.opportunityId = opportunityId;
    }

    public long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(long companyId) {
        this.companyId = companyId;
    }

    /** Display name of the company; filled in when the record is loaded, not validated or stored here. */
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getRoleTitle() {
        return roleTitle;
    }

    public void setRoleTitle(String roleTitle) {
        this.roleTitle = Validators.requireText(roleTitle, "Role title", 150);
    }

    public OpportunityType getType() {
        return type;
    }

    public void setType(OpportunityType type) {
        this.type = Validators.requireNonNull(type, "Type");
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = Validators.optionalText(location, "Location", 150);
    }

    public WorkMode getWorkMode() {
        return workMode;
    }

    public void setWorkMode(WorkMode workMode) {
        this.workMode = workMode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Validators.optionalText(description, "Description", 5000);
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = Validators.optionalHttpUrl(sourceUrl, "Source URL", 500);
    }

    public BigDecimal getMinimumCgpa() {
        return minimumCgpa;
    }

    public void setMinimumCgpa(BigDecimal minimumCgpa) {
        this.minimumCgpa = Validators.optionalScore(minimumCgpa, "Minimum CGPA");
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public OpportunityStatus getStatus() {
        return status;
    }

    public void setStatus(OpportunityStatus status) {
        this.status = Validators.requireNonNull(status, "Listing status");
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    /** Stores the skill names trimmed and without case-insensitive duplicates, keeping the first spelling. */
    public void setRequiredSkills(Collection<String> skills) {
        Map<String, String> unique = new LinkedHashMap<>();
        for (String skill : skills) {
            String name = Validators.optionalText(skill, "Required skill", 100);
            if (name != null) {
                unique.putIfAbsent(name.toLowerCase(Locale.ROOT), name);
            }
        }
        this.requiredSkills = List.copyOf(unique.values());
    }

    public boolean isDeadlinePassed(LocalDate today) {
        return deadline != null && deadline.isBefore(today);
    }
}
