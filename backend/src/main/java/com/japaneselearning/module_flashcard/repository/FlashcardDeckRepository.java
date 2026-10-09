package com.japaneselearning.module_flashcard.repository;

import com.japaneselearning.module_flashcard.entity.FlashcardDeck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashcardDeckRepository extends JpaRepository<FlashcardDeck, Long> {
}
