package com.studenttracker.dao.jdbc;

import com.studenttracker.dao.ApplicationDAO;
import com.studenttracker.enums.ApplicationStatus;
import com.studenttracker.exception.DuplicateApplicationException;
import com.studenttracker.model.JobApplication;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JdbcApplicationDAO implements ApplicationDAO {

    private static final String SELECT = "SELECT a.application_id, a.student_id, a.opportunity_id, "
            + "c.company_name, o.role_title, a.status, a.applied_on, a.notes "
            + "FROM applications a "
            + "JOIN opportunities o ON o.opportunity_id = a.opportunity_id "
            + "JOIN companies c ON c.company_id = o.company_id ";

    @Override
    public long insert(JobApplication application) {
        return JdbcUtil.execute("Couldn't save the application.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO applications (student_id, opportunity_id, status, applied_on, notes) "
                            + "VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, application.getStudentId());
                statement.setLong(2, application.getOpportunityId());
                statement.setString(3, application.getStatus().name());
                JdbcUtil.setDate(statement, 4, application.getAppliedOn());
                statement.setString(5, application.getNotes());
                statement.executeUpdate();
                return JdbcUtil.generatedKey(statement);
            } catch (SQLException ex) {
                if (JdbcUtil.isDuplicateEntry(ex)) {
                    throw new DuplicateApplicationException(
                            "You have already applied to this opportunity.", ex);
                }
                throw ex;
            }
        });
    }

    @Override
    public Optional<JobApplication> findById(long id) {
        return JdbcUtil.execute("Couldn't load the application.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    SELECT + "WHERE a.application_id = ?")) {
                statement.setLong(1, id);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(map(rs));
                    }
                    return Optional.<JobApplication>empty();
                }
            }
        });
    }

    @Override
    public boolean exists(long studentId, long opportunityId) {
        return JdbcUtil.execute("Couldn't check existing applications.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT 1 FROM applications WHERE student_id = ? AND opportunity_id = ?")) {
                statement.setLong(1, studentId);
                statement.setLong(2, opportunityId);
                try (ResultSet rs = statement.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    @Override
    public List<JobApplication> search(String keyword, ApplicationStatus status) {
        String sql = SELECT + "WHERE (c.company_name LIKE ? OR o.role_title LIKE ?) "
                + "AND (? IS NULL OR a.status = ?) ORDER BY a.applied_on DESC, a.application_id DESC";
        return JdbcUtil.execute("Couldn't load applications.", connection -> {
            List<JobApplication> applications = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                String pattern = JdbcUtil.like(keyword);
                String statusName = status == null ? null : status.name();
                statement.setString(1, pattern);
                statement.setString(2, pattern);
                statement.setString(3, statusName);
                statement.setString(4, statusName);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        applications.add(map(rs));
                    }
                }
            }
            return applications;
        });
    }

    @Override
    public void updateStatus(long id, ApplicationStatus status) {
        JdbcUtil.perform("Couldn't update the application status.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE applications SET status = ? WHERE application_id = ?")) {
                statement.setString(1, status.name());
                statement.setLong(2, id);
                statement.executeUpdate();
            }
        });
    }

    @Override
    public void updateNotes(long id, String notes) {
        JdbcUtil.perform("Couldn't save the notes.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE applications SET notes = ? WHERE application_id = ?")) {
                statement.setString(1, notes);
                statement.setLong(2, id);
                statement.executeUpdate();
            }
        });
    }

    @Override
    public Map<ApplicationStatus, Integer> countByStatus() {
        return JdbcUtil.execute("Couldn't count applications.", connection -> {
            Map<ApplicationStatus, Integer> counts = new EnumMap<>(ApplicationStatus.class);
            for (ApplicationStatus status : ApplicationStatus.values()) {
                counts.put(status, 0);
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT status, COUNT(*) AS total FROM applications GROUP BY status");
                 ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    counts.put(ApplicationStatus.valueOf(rs.getString("status")), rs.getInt("total"));
                }
            }
            return counts;
        });
    }

    private static JobApplication map(ResultSet rs) throws SQLException {
        JobApplication application = new JobApplication(
                rs.getLong("student_id"),
                rs.getLong("opportunity_id"),
                JdbcUtil.getDate(rs, "applied_on"));
        application.setApplicationId(rs.getLong("application_id"));
        application.setDisplayNames(rs.getString("company_name"), rs.getString("role_title"));
        application.setStatus(ApplicationStatus.valueOf(rs.getString("status")));
        application.setNotes(rs.getString("notes"));
        return application;
    }
}
