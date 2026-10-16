package com.japaneselearning.module_leaderboard.entity;

import com.japaneselearning.module_user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_scores", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "period_type", "period_value"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "period_type", nullable = false, length = 20)
    private String periodType; // ALL_TIME, WEEKLY, MONTHLY

    @Column(name = "period_value", nullable = false, length = 20)
    private String periodValue; // ALL, 2026-W41, 2026-10

    @Builder.Default
    @Column(name = "total_xp", nullable = false)
    private Integer totalXp = 0;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
