package com.japaneselearning.module_user.enums;

/**
 * Account status for User entity.
 */
public enum UserStatus {
    ACTIVE,     // Tài khoản hoạt động bình thường
    INACTIVE,   // Tài khoản chưa hoạt động hoặc tạm ẩn
    LOCKED,     // Tài khoản bị khóa
    DELETED     // Tài khoản đã xóa mềm
}
