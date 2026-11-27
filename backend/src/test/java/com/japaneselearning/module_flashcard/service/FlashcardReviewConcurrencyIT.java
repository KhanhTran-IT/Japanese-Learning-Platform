package com.japaneselearning.module_flashcard.service;

import com.japaneselearning.module_flashcard.dto.ReviewFlashcardReq;
import com.japaneselearning.module_flashcard.entity.Flashcard;
import com.japaneselearning.module_flashcard.entity.FlashcardDeck;
import com.japaneselearning.module_flashcard.entity.FlashcardProgress;
import com.japaneselearning.module_flashcard.entity.FlashcardReviewLog;
import com.japaneselearning.module_flashcard.repository.FlashcardDeckRepository;
import com.japaneselearning.module_flashcard.repository.FlashcardProgressRepository;
import com.japaneselearning.module_flashcard.repository.FlashcardRepository;
import com.japaneselearning.module_flashcard.repository.FlashcardReviewLogRepository;
import com.japaneselearning.module_leaderboard.entity.LearningActivity;
import com.japaneselearning.module_leaderboard.entity.UserScore;
import com.japaneselearning.module_leaderboard.repository.LearningActivityRepository;
import com.japaneselearning.module_leaderboard.repository.UserScoreRepository;
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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FlashcardReviewConcurrencyIT {

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

    @Autowired
    private LearningActivityRepository activityRepository;

    @Autowired
    private UserScoreRepository scoreRepository;

    private User studentA;
    private User studentB;
    private Flashcard card;

    @BeforeEach
    void setUp() {
        reviewLogRepository.deleteAll();
        progressRepository.deleteAll();
        flashcardRepository.deleteAll();
        deckRepository.deleteAll();
        activityRepository.deleteAll();
        scoreRepository.deleteAll();
        userRepository.deleteAll();

        studentA = userRepository.saveAndFlush(User.builder()
                .email("flash_student_a@example.com")
                .fullName("Flashcard Student A")
                .passwordHash("password")
                .status(UserStatus.ACTIVE)
                .level(1)
                .currentStreak(0)
                .build());

        studentB = userRepository.saveAndFlush(User.builder()
                .email("flash_student_b@example.com")
                .fullName("Flashcard Student B")
                .passwordHash("password")
                .status(UserStatus.ACTIVE)
                .level(1)
                .currentStreak(0)
                .build());

        FlashcardDeck deck = deckRepository.saveAndFlush(FlashcardDeck.builder()
                .name("N5 Core")
                .level("N5")
                .icon("📘")
                .build());

        card = flashcardRepository.saveAndFlush(Flashcard.builder()
                .deck(deck)
                .frontText("水")
                .frontReading("みず")
                .backMeaning("nước")
                .build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String email) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(email, "password"));
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("Concurrency: 10 concurrent duplicate reviews for same user & same idempotencyKey -> progress, log, and XP applied exactly once")
    void concurrentDuplicateReview_SameUserSameKey_AppliedExactlyOnce() throws InterruptedException {
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    authenticateAs(studentA.getEmail());
                    startLatch.await();

                    ReviewFlashcardReq req = ReviewFlashcardReq.builder()
                            .flashcardId(card.getId())
                            .difficulty("EASY")
                            .idempotencyKey("dup-review-key-1")
                            .timezone("Asia/Ho_Chi_Minh")
                            .build();
                    flashcardService.reviewCard(req);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    SecurityContextHolder.clearContext();
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertThat(errorCount.get()).isEqualTo(0);
        assertThat(successCount.get()).isEqualTo(threads);

        List<FlashcardReviewLog> logs = reviewLogRepository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getUser().getId()).isEqualTo(studentA.getId());
        assertThat(logs.get(0).getIdempotencyKey()).isEqualTo("dup-review-key-1");

        FlashcardProgress progress = progressRepository.findByUserIdAndFlashcardId(studentA.getId(), card.getId())
                .orElseThrow();
        assertThat(progress.getIntervalDays()).isEqualTo(3);
        assertThat(progress.getEaseFactor()).isEqualTo(2.65);

        List<LearningActivity> activities = activityRepository.findByUserId(studentA.getId());
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getActivityType()).isEqualTo("FLASHCARD_REVIEWED");
        assertThat(activities.get(0).getXpAmount()).isEqualTo(10);

        UserScore allTimeScore = scoreRepository
                .findByUserIdAndPeriodTypeAndPeriodValue(studentA.getId(), "ALL_TIME", "ALL")
                .orElseThrow();
        assertThat(allTimeScore.getTotalXp()).isEqualTo(10);
    }

    @Test
    @DisplayName("Isolation: Two different users submitting the same idempotencyKey concurrently both succeed independently")
    void concurrentReview_DifferentUsersSameKey_IsolatedAndBothApplied() throws InterruptedException {
        int threadsPerUser = 4;
        int totalThreads = threadsPerUser * 2;
        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalThreads);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < threadsPerUser; i++) {
            executor.submit(() -> {
                try {
                    authenticateAs(studentA.getEmail());
                    startLatch.await();
                    flashcardService.reviewCard(ReviewFlashcardReq.builder()
                            .flashcardId(card.getId())
                            .difficulty("EASY")
                            .idempotencyKey("shared-key-across-users")
                            .timezone("UTC")
                            .build());
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    SecurityContextHolder.clearContext();
                    doneLatch.countDown();
                }
            });

            executor.submit(() -> {
                try {
                    authenticateAs(studentB.getEmail());
                    startLatch.await();
                    flashcardService.reviewCard(ReviewFlashcardReq.builder()
                            .flashcardId(card.getId())
                            .difficulty("MEDIUM")
                            .idempotencyKey("shared-key-across-users")
                            .timezone("UTC")
                            .build());
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    SecurityContextHolder.clearContext();
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertThat(errorCount.get()).isEqualTo(0);

        // Exactly 1 review log per user
        assertThat(reviewLogRepository.findByUserIdAndIdempotencyKey(studentA.getId(), "shared-key-across-users")).isPresent();
        assertThat(reviewLogRepository.findByUserIdAndIdempotencyKey(studentB.getId(), "shared-key-across-users")).isPresent();
        assertThat(reviewLogRepository.count()).isEqualTo(2);

        // Each user has their own progress and exactly 10 XP
        FlashcardProgress progressA = progressRepository.findByUserIdAndFlashcardId(studentA.getId(), card.getId()).orElseThrow();
        FlashcardProgress progressB = progressRepository.findByUserIdAndFlashcardId(studentB.getId(), card.getId()).orElseThrow();
        assertThat(progressA.getIntervalDays()).isEqualTo(3);
        assertThat(progressB.getIntervalDays()).isEqualTo(1);

        UserScore scoreA = scoreRepository.findByUserIdAndPeriodTypeAndPeriodValue(studentA.getId(), "ALL_TIME", "ALL").orElseThrow();
        UserScore scoreB = scoreRepository.findByUserIdAndPeriodTypeAndPeriodValue(studentB.getId(), "ALL_TIME", "ALL").orElseThrow();
        assertThat(scoreA.getTotalXp()).isEqualTo(10);
        assertThat(scoreB.getTotalXp()).isEqualTo(10);
    }

    @Test
    @DisplayName("Concurrency: Same user with distinct idempotencyKeys -> all reviews applied without lost updates or constraint errors")
    void concurrentReview_SameUserDifferentKeys_AllAppliedSafely() throws InterruptedException {
        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            final String key = "distinct-key-" + i;
            executor.submit(() -> {
                try {
                    authenticateAs(studentA.getEmail());
                    startLatch.await();
                    flashcardService.reviewCard(ReviewFlashcardReq.builder()
                            .flashcardId(card.getId())
                            .difficulty("EASY")
                            .idempotencyKey(key)
                            .timezone("UTC")
                            .build());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    SecurityContextHolder.clearContext();
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertThat(errorCount.get()).isEqualTo(0);
        assertThat(successCount.get()).isEqualTo(threads);
        assertThat(reviewLogRepository.count()).isEqualTo(threads);

        List<LearningActivity> activities = activityRepository.findByUserId(studentA.getId());
        assertThat(activities).hasSize(threads);

        UserScore allTimeScore = scoreRepository
                .findByUserIdAndPeriodTypeAndPeriodValue(studentA.getId(), "ALL_TIME", "ALL")
                .orElseThrow();
        assertThat(allTimeScore.getTotalXp()).isEqualTo(threads * 10);
    }
}
