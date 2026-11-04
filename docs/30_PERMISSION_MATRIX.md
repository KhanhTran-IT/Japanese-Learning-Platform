# 30. PERMISSION MATRIX - MA TRẬN PHÂN QUYỀN HỆ THỐNG

## 1. Nguyên Tắc Cốt Lõi (Core Principles)

```text
+----------------------------------------------------------------------------------+
| NGUYÊN TẮC BẤT DI BẤT DỊCH VỀ BẢO MẬT PHÂN QUYỀN (DEFENSE-IN-DEPTH)             |
+----------------------------------------------------------------------------------+
| 1. UI Links & Navigation Hiding KHÔNG PHẢI là cơ chế bảo mật (Security Control). |
|    Việc ẩn/hiện menu chỉ phục vụ tối ưu trải nghiệm người dùng (UX).             |
| 2. Frontend Route Guards (`router.beforeEach`) đóng vai trò điều hướng an toàn    |
|    và thông báo lỗi sớm (Fast-fail UX, chuyển hướng về /403 Forbidden).           |
| 3. Backend Spring Security (`SecurityConfig` + `@PreAuthorize`) là chốt chặn bảo |
|    mật DUY NHẤT và THẨM QUYỀN TUYỆT ĐỐI (Authoritative Security Boundary). Mọi   |
|    request cố tình vượt rào frontend đều bị Backend từ chối với HTTP 403.         |
+----------------------------------------------------------------------------------+
```

---

## 2. Danh Sách Roles Hệ Thống

| Role | Định danh | Mô tả đối tượng | Default Landing Route |
|---|---|---|---|
| **GUEST** | `ANONYMOUS` | Người dùng vãng lai chưa đăng nhập | `/` (Trang chủ) |
| **STUDENT** | `ROLE_STUDENT` | Học viên đăng ký học, mua khóa học, làm bài tập & quiz | `/student/dashboard` |
| **TEACHER** | `ROLE_TEACHER` | Giảng viên tạo và biên tập giáo trình, bài học, đề quiz | `/admin/courses` |
| **ADMIN** | `ROLE_ADMIN` | Quản trị viên quản lý người dùng, nội dung, xem báo cáo | `/admin/dashboard` |
| **SUPER_ADMIN** | `ROLE_SUPER_ADMIN` | Quản trị cấp cao toàn quyền quản lý hệ thống | `/admin/dashboard` |

---

## 3. Ma Trận Phân Quyền Chi Tiết (Role-Permission Matrix)

### 3.1. Phân Hệ Người Dùng & Học Tập (Student & Public)

| Tính năng / Chức năng | GUEST | STUDENT | TEACHER | ADMIN | SUPER_ADMIN | Backend Enforcement | Frontend Guard / Route |
|---|:---:|:---:|:---:|:---:|:---:|---|---|
| Xem trang chủ & landing page | ✅ | ✅ | ✅ | ✅ | ✅ | `permitAll()` | `/` (Public) |
| Xem danh mục khóa học (published) | ✅ | ✅ | ✅ | ✅ | ✅ | `permitAll()` | `/courses` (Public) |
| Xem chi tiết khóa học & bài preview | ✅ | ✅ | ✅ | ✅ | ✅ | `permitAll()` | `/courses/:id` (Public) |
| Đăng ký tài khoản (Register) | ✅ | ❌ | ❌ | ❌ | ❌ | `permitAll()` | `/register` (Guest only, auto-redirect if auth) |
| Đăng nhập tài khoản (Login) | ✅ | ❌ | ❌ | ❌ | ❌ | `permitAll()` | `/login` (Guest only, auto-redirect if auth) |
| Xem hồ sơ cá nhân (Profile) | ❌ | ✅ | ✅ | ✅ | ✅ | `authenticated()` | `/student/profile` (`requiresAuth: true`) |
| Cập nhật hồ sơ & đổi mật khẩu | ❌ | ✅ | ✅ | ✅ | ✅ | `authenticated()` | `/student/profile` |
| Dashboard học viên (Student Overview) | ❌ | ✅ | ❌ | ❌ | ❌ | `hasRole('STUDENT')` | `/student/dashboard` (`roles: ['STUDENT']`) |
| Danh sách khóa học đã đăng ký | ❌ | ✅ | ❌ | ❌ | ❌ | `hasRole('STUDENT')` | `/student/my-courses` (`roles: ['STUDENT']`) |
| Học bài & cập nhật tiến độ học | ❌ | ✅ | ❌ | ❌ | ❌ | `hasRole('STUDENT')` + Enrollment check | `/student/learning/:courseId` (`roles: ['STUDENT']`) |
| Mua khóa học & Checkout | ❌ | ✅ | ❌ | ❌ | ❌ | `hasRole('STUDENT')` | `/student/checkout/:courseId` (`roles: ['STUDENT']`) |
| Xem lịch sử đơn hàng & thanh toán | ❌ | ✅ | ❌ | ❌ | ❌ | `hasRole('STUDENT')` | `/student/orders` (`roles: ['STUDENT']`) |
| Học Flashcard theo khóa | ❌ | ✅ | ❌ | ❌ | ❌ | `hasRole('STUDENT')` | `/student/flashcards` (`roles: ['STUDENT']`) |
| Bảng xếp hạng học tập (Leaderboard) | ❌ | ✅ | ❌ | ❌ | ❌ | `hasRole('STUDENT')` | `/student/leaderboard` (`roles: ['STUDENT']`) |
| Bắt đầu làm Quiz (`/start`) | ❌ | ✅ | ❌ | ❌ | ❌ | `@PreAuthorize("hasRole('STUDENT')")` | `/student/quiz/:quizId` (`roles: ['STUDENT']`) |
| Nộp bài Quiz (`/submit`) | ❌ | ✅ | ❌ | ❌ | ❌ | `@PreAuthorize("hasRole('STUDENT')")` | `/student/quiz/:quizId/result` (`roles: ['STUDENT']`) |

