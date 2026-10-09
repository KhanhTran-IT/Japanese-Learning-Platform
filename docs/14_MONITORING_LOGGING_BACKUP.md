> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

## 17. Monitoring, logging và backup

### 17.1. Monitoring

Nên có:

```text
Spring Actuator health check
CPU/RAM monitoring
Request count
Response time
Error rate
Database connection pool
Redis health
Disk usage
```

### 17.2. Logging

Nên log:

```text
Lỗi 500
Request quan trọng
Thanh toán
Đăng nhập thất bại nhiều lần
Webhook payment
Upload file
Admin thay đổi dữ liệu quan trọng
```

Không log:

```text
Password
JWT token
Refresh token
Payment secret
Thông tin nhạy cảm không cần thiết
```

### 17.3. Backup

Cần backup:

```text
Database hằng ngày
File upload/object storage
File cấu hình server
Docker compose
.env production, lưu an toàn
```

---
