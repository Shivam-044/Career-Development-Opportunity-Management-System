package com.studenttracker.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Opens a new connection per call. Fine for a single-user desktop app; add a pool if that changes. */
public final class ConnectionFactory {

    private static final int LOGIN_TIMEOUT_SECONDS = 5;

    static {
        DriverManager.setLoginTimeout(LOGIN_TIMEOUT_SECONDS);
    }

    private ConnectionFactory() {
    }

    public static Connection getConnection() throws SQLException {
        AppConfig config = AppConfig.current();
        return DriverManager.getConnection(config.dbUrl(), config.dbUser(), config.dbPassword());
    }
}
