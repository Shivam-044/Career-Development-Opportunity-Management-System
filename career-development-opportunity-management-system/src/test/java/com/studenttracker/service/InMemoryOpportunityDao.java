package com.studenttracker.service;

import com.studenttracker.dao.OpportunityDAO;
import com.studenttracker.enums.OpportunityStatus;
import com.studenttracker.model.Opportunity;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class InMemoryOpportunityDao implements OpportunityDAO {

    private final Map<Long, Opportunity> opportunities = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public long insert(Opportunity opportunity) {
        long id = nextId++;
        opportunity.setOpportunityId(id);
        opportunities.put(id, opportunity);
        return id;
    }

    @Override
    public void update(Opportunity opportunity) {
        opportunities.put(opportunity.getOpportunityId(), opportunity);
    }

    @Override
    public Optional<Opportunity> findById(long id) {
        return Optional.ofNullable(opportunities.get(id));
    }

    @Override
    public List<Opportunity> search(String keyword, OpportunityStatus status) {
        return opportunities.values().stream()
                .filter(opportunity -> status == null || opportunity.getStatus() == status)
                .toList();
    }

    @Override
    public void updateStatus(long id, OpportunityStatus status) {
        opportunities.get(id).setStatus(status);
    }

    @Override
    public int countNotArchived() {
        return (int) opportunities.values().stream()
                .filter(opportunity -> opportunity.getStatus() != OpportunityStatus.ARCHIVED)
                .count();
    }

    @Override
    public List<Opportunity> findClosingBetween(LocalDate from, LocalDate to) {
        return opportunities.values().stream()
                .filter(opportunity -> opportunity.getStatus() == OpportunityStatus.OPEN)
                .filter(opportunity -> opportunity.getDeadline() != null
                        && !opportunity.getDeadline().isBefore(from) && !opportunity.getDeadline().isAfter(to))
                .toList();
    }
}
