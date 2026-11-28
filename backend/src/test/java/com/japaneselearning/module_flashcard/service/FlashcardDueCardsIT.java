package com.japaneselearning.module_flashcard.service;

import com.japaneselearning.module_flashcard.dto.FlashcardRes;
import com.japaneselearning.module_flashcard.entity.Flashcard;
import com.japaneselearning.module_flashcard.entity.FlashcardDeck;
import com.japaneselearning.module_flashcard.entity.FlashcardProgress;
import com.japaneselearning.module_flashcard.repository.FlashcardDeckRepository;
import com.japaneselearning.module_flashcard.repository.FlashcardProgressRepository;
import com.japaneselearning.module_flashcard.repository.FlashcardRepository;
import com.japaneselearning.module_flashcard.repository.FlashcardReviewLogRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.UserStatus;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FlashcardDueCardsIT {

    @Autowired
    private FlashcardService flashcardService;

    @Autowired
    private FlashcardDeckRepository deckRepository;

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private FlashcardProgressRepository progressRepository;

    @Autowired
    private FlashcardReviewLogRepository reviewLogRepository;

    @Autowired
    private UserRepository userRepository;

    private User student;
    private User otherStudent;
    private FlashcardDeck deck;

    @BeforeEach
    void setUp() {
        reviewLogRepository.deleteAll();
        progressRepository.deleteAll();
        flashcardRepository.deleteAll();
        deckRepository.deleteAll();
        userRepository.deleteAll();

        student = userRepository.saveAndFlush(User.builder()
                .email("due_student@example.com")
                .fullName("Due Student")
                .passwordHash("password")
                .status(UserStatus.ACTIVE)
                .level(1)
                .currentStreak(0)
                .build());

        otherStudent = userRepository.saveAndFlush(User.builder()
                .email("other_due_student@example.com")
                .fullName("Other Due Student")
                .passwordHash("password")
                .status(UserStatus.ACTIVE)
                .level(1)
                .currentStreak(0)
                .build());

        deck = deckRepository.saveAndFlush(FlashcardDeck.builder()
                .name("N5 Vocabulary")
                .level("N5")
                .icon("📘")
                .build());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(student.getEmail(), "password")
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getDueCards returns at most 20 due cards in deterministic id ASC order, preserving SRS and API contract")
    void getDueCards_returnsDeterministicBatchOf20WithCorrectSrsAndContract() {
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
        List<Flashcard> cards = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            cards.add(flashcardRepository.saveAndFlush(Flashcard.builder()
                    .deck(deck)
                    .frontText("Front " + i)
                    .frontReading("Reading " + i)
                    .backMeaning("Meaning " + i)
                    .backExample("Example " + i)
                    .backExampleMeaning("Example Meaning " + i)
                    .build()));
        }

        // First 5 cards are scheduled in the future for `student` -> must be excluded
        for (int i = 0; i < 5; i++) {
            progressRepository.saveAndFlush(FlashcardProgress.builder()
                    .user(student)
                    .deck(deck)
                    .flashcard(cards.get(i))
                    .easeFactor(2.5)
                    .intervalDays(3)
                    .nextReviewTime(nowUtc.plusDays(2))
                    .build());
        }

        // Cards 5..9 are overdue for `student` -> must be included first
        for (int i = 5; i < 10; i++) {
            progressRepository.saveAndFlush(FlashcardProgress.builder()
                    .user(student)
                    .deck(deck)
                    .flashcard(cards.get(i))
                    .easeFactor(2.5)
                    .intervalDays(1)
                    .nextReviewTime(nowUtc.minusMinutes(10))
                    .build());
        }

        // Card 10 has future progress ONLY for `otherStudent` -> must still be due for `student`
        progressRepository.saveAndFlush(FlashcardProgress.builder()
                .user(otherStudent)
                .deck(deck)
                .flashcard(cards.get(10))
                .easeFactor(2.65)
                .intervalDays(5)
                .nextReviewTime(nowUtc.plusDays(5))
                .build());

        List<FlashcardRes> dueCards = flashcardService.getDueCards(deck.getId());

        // 25 cards are due (cards 5..29), Pageable caps at 20 -> cards 5..24 in deterministic ascending ID order
        assertThat(dueCards).hasSize(20);
        List<Long> expectedIds = cards.subList(5, 25).stream().map(Flashcard::getId).toList();
        assertThat(dueCards).extracting(FlashcardRes::getId).containsExactlyElementsOf(expectedIds);

        // Verify full API DTO contract mapping
        FlashcardRes first = dueCards.get(0);
        assertThat(first.getDeckId()).isEqualTo(deck.getId());
        assertThat(first.getFrontText()).isEqualTo("Front 6");
        assertThat(first.getFrontReading()).isEqualTo("Reading 6");
        assertThat(first.getBackMeaning()).isEqualTo("Meaning 6");
        assertThat(first.getBackExample()).isEqualTo("Example 6");
        assertThat(first.getBackExampleMeaning()).isEqualTo("Example Meaning 6");
        assertThat(first.getLevel()).isEqualTo("N5");
    }
}
