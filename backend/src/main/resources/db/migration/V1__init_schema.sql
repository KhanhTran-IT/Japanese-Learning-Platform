-- V1__init_schema.sql

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    avatar_url VARCHAR(500),
    status ENUM('ACTIVE','DELETED','INACTIVE','LOCKED'),
    email_verified BOOLEAN,
    last_login_at DATETIME(6),
    created_at DATETIME(6),
    updated_at DATETIME(6)
);

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name ENUM('ADMIN','CONTENT_EDITOR','GUEST','STUDENT','SUPER_ADMIN','TEACHER') NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6)
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expired_at DATETIME(6) NOT NULL,
    revoked BOOLEAN,
    created_at DATETIME(6),
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    short_description TEXT,
    description LONGTEXT,
    thumbnail_url VARCHAR(255),
    level ENUM('ALL_LEVELS','N1','N2','N3','N4','N5') NOT NULL,
    course_type ENUM('FREE','PAID') NOT NULL,
    original_price DECIMAL(38,2),
    sale_price DECIMAL(38,2),
    status ENUM('ARCHIVED','DRAFT','HIDDEN','PUBLISHED') NOT NULL,
    total_duration_minutes INT,
    total_lessons INT,
    average_rating DOUBLE,
    total_students INT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES users(id)
);

CREATE TABLE course_sections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    sort_order INT NOT NULL,
    status ENUM('ARCHIVED','DRAFT','HIDDEN','PUBLISHED') NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    CONSTRAINT fk_cs_course FOREIGN KEY (course_id) REFERENCES courses(id)
);

CREATE TABLE lessons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    section_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    content LONGTEXT,
    video_url VARCHAR(255),
    audio_url VARCHAR(255),
    duration_minutes INT,
    sort_order INT NOT NULL,
    is_preview BOOLEAN NOT NULL,
    status ENUM('ARCHIVED','DRAFT','HIDDEN','PUBLISHED') NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    UNIQUE (course_id, slug),
    CONSTRAINT fk_lesson_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_lesson_section FOREIGN KEY (section_id) REFERENCES course_sections(id)
);

CREATE TABLE lesson_resources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lesson_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    resource_type ENUM('AUDIO','DOCUMENT','EXTERNAL_LINK','PDF','VIDEO') NOT NULL,
    file_url VARCHAR(1000) NOT NULL,
    file_size BIGINT,
    sort_order INT NOT NULL,
    created_at DATETIME(6),
    CONSTRAINT fk_lr_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id)
);

CREATE TABLE course_enrollments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    status ENUM('ACTIVE','CANCELLED','COMPLETED','PAUSED') NOT NULL,
    progress_percent INT NOT NULL,
    enrolled_at DATETIME(6),
    UNIQUE (user_id, course_id),
    CONSTRAINT fk_ce_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_ce_course FOREIGN KEY (course_id) REFERENCES courses(id)
);

CREATE TABLE lesson_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    watched_percent DOUBLE NOT NULL,
    is_completed BOOLEAN NOT NULL,
    completed_at DATETIME(6),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    UNIQUE (user_id, lesson_id),
    CONSTRAINT fk_lp_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_lp_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id)
);

-- INDEXES for high-traffic queries
CREATE INDEX idx_course_status ON courses(status);
CREATE INDEX idx_enrollment_user_course ON course_enrollments(user_id, course_id);
CREATE INDEX idx_lesson_course ON lessons(course_id);
CREATE INDEX idx_lesson_section_sort ON lessons(section_id, sort_order);
CREATE INDEX idx_progress_user_lesson ON lesson_progress(user_id, lesson_id);
CREATE INDEX idx_progress_completed ON lesson_progress(is_completed);
