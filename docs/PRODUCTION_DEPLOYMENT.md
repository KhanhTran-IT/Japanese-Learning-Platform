# Hướng dẫn Triển khai Production (Production Deployment)

Tài liệu này hướng dẫn cách triển khai hệ thống Japanese Learning Web lên môi trường Production sử dụng Docker Compose với đầy đủ các tiêu chuẩn bảo mật Runtime Hardening, phân quyền tối thiểu (Least Privilege), và cô lập mạng.

Môi trường Production sử dụng profile `prod` (`SPRING_PROFILES_ACTIVE=prod`), kích hoạt các tính năng bảo mật:
- Tắt Swagger UI và API Docs.
- Bắt buộc kiểm tra Media URL sử dụng HTTPS (`app.media.require-https=true`).
- Cấu hình Redis Rate Limiter sử dụng policy `FAIL_CLOSED`.
- Kích hoạt xác thực mật khẩu cho Redis.
- Bắt buộc người dùng cơ sở dữ liệu ứng dụng phân quyền tối thiểu (không được dùng tài khoản `root`).
- Tiến trình Java chạy dưới tài khoản non-root (`appuser:appgroup` UID/GID: 10001).
- Giới hạn tài nguyên (CPU Quotas và Memory Limits) cho từng container.
- Thu hẹp phơi nhiễm cổng ra ngoài máy chủ (chỉ mở API qua `127.0.0.1` cho Reverse Proxy; DB và Redis chỉ giao tiếp mạng nội bộ).

---

## 1. Yêu cầu hệ thống

- Docker và Docker Compose (v2) hoặc Podman Compose đã được cài đặt.
- Ít nhất 2GB RAM và 2 CPU cores.
- Phân vùng ổ cứng khả dụng tối thiểu 10GB cho dữ liệu DB và lưu trữ sao lưu.

---

## 2. Kiến trúc Tăng cường Bảo mật Runtime (Hardened Runtime Architecture)

```
       [ Public Internet (HTTPS 443) ]
                     │
                     ▼
       ┌───────────────────────────┐
       │   Reverse Proxy (Nginx /  │
       │     Caddy / Traefik)      │
       └─────────────┬─────────────┘
                     │ HTTP (127.0.0.1:8080)
                     ▼
       ┌───────────────────────────┐
       │ backend (Spring Boot 3)   │
       │ - Non-Root (appuser:10001)│
       │ - Health: /actuator/health│
       │ - User: jl_app_user       │
       │ - Limits: 1.5 CPU / 1GB   │
       └──────┬─────────────┬──────┘
              │             │
  (Internal Docker Bridge: app-network-prod)
              │             │
              ▼             ▼
┌──────────────────┐  ┌──────────────────┐
│ db (MariaDB 10)  │  │ redis (Redis 7)  │
│ - Expose: 3306   │  │ - Expose: 6379   │
│ - No Host Ports  │  │ - No Host Ports  │
│ - Limits: 1C/512M│  │ - Limits: .5C/256M│
└──────────────────┘  └──────────────────┘
```

### 2.1. Phân quyền Tối thiểu Cơ sở dữ liệu (Least-Privilege Database User)
- **Tách biệt tài khoản:** Tuyệt đối không để ứng dụng Backend kết nối bằng tài khoản `root`.
- **Tài khoản ứng dụng (`DB_USER=japanese_learning_user`):**
  - Giới hạn phạm vi nghiêm ngặt duy nhất trong database `japanese_learning_db.*`.
  - Không có quyền trên các database hệ thống (`mysql`, `information_schema`, `sys`).
  - Không có quyền quản trị toàn cục (`SUPER`, `SHUTDOWN`, `GRANT OPTION`, `RELOAD`, `FILE`, `PROCESS`).
  - Quyền hạn vừa đủ gồm:
    - **DML (`SELECT, INSERT, UPDATE, DELETE`):** Phục vụ các thao tác nghiệp vụ hàng ngày của JPA/Hibernate.
    - **DDL (`CREATE, ALTER, INDEX, DROP, REFERENCES`):** Phục vụ công cụ Flyway tự động thực thi migration khi ứng dụng khởi động.
