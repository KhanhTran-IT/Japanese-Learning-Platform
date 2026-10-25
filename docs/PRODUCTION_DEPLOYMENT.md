# Hướng dẫn Triển khai Production (Production Deployment)

Tài liệu này hướng dẫn cách triển khai hệ thống Japanese Learning Web lên môi trường Production sử dụng Docker Compose.

Môi trường Production sử dụng profile `prod` (`SPRING_PROFILES_ACTIVE=prod`), kích hoạt các tính năng bảo mật như:
- Tắt Swagger UI và API Docs.
- Bắt buộc kiểm tra Media URL sử dụng HTTPS (`app.media.require-https=true`).
- Cấu hình Redis Rate Limiter sử dụng policy `FAIL_CLOSED`.
- Kích hoạt xác thực mật khẩu cho Redis.

## Yêu cầu hệ thống
- Docker và Docker Compose (v2) đã được cài đặt.
- Ít nhất 2GB RAM và 2 CPU cores.

## Cơ chế Xác thực Cấu hình Khởi động (Fail-Fast Production Validation)

Hệ thống tích hợp bộ kiểm tra bảo mật khởi động sớm (`ProductionSecurityValidator`), tự động kích hoạt **DUY NHẤT** dưới profile `prod` (`SPRING_PROFILES_ACTIVE=prod`). 

> [!IMPORTANT]
> Ứng dụng Backend sẽ **DỪNG KHỞI ĐỘNG NGAY LẬP TỨC (FAIL-FAST)** và in ra toàn bộ danh sách lỗi chi tiết nếu phát hiện bất kỳ cấu hình nào sau đây:
> 1. **Placeholder / Giá trị mẫu:** Bất kỳ giá trị nào chứa `CHANGE_ME`, `YOUR_STRONG_`, `SECRET_HERE`, `PASSWORD_HERE`, `Password@123`, `admin`, `password`...
> 2. **JWT Secrets:** Khóa truy cập (`jwt.secret.access`) và khóa làm mới (`jwt.secret.refresh`) bị trùng nhau, hoặc sau khi giải mã Base64 có độ dài nhỏ hơn 256 bits (32 bytes).
> 3. **CORS Origins:** Chứa `localhost`, `127.0.0.1`, ký tự đại diện `*`, thiếu giao thức `https://`, hoặc chứa dấu gạch chéo cuối (`/`).
> 4. **Redis Configuration:** Host trỏ về `localhost` / `127.0.0.1`, cổng không hợp lệ, hoặc mật khẩu Redis bị bỏ trống / là placeholder.
> 5. **Database Configuration:** URL sử dụng H2 in-memory, hoặc mật khẩu cơ sở dữ liệu ngắn hơn 8 ký tự / là placeholder.
> 6. **Admin Password:** Mật khẩu Admin khởi tạo ngắn hơn 12 ký tự hoặc là giá trị mặc định / placeholder.
> 7. **Cookie Secure:** Thuộc tính `app.cookie.secure` bị đặt thành `false` (bắt buộc `true` trên môi trường Production HTTPS).
> 8. **Payment Simulation:** `app.payment.simulation-enabled` bị bật thành `true` trên Production.

*Lưu ý:* Môi trường phát triển cục bộ (`dev`) và kiểm thử (`test`) hoạt động hoàn toàn tách biệt, sử dụng file `application-dev.yml` và in-memory database mà không bị ảnh hưởng bởi bộ xác thực production.

## Các bước triển khai

### Bước 1: Chuẩn bị tệp tin cấu hình môi trường (.env.prod)
Không bao giờ commit tệp chứa thông tin nhạy cảm (như mật khẩu, secret key) lên Git. Thay vào đó, hãy copy từ file example và cấu hình lại cho máy chủ Production.

```bash
cp .env.prod.example .env.prod
```

Mở tệp `.env.prod` và thay đổi **tất cả** các giá trị mặc định sang giá trị thực:
- `DB_PASSWORD`: Đặt một mật khẩu an toàn và ngẫu nhiên cho MariaDB (tối thiểu 12 ký tự, không chứa placeholder).
- `REDIS_PASSWORD`: Đặt một mật khẩu an toàn cho Redis (tối thiểu 12 ký tự, không chứa placeholder).
- `ADMIN_PASSWORD`: Mật khẩu khởi tạo cho tài khoản Quản trị viên (Admin) (tối thiểu 12 ký tự, không dùng `Password@123`).
- `JWT_ACCESS_SECRET` và `JWT_REFRESH_SECRET`: Sử dụng 2 chuỗi Base64 ngẫu nhiên khác nhau, giải mã ra tối thiểu 256 bits (32 bytes). Bạn có thể tạo bằng lệnh:
  ```bash
  # Sinh chuỗi cho JWT_ACCESS_SECRET:
  openssl rand -base64 32
  
  # Sinh chuỗi KHÁC cho JWT_REFRESH_SECRET:
  openssl rand -base64 32
  ```
- `APP_CORS_ALLOWED_ORIGINS`: Danh sách domain chính thức của frontend phân tách bởi dấu phẩy (ví dụ: `https://yourdomain.com,https://admin.yourdomain.com`). 
  - **Bắt buộc HTTPS**: Không dùng `http://`.
  - **Tuyệt đối không dùng `*`**: vì backend sử dụng cookie credentials (`allowCredentials=true`).
  - **Không dùng `localhost` hoặc `127.0.0.1`**: Trong file cấu hình production đã loại bỏ hoàn toàn fallback localhost.
  - **Không có dấu gạch chéo cuối (`/`)**: ví dụ `https://yourdomain.com` thay vì `https://yourdomain.com/`.
