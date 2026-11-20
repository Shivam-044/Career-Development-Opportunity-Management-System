package com.studenttracker.service;

import com.studenttracker.dao.ApplicationDAO;
import com.studenttracker.enums.ApplicationStatus;
import com.studenttracker.exception.DuplicateApplicationException;
import com.studenttracker.model.JobApplication;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class InMemoryApplicationDao implements ApplicationDAO {

    private final Map<Long, JobApplication> applications = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public long insert(JobApplication application) {
        if (exists(application.getStudentId(), application.getOpportunityId())) {
            throw new DuplicateApplicationException("duplicate");
        }
        long id = nextId++;
        application.setApplicationId(id);
        applications.put(id, application);
        return id;
    }

    @Override
    public Optional<JobApplication> findById(long id) {
        return Optional.ofNullable(applications.get(id));
    }

    @Override
    public boolean exists(long studentId, long opportunityId) {
        return applications.values().stream().anyMatch(application ->
                application.getStudentId() == studentId && application.getOpportunityId() == opportunityId);
    }

    @Override
    public List<JobApplication> search(String keyword, ApplicationStatus status) {
        return applications.values().stream()
                .filter(application -> status == null || application.getStatus() == status)
                .toList();
    }

    @Override
    public void updateStatus(long id, ApplicationStatus status) {
        applications.get(id).setStatus(status);
    }

    @Override
    public void updateNotes(long id, String notes) {
        applications.get(id).setNotes(notes);
    }

    @Override
    public Map<ApplicationStatus, Integer> countByStatus() {
        Map<ApplicationStatus, Integer> counts = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus status : ApplicationStatus.values()) {
            counts.put(status, 0);
        }
        applications.values().forEach(application -> counts.merge(application.getStatus(), 1, Integer::sum));
        return counts;
    }
}
