package com.japaneselearning.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Global Date and Time Configuration establishing canonical system time and timezone rules.
 * <p>
 * Architectural Invariants:
 * 1. Canonical UTC: All internal timestamps, database persistence (created_at, updated_at, expires_at),
 *    refresh tokens, quiz session deadlines, audit events, and distributed scheduler cutoffs MUST be
 *    evaluated against UTC ({@link ZoneOffset#UTC} or {@link Clock#systemUTC()}).
 * 2. Business-Local VNPay Timezone: VNPay payment provider explicitly requires create and expire
 *    timestamps (vnp_CreateDate, vnp_ExpireDate) to be formatted in Vietnam timezone
 *    ({@code Asia/Ho_Chi_Minh}, GMT+7).
 * 3. User-Local Flashcard SRS: Flashcard Spaced Repetition System evaluates next due date based on
 *    user-configured timezone rolling at start of day, converted deterministically to/from canonical UTC.
 * 4. Deterministic Testing: All time-sensitive services MUST accept an injectable {@link Clock} bean,
 *    allowing integration and boundary tests to shift time (DST transitions, leap years, month-end rollovers,
 *    and expiration deadlines) deterministically without Thread.sleep or OS clock manipulation.
 */
@Configuration
public class DateTimeConfig {

    /**
     * Canonical system timezone (UTC) for persistence, audit logs, and internal timestamps.
     */
    public static final ZoneId ZONE_UTC = ZoneOffset.UTC;

    /**
     * Provider-required business-local timezone for VNPay payment gateway (GMT+7 / Asia/Ho_Chi_Minh).
     */
    public static final ZoneId ZONE_VNPAY = ZoneId.of("Asia/Ho_Chi_Minh");

    /**
     * Primary Clock bean defaulting to UTC.
     *
     * @return a system UTC clock
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
