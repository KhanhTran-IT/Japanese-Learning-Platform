package com.japaneselearning.module_quiz.scheduler;

import com.japaneselearning.module_quiz.enums.QuizAttemptStatus;
import com.japaneselearning.module_quiz.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizAttemptScheduler {

    private final QuizAttemptRepository quizAttemptRepository;

    @Value("${app.quiz.grace-period-minutes:5}")
    private int gracePeriodMinutes = 5;

    @Value("${app.quiz.expiration-batch-size:100}")
    private int batchSize = 100;

    @Value("${app.quiz.expiration-max-batches:10}")
    private int maxBatches = 10;

    /**
     * Periodically reaps stale/abandoned in-progress quiz attempts whose expiration deadline
     * plus grace period has elapsed.
     * <p>
     * Implementation characteristics:
     * 1. Indexed lookup: Uses index on (status, expires_at) without scanning quiz tables or untimed quizzes.
     * 2. Bounded batching: Processes at most {@code maxBatches} pages of {@code batchSize} records per run.
     * 3. Multi-instance concurrency safe: Atomic conditional update {@code WHERE id IN :ids AND status = 'IN_PROGRESS'}
     *    guarantees that even if multiple application nodes query overlapping candidate IDs simultaneously,
     *    each attempt is transitioned by exactly one node with no dirty writes or lost updates.
     * 4. Transactional: Runs in a transactional context.
     *
     * @return the total number of quiz attempts transitioned to EXPIRED in this execution
     */
    @Scheduled(cron = "${app.quiz.expiration-cron:0 * * * * *}")
    @Transactional
    public int expireStaleQuizAttempts() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(gracePeriodMinutes);
        int effectiveBatchSize = Math.max(1, Math.min(batchSize, 500));
        int effectiveMaxBatches = Math.max(1, maxBatches);
        int totalExpired = 0;

        for (int batch = 0; batch < effectiveMaxBatches; batch++) {
            List<Long> expiredIds = quizAttemptRepository.findExpiredAttemptIds(
                    QuizAttemptStatus.IN_PROGRESS,
                    cutoff,
                    PageRequest.of(0, effectiveBatchSize)
            );

            if (expiredIds.isEmpty()) {
                break;
            }

            int updated = quizAttemptRepository.markAttemptsExpiredByIds(
                    expiredIds,
                    QuizAttemptStatus.EXPIRED,
                    QuizAttemptStatus.IN_PROGRESS
            );
            totalExpired += updated;

            if (expiredIds.size() < effectiveBatchSize) {
                break;
            }
        }

        if (totalExpired > 0) {
            log.info("Marked {} stale quiz attempt(s) as EXPIRED.", totalExpired);
        } else {
            log.debug("No stale quiz attempts found to expire.");
        }

        return totalExpired;
    }
}
