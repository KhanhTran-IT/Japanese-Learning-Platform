package com.japaneselearning.module_payment.scheduler;

import com.japaneselearning.module_payment.policy.PaymentRetentionPolicy;
import com.japaneselearning.module_payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled background job executing the payment data retention policy.
 * Prunes technical raw gateway responses older than the configured threshold (default 90 days),
 * preserving statutory financial accounting records.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRetentionScheduler {

    private final PaymentService paymentService;

    @Value("${app.payment.retention.raw-payload-days:90}")
    private int rawPayloadRetentionDays = PaymentRetentionPolicy.DEFAULT_RAW_PAYLOAD_RETENTION_DAYS;

    /**
     * Daily background job running at 3:00 AM (or per configured cron) to purge expired payment raw payloads.
     */
    @Scheduled(cron = "${app.payment.retention.cron:0 0 3 * * ?}")
    public int cleanupExpiredPaymentPayloads() {
        log.debug("Starting scheduled cleanup of expired payment raw responses...");
        return paymentService.purgeExpiredPaymentPayloads(rawPayloadRetentionDays);
    }
}
