package com.japaneselearning.module_payment.policy;

import java.time.Duration;
import java.time.Period;
import java.util.Set;

/**
 * Retention policy for payment data and reconciliation payloads.
 *
 * Requirements & Standards:
 * 1. Statutory Accounting Retention: Core financial transaction records
 *    (order ID, amount, currency, transaction code, payment status, paid timestamp)
 *    must be retained for 7 years to comply with statutory accounting and tax regulations.
 * 2. Data Minimization & Privacy: Raw provider callback responses and reconciliation payloads
 *    are retained for a maximum of 90 days (sufficient for chargeback/dispute resolution and monthly reconciliation).
 *    After 90 days, raw payloads are purged to prevent unnecessary data accumulation.
 * 3. Sanitization: All stored raw payloads are sanitized at ingestion time to redact cryptographic hashes,
 *    merchant credentials, client IP addresses, and personal customer data.
 */
public final class PaymentRetentionPolicy {

    /**
     * Statutory financial record retention period: 7 years.
     */
    public static final Period FINANCIAL_RECORD_RETENTION = Period.ofYears(7);

    /**
     * Technical raw response / reconciliation payload retention period: 90 days.
     */
    public static final Duration RAW_PAYLOAD_RETENTION = Duration.ofDays(90);

    /**
     * Default number of days to retain raw payment responses before purging.
     */
    public static final int DEFAULT_RAW_PAYLOAD_RETENTION_DAYS = 90;

    /**
     * Sentinel marker replacing purged raw response payloads.
     */
    public static final String PURGED_PAYLOAD_MARKER = "[PURGED_BY_RETENTION_POLICY]";

    /**
     * Whitelisted metadata fields required for reconciliation and customer support.
     */
    public static final Set<String> ALLOWED_RECONCILIATION_METADATA_KEYS = Set.of(
            "orderCode",
            "transactionNo",
            "amount",
            "bankCode",
            "bankTranNo",
            "cardType",
            "payDate",
            "responseCode",
            "transactionStatus",
            "orderInfo"
    );

    /**
     * Forbidden sensitive keys that must never be stored in payment records, audit logs, or system logs.
     */
    public static final Set<String> SENSITIVE_FORBIDDEN_KEYS = Set.of(
            "vnp_SecureHash",
            "vnp_SecureHashType",
            "vnp_HashSecret",
            "vnp_TmnCode",
            "vnp_IpAddr",
            "hashSecret",
            "secureHash",
            "signature",
            "secretKey",
            "clientIp",
            "ipAddr"
    );

    private PaymentRetentionPolicy() {}
}
