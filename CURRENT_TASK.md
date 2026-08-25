# CURRENT TASK

## Task hiện tại
Student Profile API & Page Foundation

## Trạng thái
TODO

## Mục tiêu
Hoàn thiện phần hồ sơ cá nhân P0 cho student bằng cách bổ sung API cập nhật thông tin cơ bản, API đổi mật khẩu và màn hình `/student/profile` để student tự xem/sửa thông tin tài khoản.

## Vì sao làm task này?
Theo MVP, hệ thống cần hoàn thiện lõi học online trước: auth, course, lesson, progress, admin CRUD và các màn hình student cơ bản. Sau khi student đã đăng nhập, enroll, học bài, lưu progress và điều hướng curriculum được, phần còn thiếu hợp lý trước khi sang quiz là profile cá nhân. Đây là P0 trong `SCREEN_LIST` và nằm trong nhóm Student API ở `API_PRIORITY`.

## Không làm trong task này
- Không làm quiz.
- Không làm payment/order.
- Không làm forgot password/reset password qua email.
- Không làm verify email.
- Không làm upload avatar file thật.
- Không làm notification.
- Không làm profile admin/user detail nâng cao.
- Không đổi kiến trúc auth/JWT hiện có.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/24_USER_FLOWS.md`
- `docs/25_SCREEN_LIST.md`
- `docs/26_API_PRIORITY.md`
- `docs/28_ENUM_DEFINITIONS.md`
- `docs/29_ERROR_CODE_STANDARD.md`
- `docs/30_PERMISSION_MATRIX.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`
- `docs/07_database/07_01_AUTH_USER.md`
- `docs/08_api/08_01_AUTH_API.md`
- `docs/08_api/08_02_USER_API.md`
- `docs/10_FRONTEND_STRUCTURE.md`
- `docs/11_BACKEND_FRONTEND_CONFIG.md`

## Vấn đề hiện tại
- Backend hiện có `GET /api/users/me` nhưng chưa có API cập nhật profile.
- Backend chưa có API đổi mật khẩu cho user hiện tại.
- `UserService` hiện chỉ có `getCurrentUser()`.
- `SecurityConfig` đang match chính xác `/api/users/me`; cần đảm bảo các endpoint con như `/api/users/me/change-password` được authenticated đúng.
- Frontend chưa có route `/student/profile`.
- Frontend chưa có `ProfilePage.vue`.
- `AuthService` hiện chỉ có login, register, get current user và logout.

## Hướng triển khai đề xuất

### Backend
Thêm DTO request:
- `UpdateCurrentUserReq`
  - `fullName`
  - `phone`
  - `avatarUrl` nếu muốn cho phép nhập URL tạm thời
- `ChangePasswordReq`
  - `currentPassword`
  - `newPassword`
  - `confirmPassword`

Cập nhật `UserService`:
- `CurrentUserResponse updateCurrentUser(UpdateCurrentUserReq request)`
- `void changePassword(ChangePasswordReq request)`

Cập nhật `UserServiceImpl`:
- Lấy user hiện tại từ `SecurityContextHolder`.
- Validate user tồn tại.
- Update các field cho phép sửa: `fullName`, `phone`, có thể `avatarUrl`.
- Không cho sửa email trong task này để tránh phức tạp verify email.
- Với đổi mật khẩu:
  - kiểm tra `currentPassword` bằng `PasswordEncoder.matches()`.
  - kiểm tra `newPassword` và `confirmPassword` khớp nhau.
  - encode password mới bằng BCrypt.
  - lưu vào `passwordHash`.

Cập nhật `UserController`:

```http
PUT /api/users/me
PUT /api/users/me/change-password
```

Cập nhật `SecurityConfig` nếu cần:
- Đảm bảo `/api/users/me/**` hoặc các method PUT tương ứng đều yêu cầu authenticated.

Cập nhật `ErrorCode` nếu cần:
- Có thể tái sử dụng `LOGIN_FAILED` cho sai current password, nhưng nên cân nhắc error code rõ hơn như `CURRENT_PASSWORD_INCORRECT`.
- Tái sử dụng `PASSWORD_CONFIRM_NOT_MATCH` cho confirm password sai.

### Frontend
Thêm hoặc cập nhật service:
- `AuthService.updateCurrentUser(payload)`
- `AuthService.changePassword(payload)`

Thêm route:

```text
/student/profile
```

Tạo `frontend/src/pages/student/ProfilePage.vue`:
- Load user hiện tại từ auth store hoặc gọi `AuthService.getCurrentUser()`.
- Form thông tin cá nhân:
  - full name
  - email readonly
  - phone
  - avatar URL nếu backend hỗ trợ URL tạm thời
- Form đổi mật khẩu:
  - current password
  - new password
  - confirm password
- Loading state riêng cho từng form.
- Error/success message rõ ràng.
- Sau khi cập nhật profile thành công, cập nhật lại auth store/current user để header/layout dùng dữ liệu mới.
- Không lưu hoặc log password.

## Cần tạo hoặc chỉnh sửa

### Backend
- `backend/src/main/java/com/japaneselearning/module_user/controller/UserController.java`
- `backend/src/main/java/com/japaneselearning/module_user/service/UserService.java`
- `backend/src/main/java/com/japaneselearning/module_user/service/UserServiceImpl.java`
- Tạo DTO nếu chưa có:
  - `backend/src/main/java/com/japaneselearning/module_user/dto/UpdateCurrentUserReq.java`
  - `backend/src/main/java/com/japaneselearning/module_user/dto/ChangePasswordReq.java`
- Có thể chỉnh:
  - `backend/src/main/java/com/japaneselearning/common/config/SecurityConfig.java`
  - `backend/src/main/java/com/japaneselearning/common/exception/ErrorCode.java`

### Frontend
- `frontend/src/services/auth.service.js`
- `frontend/src/stores/auth.store.js` nếu cần method refresh/update user.
- `frontend/src/router/index.js`
- `frontend/src/pages/student/ProfilePage.vue`
- Có thể chỉnh `StudentLayout.vue` nếu sidebar/nav chưa có link Profile.
- Có thể thêm test cho profile page nếu pattern hiện có thuận tiện.

## Checklist
- [x] `PUT /api/users/me` cập nhật được `fullName`, `phone`, `avatarUrl` nếu có.
- [x] `PUT /api/users/me` không cho user tự sửa role/status/email.
- [x] `PUT /api/users/me/change-password` yêu cầu current password đúng.
- [x] New password được encode bằng `PasswordEncoder`.
- [x] Confirm password sai trả lỗi rõ ràng.
- [x] Endpoint profile chỉ cho authenticated user.
- [x] Frontend có route `/student/profile`.
- [x] Profile page hiển thị user hiện tại.
- [x] Profile form update thành công và refresh auth user state.
- [x] Change password form có loading/error/success state.
- [x] Không log password ở frontend/backend.
- [x] Chạy frontend build/test.
- [x] Chạy backend package/test phù hợp, hoặc ghi rõ blocker môi trường nếu còn lỗi Mockito/Byte Buddy.

## Cách test sau khi hoàn thành
1. Đăng nhập bằng STUDENT.
2. Vào `/student/profile`.
3. Kiểm tra form hiển thị đúng full name, email, phone, avatar URL nếu có.
4. Sửa full name/phone và lưu, kỳ vọng API thành công và UI cập nhật.
5. Reload trang, kỳ vọng dữ liệu mới vẫn còn.
6. Đổi mật khẩu với current password sai, kỳ vọng báo lỗi.
7. Đổi mật khẩu với confirm password sai, kỳ vọng báo lỗi.
8. Đổi mật khẩu đúng, logout rồi login bằng mật khẩu mới.
9. Gọi API profile khi chưa login, kỳ vọng bị 401.
10. Chạy `npm run build`.
11. Chạy `npm test`.
12. Chạy backend package/test phù hợp.

## Kết quả mong muốn
Student có một trang hồ sơ cá nhân tối thiểu nhưng hoàn chỉnh cho MVP: xem thông tin tài khoản, cập nhật thông tin cơ bản và đổi mật khẩu an toàn.
