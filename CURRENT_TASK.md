# CURRENT TASK

## Task hiện tại

Login API + JWT Token Generation

## Trạng thái

TODO

## Mục tiêu

Xây dựng API đăng nhập để xác thực tài khoản user. API sẽ verify email/password, tạo JWT accessToken (ngắn hạn, dùng để call API) và refresh token (dài hạn, dùng để renew accessToken), sau đó trả về tokens cùng thông tin user.

## Vì sao làm task này?

Sau Register API, hệ thống cần cho user đã đăng ký có thể đăng nhập. Login API là P0 cho MVP. Từ Login API sẽ sinh ra JWT access token + refresh token để backend xác thực các request sau. Đây là nền tảng cho:

- Các API sau cần xác thực (GET /api/users/me, POST /api/lessons/{id}/progress, etc.)
- Token refresh flow (refresh-token API)
- Logout flow

## Không làm trong task này

- Không làm Refresh Token API (POST /api/auth/refresh-token)
- Không làm Logout API (POST /api/auth/logout)
- Không làm GET /api/users/me
- Không làm frontend
- Không làm Course
- Không làm Lesson
- Không làm Payment
- Không làm Quiz
- Không làm email verification

## File tài liệu cần dùng

- docs/00_MASTER_CONTEXT.md
- docs/23_MVP_SCOPE.md
- docs/26_API_PRIORITY.md
- docs/27_DATABASE_PHASES.md
- docs/28_ENUM_DEFINITIONS.md
- docs/29_ERROR_CODE_STANDARD.md
- docs/30_PERMISSION_MATRIX.md
- docs/07_database/07_01_AUTH_USER.md
- docs/08_api/08_01_AUTH_API.md
- docs/18_CODE_CONVENTIONS.md

## API cần làm

POST /api/auth/login

## Request mẫu

```json
{
  "email": "user@example.com",
  "password": "Password@123"
}
```

## Response mong muốn

```json
{
  "success": true,
  "code": 1000,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "fullName": "Nguyen Van A",
      "email": "user@example.com",
      "roles": ["STUDENT"]
    }
  }
}
```

## Logic xử lý

1. **Validate input:** Email không rỗng, password không rỗng
2. **Tìm user bằng email:** Kiểm tra user có tồn tại không
3. **Verify password:** So sánh password client gửi với passwordHash trong database bằng BCrypt
4. **Kiểm tra trạng thái user:** Nếu user bị LOCKED/INACTIVE → lỗi
5. **Tạo AccessToken:** JWT token ngắn hạn (ví dụ 15 phút)
   - Payload: id, email, roles
   - Secret key từ application.yml hoặc environment variable
   - Expiration: tính từ lúc login + 15 phút
6. **Tạo RefreshToken:** JWT token dài hạn (ví dụ 7 ngày)
   - Payload: id, email
   - Secret key khác hoặc cùng
   - Expiration: tính từ lúc login + 7 ngày
   - Lưu refresh token vào database (RefreshToken table)
7. **Trả response chuẩn:** access token, refresh token, user info (không password)

## Cần tạo hoặc chỉnh sửa

- **LoginRequest DTO**: email, password
- **LoginResponse DTO**: accessToken, refreshToken, user info
- **JwtUtil class**: Tạo/verify JWT token
  - `generateAccessToken(User)`
  - `generateRefreshToken(User)`
  - `validateToken(token)`
  - `extractClaims(token)`
  - `extractEmail(token)`
  - `isTokenExpired(token)`
- **AuthService**: Thêm method `login(LoginRequest)`
- **AuthController**: Thêm endpoint POST /api/auth/login
- **application.yml**: Thêm JWT configuration
  - `jwt.secret.access` (secret key cho access token)
  - `jwt.secret.refresh` (secret key cho refresh token)
  - `jwt.expiration.access` (ví dụ 900000 ms = 15 phút)
  - `jwt.expiration.refresh` (ví dụ 604800000 ms = 7 ngày)
- **pom.xml**: Thêm dependency `jjwt` (hoặc `java-jwt`)
  - `io.jsonwebtoken:jjwt-api`
  - `io.jsonwebtoken:jjwt-impl`
  - `io.jsonwebtoken:jjwt-jackson`
- **RefreshTokenRepository**: Nếu chưa có
  - `findByToken(token)`
  - `deleteByToken(token)`
- **Error codes**: Bổ sung nếu thiếu
  - AUTH_002: Email hoặc mật khẩu không đúng
  - AUTH_003: Tài khoản đã bị khóa

## Token strategy

**AccessToken (JWT):**

- Dùng để authorize request (gửi trong header: `Authorization: Bearer <token>`)
- Ngắn hạn (15-30 phút) để giảm rủi ro nếu bị leak
- Không cần lưu database (stateless)
- Verify bằng public key hoặc secret key

**RefreshToken (JWT):**

