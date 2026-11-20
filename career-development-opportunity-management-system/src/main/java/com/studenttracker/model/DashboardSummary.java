package com.studenttracker.model;

import com.studenttracker.enums.ApplicationStatus;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Read-only snapshot of the numbers and lists shown on the dashboard. */
public record DashboardSummary(
        int savedOpportunities,
        int activeApplications,
        int upcomingInterviewCount,
        int offers,
        int skillsInProgress,
        int completedSkills,
        int upcomingDeadlineCount,
        Map<ApplicationStatus, Integer> applicationsByStatus,
        List<Interview> upcomingInterviews,
        List<Opportunity> upcomingDeadlines) {

    public DashboardSummary {
        applicationsByStatus = Map.copyOf(new EnumMap<>(applicationsByStatus));
        upcomingInterviews = List.copyOf(upcomingInterviews);
        upcomingDeadlines = List.copyOf(upcomingDeadlines);
    }

    public int totalApplications() {
        return applicationsByStatus.values().stream().mapToInt(Integer::intValue).sum();
    }
}
