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
    UNAUTHENTICATED(1006, HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập để tiếp tục"),

    // ==================== 2xxx: Auth ====================
    EMAIL_ALREADY_EXISTS(2001, HttpStatus.CONFLICT, "Email đã tồn tại"),
    LOGIN_FAILED(2002, HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng"),
    ACCOUNT_LOCKED(2003, HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa"),
    INVALID_ACCESS_TOKEN(2004, HttpStatus.UNAUTHORIZED, "Access token không hợp lệ"),
    ACCESS_TOKEN_EXPIRED(2005, HttpStatus.UNAUTHORIZED, "Access token đã hết hạn"),
    INVALID_REFRESH_TOKEN(2006, HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ"),
    REFRESH_TOKEN_EXPIRED(2007, HttpStatus.UNAUTHORIZED, "Refresh token đã hết hạn"),
    REFRESH_TOKEN_REVOKED(2008, HttpStatus.UNAUTHORIZED, "Refresh token đã bị thu hồi"),
    TOO_MANY_REQUESTS(2009, HttpStatus.TOO_MANY_REQUESTS, "Quá nhiều yêu cầu, vui lòng thử lại sau"),
    PASSWORD_CONFIRM_NOT_MATCH(2010, HttpStatus.BAD_REQUEST, "Mật khẩu xác nhận không khớp"),
    CURRENT_PASSWORD_INCORRECT(2011, HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không đúng"),

    // ==================== 3xxx: User/Role ====================
    USER_NOT_FOUND(3001, HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"),
    ROLE_NOT_FOUND(3002, HttpStatus.INTERNAL_SERVER_ERROR, "Không tìm thấy role"),
    FORBIDDEN_ACCESS(3003, HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập vào tính năng này"),
    DATA_ISOLATION_FORBIDDEN(3004, HttpStatus.FORBIDDEN, "Bạn không có quyền chỉnh sửa dữ liệu của người khác"),

    // ==================== 4xxx: Course ====================
    COURSE_NOT_FOUND(4001, HttpStatus.NOT_FOUND, "Không tìm thấy khóa học"),
    COURSE_SLUG_EXISTS(4002, HttpStatus.CONFLICT, "Đường dẫn (slug) khóa học đã tồn tại"),
    SECTION_NOT_FOUND(4003, HttpStatus.NOT_FOUND, "Không tìm thấy chương học"),
    SECTION_HAS_LESSONS(4004, HttpStatus.BAD_REQUEST, "Không thể xóa chương học đang chứa bài học"),
    LESSON_NOT_FOUND(4005, HttpStatus.NOT_FOUND, "Không tìm thấy bài học"),
    LESSON_SLUG_EXISTS(4006, HttpStatus.CONFLICT, "Đường dẫn (slug) bài học đã tồn tại trong khóa học này"),
    COURSE_NOT_AVAILABLE_FOR_ENROLLMENT(4007, HttpStatus.BAD_REQUEST, "Khóa học chưa được phát hành hoặc không thể ghi danh lúc này"),
    COURSE_CANNOT_ENROLL_PAID(4008, HttpStatus.BAD_REQUEST, "Không thể ghi danh trực tiếp vào khóa học trả phí"),
    USER_ALREADY_ENROLLED(4009, HttpStatus.CONFLICT, "Bạn đã ghi danh khóa học này rồi"),
    COURSE_CANNOT_PUBLISH_EMPTY(4010, HttpStatus.BAD_REQUEST, "Không thể xuất bản khóa học chưa có bài học nào"),
    QUIZ_NOT_FOUND(4011, HttpStatus.NOT_FOUND, "Không tìm thấy bài tập/quiz"),
    QUESTION_NOT_FOUND(4012, HttpStatus.NOT_FOUND, "Không tìm thấy câu hỏi"),
    ANSWER_NOT_FOUND(4013, HttpStatus.NOT_FOUND, "Không tìm thấy đáp án"),
    QUIZ_PUBLISH_NO_QUESTION(4014, HttpStatus.BAD_REQUEST, "Không thể xuất bản quiz chưa có câu hỏi nào"),
    QUIZ_HAS_ATTEMPT(4015, HttpStatus.BAD_REQUEST, "Không thể xóa quiz đã có học viên làm bài"),
    QUESTION_HAS_ATTEMPT(4016, HttpStatus.BAD_REQUEST, "Không thể sửa/xóa câu hỏi đã có học viên làm bài"),
    QUIZ_NOT_PUBLISHED(4017, HttpStatus.BAD_REQUEST, "Quiz chưa được xuất bản"),
    QUIZ_MAX_ATTEMPTS_REACHED(4018, HttpStatus.BAD_REQUEST, "Bạn đã hết số lần làm bài cho quiz này"),
    QUIZ_ATTEMPT_NOT_FOUND(4019, HttpStatus.NOT_FOUND, "Không tìm thấy phiên làm bài"),
    QUIZ_ATTEMPT_ALREADY_SUBMITTED(4020, HttpStatus.BAD_REQUEST, "Phiên làm bài đã được nộp rồi"),
    QUIZ_ATTEMPT_FORBIDDEN(4021, HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập phiên làm bài này"),
    QUIZ_UNSUPPORTED_QUESTION_TYPE(4022, HttpStatus.BAD_REQUEST, "Loại câu hỏi này chưa được hỗ trợ chấm điểm tự động"),
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
