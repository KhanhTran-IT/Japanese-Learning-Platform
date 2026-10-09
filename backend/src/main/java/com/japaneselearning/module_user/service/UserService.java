package com.japaneselearning.module_user.service;

import com.japaneselearning.module_user.dto.ChangePasswordReq;
import com.japaneselearning.module_user.dto.CurrentUserResponse;
import com.japaneselearning.module_user.dto.UpdateCurrentUserReq;

public interface UserService {
    CurrentUserResponse getCurrentUser();
    CurrentUserResponse updateCurrentUser(UpdateCurrentUserReq request);
    void changePassword(ChangePasswordReq request);
}