- **Tài khoản quản trị (`root`):**
  - Mật khẩu độc lập (`DB_ROOT_PASSWORD`), chỉ dùng khi bảo trì, thiết lập khởi tạo, hoặc chạy tác vụ sao lưu/phục hồi chuyên dụng.

### 2.2. Chạy ứng dụng dưới tài khoản Non-Root (Container Non-Root User)
- Trong `Dockerfile`, ứng dụng tạo nhóm và người dùng không có đặc quyền:
  - Nhóm `appgroup` (GID 10001).
  - Người dùng `appuser` (UID 10001).
- Toàn bộ tệp `.jar` được gán quyền sở hữu `appuser:appgroup` và chỉ thị `USER appuser:appgroup` được áp dụng trước khi khởi chạy.
- Ngăn chặn triệt để nguy cơ leo thang đặc quyền (Container Breakout) hoặc chỉnh sửa trái phép các tệp nhạy cảm trong hệ điều hành máy chủ.

### 2.3. Giảm thiểu Phơi nhiễm Cổng Host (Reduced Host Exposure)
- **MariaDB (cổng 3306) và Redis (cổng 6379):**
  - Đã loại bỏ hoàn toàn ánh xạ cổng ra ngoài máy chủ (`ports: - "127.0.0.1:3306:3306"`).
  - Chuyển sang sử dụng `expose:` để các dịch vụ chỉ giao tiếp nội bộ qua mạng cầu nối `app-network-prod`.
  - Giảm thiểu triệt để nguy cơ bị quét cổng hoặc khai thác dịch vụ từ bên ngoài.
- **Backend API (cổng 8080):**
  - Được ánh xạ ràng buộc cục bộ: `"${BACKEND_BIND_IP:-127.0.0.1}:8080:8080"`.
  - Bắt buộc toàn bộ lưu lượng người dùng bên ngoài phải đi qua Reverse Proxy (Nginx/Caddy) để kiểm tra SSL/TLS, lọc WAF, và giới hạn tốc độ.

### 2.4. Giới hạn Tài nguyên (Resource Limits & Quotas)
Tất cả các dịch vụ trong `docker-compose.prod.yml` đều được khai báo trần tài nguyên (`limits`) và mức bảo đảm tối thiểu (`reservations`):
| Dịch vụ | CPU Limit | RAM Limit | CPU Reservation | RAM Reservation |
| :--- | :--- | :--- | :--- | :--- |
| **backend** | 1.5 cores | 1024 MB | 0.5 cores | 512 MB |
| **db** | 1.0 cores | 512 MB | 0.25 cores | 256 MB |
| **redis** | 0.5 cores | 256 MB | 0.1 cores | 64 MB |
| **backup** | 0.5 cores | 256 MB | 0.1 cores | 64 MB |

### 2.5. Kiểm tra Sức khỏe & Chuỗi Sẵn sàng (Healthchecks & Readiness Dependencies)
- **MariaDB:** `healthcheck.sh --connect --innodb_initialized` (chu kỳ 10s, timeout 5s, start-period 30s, thử lại 5 lần).
- **Redis:** `REDISCLI_AUTH="$REDIS_PASSWORD" redis-cli ping` (chu kỳ 10s, timeout 5s, start-period 10s, thử lại 3 lần).
- **Backend:** Actuator readiness probe `curl -f http://localhost:8080/actuator/health/readiness` (chu kỳ 15s, timeout 5s, start-period 60s, thử lại 3 lần).
- **Chuỗi phụ thuộc:** Service `backend` và service `backup` phụ thuộc trực tiếp vào trạng thái `condition: service_healthy` của `db` và `redis`, đảm bảo DB và Redis hoàn toàn sẵn sàng trước khi backend chạy migration.

---

## 3. Cơ chế Xác thực Cấu hình Khởi động (Fail-Fast Production Validation)

Hệ thống tích hợp bộ kiểm tra bảo mật khởi động sớm (`ProductionSecurityValidator`), tự động kích hoạt **DUY NHẤT** dưới profile `prod` (`SPRING_PROFILES_ACTIVE=prod`). 

