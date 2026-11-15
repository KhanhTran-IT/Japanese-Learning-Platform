package com.japaneselearning.module_leaderboard.service;

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
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ScoringConcurrencyIT {

    @Autowired
    private ScoringService scoringService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LearningActivityRepository activityRepository;

    @Autowired
    private UserScoreRepository scoreRepository;

    private User studentA;
    private User studentB;

    @BeforeEach
    void setUp() {
        activityRepository.deleteAll();
        scoreRepository.deleteAll();
        userRepository.deleteAll();

        studentA = userRepository.saveAndFlush(User.builder()
                .email("student_a@example.com")
                .fullName("Student A")
                .passwordHash("password")
                .status(UserStatus.ACTIVE)
                .level(1)
                .currentStreak(0)
                .build());

        studentB = userRepository.saveAndFlush(User.builder()
                .email("student_b@example.com")
                .fullName("Student B")
                .passwordHash("password")
                .status(UserStatus.ACTIVE)
                .level(1)
                .currentStreak(0)
                .build());
    }

    @AfterEach
    void tearDown() {
        activityRepository.deleteAll();
        scoreRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Concurrency: 10 concurrent requests completing the same lesson -> only rewarded once, idempotent no-op")
    void testConcurrentReferenceActivities_SameReferenceId_OnlyAwardedOnce() throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        Long lessonId = 101L;

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    scoringService.addXp(studentA.getId(), "LESSON_COMPLETED", 100, lessonId, "Hoàn thành bài học 101");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        // All threads completed cleanly without error
        assertThat(errorCount.get()).isEqualTo(0);
        assertThat(successCount.get()).isEqualTo(numberOfThreads);

        // Database checks: exactly 1 learning activity persisted
        List<LearningActivity> activities = activityRepository.findByUserId(studentA.getId());
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getActivityType()).isEqualTo("LESSON_COMPLETED");
        assertThat(activities.get(0).getReferenceId()).isEqualTo(lessonId);
        assertThat(activities.get(0).getXpAmount()).isEqualTo(100);

        // Scores checks: exactly 100 XP for all periods
        LocalDate now = LocalDate.now(ZoneOffset.UTC);
        String currentWeek = now.getYear() + "-W" + now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        String currentMonth = now.getYear() + "-" + String.format("%02d", now.getMonthValue());

        UserScore allTime = scoreRepository.findByUserIdAndPeriodTypeAndPeriodValue(studentA.getId(), "ALL_TIME", "ALL").orElseThrow();
        UserScore weekly = scoreRepository.findByUserIdAndPeriodTypeAndPeriodValue(studentA.getId(), "WEEKLY", currentWeek).orElseThrow();
        UserScore monthly = scoreRepository.findByUserIdAndPeriodTypeAndPeriodValue(studentA.getId(), "MONTHLY", currentMonth).orElseThrow();

        assertThat(allTime.getTotalXp()).isEqualTo(100);
        assertThat(weekly.getTotalXp()).isEqualTo(100);
        assertThat(monthly.getTotalXp()).isEqualTo(100);

        // User streak and level
        User reloadedUser = userRepository.findById(studentA.getId()).orElseThrow();
        assertThat(reloadedUser.getCurrentStreak()).isEqualTo(1);
        assertThat(reloadedUser.getLevel()).isEqualTo(1);
        assertThat(reloadedUser.getLastActivityDate()).isEqualTo(now);
    }

    @Test
    @DisplayName("Concurrency: 10 concurrent flashcard reviews (nullable reference) -> atomic accumulation without lost updates")
    void testConcurrentNullableReferenceActivities_AtomicAccumulation_ZeroLostUpdates() throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    scoringService.addXp(studentA.getId(), "FLASHCARD_REVIEWED", 10, null, "Ôn tập flashcard");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        assertThat(errorCount.get()).isEqualTo(0);
        assertThat(successCount.get()).isEqualTo(numberOfThreads);

        // Exactly 10 activity rows for the 10 reviews
        List<LearningActivity> activities = activityRepository.findByUserId(studentA.getId());
        assertThat(activities).hasSize(10);

        // Exactly 100 XP (10 * 10 XP) - proves NO lost updates!
        LocalDate now = LocalDate.now(ZoneOffset.UTC);
        String currentWeek = now.getYear() + "-W" + now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        String currentMonth = now.getYear() + "-" + String.format("%02d", now.getMonthValue());

        UserScore allTime = scoreRepository.findByUserIdAndPeriodTypeAndPeriodValue(studentA.getId(), "ALL_TIME", "ALL").orElseThrow();
        UserScore weekly = scoreRepository.findByUserIdAndPeriodTypeAndPeriodValue(studentA.getId(), "WEEKLY", currentWeek).orElseThrow();
        UserScore monthly = scoreRepository.findByUserIdAndPeriodTypeAndPeriodValue(studentA.getId(), "MONTHLY", currentMonth).orElseThrow();

        assertThat(allTime.getTotalXp()).isEqualTo(100);
        assertThat(weekly.getTotalXp()).isEqualTo(100);
        assertThat(monthly.getTotalXp()).isEqualTo(100);
    }

    @Test
    @DisplayName("Concurrency: 10 concurrent activities on the same day -> streak increments exactly once")
    void testConcurrentStreakUpdates_SameDay_IncrementsOnlyOnce() throws InterruptedException {
        // Set user streak = 3 from yesterday
        LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        studentA.setCurrentStreak(3);
        studentA.setLastActivityDate(yesterday);
        userRepository.saveAndFlush(studentA);

        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    scoringService.addXp(studentA.getId(), "FLASHCARD_REVIEWED", 10, null, "Ôn tập");
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        // Streak must be exactly 4 (increased by 1, NOT by 10)
        User reloadedUser = userRepository.findById(studentA.getId()).orElseThrow();
        assertThat(reloadedUser.getCurrentStreak()).isEqualTo(4);
        assertThat(reloadedUser.getLastActivityDate()).isEqualTo(LocalDate.now(ZoneOffset.UTC));
    }

    @Test
    @DisplayName("Concurrency: Level promotion crosses threshold -> promotes correctly")
    void testConcurrentLevelPromotion_CrossesThreshold_PromotesCorrectly() throws InterruptedException {
        // Seed 950 XP in ALL_TIME score
        scoreRepository.saveAndFlush(UserScore.builder()
                .user(studentA)
                .periodType("ALL_TIME")
                .periodValue("ALL")
                .totalXp(950)
                .build());

        // 10 concurrent requests of 10 XP each -> +100 XP -> 1050 XP -> Level 2
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    scoringService.addXp(studentA.getId(), "FLASHCARD_REVIEWED", 10, null, "Ôn tập lên level");
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        UserScore allTime = scoreRepository.findByUserIdAndPeriodTypeAndPeriodValue(studentA.getId(), "ALL_TIME", "ALL").orElseThrow();
        assertThat(allTime.getTotalXp()).isEqualTo(1050);

        User reloadedUser = userRepository.findById(studentA.getId()).orElseThrow();
        assertThat(reloadedUser.getLevel()).isEqualTo(2); // (1050 / 1000) + 1 = 2
    }

    @Test
    @DisplayName("Concurrency: Multi-user concurrent scoring -> isolated without deadlock")
    void testMultiUserConcurrentScoring_FullIsolation() throws InterruptedException {
        int threadsPerUser = 10;
        int totalThreads = threadsPerUser * 2;
        ExecutorService executorService = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalThreads);

        AtomicInteger successCount = new AtomicInteger(0);

        // Threads for student A
        for (int i = 0; i < threadsPerUser; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    scoringService.addXp(studentA.getId(), "FLASHCARD_REVIEWED", 10, null, "Ôn tập A");
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Threads for student B
        for (int i = 0; i < threadsPerUser; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    scoringService.addXp(studentB.getId(), "FLASHCARD_REVIEWED", 10, null, "Ôn tập B");
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        assertThat(successCount.get()).isEqualTo(totalThreads);

        UserScore allTimeA = scoreRepository.findByUserIdAndPeriodTypeAndPeriodValue(studentA.getId(), "ALL_TIME", "ALL").orElseThrow();
        UserScore allTimeB = scoreRepository.findByUserIdAndPeriodTypeAndPeriodValue(studentB.getId(), "ALL_TIME", "ALL").orElseThrow();

        assertThat(allTimeA.getTotalXp()).isEqualTo(100);
        assertThat(allTimeB.getTotalXp()).isEqualTo(100);
    }
}
