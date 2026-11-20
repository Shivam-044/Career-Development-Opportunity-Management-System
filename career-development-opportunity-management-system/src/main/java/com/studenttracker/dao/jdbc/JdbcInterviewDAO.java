package com.studenttracker.dao.jdbc;

import com.studenttracker.dao.InterviewDAO;
import com.studenttracker.enums.InterviewMode;
import com.studenttracker.enums.InterviewResult;
import com.studenttracker.model.Interview;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcInterviewDAO implements InterviewDAO {

    private static final String SELECT = "SELECT i.interview_id, i.application_id, c.company_name, "
            + "o.role_title, i.round_name, i.scheduled_at, i.interview_mode, i.result, i.feedback "
            + "FROM interviews i "
            + "JOIN applications a ON a.application_id = i.application_id "
            + "JOIN opportunities o ON o.opportunity_id = a.opportunity_id "
            + "JOIN companies c ON c.company_id = o.company_id ";

    @Override
    public long insert(Interview interview) {
        return JdbcUtil.execute("Couldn't save the interview.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO interviews (application_id, round_name, scheduled_at, interview_mode, "
                            + "result, feedback) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, interview.getApplicationId());
                statement.setString(2, interview.getRoundName());
                JdbcUtil.setDateTime(statement, 3, interview.getScheduledAt());
                statement.setString(4, interview.getMode().name());
                statement.setString(5, interview.getResult().name());
                statement.setString(6, interview.getFeedback());
                statement.executeUpdate();
                return JdbcUtil.generatedKey(statement);
            }
        });
    }

    @Override
    public void update(Interview interview) {
        JdbcUtil.perform("Couldn't save the interview.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE interviews SET round_name = ?, scheduled_at = ?, interview_mode = ?, "
                            + "result = ?, feedback = ? WHERE interview_id = ?")) {
                statement.setString(1, interview.getRoundName());
                JdbcUtil.setDateTime(statement, 2, interview.getScheduledAt());
                statement.setString(3, interview.getMode().name());
                statement.setString(4, interview.getResult().name());
                statement.setString(5, interview.getFeedback());
                statement.setLong(6, interview.getInterviewId());
                statement.executeUpdate();
            }
        });
    }

    @Override
    public void delete(long id) {
        JdbcUtil.perform("Couldn't delete the interview.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM interviews WHERE interview_id = ?")) {
                statement.setLong(1, id);
                statement.executeUpdate();
            }
        });
    }

    @Override
    public Optional<Interview> findById(long id) {
        return JdbcUtil.execute("Couldn't load the interview.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    SELECT + "WHERE i.interview_id = ?")) {
                statement.setLong(1, id);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(map(rs));
                    }
                    return Optional.<Interview>empty();
                }
            }
        });
    }

    @Override
    public List<Interview> findAll() {
        return JdbcUtil.execute("Couldn't load interviews.", connection -> {
            List<Interview> interviews = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    SELECT + "ORDER BY i.scheduled_at DESC, i.interview_id DESC");
                 ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    interviews.add(map(rs));
                }
            }
            return interviews;
        });
    }

    @Override
    public List<Interview> findUpcoming(LocalDateTime from, int limit) {
        return JdbcUtil.execute("Couldn't load upcoming interviews.", connection -> {
            List<Interview> interviews = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    SELECT + "WHERE i.scheduled_at >= ? AND i.result <> 'CANCELLED' "
                            + "ORDER BY i.scheduled_at LIMIT ?")) {
                JdbcUtil.setDateTime(statement, 1, from);
                statement.setInt(2, limit);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        interviews.add(map(rs));
                    }
                }
            }
            return interviews;
        });
    }

    @Override
    public int countUpcoming(LocalDateTime from) {
        return JdbcUtil.execute("Couldn't count upcoming interviews.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM interviews WHERE scheduled_at >= ? AND result <> 'CANCELLED'")) {
                JdbcUtil.setDateTime(statement, 1, from);
                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    return rs.getInt(1);
                }
            }
        });
    }

    private static Interview map(ResultSet rs) throws SQLException {
        Interview interview = new Interview(
                rs.getLong("application_id"),
                rs.getString("round_name"),
                JdbcUtil.getDateTime(rs, "scheduled_at"),
                InterviewMode.valueOf(rs.getString("interview_mode")));
        interview.setInterviewId(rs.getLong("interview_id"));
        interview.setDisplayNames(rs.getString("company_name"), rs.getString("role_title"));
        interview.setResult(InterviewResult.valueOf(rs.getString("result")));
        interview.setFeedback(rs.getString("feedback"));
        return interview;
    }
}
