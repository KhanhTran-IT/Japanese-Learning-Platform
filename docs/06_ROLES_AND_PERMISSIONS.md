> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

## 6. Phân quyền hệ thống

### 6.1. Danh sách role

```text
SUPER_ADMIN
ADMIN
TEACHER
CONTENT_EDITOR
STUDENT
GUEST
```

### 6.2. Quyền của SUPER_ADMIN

```text
Toàn quyền hệ thống
Quản lý admin khác
Quản lý cấu hình website
Quản lý thanh toán
Quản lý doanh thu
Quản lý toàn bộ dữ liệu
```

### 6.3. Quyền của ADMIN

```text
Quản lý user
Quản lý khóa học
Quản lý bài học
Quản lý quiz
Quản lý đơn hàng
Xem báo cáo
Không được thay đổi cấu hình nhạy cảm nếu không được cấp quyền
```

### 6.4. Quyền của TEACHER

```text
Tạo/sửa khóa học của mình
Tạo/sửa bài học của mình
Tạo quiz cho khóa học của mình
Xem học viên trong khóa học của mình
Không được xem doanh thu toàn hệ thống
```

### 6.5. Quyền của CONTENT_EDITOR

```text
Quản lý bài viết
Quản lý từ vựng
Quản lý Kanji
Quản lý ngữ pháp
Quản lý nội dung học tập
Không được quản lý thanh toán
```

### 6.6. Quyền của STUDENT

```text
Xem khóa học
Mua khóa học
Học bài
Làm quiz
Chơi game
Xem tiến độ
Cập nhật hồ sơ cá nhân
```

### 6.7. Quyền của GUEST

```text
Xem trang chủ
Xem danh sách khóa học
Xem chi tiết khóa học
Xem bài học miễn phí
Đăng ký tài khoản
Đăng nhập
```

---
