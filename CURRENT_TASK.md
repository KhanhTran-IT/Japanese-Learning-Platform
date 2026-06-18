# CURRENT TASK

## Task hiện tại

Basic Role-Based Authorization + Security Rules

## Trạng thái

TODO

## Mục tiêu

Thiết lập phân quyền cơ bản theo role cho backend. Hệ thống cần phân biệt rõ endpoint public, endpoint chỉ cần đăng nhập, và endpoint chỉ dành cho ADMIN/SUPER_ADMIN.

## Vì sao làm task này?

Sau khi backend đã có JWT authentication và API `GET /api/users/me`, hệ thống đã biết user hiện tại là ai và có role gì. Bước tiếp theo là dùng thông tin role đó để kiểm soát quyền truy cập API.

Task này giúp chuẩn bị nền cho các module sau:

* Admin Course CRUD.
* Admin User Management.
* Student learning APIs.
* Teacher APIs trong tương lai.
* Bảo vệ `/api/admin/**` khỏi user thường.

## Không làm trong task này

* Không làm frontend.
* Không làm Course.
* Không làm Lesson.
* Không làm Payment.
* Không làm Quiz.
* Không làm Admin CRUD thật.
* Không làm Teacher permission chi tiết.
* Không làm permission matrix phức tạp theo từng resource owner.
* Không làm OAuth2/social login.
* Không làm email verification.
* Không làm forgot password.

## File tài liệu cần dùng

* docs/00_MASTER_CONTEXT.md
* docs/23_MVP_SCOPE.md
* docs/26_API_PRIORITY.md
* docs/28_ENUM_DEFINITIONS.md
* docs/29_ERROR_CODE_STANDARD.md
* docs/30_PERMISSION_MATRIX.md
* docs/18_CODE_CONVENTIONS.md
* docs/31_DETAILED_TESTING_PLAN.md

## API cần kiểm soát

### Public endpoints

```http
GET  /api/health
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh-token
POST /api/auth/logout
GET  /swagger-ui/**
GET  /v3/api-docs/**
```

### Authenticated endpoints

```http
GET /api/users/me
PUT /api/users/me
PUT /api/users/me/change-password
```

### Admin endpoints

```http
/api/admin/**
```

Chỉ cho phép:

```text
ADMIN
SUPER_ADMIN
```

### Student endpoints, chuẩn bị cho tương lai

```http
/api/student/**
```

Chỉ cho phép:

```text
STUDENT
ADMIN
SUPER_ADMIN
```

## Logic xử lý

1. Kiểm tra role trong `CustomUserDetails`.
2. Đảm bảo role được map đúng sang authority.
3. Cấu hình `SecurityConfig`:

   * Public endpoints dùng `permitAll`.
   * `/api/users/me` yêu cầu authenticated.
   * `/api/admin/**` yêu cầu role ADMIN hoặc SUPER_ADMIN.
   * Các endpoint còn lại có strategy rõ ràng.
4. Bật method security nếu cần dùng `@PreAuthorize`.
5. Tạo endpoint test đơn giản nếu cần để kiểm tra admin role, nhưng không code module admin thật.
6. Đảm bảo response lỗi 401/403 rõ ràng:

   * 401: chưa đăng nhập/token sai.
   * 403: đã đăng nhập nhưng không đủ quyền.

## Cần tạo hoặc chỉnh sửa

* SecurityConfig
* CustomUserDetails nếu authority mapping chưa chuẩn
* JwtAuthenticationEntryPoint nếu cần chuẩn hóa 401
* AccessDeniedHandler nếu cần chuẩn hóa 403
* ErrorCode nếu thiếu lỗi permission
* Có thể tạo test controller tạm thời cho admin nếu cần, nhưng không tạo module admin thật

## Error code cần dùng

* AUTH_004: Access token không hợp lệ
* AUTH_005: Access token đã hết hạn
* ROLE_001: Không có quyền truy cập
* SYS_001 nếu có lỗi hệ thống không mong muốn

## Checklist

* [ ] Kiểm tra role trong database đã đúng
* [ ] Kiểm tra role trong JWT hoặc CustomUserDetails đã đúng
* [ ] Kiểm tra authority format có đúng với Spring Security không
* [ ] Cấu hình public endpoint
* [ ] Cấu hình authenticated endpoint
* [ ] Cấu hình `/api/admin/**` chỉ ADMIN/SUPER_ADMIN
* [ ] Bổ sung AccessDeniedHandler nếu cần xử lý 403
* [ ] Đảm bảo không token → 401
* [ ] Đảm bảo token hợp lệ nhưng thiếu quyền → 403
* [ ] Đảm bảo ADMIN vào được admin endpoint test nếu có
* [ ] Đảm bảo STUDENT không vào được admin endpoint test
* [ ] Đảm bảo login/register/refresh/logout vẫn public
* [ ] Đảm bảo `/api/users/me` vẫn hoạt động
* [ ] Swagger/OpenAPI vẫn hoạt động
* [ ] Ghi learning notes

## Cách test sau khi hoàn thành

1. Chạy backend.
2. Gọi `POST /api/auth/login` bằng user STUDENT.
3. Gọi `GET /api/users/me` với STUDENT token.
4. Kết quả mong muốn: HTTP 200.
5. Gọi thử endpoint `/api/admin/**` nếu có test endpoint bằng STUDENT token.
6. Kết quả mong muốn: HTTP 403.
7. Login bằng admin user.
8. Gọi endpoint `/api/admin/**` bằng ADMIN token.
9. Kết quả mong muốn: HTTP 200 nếu có test endpoint.
10. Gọi `/api/users/me` không token.
11. Kết quả mong muốn: HTTP 401.
12. Kiểm tra register/login/refresh/logout vẫn gọi được không cần access token.
13. Kiểm tra Swagger vẫn mở được.

## Kết quả mong muốn

Backend phân biệt được public API, authenticated API và admin API. User chưa đăng nhập bị 401, user đã đăng nhập nhưng không đủ quyền bị 403, ADMIN/SUPER_ADMIN có thể truy cập admin endpoint.

---
