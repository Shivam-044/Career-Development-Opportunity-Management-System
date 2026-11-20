package com.studenttracker.dao.jdbc;

import com.studenttracker.dao.OpportunityDAO;
import com.studenttracker.enums.OpportunityStatus;
import com.studenttracker.enums.OpportunityType;
import com.studenttracker.enums.WorkMode;
import com.studenttracker.model.Opportunity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JdbcOpportunityDAO implements OpportunityDAO {

    private static final String SELECT = "SELECT o.opportunity_id, o.company_id, c.company_name, "
            + "o.role_title, o.opportunity_type, o.location, o.work_mode, o.description, o.source_url, "
            + "o.minimum_cgpa, o.application_deadline, o.listing_status "
            + "FROM opportunities o JOIN companies c ON c.company_id = o.company_id ";

    private static final String NEAREST_DEADLINE_FIRST =
            "ORDER BY (o.application_deadline IS NULL), o.application_deadline, o.opportunity_id DESC";

    @Override
    public long insert(Opportunity opportunity) {
        return JdbcUtil.executeInTransaction("Couldn't save the opportunity.", connection -> {
            long id;
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO opportunities (company_id, role_title, opportunity_type, location, work_mode, "
                            + "description, source_url, minimum_cgpa, application_deadline, listing_status) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                bind(statement, opportunity);
                statement.executeUpdate();
                id = JdbcUtil.generatedKey(statement);
            }
            replaceRequiredSkills(connection, id, opportunity.getRequiredSkills());
            return id;
        });
    }

    @Override
    public void update(Opportunity opportunity) {
        JdbcUtil.executeInTransaction("Couldn't save the opportunity.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE opportunities SET company_id = ?, role_title = ?, opportunity_type = ?, "
                            + "location = ?, work_mode = ?, description = ?, source_url = ?, "
                            + "minimum_cgpa = ?, application_deadline = ?, listing_status = ? "
                            + "WHERE opportunity_id = ?")) {
                bind(statement, opportunity);
                statement.setLong(11, opportunity.getOpportunityId());
                statement.executeUpdate();
            }
            replaceRequiredSkills(connection, opportunity.getOpportunityId(), opportunity.getRequiredSkills());
            return null;
        });
    }

    @Override
    public Optional<Opportunity> findById(long id) {
        return JdbcUtil.execute("Couldn't load the opportunity.", connection -> {
            List<Opportunity> found = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    SELECT + "WHERE o.opportunity_id = ?")) {
                statement.setLong(1, id);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        found.add(map(rs));
                    }
                }
            }
            attachRequiredSkills(connection, found);
            return found.stream().findFirst();
        });
    }

    @Override
    public List<Opportunity> search(String keyword, OpportunityStatus status) {
        String sql = SELECT + "WHERE (o.role_title LIKE ? OR c.company_name LIKE ? OR o.location LIKE ?) "
                + "AND (? IS NULL OR o.listing_status = ?) " + NEAREST_DEADLINE_FIRST;
        return JdbcUtil.execute("Couldn't load opportunities.", connection -> {
            List<Opportunity> opportunities = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                String pattern = JdbcUtil.like(keyword);
                String statusName = status == null ? null : status.name();
                statement.setString(1, pattern);
                statement.setString(2, pattern);
                statement.setString(3, pattern);
                statement.setString(4, statusName);
                statement.setString(5, statusName);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        opportunities.add(map(rs));
                    }
                }
            }
            attachRequiredSkills(connection, opportunities);
            return opportunities;
        });
    }

    @Override
    public void updateStatus(long id, OpportunityStatus status) {
        JdbcUtil.perform("Couldn't update the listing status.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE opportunities SET listing_status = ? WHERE opportunity_id = ?")) {
                statement.setString(1, status.name());
                statement.setLong(2, id);
                statement.executeUpdate();
            }
        });
    }

    @Override
    public int countNotArchived() {
        return JdbcUtil.execute("Couldn't count opportunities.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM opportunities WHERE listing_status <> 'ARCHIVED'");
                 ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        });
    }

    @Override
    public List<Opportunity> findClosingBetween(LocalDate from, LocalDate to) {
        String sql = SELECT + "WHERE o.listing_status = 'OPEN' AND o.application_deadline BETWEEN ? AND ? "
                + "ORDER BY o.application_deadline, o.opportunity_id";
        return JdbcUtil.execute("Couldn't load upcoming deadlines.", connection -> {
            List<Opportunity> opportunities = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                JdbcUtil.setDate(statement, 1, from);
                JdbcUtil.setDate(statement, 2, to);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        opportunities.add(map(rs));
                    }
                }
            }
            attachRequiredSkills(connection, opportunities);
            return opportunities;
        });
    }

    private static void bind(PreparedStatement statement, Opportunity opportunity) throws SQLException {
        statement.setLong(1, opportunity.getCompanyId());
        statement.setString(2, opportunity.getRoleTitle());
        statement.setString(3, opportunity.getType().name());
        statement.setString(4, opportunity.getLocation());
        statement.setString(5, opportunity.getWorkMode() == null ? null : opportunity.getWorkMode().name());
        statement.setString(6, opportunity.getDescription());
        statement.setString(7, opportunity.getSourceUrl());
        statement.setBigDecimal(8, opportunity.getMinimumCgpa());
        JdbcUtil.setDate(statement, 9, opportunity.getDeadline());
        statement.setString(10, opportunity.getStatus().name());
    }

    private static void replaceRequiredSkills(Connection connection, long opportunityId, List<String> skills)
            throws SQLException {
        try (PreparedStatement delete = connection.prepareStatement(
                "DELETE FROM opportunity_required_skills WHERE opportunity_id = ?")) {
            delete.setLong(1, opportunityId);
            delete.executeUpdate();
        }
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO opportunity_required_skills (opportunity_id, skill_id) VALUES (?, ?)")) {
            for (String skill : skills) {
                insert.setLong(1, opportunityId);
                insert.setLong(2, SkillCatalog.findOrCreate(connection, skill));
                insert.executeUpdate();
            }
        }
    }

    private static void attachRequiredSkills(Connection connection, List<Opportunity> opportunities)
            throws SQLException {
        if (opportunities.isEmpty()) {
            return;
        }
        Map<Long, List<String>> byOpportunity = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT r.opportunity_id, s.skill_name FROM opportunity_required_skills r "
                        + "JOIN skills s ON s.skill_id = r.skill_id ORDER BY s.skill_name");
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                byOpportunity.computeIfAbsent(rs.getLong("opportunity_id"), key -> new ArrayList<>())
                        .add(rs.getString("skill_name"));
            }
        }
        for (Opportunity opportunity : opportunities) {
            opportunity.setRequiredSkills(byOpportunity.getOrDefault(opportunity.getOpportunityId(), List.of()));
        }
    }

    private static Opportunity map(ResultSet rs) throws SQLException {
        Opportunity opportunity = new Opportunity(
                rs.getLong("company_id"),
                rs.getString("role_title"),
                OpportunityType.valueOf(rs.getString("opportunity_type")));
        opportunity.setOpportunityId(rs.getLong("opportunity_id"));
        opportunity.setCompanyName(rs.getString("company_name"));
        opportunity.setLocation(rs.getString("location"));
        String workMode = rs.getString("work_mode");
        opportunity.setWorkMode(workMode == null ? null : WorkMode.valueOf(workMode));
        opportunity.setDescription(rs.getString("description"));
        opportunity.setSourceUrl(rs.getString("source_url"));
        opportunity.setMinimumCgpa(rs.getBigDecimal("minimum_cgpa"));
        opportunity.setDeadline(JdbcUtil.getDate(rs, "application_deadline"));
        opportunity.setStatus(OpportunityStatus.valueOf(rs.getString("listing_status")));
        return opportunity;
    }
}
