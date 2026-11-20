-- Optional demo records, all synthetic. Dates are relative to the day you run
-- the script so the dashboard has something to show. If a profile already
-- exists, skip this file.

USE student_placement_tracker;

INSERT INTO student_profile
    (full_name, email, roll_number, degree, department, graduation_year, cgpa)
VALUES
    ('Aarav Sharma', 'aarav.sharma@example.com', '2024CSE000', 'B.Tech',
     'Computer Science and Engineering', 2028, 8.20);

SET @student_id = LAST_INSERT_ID();

INSERT INTO student_skills (student_id, skill_id, proficiency, learning_status, notes)
SELECT @student_id, s.skill_id, v.proficiency, v.learning_status, v.notes
FROM (
    SELECT 'Java' AS skill_name, 'INTERMEDIATE' AS proficiency, 'LEARNING' AS learning_status,
           'Collections and multithreading' AS notes
    UNION ALL SELECT 'SQL', 'BEGINNER', 'LEARNING', 'Joins and grouping'
    UNION ALL SELECT 'Python', 'INTERMEDIATE', 'COMPLETED', NULL
    UNION ALL SELECT 'Data Structures', 'INTERMEDIATE', 'LEARNING', 'Trees and graphs'
    UNION ALL SELECT 'Git', 'INTERMEDIATE', 'COMPLETED', NULL
) AS v
JOIN skills s ON s.skill_name = v.skill_name;

INSERT INTO companies (company_name, industry, website, location, notes) VALUES
    ('Brightwave Systems', 'Software', 'https://brightwave.example.com', 'Bengaluru', 'Referred by a senior'),
    ('Kestrel Analytics', 'Data analytics', 'https://kestrel.example.com', 'Pune', NULL),
    ('Lumen Cloud Works', 'Cloud services', 'https://lumencloud.example.com', 'Hyderabad', NULL),
    ('Orbit Payments', 'Fintech', 'https://orbitpay.example.com', 'Gurugram', NULL);

INSERT INTO opportunities
    (company_id, role_title, opportunity_type, location, work_mode, description,
     source_url, minimum_cgpa, application_deadline, listing_status)
SELECT c.company_id, v.role_title, v.opportunity_type, v.location, v.work_mode, v.description,
       v.source_url, v.minimum_cgpa, v.deadline, v.listing_status
FROM (
    SELECT 'Brightwave Systems' AS company_name, 'Java Developer Intern' AS role_title,
           'INTERNSHIP' AS opportunity_type, 'Bengaluru' AS location, 'ONSITE' AS work_mode,
           'Six-month backend internship working on internal tooling.' AS description,
           'https://brightwave.example.com/careers/java-intern' AS source_url,
           7.00 AS minimum_cgpa, DATE_ADD(CURDATE(), INTERVAL 5 DAY) AS deadline,
           'OPEN' AS listing_status
    UNION ALL SELECT 'Kestrel Analytics', 'Data Analyst Intern', 'INTERNSHIP', 'Pune', 'HYBRID',
           'Dashboards and SQL reporting for client projects.',
           'https://kestrel.example.com/jobs/analyst-intern', 7.50,
           DATE_ADD(CURDATE(), INTERVAL 12 DAY), 'OPEN'
    UNION ALL SELECT 'Lumen Cloud Works', 'Associate Software Engineer', 'FULL_TIME', 'Hyderabad', 'HYBRID',
           'Entry-level role on the platform team.',
           'https://lumencloud.example.com/careers/ase', 7.50,
           DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'OPEN'
    UNION ALL SELECT 'Orbit Payments', 'Backend Engineer Trainee', 'FULL_TIME', 'Gurugram', 'ONSITE',
           'Campus hiring drive.', NULL, NULL,
           DATE_SUB(CURDATE(), INTERVAL 3 DAY), 'CLOSED'
    UNION ALL SELECT 'Brightwave Systems', 'QA Automation Intern', 'INTERNSHIP', 'Remote', 'REMOTE',
           NULL, NULL, NULL, NULL, 'OPEN'
) AS v
JOIN companies c ON c.company_name = v.company_name;

INSERT INTO opportunity_required_skills (opportunity_id, skill_id)
SELECT o.opportunity_id, s.skill_id
FROM (
    SELECT 'Java Developer Intern' AS role_title, 'Java' AS skill_name
    UNION ALL SELECT 'Java Developer Intern', 'SQL'
    UNION ALL SELECT 'Data Analyst Intern', 'Python'
    UNION ALL SELECT 'Data Analyst Intern', 'SQL'
    UNION ALL SELECT 'Associate Software Engineer', 'Java'
    UNION ALL SELECT 'Associate Software Engineer', 'Data Structures'
    UNION ALL SELECT 'Associate Software Engineer', 'Git'
    UNION ALL SELECT 'Backend Engineer Trainee', 'Java'
) AS v
JOIN opportunities o ON o.role_title = v.role_title
JOIN skills s ON s.skill_name = v.skill_name;

INSERT INTO applications (student_id, opportunity_id, status, applied_on, notes)
SELECT @student_id, o.opportunity_id, v.status, v.applied_on, v.notes
FROM (
    SELECT 'Java Developer Intern' AS role_title, 'INTERVIEW' AS status,
           DATE_SUB(CURDATE(), INTERVAL 9 DAY) AS applied_on,
           'Applied through the careers page' AS notes
    UNION ALL SELECT 'Data Analyst Intern', 'APPLIED', DATE_SUB(CURDATE(), INTERVAL 4 DAY), NULL
    UNION ALL SELECT 'Backend Engineer Trainee', 'REJECTED', DATE_SUB(CURDATE(), INTERVAL 20 DAY),
           'Did not clear the aptitude round'
) AS v
JOIN opportunities o ON o.role_title = v.role_title;

INSERT INTO interviews (application_id, round_name, scheduled_at, interview_mode, result, feedback)
SELECT a.application_id, v.round_name, v.scheduled_at, v.interview_mode, v.result, v.feedback
FROM (
    SELECT 'Java Developer Intern' AS role_title, 'Online assessment review' AS round_name,
           TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '11:00:00') AS scheduled_at,
           'ONLINE' AS interview_mode, 'PASSED' AS result,
           'Good problem solving, revise collections' AS feedback
    UNION ALL SELECT 'Java Developer Intern', 'Technical round 1',
           TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '15:00:00'), 'ONLINE', 'PENDING', NULL
    UNION ALL SELECT 'Java Developer Intern', 'HR round',
           TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '10:30:00'), 'PHONE', 'PENDING', NULL
) AS v
JOIN opportunities o ON o.role_title = v.role_title
JOIN applications a ON a.opportunity_id = o.opportunity_id;
