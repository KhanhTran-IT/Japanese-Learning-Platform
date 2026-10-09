# 25. SCREEN_LIST - Danh sách màn hình cần xây dựng

## 1. Mục đích của file

File này giúp kiểm soát frontend. Mỗi màn hình nên được tạo thành một page rõ ràng, sau đó dùng component nhỏ để tái sử dụng.

Trạng thái đề xuất:

```text
TODO      Chưa làm
DOING     Đang làm
DONE      Đã xong UI + kết nối API cơ bản
REVIEW    Cần review/refactor
LATER     Để sau MVP
```

## 2. Public pages

| Màn hình | Route đề xuất | Ưu tiên | Mô tả |
|---|---|---:|---|
| HomePage | `/` | P0 | Trang chủ giới thiệu nền tảng, khóa học nổi bật, lợi ích học tập |
| CourseListPage | `/courses` | P0 | Danh sách khóa học, search, filter level/type |
| CourseDetailPage | `/courses/:slug` | P0 | Chi tiết khóa học, chương/bài học, nút enroll/mua |
| BlogListPage | `/blog` | P2 | Danh sách bài viết học tiếng Nhật |
| BlogDetailPage | `/blog/:slug` | P2 | Chi tiết bài viết |
| ContactPage | `/contact` | P1 | Liên hệ, thông tin hỗ trợ |
| FAQPage | `/faq` | P2 | Câu hỏi thường gặp |
| PricingPage | `/pricing` | P2 | Gói membership nếu có |

## 3. Auth pages

| Màn hình | Route đề xuất | Ưu tiên | Mô tả |
|---|---|---:|---|
| LoginPage | `/login` | P0 | Đăng nhập email/password |
| RegisterPage | `/register` | P0 | Đăng ký học viên |
| ForgotPasswordPage | `/forgot-password` | P1 | Nhập email nhận link reset |
| ResetPasswordPage | `/reset-password` | P1 | Đặt lại mật khẩu |
| VerifyEmailPage | `/verify-email` | P1 | Xác thực email |
| UnauthorizedPage | `/unauthorized` | P0 | Không có quyền truy cập |
| NotFoundPage | `/:pathMatch(.*)*` | P0 | Trang 404 |

## 4. Student pages

| Màn hình | Route đề xuất | Ưu tiên | Mô tả |
|---|---|---:|---|
| StudentDashboardPage | `/student/dashboard` | P0 | Tổng quan học tập, khóa đang học, tiến độ |
| MyCoursesPage | `/student/my-courses` | P0 | Danh sách khóa học đã enroll/mua |
| LearningPage | `/student/courses/:courseId/learn/:lessonId?` | P0 | Giao diện học bài chính |
| LessonResourcesPage | Có thể nằm trong LearningPage | P1 | Tài liệu đính kèm bài học |
| QuizPage | `/student/quizzes/:quizId` | P1 | Làm quiz |
| QuizResultPage | `/student/quizzes/:quizId/result/:attemptId` | P1 | Kết quả quiz |
| FlashcardDecksPage | `/student/flashcards` | P2 | Danh sách bộ flashcard |
| FlashcardReviewPage | `/student/flashcards/:deckId/review` | P2 | Ôn tập flashcard |
| GameListPage | `/student/games` | P3 | Danh sách mini game |
| GamePlayPage | `/student/games/:slug/play` | P3 | Chơi game |
| ProfilePage | `/student/profile` | P0 | Hồ sơ cá nhân |
| ChangePasswordPage | Có thể nằm trong ProfilePage | P1 | Đổi mật khẩu |
| OrdersPage | `/student/orders` | P1 | Lịch sử đơn hàng |
| NotificationsPage | `/student/notifications` | P2 | Thông báo |

## 5. Teacher pages

