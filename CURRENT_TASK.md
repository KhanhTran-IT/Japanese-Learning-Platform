# CURRENT TASK

## Task hiện tại
Backend Course Enrollment Progress Recalculation

## Trạng thái
TODO

## Mục tiêu
Cập nhật backend để mỗi lần student lưu progress hoặc đánh dấu hoàn thành bài học, hệ thống tính lại `course_enrollments.progress_percent` của course tương ứng. Đảm bảo progress ở bảng enrollment phản ánh đúng số bài đã hoàn thành trên tổng số bài học của khóa.

## Vì sao làm task này?
Frontend LearningPage đã gọi `POST /api/v1/lessons/{id}/progress` và backend đã lưu `lesson_progress`. Tuy nhiên entity `CourseEnrollment` có field `progressPercent`, trong khi testing plan yêu cầu `course_enrollments.progress_percent` được tính lại. Nếu không cập nhật field này, dữ liệu enrollment có thể bị lệch với lesson progress và các màn dashboard/my courses sau này dễ hiển thị sai nếu dựa vào enrollment.

## Không làm trong task này
- Không làm frontend LearningPage mới.
- Không làm video tracking tự động.
- Không làm quiz.
- Không làm payment.
- Không thay đổi API request/response nếu chưa cần.
- Không làm lại toàn bộ progress architecture.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/24_USER_FLOWS.md`
- `docs/26_API_PRIORITY.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/05_features/05_02_COURSE_FEATURES.md`
- `docs/07_database/07_02_COURSE_LESSON.md`
- `docs/08_api/08_04_LESSON_API.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`

## API cần làm hoặc điều chỉnh
Giữ endpoint hiện tại:
```http
POST /api/v1/lessons/{id}/progress
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "watchedPercent": 100,
  "isCompleted": true
}
```

## Response mong muốn
Giữ response hiện tại nếu không cần đổi:
```json
{
  "code": 1000,
  "message": "Cập nhật tiến độ thành công",
  "result": null
}
```

Nếu muốn trả progress mới cho frontend, chỉ làm khi thấy cần và không làm vỡ contract cũ.

## Logic xử lý
- Trong `LearningServiceImpl.updateProgress()`:
  - Sau khi upsert/update `LessonProgress`, xác định `courseId` từ `lesson.getCourse().getId()`.
  - Đếm số lesson completed của user trong course.
  - Lấy tổng số lesson của course, ưu tiên field `course.totalLessons` nếu đang được duy trì đúng; nếu không chắc, dùng repository count lessons theo course.
  - Tính phần trăm = completedLessons / totalLessons * 100.
  - Làm tròn về integer vì `CourseEnrollment.progressPercent` hiện là `Integer`.
  - Update đúng enrollment của `userId + courseId`.
- Nếu `totalLessons = 0`, progressPercent = 0.
- Không cho student cập nhật progress lesson thuộc course chưa enroll, giữ check hiện tại.
- Không làm giảm watchedPercent trong `LessonProgress`.
- Cần cân nhắc transaction để progress lesson và enrollment progress được cập nhật cùng nhau.

## Cần tạo hoặc chỉnh sửa
- `backend/src/main/java/com/japaneselearning/module_learning/service/LearningServiceImpl.java`
- `backend/src/main/java/com/japaneselearning/module_enrollment/repository/CourseEnrollmentRepository.java`
- `backend/src/main/java/com/japaneselearning/module_learning/repository/LessonProgressRepository.java`
- `backend/src/main/java/com/japaneselearning/module_course/repository/LessonRepository.java` nếu cần count total lessons theo course.
- Test backend liên quan nếu project đã có test cho learning/enrollment.

## Error code cần dùng
- `LESSON_NOT_FOUND`
- `FORBIDDEN_ACCESS`
- `USER_NOT_FOUND`
- Có thể dùng lỗi hiện có nếu enrollment không tồn tại trong trường hợp đáng lẽ phải có.

## Checklist
- [ ] Sau khi complete lesson, `lesson_progress.is_completed = true`.
- [ ] Sau khi complete lesson, `course_enrollments.progress_percent` được tính lại.
- [ ] Progress percent không vượt quá 100.
- [ ] Course không có lesson thì progress = 0.
- [ ] Student chưa enroll không update được progress lesson non-preview.
- [ ] Rule watchedPercent monotonic vẫn giữ nguyên.
- [ ] Logic nằm trong service/repository, không đưa nghiệp vụ vào controller.
- [ ] Chạy `mvn test`.

## Cách test sau khi hoàn thành
1. Tạo course có nhiều lesson và student đã enroll.
2. Gọi `POST /api/v1/lessons/{id}/progress` với `isCompleted=true`.
3. Kiểm tra `lesson_progress` được cập nhật.
4. Kiểm tra `course_enrollments.progress_percent` tăng đúng theo số bài completed/tổng số bài.
5. Complete thêm lesson khác và kiểm tra progress tăng tiếp.
6. Gửi watchedPercent thấp hơn hiện tại, kiểm tra watchedPercent không giảm.
7. Dùng student chưa enroll gọi lesson non-preview, kỳ vọng bị chặn.
8. Chạy `mvn test`.

## Kết quả mong muốn
Backend lưu lesson progress và enrollment progress nhất quán. Dashboard, My Courses và các màn học tập sau này có thể dựa vào `course_enrollments.progress_percent` mà không bị lệch dữ liệu.
