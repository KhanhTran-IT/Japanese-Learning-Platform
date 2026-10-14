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

## Các bước triển khai

### Bước 1: Chuẩn bị tệp tin cấu hình môi trường (.env.prod)
Không bao giờ commit tệp chứa thông tin nhạy cảm (như mật khẩu, secret key) lên Git. Thay vào đó, hãy copy từ file example và cấu hình lại cho máy chủ Production.

```bash
cp .env.prod.example .env.prod
```

Mở tệp `.env.prod` và thay đổi **tất cả** các giá trị mặc định:
- `DB_PASSWORD`: Đặt một mật khẩu an toàn và ngẫu nhiên cho MariaDB.
- `REDIS_PASSWORD`: Đặt một mật khẩu an toàn cho Redis.
- `ADMIN_PASSWORD`: Mật khẩu khởi tạo cho tài khoản Quản trị viên (Admin).
- `JWT_ACCESS_SECRET` và `JWT_REFRESH_SECRET`: Sử dụng 2 chuỗi ngẫu nhiên dài tối thiểu 32 ký tự. Bạn có thể tạo bằng lệnh:
  ```bash
  openssl rand -hex 32
  ```
- `APP_CORS_ALLOWED_ORIGINS`: Địa chỉ domain chính thức của frontend (ví dụ: `https://yourdomain.com`). **Tuyệt đối không dùng `*`** vì backend sử dụng cookie credentials (`allowCredentials=true`).
- `APP_COOKIE_SECURE` và `APP_COOKIE_SAME_SITE`:
  - Nếu frontend và backend cùng domain hoặc chạy sau Reverse Proxy Nginx: giữ `APP_COOKIE_SECURE=true` và `APP_COOKIE_SAME_SITE=Strict`.
  - Nếu frontend chạy domain khác (ví dụ Vercel/Netlify gọi API VPS riêng): bắt buộc đặt `APP_COOKIE_SAME_SITE=None` và `APP_COOKIE_SECURE=true` để trình duyệt không chặn cookie Refresh Token.
- `APP_MEDIA_TRUSTED_DOMAINS`: Danh sách domain tin cậy cho ảnh/media (ví dụ: `s3.amazonaws.com,res.cloudinary.com`).

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
