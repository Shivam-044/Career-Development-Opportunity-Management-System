package com.studenttracker.dao.jdbc;

import com.studenttracker.config.ConnectionFactory;
import com.studenttracker.exception.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Connection handling and small mapping helpers shared by the JDBC DAOs. */
final class JdbcUtil {

    private static final int DUPLICATE_ENTRY = 1062;

    @FunctionalInterface
    interface SqlWork<T> {
        T run(Connection connection) throws SQLException;
    }

    @FunctionalInterface
    interface SqlAction {
        void run(Connection connection) throws SQLException;
    }

    private JdbcUtil() {
    }

    static <T> T execute(String failureMessage, SqlWork<T> work) {
        try (Connection connection = ConnectionFactory.getConnection()) {
            return work.run(connection);
        } catch (SQLException ex) {
            throw new DataAccessException(failureMessage, ex);
        }
    }

    static void perform(String failureMessage, SqlAction action) {
        execute(failureMessage, connection -> {
            action.run(connection);
            return null;
        });
    }

    /** Runs the work on one connection and commits only if all of it succeeds. */
    static <T> T executeInTransaction(String failureMessage, SqlWork<T> work) {
        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                T result = work.run(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new DataAccessException(failureMessage, ex);
        }
    }

    static boolean isDuplicateEntry(SQLException ex) {
        return ex.getErrorCode() == DUPLICATE_ENTRY;
    }

    /** Builds a LIKE pattern that matches the keyword literally anywhere in the column. */
    static String like(String keyword) {
        String text = keyword == null ? "" : keyword.trim();
        String escaped = text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }

    static long generatedKey(PreparedStatement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException("The database did not return a generated id.");
            }
            return keys.getLong(1);
        }
    }

    static void setDate(PreparedStatement statement, int index, LocalDate value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DATE);
        } else {
            statement.setObject(index, value);
        }
    }

    static void setDateTime(PreparedStatement statement, int index, LocalDateTime value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.TIMESTAMP);
        } else {
            statement.setObject(index, value);
        }
    }

    static void setInteger(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    static LocalDate getDate(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, LocalDate.class);
    }

    static LocalDateTime getDateTime(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, LocalDateTime.class);
    }

    static Integer getInteger(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }
}
