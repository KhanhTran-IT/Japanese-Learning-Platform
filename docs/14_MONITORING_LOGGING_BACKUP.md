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

### 17.4. Runtime Hardening & Least Privilege

> 📖 **Xem chi tiết tài liệu triển khai production:** [PRODUCTION_DEPLOYMENT.md](./PRODUCTION_DEPLOYMENT.md)

1. **Phân quyền tối thiểu cơ sở dữ liệu (Least-Privilege DB User):**
   - Backend sử dụng tài khoản `japanese_learning_user` chỉ có quyền trên `japanese_learning_db.*` (`SELECT, INSERT, UPDATE, DELETE` cho DML và `CREATE, ALTER, INDEX, DROP, REFERENCES` cho Flyway migrations).
   - Tuyệt đối cấm kết nối bằng `root` trên môi trường Production (được xác thực tự động bởi `ProductionSecurityValidator`).
   - Mật khẩu quản trị `root` (`DB_ROOT_PASSWORD`) tách biệt, chỉ dùng cho bảo trì và runner sao lưu.
2. **Tiến trình Non-Root:**
   - Ứng dụng Backend chạy dưới tài khoản `appuser:appgroup` (UID/GID: 10001), ngăn chặn nguy cơ leo thang đặc quyền container breakout.
3. **Giới hạn tài nguyên (Resource Limits):**
   - Thiết lập CPU và RAM limits/reservations cho mọi container (`backend`, `db`, `redis`, `backup`) để chống cạn kiệt tài nguyên máy chủ.
4. **Cô lập mạng & Giảm thiểu phơi nhiễm (Host Exposure Reduction):**
   - MariaDB (3306) và Redis (6379) không publish cổng ra ngoài host, chỉ giao tiếp nội bộ qua bridge network.
   - Backend API 8080 ràng buộc `127.0.0.1`, bắt buộc mọi request bên ngoài phải đi qua Reverse Proxy (HTTPS + WAF).
5. **Kiểm tra sức khỏe & Sẵn sàng (Healthchecks & Readiness Dependencies):**
   - MariaDB (`healthcheck.sh`), Redis (`redis-cli ping`), Backend (`/actuator/health/readiness`).
   - Docker Compose cấu hình `depends_on` với `condition: service_healthy`.

---


