package com.studenttracker.service;

import com.studenttracker.dao.ApplicationDAO;
import com.studenttracker.dao.InterviewDAO;
import com.studenttracker.enums.InterviewResult;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.Interview;
import com.studenttracker.model.JobApplication;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

public class InterviewService {

    public enum Scope {
        UPCOMING, PAST, ALL
    }

    private final InterviewDAO interviewDao;
    private final ApplicationDAO applicationDao;
    private final Clock clock;

    public InterviewService(InterviewDAO interviewDao, ApplicationDAO applicationDao, Clock clock) {
        this.interviewDao = interviewDao;
        this.applicationDao = applicationDao;
        this.clock = clock;
    }

    public List<Interview> list(Scope scope) {
        List<Interview> all = interviewDao.findAll();
        LocalDateTime now = LocalDateTime.now(clock);
        return switch (scope) {
            case ALL -> all;
            case UPCOMING -> all.stream()
                    .filter(interview -> isUpcoming(interview, now))
                    .sorted((a, b) -> a.getScheduledAt().compareTo(b.getScheduledAt()))
                    .toList();
            case PAST -> all.stream().filter(interview -> !isUpcoming(interview, now)).toList();
        };
    }

    public Interview add(Interview interview) {
        JobApplication application = applicationDao.findById(interview.getApplicationId())
                .orElseThrow(() -> new ValidationException("The application for this interview no longer exists."));
        if (!application.getStatus().isActive()) {
            throw new ValidationException("This application is " + application.getStatus()
                    + ", so new interview rounds can't be added. Correct its status first if that is wrong.");
        }
        interview.setInterviewId(interviewDao.insert(interview));
        return interview;
    }

    public void update(Interview interview) {
        interviewDao.update(interview);
    }

    public void delete(Interview interview) {
        interviewDao.delete(interview.getInterviewId());
    }

    private static boolean isUpcoming(Interview interview, LocalDateTime now) {
        return interview.getResult() != InterviewResult.CANCELLED
                && !interview.getScheduledAt().isBefore(now);
    }
}
