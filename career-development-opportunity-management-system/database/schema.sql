-- CareerForge: database schema (MySQL 8.0.16 or newer).
-- Safe to run more than once. Run it as a MySQL administrator, for example
-- the root account in MySQL Workbench.

CREATE DATABASE IF NOT EXISTS student_placement_tracker
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE student_placement_tracker;

CREATE TABLE IF NOT EXISTS student_profile (
    student_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(150),
    roll_number     VARCHAR(50),
    degree          VARCHAR(100),
    department      VARCHAR(100),
    graduation_year INT,
    cgpa            DECIMAL(4,2),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_profile_cgpa CHECK (cgpa IS NULL OR (cgpa >= 0 AND cgpa <= 10))
);

CREATE TABLE IF NOT EXISTS skills (
    skill_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    skill_name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS student_skills (
    student_id      BIGINT NOT NULL,
    skill_id        BIGINT NOT NULL,
    proficiency     VARCHAR(20) NOT NULL,
    learning_status VARCHAR(20) NOT NULL,
    notes           VARCHAR(500),
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (student_id, skill_id),
    CONSTRAINT fk_student_skills_student FOREIGN KEY (student_id)
        REFERENCES student_profile (student_id) ON DELETE CASCADE,
    CONSTRAINT fk_student_skills_skill FOREIGN KEY (skill_id)
        REFERENCES skills (skill_id),
    CONSTRAINT chk_student_skills_proficiency
        CHECK (proficiency IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    CONSTRAINT chk_student_skills_status
        CHECK (learning_status IN ('LEARNING', 'COMPLETED'))
);

CREATE TABLE IF NOT EXISTS companies (
    company_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_name VARCHAR(150) NOT NULL,
    industry     VARCHAR(100),
    website      VARCHAR(255),
    location     VARCHAR(150),
    notes        TEXT,
    is_archived  TINYINT(1) NOT NULL DEFAULT 0,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_companies_name UNIQUE (company_name)
);

CREATE TABLE IF NOT EXISTS opportunities (
    opportunity_id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id           BIGINT NOT NULL,
    role_title           VARCHAR(150) NOT NULL,
    opportunity_type     VARCHAR(20) NOT NULL,
    location             VARCHAR(150),
    work_mode            VARCHAR(20),
    description          TEXT,
    source_url           VARCHAR(500),
    minimum_cgpa         DECIMAL(4,2),
    application_deadline DATE,
    listing_status       VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_opportunities_company FOREIGN KEY (company_id)
        REFERENCES companies (company_id),
    CONSTRAINT chk_opportunities_type
        CHECK (opportunity_type IN ('INTERNSHIP', 'FULL_TIME')),
    CONSTRAINT chk_opportunities_work_mode
        CHECK (work_mode IS NULL OR work_mode IN ('ONSITE', 'HYBRID', 'REMOTE')),
    CONSTRAINT chk_opportunities_status
        CHECK (listing_status IN ('OPEN', 'CLOSED', 'ARCHIVED')),
    CONSTRAINT chk_opportunities_cgpa
        CHECK (minimum_cgpa IS NULL OR (minimum_cgpa >= 0 AND minimum_cgpa <= 10)),
    INDEX idx_opportunities_status_deadline (listing_status, application_deadline),
    INDEX idx_opportunities_company (company_id)
);

CREATE TABLE IF NOT EXISTS opportunity_required_skills (
    opportunity_id BIGINT NOT NULL,
    skill_id       BIGINT NOT NULL,
    PRIMARY KEY (opportunity_id, skill_id),
    CONSTRAINT fk_required_skills_opportunity FOREIGN KEY (opportunity_id)
        REFERENCES opportunities (opportunity_id) ON DELETE CASCADE,
    CONSTRAINT fk_required_skills_skill FOREIGN KEY (skill_id)
        REFERENCES skills (skill_id)
);

CREATE TABLE IF NOT EXISTS applications (
    application_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id     BIGINT NOT NULL,
    opportunity_id BIGINT NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'APPLIED',
    applied_on     DATE NOT NULL,
    notes          TEXT,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_applications_student FOREIGN KEY (student_id)
        REFERENCES student_profile (student_id),
    CONSTRAINT fk_applications_opportunity FOREIGN KEY (opportunity_id)
        REFERENCES opportunities (opportunity_id),
    CONSTRAINT uq_applications_student_opportunity UNIQUE (student_id, opportunity_id),
    CONSTRAINT chk_applications_status
        CHECK (status IN ('APPLIED', 'ASSESSMENT', 'INTERVIEW', 'OFFER', 'REJECTED', 'WITHDRAWN')),
    INDEX idx_applications_status (status)
);

CREATE TABLE IF NOT EXISTS interviews (
    interview_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    application_id BIGINT NOT NULL,
    round_name     VARCHAR(100) NOT NULL,
    scheduled_at   DATETIME NOT NULL,
    interview_mode VARCHAR(20) NOT NULL,
    result         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    feedback       TEXT,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_interviews_application FOREIGN KEY (application_id)
        REFERENCES applications (application_id) ON DELETE CASCADE,
    CONSTRAINT chk_interviews_mode
        CHECK (interview_mode IN ('ONLINE', 'OFFLINE', 'PHONE')),
    CONSTRAINT chk_interviews_result
        CHECK (result IN ('PENDING', 'PASSED', 'FAILED', 'CANCELLED')),
    INDEX idx_interviews_application (application_id),
    INDEX idx_interviews_scheduled (scheduled_at)
);

INSERT IGNORE INTO skills (skill_name) VALUES
    ('Java'), ('Python'), ('SQL'), ('Data Structures'), ('Git');
