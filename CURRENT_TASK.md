# CURRENT TASK

## Task hiện tại
Frontend Authenticated User Flow & Enrollment UX Hardening

## Trạng thái
TODO

## Mục tiêu
Rà soát và sửa lại toàn bộ flow frontend sau redesign để trải nghiệm người dùng thật không bị khó chịu: trạng thái đăng nhập phải đúng ở mọi trang, trạng thái đã ghi danh phải hiển thị đúng ngay từ đầu, người dùng đã login vẫn quay lại public pages được, và trang học bài phải là một learning workspace riêng thay vì bị cảm giác nằm trong dashboard học viên.

## Vì sao làm task này?
Sau task visual redesign, giao diện nhìn đẹp hơn nhưng khi giả lập người dùng thật xuất hiện nhiều lỗi UX nghiêm trọng:
- Đã ghi danh course nhưng vào course detail vẫn thấy nút đăng ký.
- Chỉ khi bấm đăng ký lần nữa mới nhận được message đã ghi danh.
- Login xong bị đưa thẳng vào student dashboard và cảm giác bị kẹt, không biết quay lại trang chủ/danh sách khóa học để học thêm khóa khác.
- Khi dùng URL quay lại trang chủ lúc đã đăng nhập, header vẫn hiện nút đăng nhập/đăng ký làm user tưởng mình chưa login.
- Trang học bài đang nằm trong student dashboard layout, gây cảm giác học bài không phải một không gian riêng.
- Có thể còn nhiều lỗi state/navigation khác do redesign chưa kiểm tra hết flow authenticated/enrolled.

Task này ưu tiên sửa UX flow và state logic, không phải tiếp tục đổi style đại trà.

