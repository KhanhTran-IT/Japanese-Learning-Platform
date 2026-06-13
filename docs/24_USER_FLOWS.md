# 24. USER_FLOWS - Luồng người dùng chính

## 1. Mục đích của file

File này mô tả các luồng thao tác quan trọng trong hệ thống. Khi làm frontend, backend hoặc test, hãy dùng file này để hiểu người dùng đi từ màn hình nào đến màn hình nào và backend cần xử lý gì.

## 2. Flow 1 - Guest xem và đăng ký tài khoản

```text
Guest vào trang chủ
→ Xem danh sách khóa học
→ Xem chi tiết khóa học
→ Bấm Đăng ký học / Bắt đầu học
→ Hệ thống yêu cầu đăng nhập
→ Guest chọn Đăng ký
→ Nhập họ tên, email, mật khẩu
→ Backend tạo tài khoản STUDENT
→ Trả token đăng nhập hoặc yêu cầu verify email tùy cấu hình
→ Điều hướng về Student Dashboard
```

Backend liên quan:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/users/me`

Frontend liên quan:

- HomePage
- CourseListPage
- CourseDetailPage
- RegisterPage
- StudentDashboardPage

## 3. Flow 2 - Student đăng nhập

```text
Student vào LoginPage
→ Nhập email/password
→ Frontend gọi login API
→ Backend kiểm tra password bằng BCrypt
→ Backend trả accessToken, refreshToken, user
→ Frontend lưu token theo cơ chế đã chọn
→ Frontend gọi /users/me nếu cần
→ Điều hướng theo role
```

Điều hướng:

```text
ADMIN/SUPER_ADMIN → AdminDashboardPage
TEACHER → TeacherDashboardPage
STUDENT → StudentDashboardPage
```

Backend liên quan:

- `POST /api/auth/login`
- `GET /api/users/me`

Lưu ý bảo mật:

- Không log password.
- Không trả passwordHash.
- Nếu account bị LOCKED thì không cho login.

## 4. Flow 3 - Student enroll khóa học miễn phí

```text
Student xem CourseDetailPage
→ Bấm Ghi danh miễn phí
→ Frontend gọi enroll API
→ Backend kiểm tra user đã đăng nhập
→ Backend kiểm tra khóa học có tồn tại và PUBLISHED
→ Backend kiểm tra course_type = FREE hoặc user đã thanh toán
→ Backend tạo course_enrollments
→ Frontend điều hướng tới LearningPage
```

Backend liên quan:

- `GET /api/courses/{slug}`
- `POST /api/courses/{id}/enroll`
- `GET /api/users/me/courses`

Database liên quan:

- `courses`
- `course_enrollments`

## 5. Flow 4 - Student học bài và lưu tiến độ

```text
Student vào MyCoursesPage
→ Chọn khóa học đang học
→ Vào LearningPage
→ Chọn bài học
→ Frontend gọi lesson detail API
→ Student xem video/text/audio/pdf
→ Frontend định kỳ gửi watchedPercent
→ Student bấm Hoàn thành bài học
→ Backend cập nhật lesson_progress
→ Backend tính lại progress_percent trong course_enrollments
```

Backend liên quan:

- `GET /api/lessons/{id}`
- `POST /api/lessons/{id}/progress`
- `POST /api/lessons/{id}/complete`

Database liên quan:

- `lesson_progress`
- `course_enrollments`

Quy tắc:

- Student chỉ được học bài nếu đã enroll hoặc bài học là preview.
- Không tin dữ liệu completed từ client nếu cần kiểm soát chặt.

## 6. Flow 5 - Admin tạo khóa học

```text
Admin đăng nhập
→ Vào CourseManagementPage
→ Bấm Tạo khóa học
→ Nhập title, level, type, price, description
→ Upload thumbnail nếu có
→ Backend validate request
→ Backend tạo course với status DRAFT
→ Admin thêm chương học
→ Admin thêm bài học
→ Admin publish khóa học
```

Backend liên quan:

- `POST /api/admin/courses`
- `POST /api/admin/courses/{courseId}/sections`
- `POST /api/admin/sections/{sectionId}/lessons`
- `PUT /api/admin/courses/{id}/publish`

Role được phép:

- `ADMIN`
- `SUPER_ADMIN`
- `TEACHER` nếu là khóa học của mình

## 7. Flow 6 - Admin quản lý user

```text
Admin vào UserManagementPage
→ Xem danh sách user
→ Tìm kiếm/lọc role/status
→ Xem chi tiết user
→ Khóa/mở tài khoản nếu cần
→ Gán role nếu có quyền
```

Backend liên quan:

- `GET /api/admin/users`
- `GET /api/admin/users/{id}`
- `PUT /api/admin/users/{id}/lock`
- `PUT /api/admin/users/{id}/unlock`
- `PUT /api/admin/users/{id}/roles`

Quy tắc:

- ADMIN không được tự nâng mình thành SUPER_ADMIN.
- Chỉ SUPER_ADMIN được gán role nhạy cảm.

## 8. Flow 7 - Student làm quiz sau bài học

```text
Student hoàn thành hoặc mở bài học
→ Chọn Làm quiz
→ Backend tạo quiz_attempt IN_PROGRESS
→ Student trả lời câu hỏi
→ Submit quiz
→ Backend chấm điểm
→ Backend lưu quiz_attempt_answers
→ Backend cập nhật score, passed
→ Frontend hiển thị kết quả và giải thích đáp án
```

Backend liên quan:

- `GET /api/quizzes/{id}`
- `POST /api/quizzes/{id}/start`
- `POST /api/quizzes/{id}/submit`
- `GET /api/quizzes/{id}/result/{attemptId}`

## 9. Flow 8 - Student mua khóa học trả phí

```text
Student xem khóa học trả phí
→ Bấm Mua khóa học
→ Backend tạo order PENDING
→ Backend tạo payment request
→ Student chuyển sang cổng thanh toán
→ Payment provider callback/webhook về backend
→ Backend xác thực chữ ký giao dịch
→ Backend cập nhật payment SUCCESS
→ Backend cập nhật order PAID
→ Backend tạo course_enrollment ACTIVE
→ Gửi email xác nhận
```

Backend liên quan:

- `POST /api/orders`
- `POST /api/payments/create`
- `POST /api/payments/vnpay/callback`
- `POST /api/payments/momo/callback`

Quy tắc quan trọng:

- Không mở khóa học chỉ vì frontend báo thanh toán thành công.
- Chỉ mở khóa sau khi backend xác thực callback/webhook hợp lệ.

## 10. Flow 9 - Admin cấu hình website

```text
Admin vào SiteSettingPage
→ Sửa logo, tên website, thông tin liên hệ, SEO
→ Backend lưu vào site_settings
→ Frontend public đọc site settings để hiển thị
```

Backend liên quan:

- `GET /api/admin/site-settings`
- `PUT /api/admin/site-settings`

## 11. Prompt dùng với AI

```text
Hãy đọc 24_USER_FLOWS.md.
Tôi đang làm flow: [tên flow].
Hãy phân tích frontend cần màn hình nào, backend cần API nào, database bảng nào, và thứ tự code đúng nhất.
```
