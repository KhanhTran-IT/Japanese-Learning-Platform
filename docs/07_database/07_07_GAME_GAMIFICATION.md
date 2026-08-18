> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

# 7.7. Nhóm Game/Gamification

## Bảng `games`

```sql
CREATE TABLE games (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    game_type VARCHAR(50) NOT NULL,
    thumbnail_url VARCHAR(500),
    status VARCHAR(30) DEFAULT 'PUBLISHED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

Game type:

```text
VOCAB_MATCHING
KANJI_CHOICE
LISTENING_CHOICE
HIRAGANA_ROMAJI
SENTENCE_REORDER
```

## Bảng `game_levels`

```sql
CREATE TABLE game_levels (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    game_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    difficulty VARCHAR(30) DEFAULT 'EASY',
    jlpt_level_id BIGINT,
    time_limit_seconds INT,
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_game_levels_game FOREIGN KEY (game_id) REFERENCES games(id),
    CONSTRAINT fk_game_levels_jlpt FOREIGN KEY (jlpt_level_id) REFERENCES jlpt_levels(id)
);
```

Difficulty:

```text
EASY
MEDIUM
HARD
EXPERT
```

## Bảng `game_sessions`

```sql
CREATE TABLE game_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    game_id BIGINT NOT NULL,
    game_level_id BIGINT,
    started_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    ended_at DATETIME,
    score INT DEFAULT 0,
    correct_count INT DEFAULT 0,
    wrong_count INT DEFAULT 0,
    status VARCHAR(30) DEFAULT 'IN_PROGRESS',
    CONSTRAINT fk_game_sessions_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_game_sessions_game FOREIGN KEY (game_id) REFERENCES games(id),
    CONSTRAINT fk_game_sessions_level FOREIGN KEY (game_level_id) REFERENCES game_levels(id)
);
```

## Bảng `user_xp_logs`

```sql
CREATE TABLE user_xp_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    source_id BIGINT,
    xp_amount INT NOT NULL,
    description VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_xp_logs_user FOREIGN KEY (user_id) REFERENCES users(id)
);
```

Source type:

```text
LESSON_COMPLETE
QUIZ_PASS
GAME_PLAY
DAILY_TASK
STREAK
BADGE
```

## Bảng `user_stats`

```sql
CREATE TABLE user_stats (
    user_id BIGINT PRIMARY KEY,
    total_xp INT DEFAULT 0,
    current_level INT DEFAULT 1,
    current_streak INT DEFAULT 0,
    longest_streak INT DEFAULT 0,
    last_study_date DATE,
    completed_lessons INT DEFAULT 0,
    completed_courses INT DEFAULT 0,
    completed_quizzes INT DEFAULT 0,
    played_games INT DEFAULT 0,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_stats_user FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## Bảng `badges`

```sql
CREATE TABLE badges (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    icon_url VARCHAR(500),
    condition_type VARCHAR(50),
    condition_value INT,
    status VARCHAR(30) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

## Bảng `user_badges`

```sql
CREATE TABLE user_badges (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    badge_id BIGINT NOT NULL,
    earned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_badges_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_badges_badge FOREIGN KEY (badge_id) REFERENCES badges(id),
    UNIQUE KEY uk_user_badge (user_id, badge_id)
);
```

---
