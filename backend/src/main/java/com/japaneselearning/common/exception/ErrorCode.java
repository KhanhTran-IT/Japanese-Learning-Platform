package com.japaneselearning.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Centralized error codes for the application.
 * Each error has a unique numeric code, HTTP status, and message.
 *
 * Ranges:
 * - 1xxx: General / System
 * - 2xxx: Auth (future)
 * - 3xxx: User (future)
 * - 4xxx: Course (future)
 * - 5xxx: Payment (future)
 */
@Getter
public enum ErrorCode {

    // ==================== 1xxx: General ====================
    SUCCESS(1000, HttpStatus.OK, "Success"),
    UNCATEGORIZED_EXCEPTION(1001, HttpStatus.INTERNAL_SERVER_ERROR, "Uncategorized error"),
    INVALID_REQUEST(1002, HttpStatus.BAD_REQUEST, "Invalid request"),
    RESOURCE_NOT_FOUND(1003, HttpStatus.NOT_FOUND, "Resource not found"),
    METHOD_NOT_ALLOWED(1004, HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed"),
    VALIDATION_ERROR(1005, HttpStatus.BAD_REQUEST, "Validation error"),

    // ==================== 2xxx: Auth ====================
    EMAIL_ALREADY_EXISTS(2001, HttpStatus.CONFLICT, "Email đã tồn tại"),
    LOGIN_FAILED(2002, HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng"),
    ACCOUNT_LOCKED(2003, HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa"),
    INVALID_REFRESH_TOKEN(2006, HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ"),
    REFRESH_TOKEN_EXPIRED(2007, HttpStatus.UNAUTHORIZED, "Refresh token đã hết hạn"),
    REFRESH_TOKEN_REVOKED(2008, HttpStatus.UNAUTHORIZED, "Refresh token đã bị thu hồi"),
    PASSWORD_CONFIRM_NOT_MATCH(2010, HttpStatus.BAD_REQUEST, "Mật khẩu xác nhận không khớp"),

    // ==================== 3xxx: User/Role ====================
    ROLE_NOT_FOUND(3002, HttpStatus.INTERNAL_SERVER_ERROR, "Không tìm thấy role"),
    ;

    private final int code;
    private final HttpStatusCode httpStatus;
    private final String message;

    ErrorCode(int code, HttpStatusCode httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
