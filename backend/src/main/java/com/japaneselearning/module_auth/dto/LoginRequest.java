package com.japaneselearning.module_auth.dto;

import jakarta.validation.constraints.Email;
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
public class LoginRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 150, message = "Email không được vượt quá 150 ký tự")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(max = 100, message = "Mật khẩu không được vượt quá 100 ký tự")
    private String password;
}
