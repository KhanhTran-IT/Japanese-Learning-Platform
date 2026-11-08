package com.japaneselearning.module_enrollment.enums;

public enum EnrollmentStatus {
    ACTIVE,
    PAUSED,
    COMPLETED,
    CANCELLED;

    /**
     * Helper to verify if this status grants active learning access.
     */
    public boolean hasAccess() {
        return this == ACTIVE || this == COMPLETED;
    }
}
