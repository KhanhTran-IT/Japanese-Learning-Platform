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

### 17.3. Backup & Disaster Recovery

> 📖 **Xem chi tiết tài liệu kiến trúc & quy trình vận hành:**  
> - [DATABASE_BACKUP_RECOVERY.md](./DATABASE_BACKUP_RECOVERY.md) — Chi tiết kiến trúc sao lưu MariaDB, mã hóa AES-256, kiểm tra độc lập, RPO/RTO, và diễn tập phục hồi.
> - [MONITORING_ALERTS.md](./MONITORING_ALERTS.md) — Giám sát Prometheus, cấu hình cảnh báo, và dashboard Grafana.

#### Dữ liệu Bắt buộc Sao lưu (MariaDB 10.11 InnoDB):
- Toàn bộ cơ sở dữ liệu `japanese_learning_db` (users, roles, courses, lessons, progress, quizzes, orders, payments, refresh tokens).
- Tần suất: Hàng ngày lúc 02:00 UTC (online non-blocking với `--single-transaction --quick`).
- Mã hóa: Chuẩn AES-256-CBC PBKDF2 (100,000 iterations).
- Toàn vẹn: Checksum SHA-256 đi kèm mọi bản sao lưu.
- Lưu trữ ngoại biên: Tự động đồng bộ lên S3 / Cloudflare R2 / Remote rsync.
- Lưu trữ phân tầng (GFS Retention): 7 ngày (Daily), 4 tuần (Weekly), 12 tháng (Monthly).

#### Định nghĩa Dữ liệu Redis (Persistence vs Rebuild):
- **Redis chỉ dùng cho Rate Limiting (cửa sổ 15 phút) và Readiness health checks.**
- Redis **KHÔNG** lưu phiên người dùng (refresh tokens lưu trong MariaDB).
- Dữ liệu Redis **KHÔNG CẦN BACKUP NGOẠI BIÊN** và có thể **TÁI TẠO TỨC THÌ (INSTANTLY REBUILT)** khi khởi động lại container mà không làm mất mát dữ liệu nghiệp vụ.

#### Mục tiêu Phục hồi:
- **RPO (Recovery Point Objective):** ≤ 24 giờ (Full dump) / ≤ 15 phút (với MariaDB binlog).
- **RTO (Recovery Time Objective):** < 30 phút.

---

