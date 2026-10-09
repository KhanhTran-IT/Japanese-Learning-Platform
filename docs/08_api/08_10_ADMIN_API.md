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
GET /api/v1/admin/quizzes
POST /api/v1/admin/quizzes
GET /api/v1/admin/quizzes/{id}
PUT /api/v1/admin/quizzes/{id}
DELETE /api/v1/admin/quizzes/{id}
PUT /api/v1/admin/quizzes/{id}/publish
PUT /api/v1/admin/quizzes/{id}/hide
GET /api/v1/admin/quizzes/{quizId}/questions
POST /api/v1/admin/quizzes/{quizId}/questions
GET /api/v1/admin/questions/{id}
PUT /api/v1/admin/questions/{id}
DELETE /api/v1/admin/questions/{id}
GET /api/v1/admin/questions/{questionId}/answers
POST /api/v1/admin/questions/{questionId}/answers
PUT /api/v1/admin/answers/{id}
DELETE /api/v1/admin/answers/{id}
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
