# Planned features (not implemented)

These need new tables, so they are written down as designs instead of being added casually to the schema.

## Custom reminders

The bell already shows reminders derived from interviews and deadlines. A user-defined reminder needs persistence:

```sql
CREATE TABLE reminders (
    reminder_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id     BIGINT NOT NULL,
    title          VARCHAR(150) NOT NULL,
    remind_at      DATETIME NOT NULL,
    lead_minutes   INT NOT NULL DEFAULT 30,
    opportunity_id BIGINT NULL,
    interview_id   BIGINT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    snoozed_until  DATETIME NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES student_profile (student_id) ON DELETE CASCADE,
    FOREIGN KEY (opportunity_id) REFERENCES opportunities (opportunity_id) ON DELETE SET NULL,
    FOREIGN KEY (interview_id) REFERENCES interviews (interview_id) ON DELETE SET NULL,
    CHECK (status IN ('PENDING', 'DISMISSED', 'DONE'))
);
```

Java side: a `Reminder` model with a `ReminderStatus` enum, `ReminderDAO` and `JdbcReminderDAO`, a `ReminderService`
(validation, snooze, dismiss, complete, "due now" query) and a `ReminderDialog`. `NotificationCenter` would merge due
reminders with the derived ones. Reminders can only fire while the application is open; running in the background would
need a separate service or OS scheduling.

Ship it as `database/migrations/002-reminders.sql`, and never edit `schema.sql` in place for an existing database.

## Login

The application is single-user and has no login on purpose. Adding one is a separate change:

- `users` table (id, email unique, password hash, salt, iterations, created_at) and an owner column on the student profile.
- Password hashing with PBKDF2WithHmacSHA256 (at least 210,000 iterations, random 16-byte salt, constant-time compare).
  No plain-text passwords anywhere, including logs.
- `AuthenticationService` (register, login, change password) and a `Session` object holding the current user id.
- Login and registration screens that sit in front of the main shell.
- Services take the student id from the session instead of "the one profile", and the single-profile rule becomes
  one profile per user. This touches `StudentService` and the DAOs, so it needs its own migration and tests.
