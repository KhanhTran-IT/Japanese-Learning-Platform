# 23. MVP_SCOPE - Phạm vi phiên bản đầu tiên

## 1. Mục đích của file

File này dùng để khóa phạm vi MVP, tránh tình trạng dự án quá lớn, làm nhiều tháng nhưng chưa có bản chạy được.

MVP không phải là bản hoàn hảo nhất. MVP là bản nhỏ nhất nhưng đủ để chứng minh hệ thống có thể vận hành thật:

- Người dùng đăng ký / đăng nhập được.
- Người dùng xem khóa học được.
- Người dùng học bài được.
- Hệ thống lưu tiến độ học được.
- Admin tạo và quản lý nội dung học được.

## 2. Nguyên tắc MVP

```text
Không làm tất cả ngay từ đầu.
Làm lõi học online trước.
Làm chắc Auth + Course + Lesson + Progress + Admin CRUD.
Thanh toán, quiz, game, flashcard, AI để sau nếu chưa cần.
```

## 3. MVP bắt buộc phải có - P0

### 3.1. Auth cơ bản

Bắt buộc có:

- Đăng ký tài khoản bằng email/password.
- Đăng nhập bằng email/password.
- Mã hóa mật khẩu bằng BCrypt.
- JWT access token.
- Refresh token lưu database.
- Logout và revoke refresh token.
- API `/api/users/me` để lấy thông tin user hiện tại.
- Role tối thiểu: `ADMIN`, `STUDENT`.

Chưa bắt buộc ở MVP:

- Đăng nhập Google/Facebook.
- Xác thực email bắt buộc.
- 2FA.
- Login bằng số điện thoại.

### 3.2. Public website

Bắt buộc có:

- Trang chủ.
- Danh sách khóa học.
- Chi tiết khóa học.
- Hiển thị khóa học theo level: N5, N4, N3, N2, N1.
- Tìm kiếm khóa học theo từ khóa.
- Lọc khóa học theo miễn phí/trả phí.
- Xem bài học preview miễn phí.

Chưa bắt buộc:

- Blog đầy đủ.
- Landing page phức tạp.
- SEO nâng cao.
- Review/rating nâng cao.

### 3.3. Course/Lesson core

Bắt buộc có:

- Admin tạo/sửa/xóa khóa học.
- Admin publish/hide khóa học.
- Admin tạo/sửa/xóa chương học.
- Admin tạo/sửa/xóa bài học.
- Bài học hỗ trợ text content.
- Bài học có thể gắn video URL/audio URL/file tài liệu.
- Bài học có sort order.
- Bài học có trạng thái `DRAFT`, `PUBLISHED`, `HIDDEN`.

Chưa bắt buộc:

- Video streaming chuyên nghiệp.
- DRM bảo vệ video.
- Subtitle đa ngôn ngữ.
- Lesson comment.

### 3.4. Enrollment và progress

Bắt buộc có:

- Student enroll khóa học miễn phí.
- Student truy cập khóa học đã enroll.
- Hệ thống lưu bài học đã hoàn thành.
- Hệ thống lưu phần trăm video đã xem nếu có.
- Hệ thống tính phần trăm hoàn thành khóa học.
- Student xem danh sách khóa học của mình.

Chưa bắt buộc:

- Chứng chỉ hoàn thành.
- Gợi ý học lại bằng AI.
- Thống kê học tập nâng cao.

### 3.5. Admin dashboard MVP

Bắt buộc có:

- Tổng số user.
- Tổng số khóa học.
- Tổng số bài học.
- Tổng số học viên đã ghi danh.
- Danh sách user mới gần đây.
- Danh sách khóa học mới gần đây.

Chưa bắt buộc:

- Biểu đồ doanh thu nâng cao.
- Export Excel.
- Báo cáo học tập phức tạp.

## 4. MVP nên có nếu còn thời gian - P1

- Quên mật khẩu qua email.
- Xác thực email.
- Upload avatar.
- Upload thumbnail khóa học.
- Quiz đơn giản cuối bài dạng single choice.
- Đơn hàng giả lập cho khóa học trả phí.
- Thanh toán thủ công/bank transfer trước khi tích hợp VNPay/Momo.

## 5. Không đưa vào MVP - P2/P3

Các chức năng sau để sau MVP:

- Flashcard nâng cao.
- Spaced Repetition.
- Game học tiếng Nhật.
- XP, level, badge, streak.
- Leaderboard.
- JLPT mock test đầy đủ.
- AI tutor.
- Mobile app.
- Membership toàn hệ thống.
- Coupon nâng cao.
- Nhiều cổng thanh toán cùng lúc.
- Notification realtime.

## 6. Definition of Done cho MVP

MVP chỉ được xem là hoàn thành khi:

```text
[ ] Backend chạy ổn bằng Spring Boot.
[ ] Frontend chạy ổn bằng Vue 3.
[ ] User đăng ký/đăng nhập được.
[ ] Admin tạo khóa học/chương/bài học được.
[ ] Public xem danh sách/chi tiết khóa học được.
[ ] Student enroll khóa học miễn phí được.
[ ] Student học bài và lưu tiến độ được.
[ ] API có response chuẩn.
[ ] API quan trọng có validate.
[ ] API admin có phân quyền.
[ ] Có dữ liệu seed để demo.
[ ] Có thể demo toàn bộ flow từ đăng ký đến học bài.
```

## 7. Prompt dùng với AI

```text
Hãy đọc 00_MASTER_CONTEXT.md và 23_MVP_SCOPE.md.
Trong giai đoạn hiện tại, chỉ được tập trung vào MVP.
Không đề xuất code game, AI, leaderboard, payment nâng cao nếu tôi chưa yêu cầu.
Hãy giúp tôi chia task nhỏ để hoàn thành MVP theo thứ tự đúng.
```
