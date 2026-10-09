> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

## 3. Công nghệ đề xuất

### 3.1. Frontend

Công nghệ chính:

```text
Vue 3
Vite
Vue Router
Pinia
Axios
Tailwind CSS
Element Plus hoặc Naive UI
VueUse
```

Vai trò:

- Vue 3: xây dựng giao diện theo component.
- Vite: build project nhanh, tối ưu frontend.
- Vue Router: quản lý route public, student, admin.
- Pinia: quản lý trạng thái đăng nhập, giỏ hàng, user, khóa học.
- Axios: gọi API backend.
- Tailwind CSS: xây dựng giao diện nhanh, hiện đại, responsive.
- Element Plus/Naive UI: dùng cho dashboard admin, table, form, modal.
- VueUse: hỗ trợ nhiều composition utility hữu ích.

### 3.2. Backend

Công nghệ chính:

```text
Java Spring Boot
Spring Web
Spring Security
JWT
Spring Data JPA
Hibernate
Spring Validation
Spring Mail
Spring Scheduler
Spring Actuator
Swagger/OpenAPI
```

Vai trò:

- Spring Boot: xây dựng REST API.
- Spring Security: xác thực và phân quyền.
- JWT: đăng nhập bằng access token/refresh token.
- Spring Data JPA/Hibernate: thao tác database.
- Spring Validation: validate dữ liệu đầu vào.
- Spring Mail: gửi email xác thực/quên mật khẩu/thông báo.
- Spring Scheduler: xử lý tác vụ định kỳ.
- Spring Actuator: health check, monitoring.
- Swagger/OpenAPI: tài liệu API cho frontend/team/tester.

### 3.3. Database

Khuyến nghị giai đoạn đầu:

```text
MariaDB
```

Lý do:

- Phù hợp với Spring Boot/JPA.
- Dễ học, dễ triển khai.
- Dễ thiết kế ERD.
- Dễ backup.
- Phù hợp hệ thống khóa học, user, payment, order.

Có thể cân nhắc sau:

```text
PostgreSQL
Supabase
```

Lưu ý:

- Supabase dùng PostgreSQL, không phải MySQL.
- Nếu chọn Supabase, cần cân nhắc việc dùng Auth/Storage/Realtime của Supabase có trùng với Spring Boot hay không.
- Với hệ thống cần backend nghiệp vụ rõ ràng, Spring Boot + MySQL/PostgreSQL là hướng ổn định hơn.

### 3.4. Cache

Khuyến nghị:

```text
Redis
```

Dùng cho:

- Cache danh sách khóa học.
- Cache chi tiết khóa học.
- Cache trang chủ.
- Cache cấu hình website.
- Cache leaderboard.
- Lưu OTP tạm thời.
- Rate limit login/register.
- Token blacklist nếu cần.

### 3.5. File Storage

Không nên lưu file lớn trong database.

Nên dùng:

```text
Cloudflare R2
AWS S3
Google Cloud Storage
Supabase Storage
MinIO
```

Lưu trữ:

- Video bài học.
- Audio luyện nghe.
- Ảnh khóa học.
- Avatar người dùng.
- File PDF.
- Tài liệu tải về.

### 3.6. Deploy & DevOps

Khuyến nghị:

```text
Docker
Docker Compose
Nginx
HTTPS SSL
GitHub Actions
Cloudflare CDN
```

Dùng cho:

- Đóng gói backend/frontend/database/cache.
- Reverse proxy.
- Cấu hình domain.
- HTTPS.
- CI/CD tự động.
- Backup và monitoring.

---
