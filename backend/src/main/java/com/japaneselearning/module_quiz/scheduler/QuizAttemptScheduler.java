package com.japaneselearning.module_quiz.scheduler;

import com.japaneselearning.module_quiz.entity.QuizAttempt;
import com.japaneselearning.module_quiz.enums.QuizAttemptStatus;
import com.japaneselearning.module_quiz.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizAttemptScheduler {

    private final QuizAttemptRepository quizAttemptRepository;

    @Value("${app.quiz.grace-period-minutes:5}")
    private int gracePeriodMinutes;

    /**
     * Chạy mỗi phút một lần để kiểm tra các bài làm đang diễn ra có quá hạn không.
     * Cấu hình cron: Giây 0 của mỗi phút (0 * * * * *)
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void expireStaleQuizAttempts() {
        log.debug("Bắt đầu quét các bài làm quiz hết hạn...");
        
        List<QuizAttempt> inProgressTimedAttempts = quizAttemptRepository.findByStatusAndQuizTimeLimitMinutesIsNotNull(QuizAttemptStatus.IN_PROGRESS);
        
        LocalDateTime now = LocalDateTime.now();
        List<QuizAttempt> expiredAttempts = new ArrayList<>();
        
        for (QuizAttempt attempt : inProgressTimedAttempts) {
            Integer timeLimit = attempt.getQuiz().getTimeLimitMinutes();
            if (timeLimit != null) {
                LocalDateTime deadline = attempt.getStartedAt()
                        .plusMinutes(timeLimit)
                        .plusMinutes(gracePeriodMinutes);
                        
                if (now.isAfter(deadline)) {
                    attempt.setStatus(QuizAttemptStatus.EXPIRED);
                    expiredAttempts.add(attempt);
                }
            }
        }
        
        if (!expiredAttempts.isEmpty()) {
            quizAttemptRepository.saveAll(expiredAttempts);
            log.info("Đã đánh dấu {} bài làm quiz thành EXPIRED do quá hạn.", expiredAttempts.size());
        } else {
            log.debug("Không tìm thấy bài làm quiz nào quá hạn.");
        }
    }
}
