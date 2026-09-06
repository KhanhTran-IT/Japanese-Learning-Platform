# CURRENT TASK

## Task hiện tại
Backend Lesson Quiz Discovery API

## Trạng thái
DONE

## Mục tiêu
Bổ sung API backend tối thiểu để frontend có thể biết một lesson hoặc course đang có quiz published nào, từ đó hiển thị CTA "Làm quiz" đúng chỗ trong lesson learning flow. Task này giải khoảng trống sau khi đã có trang làm quiz frontend nhưng chưa có cách discover quiz tự nhiên từ bài học.

## Vì sao làm task này?
Frontend hiện đã có route làm quiz trực tiếp `/student/quizzes/:quizId`, nhưng người học không nên phải biết `quizId` thủ công. Trong flow thật, student đang học lesson cần thấy quiz liên quan ngay trên màn hình học bài. Vì vậy backend cần expose contract rõ ràng để tìm quiz theo lesson/course, vẫn giữ access rule và publish rule an toàn.

## Không làm trong task này
- Không làm admin quiz builder UI.
- Không redesign toàn bộ frontend.
- Không làm scoring mới.
- Không làm timer enforcement.
- Không đổi schema nếu có thể query bằng quan hệ quiz hiện tại.
- Không hardcode quiz id ở frontend.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/24_USER_FLOWS.md`
- `docs/26_API_PRIORITY.md`
- `docs/29_ERROR_CODE_STANDARD.md`
- `docs/30_PERMISSION_MATRIX.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`
- `docs/05_features/05_04_QUIZ_FEATURES.md`
- `docs/08_api/08_05_QUIZ_API.md`

## API đề xuất

Ưu tiên endpoint rõ theo ngữ cảnh lesson:

```http
GET /api/v1/lessons/{lessonId}/quizzes
```

Response trả danh sách quiz published mà student được phép thấy:

```json
[
  {
    "id": 1,
    "courseId": 10,
    "lessonId": 100,
    "title": "Quiz bài 1",
    "description": "Ôn tập từ vựng bài 1",
    "timeLimitMinutes": 10,
    "passingScore": 8,
    "maxAttempts": 3,
    "questionCount": 5,
    "latestAttemptId": 20,
    "latestAttemptStatus": "SUBMITTED",
    "latestScore": 9,
    "latestPassed": true,
    "remainingAttempts": 2
  }
]
```

Có thể thêm endpoint course nếu codebase cần:

```http
GET /api/v1/courses/{courseId}/quizzes
```

Nhưng nếu scope cần gọn, làm lesson endpoint trước là đủ.

## Backend cần triển khai

### DTO
- Tạo `QuizDiscoveryRes` hoặc tên tương đương.
- Field tối thiểu:
  - `id`
  - `courseId`
  - `lessonId`
  - `title`
  - `description`
  - `timeLimitMinutes`
  - `passingScore`
  - `maxAttempts`
  - `questionCount`
  - attempt summary gần nhất nếu tính được.

### Repository
- Bổ sung query tìm quiz `PUBLISHED` theo `lessonId`.
- Nếu làm course endpoint, bổ sung query tìm quiz `PUBLISHED` theo `courseId`.
- Bổ sung query latest attempt theo current user và quiz nếu cần hiển thị trạng thái.

### Service
- Reuse access rule tương tự `QuizLearningServiceImpl`:
  - Lesson/course phải published.
  - Student phải enroll nếu lesson/course không public preview.
  - Chỉ trả quiz `PUBLISHED`.
- Không trả questions/answers trong discovery response.
- Tính `remainingAttempts` nếu `maxAttempts` có giới hạn.

### Controller
- Có thể đặt endpoint trong `QuizLearningController` hoặc controller learning phù hợp với package hiện tại.
- Route yêu cầu authenticated student nếu quiz thuộc course cần enroll.

## Frontend nhỏ nếu phù hợp
- Cập nhật `quiz.service.js` thêm:
  - `getLessonQuizzes(lessonId)`
- Cập nhật `LessonLearningPage.vue`:
  - Gọi discovery API theo lesson hiện tại.
  - Nếu có quiz, hiển thị CTA "Làm quiz".
  - Nếu quiz đã làm, hiển thị trạng thái gần nhất và nút xem kết quả nếu có `latestAttemptId`.
- Không redesign lesson page trong task này.

## Checklist
- [ ] API `GET /api/v1/lessons/{lessonId}/quizzes` hoạt động.
- [ ] Chỉ trả quiz `PUBLISHED`.
- [ ] Không trả question answer detail trong discovery response.
- [ ] Access rule enrollment không bị nới lỏng.
- [ ] Response có đủ thông tin để frontend render CTA.
- [ ] `remainingAttempts` hoặc attempt summary được tính đúng nếu có.
- [ ] Lesson learning page có CTA làm quiz nếu frontend scope cho phép.
- [ ] Không hardcode quiz id.
- [ ] Backend/frontend tests hoặc build pass, hoặc blocker được ghi rõ.

## Cách test sau khi hoàn thành
1. Tạo quiz published gắn với lesson.
2. Login student đã enroll course.
3. Gọi `GET /api/v1/lessons/{lessonId}/quizzes`, kỳ vọng thấy quiz.
4. Login student chưa enroll course, kỳ vọng bị chặn hoặc không thấy dữ liệu theo rule hiện tại.
5. Tạo quiz draft cùng lesson, kỳ vọng không xuất hiện trong response.
6. Làm quiz một lần, gọi lại discovery API và kiểm tra latest attempt/remaining attempts nếu có triển khai.
