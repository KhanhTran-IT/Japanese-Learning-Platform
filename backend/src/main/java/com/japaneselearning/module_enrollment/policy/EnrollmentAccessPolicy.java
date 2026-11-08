package com.japaneselearning.module_enrollment.policy;

import com.japaneselearning.module_enrollment.entity.CourseEnrollment;
import com.japaneselearning.module_enrollment.enums.EnrollmentStatus;

import java.util.Set;

/**
 * Shared enrollment access policy across public course details, student dashboard,
 * learning flow, quizzes, orders, and payment fulfillment.
 */
public final class EnrollmentAccessPolicy {

    private EnrollmentAccessPolicy() {}

    /**
     * The set of enrollment statuses that grant active course access,
     * dashboard visibility, quiz attempts, and mark course as enrolled.
     */
    public static final Set<EnrollmentStatus> ACCESSIBLE_STATUSES = Set.of(
            EnrollmentStatus.ACTIVE,
            EnrollmentStatus.COMPLETED
    );

    /**
     * Determines whether the given status grants active course access.
     */
    public static boolean hasAccess(EnrollmentStatus status) {
        return status != null && ACCESSIBLE_STATUSES.contains(status);
    }

    /**
     * Determines whether the given enrollment record grants active course access.
     */
    public static boolean hasAccess(CourseEnrollment enrollment) {
        return enrollment != null && hasAccess(enrollment.getStatus());
    }

    /**
     * Determines whether a student with this status is considered already enrolled,
     * preventing duplicate orders or re-enrollment.
     */
    public static boolean isAlreadyEnrolled(EnrollmentStatus status) {
        return hasAccess(status);
    }
}
