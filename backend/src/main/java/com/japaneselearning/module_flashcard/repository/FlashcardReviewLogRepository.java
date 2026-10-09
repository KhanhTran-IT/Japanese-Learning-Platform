package com.japaneselearning.module_flashcard.repository;

import com.japaneselearning.module_flashcard.entity.FlashcardReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlashcardReviewLogRepository extends JpaRepository<FlashcardReviewLog, Long> {
    Optional<FlashcardReviewLog> findByIdempotencyKey(String idempotencyKey);
}
