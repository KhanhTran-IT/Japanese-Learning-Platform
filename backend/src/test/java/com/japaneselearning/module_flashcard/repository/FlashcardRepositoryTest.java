package com.japaneselearning.module_flashcard.repository;

import com.japaneselearning.module_flashcard.entity.Flashcard;
import com.japaneselearning.module_flashcard.entity.FlashcardDeck;
import com.japaneselearning.module_flashcard.entity.FlashcardProgress;
import com.japaneselearning.module_user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FlashcardRepositoryTest {

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private FlashcardProgressRepository progressRepository;

    @Autowired
    private FlashcardDeckRepository deckRepository;

    @Autowired
    private EntityManager em;

    private User student;
    private FlashcardDeck deckA;
    private FlashcardDeck deckB;
    private Flashcard cardA1;
    private Flashcard cardA2;
    private Flashcard cardA3;
    private Flashcard cardB1;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .email("student@test.com")
                .fullName("Flashcard Student")
                .passwordHash("hashed")
                .build();
        em.persist(student);

        deckA = FlashcardDeck.builder()
                .name("Deck A - N5")
                .level("N5")
                .icon("📗")
                .build();
        em.persist(deckA);

        deckB = FlashcardDeck.builder()
                .name("Deck B - N4")
                .level("N4")
                .icon("📘")
                .build();
        em.persist(deckB);

        cardA1 = Flashcard.builder()
                .deck(deckA)
                .frontText("食べる")
                .backMeaning("ăn")
                .build();
        em.persist(cardA1);

        cardA2 = Flashcard.builder()
                .deck(deckA)
                .frontText("飲む")
                .backMeaning("uống")
                .build();
        em.persist(cardA2);

        cardA3 = Flashcard.builder()
                .deck(deckA)
                .frontText("見る")
                .backMeaning("xem")
                .build();
        em.persist(cardA3);

        cardB1 = Flashcard.builder()
                .deck(deckB)
                .frontText("走る")
                .backMeaning("chạy")
                .build();
        em.persist(cardB1);

        em.flush();
    }

    @Test
    @DisplayName("countCardsByDeckIds aggregates card counts for all specified decks in a single query")
    void countCardsByDeckIds_aggregatesCounts() {
        List<Object[]> results = flashcardRepository.countCardsByDeckIds(List.of(deckA.getId(), deckB.getId()));

        Map<Long, Long> countsByDeck = results.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).longValue()
                ));

        assertThat(countsByDeck).containsEntry(deckA.getId(), 3L);
        assertThat(countsByDeck).containsEntry(deckB.getId(), 1L);
    }

    @Test
    @DisplayName("findByDeckId returns cards ordered by id ascending with fetch-joined deck")
    void findByDeckId_eagerlyLoadsDeck() {
        List<Flashcard> cards = flashcardRepository.findByDeckId(deckA.getId());

        assertThat(cards).hasSize(3);
        assertThat(cards.get(0).getId()).isLessThan(cards.get(1).getId());
        assertThat(cards.get(0).getDeck().getLevel()).isEqualTo("N5");
    }

    @Test
    @DisplayName("countNotDueCardsByDeckIds counts only cards with nextReviewTime strictly in the future")
    void countNotDueCardsByDeckIds_filtersByCurrentTime() {
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

        // cardA1: reviewed, not due yet (tomorrow)
        FlashcardProgress progressA1 = FlashcardProgress.builder()
                .user(student)
                .deck(deckA)
                .flashcard(cardA1)
                .nextReviewTime(nowUtc.plusDays(1))
                .build();
        em.persist(progressA1);

        // cardA2: reviewed, overdue (yesterday)
        FlashcardProgress progressA2 = FlashcardProgress.builder()
                .user(student)
                .deck(deckA)
                .flashcard(cardA2)
                .nextReviewTime(nowUtc.minusDays(1))
                .build();
        em.persist(progressA2);

        // cardA3 has no progress (new card)

        em.flush();

        List<Object[]> results = progressRepository.countNotDueCardsByDeckIds(
                student.getId(), List.of(deckA.getId(), deckB.getId()), nowUtc);

        Map<Long, Long> notDueMap = results.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).longValue()
                ));

        // Only cardA1 is not due in deckA
        assertThat(notDueMap).containsEntry(deckA.getId(), 1L);
        assertThat(notDueMap.getOrDefault(deckB.getId(), 0L)).isEqualTo(0L);
    }

    @Test
    @DisplayName("findByUserIdAndFlashcardIdIn loads progress for given card IDs in batch")
    void findByUserIdAndFlashcardIdIn_loadsProgressBatch() {
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

        FlashcardProgress progressA1 = FlashcardProgress.builder()
                .user(student)
                .deck(deckA)
                .flashcard(cardA1)
                .nextReviewTime(nowUtc.plusDays(1))
                .build();
        em.persist(progressA1);

        em.flush();

        List<FlashcardProgress> progresses = progressRepository.findByUserIdAndFlashcardIdIn(
                student.getId(), List.of(cardA1.getId(), cardA2.getId()));

        assertThat(progresses).hasSize(1);
        assertThat(progresses.get(0).getFlashcard().getId()).isEqualTo(cardA1.getId());
    }

    @Test
    @DisplayName("findDueCardsByDeckId returns new cards and overdue cards while excluding future cards")
    void findDueCardsByDeckId_returnsDueAndNewCards() {
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

        // cardA1: future (not due)
        FlashcardProgress progressA1 = FlashcardProgress.builder()
                .user(student)
                .deck(deckA)
                .flashcard(cardA1)
                .nextReviewTime(nowUtc.plusDays(2))
                .build();
        em.persist(progressA1);

        // cardA2: overdue (due)
        FlashcardProgress progressA2 = FlashcardProgress.builder()
                .user(student)
                .deck(deckA)
                .flashcard(cardA2)
                .nextReviewTime(nowUtc.minusMinutes(30))
                .build();
        em.persist(progressA2);

        // cardA3: no progress (due)
        em.flush();

        List<Flashcard> dueCards = flashcardRepository.findDueCardsByDeckId(
                student.getId(), deckA.getId(), nowUtc, PageRequest.of(0, 20));

        assertThat(dueCards).hasSize(2);
        assertThat(dueCards.stream().map(Flashcard::getId))
                .containsExactlyInAnyOrder(cardA2.getId(), cardA3.getId());
    }

    @Test
    @DisplayName("findAllByOrderByIdAsc returns decks ordered by ID ascending")
    void findAllByOrderByIdAsc_ordersCorrectly() {
        List<FlashcardDeck> decks = deckRepository.findAllByOrderByIdAsc();
        assertThat(decks).hasSize(2);
        assertThat(decks.get(0).getId()).isLessThan(decks.get(1).getId());
    }
}
