# CURRENT TASK

## Task hiện tại
Frontend Admin User Management UI & API Integration

## Trạng thái
TODO

## Mục tiêu
Xây dựng màn hình quản lý người dùng cho Admin tại route `/admin/users`, tích hợp với nhóm API backend vừa hoàn thành để admin có thể xem danh sách user, tìm kiếm/lọc user, xem thông tin chính và khóa/mở khóa tài khoản.

## Vì sao làm task này?
Backend Admin User Management API đã sẵn sàng, nhưng frontend admin hiện mới có Dashboard. Màn `UserManagementPage` là P0 trong MVP, giúp admin vận hành hệ thống thực tế: theo dõi tài khoản, tìm user theo email/tên, lọc theo role/status và xử lý khóa/mở khóa user khi cần.

## Không làm trong task này
- Không làm tạo user mới từ admin.
- Không làm chỉnh sửa hồ sơ user.
- Không làm gán role/nâng quyền user.
- Không làm xóa mềm user.
- Không làm trang chi tiết user riêng `/admin/users/:id`.
- Không làm lịch sử học tập, đơn hàng hoặc tiến độ học của user.
- Không dùng thư viện UI/table mới nếu project chưa dùng.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/24_USER_FLOWS.md`
- `docs/25_SCREEN_LIST.md`
- `docs/26_API_PRIORITY.md`
- `docs/30_PERMISSION_MATRIX.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/10_FRONTEND_STRUCTURE.md`
- `docs/11_BACKEND_FRONTEND_CONFIG.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`

## API cần làm
Frontend gọi các API backend sau:

```http
GET /api/v1/admin/users?page=0&size=10&keyword=&role=&status=
GET /api/v1/admin/users/{id}
PUT /api/v1/admin/users/{id}/lock
PUT /api/v1/admin/users/{id}/unlock
```

## Request mẫu
```javascript
AdminService.getUsers({
  page: 0,
  size: 10,
  keyword: 'student',
  role: 'STUDENT',
  status: 'ACTIVE'
})

AdminService.lockUser(5)
AdminService.unlockUser(5)
```

## Response mong muốn
Frontend kỳ vọng backend trả `ApiResponse`:

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
        "createdAt": "2026-07-19T10:00:00",
        "lastLoginAt": null
      }
    ]
  }
}
```

## Logic xử lý
- Khai báo route `/admin/users` dưới `AdminLayout`, có meta kế thừa `requiresAuth` và role `ADMIN`.
- Thêm menu "Người dùng" vào `AdminLayout.vue`.
- Mở rộng `AdminService`:
  - `getUsers(params)`
  - `getUserDetail(id)` nếu cần cho modal xem nhanh
  - `lockUser(id)`
  - `unlockUser(id)`
- Tạo `AdminUserManagementPage.vue`.
- Khi page mounted, gọi API lấy danh sách user trang đầu tiên.
- UI cần có:
  - ô tìm kiếm keyword theo tên/email
  - select lọc `role`
  - select lọc `status`
  - nút reset filter
  - bảng user
  - phân trang đơn giản
  - trạng thái loading/error/empty
  - nút lock/unlock theo status user
- Khi đổi filter, gọi lại API từ page 0.
- Khi đổi page, gọi lại API với page mới.
- Khi lock/unlock:
  - hỏi xác nhận bằng `window.confirm` hoặc modal đơn giản nếu project đã có component
  - gọi API tương ứng
  - reload danh sách hoặc cập nhật row hiện tại
  - hiển thị thông báo lỗi/thành công bằng UI inline, không dùng alert nếu có thể tránh
- Không hiển thị hoặc disable nút lock/unlock với user có role `SUPER_ADMIN`.
- Không hiển thị field nhạy cảm nào.
- Format date theo `vi-VN`.

## Cần tạo hoặc chỉnh sửa
- `frontend/src/pages/admin/AdminUserManagementPage.vue`
- `frontend/src/services/admin.service.js`
- `frontend/src/router/index.js`
- `frontend/src/layouts/AdminLayout.vue`
- Có thể tạo component nhỏ nếu thật sự cần:
  - `frontend/src/components/admin/UserStatusBadge.vue`
  - `frontend/src/components/admin/RoleBadge.vue`

## Error code cần dùng
Không tạo error code frontend riêng. Frontend cần xử lý các tình huống:
- 401: chưa đăng nhập hoặc token hết hạn.
- 403: không có quyền admin.
- 404: user không tồn tại khi lock/unlock.
- 400: request không hợp lệ hoặc cố khóa tài khoản không được phép.

## Checklist
- [ ] Khai báo route `/admin/users`.
- [ ] Thêm menu "Người dùng" trong `AdminLayout.vue`.
- [ ] Thêm `getUsers`, `lockUser`, `unlockUser` vào `admin.service.js`.
- [ ] Tạo `AdminUserManagementPage.vue`.
- [ ] Render bảng user với các cột: tên, email, role, status, email verified, ngày tạo, đăng nhập cuối, thao tác.
- [ ] Thêm search keyword.
- [ ] Thêm filter role.
- [ ] Thêm filter status.
- [ ] Thêm phân trang.
- [ ] Xử lý loading/error/empty state.
- [ ] Thêm lock/unlock action có confirm.
- [ ] Ẩn hoặc disable lock/unlock với `SUPER_ADMIN`.
- [ ] Chạy `npm run build`.

## Cách test sau khi hoàn thành
1. Chạy backend Spring Boot.
2. Chạy frontend Vue.
3. Đăng nhập bằng tài khoản admin.
4. Mở `/admin/users`, kỳ vọng thấy bảng danh sách user.
5. Tìm theo keyword email/tên, kỳ vọng danh sách thay đổi đúng.
6. Lọc theo role `STUDENT`, kỳ vọng chỉ thấy user có role student.
7. Lọc theo status `ACTIVE` hoặc `LOCKED`, kỳ vọng dữ liệu đúng.
8. Bấm khóa một user thường, xác nhận, kỳ vọng status chuyển sang `LOCKED`.
9. Bấm mở khóa user đó, kỳ vọng status chuyển lại `ACTIVE`.
10. Đăng nhập bằng student rồi cố vào `/admin/users`, kỳ vọng bị route guard chặn hoặc chuyển hướng.

## Kết quả mong muốn
Admin có màn quản lý người dùng cơ bản hoạt động mượt, gọi API thật, có filter/pagination, xử lý trạng thái UI rõ ràng và thao tác lock/unlock an toàn.
