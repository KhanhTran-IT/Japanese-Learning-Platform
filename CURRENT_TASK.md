# CURRENT TASK

## Task hiện tại

Access Token Authentication + GET /api/users/me

## Trạng thái

DONE
Ngày hoàn thành: 17/06/2026

## Mục tiêu

Xây dựng cơ chế xác thực request bằng JWT access token và tạo API `GET /api/users/me` để lấy thông tin user hiện tại đang đăng nhập.

## Vì sao làm task này?

Sau khi hệ thống đã có Register, Login, Refresh Token và Logout, backend cần có khả năng đọc access token từ header `Authorization: Bearer <token>` để xác định user hiện tại.

API `GET /api/users/me` là API nền tảng để frontend biết user đang đăng nhập là ai, có role gì và điều hướng dashboard phù hợp.

Task này là bước chuyển từ “có token” sang “dùng token để bảo vệ API”.

## Không làm trong task này

* Không làm frontend
* Không làm Course
* Không làm Lesson
* Không làm Payment
* Không làm Quiz
* Không làm phân quyền admin chi tiết
* Không làm role permission phức tạp
* Không làm refresh token rotation
* Không làm email verification
* Không làm forgot password

## File tài liệu cần dùng

* docs/00_MASTER_CONTEXT.md
* docs/23_MVP_SCOPE.md
* docs/26_API_PRIORITY.md
* docs/27_DATABASE_PHASES.md
* docs/28_ENUM_DEFINITIONS.md
* docs/29_ERROR_CODE_STANDARD.md
* docs/30_PERMISSION_MATRIX.md
* docs/07_database/07_01_AUTH_USER.md
* docs/08_api/08_02_USER_API.md
* docs/18_CODE_CONVENTIONS.md
* docs/31_DETAILED_TESTING_PLAN.md

## API cần làm

```http id="clje7z"
GET /api/users/me
```

## Request yêu cầu

Client gửi access token trong header:

```http id="arh9t9"
Authorization: Bearer <accessToken>
```

## Response mong muốn

```json id="mv3dxa"
{
  "success": true,
  "code": 1000,
  "message": "Get current user successfully",
  "data": {
    "id": 1,
    "fullName": "Nguyen Van A",
    "email": "user@example.com",
    "phone": null,
    "avatarUrl": null,
    "status": "ACTIVE",
    "roles": ["STUDENT"]
  }
}
```

## Logic xử lý

### 1. JWT Authentication Filter

1. Đọc header `Authorization`.
2. Kiểm tra header có bắt đầu bằng `Bearer ` không.
3. Extract access token.
4. Validate token bằng `JwtUtil`.
5. Extract email hoặc userId từ token.
6. Load user từ database.
7. Kiểm tra user còn ACTIVE.
8. Tạo Authentication object.
9. Set Authentication vào `SecurityContextHolder`.

### 2. GET /api/users/me

1. Lấy user hiện tại từ `SecurityContextHolder`.
2. Tìm user trong database nếu cần.
3. Map User entity sang `CurrentUserResponse` DTO.
4. Trả response chuẩn bằng `ApiResponse`.
5. Không trả passwordHash hoặc Entity trực tiếp.

## Cần tạo hoặc chỉnh sửa

* JwtAuthenticationFilter
* CustomUserDetails
* CustomUserDetailsService nếu chưa có
* SecurityConfig
* UserController hoặc CurrentUserController
* UserService nếu cần
* CurrentUserResponse DTO
* User mapper method nếu project có mapper
* ErrorCode nếu thiếu
* Swagger/OpenAPI description bằng English

## Error code cần dùng

* AUTH_004: Access token không hợp lệ
* AUTH_005: Access token đã hết hạn
* AUTH_003: Tài khoản đã bị khóa
* USER_001: Không tìm thấy người dùng
* ROLE_001: Không có quyền truy cập nếu cần
* VALID_001: Dữ liệu không hợp lệ nếu cần

## Checklist

* [ ] Tạo `CustomUserDetails`
* [ ] Tạo `CustomUserDetailsService` nếu chưa có
* [ ] Tạo `JwtAuthenticationFilter`
* [ ] Cấu hình SecurityConfig để dùng JWT filter
* [ ] Cho phép public các endpoint auth cần thiết
* [ ] Bảo vệ endpoint `/api/users/me`
* [ ] Tạo `CurrentUserResponse` DTO
* [ ] Tạo hoặc cập nhật UserController
* [ ] Tạo endpoint `GET /api/users/me`
* [ ] Lấy user hiện tại từ SecurityContext
* [ ] Không trả passwordHash
* [ ] Trả roles trong response
* [ ] Swagger mô tả bằng English
* [ ] Test không gửi token → 401
* [ ] Test gửi token sai → 401
* [ ] Test gửi access token hợp lệ → 200
* [ ] Test token hết hạn nếu có thể
* [ ] Test account LOCKED nếu có dữ liệu
* [ ] Kiểm tra các endpoint public như login/register vẫn truy cập được
* [ ] Ghi learning notes

## Cách test sau khi hoàn thành

1. Chạy backend.
2. Gọi `POST /api/auth/login` để lấy access token.
3. Gọi `GET /api/users/me` không có token.
4. Kết quả mong muốn: HTTP 401.
5. Gọi `GET /api/users/me` với token sai.
6. Kết quả mong muốn: HTTP 401.
7. Gọi `GET /api/users/me` với access token hợp lệ.
8. Kết quả mong muốn: HTTP 200 và trả thông tin user hiện tại.
9. Kiểm tra response không có passwordHash.
10. Kiểm tra roles trả đúng.
11. Kiểm tra `POST /api/auth/login`, `POST /api/auth/register`, `POST /api/auth/refresh-token` vẫn public đúng như mong muốn.
12. Kiểm tra Swagger vẫn hoạt động.
13. Kiểm tra `GET /api/health` vẫn hoạt động.

## Kết quả mong muốn

Backend có thể xác thực request bằng access token. API `GET /api/users/me` trả đúng thông tin user đang đăng nhập. Các API public vẫn truy cập được, API protected yêu cầu token hợp lệ.