### 3.2. Phân Hệ Quản Trị & Giảng Dạy (Admin & Teacher Management)

| Tính năng / Chức năng | GUEST | STUDENT | TEACHER | ADMIN | SUPER_ADMIN | Backend Enforcement | Frontend Guard / Route |
|---|:---:|:---:|:---:|:---:|:---:|---|---|
| Truy cập Layout Quản trị (`/admin`) | ❌ | ❌ | ✅ | ✅ | ✅ | `hasAnyRole('TEACHER','ADMIN','SUPER_ADMIN')` | `/admin` (`roles: ['ADMIN','SUPER_ADMIN','TEACHER']`) |
| Dashboard thống kê tổng quan (Admin Overview) | ❌ | ❌ | ❌ | ✅ | ✅ | `@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")` | `/admin/dashboard` (`roles: ['ADMIN','SUPER_ADMIN']`) |
| Quản lý tài khoản người dùng (User Management) | ❌ | ❌ | ❌ | ✅ | ✅ | `@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")` | `/admin/users` (`roles: ['ADMIN','SUPER_ADMIN']`) |
| Khóa / Mở khóa tài khoản | ❌ | ❌ | ❌ | ✅ | ✅ | `@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")` | `/admin/users` |
| Quản lý danh sách khóa học (Course List) | ❌ | ❌ | ✅ | ✅ | ✅ | `@PreAuthorize("hasAnyRole('TEACHER','ADMIN','SUPER_ADMIN')")` | `/admin/courses` (`roles: ['ADMIN','SUPER_ADMIN','TEACHER']`) |
| Tạo / Cập nhật khóa học, chương, bài học | ❌ | ❌ | ✅ | ✅ | ✅ | `@PreAuthorize("hasAnyRole('TEACHER','ADMIN','SUPER_ADMIN')")` | `/admin/courses/:id/structure` |
| Quản lý ngân hàng đề thi & Quiz | ❌ | ❌ | ✅ | ✅ | ✅ | `@PreAuthorize("hasAnyRole('TEACHER','ADMIN','SUPER_ADMIN')")` | `/admin/quizzes` (`roles: ['ADMIN','SUPER_ADMIN','TEACHER']`) |
| Tạo câu hỏi, đáp án, chỉnh sửa đề Quiz | ❌ | ❌ | ✅ | ✅ | ✅ | `@PreAuthorize("hasAnyRole('TEACHER','ADMIN','SUPER_ADMIN')")` | `/admin/quizzes/builder/:id` |

---

## 4. Đặc Tả Triển Khai Kỹ Thuật (Technical Implementation)

### 4.1. Phía Backend (Spring Security & Method Security)

1. **HTTP Security Request Matchers (`SecurityConfig.java`):**
   ```java
   http.authorizeHttpRequests(auth -> auth
       .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
       .requestMatchers("/api/v1/auth/**").permitAll()
       .requestMatchers("/api/v1/courses/**").permitAll()
       .requestMatchers("/api/v1/lessons/preview/**").permitAll()
       .requestMatchers("/api/v1/admin/courses/**", "/api/v1/admin/quizzes/**")
           .hasAnyRole("TEACHER", "ADMIN", "SUPER_ADMIN")
       .requestMatchers("/api/v1/admin/**")
           .hasAnyRole("ADMIN", "SUPER_ADMIN")
       .requestMatchers("/api/v1/student/**", "/api/v1/orders/**", "/api/v1/payments/**")
           .hasRole("STUDENT")
       .requestMatchers("/api/v1/users/me/**").authenticated()
       .anyRequest().authenticated()
   );
   ```

