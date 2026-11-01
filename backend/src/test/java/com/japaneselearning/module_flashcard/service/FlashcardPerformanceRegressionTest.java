package com.japaneselearning.module_flashcard.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_flashcard.dto.FlashcardDeckRes;
import com.japaneselearning.module_flashcard.dto.FlashcardRes;
import com.japaneselearning.module_flashcard.dto.ReviewFlashcardReq;
import com.japaneselearning.module_flashcard.entity.Flashcard;
import com.japaneselearning.module_flashcard.entity.FlashcardDeck;
import com.japaneselearning.module_flashcard.entity.FlashcardProgress;
import com.japaneselearning.module_flashcard.entity.FlashcardReviewLog;
import com.japaneselearning.module_flashcard.repository.FlashcardDeckRepository;
import com.japaneselearning.module_flashcard.repository.FlashcardProgressRepository;
import com.japaneselearning.module_flashcard.repository.FlashcardRepository;
import com.japaneselearning.module_flashcard.repository.FlashcardReviewLogRepository;
import com.japaneselearning.module_leaderboard.service.ScoringService;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardPerformanceRegressionTest {

    @Mock
    private FlashcardDeckRepository deckRepository;

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private FlashcardProgressRepository progressRepository;

    @Mock
    private FlashcardReviewLogRepository reviewLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ScoringService scoringService;

    @InjectMocks
    private FlashcardServiceImpl flashcardService;

    private User student;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .id(1L)
                .email("student@test.com")
                .fullName("Test Student")
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("student@test.com", "password")
        );

        lenient().when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getUserDecks query count must be O(1) and not grow with the number of decks (tested with 50 decks)")
    void getUserDecks_queryCountDoesNotGrowWithNumberOfDecks() {
        // Arrange: 50 decks
        int deckCount = 50;
        List<FlashcardDeck> decks = new ArrayList<>();
        List<Long> deckIds = new ArrayList<>();
        List<Object[]> cardCounts = new ArrayList<>();
        List<Object[]> notDueCounts = new ArrayList<>();

        for (long i = 1; i <= deckCount; i++) {
            decks.add(FlashcardDeck.builder().id(i).name("Deck " + i).level("N5").build());
            deckIds.add(i);
            cardCounts.add(new Object[]{i, 20L}); // 20 cards per deck
            notDueCounts.add(new Object[]{i, 5L}); // 5 not due -> 15 due
        }

        when(deckRepository.findAllByOrderByIdAsc()).thenReturn(decks);
        when(flashcardRepository.countCardsByDeckIds(deckIds)).thenReturn(cardCounts);
        when(progressRepository.countNotDueCardsByDeckIds(eq(student.getId()), eq(deckIds), any(LocalDateTime.class)))
                .thenReturn(notDueCounts);

        // Act
        List<FlashcardDeckRes> result = flashcardService.getUserDecks();

        // Assert
        assertThat(result).hasSize(50);
        for (FlashcardDeckRes res : result) {
            assertThat(res.getTotalCards()).isEqualTo(20L);
            assertThat(res.getDueCards()).isEqualTo(15L); // 20 - 5 = 15
        }

        // Verify constant query count (O(1))
        verify(deckRepository, times(1)).findAllByOrderByIdAsc();
        verify(flashcardRepository, times(1)).countCardsByDeckIds(deckIds);
        verify(progressRepository, times(1)).countNotDueCardsByDeckIds(eq(student.getId()), eq(deckIds), any(LocalDateTime.class));

        // CRITICAL REGRESSION ASSERTION: N+1 queries must NEVER be triggered
        verify(flashcardRepository, never()).countByDeckId(anyLong());
        verify(progressRepository, never()).countNotDueCardsByDeckId(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("getDueCards query count must be O(1) and not grow with the number of cards (tested with 100 cards)")
    void getDueCards_queryCountDoesNotGrowWithNumberOfCards_andLimitsTo20() {
        // Arrange: 100 cards in deck 1
        Long deckId = 1L;
        FlashcardDeck deck = FlashcardDeck.builder().id(deckId).name("N5 Kanji").level("N5").build();
        when(deckRepository.existsById(deckId)).thenReturn(true);

        List<Flashcard> cards = new ArrayList<>();
        List<Long> cardIds = new ArrayList<>();
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
        List<FlashcardProgress> progresses = new ArrayList<>();

        for (long i = 1; i <= 100; i++) {
            Flashcard card = Flashcard.builder()
                    .id(i)
                    .deck(deck)
                    .frontText("Kanji " + i)
                    .backMeaning("Meaning " + i)
                    .build();
            cards.add(card);
            cardIds.add(i);

            // Odd cards are overdue (due), even cards are scheduled for tomorrow (not due)
            if (i % 2 == 0) {
                progresses.add(FlashcardProgress.builder()
                        .flashcard(card)
                        .nextReviewTime(nowUtc.plusDays(1))
                        .build());
            } else {
                progresses.add(FlashcardProgress.builder()
                        .flashcard(card)
                        .nextReviewTime(nowUtc.minusHours(5))
                        .build());
            }
        }

        when(flashcardRepository.findByDeckId(deckId)).thenReturn(cards);
        when(progressRepository.findByUserIdAndFlashcardIdIn(eq(student.getId()), eq(cardIds)))
                .thenReturn(progresses);

        // Act
        List<FlashcardRes> result = flashcardService.getDueCards(deckId);

        // Assert: 50 odd cards are due, but endpoint must enforce batch limit 20
        assertThat(result).hasSize(20);
        for (FlashcardRes cardRes : result) {
            assertThat(cardRes.getId() % 2).isEqualTo(1); // Only odd cards (due)
            assertThat(cardRes.getLevel()).isEqualTo("N5");
        }

        // Verify constant query count (O(1))
        verify(deckRepository, times(1)).existsById(deckId);
        verify(flashcardRepository, times(1)).findByDeckId(deckId);
        verify(progressRepository, times(1)).findByUserIdAndFlashcardIdIn(eq(student.getId()), eq(cardIds));

        // CRITICAL REGRESSION ASSERTION: Never execute findByUserIdAndFlashcardId per card
        verify(progressRepository, never()).findByUserIdAndFlashcardId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("getDueCards should throw RESOURCE_NOT_FOUND when deck does not exist")
    void getDueCards_deckNotFound_throwsException() {
        when(deckRepository.existsById(999L)).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> flashcardService.getDueCards(999L));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        verify(flashcardRepository, never()).findByDeckId(anyLong());
        verify(progressRepository, never()).findByUserIdAndFlashcardIdIn(any(), any());
    }

    @Test
    @DisplayName("reviewCard must preserve idempotency and prevent duplicate XP/interval calculation")
    void reviewCard_preservesIdempotency() {
        ReviewFlashcardReq req = new ReviewFlashcardReq();
        req.setFlashcardId(10L);
        req.setIdempotencyKey("idem-key-777");
        req.setDifficulty("HARD");
        req.setTimezone("Asia/Tokyo");

        // Existing review log with this idempotency key
        when(reviewLogRepository.findByIdempotencyKey("idem-key-777"))
                .thenReturn(Optional.of(new FlashcardReviewLog()));

        // Act
        flashcardService.reviewCard(req);

        // Assert
        verify(flashcardRepository, never()).findById(anyLong());
        verify(progressRepository, never()).save(any());
        verify(reviewLogRepository, never()).save(any());
        verify(scoringService, never()).addXp(anyLong(), anyString(), anyInt(), any(), anyString());
    }
}
