package com.studenttracker.config;

import com.studenttracker.exception.DataAccessException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * Database settings. Environment variables (DB_URL, DB_USER, DB_PASSWORD) win over
 * the optional properties file. The file is config/application.properties next to the
 * project, or ~/.careerforge/application.properties for installed copies, and can be
 * relocated with the TRACKER_CONFIG environment variable.
 */
public final class AppConfig {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/student_placement_tracker";
    private static final Path DEFAULT_FILE = Path.of("config", "application.properties");
    private static final Path USER_FILE =
            Path.of(System.getProperty("user.home", "."), ".careerforge", "application.properties");

    private static AppConfig current;

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    private AppConfig(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    public static synchronized AppConfig current() {
        if (current == null) {
            String override = System.getenv("TRACKER_CONFIG");
            Path file;
            if (override != null && !override.isBlank()) {
                file = Path.of(override);
            } else if (Files.isRegularFile(DEFAULT_FILE) || !Files.isRegularFile(USER_FILE)) {
                file = DEFAULT_FILE;
            } else {
                file = USER_FILE;
            }
            current = load(System.getenv(), file);
        }
        return current;
    }

    static AppConfig load(Map<String, String> env, Path file) {
        Properties fileValues = readFile(file);
        String url = pick(env.get("DB_URL"), fileValues.getProperty("db.url"));
        String user = pick(env.get("DB_USER"), fileValues.getProperty("db.user"));
        String password = pick(env.get("DB_PASSWORD"), fileValues.getProperty("db.password"));
        if (user == null || password == null) {
            throw new DataAccessException("Database settings are missing. Set DB_USER and DB_PASSWORD "
                    + "(and DB_URL if MySQL is not on localhost:3306), or fill in config/application.properties.");
        }
        return new AppConfig(url == null ? DEFAULT_URL : url, user, password);
    }

    private static Properties readFile(Path file) {
        Properties properties = new Properties();
        if (Files.isRegularFile(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                properties.load(in);
            } catch (IOException ex) {
                throw new DataAccessException("Couldn't read the settings file " + file + ".", ex);
            }
        }
        return properties;
    }

    private static String pick(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        if (second != null && !second.isBlank()) {
            return second.trim();
        }
        return null;
    }

    public String dbUrl() {
        return dbUrl;
    }

    public String dbUser() {
        return dbUser;
    }

    public String dbPassword() {
        return dbPassword;
    }
}
