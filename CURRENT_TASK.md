# CURRENT TASK

## Task hiện tại
Backend Admin User Management API

## Trạng thái
TODO

## Mục tiêu
Xây dựng nhóm API quản lý user cơ bản cho Admin, gồm xem danh sách user có phân trang/tìm kiếm/lọc, xem chi tiết user, khóa tài khoản và mở khóa tài khoản. API phải trả DTO sạch, không lộ `passwordHash`, và chỉ cho `ADMIN` hoặc `SUPER_ADMIN` truy cập.

## Vì sao làm task này?
Sau khi Admin Dashboard đã có dữ liệu tổng quan thật, bước tiếp theo trong nhóm P0 Admin là cho admin quản lý người dùng. Đây là nền tảng để vận hành hệ thống: kiểm tra tài khoản mới, tìm kiếm học viên, xử lý tài khoản vi phạm bằng lock/unlock và chuẩn bị cho màn `UserManagementPage` ở frontend.

## Không làm trong task này
- Không làm tạo user từ admin.
- Không làm cập nhật thông tin user.
- Không làm gán role hoặc nâng quyền user.
- Không làm xóa mềm user.
- Không làm lịch sử học tập/đơn hàng chi tiết của user.
- Không làm frontend `UserManagementPage` trong task này.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/24_USER_FLOWS.md`
- `docs/25_SCREEN_LIST.md`
- `docs/26_API_PRIORITY.md`
- `docs/27_DATABASE_PHASES.md`
- `docs/28_ENUM_DEFINITIONS.md`
- `docs/29_ERROR_CODE_STANDARD.md`
- `docs/30_PERMISSION_MATRIX.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/07_database/07_01_AUTH_USER.md`
- `docs/08_api/08_10_ADMIN_API.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`

## API cần làm
```http
GET /api/v1/admin/users
GET /api/v1/admin/users/{id}
PUT /api/v1/admin/users/{id}/lock
PUT /api/v1/admin/users/{id}/unlock
```

Quyền truy cập:
- `ADMIN`
- `SUPER_ADMIN`

## Request mẫu
Danh sách user:

```http
GET /api/v1/admin/users?page=0&size=10&keyword=nguyen&role=STUDENT&status=ACTIVE
Authorization: Bearer <adminAccessToken>
```

Chi tiết user:

```http
GET /api/v1/admin/users/5
Authorization: Bearer <adminAccessToken>
```

Khóa user:

```http
PUT /api/v1/admin/users/5/lock
Authorization: Bearer <adminAccessToken>
```

Mở khóa user:

```http
PUT /api/v1/admin/users/5/unlock
Authorization: Bearer <adminAccessToken>
```

## Response mong muốn
Danh sách user:

```json
{
  "code": 1000,
  "message": "Lấy danh sách người dùng thành công",
  "result": {
    "currentPage": 0,
    "pageSize": 10,
    "totalPages": 3,
    "totalElements": 25,
    "data": [
      {
        "id": 5,
        "fullName": "Nguyễn Văn A",
        "email": "student@example.com",
        "phone": "0900000000",
        "avatarUrl": null,
        "status": "ACTIVE",
        "emailVerified": false,
        "roles": ["STUDENT"],
        "createdAt": "2026-07-18T10:00:00",
        "lastLoginAt": null
      }
    ]
  }
}
```

Chi tiết/lock/unlock user trả về cùng DTO user:

```json
{
  "code": 1000,
  "message": "Khóa tài khoản thành công",
  "result": {
    "id": 5,
    "fullName": "Nguyễn Văn A",
    "email": "student@example.com",
    "status": "LOCKED",
    "roles": ["STUDENT"]
  }
}
```

## Logic xử lý
- Tạo DTO `AdminUserRes` chứa thông tin an toàn để admin xem, tuyệt đối không trả `passwordHash`.
- Tạo service `AdminUserService` và `AdminUserServiceImpl`.
- Tạo controller `AdminUserController` với base path `/api/v1/admin/users`.
- API danh sách hỗ trợ:
  - `page`, `size`
  - `keyword` tìm theo `fullName` hoặc `email`
  - `status` lọc theo `UserStatus`
  - `role` lọc theo `RoleName`
- Nếu chưa làm filter động được ngay, ưu tiên làm phân trang + keyword + status trước, sau đó bổ sung role filter bằng query join `roles`.
- Dùng `PageResponse<AdminUserRes>` hoặc chuẩn phân trang hiện có trong project.
- Khi lấy user list/detail, fetch kèm `roles` để tránh N+1 query.
- `lockUser(id)` đổi `status` thành `LOCKED`.
- `unlockUser(id)` đổi `status` thành `ACTIVE`.
- Không cho admin tự khóa chính tài khoản đang đăng nhập nếu project đã có cách lấy current user từ `SecurityContext`.
- Nếu user không tồn tại, throw `AppException(ErrorCode.USER_NOT_FOUND)`.
- Controller không chứa logic nghiệp vụ, chỉ gọi service và trả `ApiResponse`.
- Tất cả endpoint dùng `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")`.

