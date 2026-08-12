package com.japaneselearning.module_auth.service;

import com.japaneselearning.module_auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Run daily at 2:00 AM to clean up expired and revoked refresh tokens
     * to prevent database bloat.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupTokens() {
        log.info("Starting cleanup of expired or revoked refresh tokens...");
        
        try {
            refreshTokenRepository.deleteExpiredOrRevoked(LocalDateTime.now());
            log.info("Successfully cleaned up expired or revoked refresh tokens.");
        } catch (Exception e) {
            log.error("Failed to clean up refresh tokens", e);
        }
    }
}
