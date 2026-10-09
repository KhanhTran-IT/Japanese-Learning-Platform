> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

## 27. Kết luận kiến trúc đề xuất

Cấu hình phù hợp nhất cho giai đoạn đầu:

```text
Frontend:
Vue 3 + Vite + Pinia + Vue Router + Tailwind CSS + Axios

Backend:
Java Spring Boot + Spring Security + JWT + Spring Data JPA + Hibernate + Spring Validation + Spring Actuator

Database:
MySQL

Cache:
Redis

File storage:
Cloudflare R2 hoặc Supabase Storage

Deploy:
Docker + Docker Compose + Nginx + HTTPS + VPS

Monitoring:
Spring Actuator + logs + Sentry/Grafana tùy ngân sách

Kiến trúc:
Modular Monolith, chia module rõ ràng, sau này mới tách service nếu cần
```

Thứ tự triển khai tốt nhất:

```text
Nghiệp vụ
Database
API base
Auth
Course
Lesson
Enrollment
Payment
Progress
Quiz
Flashcard
Game
Admin
Cache
Deploy production
Monitoring
Backup
```

Nếu làm đúng theo tài liệu này, dự án có thể phát triển từ bản MVP thành một nền tảng học tiếng Nhật thật sự, có khả năng vận hành thực tế, mở rộng chức năng và phục vụ nhiều người dùng trong tương lai.

---

# PHỤ LỤC A: Danh sách bảng database tổng hợp

```text
roles
users
user_roles
refresh_tokens
password_reset_tokens
email_verification_tokens
courses
course_sections
lessons
lesson_resources
course_enrollments
lesson_progress
course_reviews
orders
order_items
payments
coupons
coupon_usages
quizzes
questions
answers
quiz_attempts
quiz_attempt_answers
jlpt_levels
vocabularies
kanjis
grammar_points
flashcard_decks
flashcard_items
user_flashcard_progress
games
game_levels
game_sessions
user_xp_logs
user_stats
badges
user_badges
notifications
user_notifications
email_logs
site_settings
banners
blog_posts
faqs
```

---

# PHỤ LỤC B: Danh sách module backend tổng hợp

```text
common
module_auth
module_user
module_course
module_lesson
module_quiz
module_flashcard
module_game
module_payment
module_notification
module_admin
module_report
module_cms
```

---

# PHỤ LỤC C: Danh sách trang frontend tổng hợp

## Public

```text
HomePage
CourseListPage
CourseDetailPage
BlogPage
BlogDetailPage
ContactPage
LoginPage
RegisterPage
ForgotPasswordPage
ResetPasswordPage
```

## Student

```text
StudentDashboardPage
MyCoursesPage
LearningPage
QuizPage
QuizResultPage
FlashcardPage
GamePage
LeaderboardPage
ProfilePage
OrdersPage
NotificationsPage
```

## Admin

```text
AdminDashboardPage
UserManagementPage
CourseManagementPage
SectionManagementPage
LessonManagementPage
QuizManagementPage
QuestionManagementPage
VocabularyManagementPage
KanjiManagementPage
GrammarManagementPage
GameManagementPage
OrderManagementPage
PaymentManagementPage
CouponManagementPage
ReportPage
BannerManagementPage
SiteSettingPage
```

---

# PHỤ LỤC D: Checklist MVP bắt buộc

```text
[ ] Setup Spring Boot backend
[ ] Setup Vue 3 frontend
[ ] Setup MySQL database
[ ] Thiết kế bảng user/role
[ ] Đăng ký
[ ] Đăng nhập
[ ] JWT
[ ] Refresh token
[ ] Phân quyền admin/student
[ ] Trang chủ
[ ] Danh sách khóa học
[ ] Chi tiết khóa học
[ ] Admin CRUD khóa học
[ ] Admin CRUD chương
[ ] Admin CRUD bài học
[ ] Upload ảnh khóa học
[ ] User enroll khóa miễn phí
[ ] User học bài
[ ] Lưu tiến độ học
[ ] Tạo order
[ ] Thanh toán cơ bản
[ ] Mở khóa học sau thanh toán
[ ] Dashboard admin cơ bản
[ ] Docker dev
[ ] Deploy bản test
```

---

# PHỤ LỤC E: Checklist Production

```text
[ ] HTTPS
[ ] Domain
[ ] Nginx reverse proxy
[ ] Docker production
[ ] MySQL không public port
[ ] Redis không public port
[ ] CORS đúng domain
[ ] JWT secret an toàn
[ ] Không hard-code password
[ ] Backup database tự động
[ ] Log lỗi backend
[ ] Monitoring health check
[ ] Rate limit login
[ ] Validate upload file
[ ] CDN cho ảnh/video
[ ] Index database
[ ] Pagination toàn bộ API danh sách
[ ] Tối ưu frontend bundle
[ ] Load testing
[ ] Tài liệu hướng dẫn admin
```
