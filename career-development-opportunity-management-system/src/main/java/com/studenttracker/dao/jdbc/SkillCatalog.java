package com.studenttracker.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Looks up skill names in the shared catalogue, adding them when they are new. */
final class SkillCatalog {

    private SkillCatalog() {
    }

    static long findOrCreate(Connection connection, String skillName) throws SQLException {
        try (PreparedStatement select = connection.prepareStatement(
                "SELECT skill_id FROM skills WHERE skill_name = ?")) {
            select.setString(1, skillName);
            try (ResultSet rs = select.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO skills (skill_name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, skillName);
            insert.executeUpdate();
            return JdbcUtil.generatedKey(insert);
        }
    }
}
