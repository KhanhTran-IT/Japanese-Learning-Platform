package com.japaneselearning.module_flashcard.repository;

import com.japaneselearning.module_flashcard.entity.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    List<Flashcard> findByDeckId(Long deckId);
    Long countByDeckId(Long deckId);
}
