package com.japaneselearning.module_auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_auth.dto.LoginRequest;
import com.japaneselearning.module_auth.dto.LogoutRequest;
import com.japaneselearning.module_auth.dto.RefreshTokenRequest;
import com.japaneselearning.module_auth.dto.RegisterRequest;
import com.japaneselearning.module_auth.repository.RefreshTokenRepository;
import com.japaneselearning.module_auth.entity.RefreshToken;
import com.japaneselearning.module_auth.util.JwtUtil;
import com.japaneselearning.module_user.entity.Role;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.RoleName;
import com.japaneselearning.module_user.enums.UserStatus;
import com.japaneselearning.module_user.repository.RoleRepository;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtil jwtUtil;

    private User activeStudent;
    private Role studentRole;

    @BeforeEach
    void setUp() {
        studentRole = Role.builder().id(1L).name(RoleName.STUDENT).build();
        activeStudent = User.builder()
                .id(1L)
                .email("student@example.com")
                .passwordHash("hashedpassword")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(studentRole))
                .build();
    }

    @Test
    void register_Success() throws Exception {
        RegisterRequest request = new RegisterRequest("Test User", "new@example.com", "Password123!", "Password123!");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(RoleName.STUDENT)).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(2L);
            return u;
        });

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.email").value("new@example.com"));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() throws Exception {
        RegisterRequest request = new RegisterRequest("Test User", "student@example.com", "Password123!", "Password123!");

        when(userRepository.existsByEmail("student@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.EMAIL_ALREADY_EXISTS.getCode()));
    }

    @Test
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest("student@example.com", "password");

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(activeStudent));
        when(passwordEncoder.matches("password", "hashedpassword")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(activeStudent);
        when(jwtUtil.generateAccessToken(activeStudent)).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(activeStudent)).thenReturn("refresh-token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.accessToken").exists())
                .andExpect(jsonPath("$.result.refreshToken").exists());
    }

    @Test
    void login_InvalidPassword_ThrowsException() throws Exception {
        LoginRequest request = new LoginRequest("student@example.com", "wrongpassword");

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(activeStudent));
        when(passwordEncoder.matches("wrongpassword", "hashedpassword")).thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.LOGIN_FAILED.getCode()));
    }

    @Test
    void refreshToken_Success_ReturnsNewTokens() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        
        RefreshToken mockEntity = RefreshToken.builder()
                .id(1L)
                .token("valid-refresh-token")
                .user(activeStudent)
                .expiredAt(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(mockEntity));
        when(jwtUtil.extractRefreshEmail("valid-refresh-token")).thenReturn("student@example.com");
        when(jwtUtil.isRefreshTokenValid("valid-refresh-token", "student@example.com")).thenReturn(true);
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(activeStudent));
        
        when(jwtUtil.generateAccessToken(activeStudent)).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken(activeStudent)).thenReturn("new-refresh-token");

        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.result.refreshToken").value("new-refresh-token"));
    }

    @Test
    void refreshToken_ExpiredToken_ThrowsException() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("expired-token");
        
        RefreshToken mockEntity = RefreshToken.builder()
                .id(1L)
                .token("expired-token")
                .user(activeStudent)
                .expiredAt(LocalDateTime.now().minusDays(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(mockEntity));

        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.REFRESH_TOKEN_EXPIRED.getCode()));
    }

    @Test
    void refreshToken_RevokedToken_RevokesFamilyAndThrowsException() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("revoked-token");
        
        RefreshToken mockEntity = RefreshToken.builder()
                .id(1L)
                .token("revoked-token")
                .user(activeStudent)
                .expiredAt(LocalDateTime.now().plusDays(1))
                .revoked(true) // Simulating token reuse
                .build();

        when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(mockEntity));

        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.REFRESH_TOKEN_REVOKED.getCode()));
    }

    @Test
    void logout_Idempotent() throws Exception {
        LogoutRequest request = new LogoutRequest("some-token");

        RefreshToken mockEntity = RefreshToken.builder()
                .id(1L)
                .token("some-token")
                .user(activeStudent)
                .revoked(false)
                .build();

        // Simulate token exists
        when(refreshTokenRepository.findByToken("some-token")).thenReturn(Optional.of(mockEntity));

        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Simulate token does NOT exist (already logged out)
        when(refreshTokenRepository.findByToken("some-token")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
