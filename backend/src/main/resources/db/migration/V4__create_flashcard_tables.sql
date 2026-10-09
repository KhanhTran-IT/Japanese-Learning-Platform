CREATE TABLE flashcard_decks (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    level VARCHAR(50),
    icon VARCHAR(50),
    course_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE flashcards (
    id BIGSERIAL PRIMARY KEY,
    deck_id BIGINT NOT NULL REFERENCES flashcard_decks(id) ON DELETE CASCADE,
    front_text VARCHAR(255) NOT NULL,
    front_reading VARCHAR(255),
    back_meaning VARCHAR(1000) NOT NULL,
    back_example VARCHAR(1000),
    back_example_meaning VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE flashcard_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    flashcard_id BIGINT NOT NULL REFERENCES flashcards(id) ON DELETE CASCADE,
    deck_id BIGINT NOT NULL REFERENCES flashcard_decks(id) ON DELETE CASCADE,
    ease_factor DOUBLE PRECISION NOT NULL DEFAULT 2.5,
    interval_days INTEGER NOT NULL DEFAULT 0,
    next_review_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, flashcard_id)
);

CREATE TABLE flashcard_review_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    flashcard_id BIGINT NOT NULL REFERENCES flashcards(id) ON DELETE CASCADE,
    difficulty VARCHAR(50) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    review_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for fast querying
CREATE INDEX idx_flashcards_deck_id ON flashcards(deck_id);
CREATE INDEX idx_flashcard_progress_user_deck ON flashcard_progress(user_id, deck_id);
CREATE INDEX idx_flashcard_progress_due ON flashcard_progress(user_id, deck_id, next_review_time);
