package com.japaneselearning.module_admin.dto;

import com.japaneselearning.module_user.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserRes {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private UserStatus status;
    private Boolean emailVerified;
    private List<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
