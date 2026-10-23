package com.japaneselearning.module_admin.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_admin.dto.AdminUserRes;
import com.japaneselearning.module_auth.service.AuthService;
import com.japaneselearning.module_user.entity.Role;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.RoleName;
import com.japaneselearning.module_user.enums.UserStatus;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private User targetStudent;
    private User superAdminUser;

    @BeforeEach
    void setUp() {
        Role studentRole = Role.builder().id(1L).name(RoleName.STUDENT).build();
        Role superAdminRole = Role.builder().id(2L).name(RoleName.SUPER_ADMIN).build();

        targetStudent = User.builder()
                .id(100L)
                .email("student100@example.com")
                .fullName("Student 100")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(studentRole))
                .build();

        superAdminUser = User.builder()
                .id(1L)
                .email("superadmin@example.com")
                .fullName("Super Admin")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(superAdminRole))
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@example.com", "password")
        );
    }

    @Test
    void lockUser_Success_SetsLockedStatusAndRevokesActiveSessions() {
        when(userRepository.findById(100L)).thenReturn(Optional.of(targetStudent));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminUserRes response = adminUserService.lockUser(100L);

        assertThat(response.getStatus()).isEqualTo(UserStatus.LOCKED);
        assertThat(targetStudent.getStatus()).isEqualTo(UserStatus.LOCKED);

        verify(userRepository).save(targetStudent);
        verify(authService).revokeUserSessions(100L);
    }

    @Test
    void lockUser_CannotLockSelf_ThrowsExceptionAndDoesNotRevoke() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("student100@example.com", "password")
        );
        when(userRepository.findById(100L)).thenReturn(Optional.of(targetStudent));

        AppException ex = assertThrows(AppException.class, () -> adminUserService.lockUser(100L));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
        assertThat(ex.getMessage()).contains("Không thể khóa tài khoản của chính mình");

        verify(userRepository, never()).save(any());
        verify(authService, never()).revokeUserSessions(any());
    }

    @Test
    void lockUser_CannotLockSuperAdmin_ThrowsExceptionAndDoesNotRevoke() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(superAdminUser));

        AppException ex = assertThrows(AppException.class, () -> adminUserService.lockUser(1L));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
        assertThat(ex.getMessage()).contains("Không thể khóa tài khoản SUPER_ADMIN");

        verify(userRepository, never()).save(any());
        verify(authService, never()).revokeUserSessions(any());
    }

    @Test
    void lockUser_UserNotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> adminUserService.lockUser(999L));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(authService, never()).revokeUserSessions(any());
    }

    @Test
    void unlockUser_Success_SetsActiveStatusWithoutRevoking() {
        targetStudent.setStatus(UserStatus.LOCKED);
        when(userRepository.findById(100L)).thenReturn(Optional.of(targetStudent));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminUserRes response = adminUserService.unlockUser(100L);

        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(targetStudent.getStatus()).isEqualTo(UserStatus.ACTIVE);

        verify(userRepository).save(targetStudent);
        // Unlock does not touch sessions
        verify(authService, never()).revokeUserSessions(any());
    }
}