> [!IMPORTANT]
> Ứng dụng Backend sẽ **DỪNG KHỞI ĐỘNG NGAY LẬP TỨC (FAIL-FAST)** và in ra toàn bộ danh sách lỗi chi tiết nếu phát hiện bất kỳ cấu hình nào sau đây:
> 1. **Database User là Root:** `spring.datasource.username` đặt là `root`. Bắt buộc phải sử dụng tài khoản chuyên dụng phân quyền tối thiểu (`japanese_learning_user`).
> 2. **Placeholder / Giá trị mẫu:** Bất kỳ giá trị nào chứa `CHANGE_ME`, `YOUR_STRONG_`, `SECRET_HERE`, `PASSWORD_HERE`, `Password@123`, `admin`, `password`...
> 3. **JWT Secrets:** Khóa truy cập (`jwt.secret.access`) và khóa làm mới (`jwt.secret.refresh`) bị trùng nhau, hoặc sau khi giải mã Base64 có độ dài nhỏ hơn 256 bits (32 bytes).
> 4. **CORS Origins:** Chứa `localhost`, `127.0.0.1`, ký tự đại diện `*`, thiếu giao thức `https://`, hoặc chứa dấu gạch chéo cuối (`/`).
> 5. **Redis Configuration:** Host trỏ về `localhost` / `127.0.0.1`, cổng không hợp lệ, hoặc mật khẩu Redis bị bỏ trống / là placeholder.
> 6. **Database Configuration:** URL sử dụng H2 in-memory, hoặc mật khẩu cơ sở dữ liệu ngắn hơn 8 ký tự / là placeholder.
> 7. **Admin Password:** Mật khẩu Admin khởi tạo ngắn hơn 12 ký tự hoặc là giá trị mặc định / placeholder.
> 8. **Cookie Secure:** Thuộc tính `app.cookie.secure` bị đặt thành `false` (bắt buộc `true` trên môi trường Production HTTPS).
> 9. **Payment Simulation:** `app.payment.simulation-enabled` bị bật thành `true` trên Production.

*Lưu ý:* Môi trường phát triển cục bộ (`dev`) và kiểm thử (`test`) hoạt động hoàn toàn tách biệt, sử dụng file `application-dev.yml` và in-memory database mà không bị ảnh hưởng bởi bộ xác thực production.

---

## 4. Các bước triển khai cho Môi trường Mới (Greenfield Deployment)

### Bước 1: Chuẩn bị tệp tin cấu hình môi trường (.env.prod)
```bash
cp .env.prod.example .env.prod
chmod 600 .env.prod
```

Mở tệp `.env.prod` và thay đổi **tất cả** các giá trị mặc định sang giá trị thực:
- `DB_USER`: Đặt tên người dùng ứng dụng (mặc định `japanese_learning_user`).
- `DB_PASSWORD`: Đặt mật khẩu mạnh cho `DB_USER` (tối thiểu 12 ký tự, không chứa placeholder).
- `DB_ROOT_PASSWORD`: Mật khẩu cho tài khoản `root` quản trị database (tối thiểu 12 ký tự).
- `BACKEND_BIND_IP`: Giữ nguyên `127.0.0.1` để chỉ cho phép Reverse Proxy truy cập.
- `REDIS_PASSWORD`: Mật khẩu an toàn cho Redis (tối thiểu 12 ký tự).
- `ADMIN_PASSWORD`: Mật khẩu khởi tạo cho tài khoản Quản trị viên (Admin) (tối thiểu 12 ký tự).
- `JWT_ACCESS_SECRET` và `JWT_REFRESH_SECRET`: Sử dụng 2 chuỗi Base64 ngẫu nhiên khác nhau (tối thiểu 32 bytes decoded):
  ```bash
  openssl rand -base64 32
  openssl rand -base64 32
  ```
- `BACKUP_ENCRYPTION_KEY`: Chuỗi bí mật mã hóa sao lưu AES-256 (tối thiểu 32 ký tự).
- `APP_CORS_ALLOWED_ORIGINS`: Danh sách domain chính thức (ví dụ: `https://yourdomain.com`).

### Bước 2: Đóng gói ứng dụng Backend
```bash
cd backend
./mvnw clean package -DskipTests
cd ..
```

