# CURRENT TASK

## Task hiện tại
Course Detail Enrollment CTA & Discovery Flow Hardening

## Trạng thái
DONE ✅

## Tóm tắt thực hiện

### Backend
- **CourseDetailPublicRes.java**: Thêm `Boolean isEnrolled` + `Long lastLessonId` vào DTO.
- **CoursePublicServiceImpl.java**: Inject `UserRepository`, `CourseEnrollmentRepository`, `LessonProgressRepository`. Trong `getCourseDetailBySlug()`, detect authenticated user từ `SecurityContextHolder`, kiểm tra enrollment, tìm `lastLessonId` qua `progressRepository.findLatestProgressForEachCourseByUserId()`, populate vào response. Guest request vẫn nhận `isEnrolled=false`, `lastLessonId=null`.
- **CoursePublicControllerIT.java** (NEW): 3 test cases — guest view, enrolled student, not-enrolled student. Tất cả PASS.

### Frontend
- **Navbar.vue**: Thêm "Trang chủ" vào desktop nav. Thêm "Khóa học của tôi" (`/student/my-courses`) cho student ở cả desktop và mobile menu. Thêm `closeMobileMenu()` helper.
- **HomePage.vue**: Import `useAuthStore`. Bottom CTA banner hiện "Vào học ngay → /student/dashboard" khi đã login thay vì "Đăng ký miễn phí → /register". Subtitle text cũng thay đổi contextual.
- **CourseDetailPage.vue**:
  - `checkEnrollmentStatus()` giờ ưu tiên dùng `course.isEnrolled` từ backend (instant, không cần gọi thêm API). Fallback sang `StudentService.getMyCourses()` nếu backend chưa trả.
  - Mobile CTA thống nhất với desktop: "▶ Tiếp tục học" + "✓ Đã ghi danh" + link "Xem khóa học của tôi →".
  - `handleContinueLearning()` ưu tiên `course.value.lastLessonId` từ backend.
  - `handleEnroll()` error handler check thêm `error.response?.data?.code === 4009`.
  - Thêm watcher `authStore.isAuthenticated` để re-fetch khi user login trên trang.
- **CourseDetailPage.spec.js**: Thêm test "uses backend isEnrolled field without extra API call". Tổng 6 tests, 49 tests toàn project đều PASS.

## Checklist
- [x] Course Detail không hiện nút đăng ký khi user đã đăng ký khóa học.
- [x] User đã đăng ký có CTA "Tiếp tục học" hoặc điều hướng học phù hợp.
- [x] Guest vẫn xem được Course Detail và được điều hướng login khi muốn đăng ký.
- [x] User đã login vẫn vào được trang chủ và danh sách khóa học.
- [x] Navbar hiển thị đúng trạng thái auth trên public pages.
- [x] My Courses/Dashboard có đường rõ ràng để khám phá khóa học khác.
- [x] Frontend tests cover các trạng thái CTA chính.
- [x] Backend tests được cập nhật nếu thay đổi API contract.
- [x] Learning docs được cập nhật sau khi hoàn tất.
- [x] Đề xuất task tiếp theo sau khi hoàn tất flow hardening.

## Gợi ý branch
`feature/339-Course-Detail-Enrollment-CTA-Discovery-Flow-Hardening`
