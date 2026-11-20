package com.studenttracker.fx.navigation;

/** Every page of the application, with the text shown in the header and the FXML that builds it. */
public enum Route {
    DASHBOARD("Dashboard", "Where your placement preparation stands today.", "dashboard-view.fxml"),
    PROFILE("My Profile", "Your academic details. Skills and applications are linked to this profile.",
            "profile-view.fxml"),
    SKILLS("Skills", "Track what you are learning and what you have completed.", "skills-view.fxml"),
    COMPANIES("Companies", "Companies you are interested in. Each can have several opportunities.",
            "companies-view.fxml"),
    OPPORTUNITIES("Opportunities", "Jobs and internships you have saved. Mark one as applied to start tracking it.",
            "opportunities-view.fxml"),
    APPLICATIONS("Applications", "Follow each application from applied to offer.", "applications-view.fxml"),
    INTERVIEWS("Interviews", "Every interview round, with the outcome and what you learned from it.",
            "interviews-view.fxml"),
    REPORTS("Reports", "Application totals, interview outcomes and CSV exports.", "reports-view.fxml"),
    SETTINGS("Settings", "Appearance, accessibility and connection status.", "settings-view.fxml");

    private final String title;
    private final String subtitle;
    private final String fxml;

    Route(String title, String subtitle, String fxml) {
        this.title = title;
        this.subtitle = subtitle;
        this.fxml = fxml;
    }

    public String title() {
        return title;
    }

    public String subtitle() {
        return subtitle;
    }

    public String fxml() {
        return fxml;
    }
}