| Màn hình | Route đề xuất | Ưu tiên | Mô tả |
|---|---|---:|---|
| TeacherDashboardPage | `/teacher/dashboard` | P2 | Tổng quan khóa học của teacher |
| TeacherCoursePage | `/teacher/courses` | P2 | Quản lý khóa học của teacher |
| TeacherLessonPage | `/teacher/courses/:courseId/lessons` | P2 | Quản lý bài học của teacher |
| TeacherStudentPage | `/teacher/courses/:courseId/students` | P2 | Xem học viên trong khóa |
| TeacherQuizPage | `/teacher/quizzes` | P2 | Quản lý quiz của teacher |

Ghi chú: MVP có thể chưa cần Teacher Layout riêng. Admin có thể quản lý trước.

## 6. Admin pages

| Màn hình | Route đề xuất | Ưu tiên | Mô tả |
|---|---|---:|---|
| AdminDashboardPage | `/admin/dashboard` | P0 | Thống kê tổng quan |
| UserManagementPage | `/admin/users` | P0 | Danh sách, tìm kiếm, khóa/mở user |
| UserDetailPage | `/admin/users/:id` | P1 | Chi tiết user, khóa học, đơn hàng |
| CourseManagementPage | `/admin/courses` | P0 | CRUD khóa học |
| CourseFormPage | `/admin/courses/create`, `/admin/courses/:id/edit` | P0 | Form tạo/sửa khóa học |
| SectionLessonManagementPage | `/admin/courses/:id/structure` | P0 | Quản lý chương và bài học |
| LessonFormPage | `/admin/lessons/:id/edit` | P0 | Form tạo/sửa bài học |
| QuizManagementPage | `/admin/quizzes` | P1 | CRUD quiz |
| QuestionManagementPage | `/admin/quizzes/:id/questions` | P1 | Quản lý câu hỏi/đáp án |
| OrderManagementPage | `/admin/orders` | P1 | Danh sách đơn hàng |
| PaymentManagementPage | `/admin/payments` | P2 | Danh sách thanh toán |
| FlashcardManagementPage | `/admin/flashcards` | P2 | Quản lý bộ flashcard |
| JapaneseContentManagementPage | `/admin/japanese-content` | P2 | Từ vựng, Kanji, ngữ pháp |
| GameManagementPage | `/admin/games` | P3 | Quản lý game |
| BannerManagementPage | `/admin/banners` | P1 | Quản lý banner |
| BlogManagementPage | `/admin/blogs` | P2 | Quản lý bài viết |
| SiteSettingPage | `/admin/site-settings` | P1 | Cấu hình website |
| ReportPage | `/admin/reports` | P2 | Báo cáo doanh thu/học tập |

## 7. Component nên tách sớm

### Common components

- AppButton
- AppInput
- AppSelect
- AppModal
- AppTable
- AppPagination
- AppBreadcrumb
- AppLoading
- AppEmptyState
- AppConfirmDialog
- AppToast

### Course components

- CourseCard
- CourseFilter
- CourseLevelBadge
- CourseCurriculum
- LessonItem
- CoursePriceBox

### Admin components

- AdminSidebar
- AdminHeader
- StatCard
- DataTable
- SearchFilterBar
- StatusBadge
- FormSection

### Student learning components

- VideoPlayer
- LessonSidebar
- LessonContent
- ProgressBar
- CompleteLessonButton
- ResourceList

## 8. MVP frontend thứ tự làm

```text
1. Layout cơ bản: MainLayout, AuthLayout, StudentLayout, AdminLayout
2. Router + route guard
3. LoginPage, RegisterPage
4. HomePage
5. CourseListPage
6. CourseDetailPage
7. StudentDashboardPage
8. MyCoursesPage
9. LearningPage
10. AdminDashboardPage
11. CourseManagementPage
12. SectionLessonManagementPage
```

## 9. Prompt dùng với AI

```text
Hãy đọc 25_SCREEN_LIST.md và 10_FRONTEND_STRUCTURE.md.
Tôi đang làm màn hình: [tên màn hình].
Hãy đề xuất component con, state cần quản lý, API cần gọi, route guard cần có, và code Vue 3 theo cấu trúc project.
```
