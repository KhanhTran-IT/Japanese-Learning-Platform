> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

## 20. Lộ trình phát triển theo giai đoạn

## Giai đoạn 1: MVP nền tảng chính

Mục tiêu:

```text
Có website học được, mua được, admin quản lý được.
```

Chức năng:

```text
Đăng ký/đăng nhập
Phân quyền user/admin
Trang chủ
Danh sách khóa học
Chi tiết khóa học
Bài học miễn phí
Khóa học trả phí
Admin CRUD khóa học
Admin CRUD chương
Admin CRUD bài học
Upload ảnh/file
Tiến độ học bài
Đơn hàng cơ bản
Thanh toán cơ bản
```

Kết quả:

```text
User có thể đăng ký, đăng nhập, xem khóa học, mua khóa học, học bài.
Admin có thể quản lý khóa học và bài học.
```

---

## Giai đoạn 2: Quiz và học tương tác

Mục tiêu:

```text
Người học có thể luyện tập và kiểm tra kiến thức.
```

Chức năng:

```text
Quiz cuối bài
Tạo bộ câu hỏi
Chấm điểm tự động
Lưu lịch sử làm bài
Hiển thị kết quả
Gợi ý học lại bài yếu
Flashcard từ vựng
```

---

## Giai đoạn 3: Game hóa

Mục tiêu:

```text
Tăng tương tác, giữ chân học viên.
```

Chức năng:

```text
XP
Level
Streak
Daily task
Mini game từ vựng
Mini game Kanji
Leaderboard
Badge thành tích
```

---

## Giai đoạn 4: JLPT chuyên sâu

Mục tiêu:

```text
Biến web thành nền tảng học JLPT nghiêm túc.
```

Chức năng:

```text
Kho từ vựng N5-N1
Kho Kanji N5-N1
Kho ngữ pháp N5-N1
Đề thi thử JLPT
Phân tích kết quả
Gợi ý lộ trình học
```

---

## Giai đoạn 5: Production scale

Mục tiêu:

```text
Chạy ổn định với nhiều người dùng.
```

Công việc:

```text
Redis cache
CDN
Nginx reverse proxy
Docker production
CI/CD
Database backup
Monitoring
Logging
Rate limit
Tối ưu query
Tối ưu frontend bundle
Load testing
Fix bugs
```

---

## 21. Chia sprint chuyên nghiệp

### Sprint 1: Nền tảng hệ thống

```text
Setup backend Spring Boot
Setup frontend Vue 3
Setup MySQL
Setup base response
Setup global exception
Setup Swagger
Setup Docker dev
Setup Spring Security JWT
Login/register
Role permission cơ bản
```

### Sprint 2: Khóa học

```text
CRUD course
CRUD section
CRUD lesson
Upload ảnh/file
Trang danh sách khóa học
Trang chi tiết khóa học
Trang học bài
```

### Sprint 3: Học viên và tiến độ

```text
Enrollment
Lesson progress
Dashboard học viên
Khóa học của tôi
Hoàn thành bài học
Tính phần trăm khóa học
```

### Sprint 4: Thanh toán

```text
Order
Payment
Coupon
Webhook
Mở khóa học tự động
Lịch sử giao dịch
Email xác nhận
```

### Sprint 5: Quiz

```text
CRUD quiz
CRUD question
CRUD answer
Làm bài quiz
Chấm điểm
Lưu kết quả
Hiển thị kết quả
```

### Sprint 6: Flashcard và game hóa

```text
Flashcard deck
Flashcard item
Review flashcard
XP
Level
Streak
Leaderboard
Daily task
```

### Sprint 7: Admin dashboard

```text
Thống kê user
Thống kê doanh thu
Thống kê khóa học
Quản lý user
Quản lý đơn hàng
Cấu hình website
```

### Sprint 8: Production

```text
Cache Redis
Index database
Docker production
Nginx
SSL
Backup
Monitoring
Load testing
Fix bugs
```

---
