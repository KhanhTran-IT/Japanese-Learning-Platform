package com.japaneselearning.module_auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {

    @NotBlank(message = "Refresh token không được để trống")
    @Size(max = 2048, message = "Refresh token không được vượt quá 2048 ký tự")
    private String refreshToken;
}