## Cần tạo hoặc chỉnh sửa
- `backend/src/main/java/com/japaneselearning/module_admin/controller/AdminUserController.java`
- `backend/src/main/java/com/japaneselearning/module_admin/service/AdminUserService.java`
- `backend/src/main/java/com/japaneselearning/module_admin/service/AdminUserServiceImpl.java`
- `backend/src/main/java/com/japaneselearning/module_admin/dto/AdminUserRes.java`
- `backend/src/main/java/com/japaneselearning/module_user/repository/UserRepository.java`
- Có thể chỉnh `backend/src/main/java/com/japaneselearning/common/exception/ErrorCode.java` nếu cần thêm lỗi rõ hơn cho tự khóa tài khoản.

## Error code cần dùng
- `USER_NOT_FOUND` khi không tìm thấy user.
- `FORBIDDEN_ACCESS` nếu user hiện tại không được thực hiện hành động.
- `INVALID_REQUEST` nếu request filter không hợp lệ hoặc admin cố tự khóa tài khoản của mình.

## Checklist
- [ ] Tạo `AdminUserRes`, không chứa `passwordHash`.
- [ ] Tạo `AdminUserService` và `AdminUserServiceImpl`.
- [ ] Tạo `AdminUserController` với 4 endpoint đúng path.
- [ ] Bổ sung repository query phục vụ paging/search/filter.
- [ ] Dùng `@EntityGraph` hoặc query fetch roles để tránh N+1.
- [ ] Implement list user có phân trang.
- [ ] Implement get user detail.
- [ ] Implement lock user.
- [ ] Implement unlock user.
- [ ] Chặn admin tự lock chính mình nếu lấy được current user.
- [ ] Thêm `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")` cho endpoint.
- [ ] Chạy `mvn test`.

## Cách test sau khi hoàn thành
1. Chạy backend Spring Boot.
2. Đăng nhập bằng admin và lấy access token.
3. Gọi `GET /api/v1/admin/users?page=0&size=10`, kỳ vọng trả danh sách user có phân trang.
4. Gọi `GET /api/v1/admin/users?keyword=student`, kỳ vọng lọc theo email hoặc fullName.
5. Gọi `GET /api/v1/admin/users?status=ACTIVE`, kỳ vọng chỉ thấy user active.
6. Gọi `GET /api/v1/admin/users/{id}`, kỳ vọng trả chi tiết user nhưng không có `passwordHash`.
7. Gọi `PUT /api/v1/admin/users/{id}/lock`, kỳ vọng status đổi thành `LOCKED`.
8. Gọi `PUT /api/v1/admin/users/{id}/unlock`, kỳ vọng status đổi lại `ACTIVE`.
9. Dùng token student gọi các endpoint admin, kỳ vọng bị 403.
10. Gọi không có token, kỳ vọng bị 401.

## Kết quả mong muốn
Backend có nhóm API quản lý user cơ bản cho admin, phân quyền đúng, response an toàn, hỗ trợ phân trang/tìm kiếm/lọc và thao tác lock/unlock tài khoản hoạt động ổn định.
