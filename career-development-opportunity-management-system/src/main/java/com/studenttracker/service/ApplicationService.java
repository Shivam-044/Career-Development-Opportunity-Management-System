package com.studenttracker.service;

import com.studenttracker.dao.ApplicationDAO;
import com.studenttracker.dao.OpportunityDAO;
import com.studenttracker.dao.StudentDAO;
import com.studenttracker.enums.ApplicationStatus;
import com.studenttracker.enums.OpportunityStatus;
import com.studenttracker.exception.DuplicateApplicationException;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.JobApplication;
import com.studenttracker.model.Opportunity;
import com.studenttracker.model.Student;
import com.studenttracker.util.Dates;
import com.studenttracker.util.Validators;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ApplicationService {

    private final ApplicationDAO applicationDao;
    private final OpportunityDAO opportunityDao;
    private final StudentDAO studentDao;
    private final Clock clock;

    public ApplicationService(ApplicationDAO applicationDao, OpportunityDAO opportunityDao,
                              StudentDAO studentDao, Clock clock) {
        this.applicationDao = applicationDao;
        this.opportunityDao = opportunityDao;
        this.studentDao = studentDao;
        this.clock = clock;
    }

    public List<JobApplication> search(String keyword) {
        return search(keyword, null);
    }

    public List<JobApplication> search(String keyword, ApplicationStatus status) {
        return applicationDao.search(keyword, status);
    }

    /** Applications still in progress; interviews can only be added to these. */
    public List<JobApplication> listActive() {
        return applicationDao.search("", null).stream()
                .filter(application -> application.getStatus().isActive())
                .toList();
    }

    /**
     * Records an application for an open opportunity.
     *
     * @throws DuplicateApplicationException if the student already applied to it
     */
    public JobApplication apply(long opportunityId, LocalDate appliedOn, String notes) {
        Student student = studentDao.find().orElseThrow(() -> new ValidationException(
                "Create your profile first. Applications are linked to it."));
        Opportunity opportunity = opportunityDao.findById(opportunityId)
                .orElseThrow(() -> new ValidationException("That opportunity no longer exists."));
        LocalDate today = LocalDate.now(clock);

        if (opportunity.getStatus() != OpportunityStatus.OPEN) {
            throw new ValidationException("This listing is " + opportunity.getStatus().toString().toLowerCase()
                    + ". You can only apply to open listings.");
        }
        if (opportunity.isDeadlinePassed(today)) {
            throw new ValidationException("The deadline for this role was "
                    + Dates.display(opportunity.getDeadline()) + ", so it can't be marked as applied.");
        }
        LocalDate date = appliedOn == null ? today : appliedOn;
        if (date.isAfter(today)) {
            throw new ValidationException("The applied date can't be in the future.");
        }
        if (applicationDao.exists(student.getStudentId(), opportunityId)) {
            throw new DuplicateApplicationException(
                    "You have already applied for " + opportunity.getRoleTitle()
                            + " at " + opportunity.getCompanyName() + ".");
        }

        JobApplication application = new JobApplication(student.getStudentId(), opportunityId, date);
        application.setNotes(notes);
        application.setDisplayNames(opportunity.getCompanyName(), opportunity.getRoleTitle());
        application.setApplicationId(applicationDao.insert(application));
        return application;
    }

    /** A warning when the profile CGPA is below the opportunity's minimum; empty when nothing to flag. */
    public Optional<String> minimumCgpaWarning(long opportunityId) {
        Optional<Student> student = studentDao.find();
        Optional<Opportunity> opportunity = opportunityDao.findById(opportunityId);
        if (student.isEmpty() || opportunity.isEmpty()) {
            return Optional.empty();
        }
        var required = opportunity.get().getMinimumCgpa();
        var current = student.get().getCgpa();
        if (required == null || current == null || current.compareTo(required) >= 0) {
            return Optional.empty();
        }
        return Optional.of("This role asks for a CGPA of at least " + required.toPlainString()
                + ", and your profile shows " + current.toPlainString() + ".");
    }

    /** Moves an application along the normal workflow; skipping or reversing steps is rejected. */
    public void changeStatus(JobApplication application, ApplicationStatus target) {
        Validators.requireNonNull(target, "Status");
        ApplicationStatus current = application.getStatus();
        if (!current.canMoveTo(target)) {
            if (current.isTerminal()) {
                throw new ValidationException("An application marked " + current
                        + " is final. If it was set by mistake, use Correct status.");
            }
            String allowed = current.nextStatuses().stream()
                    .map(ApplicationStatus::toString)
                    .collect(Collectors.joining(", "));
            throw new ValidationException("An application can't go from " + current + " to " + target
                    + ". Allowed next steps: " + allowed + ".");
        }
        applicationDao.updateStatus(application.getApplicationId(), target);
        application.setStatus(target);
    }

    /** Deliberate fix for a status entered by mistake; bypasses the transition rules. */
    public void correctStatus(JobApplication application, ApplicationStatus target) {
        Validators.requireNonNull(target, "Status");
        applicationDao.updateStatus(application.getApplicationId(), target);
        application.setStatus(target);
    }

    public void updateNotes(JobApplication application, String notes) {
        application.setNotes(notes);
        applicationDao.updateNotes(application.getApplicationId(), application.getNotes());
    }
}
