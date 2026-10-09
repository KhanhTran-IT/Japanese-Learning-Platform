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
}
