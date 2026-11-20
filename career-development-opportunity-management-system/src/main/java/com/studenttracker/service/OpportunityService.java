package com.studenttracker.service;

import com.studenttracker.dao.CompanyDAO;
import com.studenttracker.dao.OpportunityDAO;
import com.studenttracker.enums.OpportunityStatus;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.Company;
import com.studenttracker.model.Opportunity;
import com.studenttracker.util.Dates;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class OpportunityService {

    private final OpportunityDAO opportunityDao;
    private final CompanyDAO companyDao;
    private final Clock clock;

    public OpportunityService(OpportunityDAO opportunityDao, CompanyDAO companyDao, Clock clock) {
        this.opportunityDao = opportunityDao;
        this.companyDao = companyDao;
        this.clock = clock;
    }

    public List<Opportunity> search(String keyword) {
        return search(keyword, null);
    }

    public List<Opportunity> search(String keyword, OpportunityStatus status) {
        return opportunityDao.search(keyword, status);
    }

    public Optional<Opportunity> findById(long id) {
        return opportunityDao.findById(id);
    }

    /** True when the listing is still open although its deadline is already behind us. */
    public boolean hasPassedDeadline(Opportunity opportunity) {
        return opportunity.getStatus() == OpportunityStatus.OPEN
                && opportunity.isDeadlinePassed(LocalDate.now(clock));
    }

    /**
     * Saves the opportunity. An open listing with a past deadline is rejected unless the
     * caller passes {@code keepPastDeadlineOpen}, for people who track old listings on purpose.
     */
    public Opportunity save(Opportunity opportunity, boolean keepPastDeadlineOpen) {
        Company company = companyDao.findById(opportunity.getCompanyId())
                .orElseThrow(() -> new ValidationException("Choose a company for this opportunity."));
        if (company.isArchived() && opportunity.getOpportunityId() == null) {
            throw new ValidationException(company.getCompanyName()
                    + " is archived. Restore the company before adding opportunities for it.");
        }
        if (hasPassedDeadline(opportunity) && !keepPastDeadlineOpen) {
            throw new ValidationException("The deadline " + Dates.display(opportunity.getDeadline())
                    + " has passed. Mark the listing as closed, or change the deadline.");
        }
        if (opportunity.getOpportunityId() == null) {
            opportunity.setOpportunityId(opportunityDao.insert(opportunity));
        } else {
            opportunityDao.update(opportunity);
        }
        return opportunity;
    }

    public void changeStatus(Opportunity opportunity, OpportunityStatus status) {
        if (status == OpportunityStatus.OPEN && opportunity.isDeadlinePassed(LocalDate.now(clock))) {
            throw new ValidationException("The deadline " + Dates.display(opportunity.getDeadline())
                    + " has passed, so the listing can't be reopened. Edit it and set a new deadline first.");
        }
        opportunityDao.updateStatus(opportunity.getOpportunityId(), status);
        opportunity.setStatus(status);
    }
}
