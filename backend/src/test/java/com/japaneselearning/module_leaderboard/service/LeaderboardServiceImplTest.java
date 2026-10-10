package com.japaneselearning.module_leaderboard.service;

import com.japaneselearning.module_leaderboard.dto.LeaderboardRes;
import com.japaneselearning.module_leaderboard.dto.LeaderboardUserRes;
import com.japaneselearning.module_leaderboard.entity.UserScore;
import com.japaneselearning.module_leaderboard.repository.UserScoreRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceImplTest {

    @Mock
    private UserScoreRepository scoreRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LeaderboardServiceImpl leaderboardService;

    private User testUser;
    private User otherUser1;
    private User otherUser2;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .isPrivateLeaderboard(false)
                .build();

        otherUser1 = User.builder()
                .id(2L)
                .email("user1@example.com")
                .fullName("User One")
                .isPrivateLeaderboard(false)
                .build();

        otherUser2 = User.builder()
                .id(3L)
                .email("user2@example.com")
                .fullName("User Two")
                .isPrivateLeaderboard(false)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@example.com", "password")
        );
    }

    @Test
    void getLeaderboard_ShouldReturnCorrectRankingAndMyRank() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        UserScore score1 = UserScore.builder().user(otherUser1).totalXp(300).build();
        UserScore score2 = UserScore.builder().user(testUser).totalXp(200).build();
        UserScore score3 = UserScore.builder().user(otherUser2).totalXp(100).build();

        Page<UserScore> pageResult = new PageImpl<>(List.of(score1, score2, score3));
        
        when(scoreRepository.findTopByPeriod(eq("ALL_TIME"), eq("ALL"), any(PageRequest.class)))
                .thenReturn(pageResult);

        when(scoreRepository.getRankByUserIdAndPeriod(eq(1L), eq("ALL_TIME"), eq("ALL")))
                .thenReturn(2L);
        when(scoreRepository.findByUserIdAndPeriodTypeAndPeriodValue(eq(1L), eq("ALL_TIME"), eq("ALL")))
                .thenReturn(Optional.of(score2));

        // Act
        LeaderboardRes result = leaderboardService.getLeaderboard("ALL_TIME", 0, 50);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRankings()).hasSize(3);
        
        LeaderboardUserRes rank1 = result.getRankings().get(0);
        assertThat(rank1.getRank()).isEqualTo(1L);
        assertThat(rank1.getName()).isEqualTo("User One");
        assertThat(rank1.getXp()).isEqualTo(300);
        assertThat(rank1.getBadge()).isEqualTo("🥇");
        
        LeaderboardUserRes rank2 = result.getRankings().get(1);
        assertThat(rank2.getRank()).isEqualTo(2L);
        assertThat(rank2.getName()).isEqualTo("Test User");
        assertThat(rank2.getXp()).isEqualTo(200);
        assertThat(rank2.getIsMe()).isTrue();

        assertThat(result.getCurrentUserRanking()).isNotNull();
        assertThat(result.getCurrentUserRanking().getRank()).isEqualTo(2L);
        assertThat(result.getCurrentUserRanking().getName()).isEqualTo("Test User");
    }

    @Test
    void getLeaderboard_WhenPrivate_ShouldNotReturnMyRanking() {
        // Arrange
        testUser.setIsPrivateLeaderboard(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        UserScore score1 = UserScore.builder().user(otherUser1).totalXp(300).build();

        Page<UserScore> pageResult = new PageImpl<>(List.of(score1));
        
        when(scoreRepository.findTopByPeriod(eq("WEEKLY"), any(String.class), any(PageRequest.class)))
                .thenReturn(pageResult);

        // Act
        LeaderboardRes result = leaderboardService.getLeaderboard("WEEKLY", 0, 50);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRankings()).hasSize(1);
        assertThat(result.getCurrentUserRanking()).isNull();
    }
}
