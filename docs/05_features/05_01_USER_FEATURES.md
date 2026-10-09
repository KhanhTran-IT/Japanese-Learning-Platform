> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

## 5.1. Nhóm chức năng người dùng

### 5.1.1. Đăng ký tài khoản

Chức năng:

- Đăng ký bằng email/password.
- Validate email.
- Validate mật khẩu mạnh.
- Gửi email xác thực.
- Không cho đăng nhập nếu chưa xác thực email, tùy cấu hình.

Thông tin đăng ký:

```text
Full name
Email
Password
Confirm password
Phone, optional
```

### 5.1.2. Đăng nhập

Chức năng:

- Đăng nhập bằng email/password.
- Trả về access token và refresh token.
- Lưu thông tin user cơ bản.
- Điều hướng theo role.

### 5.1.3. Quên mật khẩu

Chức năng:

- Nhập email.
- Gửi link reset password.
- Token reset có thời hạn.
- Đổi mật khẩu mới.

### 5.1.4. Hồ sơ cá nhân

Chức năng:

- Xem thông tin cá nhân.
- Cập nhật họ tên.
- Cập nhật avatar.
- Cập nhật số điện thoại.
- Đổi mật khẩu.
- Xem khóa học đã mua.
- Xem lịch sử học tập.
- Xem lịch sử đơn hàng.

---
