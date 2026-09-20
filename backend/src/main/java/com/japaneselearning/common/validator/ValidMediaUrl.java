package com.japaneselearning.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validates that a string is a safe and well-formed URL.
 * Rejects unsafe protocols (like javascript:, file:) and validates
 * against a trusted domain allowlist if configured.
 */
@Documented
@Constraint(validatedBy = MediaUrlValidator.class)
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface ValidMediaUrl {
    String message() default "Đường dẫn không hợp lệ hoặc không an toàn";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
