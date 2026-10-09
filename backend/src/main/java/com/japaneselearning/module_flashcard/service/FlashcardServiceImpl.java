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
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlashcardServiceImpl implements FlashcardService {

    private final FlashcardDeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardProgressRepository progressRepository;
    private final FlashcardReviewLogRepository reviewLogRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlashcardDeckRes> getUserDecks() {
        User user = getCurrentUser();
        LocalDateTime currentUtc = LocalDateTime.now(ZoneOffset.UTC);
        
        return deckRepository.findAll().stream().map(deck -> {
            Long totalCards = flashcardRepository.countByDeckId(deck.getId());
            // notDue = cards that have progress AND next_review_time > now (not yet due)
            Long notDueCount = progressRepository.countNotDueCardsByDeckId(user.getId(), deck.getId(), currentUtc);
            // due = total - notDue (includes new cards with no progress + overdue cards)
            Long dueCards = totalCards - notDueCount;
            
            return FlashcardDeckRes.builder()
                    .id(deck.getId())
                    .name(deck.getName())
                    .level(deck.getLevel())
                    .icon(deck.getIcon())
                    .totalCards(totalCards)
                    .dueCards(Math.max(0, dueCards))
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlashcardRes> getDueCards(Long deckId) {
        User user = getCurrentUser();
        LocalDateTime currentUtc = LocalDateTime.now(ZoneOffset.UTC);
        
        List<Flashcard> allCards = flashcardRepository.findByDeckId(deckId);
        
        return allCards.stream()
            .filter(card -> {
                Optional<FlashcardProgress> prog = progressRepository.findByUserIdAndFlashcardId(user.getId(), card.getId());
                return prog.isEmpty() || prog.get().getNextReviewTime().isBefore(currentUtc) || prog.get().getNextReviewTime().isEqual(currentUtc);
            })
            .limit(20) // Batch size
            .map(this::mapToRes)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void reviewCard(ReviewFlashcardReq req) {
        User user = getCurrentUser();
        
        // Idempotency check
        if (reviewLogRepository.findByIdempotencyKey(req.getIdempotencyKey()).isPresent()) {
            return; // Already processed
        }

        Flashcard flashcard = flashcardRepository.findById(req.getFlashcardId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));
                
        FlashcardProgress progress = progressRepository.findByUserIdAndFlashcardId(user.getId(), flashcard.getId())
                .orElse(FlashcardProgress.builder()
                        .user(user)
                        .flashcard(flashcard)
                        .deck(flashcard.getDeck())
                        .easeFactor(2.5)
                        .intervalDays(0)
                        .build());
                        
        // Simple SRS Logic
        String difficulty = req.getDifficulty().toUpperCase();
        if (difficulty.equals("HARD")) {
            progress.setEaseFactor(Math.max(1.3, progress.getEaseFactor() - 0.2));
            progress.setIntervalDays(0);
        } else if (difficulty.equals("MEDIUM")) {
            if (progress.getIntervalDays() == 0) progress.setIntervalDays(1);
            else progress.setIntervalDays((int)(progress.getIntervalDays() * 1.2));
        } else if (difficulty.equals("EASY")) {
            progress.setEaseFactor(progress.getEaseFactor() + 0.15);
            if (progress.getIntervalDays() == 0) progress.setIntervalDays(3);
            else progress.setIntervalDays((int)(progress.getIntervalDays() * progress.getEaseFactor()));
        }

        // Calculate next review time with timezone consideration
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(req.getTimezone());
        } catch (Exception e) {
            zoneId = ZoneId.of("UTC");
        }
        
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
        if (progress.getIntervalDays() == 0) {
            // Due in 1 minute
            progress.setNextReviewTime(nowUtc.plusMinutes(1));
        } else {
            // Start of day in local timezone + interval
            LocalDateTime localNow = nowUtc.atZone(ZoneOffset.UTC).withZoneSameInstant(zoneId).toLocalDateTime();
            LocalDateTime localNextDue = localNow.plusDays(progress.getIntervalDays()).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime nextDueUtc = localNextDue.atZone(zoneId).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
            progress.setNextReviewTime(nextDueUtc);
        }

        progressRepository.save(progress);
        
        // Save log
        FlashcardReviewLog log = FlashcardReviewLog.builder()
                .user(user)
                .flashcard(flashcard)
                .difficulty(difficulty)
                .idempotencyKey(req.getIdempotencyKey())
                .reviewTime(nowUtc)
                .build();
        reviewLogRepository.save(log);
    }
    
    private FlashcardRes mapToRes(Flashcard f) {
        return FlashcardRes.builder()
                .id(f.getId())
                .deckId(f.getDeck().getId())
                .frontText(f.getFrontText())
                .frontReading(f.getFrontReading())
                .backMeaning(f.getBackMeaning())
                .backExample(f.getBackExample())
                .backExampleMeaning(f.getBackExampleMeaning())
                .level(f.getDeck().getLevel())
                .build();
    }
}
