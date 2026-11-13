package com.japaneselearning.common.exception;

import com.japaneselearning.common.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.security.access.AccessDeniedException;

import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers.
 * Converts exceptions to standardized ApiResponse format.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle custom AppException.
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("AppException: code={}, message={}", errorCode.getCode(), e.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(errorCode.getCode(), e.getMessage()));
    }

    /**
     * Handle AccessDeniedException (403 Forbidden).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());

        return ResponseEntity
                .status(ErrorCode.FORBIDDEN_ACCESS.getHttpStatus())
                .body(ApiResponse.error(
                        ErrorCode.FORBIDDEN_ACCESS.getCode(),
                        ErrorCode.FORBIDDEN_ACCESS.getMessage()));
    }

    /**
     * Handle @Valid validation errors on @RequestBody.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        if (message == null || message.isBlank()) {
            message = e.getBindingResult().getAllErrors().stream()
                    .map(org.springframework.context.MessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        }

        log.warn("Validation error: {}", message);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR.getCode(), message));
    }

    /**
     * Handle @Validated constraint violations on path/query params.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        log.warn("Constraint violation: {}", message);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR.getCode(), message));
    }

    /**
     * Handle HandlerMethodValidationException (Spring 6.1+ method parameter validation).
     */
    @ExceptionHandler(org.springframework.web.method.annotation.HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleHandlerMethodValidation(
            org.springframework.web.method.annotation.HandlerMethodValidationException e) {
        String message = e.getAllValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream())
                .map(org.springframework.context.MessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));

        if (message == null || message.isBlank()) {
            message = ErrorCode.VALIDATION_ERROR.getMessage();
        }

        log.warn("Handler method validation error: {}", message);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR.getCode(), message));
    }

    /**
     * Handle HttpMessageNotReadableException (malformed JSON or unparseable body).
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException e) {
        log.warn("Malformed JSON or unreadable request: {}", e.getMessage());

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ErrorCode.INVALID_REQUEST.getCode(), "Dữ liệu yêu cầu không hợp lệ hoặc sai định dạng JSON"));
    }

    /**
     * Handle MethodArgumentTypeMismatchException (type conversion failure on path/query params).
     */
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException e) {
        String message = String.format("Tham số '%s' có giá trị không hợp lệ", e.getName());
        log.warn("Type mismatch: {}", message);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ErrorCode.INVALID_REQUEST.getCode(), message));
    }

    /**
     * Handle MissingServletRequestParameterException.
     */
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(
            org.springframework.web.bind.MissingServletRequestParameterException e) {
        String message = String.format("Thiếu tham số bắt buộc '%s'", e.getParameterName());
        log.warn("Missing parameter: {}", message);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ErrorCode.INVALID_REQUEST.getCode(), message));
    }

    /**
     * Handle MaxUploadSizeExceededException (413 Payload Too Large).
     */
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceeded(
            org.springframework.web.multipart.MaxUploadSizeExceededException e) {
        log.warn("Max upload size exceeded: {}", e.getMessage());

        return ResponseEntity
                .status(org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.error(ErrorCode.INVALID_REQUEST.getCode(), "Kích thước tệp tải lên vượt quá giới hạn cho phép"));
    }

    /**
     * Handle 404 - resource not found.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException e) {
        return ResponseEntity
                .status(ErrorCode.RESOURCE_NOT_FOUND.getHttpStatus())
                .body(ApiResponse.error(
                        ErrorCode.RESOURCE_NOT_FOUND.getCode(),
                        ErrorCode.RESOURCE_NOT_FOUND.getMessage()));
    }

    /**
     * Handle 405 - method not allowed.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException e) {
        return ResponseEntity
                .status(ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus())
                .body(ApiResponse.error(
                        ErrorCode.METHOD_NOT_ALLOWED.getCode(),
                        ErrorCode.METHOD_NOT_ALLOWED.getMessage()));
    }

    /**
     * Catch-all for unhandled exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUncategorized(Exception e) {
        log.error("Uncategorized exception: ", e);

        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.error(
                        ErrorCode.UNCATEGORIZED_EXCEPTION.getCode(),
                        ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage()));
    }
}
