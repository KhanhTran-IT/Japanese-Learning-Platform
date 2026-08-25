package com.japaneselearning.module_user.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCurrentUserReq {

    @Size(min = 1, max = 150, message = "Họ tên phải từ 1 đến 150 ký tự")
    private String fullName;

    @Size(max = 30, message = "Số điện thoại tối đa 30 ký tự")
    private String phone;

    @Size(max = 500, message = "Avatar URL tối đa 500 ký tự")
    private String avatarUrl;
}