- `APP_COOKIE_SECURE` và `APP_COOKIE_SAME_SITE`:
  - Mặc định: `APP_COOKIE_SECURE=true` và `APP_COOKIE_SAME_SITE=Strict`.
  - Nếu frontend chạy khác domain (ví dụ Vercel/Netlify gọi API VPS riêng): đặt `APP_COOKIE_SAME_SITE=None` và bắt buộc `APP_COOKIE_SECURE=true`.
- `APP_MEDIA_TRUSTED_DOMAINS`: Danh sách domain tin cậy cho ảnh/media (ví dụ: `s3.amazonaws.com,res.cloudinary.com`).
- `APP_PAYMENT_SIMULATION_ENABLED`: Luôn đặt `false` trên Production.

### Bước 2: Build project (Nội dung Backend)
Trước khi khởi chạy Docker, bạn cần đóng gói ứng dụng Backend thành file `.jar` (Docker sẽ sao chép file này vào container).

```bash
cd backend
./mvnw clean package -DskipTests
cd ..
```

*(Ghi chú: Nếu hệ thống CI/CD đã tự động build file `.jar`, bạn có thể bỏ qua bước này)*

### Bước 3: Khởi chạy các container bằng Docker Compose
Sau khi đã chuẩn bị xong biến môi trường và file `.jar`, hãy khởi chạy hệ thống bằng cấu hình dành riêng cho Production.

```bash
# Chạy ở chế độ background (detached)
docker-compose -f docker-compose.prod.yml up -d --build
```

### Bước 4: Kiểm tra trạng thái
Kiểm tra xem tất cả các container có đang chạy bình thường không:

```bash
docker-compose -f docker-compose.prod.yml ps
```

Bạn cũng nên kiểm tra log của backend để đảm bảo ứng dụng khởi động thành công (không có lỗi kết nối DB hay Redis):

```bash
docker logs -f japanese_learning_api_prod
```

Khi nhìn thấy dòng log `Started JapaneseLearningApplication in ... seconds (process running for ...)`, ứng dụng đã sẵn sàng phục vụ.

## Cập nhật ứng dụng (Update Deployment)
Khi có phiên bản mới của mã nguồn Backend, thực hiện các bước sau:

1. Kéo mã nguồn mới nhất (`git pull`).
2. Build lại file `.jar` (`cd backend && ./mvnw clean package -DskipTests && cd ..`).
3. Khởi động lại service `backend` bằng lệnh:
   ```bash
   docker-compose -f docker-compose.prod.yml up -d --build backend
   ```

## Xử lý sự cố (Troubleshooting)

- **Lỗi kết nối Redis:** Nếu backend báo lỗi `NOAUTH Authentication required`, hãy kiểm tra xem `REDIS_PASSWORD` trong `.env.prod` có khớp với `spring.data.redis.password` không. (Hệ thống đã tự động cấu hình trong `docker-compose.prod.yml`).
- **Lỗi Mixed Content trên Frontend:** Trên Production, backend bắt buộc các URL media (ảnh, video) phải sử dụng giao thức HTTPS. Nếu có URL HTTP được lưu, trình duyệt sẽ chặn. Xem thêm [MEDIA_URL_SECURITY.md](./MEDIA_URL_SECURITY.md).
- **Rate Limiting chặn tất cả (503):** Nếu Redis sập, do sử dụng `FAIL_CLOSED` policy, tất cả request xác thực (Login/Register) sẽ bị chặn. Đảm bảo container `japanese_learning_redis_prod` đang chạy:
  ```bash
  docker restart japanese_learning_redis_prod
  ```

## Quản lý Sao lưu & Phục hồi Thảm họa (Backup & Disaster Recovery)

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

## Kiểm thử Tự động & CI/CD Pipeline (Continuous Integration)

Để đảm bảo không bị lỗi hồi quy (regression) và bảo vệ chất lượng mã nguồn trước khi deploy, dự án tích hợp pipeline GitHub Actions tự động (`.github/workflows/ci.yml`).

### Các Job trong Pipeline:
1. **Backend CI (`backend`)**: Sử dụng JDK 21 (Eclipse Temurin), cache Maven, và chạy `mvn -B clean verify`.
2. **Frontend CI (`frontend`)**: Sử dụng Node.js 22, cache npm dependencies, chạy `npm ci`, sau đó chạy `npm run test` (Vitest) và `npm run build` (Vite).
3. **Docker Config CI (`docker-config`)**: Tự động kiểm tra tính hợp lệ của file `docker-compose.prod.yml` thông qua tệp mẫu `.env.prod.example` mà không cần secret thật.

### Lệnh chạy kiểm thử cục bộ (Local Equivalents):
Các lập trình viên cần chạy các lệnh sau trước khi tạo Pull Request:
```bash
# 1. Kiểm thử backend
cd backend && mvn -B clean verify && cd ..

# 2. Kiểm thử và build frontend
cd frontend && npm ci && npm run test && npm run build && cd ..

# 3. Kiểm tra static cấu hình Docker Compose production
cp .env.prod.example .env.prod
docker compose -f docker-compose.prod.yml config
```
