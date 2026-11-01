package com.japaneselearning.module_flashcard.repository;

import com.japaneselearning.module_flashcard.entity.FlashcardProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardProgressRepository extends JpaRepository<FlashcardProgress, Long> {
    
    Optional<FlashcardProgress> findByUserIdAndFlashcardId(Long userId, Long flashcardId);
    
    @Query("SELECT COUNT(fp) FROM FlashcardProgress fp WHERE fp.user.id = :userId AND fp.deck.id = :deckId AND fp.nextReviewTime > :currentTime")
    Long countNotDueCardsByDeckId(@Param("userId") Long userId, @Param("deckId") Long deckId, @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT fp.deck.id, COUNT(fp.id) FROM FlashcardProgress fp " +
           "WHERE fp.user.id = :userId AND fp.deck.id IN :deckIds AND fp.nextReviewTime > :currentTime " +
           "GROUP BY fp.deck.id")
    List<Object[]> countNotDueCardsByDeckIds(@Param("userId") Long userId,
                                            @Param("deckIds") Collection<Long> deckIds,
                                            @Param("currentTime") LocalDateTime currentTime);

    List<FlashcardProgress> findByUserIdAndFlashcardIdIn(Long userId, Collection<Long> flashcardIds);

    List<FlashcardProgress> findByUserIdAndDeckId(Long userId, Long deckId);
}