### Bước 3: Khởi chạy các container bằng Docker Compose
```bash
docker compose -f docker-compose.prod.yml up -d --build
```

### Bước 4: Kiểm tra trạng thái và log
```bash
# Kiểm tra trạng thái healthcheck của tất cả container
docker compose -f docker-compose.prod.yml ps

# Xem log khởi động của backend
docker logs -f japanese_learning_api_prod
```

Khi thấy thông báo:
```text
Production security configuration validation PASSED. All required secrets and settings are verified.
Started JapaneseLearningApplication in ... seconds
```
Ứng dụng đã khởi động thành công và an toàn.

---

## 5. Quy trình Di chuyển Hệ thống Hiện tại (Migration Steps for Existing Deployments)

Nếu bạn đang nâng cấp một máy chủ Production đã triển khai từ trước (dữ liệu MariaDB đã tồn tại trong volume `db_data_prod` nhưng trước đây backend kết nối bằng `root`):

> [!WARNING]
> Khi MariaDB đã có dữ liệu trong volume `/var/lib/mysql`, container MariaDB sẽ **KHÔNG** tự động tạo người dùng mới từ biến môi trường `MARIADB_USER`. Bạn cần thực hiện các bước sau để khởi tạo quyền cho người dùng mới trước khi khởi động phiên bản backend mới.

### Bước 1: Kết nối vào container MariaDB hiện tại với quyền Root
```bash
docker exec -it japanese_learning_db_prod mariadb -u root -p
# Nhập mật khẩu root hiện tại của MariaDB
```

### Bước 2: Tạo User ứng dụng và Phân quyền Tối thiểu
Thực thi các câu lệnh SQL sau trong MariaDB prompt:
```sql
-- Tạo người dùng ứng dụng chuyên dụng
CREATE USER IF NOT EXISTS 'japanese_learning_user'@'%' IDENTIFIED BY 'YOUR_NEW_STRONG_APP_PASSWORD';

-- Phân quyền DML (thao tác dữ liệu) và DDL (Flyway migration) trên database ứng dụng
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES 
ON japanese_learning_db.* TO 'japanese_learning_user'@'%';

-- Nạp lại bảng quyền
FLUSH PRIVILEGES;

-- Kiểm tra lại quyền đã cấp
SHOW GRANTS FOR 'japanese_learning_user'@'%';

-- Thoát MariaDB
EXIT;
```

### Bước 3: Cập nhật tệp `.env.prod`
Thêm hoặc cập nhật các biến:
```bash
DB_USER=japanese_learning_user
DB_PASSWORD=YOUR_NEW_STRONG_APP_PASSWORD
DB_ROOT_PASSWORD=YOUR_EXISTING_ROOT_PASSWORD
BACKEND_BIND_IP=127.0.0.1
```

### Bước 4: Build lại file `.jar` và Re-deploy
```bash
cd backend && ./mvnw clean package -DskipTests && cd ..
docker compose -f docker-compose.prod.yml up -d --build backend
```

### Bước 5: Xác nhận kiểm tra sau Di chuyển
1. **Kiểm tra Log Backend:**
   ```bash
   docker logs japanese_learning_api_prod | grep -i "HikariPool"
   ```
   Đảm bảo HikariCP kết nối thành công với `japanese_learning_user`.
2. **Kiểm tra tiến trình Non-Root:**
   ```bash
   docker top japanese_learning_api_prod
   ```
   Cột `UID` phải hiển thị `10001` (hoặc `appuser`), không phải `root` (0).
3. **Kiểm tra cổng Host:**
   ```bash
   ss -tulpn | grep -E '3306|6379'
   ```
   Không còn tiến trình nào mở cổng 3306 hoặc 6379 ra ngoài host public.

---

## 6. Xử lý sự cố (Troubleshooting)

- **Lỗi `Production configuration validation failed: [Database] 'spring.datasource.username' must not be 'root' in production`:**
  Backend phát hiện bạn vẫn đang dùng user `root`. Hãy cập nhật `DB_USER=japanese_learning_user` trong `.env.prod`.
