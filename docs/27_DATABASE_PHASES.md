# 27. DATABASE_PHASES - Chia database theo giai đoạn triển khai

## 1. Mục đích của file

File schema tổng rất lớn. Nếu tạo toàn bộ bảng ngay từ đầu, bạn sẽ dễ rối và khó kiểm soát Entity/JPA.

File này chia database thành từng phase để code tới đâu tạo bảng tới đó.

## 2. Phase 1 - Core Auth + Course + Learning

Đây là database bắt buộc cho MVP.

### Auth/User

```text
roles
users
user_roles
refresh_tokens
```

Tạm hoãn nếu chưa cần:

```text
password_reset_tokens
email_verification_tokens
```

### Course/Lesson

```text
courses
course_sections
lessons
lesson_resources
course_enrollments
lesson_progress
```

Tạm hoãn nếu chưa cần:

```text
course_reviews
```

### Lý do Phase 1

Phase này đủ để:

- Đăng ký/đăng nhập.
- Phân quyền ADMIN/STUDENT.
- Admin tạo khóa học/chương/bài học.
- Student enroll khóa học miễn phí.
- Student học bài và lưu tiến độ.

## 3. Phase 2 - Admin/CMS basic

Làm sau khi Phase 1 chạy ổn.

```text
site_settings
banners
faqs
blog_posts
```

Lý do:

- Giúp website nhìn thật hơn.
- Admin có thể đổi tên website, logo, banner.
- Blog/FAQ hỗ trợ SEO và nội dung.

## 4. Phase 3 - Quiz basic

```text
quizzes
questions
answers
quiz_attempts
quiz_attempt_answers
```

Lý do:

- Thêm kiểm tra cuối bài.
- Tăng tương tác học tập.
- Có thể tính điểm và lưu lịch sử làm bài.

## 5. Phase 4 - Payment/Order

```text
orders
order_items
payments
coupons
coupon_usages
```

Làm khi:

- Course/Lesson đã ổn.
- Có nhu cầu bán khóa học trả phí.
- Đã xác định cổng thanh toán đầu tiên.

Giai đoạn đầu có thể chưa cần `coupons` và `coupon_usages` nếu muốn đơn giản.

## 6. Phase 5 - Japanese Learning Content

```text
jlpt_levels
vocabularies
kanjis
grammar_points
```

Lý do:

- Xây kho từ vựng/Kanji/ngữ pháp.
- Dùng cho flashcard, quiz, game sau này.

## 7. Phase 6 - Flashcard

```text
flashcard_decks
flashcard_items
user_flashcard_progress
```

Làm khi:

- Đã có Japanese Content hoặc muốn tạo flashcard thủ công.
- Student cần ôn tập từ vựng/Kanji.

## 8. Phase 7 - Game/Gamification

```text
games
game_levels
game_sessions
user_xp_logs
user_stats
badges
user_badges
```

Làm khi:

- Hệ thống học bài/quiz/flashcard đã ổn.
- Muốn tăng retention.
- Muốn có XP, streak, leaderboard, badge.

## 9. Phase 8 - Notification/Email logs

```text
notifications
user_notifications
email_logs
```

Làm khi:

- Cần thông báo học tập.
- Cần log email gửi đi.
- Cần dashboard thông báo cho user.

## 10. Entity nên tạo theo thứ tự

### Sprint Auth

```text
Role
User
RefreshToken
```

### Sprint Course

```text
Course
CourseSection
Lesson
LessonResource
```

### Sprint Learning

```text
CourseEnrollment
LessonProgress
```

### Sprint Quiz

```text
Quiz
Question
Answer
QuizAttempt
QuizAttemptAnswer
```

### Sprint Payment

```text
Order
OrderItem
Payment
Coupon
CouponUsage
```

## 11. Migration đề xuất

Nếu dùng Flyway/Liquibase, có thể chia migration:

```text
V1__create_auth_user_tables.sql
V2__create_course_lesson_tables.sql
V3__create_learning_progress_tables.sql
V4__create_cms_tables.sql
V5__create_quiz_tables.sql
V6__create_payment_order_tables.sql
V7__create_japanese_content_tables.sql
V8__create_flashcard_tables.sql
V9__create_game_gamification_tables.sql
V10__create_notification_tables.sql
```

## 12. Index nên tạo ngay ở Phase 1

```sql
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_courses_slug ON courses(slug);
CREATE INDEX idx_courses_status ON courses(status);
CREATE INDEX idx_courses_level ON courses(level);
CREATE INDEX idx_lessons_course_id ON lessons(course_id);
CREATE INDEX idx_lessons_section_id ON lessons(section_id);
CREATE INDEX idx_enrollments_user_id ON course_enrollments(user_id);
CREATE INDEX idx_enrollments_course_id ON course_enrollments(course_id);
CREATE INDEX idx_lesson_progress_user_id ON lesson_progress(user_id);
CREATE INDEX idx_lesson_progress_lesson_id ON lesson_progress(lesson_id);
```

## 13. Prompt dùng với AI

```text
Hãy đọc 27_DATABASE_PHASES.md.
Tôi đang triển khai Phase [số phase].
Chỉ tạo Entity/Repository/DTO/Service cho các bảng thuộc phase này.
Không tạo các bảng phase sau nếu tôi chưa yêu cầu.
```
