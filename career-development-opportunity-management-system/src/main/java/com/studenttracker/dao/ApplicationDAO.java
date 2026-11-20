package com.studenttracker.dao;

import com.studenttracker.enums.ApplicationStatus;
import com.studenttracker.model.JobApplication;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ApplicationDAO {

    /**
     * @throws com.studenttracker.exception.DuplicateApplicationException if the student already applied
     */
    long insert(JobApplication application);

    Optional<JobApplication> findById(long id);

    boolean exists(long studentId, long opportunityId);

    /** Matches company or role; a {@code null} status means any status. */
    List<JobApplication> search(String keyword, ApplicationStatus status);

    void updateStatus(long id, ApplicationStatus status);

    void updateNotes(long id, String notes);

    /** Application counts for every status, including statuses with no applications. */
    Map<ApplicationStatus, Integer> countByStatus();
}
