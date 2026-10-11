package com.japaneselearning.module_user.service;

import com.japaneselearning.module_user.dto.CurrentUserResponse;
import com.japaneselearning.module_user.dto.UpdateCurrentUserReq;
import com.japaneselearning.module_user.entity.Role;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.RoleName;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User student;

    @BeforeEach
    void setUp() {
        Role studentRole = Role.builder().id(1L).name(RoleName.STUDENT).build();
        student = User.builder()
                .id(1L)
                .email("student@example.com")
                .fullName("Student")
                .phone("0900000000")
                .avatarUrl("https://example.com/avatar.png")
                .country("🇻🇳")
                .isPrivateLeaderboard(false)
                .level(3)
                .currentStreak(7)
                .roles(Set.of(studentRole))
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("student@example.com", "password")
        );
    }

    @Test
    void getCurrentUser_IncludesLeaderboardProfileFields() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));

        CurrentUserResponse result = userService.getCurrentUser();

        assertThat(result.getCountry()).isEqualTo("🇻🇳");
        assertThat(result.getIsPrivateLeaderboard()).isFalse();
        assertThat(result.getLevel()).isEqualTo(3);
        assertThat(result.getCurrentStreak()).isEqualTo(7);
    }

    @Test
    void updateCurrentUser_UpdatesLeaderboardPrivacySettings() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateCurrentUserReq request = new UpdateCurrentUserReq();
        request.setCountry("🇯🇵");
        request.setIsPrivateLeaderboard(true);

        CurrentUserResponse result = userService.updateCurrentUser(request);

        assertThat(result.getCountry()).isEqualTo("🇯🇵");
        assertThat(result.getIsPrivateLeaderboard()).isTrue();
        assertThat(student.getCountry()).isEqualTo("🇯🇵");
        assertThat(student.getIsPrivateLeaderboard()).isTrue();
    }
}
