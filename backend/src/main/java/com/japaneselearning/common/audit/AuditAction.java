package com.japaneselearning.common.audit;

/**
 * Enumeration of sensitive security, administrative, and business actions
 * tracked by the append-only audit trail.
 */
public enum AuditAction {
    // --- Authentication & Session Actions ---
    AUTH_LOGIN,
    AUTH_LOGOUT,
    AUTH_REGISTER,
    AUTH_REFRESH_TOKEN,
    AUTH_TOKEN_REVOKED,
    PASSWORD_CHANGE,

    // --- User & Access Control Actions ---
    USER_LOCK,
    USER_UNLOCK,
    USER_DELETE,
    USER_ROLE_UPDATE,
    USER_PROFILE_UPDATE,

    // --- Course & Curriculum Actions ---
    COURSE_CREATE,
    COURSE_UPDATE,
    COURSE_DELETE,
    COURSE_PUBLISH,
    LESSON_CREATE,
    LESSON_UPDATE,
    LESSON_DELETE,

    // --- Enrollment Actions ---
    COURSE_ENROLLMENT_CREATE,

    // --- Order & Financial Actions ---
    ORDER_CREATE,
    ORDER_STATUS_UPDATE,
    PAYMENT_INITIATED,
    PAYMENT_COMPLETED,
    PAYMENT_CANCELLED,
    PAYMENT_FAILED,
    PAYMENT_WEBHOOK_RECEIVED
}
