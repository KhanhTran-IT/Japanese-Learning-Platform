-- ============================================================
-- V2: Create Quiz module tables
-- Tables: quizzes, questions, answers, quiz_attempts, quiz_attempt_answers
-- ============================================================

-- 1. quizzes
CREATE TABLE quizzes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT,
    lesson_id BIGINT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    time_limit_minutes INT,
    passing_score DECIMAL(5,2) DEFAULT 0,
    max_attempts INT,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_quizzes_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_quizzes_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id)
);

-- 2. questions
CREATE TABLE questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    quiz_id BIGINT NOT NULL,
    question_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    audio_url VARCHAR(500),
    image_url VARCHAR(500),
    explanation TEXT,
    points DECIMAL(6,2) DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_questions_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
);

-- 3. answers
CREATE TABLE answers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    question_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_answers_question FOREIGN KEY (question_id) REFERENCES questions(id)
);

-- 4. quiz_attempts
CREATE TABLE quiz_attempts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    quiz_id BIGINT NOT NULL,
    started_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    submitted_at DATETIME,
    score DECIMAL(6,2) DEFAULT 0,
    total_questions INT DEFAULT 0,
    correct_count INT DEFAULT 0,
    wrong_count INT DEFAULT 0,
    passed BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',

    CONSTRAINT fk_quiz_attempts_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_quiz_attempts_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
);

-- 5. quiz_attempt_answers
CREATE TABLE quiz_attempt_answers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_id BIGINT,
    user_answer_text TEXT,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    points_earned DECIMAL(6,2) DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_attempt_answers_attempt FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id),
    CONSTRAINT fk_attempt_answers_question FOREIGN KEY (question_id) REFERENCES questions(id),
    CONSTRAINT fk_attempt_answers_answer FOREIGN KEY (answer_id) REFERENCES answers(id)
);

-- ============================================================
-- Indexes for common queries
-- ============================================================
CREATE INDEX idx_quizzes_course_status ON quizzes(course_id, status);
CREATE INDEX idx_quizzes_lesson_status ON quizzes(lesson_id, status);
CREATE INDEX idx_questions_quiz_sort ON questions(quiz_id, sort_order);
CREATE INDEX idx_answers_question_sort ON answers(question_id, sort_order);
CREATE INDEX idx_quiz_attempts_user_quiz ON quiz_attempts(user_id, quiz_id);
CREATE INDEX idx_attempt_answers_attempt ON quiz_attempt_answers(attempt_id);
