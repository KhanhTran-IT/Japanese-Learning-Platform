package com.japaneselearning.module_admin.service;

import com.japaneselearning.common.response.PageResponse;
import com.japaneselearning.module_admin.dto.AdminUserRes;
import com.japaneselearning.module_user.enums.RoleName;
import com.japaneselearning.module_user.enums.UserStatus;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {
    PageResponse<AdminUserRes> getUsers(String keyword, UserStatus status, RoleName role, Pageable pageable);
    AdminUserRes getUserDetail(Long id);
    AdminUserRes lockUser(Long id);
    AdminUserRes unlockUser(Long id);
}
