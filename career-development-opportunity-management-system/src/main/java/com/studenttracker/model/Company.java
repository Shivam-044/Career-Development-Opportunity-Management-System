package com.studenttracker.model;

import com.studenttracker.util.Validators;

public final class Company {

    private Long companyId;
    private String companyName;
    private String industry;
    private String website;
    private String location;
    private String notes;
    private boolean archived;

    public Company(String companyName) {
        setCompanyName(companyName);
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = Validators.requireText(companyName, "Company name", 150);
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = Validators.optionalText(industry, "Industry", 100);
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = Validators.optionalHttpUrl(website, "Website", 255);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = Validators.optionalText(location, "Location", 150);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = Validators.optionalText(notes, "Notes", 2000);
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    @Override
    public String toString() {
        return companyName;
    }
}
