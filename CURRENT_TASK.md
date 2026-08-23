# CURRENT TASK

## Task hiện tại
Student Course Learning Navigation & Curriculum Sidebar

## Trạng thái
TODO

## Mục tiêu
Hoàn thiện trải nghiệm học P0 bằng cách thêm curriculum/sidebar trong trang học bài, cho phép student xem danh sách section/lesson của course, highlight bài đang học và chuyển bài trước/sau thuận tiện.

## Vì sao làm task này?
Hiện tại student đã có thể mở một lesson theo `/student/lessons/:id`, lưu progress, đánh dấu hoàn thành và xem tài liệu đính kèm. Tuy nhiên trải nghiệm học vẫn bị rời rạc vì trang học bài chưa có danh sách bài trong course và chưa có điều hướng bài trước/bài sau. Trước khi mở module lớn như quiz, nên hoàn thiện core learning flow của P0 để sản phẩm học khóa học mượt hơn.

## Không làm trong task này
- Không làm quiz.
- Không làm payment.
- Không làm upload file.
- Không làm lesson resource CRUD mới.
- Không redesign toàn bộ learning page.
- Không làm video auto tracking.
- Không làm drag-and-drop/reorder.
- Không làm admin course structure.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/24_USER_FLOWS.md`
- `docs/25_SCREEN_LIST.md`
- `docs/26_API_PRIORITY.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/10_FRONTEND_STRUCTURE.md`
- `docs/11_BACKEND_FRONTEND_CONFIG.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`
- `docs/05_features/05_02_COURSE_FEATURES.md`
- `docs/05_features/05_03_LEARNING_PROGRESS_FEATURES.md`
- `docs/07_database/07_02_COURSE_LESSON.md`
- `docs/08_api/08_04_LESSON_API.md`

## Vấn đề hiện tại
- `LessonLearningPage.vue` chỉ load lesson theo `lessonId`.
- `LessonLearningRes` hiện chưa trả `courseId`, `courseTitle` hoặc curriculum.
- Student không thấy các lesson khác trong cùng course khi đang học.
- Student chưa có nút bài trước/bài sau.
- Khi hoàn thành một bài, UI chưa giúp chuyển tự nhiên sang bài tiếp theo.

## Hướng triển khai đề xuất

### Backend
Tùy codebase hiện tại, chọn cách ít rủi ro nhất:

#### Option A - Mở rộng lesson detail response
Mở rộng `LessonLearningRes` để trả thêm:
- `courseId`
- `courseTitle`
- `courseSlug`
- `sections`
- `previousLessonId`
- `nextLessonId`

Trong đó `sections` là curriculum rút gọn:
```json
[
  {
    "id": 1,
    "title": "Chương 1",
    "sortOrder": 1,
    "lessons": [
      {
        "id": 10,
        "title": "Bài 1",
        "sortOrder": 1,
        "durationMinutes": 15,
        "isPreview": true,
        "isCompleted": false,
        "watchedPercent": 20
      }
    ]
  }
]
```

#### Option B - Tạo endpoint curriculum riêng
Nếu response lesson detail trở nên quá lớn, tạo endpoint:
```http
GET /api/v1/lessons/{id}/curriculum
Authorization: Bearer <accessToken>
```

Endpoint này trả course info + sections + lessons + previous/next lesson.

### Backend access rule
- Reuse logic access hiện có trong `LearningServiceImpl`.
- Course phải `PUBLISHED`.
- Nếu lesson không preview thì student phải enroll.
- Curriculum nên chỉ trả các lesson `PUBLISHED`.
- Với student đã enroll, có thể xem toàn bộ lesson list trong course.
- Với preview/non-enrolled nếu sau này cần public preview, không làm trong task này trừ khi logic hiện có đã hỗ trợ.

### Frontend
Cập nhật `LessonLearningPage.vue`:
- Thêm layout có curriculum/sidebar bên trái hoặc bên phải.
- Sidebar hiển thị:
  - course title.
  - danh sách section.
  - danh sách lessons trong từng section.
  - highlight lesson hiện tại.
  - badge completed nếu lesson đã hoàn thành.
  - watchedPercent nếu có dữ liệu.
- Click lesson trong sidebar điều hướng tới `/student/lessons/{lessonId}`.
- Thêm nút:
  - "Bài trước"
  - "Bài tiếp theo"
- Disable nút khi không có previous/next.
- Sau khi `markCompleted()` thành công, nếu có `nextLessonId`, có thể hiển thị nút/chỉ dẫn để học bài tiếp theo, nhưng không tự chuyển trang bắt buộc.
- Giữ nguyên progress panel và resources panel hiện có.
- Lỗi curriculum không được làm hỏng lesson content chính nếu tách endpoint riêng.

## Cần tạo hoặc chỉnh sửa

### Backend
- `backend/src/main/java/com/japaneselearning/module_learning/dto/LessonLearningRes.java`
- Có thể tạo DTO mới:
  - `LearningCurriculumRes`
  - `LearningSectionRes`
  - `LearningLessonItemRes`
- `backend/src/main/java/com/japaneselearning/module_learning/service/LearningService.java`
- `backend/src/main/java/com/japaneselearning/module_learning/service/LearningServiceImpl.java`
- `backend/src/main/java/com/japaneselearning/module_learning/controller/LearningController.java`
- Có thể chỉnh repository nếu cần query section/lesson theo course.

### Frontend
- `frontend/src/services/learning.service.js`
- `frontend/src/pages/student/LessonLearningPage.vue`
- Có thể tạo component nhỏ nếu giúp code gọn:
  - `frontend/src/components/lesson/LearningCurriculumSidebar.vue`

## Checklist
- [ ] Lesson learning page biết được course hiện tại.
- [ ] Sidebar hiển thị sections và lessons của course.
- [ ] Lesson hiện tại được highlight rõ.
- [ ] Completed/progress của từng lesson hiển thị nếu backend trả dữ liệu.
- [ ] Click lesson trong sidebar điều hướng đúng.
- [ ] Có nút bài trước/bài tiếp theo.
- [ ] Previous/next disable đúng khi ở đầu/cuối course.
- [ ] Progress panel hiện có vẫn hoạt động.
- [ ] Resource panel hiện có vẫn hoạt động.
- [ ] Access rule backend không bị nới lỏng sai.
- [ ] Chạy frontend build/test.
- [ ] Chạy backend package/test phù hợp, hoặc ghi rõ blocker test môi trường nếu còn lỗi Mockito/Byte Buddy.

## Cách test sau khi hoàn thành
1. Đăng nhập bằng STUDENT đã enroll course.
2. Vào `/student/lessons/{lessonId}`.
3. Kiểm tra sidebar hiển thị course curriculum.
4. Kiểm tra lesson hiện tại được highlight.
5. Click lesson khác trong sidebar, kỳ vọng route đổi và lesson mới load.
6. Bấm "Bài trước" và "Bài tiếp theo", kỳ vọng điều hướng đúng.
7. Hoàn thành lesson, kỳ vọng progress/resource flow cũ vẫn ổn.
8. Student chưa enroll truy cập lesson non-preview, kỳ vọng vẫn bị chặn.
9. Chạy `npm run build`.
10. Chạy `npm test`.
11. Chạy backend package/test phù hợp.

## Kết quả mong muốn
Luồng học P0 hoàn chỉnh hơn: student không chỉ xem từng lesson đơn lẻ mà có thể học xuyên suốt một course, biết mình đang ở đâu trong curriculum và chuyển bài thuận tiện.
