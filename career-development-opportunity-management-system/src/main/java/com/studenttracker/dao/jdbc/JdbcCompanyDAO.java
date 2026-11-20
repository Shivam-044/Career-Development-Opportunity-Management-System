package com.studenttracker.dao.jdbc;

import com.studenttracker.dao.CompanyDAO;
import com.studenttracker.model.Company;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCompanyDAO implements CompanyDAO {

    private static final String COLUMNS =
            "company_id, company_name, industry, website, location, notes, is_archived";

    @Override
    public long insert(Company company) {
        return JdbcUtil.execute("Couldn't save the company.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO companies (company_name, industry, website, location, notes, is_archived) "
                            + "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                bind(statement, company);
                statement.executeUpdate();
                return JdbcUtil.generatedKey(statement);
            }
        });
    }

    @Override
    public void update(Company company) {
        JdbcUtil.perform("Couldn't save the company.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE companies SET company_name = ?, industry = ?, website = ?, location = ?, "
                            + "notes = ?, is_archived = ? WHERE company_id = ?")) {
                bind(statement, company);
                statement.setLong(7, company.getCompanyId());
                statement.executeUpdate();
            }
        });
    }

    @Override
    public Optional<Company> findById(long id) {
        return JdbcUtil.execute("Couldn't load the company.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT " + COLUMNS + " FROM companies WHERE company_id = ?")) {
                statement.setLong(1, id);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(map(rs));
                    }
                    return Optional.<Company>empty();
                }
            }
        });
    }

    @Override
    public List<Company> search(String keyword, boolean includeArchived) {
        String sql = "SELECT " + COLUMNS + " FROM companies "
                + "WHERE (company_name LIKE ? OR industry LIKE ? OR location LIKE ?) "
                + "AND (is_archived = 0 OR ?) ORDER BY company_name";
        return JdbcUtil.execute("Couldn't load companies.", connection -> {
            List<Company> companies = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                String pattern = JdbcUtil.like(keyword);
                statement.setString(1, pattern);
                statement.setString(2, pattern);
                statement.setString(3, pattern);
                statement.setBoolean(4, includeArchived);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        companies.add(map(rs));
                    }
                }
            }
            return companies;
        });
    }

    @Override
    public boolean existsByName(String name, Long excludeId) {
        return JdbcUtil.execute("Couldn't check company names.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT 1 FROM companies WHERE company_name = ? AND company_id <> ?")) {
                statement.setString(1, name);
                statement.setLong(2, excludeId == null ? -1L : excludeId);
                try (ResultSet rs = statement.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    @Override
    public void setArchived(long id, boolean archived) {
        JdbcUtil.perform("Couldn't update the company.", connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE companies SET is_archived = ? WHERE company_id = ?")) {
                statement.setBoolean(1, archived);
                statement.setLong(2, id);
                statement.executeUpdate();
            }
        });
    }

    private static void bind(PreparedStatement statement, Company company) throws SQLException {
        statement.setString(1, company.getCompanyName());
        statement.setString(2, company.getIndustry());
        statement.setString(3, company.getWebsite());
        statement.setString(4, company.getLocation());
        statement.setString(5, company.getNotes());
        statement.setBoolean(6, company.isArchived());
    }

    private static Company map(ResultSet rs) throws SQLException {
        Company company = new Company(rs.getString("company_name"));
        company.setCompanyId(rs.getLong("company_id"));
        company.setIndustry(rs.getString("industry"));
        company.setWebsite(rs.getString("website"));
        company.setLocation(rs.getString("location"));
        company.setNotes(rs.getString("notes"));
        company.setArchived(rs.getBoolean("is_archived"));
        return company;
    }
}
