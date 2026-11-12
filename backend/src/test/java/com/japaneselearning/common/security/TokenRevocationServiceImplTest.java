package com.japaneselearning.common.security;

import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.UserStatus;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenRevocationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private TokenRevocationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TokenRevocationServiceImpl(userRepository);
        ReflectionTestUtils.setField(service, "redisTemplate", redisTemplate);
        ReflectionTestUtils.setField(service, "cacheTtlSeconds", 60L);
    }

    @Test
    @DisplayName("isTokenActive: Returns false when userId is null")
    void isTokenActive_NullUserId_ReturnsFalse() {
        assertThat(service.isTokenActive(null, 1, "test@example.com")).isFalse();
    }

    @Test
    @DisplayName("isTokenActive: Returns true when token version matches and user is ACTIVE")
    void isTokenActive_ValidActiveUser_ReturnsTrue() {
        User user = User.builder()
                .id(1L)
                .email("student@example.com")
                .status(UserStatus.ACTIVE)
                .tokenVersion(1)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        boolean active = service.isTokenActive(1L, 1, "student@example.com");
        assertThat(active).isTrue();

        // Second call should hit L1 local cache and not call userRepository again
        boolean activeSecond = service.isTokenActive(1L, 1, "student@example.com");
        assertThat(activeSecond).isTrue();
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("isTokenActive: Returns false when user status is LOCKED or DELETED")
    void isTokenActive_LockedOrDeletedUser_ReturnsFalse() {
        User lockedUser = User.builder()
                .id(2L)
                .email("locked@example.com")
                .status(UserStatus.LOCKED)
                .tokenVersion(1)
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(lockedUser));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        assertThat(service.isTokenActive(2L, 1, "locked@example.com")).isFalse();
    }

    @Test
    @DisplayName("isTokenActive: Returns false when token version in claim is older than user version")
    void isTokenActive_ObsoleteTokenVersion_ReturnsFalse() {
        User user = User.builder()
                .id(3L)
                .email("student@example.com")
                .status(UserStatus.ACTIVE)
                .tokenVersion(2)
                .build();

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Claimed version 1 < current version 2
        assertThat(service.isTokenActive(3L, 1, "student@example.com")).isFalse();
        // Claimed version 2 >= current version 2
        assertThat(service.isTokenActive(3L, 2, "student@example.com")).isTrue();
    }

    @Test
    @DisplayName("revokeUserTokens & evict: Clears local cache and Redis keys")
    void revokeUserTokens_ClearsCaches() {
        service.revokeUserTokens(5L);

        verify(redisTemplate).delete("auth:user:version:5");
        verify(redisTemplate).delete("auth:user:status:5");
    }

    @Test
    @DisplayName("isTokenActive: Fallback to DB when Redis is down or unavailable")
    void isTokenActive_RedisDown_FallsBackToDb() {
        User user = User.builder()
                .id(4L)
                .email("test@example.com")
                .status(UserStatus.ACTIVE)
                .tokenVersion(1)
                .build();

        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis connection error"));
        when(userRepository.findById(4L)).thenReturn(Optional.of(user));

        boolean active = service.isTokenActive(4L, 1, "test@example.com");
        assertThat(active).isTrue();
    }
}
