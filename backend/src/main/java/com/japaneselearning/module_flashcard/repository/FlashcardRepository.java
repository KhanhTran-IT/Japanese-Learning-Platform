package com.japaneselearning.module_flashcard.repository;

import com.japaneselearning.module_flashcard.entity.Flashcard;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    @Query("SELECT f FROM Flashcard f JOIN FETCH f.deck WHERE f.deck.id = :deckId ORDER BY f.id ASC")
    List<Flashcard> findByDeckId(@Param("deckId") Long deckId);

    Long countByDeckId(Long deckId);

    @Query("SELECT f.deck.id, COUNT(f.id) FROM Flashcard f WHERE f.deck.id IN :deckIds GROUP BY f.deck.id")
    List<Object[]> countCardsByDeckIds(@Param("deckIds") Collection<Long> deckIds);

    @Query("SELECT f FROM Flashcard f JOIN FETCH f.deck d " +
           "LEFT JOIN FlashcardProgress fp ON fp.flashcard.id = f.id AND fp.user.id = :userId " +
           "WHERE f.deck.id = :deckId " +
           "  AND (fp.id IS NULL OR fp.nextReviewTime <= :currentTime) " +
           "ORDER BY f.id ASC")
    List<Flashcard> findDueCardsByDeckId(@Param("userId") Long userId,
                                        @Param("deckId") Long deckId,
                                        @Param("currentTime") LocalDateTime currentTime,
                                        Pageable pageable);
}