- **Lỗi `Access denied for user 'japanese_learning_user'@'%' to database 'japanese_learning_db'`:**
  Tài khoản ứng dụng chưa được cấp đủ quyền. Thực hiện lại lệnh `GRANT ...` ở Mục 5, Bước 2.
- **Lỗi `FlywayException: Access denied` khi chạy migration:**
  User ứng dụng thiếu quyền DDL. Kiểm tra đảm bảo các quyền `CREATE, ALTER, INDEX, DROP, REFERENCES` đã được cấp cho `japanese_learning_user`.
- **Lỗi `NOAUTH Authentication required` từ Redis:**
  Kiểm tra `REDIS_PASSWORD` trong `.env.prod` có khớp với cấu hình `redis-server --requirepass` trong `docker-compose.prod.yml`.
- **Rate Limiting chặn tất cả (503):**
  Redis sập khi sử dụng policy `FAIL_CLOSED`. Khởi động lại Redis:
  ```bash
  docker restart japanese_learning_redis_prod
  ```

---

## 7. Quản lý Sao lưu & Phục hồi Thảm họa (Backup & Disaster Recovery)

Hệ thống tích hợp bộ công cụ sao lưu chuẩn hóa tại thư mục `scripts/backup/`:
- **Sao lưu hàng ngày:** Thực hiện tự động lúc 02:00 UTC thông qua cron:
  ```bash
  ./scripts/backup/backup.sh
  ```
  File sao lưu được nén `gzip -9`, mã hóa `AES-256-CBC PBKDF2` (100k vòng lặp), tạo mã băm `SHA-256`, áp dụng chính sách lưu trữ phân tầng GFS (7 ngày / 4 tuần / 12 tháng) và đồng bộ lên S3 / Remote Storage.
- **Kiểm tra tính toàn vẹn độc lập (Isolated Verification):** Chạy kiểm tra mà **KHÔNG ẢNH HƯỞNG DATABASE PRODUCTION**:
  ```bash
  ./scripts/backup/verify-restore.sh /path/to/backup.sql.gz.enc
  ```
- **Phục hồi có kiểm soát:** Chốt chặn an toàn yêu cầu xác nhận trước khi phục hồi:
  ```bash
  ./scripts/backup/restore.sh /path/to/backup.sql.gz.enc --confirm-destructive-overwrite
  ```
- **Chiến lược Redis:** Dữ liệu Redis chỉ phục vụ Rate Limiting (cửa sổ 15 phút) và Readiness health checks. Redis không chứa session hay dữ liệu nghiệp vụ lâu dài nên **không cần backup ngoại biên** và sẽ được **tái tạo tức thì khi khởi động lại**.
- Chi tiết xem tài liệu kiến trúc đầy đủ tại [DATABASE_BACKUP_RECOVERY.md](./DATABASE_BACKUP_RECOVERY.md).

---

## 8. Kiểm thử Tự động & CI/CD Pipeline (Continuous Integration)

Để đảm bảo không bị lỗi hồi quy (regression) và bảo vệ chất lượng mã nguồn trước khi deploy, dự án tích hợp pipeline GitHub Actions tự động (`.github/workflows/ci.yml`).

### Các Job trong Pipeline:
1. **Backend CI (`backend`)**: Sử dụng JDK 21 (Eclipse Temurin), cache Maven, và chạy `mvn -B clean verify`.
2. **Frontend CI (`frontend`)**: Sử dụng Node.js 22, cache npm dependencies, chạy `npm ci`, sau đó chạy `npm run test` (Vitest) và `npm run build` (Vite).
3. **Docker Config CI (`docker-config`)**: Tự động kiểm tra tính hợp lệ của file `docker-compose.prod.yml` thông qua tệp mẫu `.env.prod.example` mà không cần secret thật.

### Lệnh chạy kiểm thử cục bộ:
```bash
# 1. Kiểm thử backend
cd backend && mvn -B clean verify && cd ..

# 2. Kiểm thử và build frontend
cd frontend && npm ci && npm run test && npm run build && cd ..

# 3. Kiểm tra static cấu hình Docker Compose production
cp .env.prod.example .env.prod
docker compose -f docker-compose.prod.yml config
```