- Dùng để tạo access token mới khi access token hết hạn
- Dài hạn (7-30 ngày)
- **Nên lưu database** để có thể revoke (logout, change password)
- Verify bằng database lookup (có trong bảng RefreshToken không?)

**Lý do 2 token:**

- AccessToken ngắn hạn → bảo mật tốt
- RefreshToken dài hạn → UX tốt (không cần login liên tục)
- Nếu chỉ 1 token dài hạn → bảo mật tệ
- Nếu chỉ 1 token ngắn hạn → UX tệ (login liên tục)

## Error codes

- **AUTH_001**: Email đã tồn tại (Register)
- **AUTH_002**: Email hoặc mật khẩu không đúng (Login) - HTTP 401
- **AUTH_003**: Tài khoản đã bị khóa (LOCKED status) - HTTP 403
- **AUTH_004**: Access token không hợp lệ (Invalid/tampered JWT) - HTTP 401
- **AUTH_005**: Access token đã hết hạn (JWT expired) - HTTP 401
- **AUTH_006**: Refresh token không hợp lệ - HTTP 401
- **AUTH_007**: Refresh token đã hết hạn - HTTP 401
- **AUTH_008**: Refresh token đã bị thu hồi (revoked=true) - HTTP 401
- **AUTH_010**: Mật khẩu xác nhận không khớp (Register/Reset password) - HTTP 400

## Checklist

- [ ] Thêm jjwt dependency vào pom.xml
- [ ] Tạo LoginRequest DTO
- [ ] Tạo LoginResponse DTO
- [ ] Tạo JwtUtil class để generate/verify JWT
- [ ] Bổ sung jwt configuration vào application.yml
- [ ] Thêm method login() vào AuthService
- [ ] Thêm endpoint POST /api/auth/login vào AuthController
- [ ] Validate input (email, password không rỗng)
- [ ] Find user by email từ UserRepository
- [ ] Verify password bằng PasswordEncoder.matches()
- [ ] Check user status (nếu LOCKED → throw AUTH_003)
- [ ] Generate access token + refresh token
- [ ] Lưu refresh token vào database (RefreshToken table)
- [ ] Trả LoginResponse chuẩn (không bao giờ trả Entity)
- [ ] Thêm error code AUTH_002, AUTH_003 nếu chưa có
- [ ] Test bằng Swagger/Postman
- [ ] Test case email không tồn tại
- [ ] Test case password sai
- [ ] Test case email sai định dạng
- [ ] Test case account bị lock
- [ ] Verify access token structure bằng jwt.io
- [ ] Verify refresh token được lưu database
- [ ] Ghi learning notes

## Cách test sau khi hoàn thành

1. Chạy backend.
2. Mở Swagger hoặc Postman.
3. Gọi POST /api/auth/register để tạo user (nếu chưa có).
4. Gọi POST /api/auth/login với email/password đúng.
5. Kiểm tra response có 2 tokens (access + refresh).
6. Copy access token, paste vào jwt.io để decode (verify payload).
7. Kiểm tra database RefreshToken table có record mới.
8. Gọi POST /api/auth/login với password sai → HTTP 401 + code AUTH_002.
9. Gọi POST /api/auth/login với email không tồn tại → HTTP 401 + code AUTH_002.
10. Gọi POST /api/auth/login với email rỗng → HTTP 400 + validation error.

## Kết quả mong muốn

User có thể đăng nhập bằng email/password. Backend tạo 2 JWT tokens (access + refresh), lưu refresh token vào database. API trả response chuẩn với tokens và user info (không password). Tokens có thể decode bằng jwt.io để xác minh payload, chuẩn hóa lại swagger không có sử dụng Tiếng Việt trong mô tả, dùng enlish hết.

---

## Task vừa hoàn thành: Register API ✅

**Status:** DONE
**Ngày hoàn thành:** 14/06/2026

### Kết quả đã đạt được

- ✅ Tạo POST /api/auth/register endpoint
- ✅ Validate email format, password length, confirmPassword match
- ✅ Check email trùng → HTTP 409
- ✅ Hash password bằng BCrypt
- ✅ Assign role STUDENT mặc định
- ✅ Trả RegisterResponse DTO (không expose Entity)
- ✅ Lỗi chuẩn hóa (ErrorCode enum)

### Cách đã test

- ✅ Gọi register với dữ liệu hợp lệ → HTTP 201
- ✅ Database có user mới
- ✅ Password đã hash ($2a$...)
- ✅ User được gán role STUDENT
- ✅ Gọi register với email trùng → HTTP 409 (AUTH_001)
- ✅ Gọi register với password ≠ confirmPassword → HTTP 400 (AUTH_010)

### Ghi chú học tập đã cập nhật

- ✅ docs/learning/LEARNING_LOG.md - Mục 14/06/2026 (hoàn thành)
- ✅ docs/learning/INTERVIEW_NOTES.md - Register API section (10 câu hỏi)
- ✅ docs/learning/CONCEPTS_EXPLAINED.md - DTO, Bean Validation, @Transactional, ErrorCode, PasswordEncoder
