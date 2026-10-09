> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

# 10.1. Auth API

```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh-token
POST /api/auth/logout
POST /api/auth/forgot-password
POST /api/auth/reset-password
POST /api/auth/verify-email
POST /api/auth/resend-verification-email
```

### Register request

```json
{
  "fullName": "Nguyen Van A",
  "email": "user@example.com",
  "password": "Password@123",
  "confirmPassword": "Password@123"
}
```

### Login request

```json
{
  "email": "user@example.com",
  "password": "Password@123"
}
```

### Login response data

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token",
  "user": {
    "id": 1,
    "fullName": "Nguyen Van A",
    "email": "user@example.com",
    "roles": ["STUDENT"]
  }
}
```

---
