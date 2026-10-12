CREATE TABLE flashcard_decks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    level VARCHAR(50),
    icon VARCHAR(50),
    course_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE flashcards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    deck_id BIGINT NOT NULL,
    front_text VARCHAR(255) NOT NULL,
    front_reading VARCHAR(255),
    back_meaning VARCHAR(1000) NOT NULL,
    back_example VARCHAR(1000),
    back_example_meaning VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_flashcards_deck FOREIGN KEY (deck_id) REFERENCES flashcard_decks(id) ON DELETE CASCADE
);

CREATE TABLE flashcard_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    flashcard_id BIGINT NOT NULL,
    deck_id BIGINT NOT NULL,
    ease_factor DOUBLE NOT NULL DEFAULT 2.5,
    interval_days INT NOT NULL DEFAULT 0,
    next_review_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_flashcard_progress_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_flashcard_progress_flashcard FOREIGN KEY (flashcard_id) REFERENCES flashcards(id) ON DELETE CASCADE,
    CONSTRAINT fk_flashcard_progress_deck FOREIGN KEY (deck_id) REFERENCES flashcard_decks(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_flashcard UNIQUE (user_id, flashcard_id)
);

CREATE TABLE flashcard_review_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    flashcard_id BIGINT NOT NULL,
    difficulty VARCHAR(50) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    review_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_flashcard_review_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_flashcard_review_logs_flashcard FOREIGN KEY (flashcard_id) REFERENCES flashcards(id) ON DELETE CASCADE
);

-- Indexes for fast querying
CREATE INDEX idx_flashcards_deck_id ON flashcards(deck_id);
CREATE INDEX idx_flashcard_progress_user_deck ON flashcard_progress(user_id, deck_id);
CREATE INDEX idx_flashcard_progress_due ON flashcard_progress(user_id, deck_id, next_review_time);
