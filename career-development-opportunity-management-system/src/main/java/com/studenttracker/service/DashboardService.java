package com.studenttracker.service;

import com.studenttracker.dao.ApplicationDAO;
import com.studenttracker.dao.InterviewDAO;
import com.studenttracker.dao.OpportunityDAO;
import com.studenttracker.dao.SkillDAO;
import com.studenttracker.dao.StudentDAO;
import com.studenttracker.enums.ApplicationStatus;
import com.studenttracker.enums.SkillStatus;
import com.studenttracker.model.DashboardSummary;
import com.studenttracker.model.Interview;
import com.studenttracker.model.Opportunity;
import com.studenttracker.model.Student;
import com.studenttracker.model.StudentSkill;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DashboardService {

    private static final int DEADLINE_WINDOW_DAYS = 7;
    private static final int UPCOMING_INTERVIEW_LIMIT = 8;

    private final StudentDAO studentDao;
    private final SkillDAO skillDao;
    private final OpportunityDAO opportunityDao;
    private final ApplicationDAO applicationDao;
    private final InterviewDAO interviewDao;
    private final Clock clock;

    public DashboardService(StudentDAO studentDao, SkillDAO skillDao, OpportunityDAO opportunityDao,
                            ApplicationDAO applicationDao, InterviewDAO interviewDao, Clock clock) {
        this.studentDao = studentDao;
        this.skillDao = skillDao;
        this.opportunityDao = opportunityDao;
        this.applicationDao = applicationDao;
        this.interviewDao = interviewDao;
        this.clock = clock;
    }

    public DashboardSummary summary() {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate today = now.toLocalDate();

        Map<ApplicationStatus, Integer> byStatus = applicationDao.countByStatus();
        int active = byStatus.entrySet().stream()
                .filter(entry -> entry.getKey().isActive())
                .mapToInt(Map.Entry::getValue)
                .sum();

        List<Interview> upcoming = interviewDao.findUpcoming(now, UPCOMING_INTERVIEW_LIMIT);
        List<Opportunity> deadlines = opportunityDao.findClosingBetween(today, today.plusDays(DEADLINE_WINDOW_DAYS));

        Optional<Student> student = studentDao.find();
        List<StudentSkill> skills = student.isPresent()
                ? skillDao.findByStudent(student.get().getStudentId())
                : List.of();

        return new DashboardSummary(
                opportunityDao.countNotArchived(),
                active,
                interviewDao.countUpcoming(now),
                byStatus.getOrDefault(ApplicationStatus.OFFER, 0),
                countSkills(skills, SkillStatus.LEARNING),
                countSkills(skills, SkillStatus.COMPLETED),
                deadlines.size(),
                byStatus,
                upcoming,
                deadlines);
    }

    private static int countSkills(List<StudentSkill> skills, SkillStatus status) {
        return (int) skills.stream().filter(skill -> skill.getLearningStatus() == status).count();
    }
}
