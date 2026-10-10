CREATE TABLE learning_activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    activity_type VARCHAR(50) NOT NULL, -- LESSON_COMPLETED, QUIZ_PASSED, FLASHCARD_REVIEWED, DAILY_STREAK
    xp_amount INT NOT NULL,
    reference_id BIGINT,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_scores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    period_type VARCHAR(20) NOT NULL, -- ALL_TIME, WEEKLY, MONTHLY
    period_value VARCHAR(20) NOT NULL, -- ALL, 2026-W41, 2026-10
    total_xp INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_score_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_period (user_id, period_type, period_value)
);

CREATE INDEX idx_scores_ranking ON user_scores(period_type, period_value, total_xp DESC);

ALTER TABLE users
ADD COLUMN level INT NOT NULL DEFAULT 1,
ADD COLUMN current_streak INT NOT NULL DEFAULT 0,
ADD COLUMN last_activity_date DATE,
ADD COLUMN country VARCHAR(10) DEFAULT '🇻🇳',
ADD COLUMN is_private_leaderboard BOOLEAN NOT NULL DEFAULT FALSE;