## Không làm trong task này
- Không redesign lại toàn bộ visual từ đầu.
- Không đổi backend API nếu frontend có thể xử lý bằng API hiện có.
- Không làm quiz.
- Không làm payment.
- Không làm upload file thật.
- Không làm admin feature mới.
- Không hardcode enrollment state.
- Không bỏ route guard/security hiện có.
- Không xóa loading/error/empty state.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/24_USER_FLOWS.md`
- `docs/25_SCREEN_LIST.md`
- `docs/26_API_PRIORITY.md`
- `docs/30_PERMISSION_MATRIX.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/10_FRONTEND_STRUCTURE.md`
- `docs/11_BACKEND_FRONTEND_CONFIG.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`

## File frontend cần rà soát
- `frontend/src/stores/auth.store.js`
- `frontend/src/router/guards.js`
- `frontend/src/router/index.js`
- `frontend/src/layouts/MainLayout.vue`
- `frontend/src/layouts/StudentLayout.vue`
- `frontend/src/pages/auth/LoginPage.vue`
- `frontend/src/pages/auth/RegisterPage.vue`
- `frontend/src/pages/public/HomePage.vue`
- `frontend/src/pages/public/CourseListPage.vue`
- `frontend/src/pages/public/CourseDetailPage.vue`
- `frontend/src/pages/student/StudentDashboardPage.vue`
- `frontend/src/pages/student/MyCoursesPage.vue`
- `frontend/src/pages/student/LessonLearningPage.vue`
- `frontend/src/components/lesson/LearningCurriculumSidebar.vue`
- `frontend/src/services/auth.service.js`
- `frontend/src/services/course.service.js`
- `frontend/src/services/student.service.js`
- `frontend/src/services/learning.service.js`

## Vấn đề cần xử lý

### 1. Header public sai trạng thái đăng nhập
- Khi user đã login và truy cập `/`, `/courses`, `/courses/:slug`, header không được hiện nút "Đăng nhập" / "Đăng ký".
- Header phải hiển thị trạng thái user đã đăng nhập:
  - tên/avatar nếu có.
  - link "Khóa học của tôi".
  - link "Khám phá khóa học" hoặc "Tất cả khóa học".
  - link Dashboard/Profile phù hợp role.
  - nút/logout menu nếu layout có.
- Kiểm tra bug có thể do `MainLayout.vue` đang dùng sai getter, ví dụ `authStore.token` thay vì `authStore.accessToken` hoặc `authStore.isAuthenticated`.

### 2. User đã login vẫn phải xem được public pages
- Login xong có thể redirect về dashboard theo mặc định, nhưng user không được bị "kẹt" trong student dashboard.
- Student layout cần có đường quay về:
  - Trang chủ.
  - Tất cả khóa học.
  - Khóa học của tôi.
- Public pages không nên chặn authenticated user.
- Route guard chỉ redirect khỏi `/login` và `/register`, không redirect user khỏi `/` hoặc `/courses`.

### 3. Course detail phải biết user đã enroll hay chưa
- Khi student đã ghi danh course, vào `/courses/:slug` phải hiển thị ngay trạng thái "Đã ghi danh" hoặc "Tiếp tục học".
- Không được bắt user bấm đăng ký lần nữa mới biết mình đã enroll.
- Dùng API hiện có để kiểm tra:
  - `GET /api/users/me/courses` qua `StudentService.getMyCourses()`, hoặc
  - response course detail nếu backend đã có field enrollment.
- So sánh course theo `course.id` là chính, fallback slug nếu cần.
- Nếu đã enroll:
  - disable/hide nút đăng ký.
  - hiển thị CTA "Tiếp tục học".
  - CTA nên đưa tới lesson gần nhất hoặc lesson đầu tiên có thể học.
- Nếu chưa enroll và course free:
  - hiển thị "Đăng ký học miễn phí".
- Nếu chưa login:
  - click enroll/learn đưa sang login với redirect về course detail hoặc lesson phù hợp.

### 4. Sau enroll nên đưa user tới learning flow hợp lý
- Sau khi enroll thành công, không nên luôn đưa về `/student/dashboard` nếu có thể đưa thẳng vào khóa học/bài học đầu tiên.
- Ưu tiên:
  - lesson đầu tiên trong course nếu có.
  - nếu chưa xác định được lesson, đưa tới `/student/my-courses`.
- Message sau enroll cần rõ: "Ghi danh thành công, bắt đầu học".

### 5. Trang học bài phải là learning workspace riêng
- Route `/student/lessons/:id` hiện đang nằm trong `StudentLayout`, làm trang học bài có cảm giác nằm trong dashboard.
- Cần cân nhắc tạo layout riêng:
  - `LearningLayout.vue`, hoặc
  - route riêng ngoài `StudentLayout` nhưng vẫn yêu cầu auth role STUDENT.
- Learning page nên có topbar riêng gọn:
  - logo/brand.
  - nút quay về khóa học của tôi.
  - nút khám phá khóa học.
  - profile/logout nhỏ.
- Không để dashboard sidebar chiếm không gian học bài nếu user đang học lesson.
- Mobile không được bị bottom nav che nội dung học.
- Phần nav bên phải không hiện bài học tiếp theo cũng như các chương học của khoá hiệu tại nav bên phải chỉ hiện 1 khoảng trống vô nghĩa.

### 6. Rà lại authenticated navigation toàn app
- Sau login:
  - nếu có `redirect` query thì đi đúng redirect.
  - nếu không có redirect thì theo role vào dashboard.
- Sau logout:
  - clear auth state.
  - public header trở về trạng thái guest.
- Khi reload page:
  - `initAuth()` hydrate token/user trước khi route guard quyết định.
  - public header phải cập nhật đúng sau hydrate.

### 7. Rà lại UI state gây hiểu nhầm
- Button disabled phải có text rõ nghĩa.
- Không hiển thị "Đăng ký" nếu đã enroll.
- Không hiển thị "Đăng nhập" nếu đã login.
- Không để user click vào locked lesson mà không có feedback.
- Empty state phải có CTA phù hợp, ví dụ chưa có khóa học thì link tới `/courses`.

## Hướng triển khai đề xuất

### Bước 1 - Audit flow hiện tại
- Đọc auth store và route guards.
- Đọc MainLayout/StudentLayout.
- Đọc CourseDetailPage logic enroll.
- Đọc MyCoursesPage để biết response có `lastLessonId` hoặc field tương tự không.
- Đọc LessonLearningPage route/layout usage.

### Bước 2 - Fix auth header và navigation
- Sửa computed login state trong `MainLayout`.
- Hiển thị menu/link phù hợp khi authenticated.
- Đảm bảo public pages vẫn dùng được khi logged in.

### Bước 3 - Fix enrollment state trên course detail
- Khi course detail load xong và user là STUDENT đã login, fetch my courses.
- Set `isEnrolled` trước khi user click enroll.
- Nếu enrolled, hiển thị CTA tiếp tục học.

### Bước 4 - Fix learning layout
- Tách lesson learning route ra khỏi `StudentLayout` nếu phù hợp.
- Tạo `LearningLayout.vue` nếu cần.
- Đảm bảo route vẫn protected bởi auth + STUDENT.

### Bước 5 - Test regression
- Cập nhật unit/component tests nếu selector/behavior đổi.
- Chạy build/test.

## Checklist
- [ ] Public header hiện đúng guest/authenticated state.
- [ ] Logged-in student vào `/` không thấy nút login/register.
- [ ] Logged-in student vào `/courses` để tìm khóa học khác được.
- [ ] Student dashboard có link rõ tới trang chủ và danh sách khóa học.
- [ ] Course detail tự phát hiện course đã enroll.
- [ ] Course detail không hiện CTA đăng ký nếu đã enroll.
- [ ] Course detail có CTA "Tiếp tục học" nếu đã enroll.
- [ ] Enroll thành công điều hướng hợp lý tới học bài hoặc my courses.
- [ ] Login redirect query vẫn hoạt động.
- [ ] Logout clear auth state và header cập nhật đúng.
- [ ] Lesson learning không còn bị cảm giác nằm trong dashboard sidebar.
- [ ] Learning route vẫn được bảo vệ bởi auth + student role.
- [ ] Mobile layout không bị bottom nav/dashboard nav che lesson content.
- [ ] Loading/error/empty state vẫn còn.
- [ ] `npm run build` pass.
- [ ] `npm test` pass hoặc lỗi được ghi rõ.

## Cách test sau khi hoàn thành
1. Mở trang chủ khi chưa login, thấy login/register.
2. Login student, quay lại `/`, không còn thấy login/register.
3. Từ student dashboard bấm về trang chủ/danh sách khóa học được.
4. Vào course đã enroll, thấy "Đã ghi danh" hoặc "Tiếp tục học" ngay lập tức.
5. Vào course chưa enroll, thấy nút đăng ký phù hợp.
6. Enroll course free, được đưa tới learning flow hợp lý.
7. Logout, header public trở về trạng thái guest.
8. Login với redirect query, sau login về đúng trang.
9. Mở `/student/lessons/{id}`, trang học bài không bị dashboard sidebar/bottom nav làm rối.
10. Test desktop và mobile.
11. Chạy `npm run build`.
12. Chạy `npm test`.

## Kết quả mong muốn
Frontend không chỉ đẹp mà còn đúng flow người dùng thật: đăng nhập rõ ràng, ghi danh rõ ràng, khám phá khóa học sau login dễ dàng, học bài trong workspace riêng và không còn các CTA gây hiểu nhầm.
