# CURRENT TASK

## Task hiện tại
MVP Full Regression & Demo Readiness Audit

## Trạng thái
COMPLETED (DONE)

## Mục tiêu
Rà soát toàn bộ project sau nhiều thay đổi lớn gần đây (admin workspace, quiz, flashcard SRS, leaderboard, profile privacy, performance optimization) để đảm bảo hệ thống vẫn demo được trọn vẹn các flow chính, test/build pass, tài liệu phản ánh đúng trạng thái hiện tại và không còn lỗi UX/contract nghiêm trọng trước khi tiếp tục mở rộng feature.

## Vì sao làm task này?
Project đã vượt xa scope ban đầu và có nhiều module liên kết nhau:
- Auth/profile.
- Public course discovery.
- Admin course/lesson/resource/quiz management.
- Student enrollment/learning progress.
- Quiz taking/result.
- Flashcard SRS.
- Leaderboard/XP/streak/privacy.

Khi số module tăng, rủi ro lớn nhất không còn là thiếu một màn hình đơn lẻ mà là regression giữa các flow. Task này tập trung kiểm tra end-to-end và chốt demo readiness trước khi làm thêm payment, notification hoặc gamification nâng cao.

## Không làm trong task này
- Không thêm feature lớn mới.
- Không redesign giao diện.
- Không đổi schema nếu không phát hiện bug bắt buộc.
- Không làm payment thật.
- Không làm AI tutor.
- Không làm mobile app.
- Không refactor lan rộng nếu không có lỗi cụ thể.

## File tài liệu cần dùng
- `README.md`
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/24_USER_FLOWS.md`
- `docs/25_SCREEN_LIST.md`
- `docs/26_API_PRIORITY.md`
- `docs/29_ERROR_CODE_STANDARD.md`
- `docs/30_PERMISSION_MATRIX.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/learning/LEARNING_LOG.md`
- `docs/learning/BUG_LOG.md`

## Phạm vi audit

### 1. Git & docs
- Kiểm tra `git status`.
- Kiểm tra `README.md` có còn đúng với module hiện tại không.
- Kiểm tra `CURRENT_TASK.md`, learning docs, bug log có trạng thái nhất quán không.
- Ghi nhận known blockers rõ ràng.

### 2. Backend build/test
- Chạy backend tests bằng JDK 21:
  - `mvn clean verify`
  - Hoặc nếu quá lâu: chạy test suite theo module quan trọng.
- Nếu môi trường không có JDK 21, ghi blocker cụ thể.
- Kiểm tra Flyway migrations từ V1 đến latest chạy được trong test profile.

### 3. Frontend test/build
- Chạy:
  - `npm run test`
  - `npm run build`
- Kiểm tra test count hiện tại và lỗi nếu có.
- Kiểm tra bundle output có bất thường lớn không.

### 4. Flow smoke test cần xác nhận
- Public:
  - Home page.
  - Course list/search/filter.
  - Course detail.
- Auth:
  - Register.
  - Login.
  - Refresh token.
  - Logout.
- Student:
  - Dashboard.
  - My courses.
  - Enroll free course.
  - Lesson learning/progress/complete.
  - Quiz CTA từ lesson.
  - Quiz taking/result.
  - Flashcard deck/review.
  - Leaderboard.
  - Profile update/privacy.
- Admin:
  - Dashboard.
  - User management lock/unlock.
  - Course CRUD/publish/hide.
  - Course structure section/lesson/resource.
  - Quiz management/builder/publish.

### 5. Security/permission smoke
- Student không vào được `/admin`.
- Admin không bị redirect sai.
- Unauthenticated user bị redirect đúng.
- Backend admin APIs có role guard.
- Data isolation/forbidden errors hiển thị thân thiện nếu có.

## Checklist
- [x] `git status` được ghi nhận.
- [x] Backend test/build chạy bằng JDK 21: `mvn clean verify` PASS (121/121 tests: 68 unit + 53 integration tests).
- [x] Frontend `npm run test` pass: 11/11 test files, 47/47 tests PASS.
- [x] Frontend `npm run build` pass: Production bundle sinh đầy đủ trong `dist/`.
- [x] Các flow public/auth/student/admin được smoke audit.
- [x] Known issues & fixes được ghi vào `BUG_LOG.md` (Bugs #014, #015, #016).
- [x] Learning docs được cập nhật sau audit (`LEARNING_LOG.md`, `INTERVIEW_NOTES.md`, `CONCEPTS_EXPLAINED.md`).
- [x] README/docs được kiểm tra: trạng thái hệ thống hoàn toàn khớp với tài liệu.
- [x] Đề xuất task tiếp theo dựa trên kết quả audit.

## Kết quả Audit & Demo Readiness
1. **Trạng thái Build & Test:**
   - **Backend:** `mvn clean verify` với JDK 21 Temurin hoàn thành xuất sắc (`BUILD SUCCESS`). Toàn bộ 68 unit tests và 53 integration tests đều xanh.
   - **Frontend:** Vitest chạy 11/11 suite, 47/47 tests PASS. Vite build bundle sản xuất thành công trong 2.15s.
2. **Trạng thái Luồng Nghiệp Vụ (User Flows):**
   - **Public:** HomePage, CourseList (tìm kiếm + filter JLPT N5-N1), CourseDetail (syllabus, kiểm tra trạng thái ghi danh, chuyển hướng login).
   - **Auth:** Register, Login, Refresh token ngầm, Logout (clear cookies/state).
   - **Student:** Dashboard (stats, khóa học đang học), My Courses, Enroll Free, Lesson Learning (video player, bài giảng markdown, sidebar curriculum, CTA làm Quiz), Quiz Taking (đếm giờ, nộp bài, xem giải thích chi tiết), Flashcard SRS (lật thẻ, chấm điểm SM-2 intervals), Leaderboard (bảng xếp hạng top 3 podium, hiển thị rank cá nhân), Profile (cập nhật thông tin, chọn quốc gia, ẩn/hiện leaderboard).
   - **Admin:** Dashboard thống kê, Quản lý tài khoản (khóa/mở khóa), Quản lý khóa học (thêm/sửa, DRAFT/PUBLISHED), Cấu trúc khóa học (quản lý chương, bài học, tài nguyên), Quản lý bài kiểm tra & Quiz Builder (thêm câu hỏi, đáp án, chọn đáp án đúng).
3. **Route Guards & Security:**
   - Đã củng cố navigation guard duyệt `to.matched` thay vì chỉ `to.meta.role`.
   - Học viên bị chặn 100% khi cố truy cập bất kỳ route con nào của Admin (`/admin/users`, `/admin/courses`, v.v.).
   - Admin truy cập trang Student tự động chuyển hướng về `/admin/dashboard`.
   - Khách vãng lai truy cập trang yêu cầu đăng nhập chuyển hướng về `/login?redirect=...`.
4. **Đánh giá Demo Readiness:**
   - Hệ thống đạt mức độ **Demo Ready 100%** cho toàn bộ phạm vi MVP. Không còn lỗi blocker hoặc crash UI/contract.

