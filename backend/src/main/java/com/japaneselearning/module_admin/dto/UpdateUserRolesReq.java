package com.japaneselearning.module_admin.dto;

import com.japaneselearning.module_user.enums.RoleName;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRolesReq {

    @NotEmpty(message = "Danh sách role không được để trống")
    @Size(max = 10, message = "Danh sách role không được vượt quá 10 phần tử")
    private Set<RoleName> roles;
}
