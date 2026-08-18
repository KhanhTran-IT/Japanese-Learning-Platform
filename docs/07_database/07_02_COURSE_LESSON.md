> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

# 7.2. Nhóm Course/Lesson

## Bảng `courses`

```sql
CREATE TABLE courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teacher_id BIGINT,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    short_description VARCHAR(500),
    description TEXT,
    thumbnail_url VARCHAR(500),
    level VARCHAR(20),
    course_type VARCHAR(20) DEFAULT 'FREE',
    original_price DECIMAL(12,2) DEFAULT 0,
    sale_price DECIMAL(12,2) DEFAULT 0,
    status VARCHAR(30) DEFAULT 'DRAFT',
    total_duration_minutes INT DEFAULT 0,
    total_lessons INT DEFAULT 0,
    average_rating DECIMAL(3,2) DEFAULT 0,
    total_students INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id) REFERENCES users(id)
);
```

Level đề xuất:

```text
N5
N4
N3
N2
N1
BASIC
ADVANCED
```

Course type:

```text
FREE
PAID
MEMBERSHIP_ONLY
```

Status:

```text
DRAFT
PUBLISHED
HIDDEN
ARCHIVED
```

## Bảng `course_sections`

```sql
CREATE TABLE course_sections (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    sort_order INT DEFAULT 0,
    status VARCHAR(30) DEFAULT 'PUBLISHED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sections_course FOREIGN KEY (course_id) REFERENCES courses(id)
);
```

## Bảng `lessons`

```sql
CREATE TABLE lessons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    section_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    content LONGTEXT,
    video_url VARCHAR(500),
    audio_url VARCHAR(500),
    duration_minutes INT DEFAULT 0,
    sort_order INT DEFAULT 0,
    is_preview BOOLEAN DEFAULT FALSE,
    status VARCHAR(30) DEFAULT 'DRAFT',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_lessons_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_lessons_section FOREIGN KEY (section_id) REFERENCES course_sections(id),
    UNIQUE KEY uk_lesson_course_slug (course_id, slug)
);
```

## Bảng `lesson_resources`

```sql
CREATE TABLE lesson_resources (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lesson_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size BIGINT,
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resources_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id)
);
```

Resource type:

```text
PDF
IMAGE
AUDIO
VIDEO
DOCUMENT
LINK
```

## Bảng `course_enrollments`

```sql
CREATE TABLE course_enrollments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    order_id BIGINT,
    enrolled_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    expired_at DATETIME,
    progress_percent DECIMAL(5,2) DEFAULT 0,
    status VARCHAR(30) DEFAULT 'ACTIVE',
    completed_at DATETIME,
    CONSTRAINT fk_enrollments_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id) REFERENCES courses(id),
    UNIQUE KEY uk_user_course (user_id, course_id)
);
```

## Bảng `lesson_progress`

```sql
CREATE TABLE lesson_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    watched_percent DECIMAL(5,2) DEFAULT 0,
    completed BOOLEAN DEFAULT FALSE,
    completed_at DATETIME,
    last_accessed_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_lesson_progress_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_lesson_progress_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_lesson_progress_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id),
    UNIQUE KEY uk_user_lesson_progress (user_id, lesson_id)
);
```

## Bảng `course_reviews`

```sql
CREATE TABLE course_reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    status VARCHAR(30) DEFAULT 'VISIBLE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_reviews_course FOREIGN KEY (course_id) REFERENCES courses(id),
    UNIQUE KEY uk_user_course_review (user_id, course_id)
);
```

---
