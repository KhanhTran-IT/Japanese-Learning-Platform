package com.japaneselearning.module_auth.service;

import com.japaneselearning.module_auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenCleanupSchedulerTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenCleanupScheduler scheduler;

    @Test
    void cleanupTokens_CallsCleanupAndReturnsDeletedCount() {
        when(refreshTokenRepository.deleteExpiredOrRevoked(any(LocalDateTime.class))).thenReturn(7);

        int count = scheduler.cleanupTokens();

        assertThat(count).isEqualTo(7);
        verify(refreshTokenRepository).deleteExpiredOrRevoked(any(LocalDateTime.class));
    }

    @Test
    void cleanupExpiredOrRevokedTokens_RepeatableWhenNoTokensLeft() {
        when(refreshTokenRepository.deleteExpiredOrRevoked(any(LocalDateTime.class))).thenReturn(0);

        int count = scheduler.cleanupExpiredOrRevokedTokens();

        assertThat(count).isEqualTo(0);
        verify(refreshTokenRepository).deleteExpiredOrRevoked(any(LocalDateTime.class));
    }

    @Test
    void cleanupExpiredOrRevokedTokens_PropagatesExceptionForTransactionRollback() {
        when(refreshTokenRepository.deleteExpiredOrRevoked(any(LocalDateTime.class)))
                .thenThrow(new DataIntegrityViolationException("Database locked"));

        assertThrows(DataIntegrityViolationException.class, () -> scheduler.cleanupExpiredOrRevokedTokens());

        verify(refreshTokenRepository).deleteExpiredOrRevoked(any(LocalDateTime.class));
    }
}
