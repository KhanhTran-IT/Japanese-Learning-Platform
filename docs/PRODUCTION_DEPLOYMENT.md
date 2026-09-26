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
