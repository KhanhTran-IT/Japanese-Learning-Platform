# CURRENT TASK

## Task hiện tại

Refresh Token API + Logout API

## Trạng thái

DONE
Ngày hoàn thành: 16/06/2026

## Mục tiêu

Xây dựng API refresh token và logout cho hệ thống Auth. Refresh Token API cho phép user lấy access token mới khi access token hết hạn. Logout API cho phép user thu hồi refresh token hiện tại để kết thúc phiên đăng nhập.

## Vì sao làm task này?

Sau Login API, hệ thống đã tạo access token và refresh token. Tuy nhiên nếu access token hết hạn, user cần cách lấy access token mới mà không phải đăng nhập lại. Đồng thời khi user logout, hệ thống cần revoke refresh token để token đó không thể tiếp tục được dùng.

Task này hoàn thiện vòng đời cơ bản của authentication:

```text
Register
→ Login
→ Access protected APIs
→ Refresh access token
→ Logout / revoke refresh token
```

## Không làm trong task này

* Không làm GET /api/users/me
* Không làm Spring Security JWT Filter đầy đủ nếu chưa cần
* Không làm frontend
* Không làm Course
* Không làm Lesson
* Không làm Payment
* Không làm Quiz
* Không làm email verification
* Không làm forgot password
* Không làm multi-device session management nâng cao

## File tài liệu cần dùng

* docs/00_MASTER_CONTEXT.md
* docs/23_MVP_SCOPE.md
* docs/26_API_PRIORITY.md
* docs/27_DATABASE_PHASES.md
* docs/28_ENUM_DEFINITIONS.md
* docs/29_ERROR_CODE_STANDARD.md
* docs/30_PERMISSION_MATRIX.md
* docs/07_database/07_01_AUTH_USER.md
* docs/08_api/08_01_AUTH_API.md
* docs/18_CODE_CONVENTIONS.md
* docs/31_DETAILED_TESTING_PLAN.md

## API cần làm

```http
POST /api/auth/refresh-token
POST /api/auth/logout
```

## Request mẫu

### Refresh token request

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

### Logout request

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

## Response mong muốn

### Refresh token response

```json
{
  "success": true,
  "code": 1000,
  "message": "Refresh token successfully",
  "data": {
    "accessToken": "new-jwt-access-token"
  }
}
```

### Logout response

```json
{
  "success": true,
  "code": 1000,
  "message": "Logout successfully",
  "data": null
}
```

## Logic xử lý

### 1. Refresh Token API

1. Nhận refresh token từ request body.
2. Validate refresh token không rỗng.
3. Kiểm tra refresh token có tồn tại trong database không.
4. Kiểm tra refresh token chưa bị revoked.
5. Kiểm tra refresh token chưa hết hạn theo database hoặc JWT expiration.
6. Validate chữ ký JWT refresh token.
7. Extract email hoặc userId từ refresh token.
8. Tìm user tương ứng trong database.
9. Kiểm tra user còn ACTIVE.
10. Generate access token mới.
11. Trả access token mới về frontend.

### 2. Logout API

1. Nhận refresh token từ request body.
2. Validate refresh token không rỗng.
3. Tìm refresh token trong database.
4. Nếu tồn tại, cập nhật `revoked = true`.
5. Nếu token không tồn tại, có thể trả lỗi hoặc trả success tùy strategy.
6. Trả response logout thành công.

## Cần tạo hoặc chỉnh sửa

* RefreshTokenRequest DTO
* RefreshTokenResponse DTO
* LogoutRequest DTO nếu muốn tách riêng
* AuthService
* AuthServiceImpl
* AuthController
* RefreshTokenRepository
* JwtUtil nếu cần thêm method validate refresh token
* ErrorCode nếu còn thiếu
* Swagger/OpenAPI description bằng English

## Error code cần dùng

* AUTH_006: Refresh token không hợp lệ
* AUTH_007: Refresh token đã hết hạn
* AUTH_008: Refresh token đã bị thu hồi
* AUTH_003: Tài khoản đã bị khóa
* VALID_001: Dữ liệu không hợp lệ

## Checklist

* [ ] Tạo RefreshTokenRequest DTO
* [ ] Tạo RefreshTokenResponse DTO
* [ ] Tạo LogoutRequest DTO nếu cần
* [ ] Bổ sung method refreshToken() vào AuthService
* [ ] Bổ sung method logout() vào AuthService
* [ ] Kiểm tra refresh token tồn tại trong database
* [ ] Kiểm tra refresh token chưa revoked
* [ ] Kiểm tra refresh token chưa expired
* [ ] Validate JWT refresh token bằng JwtUtil
* [ ] Generate access token mới
* [ ] Không generate refresh token mới trong task này nếu chưa cần rotation
* [ ] Tạo endpoint POST /api/auth/refresh-token
* [ ] Tạo endpoint POST /api/auth/logout
* [ ] Logout cập nhật revoked = true
* [ ] Trả response chuẩn bằng ApiResponse
* [ ] Không trả Entity trực tiếp
* [ ] Swagger mô tả bằng English
* [ ] Test refresh token hợp lệ
* [ ] Test refresh token sai
* [ ] Test refresh token đã revoked
* [ ] Test logout thành công
* [ ] Test logout xong dùng refresh token cũ bị từ chối
* [ ] Ghi learning notes

## Cách test sau khi hoàn thành

1. Chạy backend.
2. Login bằng `POST /api/auth/login` để lấy refresh token.
3. Gọi `POST /api/auth/refresh-token` với refresh token hợp lệ.
4. Kiểm tra response trả access token mới.
5. Gọi `POST /api/auth/logout` với refresh token.
6. Kiểm tra database: refresh token có `revoked = true`.
7. Gọi lại `POST /api/auth/refresh-token` bằng refresh token đã logout.
8. Kết quả mong muốn: bị từ chối với `AUTH_008`.
9. Test refresh token rỗng hoặc sai format.
10. Test refresh token không tồn tại trong database.
11. Kiểm tra Swagger vẫn hoạt động.
12. Kiểm tra `GET /api/health` vẫn hoạt động.

## Kết quả mong muốn

Hệ thống có thể cấp access token mới bằng refresh token hợp lệ và có thể logout bằng cách revoke refresh token. Sau khi logout, refresh token cũ không thể dùng để lấy access token mới.
