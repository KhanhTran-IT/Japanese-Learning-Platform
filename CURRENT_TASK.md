# CURRENT TASK

## Task hiện tại
Frontend Lesson Learning Page & Progress Integration

## Trạng thái
TODO

## Mục tiêu
Hoàn thiện `LessonLearningPage.vue` cho route `/student/lessons/:id`, tích hợp API thật `GET /api/v1/lessons/{id}` và `POST /api/v1/lessons/{id}/progress`. Trang cần hiển thị nội dung bài học, video/content nếu có, tiến độ hiện tại, nút cập nhật tiến độ/đánh dấu hoàn thành cơ bản và xử lý loading/error/forbidden state.

## Vì sao làm task này?
MyCoursesPage và StudentDashboard đã điều hướng đúng sang `/student/lessons/:id`. Backend Learning API đã có endpoint lấy bài học và cập nhật progress. Bước tiếp theo là thay placeholder LearningPage bằng giao diện học bài thật để student có thể bắt đầu học và lưu tiến độ.

## Không làm trong task này
- Không làm playlist/sidebar toàn bộ curriculum nếu backend chưa có API phù hợp.
- Không làm video player nâng cao hoặc tracking tự động theo thời gian xem.
- Không làm quiz.
- Không làm upload/download resource.
- Không làm payment.
- Không sửa nghiệp vụ enrollment nếu backend đã chặn đúng quyền học bài.
- Không làm UI quá phức tạp vượt MVP.

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

## API cần tích hợp
```http
GET /api/v1/lessons/{id}
Authorization: Bearer <accessToken>
```

```http
POST /api/v1/lessons/{id}/progress
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "watchedPercent": 50,
  "isCompleted": false
}
```

## Request mẫu
```http
GET /api/v1/lessons/10
```

```http
POST /api/v1/lessons/10/progress
{
  "watchedPercent": 100,
  "isCompleted": true
}
```

## Response mong muốn
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

## Logic xử lý
- Tạo hoặc cập nhật service cho learning API, ví dụ `frontend/src/services/learning.service.js`.
- Trong `LessonLearningPage.vue`, lấy `id` từ route params.
- Validate route param là số hợp lệ trước khi gọi API.
- Gọi `GET /api/v1/lessons/{id}` khi mounted hoặc khi id thay đổi.
- Hiển thị loading state khi đang tải.
- Hiển thị error state khi API lỗi.
- Nếu lỗi 403/forbidden, hiển thị thông báo user chưa có quyền học bài này và link về `/student/my-courses`.
- Render lesson title, duration, progress, content.
- Nếu có `videoUrl`, hiển thị video element đơn giản.
- Nếu có `content`, hiển thị text nội dung bài học an toàn, không dùng `v-html` nếu chưa sanitize.
- Cho phép student cập nhật watchedPercent bằng control đơn giản trong MVP, ví dụ input/range 0-100.
- Nút "Lưu tiến độ" gọi `POST /api/v1/lessons/{id}/progress`.
- Nút "Đánh dấu hoàn thành" gửi `watchedPercent: 100`, `isCompleted: true`.
- Khi update thành công, cập nhật UI local theo response/request đã gửi.
- Không làm giảm progress hiện tại trên UI nếu user nhập watchedPercent thấp hơn.
- Có link quay lại `/student/my-courses`.

## Cần tạo hoặc chỉnh sửa
- `frontend/src/pages/student/LessonLearningPage.vue`
- `frontend/src/services/learning.service.js`
- Có thể chỉnh `frontend/src/services/student.service.js` nếu muốn gom learning API, nhưng nên tách service riêng nếu rõ hơn.
- Có thể chỉnh `frontend/src/router/index.js` nếu cần props hoặc route name.

## Error code cần xử lý
- `LESSON_NOT_FOUND`
- `FORBIDDEN_ACCESS`
- `USER_NOT_FOUND`
- Validation error khi `watchedPercent < 0` hoặc `watchedPercent > 100`
- Lỗi 401/403 do token hoặc role không hợp lệ.

## Checklist
- [ ] `LessonLearningPage.vue` không còn placeholder.
- [ ] Page lấy lesson id từ route `/student/lessons/:id`.
- [ ] Có service gọi `GET /api/v1/lessons/{id}`.
- [ ] Có service gọi `POST /api/v1/lessons/{id}/progress`.
- [ ] Loading state hoạt động.
- [ ] Error/forbidden state hoạt động.
- [ ] Hiển thị title/content/video/progress cơ bản.
- [ ] Lưu tiến độ watchedPercent hoạt động.
- [ ] Đánh dấu hoàn thành gửi `isCompleted: true`.
- [ ] Không dùng `v-html` cho content chưa sanitize.
- [ ] Có link quay lại My Courses.
- [ ] Chạy `npm run build`.

## Cách test sau khi hoàn thành
1. Chạy backend Spring Boot.
2. Chạy frontend dev server.
3. Đăng nhập bằng STUDENT đã enroll course.
4. Mở `/student/my-courses`.
5. Bấm "Học tiếp" để vào `/student/lessons/{id}`.
6. Kiểm tra lesson detail hiển thị đúng.
7. Thay đổi watchedPercent và bấm lưu tiến độ.
8. Bấm đánh dấu hoàn thành.
9. Reload page và kiểm tra progress được giữ.
10. Dùng student chưa enroll mở lesson non-preview, kỳ vọng bị chặn.
11. Chạy `npm run build`.

## Kết quả mong muốn
Student có thể mở trang học bài thật, xem nội dung bài học và lưu tiến độ cơ bản bằng API backend. Đây là nền tảng để tiếp tục làm lesson navigation, resources, complete flow nâng cao và quiz sau bài học.
