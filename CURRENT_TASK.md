# CURRENT TASK

## Task hiện tại
Frontend Student Quiz Taking Integration

## Trạng thái
DONE

## Mục tiêu
Xây dựng giao diện frontend để student có thể làm quiz thật bằng các API backend đã có: xem quiz, bắt đầu attempt, chọn đáp án, submit, xem kết quả và xem lịch sử làm quiz. Task này tập trung vào student quiz UX, chưa làm admin quiz UI đầy đủ.

## Vì sao làm task này?
Backend hiện đã có data model quiz, admin quiz APIs và student quiz taking APIs. Bước tiếp theo hợp lý nhất là nối frontend vào các API này để hoàn thiện vòng học tập P0/P1: học bài xong có thể làm quiz và xem kết quả ngay trong trải nghiệm student.

## Không làm trong task này
- Không redesign toàn bộ website.
- Không làm frontend admin quiz builder đầy đủ.
- Không làm drag/drop matching/reorder phức tạp.
- Không làm timer enforcement tuyệt đối nếu backend chưa enforce expired attempt.
- Không làm payment/order.
- Không đổi schema quiz nếu không bắt buộc.
- Không refactor lớn ngoài quiz learning flow.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/24_USER_FLOWS.md`
- `docs/26_API_PRIORITY.md`
- `docs/28_ENUM_DEFINITIONS.md`
- `docs/29_ERROR_CODE_STANDARD.md`
- `docs/30_PERMISSION_MATRIX.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`
- `docs/05_features/05_04_QUIZ_FEATURES.md`
- `docs/08_api/08_05_QUIZ_API.md`

## Backend API đã có để tích hợp

```http
GET  /api/v1/quizzes/{id}
POST /api/v1/quizzes/{id}/start
POST /api/v1/quizzes/{id}/submit
GET  /api/v1/quizzes/{id}/result/{attemptId}
GET  /api/users/me/quiz-attempts
```

## Frontend cần triển khai

### Service
- Tạo hoặc cập nhật `frontend/src/services/quiz.service.js`.
- Hàm đề xuất:
  - `getQuiz(id)`
  - `startQuiz(id)`
  - `submitQuiz(id, payload)`
  - `getQuizResult(id, attemptId)`
  - `getMyQuizAttempts()`

### Router
- Thêm route student làm quiz:
  - `/student/quizzes/:quizId`
  - `/student/quizzes/:quizId/result/:attemptId`
- Route phải yêu cầu đăng nhập student.

### Pages/components
- Tạo `QuizTakingPage.vue`:
  - Load quiz detail.
  - Không giả định response có `isCorrect`.
  - Có nút bắt đầu làm bài.
  - Render câu hỏi và đáp án theo `SINGLE_CHOICE`, `TRUE_FALSE` trước.
  - Với type chưa hỗ trợ UI đầy đủ, hiển thị trạng thái chưa hỗ trợ rõ ràng hoặc fallback text input nếu backend đang nhận `userAnswerText`.
  - Submit payload đúng contract backend.
  - Điều hướng sang result sau khi submit thành công.
- Tạo `QuizResultPage.vue`:
  - Load result theo `quizId` và `attemptId`.
  - Hiển thị score, passing score, passed/failed, correct/wrong count.
  - Hiển thị từng câu, đáp án đã chọn, đáp án đúng, explanation nếu có.
- Tích hợp lịch sử quiz:
  - Có thể thêm vào dashboard/profile một block nhỏ lấy `/api/users/me/quiz-attempts`.
  - Không làm màn hình history phức tạp nếu chưa cần.

## Tích hợp với lesson learning flow
- Kiểm tra frontend hiện có biết `quizId` của lesson không.
- Nếu đã có dữ liệu quiz trong lesson/course response, thêm CTA từ màn hình học bài sang quiz.
- Nếu frontend chưa có cách discover quiz theo lesson, ghi rõ blocker trong docs hoặc thêm TODO cho task backend nhỏ kế tiếp như `GET /api/v1/lessons/{lessonId}/quizzes`.
- Không hardcode quiz id trong UI chính.

## UX yêu cầu
- Khi chưa start attempt, user thấy thông tin quiz và nút bắt đầu.
- Khi đang làm bài, user thấy tiến độ số câu đã trả lời.
- Nút submit bị disabled nếu chưa có attempt hoặc đang submit.
- Hiển thị lỗi thân thiện khi:
  - Quiz chưa publish/không tìm thấy.
  - User chưa enroll course.
  - Hết số lần làm quiz.
  - Attempt đã submit.
- Sau khi submit thành công, chuyển sang trang kết quả.
- Không để màn hình học quiz bị nhốt sai trong dashboard nếu route student hiện tại gây UX xấu.

## Checklist
- [x] `quiz.service.js` gọi đúng toàn bộ API student quiz.
- [x] Route làm quiz và route result hoạt động.
- [x] Quiz detail không phụ thuộc vào `isCorrect`.
- [x] Start attempt hoạt động và lưu `attemptId`.
- [x] Submit payload đúng format backend.
- [x] Result page hiển thị score/passed/correct/wrong.
- [x] Attempt history hiển thị tối thiểu ở dashboard/profile nếu phù hợp.
- [x] Lỗi API được hiển thị thân thiện.
- [x] Không hardcode quiz id trong production UI.
- [x] Build/test frontend pass hoặc blocker được ghi rõ.

## Cách test sau khi hoàn thành
1. Login bằng student đã enroll course có quiz published.
2. Mở trang quiz bằng route `/student/quizzes/:quizId`.
3. Kiểm tra quiz detail không cần `isCorrect`.
4. Bấm bắt đầu làm bài, nhận attempt.
5. Chọn đáp án và submit.
6. Kiểm tra điều hướng sang result page.
7. Kiểm tra score, passed/failed, đáp án đúng/sai.
8. Mở dashboard/profile để kiểm tra lịch sử attempt nếu đã tích hợp.
9. Thử submit khi hết `maxAttempts`, kỳ vọng UI hiển thị lỗi dễ hiểu.
