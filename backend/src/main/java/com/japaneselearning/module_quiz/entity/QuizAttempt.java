package com.japaneselearning.module_quiz.entity;

import com.japaneselearning.module_quiz.enums.QuizAttemptStatus;
import com.japaneselearning.module_user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime submittedAt;

    @Builder.Default
    @Column(precision = 6, scale = 2)
    private BigDecimal score = BigDecimal.ZERO;

    @Builder.Default
    private Integer totalQuestions = 0;

    @Builder.Default
    private Integer correctCount = 0;

    @Builder.Default
    private Integer wrongCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean passed = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private QuizAttemptStatus status = QuizAttemptStatus.IN_PROGRESS;
}
