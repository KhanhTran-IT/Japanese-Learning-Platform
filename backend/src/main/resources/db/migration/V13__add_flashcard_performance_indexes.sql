-- V13__add_flashcard_performance_indexes.sql
-- Performance indexes for flashcard study and batch progress queries
-- 1. flashcards: deck_id with id ASC for fast deterministic retrieval and join fetch
-- 2. flashcard_progress: composite index on (user_id, flashcard_id, next_review_time) for covering index during due card lookups

CREATE INDEX idx_flashcards_deck_id_asc ON flashcards(deck_id, id ASC);
CREATE INDEX idx_flashcard_progress_user_card_due ON flashcard_progress(user_id, flashcard_id, next_review_time);
