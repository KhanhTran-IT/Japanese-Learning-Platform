# CURRENT TASK

## Task hiện tại
Course Detail Enrollment CTA & Discovery Flow Hardening

## Trạng thái
TODO

## Mục tiêu
Rà soát và làm cứng toàn bộ flow khám phá khóa học sau khi đã nâng cấp Course Search & Filter: từ Course List sang Course Detail, đăng ký khóa học, quay lại My Courses, tiếp tục học, và điều hướng để học viên có thể đăng ký thêm khóa học khác.

## Vì sao làm task này?
Đây là task hợp lý nhất tiếp theo vì nó xử lý trực tiếp các lỗi UX gây khó chịu trong flow học viên:
- User đã đăng ký khóa học nhưng vào Course Detail vẫn thấy nút "Đăng ký khóa học".
- User đăng nhập xong bị đẩy vào dashboard và khó quay lại trang chủ/danh sách khóa học để đăng ký khóa khác.
- Navbar/Home/Courses có thể hiển thị trạng thái gây nhầm lẫn như user chưa đăng nhập.
- Course Detail chưa đủ thông minh để đổi CTA theo trạng thái enrollment.
- Discovery flow cần ổn định trước khi mở rộng feature lớn hơn như payment, review, recommendation hoặc redesign UI sâu.

## Không làm trong task này
- Không làm payment thật.
- Không redesign toàn bộ frontend.
- Không thay đổi schema nếu không bắt buộc.
- Không thêm module recommendation.
- Không xử lý quiz/flashcard mới.
- Không thêm admin CMS mới.

## File cần kiểm tra/chỉnh sửa
- `frontend/src/pages/public/CourseDetailPage.vue`
- `frontend/src/pages/public/CourseDetailPage.spec.js`
- `frontend/src/pages/public/CourseListPage.vue`
- `frontend/src/components/common/Navbar.vue`
- `frontend/src/layouts/LearningLayout.vue`
- `frontend/src/router/index.js`
- `frontend/src/services/course.service.js`
- `frontend/src/services/student.service.js`
- `frontend/src/stores/auth.store.js`
- `backend/src/main/java/com/japaneselearning/module_course/controller/CoursePublicController.java`
- `backend/src/main/java/com/japaneselearning/module_course/service/CoursePublicServiceImpl.java`
- `backend/src/main/java/com/japaneselearning/module_enrollment/repository/CourseEnrollmentRepository.java`
- `docs/learning/LEARNING_LOG.md`
- `docs/learning/INTERVIEW_NOTES.md`
- `docs/learning/CONCEPTS_EXPLAINED.md`
- `docs/learning/BUG_LOG.md` nếu phát hiện bug thực tế

## Phạm vi thực hiện

### 1. Course Detail Enrollment State
- Kiểm tra Course Detail hiện đang lấy dữ liệu enrollment bằng cách nào.
- Nếu backend chưa trả trạng thái enrollment, cân nhắc bổ sung contract an toàn cho user đang login.
- Nếu user chưa đăng nhập: CTA là "Đăng nhập để đăng ký" hoặc điều hướng login hợp lý.
- Nếu user đã đăng nhập nhưng chưa đăng ký: CTA là "Đăng ký khóa học" hoặc "Mua khóa học" tùy course type.
- Nếu user đã đăng ký: CTA đổi thành "Tiếp tục học" hoặc "Vào khóa học".
- Không gọi enroll lại khi user đã enrolled.

### 2. Discovery Navigation
- Đảm bảo học viên đã login vẫn truy cập được `/`, `/courses`, `/courses/:slug`.
- Navbar phải hiển thị trạng thái đăng nhập đúng.
- Student dashboard/my-courses phải có đường rõ ràng quay lại danh sách khóa học.
- Learning page là trải nghiệm riêng, nhưng không khóa user khỏi discovery flow.

### 3. Frontend Contract & Tests
- Cập nhật test Course Detail cho các trạng thái:
  - guest.
  - logged-in chưa enroll.
  - logged-in đã enroll.
  - enroll success chuyển CTA đúng.
- Cập nhật test Navbar/navigation nếu cần.
- Giữ response handling nhất quán với service layer.

### 4. Backend Contract & Tests
- Nếu cần thêm enrollment state vào public course detail, đảm bảo guest request vẫn hoạt động.
- Tránh leak dữ liệu riêng tư.
- Thêm/cập nhật test backend nếu có thay đổi contract.

## Checklist
- [ ] Course Detail không hiện nút đăng ký khi user đã đăng ký khóa học.
- [ ] User đã đăng ký có CTA "Tiếp tục học" hoặc điều hướng học phù hợp.
- [ ] Guest vẫn xem được Course Detail và được điều hướng login khi muốn đăng ký.
- [ ] User đã login vẫn vào được trang chủ và danh sách khóa học.
- [ ] Navbar hiển thị đúng trạng thái auth trên public pages.
- [ ] My Courses/Dashboard có đường rõ ràng để khám phá khóa học khác.
- [ ] Frontend tests cover các trạng thái CTA chính.
- [ ] Backend tests được cập nhật nếu thay đổi API contract.
- [ ] Learning docs được cập nhật sau khi hoàn tất.
- [ ] Đề xuất task tiếp theo sau khi hoàn tất flow hardening.

## Gợi ý branch
`feature/339-Course-Detail-Enrollment-CTA-Discovery-Flow-Hardening`
