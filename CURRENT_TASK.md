# CURRENT TASK

## Task hiện tại

Register API

## Trạng thái

TODO

## Mục tiêu

Xây dựng API đăng ký tài khoản cho học viên. User mới đăng ký sẽ được tạo trong bảng users, mật khẩu được hash bằng BCrypt và được gán role STUDENT mặc định.

## Vì sao làm task này?

Sau khi hệ thống đã có User, Role, RefreshToken entity và đã seed role mặc định, bước tiếp theo là cho phép người dùng tạo tài khoản mới. Đây là API đầu tiên của module Auth và là nền tảng cho Login API, JWT và các chức năng học tập sau này.

## Không làm trong task này

* Không làm Login API
* Không làm JWT
* Không làm Refresh Token API
* Không làm Logout API
* Không làm frontend
* Không làm Course
* Không làm Lesson
* Không làm Payment
* Không làm Quiz

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

## API cần làm

POST /api/auth/register

## Request mẫu

```json
{
  "fullName": "Nguyen Van A",
  "email": "user@example.com",
  "password": "Password@123",
  "confirmPassword": "Password@123"
}
```

## Logic xử lý

* Validate fullName không được rỗng
* Validate email đúng định dạng
* Validate password đủ mạnh
* Validate confirmPassword khớp password
* Kiểm tra email đã tồn tại chưa
* Hash password bằng BCrypt
* Lấy role STUDENT từ database
* Tạo user mới với status ACTIVE
* Gán role STUDENT cho user
* Lưu user vào database
* Trả response chuẩn

## Cần tạo hoặc chỉnh sửa

* RegisterRequest
* AuthResponse hoặc RegisterResponse
* AuthController
* AuthService
* AuthServiceImpl nếu project có interface
* ErrorCode bổ sung nếu thiếu
* GlobalExceptionHandler nếu cần xử lý validation
* UserRepository nếu thiếu method existsByEmail/findByEmail
* RoleRepository nếu thiếu method findByName

## Error code cần dùng

* AUTH_001: Email đã tồn tại
* AUTH_010: Mật khẩu xác nhận không khớp
* ROLE_002 hoặc SYS_001 nếu không tìm thấy role STUDENT
* VALID_001: Dữ liệu không hợp lệ

## Checklist

* [ ] Tạo RegisterRequest DTO
* [ ] Tạo RegisterResponse hoặc AuthResponse DTO
* [ ] Bổ sung method existsByEmail trong UserRepository nếu chưa có
* [ ] Bổ sung method findByName trong RoleRepository nếu chưa có
* [ ] Tạo AuthService
* [ ] Viết logic register
* [ ] Tạo AuthController
* [ ] Thêm endpoint POST /api/auth/register
* [ ] Validate request body
* [ ] Check email trùng
* [ ] Check confirmPassword
* [ ] Hash password bằng BCrypt
* [ ] Gán role STUDENT mặc định
* [ ] Trả ApiResponse chuẩn
* [ ] Test bằng Swagger/Postman
* [ ] Test case email trùng
* [ ] Test case password không khớp
* [ ] Test case email sai định dạng
* [ ] Kiểm tra database user được tạo đúng
* [ ] Ghi learning notes

## Cách test sau khi hoàn thành

1. Chạy backend.
2. Mở Swagger.
3. Gọi POST /api/auth/register với dữ liệu hợp lệ.
4. Kiểm tra response thành công.
5. Kiểm tra database có user mới.
6. Kiểm tra password trong database đã được hash, không phải plain text.
7. Kiểm tra user được gán role STUDENT.
8. Gọi lại register với email cũ để kiểm tra lỗi email trùng.
9. Gọi register với confirmPassword sai để kiểm tra lỗi validate.

## Kết quả mong muốn

User có thể đăng ký tài khoản mới thành công. User mới có role STUDENT, password được hash bằng BCrypt và API trả response chuẩn.
