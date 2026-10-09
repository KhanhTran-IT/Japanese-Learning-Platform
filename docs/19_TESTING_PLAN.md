> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

## 24. Testing đề xuất

### 24.1. Backend test

```text
Unit test service quan trọng
Integration test auth/payment/course
Test API bằng Postman/Swagger
Test phân quyền
Test payment webhook
Test validate dữ liệu
```

### 24.2. Frontend test

```text
Test login/register
Test route guard
Test mua khóa học
Test học bài
Test quiz
Test responsive mobile/tablet/desktop
```

### 24.3. Load testing

Nên test:

```text
Trang chủ
Danh sách khóa học
Chi tiết khóa học
Login
Trang học bài
Submit quiz
Leaderboard
```

Công cụ:

```text
JMeter
k6
Locust
```

---

## 25. Những lỗi cần tránh

```text
Không thiết kế database trước khi code
Không làm rõ nghiệp vụ thanh toán
Không trả entity trực tiếp ra frontend
Không lưu video trong database
Không query toàn bộ dữ liệu không phân trang
Không bỏ qua phân quyền admin/user
Không hard-code secret key
Không lưu password dạng plain text
Không dùng localStorage bừa bãi cho dữ liệu nhạy cảm
Không dùng microservice quá sớm
Không bỏ qua backup database
Không deploy production mà chưa có HTTPS
Không để database public internet
Không xử lý webhook thanh toán qua loa
```

---
