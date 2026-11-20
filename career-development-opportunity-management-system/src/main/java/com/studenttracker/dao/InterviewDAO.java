package com.studenttracker.dao;

import com.studenttracker.model.Interview;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InterviewDAO {

    long insert(Interview interview);

    void update(Interview interview);

    void delete(long id);

    Optional<Interview> findById(long id);

    /** All interviews, most recent first. */
    List<Interview> findAll();

    /** Interviews scheduled at or after {@code from} that are not cancelled, soonest first. */
    List<Interview> findUpcoming(LocalDateTime from, int limit);

    int countUpcoming(LocalDateTime from);
}
