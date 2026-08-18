> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

## 18. Docker Compose kiến trúc mẫu

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: japanese_mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: japanese_learning_db
      MYSQL_USER: app_user
      MYSQL_PASSWORD: app_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7
    container_name: japanese_redis
    restart: always
    ports:
      - "6379:6379"

  backend:
    build: ./backend
    container_name: japanese_backend
    restart: always
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/japanese_learning_db?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: app_user
      SPRING_DATASOURCE_PASSWORD: app_password
      SPRING_DATA_REDIS_HOST: redis
      SPRING_DATA_REDIS_PORT: 6379

  frontend:
    build: ./frontend
    container_name: japanese_frontend
    restart: always
    ports:
      - "5173:80"
    depends_on:
      - backend

volumes:
  mysql_data:
```

---

## 19. Deploy production đề xuất

### 19.1. Mô hình deploy cơ bản

```text
1 VPS Linux
Nginx
Docker
Docker Compose
MySQL
Redis
Spring Boot API
Vue build static
HTTPS bằng Let's Encrypt
Cloudflare quản lý DNS/CDN
```

### 19.2. Domain đề xuất

```text
https://yourdomain.com              -> Frontend
https://api.yourdomain.com          -> Backend API
https://admin.yourdomain.com        -> Admin, nếu muốn tách riêng
```

### 19.3. Production checklist

```text
Bật HTTPS
Ẩn port database khỏi public
Ẩn port Redis khỏi public
Cấu hình firewall
Cấu hình CORS đúng domain
Tắt show-sql ở production
Không dùng ddl-auto update ở production lâu dài
Backup database tự động
Cấu hình log rotation
Cấu hình rate limit
Cấu hình health check
Cấu hình monitoring
Tối ưu Nginx gzip/brotli
Dùng CDN cho ảnh/video
```

---
