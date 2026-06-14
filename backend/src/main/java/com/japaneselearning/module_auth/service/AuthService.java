package com.japaneselearning.module_auth.service;

import com.japaneselearning.module_auth.dto.RegisterRequest;
import com.japaneselearning.module_auth.dto.RegisterResponse;

/**
 * Auth service contract.
 * Business logic for authentication operations.
 */
public interface AuthService {

    RegisterResponse register(RegisterRequest request);
}
