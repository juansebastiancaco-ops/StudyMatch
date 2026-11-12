-- =====================================================================
-- StudyMatch - esquema relacional MySQL 8
-- Ejecutado por docker-compose en /docker-entrypoint-initdb.d/01-schema.sql
-- =====================================================================

CREATE TABLE IF NOT EXISTS subjects (
    subject_id   VARCHAR(36)  NOT NULL,
    code         VARCHAR(30)  NOT NULL,
    name         VARCHAR(150) NOT NULL,
    area         VARCHAR(100) NOT NULL,
    credits      INT          NOT NULL,
    semester     INT          NOT NULL,
    active       TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (subject_id),
    CONSTRAINT uk_subjects_code UNIQUE (code),
    KEY idx_subjects_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS students (
    student_id    VARCHAR(36)  NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(150) NOT NULL,
    career        VARCHAR(120) NOT NULL,
    semester      INT          NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (student_id),
    CONSTRAINT uk_students_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS academic_profiles (
    profile_id         VARCHAR(36) NOT NULL,
    student_id         VARCHAR(36) NOT NULL,
    bio                VARCHAR(500),
    availability_state VARCHAR(20) NOT NULL,
    created_at         DATETIME(6) NOT NULL,
    updated_at         DATETIME(6) NOT NULL,
    PRIMARY KEY (profile_id),
    CONSTRAINT uk_profiles_student UNIQUE (student_id),
    CONSTRAINT fk_profiles_student FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS availability_slots (
    slot_id     VARCHAR(36) NOT NULL,
    profile_id  VARCHAR(36) NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    start_time  TIME(6)     NOT NULL,
    end_time    TIME(6)     NOT NULL,
    PRIMARY KEY (slot_id),
    KEY idx_availability_profile (profile_id),
    CONSTRAINT fk_availability_profile FOREIGN KEY (profile_id)
        REFERENCES academic_profiles (profile_id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS student_subjects (
    student_subject_id VARCHAR(36) NOT NULL,
    student_id         VARCHAR(36) NOT NULL,
    subject_id         VARCHAR(36) NOT NULL,
    PRIMARY KEY (student_subject_id),
    CONSTRAINT uk_student_subject UNIQUE (student_id, subject_id),
    KEY idx_student_subjects_student (student_id),
    CONSTRAINT fk_student_subjects_student FOREIGN KEY (student_id)
        REFERENCES students (student_id) ON DELETE CASCADE,
    CONSTRAINT fk_student_subjects_subject FOREIGN KEY (subject_id)
        REFERENCES subjects (subject_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS study_groups (
    group_id    VARCHAR(36)   NOT NULL,
    subject_id  VARCHAR(36)   NOT NULL,
    name        VARCHAR(100)  NOT NULL,
    description VARCHAR(1000),
    creator_id  VARCHAR(36)   NOT NULL,
    max_members INT           NOT NULL,
    modality    VARCHAR(20)   NOT NULL,
    status      VARCHAR(20)   NOT NULL,
    created_at  DATETIME(6)   NOT NULL,
    PRIMARY KEY (group_id),
    KEY idx_groups_subject (subject_id),
    KEY idx_groups_status (status),
    KEY idx_groups_creator (creator_id),
    CONSTRAINT fk_groups_subject FOREIGN KEY (subject_id) REFERENCES subjects (subject_id),
    CONSTRAINT fk_groups_creator FOREIGN KEY (creator_id) REFERENCES students (student_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS group_members (
    membership_id VARCHAR(36) NOT NULL,
    group_id      VARCHAR(36) NOT NULL,
    student_id    VARCHAR(36) NOT NULL,
    role          VARCHAR(20) NOT NULL,
    joined_at     DATETIME(6) NOT NULL,
    PRIMARY KEY (membership_id),
    CONSTRAINT uk_group_member UNIQUE (group_id, student_id),
    KEY idx_group_members_group (group_id),
    KEY idx_group_members_student (student_id),
    CONSTRAINT fk_group_members_group FOREIGN KEY (group_id)
        REFERENCES study_groups (group_id) ON DELETE CASCADE,
    CONSTRAINT fk_group_members_student FOREIGN KEY (student_id) REFERENCES students (student_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS group_schedule (
    slot_id     VARCHAR(36) NOT NULL,
    group_id    VARCHAR(36) NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    start_time  TIME(6)     NOT NULL,
    end_time    TIME(6)     NOT NULL,
    PRIMARY KEY (slot_id),
    KEY idx_group_schedule_group (group_id),
    CONSTRAINT fk_group_schedule_group FOREIGN KEY (group_id)
        REFERENCES study_groups (group_id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS join_requests (
    request_id  VARCHAR(36)  NOT NULL,
    group_id    VARCHAR(36)  NOT NULL,
    student_id  VARCHAR(36)  NOT NULL,
    message     VARCHAR(500),
    status      VARCHAR(20)  NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    resolved_at DATETIME(6),
    PRIMARY KEY (request_id),
    KEY idx_requests_group (group_id),
    KEY idx_requests_student (student_id),
    KEY idx_requests_status (status),
    CONSTRAINT fk_requests_group FOREIGN KEY (group_id)
        REFERENCES study_groups (group_id) ON DELETE CASCADE,
    CONSTRAINT fk_requests_student FOREIGN KEY (student_id) REFERENCES students (student_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notifications (
    notification_id VARCHAR(36)  NOT NULL,
    student_id      VARCHAR(36)  NOT NULL,
    type            VARCHAR(40)  NOT NULL,
    message         VARCHAR(500) NOT NULL,
    is_read         TINYINT(1)   NOT NULL DEFAULT 0,
    created_at      DATETIME(6)  NOT NULL,
    related_id      VARCHAR(36),
    PRIMARY KEY (notification_id),
    KEY idx_notifications_student (student_id),
    KEY idx_notifications_read (is_read),
    CONSTRAINT fk_notifications_student FOREIGN KEY (student_id)
        REFERENCES students (student_id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS action_history (
    action_id   VARCHAR(36)  NOT NULL,
    student_id  VARCHAR(36)  NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    description VARCHAR(500) NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    undone      TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (action_id),
    KEY idx_action_history_student (student_id),
    KEY idx_action_history_created (created_at),
    CONSTRAINT fk_action_history_student FOREIGN KEY (student_id)
        REFERENCES students (student_id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
