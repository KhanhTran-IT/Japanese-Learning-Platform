# CURRENT TASK

## Task hiện tại
Lesson Learning Route/API Contract Alignment

## Trạng thái
TODO

## Mục tiêu
Thống nhất contract giữa frontend route học bài và backend Lesson Learning API trước khi hoàn thiện LearningPage. Hiện frontend đang điều hướng tới `/student/lessons/:slug`, trong khi backend API học bài hiện là `GET /api/v1/lessons/{id}` và `POST /api/v1/lessons/{id}/progress`. Cần chọn hướng xử lý rõ ràng, cập nhật backend/frontend để student có thể mở đúng bài học sau khi bấm "Học tiếp".

## Vì sao làm task này?
MyCoursesPage đã có nút "Học tiếp", nhưng dữ liệu hiện có chỉ trả `lastLessonSlug`, còn backend learning API nhận `lessonId`. Nếu không xử lý contract này trước, task LearningPage sẽ dễ bị 404 hoặc phải code tạm. Đây là bước khóa khớp backend/frontend để chuẩn bị làm giao diện học bài thật.

## Không làm trong task này
- Không xây dựng UI LearningPage đầy đủ.
- Không làm video player nâng cao.
- Không làm complete lesson UI.
- Không làm quiz.
- Không làm payment.
- Không thay đổi nghiệp vụ enrollment nếu không cần.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/24_USER_FLOWS.md`
- `docs/25_SCREEN_LIST.md`
- `docs/26_API_PRIORITY.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/05_features/05_02_COURSE_FEATURES.md`
- `docs/07_database/07_02_COURSE_LESSON.md`
- `docs/08_api/08_04_LESSON_API.md`
- `docs/10_FRONTEND_STRUCTURE.md`
- `docs/11_BACKEND_FRONTEND_CONFIG.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`

## API cần làm hoặc điều chỉnh
Chọn một trong hai hướng, ưu tiên hướng ít rủi ro và nhất quán nhất:

### Hướng A - Dùng lesson id cho learning route
```http
GET /api/v1/lessons/{id}
POST /api/v1/lessons/{id}/progress
```

Cần backend trả thêm `lastLessonId` trong `MyCourseRes`, frontend điều hướng `/student/lessons/{lastLessonId}`.

### Hướng B - Thêm API học bài theo slug
```http
GET /api/v1/courses/{courseSlug}/lessons/{lessonSlug}
POST /api/v1/lessons/{id}/progress
```

Vì `Lesson.slug` chỉ unique theo `course_id`, nếu dùng slug cần có thêm `courseSlug` hoặc course id.

## Request mẫu
Nếu chọn Hướng A:
```http
GET /api/v1/lessons/10
POST /api/v1/lessons/10/progress
```

Nếu chọn Hướng B:
```http
GET /api/v1/courses/n5-co-ban/lessons/bai-1-hiragana
```

## Response mong muốn
`GET lesson detail` trả `ApiResponse<LessonLearningRes>`:
```json
{
  "code": 1000,
  "message": "Lấy thông tin bài học thành công",
  "result": {
    "id": 10,
    "title": "Bài 1: Hiragana",
    "slug": "bai-1-hiragana",
    "content": "Nội dung bài học",
    "videoUrl": null,
    "isPreview": false,
    "sortOrder": 1,
    "durationMinutes": 15,
    "watchedPercent": 0,
    "isCompleted": false
  }
}
```

Nếu cập nhật `MyCourseRes`, response `GET /api/users/me/courses` nên có:
```json
{
  "courseId": 1,
  "courseName": "N5 cơ bản",
  "slug": "n5-co-ban",
  "progressPercent": 0,
  "completedLessons": 0,
  "totalLessons": 10,
  "lastLessonId": 10,
  "lastLessonName": "Bài 1: Hiragana",
  "lastLessonSlug": "bai-1-hiragana"
}
```

## Logic xử lý
- Rà soát backend `MyCourseRes`, `StudentDashboardServiceImpl`, `LearningController`, `LearningServiceImpl`.
- Quyết định dùng route học bài theo id hay theo courseSlug + lessonSlug.
- Nếu chọn Hướng A:
  - Thêm `lastLessonId` vào `MyCourseRes`.
  - Map `lastLessonId` trong `StudentDashboardServiceImpl`.
  - Cập nhật frontend `MyCoursesPage.vue` và `StudentDashboardPage.vue` điều hướng theo `lastLessonId`.
  - Cập nhật route student từ `lessons/:slug` thành `lessons/:id` nếu cần.
- Nếu chọn Hướng B:
  - Thêm repository method tìm lesson theo course slug + lesson slug.
  - Thêm endpoint public/protected phù hợp.
  - Giữ kiểm tra enrollment/preview như API học bài hiện tại.
- Dù chọn hướng nào, không được bỏ qua kiểm tra enrollment trong backend.
- Chạy backend tests và frontend build nếu có chỉnh cả hai bên.

## Cần tạo hoặc chỉnh sửa
- `backend/src/main/java/com/japaneselearning/module_learning/dto/MyCourseRes.java`
- `backend/src/main/java/com/japaneselearning/module_user/service/StudentDashboardServiceImpl.java`
- `backend/src/main/java/com/japaneselearning/module_learning/controller/LearningController.java` nếu thêm endpoint mới.
- `backend/src/main/java/com/japaneselearning/module_learning/service/LearningService.java` nếu thêm method mới.
- `backend/src/main/java/com/japaneselearning/module_learning/service/LearningServiceImpl.java` nếu thêm method mới.
- `backend/src/main/java/com/japaneselearning/module_course/repository/LessonRepository.java` nếu cần query mới.
- `frontend/src/router/index.js`
- `frontend/src/pages/student/MyCoursesPage.vue`
- `frontend/src/pages/student/StudentDashboardPage.vue`
- `frontend/src/pages/student/LessonLearningPage.vue` chỉ chỉnh route param/placeholder nếu cần, chưa làm UI đầy đủ.

## Error code cần dùng
- `LESSON_NOT_FOUND`
- `FORBIDDEN_ACCESS`
- `USER_NOT_FOUND`
- Validation lỗi nếu id/slug không hợp lệ.

## Checklist
- [ ] Chọn rõ hướng dùng lesson id hay courseSlug + lessonSlug.
- [ ] Frontend route học bài khớp backend API.
- [ ] MyCoursesPage điều hướng không còn lệch contract.
- [ ] StudentDashboard điều hướng không còn lệch contract.
- [ ] Backend vẫn kiểm tra enrollment cho lesson không preview.
- [ ] Response my courses có đủ dữ liệu để frontend điều hướng.
- [ ] Không làm LearningPage UI đầy đủ trong task này.
- [ ] Chạy `mvn test` nếu sửa backend.
- [ ] Chạy `npm run build` nếu sửa frontend.

## Cách test sau khi hoàn thành
1. Chạy backend tests nếu có sửa backend.
2. Chạy frontend build nếu có sửa frontend.
3. Đăng nhập bằng STUDENT đã enroll course.
4. Mở `/student/my-courses`.
5. Bấm "Học tiếp" hoặc "Bắt đầu học".
6. Kiểm tra route frontend sinh ra đúng theo contract đã chọn.
7. Kiểm tra API lesson detail trả 200 nếu student có quyền.
8. Kiểm tra student chưa enroll không mở được lesson non-preview.

## Kết quả mong muốn
Frontend và backend thống nhất cách định danh bài học cho learning flow. MyCoursesPage/StudentDashboard có thể điều hướng tới lesson route đúng, sẵn sàng cho task kế tiếp là xây dựng LearningPage và cập nhật progress.
