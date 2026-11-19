package com.japaneselearning.module_auth.service;

import com.japaneselearning.module_auth.repository.RefreshTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@Slf4j
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    public TokenCleanupScheduler(RefreshTokenRepository refreshTokenRepository) {
        this(refreshTokenRepository, Clock.systemUTC());
    }

    @Autowired
    public TokenCleanupScheduler(RefreshTokenRepository refreshTokenRepository, Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    /**
     * Run daily at 2:00 AM (or per configured cron) to clean up expired and revoked refresh tokens
     * to prevent database bloat and prune obsolete sessions.
     */
    @Scheduled(cron = "${app.jwt.token-cleanup-cron:0 0 2 * * ?}")
    @Transactional
    public int cleanupTokens() {
        return cleanupExpiredOrRevokedTokens();
    }

    /**
     * Transactional and repeatable cleanup of expired or revoked tokens.
     * Guaranteed not to delete or affect active sessions (where revoked = false AND expiredAt >= now).
     *
     * @return the number of deleted records
     */
    @Transactional
    public int cleanupExpiredOrRevokedTokens() {
        log.info("Starting cleanup of expired or revoked refresh tokens...");
        LocalDateTime now = LocalDateTime.now(clock);
        int deletedCount = refreshTokenRepository.deleteExpiredOrRevoked(now);
        log.info("Successfully cleaned up {} expired or revoked refresh token(s).", deletedCount);
        return deletedCount;
    }
}
