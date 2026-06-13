> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

## 4. Kiến trúc hệ thống đề xuất

### 4.1. Kiến trúc tổng thể

```text
User Browser
   |
   | HTTPS
   v
Nginx / Reverse Proxy
   |
   |-----------------------------
   |                            |
Vue Frontend                Spring Boot API
                                |
        ------------------------------------------------
        |             |              |                 |
      MySQL         Redis       Object Storage      Payment Gateway
        |
   Backup System

Monitoring / Logging / Alerting
```

### 4.2. Kiến trúc backend

Khuyến nghị dùng:

```text
Modular Monolith
```

Tức là một project Spring Boot duy nhất nhưng chia module rõ ràng:

```text
module_auth
module_user
module_course
module_lesson
module_payment
module_quiz
module_game
module_progress
module_notification
module_admin
module_report
```

Lý do không nên dùng microservices ngay từ đầu:

- Team nhỏ sẽ khó quản lý.
- Tốn thời gian deploy.
- Khó debug.
- Khó đồng bộ transaction.
- Chưa cần thiết khi sản phẩm chưa có lượng user cực lớn.

Khi hệ thống lớn hơn, có thể tách dần:

- Payment Service.
- Notification Service.
- Media Service.
- Game Service.
- Search Service.

### 4.3. Kiến trúc frontend

Frontend chia theo layout:

```text
Public Layout
Student Layout
Admin Layout
Teacher Layout
```

Public Layout dùng cho:

- Trang chủ.
- Danh sách khóa học.
- Chi tiết khóa học.
- Blog.
- Login/Register.

Student Layout dùng cho:

- Dashboard học viên.
- Khóa học của tôi.
- Trang học bài.
- Quiz.
- Flashcard.
- Game.
- Hồ sơ cá nhân.

Admin Layout dùng cho:

- Dashboard admin.
- Quản lý user.
- Quản lý khóa học.
- Quản lý bài học.
- Quản lý quiz.
- Quản lý đơn hàng.
- Báo cáo.

---
