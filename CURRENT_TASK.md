# CURRENT TASK

## Task hiện tại
Backend Lesson Complete API

## Trạng thái
TODO

## Mục tiêu
Thêm endpoint chuyên biệt `POST /api/v1/lessons/{id}/complete` để student đánh dấu hoàn thành bài học. Endpoint này nên tái sử dụng logic cập nhật progress hiện có, đặt `watchedPercent = 100`, `isCompleted = true`, cập nhật `completedAt` và tính lại `course_enrollments.progress_percent`.

## Vì sao làm task này?
Hiện frontend có thể đánh dấu hoàn thành bằng `POST /api/v1/lessons/{id}/progress` với `isCompleted=true`, nhưng tài liệu MVP và testing plan có endpoint riêng `POST /api/lessons/{id}/complete`. Một endpoint complete riêng giúp nghiệp vụ rõ ràng hơn, frontend dễ gọi hơn và backend có thể kiểm soát hành động hoàn thành bài học tốt hơn.

## Không làm trong task này
- Không làm frontend đổi sang gọi endpoint complete mới.
- Không làm quiz.
- Không làm lesson resources.
- Không làm video tracking tự động.
- Không đổi response DTO của lesson detail nếu chưa cần.
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

## API cần làm
```http
POST /api/v1/lessons/{id}/complete
Authorization: Bearer <accessToken>
```

## Request mẫu
```http
POST /api/v1/lessons/10/complete
```

Không cần body.

## Response mong muốn
```json
{
  "code": 1000,
  "message": "Hoàn thành bài học thành công",
  "result": null
}
```

## Logic xử lý
- Thêm method `completeLesson(Long lessonId)` vào `LearningService`.
- Implement trong `LearningServiceImpl`.
- Tái sử dụng hoặc tách helper chung từ `updateProgress()` để tránh duplicate quá nhiều:
  - lấy current user.
  - tìm lesson.
  - kiểm tra course `PUBLISHED`.
  - kiểm tra enrollment nếu lesson không preview.
  - upsert/update `LessonProgress`.
  - set `watchedPercent = 100`.
  - set `isCompleted = true`.
  - set `completedAt` nếu trước đó chưa completed.
  - tính lại `CourseEnrollment.progressPercent`.
- Thêm endpoint controller:
  - `@PostMapping("/{id}/complete")`
  - `@PreAuthorize("hasRole('STUDENT')")`
- Giữ rule idempotent: gọi complete nhiều lần không làm lỗi và không làm giảm progress.
- Không đưa logic nghiệp vụ vào controller.

## Cần tạo hoặc chỉnh sửa
- `backend/src/main/java/com/japaneselearning/module_learning/controller/LearningController.java`
- `backend/src/main/java/com/japaneselearning/module_learning/service/LearningService.java`
- `backend/src/main/java/com/japaneselearning/module_learning/service/LearningServiceImpl.java`
- Có thể chỉnh `backend/src/main/java/com/japaneselearning/module_learning/repository/LessonProgressRepository.java` nếu cần helper update riêng.
- Test backend liên quan nếu project đã có test cho learning/progress.

## Error code cần dùng
- `LESSON_NOT_FOUND`
- `FORBIDDEN_ACCESS`
- `USER_NOT_FOUND`

## Checklist
- [ ] Có endpoint `POST /api/v1/lessons/{id}/complete`.
- [ ] Endpoint yêu cầu role `STUDENT`.
- [ ] Complete lesson set watchedPercent = 100.
- [ ] Complete lesson set isCompleted = true.
- [ ] Complete lesson set completedAt hợp lý.
- [ ] Gọi complete nhiều lần không gây lỗi.
- [ ] Student chưa enroll không complete được lesson non-preview.
- [ ] Course enrollment progress được tính lại.
- [ ] Không duplicate logic quá nhiều với `updateProgress()`.
- [ ] Chạy `mvn test`.

## Cách test sau khi hoàn thành
1. Đăng nhập bằng STUDENT đã enroll course.
2. Gọi `POST /api/v1/lessons/{id}/complete`.
3. Kiểm tra `lesson_progress.watched_percent = 100`.
4. Kiểm tra `lesson_progress.is_completed = true`.
5. Kiểm tra `completed_at` có giá trị.
6. Kiểm tra `course_enrollments.progress_percent` được cập nhật.
7. Gọi complete lại lần nữa, kỳ vọng không lỗi.
8. Dùng student chưa enroll gọi complete lesson non-preview, kỳ vọng 403.
9. Chạy `mvn test`.

## Kết quả mong muốn
Backend có API hoàn thành bài học rõ ràng, idempotent và nhất quán với enrollment progress. Task này chuẩn bị cho frontend chuyển nút "Đánh dấu hoàn thành" sang endpoint chuyên biệt ở bước tiếp theo.
