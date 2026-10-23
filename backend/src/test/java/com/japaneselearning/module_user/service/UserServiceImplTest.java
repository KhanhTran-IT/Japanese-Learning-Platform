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

    @Mock
    private com.japaneselearning.module_auth.service.AuthService authService;

    @InjectMocks
    private UserServiceImpl userService;

    private User student;

    @BeforeEach
    void setUp() {
        Role studentRole = Role.builder().id(1L).name(RoleName.STUDENT).build();
        student = User.builder()
                .id(1L)
                .email("student@example.com")
                .passwordHash("hashedOldPassword")
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

    @Test
    void changePassword_Success_UpdatesPasswordAndRevokesSessions() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("OldPassword123!", "hashedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("hashedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        com.japaneselearning.module_user.dto.ChangePasswordReq req = new com.japaneselearning.module_user.dto.ChangePasswordReq(
                "OldPassword123!",
                "NewPassword123!",
                "NewPassword123!"
        );

        userService.changePassword(req);

        org.mockito.Mockito.verify(userRepository).save(student);
        assertThat(student.getPasswordHash()).isEqualTo("hashedNewPassword");
        org.mockito.Mockito.verify(authService).revokeUserSessions(student.getId());
    }

    @Test
    void changePassword_WrongCurrentPassword_ThrowsAndDoesNotRevokeSessions() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("WrongPassword123!", "hashedOldPassword")).thenReturn(false);

        com.japaneselearning.module_user.dto.ChangePasswordReq req = new com.japaneselearning.module_user.dto.ChangePasswordReq(
                "WrongPassword123!",
                "NewPassword123!",
                "NewPassword123!"
        );

        com.japaneselearning.common.exception.AppException ex = org.junit.jupiter.api.Assertions.assertThrows(
                com.japaneselearning.common.exception.AppException.class,
                () -> userService.changePassword(req)
        );

        assertThat(ex.getErrorCode()).isEqualTo(com.japaneselearning.common.exception.ErrorCode.CURRENT_PASSWORD_INCORRECT);
        org.mockito.Mockito.verify(userRepository, org.mockito.Mockito.never()).save(any());
        org.mockito.Mockito.verify(authService, org.mockito.Mockito.never()).revokeUserSessions(any());
    }

    @Test
    void changePassword_ConfirmMismatch_ThrowsAndDoesNotRevokeSessions() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("OldPassword123!", "hashedOldPassword")).thenReturn(true);

        com.japaneselearning.module_user.dto.ChangePasswordReq req = new com.japaneselearning.module_user.dto.ChangePasswordReq(
                "OldPassword123!",
                "NewPassword123!",
                "DifferentConfirm123!"
        );

        com.japaneselearning.common.exception.AppException ex = org.junit.jupiter.api.Assertions.assertThrows(
                com.japaneselearning.common.exception.AppException.class,
                () -> userService.changePassword(req)
        );

        assertThat(ex.getErrorCode()).isEqualTo(com.japaneselearning.common.exception.ErrorCode.PASSWORD_CONFIRM_NOT_MATCH);
        org.mockito.Mockito.verify(userRepository, org.mockito.Mockito.never()).save(any());
        org.mockito.Mockito.verify(authService, org.mockito.Mockito.never()).revokeUserSessions(any());
    }
}
