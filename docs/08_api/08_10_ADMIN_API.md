> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

# 10.10. Admin API

## Admin Dashboard

```http
GET /api/admin/dashboard
GET /api/admin/reports/revenue
GET /api/admin/reports/users
GET /api/admin/reports/courses
GET /api/admin/reports/learning-progress
```

## Admin User

```http
GET /api/admin/users
GET /api/admin/users/{id}
PUT /api/admin/users/{id}
PUT /api/admin/users/{id}/lock
PUT /api/admin/users/{id}/unlock
PUT /api/admin/users/{id}/roles
```

## Admin Course

```http
GET /api/admin/courses
POST /api/admin/courses
GET /api/admin/courses/{id}
PUT /api/admin/courses/{id}
DELETE /api/admin/courses/{id}
PUT /api/admin/courses/{id}/publish
PUT /api/admin/courses/{id}/hide
```

## Admin Section/Lesson

```http
POST /api/admin/courses/{courseId}/sections
PUT /api/admin/sections/{id}
DELETE /api/admin/sections/{id}
POST /api/admin/sections/{sectionId}/lessons
PUT /api/admin/lessons/{id}
DELETE /api/admin/lessons/{id}
POST /api/admin/lessons/{id}/resources
```

## Admin Quiz

```http
GET /api/admin/quizzes
POST /api/admin/quizzes
PUT /api/admin/quizzes/{id}
DELETE /api/admin/quizzes/{id}
POST /api/admin/quizzes/{id}/questions
PUT /api/admin/questions/{id}
DELETE /api/admin/questions/{id}
POST /api/admin/questions/{id}/answers
```

## Admin Payment/Order

```http
GET /api/admin/orders
GET /api/admin/orders/{id}
PUT /api/admin/orders/{id}/status
GET /api/admin/payments
GET /api/admin/payments/{id}
```

## Admin Website Config

```http
GET /api/admin/site-settings
PUT /api/admin/site-settings
GET /api/admin/banners
POST /api/admin/banners
PUT /api/admin/banners/{id}
DELETE /api/admin/banners/{id}
```

---
