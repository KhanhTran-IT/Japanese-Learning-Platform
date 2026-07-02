# CURRENT TASK

## Task hiện tại
Frontend Auth UI & Integration (Giao diện và Tích hợp Đăng nhập/Đăng ký)

## Trạng thái
TODO

## Mục tiêu
Xây dựng giao diện cho màn hình Đăng nhập (Login) và Đăng ký (Register) bằng Vue 3. Tích hợp các form này với Backend API thông qua Axios client đã thiết lập, và lưu trữ trạng thái người dùng vào Pinia store.

## Vì sao làm task này?
Đây là cánh cổng bắt buộc để người dùng bước vào hệ thống. Việc xử lý tốt luồng xác thực ở Frontend (lưu token, điều hướng dựa trên role) là yêu cầu tiên quyết (P0) của MVP để mở khóa các tính năng như xem khóa học, học bài và dashboard.

## Không làm trong task này
- Không làm tính năng Đăng nhập bằng Google/Facebook (Chưa thuộc MVP).
- Không làm tính năng Quên mật khẩu/Reset mật khẩu.

## File tài liệu cần dùng
- Yêu cầu MVP: `docs/23_MVP_SCOPE.md` (Mục 3.1. Auth cơ bản).

## Cấu trúc luồng xử lý (Logic)
1. **Validation (Xác thực Form):**
   - Form Login: Yêu cầu email đúng định dạng, password không được để trống.
   - Form Register: Email chuẩn, password tối thiểu 6 ký tự, nhập lại password phải khớp.
2. **Gọi API & Xử lý State:**
   - Khi submit form Login, gọi action trong Pinia (`auth.store.js`). Action này sẽ dùng Axios gọi `POST /api/v1/auth/login`.
   - Lưu Access Token vào LocalStorage hoặc Cookie, lưu thông tin User vào Pinia state.
3. **Điều hướng (Navigation):**
   - Sau khi Login thành công, kiểm tra Role của user.
   - Nếu là `ADMIN` -> Đẩy về `/admin/dashboard`.
   - Nếu là `STUDENT` -> Đẩy về `/student/dashboard` hoặc `/courses`.

## Cần tạo hoặc chỉnh sửa
- `src/pages/auth/LoginPage.vue`: Code UI form đăng nhập và xử lý sự kiện submit.
- `src/pages/auth/RegisterPage.vue`: Code UI form đăng ký.
- `src/stores/auth.store.js`: Bổ sung các actions thực thi việc gọi service và set state.
- `src/services/auth.service.js`: Định nghĩa các hàm `login(credentials)` và `register(data)`.

## Checklist
- [ ] Xây dựng form Login và Register với tính năng validation hiển thị lỗi thân thiện.
- [ ] Tích hợp API Đăng ký, hiển thị thông báo thành công và chuyển hướng sang trang Login.
- [ ] Tích hợp API Đăng nhập, lưu token an toàn.
- [ ] Điều hướng (Router push) chính xác sau khi đăng nhập thành công dựa vào Role.
- [ ] Cấu hình Navigation Guards (`router/guards.js`) để chặn người dùng chưa đăng nhập truy cập vào route `/student/*` hoặc `/admin/*`.

## Kết quả mong muốn
Hệ thống Frontend đã hoàn chỉnh chu trình xác thực. Người dùng có thể tạo tài khoản mới, đăng nhập thành công, và bị chặn lại một cách an toàn nếu cố gắng truy cập các trang nội bộ mà không có tài khoản.