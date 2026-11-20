package com.studenttracker.dao;

import com.studenttracker.enums.OpportunityStatus;
import com.studenttracker.model.Opportunity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OpportunityDAO {

    long insert(Opportunity opportunity);

    void update(Opportunity opportunity);

    Optional<Opportunity> findById(long id);

    /** Matches role, company or location; a {@code null} status means any status. */
    List<Opportunity> search(String keyword, OpportunityStatus status);

    void updateStatus(long id, OpportunityStatus status);

    int countNotArchived();

    /** Open listings whose deadline falls between the two dates, inclusive, earliest first. */
    List<Opportunity> findClosingBetween(LocalDate from, LocalDate to);
}
