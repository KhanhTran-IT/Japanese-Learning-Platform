> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

## 13. Cấu hình backend đề xuất

### 13.1. `application.yml` mẫu

```yaml
server:
  port: 8080

spring:
  application:
    name: japanese-learning-api

  datasource:
    url: jdbc:mysql://localhost:3306/japanese_learning_db?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB

  mail:
    host: smtp.gmail.com
    port: 587
    username: your_email@gmail.com
    password: your_app_password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: change_this_secret_key_to_long_random_string
  access-token-expiration-ms: 900000
  refresh-token-expiration-ms: 604800000

app:
  frontend-url: http://localhost:5173
  backend-url: http://localhost:8080
  upload-dir: uploads

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

### 13.2. Cấu hình production nên dùng environment variables

Không nên hard-code:

```text
Database password
JWT secret
Email password
Payment secret
Storage secret key
```

Nên dùng:

```text
.env
Docker secrets
Environment variables trên server
```

---

## 14. Cấu hình frontend đề xuất

### 14.1. `.env.development`

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_NAME=Japanese Learning Platform
```

### 14.2. `.env.production`

```env
VITE_API_BASE_URL=https://api.yourdomain.com/api
VITE_APP_NAME=Japanese Learning Platform
```

### 14.3. Axios interceptor

Cần có:

- Tự gắn access token vào request.
- Tự refresh token khi gặp lỗi 401.
- Logout nếu refresh token hết hạn.
- Điều hướng về login nếu chưa đăng nhập.

---
