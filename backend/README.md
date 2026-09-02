# Backend - Japanese Learning API

## Prerequisites

- **Java**: 21 (LTS) - Dự án bắt buộc sử dụng Java 21. Đảm bảo `JAVA_HOME` của bạn chỉ định đúng JDK 21.
- **Maven**: 3.8+

## Testing & CI

Để chạy toàn bộ các bài unit test và integration test, cũng như kiểm tra ứng dụng có biên dịch thành công với Java 21 hay không:

```bash
mvn clean verify
```

Dự án sử dụng `maven-failsafe-plugin` để chạy integration test trong giai đoạn `verify`. Luôn luôn chạy lệnh này trước khi commit / push hoặc trên hệ thống CI/CD để phát hiện lỗi hồi quy.

## Spring Profiles

Ứng dụng **không** có profile mặc định được gắn cứng trong `application.yml`.  
Khi khởi động, bạn **bắt buộc** phải chỉ định profile thông qua biến môi trường hoặc tham số JVM.

### Cấu hình môi trường (Environment Variables)

Dự án yêu cầu các biến môi trường để chạy (như mật khẩu cơ sở dữ liệu, JWT secret).

1. Copy file cấu hình mẫu:
   ```bash
   cp .env.example .env
   ```
2. Mở file `.env` và điền các secret thực tế của bạn cho môi trường local.
3. **Quan trọng**: File `.env` chứa thông tin nhạy cảm nên đã được tự động bỏ qua (ignored) bởi Git. Tuyệt đối không bao giờ commit file `.env` lên repository.

### Chạy Local (Development)

```bash
# Cách 1: Dùng biến môi trường
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run

# Cách 2: Dùng tham số JVM
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Cách 3: Dùng biến môi trường trên terminal (Linux/macOS)
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

File cấu hình được sử dụng: `application.yml` + `application-dev.yml`

### Chạy Production

```bash
# Dùng biến môi trường (khuyến nghị cho Docker/CI)
SPRING_PROFILES_ACTIVE=prod java -jar target/japanese-learning-api-*.jar

# Hoặc dùng tham số JVM
java -jar target/japanese-learning-api-*.jar --spring.profiles.active=prod
```

File cấu hình được sử dụng: `application.yml` + `application-prod.yml`

### Tại sao không gắn cứng `spring.profiles.active: dev`?

Gắn cứng profile `dev` trong `application.yml` tạo ra rủi ro bảo mật: nếu quên set biến môi trường khi triển khai lên production, ứng dụng sẽ âm thầm chạy với cấu hình dev (database dev, debug mode, CORS mở rộng...). Việc **bắt buộc** chỉ định profile giúp đảm bảo không bao giờ có sự nhầm lẫn môi trường.
