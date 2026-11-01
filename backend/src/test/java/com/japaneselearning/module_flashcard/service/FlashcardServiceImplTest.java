package com.japaneselearning.module_flashcard.service;

import com.japaneselearning.common.exception.AppException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardServiceImplTest {

    @Mock private FlashcardDeckRepository deckRepository;
    @Mock private FlashcardRepository flashcardRepository;
    @Mock private FlashcardProgressRepository progressRepository;
    @Mock private FlashcardReviewLogRepository reviewLogRepository;
    @Mock private UserRepository userRepository;
    @Mock private ScoringService scoringService;

    @InjectMocks
    private FlashcardServiceImpl flashcardService;

    private User mockUser;
    private Flashcard mockCard;
    
    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(1L).email("test@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@example.com", "password")
        );
        
        FlashcardDeck mockDeck = FlashcardDeck.builder().id(1L).build();
        mockCard = Flashcard.builder().id(101L).deck(mockDeck).build();
    }

    @Test
    void reviewCard_CreatesNewProgressAndCalculatesInterval() {
        // Arrange
        ReviewFlashcardReq req = new ReviewFlashcardReq();
        req.setFlashcardId(101L);
        req.setDifficulty("EASY");
        req.setIdempotencyKey("uuid-123");
        req.setTimezone("UTC");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(reviewLogRepository.findByIdempotencyKey("uuid-123")).thenReturn(Optional.empty());
        when(flashcardRepository.findById(101L)).thenReturn(Optional.of(mockCard));
        when(progressRepository.findByUserIdAndFlashcardId(1L, 101L)).thenReturn(Optional.empty());

        // Act
        flashcardService.reviewCard(req);

        // Assert
        verify(progressRepository).save(argThat(progress -> 
            progress.getFlashcard().getId().equals(101L) &&
            progress.getIntervalDays() == 3 &&
            progress.getEaseFactor() > 2.5
        ));
        
        verify(reviewLogRepository).save(any(FlashcardReviewLog.class));
    }
    
    @Test
    void reviewCard_IdempotencyCheck_PreventsDoubleProcessing() {
        // Arrange
        ReviewFlashcardReq req = new ReviewFlashcardReq();
        req.setFlashcardId(101L);
        req.setIdempotencyKey("uuid-123");
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(reviewLogRepository.findByIdempotencyKey("uuid-123")).thenReturn(Optional.of(new FlashcardReviewLog()));

        // Act
        flashcardService.reviewCard(req);

        // Assert
        verify(flashcardRepository, never()).findById(anyLong());
        verify(progressRepository, never()).save(any());
    }

    @Test
    void getUserDecks_shouldUseBatchAggregateQueries_andCalculateDueCards() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        FlashcardDeck deck1 = FlashcardDeck.builder().id(1L).name("N5 Deck").level("N5").icon("📗").build();
        FlashcardDeck deck2 = FlashcardDeck.builder().id(2L).name("N4 Deck").level("N4").icon("📘").build();
        when(deckRepository.findAllByOrderByIdAsc()).thenReturn(List.of(deck1, deck2));

        List<Object[]> cardCounts = List.of(
                new Object[]{1L, 10L},
                new Object[]{2L, 5L}
        );
        when(flashcardRepository.countCardsByDeckIds(List.of(1L, 2L))).thenReturn(cardCounts);

        List<Object[]> notDueCounts = List.of(
                new Object[]{1L, 3L},
                new Object[]{2L, 1L}
        );
        when(progressRepository.countNotDueCardsByDeckIds(eq(1L), eq(List.of(1L, 2L)), any(LocalDateTime.class)))
                .thenReturn(notDueCounts);

        // Act
        var result = flashcardService.getUserDecks();

        // Assert
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(10L, result.get(0).getTotalCards());
        assertEquals(7L, result.get(0).getDueCards()); // 10 - 3 = 7

        assertEquals(2L, result.get(1).getId());
        assertEquals(5L, result.get(1).getTotalCards());
        assertEquals(4L, result.get(1).getDueCards()); // 5 - 1 = 4

        // Verify aggregate queries called exactly once
        verify(flashcardRepository, times(1)).countCardsByDeckIds(List.of(1L, 2L));
        verify(progressRepository, times(1)).countNotDueCardsByDeckIds(eq(1L), eq(List.of(1L, 2L)), any(LocalDateTime.class));

        // Verify per-deck queries were NEVER called (avoiding N+1)
        verify(flashcardRepository, never()).countByDeckId(anyLong());
        verify(progressRepository, never()).countNotDueCardsByDeckId(anyLong(), anyLong(), any());
    }

    @Test
    void getUserDecks_whenNoDecksExist_returnsEmptyListImmediately() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(deckRepository.findAllByOrderByIdAsc()).thenReturn(List.of());

        // Act
        var result = flashcardService.getUserDecks();

        // Assert
        assertTrue(result.isEmpty());
        verify(flashcardRepository, never()).countCardsByDeckIds(any());
        verify(progressRepository, never()).countNotDueCardsByDeckIds(any(), any(), any());
    }

    @Test
    void getDueCards_shouldUseBatchProgressLoading_andFilterDueCards() {
        // Arrange
        Long deckId = 1L;
        FlashcardDeck deck = FlashcardDeck.builder().id(deckId).level("N5").build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(deckRepository.existsById(deckId)).thenReturn(true);

        Flashcard card1 = Flashcard.builder().id(101L).deck(deck).frontText("Card 1").build();
        Flashcard card2 = Flashcard.builder().id(102L).deck(deck).frontText("Card 2").build();
        Flashcard card3 = Flashcard.builder().id(103L).deck(deck).frontText("Card 3").build();

        when(flashcardRepository.findByDeckId(deckId)).thenReturn(List.of(card1, card2, card3));

        // card2 is NOT due (review in future); card3 is OVERDUE (review in past); card1 has NO progress (new)
        LocalDateTime nowUtc = LocalDateTime.now(java.time.ZoneOffset.UTC);
        FlashcardProgress progress2 = FlashcardProgress.builder()
                .flashcard(card2)
                .nextReviewTime(nowUtc.plusDays(2))
                .build();
        FlashcardProgress progress3 = FlashcardProgress.builder()
                .flashcard(card3)
                .nextReviewTime(nowUtc.minusHours(2))
                .build();

        when(progressRepository.findByUserIdAndFlashcardIdIn(eq(1L), eq(List.of(101L, 102L, 103L))))
                .thenReturn(List.of(progress2, progress3));

        // Act
        var result = flashcardService.getDueCards(deckId);

        // Assert: Only card1 (new) and card3 (overdue) should be returned
        assertEquals(2, result.size());
        assertEquals(101L, result.get(0).getId());
        assertEquals(103L, result.get(1).getId());

        // Verify batch query was called once
        verify(progressRepository, times(1)).findByUserIdAndFlashcardIdIn(eq(1L), eq(List.of(101L, 102L, 103L)));
        // Verify individual queries were NEVER called (avoiding N+1)
        verify(progressRepository, never()).findByUserIdAndFlashcardId(anyLong(), anyLong());
    }

    @Test
    void getDueCards_whenDeckNotFound_throwsResourceNotFound() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(deckRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        AppException ex = assertThrows(AppException.class, () -> flashcardService.getDueCards(999L));
        assertEquals(com.japaneselearning.common.exception.ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        verify(flashcardRepository, never()).findByDeckId(anyLong());
    }

    @Test
    void getDueCards_whenDeckHasNoCards_returnsEmptyList() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(deckRepository.existsById(1L)).thenReturn(true);
        when(flashcardRepository.findByDeckId(1L)).thenReturn(List.of());

        // Act
        var result = flashcardService.getDueCards(1L);

        // Assert
        assertTrue(result.isEmpty());
        verify(progressRepository, never()).findByUserIdAndFlashcardIdIn(any(), any());
    }
}
