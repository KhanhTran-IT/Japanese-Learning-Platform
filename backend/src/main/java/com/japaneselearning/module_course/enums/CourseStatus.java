package com.japaneselearning.module_course.enums;

public enum CourseStatus {
    DRAFT,
    PUBLISHED,
    HIDDEN,
    ARCHIVED;

    public boolean canTransitionTo(CourseStatus target) {
        if (target == null) {
            return false;
        }
        if (this == target) {
            return this != ARCHIVED;
        }
        return switch (this) {
            case DRAFT -> target == PUBLISHED || target == ARCHIVED;
            case PUBLISHED -> target == HIDDEN || target == ARCHIVED;
            case HIDDEN -> target == PUBLISHED || target == ARCHIVED;
            case ARCHIVED -> false;
        };
    }
}
