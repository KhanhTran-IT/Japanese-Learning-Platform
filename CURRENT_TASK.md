# CURRENT TASK

## Task hiện tại
MVP Full Regression & Demo Readiness Audit

## Trạng thái
TODO

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
- [ ] `git status` được ghi nhận.
- [ ] Backend test/build chạy hoặc blocker JDK 21 được ghi rõ.
- [ ] Frontend `npm run test` pass hoặc lỗi được ghi rõ.
- [ ] Frontend `npm run build` pass hoặc lỗi được ghi rõ.
- [ ] Các flow public/auth/student/admin được smoke audit.
- [ ] Known issues được ghi vào `BUG_LOG.md`.
- [ ] Learning docs được cập nhật sau audit.
- [ ] README/docs được chỉnh nếu phát hiện lệch trạng thái.
- [ ] Đề xuất task tiếp theo dựa trên kết quả audit, không dựa trên task cũ.

## Cách test sau khi hoàn thành
1. Chạy test/build theo checklist.
2. Nếu có dev server, mở các route chính và kiểm tra UI không trắng màn hình.
3. Dùng account admin/student để đi qua flow demo chính.
4. Ghi rõ:
   - pass,
   - fail,
   - blocker môi trường,
   - task follow-up.
