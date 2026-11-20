package com.studenttracker.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.studenttracker.exception.DataAccessException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AppConfigTest {

    private static final Path NO_FILE = Path.of("does-not-exist.properties");

    @Test
    void readsSettingsFromEnvironmentVariables() {
        AppConfig config = AppConfig.load(
                Map.of("DB_URL", "jdbc:mysql://db:3306/x", "DB_USER", "app", "DB_PASSWORD", "secret"), NO_FILE);

        assertEquals("jdbc:mysql://db:3306/x", config.dbUrl());
        assertEquals("app", config.dbUser());
        assertEquals("secret", config.dbPassword());
    }

    @Test
    void fallsBackToTheDefaultLocalUrl() {
        AppConfig config = AppConfig.load(Map.of("DB_USER", "app", "DB_PASSWORD", "secret"), NO_FILE);

        assertEquals("jdbc:mysql://localhost:3306/student_placement_tracker", config.dbUrl());
    }

    @Test
    void environmentVariablesBeatTheFile() throws IOException {
        Path file = Files.createTempFile("tracker", ".properties");
        try {
            Files.writeString(file, "db.user=fileUser\ndb.password=filePass\ndb.url=jdbc:mysql://file/db\n");

            AppConfig config = AppConfig.load(Map.of("DB_USER", "envUser"), file);

            assertEquals("envUser", config.dbUser());
            assertEquals("filePass", config.dbPassword());
            assertEquals("jdbc:mysql://file/db", config.dbUrl());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void failsClearlyWhenCredentialsAreMissing() {
        DataAccessException failure = assertThrows(DataAccessException.class,
                () -> AppConfig.load(Map.of("DB_USER", "app"), NO_FILE));

        assertTrue(failure.getMessage().contains("DB_PASSWORD"));
    }
}
