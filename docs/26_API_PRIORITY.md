# 26. API_PRIORITY - Thứ tự ưu tiên API

## 1. Mục đích của file

File này giúp bạn không code API lan man. Khi làm một mình, hãy ưu tiên P0 trước. P0 xong mới sang P1.

Quy ước:

```text
P0 = Bắt buộc cho MVP
P1 = Nên có ngay sau MVP
P2 = Nâng cao
P3 = Tương lai / khi sản phẩm lớn hơn
```

## 2. P0 - API bắt buộc cho MVP

### Auth API

```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh-token
POST /api/auth/logout
GET  /api/users/me
```

Lý do:

- Cần đăng ký, đăng nhập, giữ phiên đăng nhập.
- Cần biết user hiện tại để phân quyền frontend/backend.

### Course public API

```http
GET  /api/courses
GET  /api/courses/{slug}
GET  /api/courses/{id}/sections
GET  /api/courses/{id}/lessons
POST /api/courses/{id}/enroll
```

Lý do:

- Public cần xem khóa học.
- Student cần enroll khóa miễn phí.

### Lesson learning API

```http
GET  /api/lessons/{id}
GET  /api/lessons/{id}/resources
POST /api/lessons/{id}/progress
POST /api/lessons/{id}/complete
```

Lý do:

- Đây là lõi học tập.
- Phải lưu được tiến độ học.

### Student API

```http
GET /api/users/me/courses
GET /api/users/me/progress
PUT /api/users/me
PUT /api/users/me/change-password
```

Lý do:

- Student cần xem khóa học của mình.
- Student cần cập nhật hồ sơ cơ bản.

### Admin course API

```http
GET    /api/admin/courses
POST   /api/admin/courses
GET    /api/admin/courses/{id}
PUT    /api/admin/courses/{id}
DELETE /api/admin/courses/{id}
PUT    /api/admin/courses/{id}/publish
PUT    /api/admin/courses/{id}/hide
```

### Admin section/lesson API

```http
POST   /api/admin/courses/{courseId}/sections
PUT    /api/admin/sections/{id}
DELETE /api/admin/sections/{id}
POST   /api/admin/sections/{sectionId}/lessons
PUT    /api/admin/lessons/{id}
DELETE /api/admin/lessons/{id}
POST   /api/admin/lessons/{id}/resources
```

### Admin dashboard/user API

```http
GET /api/admin/dashboard
GET /api/admin/users
GET /api/admin/users/{id}
PUT /api/admin/users/{id}/lock
PUT /api/admin/users/{id}/unlock
```

## 3. P1 - API nên làm sau MVP

### Auth nâng cao

```http
POST /api/auth/forgot-password
POST /api/auth/reset-password
POST /api/auth/verify-email
POST /api/auth/resend-verification-email
```

### Quiz API cơ bản

```http
GET  /api/quizzes/{id}
POST /api/quizzes/{id}/start
POST /api/quizzes/{id}/submit
GET  /api/quizzes/{id}/result/{attemptId}
GET  /api/users/me/quiz-attempts
```

### Admin Quiz API

```http
GET    /api/admin/quizzes
POST   /api/admin/quizzes
PUT    /api/admin/quizzes/{id}
DELETE /api/admin/quizzes/{id}
POST   /api/admin/quizzes/{id}/questions
PUT    /api/admin/questions/{id}
DELETE /api/admin/questions/{id}
POST   /api/admin/questions/{id}/answers
```

### Order/Payment basic API

```http
POST /api/orders
GET  /api/orders/me
GET  /api/orders/{id}
POST /api/payments/create
```

Giai đoạn đầu có thể dùng payment giả lập hoặc bank transfer trước.

### CMS basic API

```http
GET /api/admin/site-settings
PUT /api/admin/site-settings
GET /api/admin/banners
POST /api/admin/banners
PUT /api/admin/banners/{id}
DELETE /api/admin/banners/{id}
```

## 4. P2 - API nâng cao

### Japanese content API

```http
GET /api/jlpt-levels
GET /api/vocabularies
GET /api/vocabularies/{id}
GET /api/kanjis
GET /api/kanjis/{id}
GET /api/grammar-points
GET /api/grammar-points/{id}
```

### Flashcard API

```http
GET  /api/flashcard-decks
GET  /api/flashcard-decks/{id}
GET  /api/flashcard-decks/{id}/items
POST /api/flashcards/{id}/review
GET  /api/users/me/flashcards/review-today
```

### Admin report API

```http
GET /api/admin/reports/revenue
GET /api/admin/reports/users
GET /api/admin/reports/courses
GET /api/admin/reports/learning-progress
```

## 5. P3 - API tương lai

### Game API

```http
GET  /api/games
GET  /api/games/{slug}
POST /api/games/{id}/start
POST /api/games/{id}/submit
GET  /api/games/{id}/leaderboard
GET  /api/games/leaderboard/daily
GET  /api/games/leaderboard/weekly
GET  /api/games/leaderboard/monthly
```

### Payment provider callback/webhook thật

```http
POST /api/payments/vnpay/callback
POST /api/payments/momo/callback
POST /api/payments/webhook
```

Chỉ làm khi đã hiểu kỹ xác thực chữ ký giao dịch.

## 6. API không nên code sớm

Không nên code sớm khi MVP chưa xong:

- Leaderboard phức tạp.
- Badge automation.
- AI tutor.
- Notification realtime.
- Export Excel nâng cao.
- Multi-payment provider.

## 7. Thứ tự code API gợi ý

```text
1. Auth register/login/me/refresh/logout
2. Admin course CRUD
3. Admin section/lesson CRUD
4. Public course list/detail
5. Student enroll course
6. Lesson detail/progress/complete
7. Student my courses/progress
8. Admin dashboard basic
9. Profile/change password
10. Quiz basic
11. Order/payment basic
```

## 8. Prompt dùng với AI

```text
Hãy đọc 26_API_PRIORITY.md.
Tôi đang ở giai đoạn MVP nên chỉ code API P0.
Hãy đề xuất thứ tự tạo Controller/Service/DTO/Repository cho API: [tên API].
Không code API P1/P2/P3 nếu tôi chưa yêu cầu.
```