2. **Method-Level Security (`@PreAuthorize`):**
   ```java
   // Admin Controller
   @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
   @GetMapping("/api/v1/admin/dashboard/stats")
   public ApiResponse<DashboardStatsResponse> getStats() { ... }

   // Course Management Controller
   @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN')")
   @PostMapping("/api/v1/admin/courses")
   public ApiResponse<CourseDetailResponse> createCourse(@RequestBody CreateCourseRequest req) { ... }

   // Quiz Taking Controller
   @PreAuthorize("hasRole('STUDENT')")
   @PostMapping("/api/v1/quizzes/{quizId}/submit")
   public ApiResponse<QuizResultResponse> submitQuiz(...) { ... }
   ```

### 4.2. Phía Frontend (Vue Router Navigation Guards & Layouts)

1. **Navigation Guard Kiểm Soát Phân Quyền (`guards.js`):**
   - Kiểm tra `to.matched.some(record => record.meta.requiresAuth || record.meta.roles || record.meta.role)`.
   - Đối với khách vãng lai (Guest): Nếu vào trang yêu cầu đăng nhập $\rightarrow$ chuyển hướng về `/login?redirect=...`.
   - Đối với người dùng đã đăng nhập:
     - Nếu vào `/login` hoặc `/register` $\rightarrow$ tự động chuyển hướng về trang Dashboard mặc định theo Role (`getDefaultRouteForRoles`).
     - Nếu cố truy cập route vượt quá quyền hạn (ví dụ `STUDENT` vào `/admin/courses`, hoặc `TEACHER` vào `/admin/users`) $\rightarrow$ chuyển hướng an toàn về `/403` (Forbidden).
     - Tuyệt đối không cho phép route loop vô tận khi chuyển hướng về `/403`.

2. **Trang Báo Lỗi 403 (`ForbiddenPage.vue`):**
   - Hiển thị thông báo thân thiện: "403 - Bạn không có quyền truy cập trang này".
   - Hiển thị vai trò hiện tại của người dùng (Ví dụ: `Học viên`, `Giảng viên`, `Quản trị viên`).
   - Cung cấp nút điều hướng an toàn:
     - "Về trang của bạn" $\rightarrow$ dẫn trực tiếp về Landing Page hợp lệ của Role đó.
     - "Về trang chủ" $\rightarrow$ dẫn về `/`.

3. **Điều Hướng UI Theo Vai Trò (UI Link Visibility):**
   - **`Navbar.vue`**:
     - `STUDENT`: Hiển thị menu Khóa học của tôi, Flashcard, Bảng xếp hạng.
     - `TEACHER`: Hiển thị menu Quản lý giảng dạy (dẫn tới `/admin/courses`).
     - `ADMIN` / `SUPER_ADMIN`: Hiển thị menu Quản trị hệ thống (dẫn tới `/admin/dashboard`).
   - **`AdminLayout.vue`**:
     - `TEACHER`: Sidebar chỉ hiển thị mục "Quản lý khóa học" và "Quản lý bài tập/Quiz". Mục "Tổng quan" và "Người dùng" bị ẩn hoàn toàn khỏi DOM.
     - `ADMIN` / `SUPER_ADMIN`: Sidebar hiển thị đầy đủ tất cả các phân hệ quản trị.

---

## 5. Bảng Điều Hướng Mặc Định Theo Role (Landing Route Resolution)

Khi người dùng đăng nhập thành công hoặc truy cập vào `/login` khi phiên đã có hiệu lực, ứng dụng sẽ chuyển hướng dựa trên role có mức độ ưu tiên cao nhất:

| Danh sách Role của User | Landing Route | Ghi chú |
|---|---|---|
| Chứa `SUPER_ADMIN` | `/admin/dashboard` | Toàn quyền quản trị |
| Chứa `ADMIN` | `/admin/dashboard` | Báo cáo & quản trị |
| Chứa `TEACHER` | `/admin/courses` | Quản lý giáo trình |
| Chứa `STUDENT` | `/student/dashboard` | Tổng quan học tập |
| Khác / Không có role hợp lệ | `/` | Quay về trang chủ |

---

## 6. Bộ Kiểm Thử Hồi Quy Phân Quyền (Test Matrix)

| Test File | Số lượng test | Mục tiêu kiểm thử |
|---|:---:|---|
| `frontend/src/router/guards.spec.js` | 27 tests | Kiểm thử định tuyến cho cả 4 role (STUDENT, TEACHER, ADMIN, SUPER_ADMIN), guest redirect, chuyển hướng 403, và safe redirect tại `/login` |
| `frontend/src/pages/error/ForbiddenPage.spec.js` | 4 tests | Kiểm thử hiển thị trang 403, mapping tên role, và các nút điều hướng |
| `frontend/src/components/common/Navbar.spec.js` | 13 tests | Kiểm thử ẩn/hiện menu trên thanh điều hướng cho từng role |
| Backend Security Tests | 216 tests | Kiểm thử xác thực JWT, `@PreAuthorize`, RBAC method annotations |
