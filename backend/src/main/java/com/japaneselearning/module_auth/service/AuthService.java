package com.japaneselearning.module_auth.service;

import com.japaneselearning.module_auth.dto.LoginRequest;
import com.japaneselearning.module_auth.dto.LoginResponse;
import com.japaneselearning.module_auth.dto.LogoutRequest;
import com.japaneselearning.module_auth.dto.RefreshTokenRequest;
import com.japaneselearning.module_auth.dto.RefreshTokenResponse;
import com.japaneselearning.module_auth.dto.RegisterRequest;
import com.japaneselearning.module_auth.dto.RegisterResponse;

/**
 * Auth service contract.
 * Business logic for authentication operations.
 */
public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request);
}
