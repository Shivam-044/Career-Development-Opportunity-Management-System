-- Creates the account the application connects with. It can read and write
-- this one database and nothing else. Run it once as a MySQL administrator,
-- after schema.sql, and replace CHANGE_ME with your own password first.

CREATE USER IF NOT EXISTS 'tracker_app'@'localhost' IDENTIFIED BY 'CHANGE_ME';

GRANT SELECT, INSERT, UPDATE, DELETE
    ON student_placement_tracker.*
    TO 'tracker_app'@'localhost';
