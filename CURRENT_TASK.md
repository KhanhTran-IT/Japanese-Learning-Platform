# CURRENT TASK

## Task hiện tại
Backend Admin Quiz Management API Foundation

## Trạng thái
TODO

## Mục tiêu
Xây dựng API backend nền tảng để admin/teacher quản lý quiz, câu hỏi và đáp án. Task này tạo được dữ liệu quiz cho hệ thống, nhưng chưa làm student start/submit/result flow.

## Vì sao làm task này?
Task trước đã tạo data model quiz: entity, enum, repository và Flyway migration. Trước khi làm student làm quiz, cần có API admin để tạo quiz, thêm câu hỏi, thêm đáp án và publish quiz. Nếu không có dữ liệu quiz chuẩn từ admin, student quiz API sẽ khó test và dễ phải hardcode seed tạm.

## Không làm trong task này
- Không làm frontend admin quiz UI.
- Không làm frontend student quiz UI.
- Không làm API student start quiz.
- Không làm API submit/chấm điểm.
- Không làm quiz result page.
- Không làm random question.
- Không làm timer enforcement.
- Không làm payment/order.
- Không đổi lại schema quiz nếu không thật sự cần.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/26_API_PRIORITY.md`
- `docs/28_ENUM_DEFINITIONS.md`
- `docs/29_ERROR_CODE_STANDARD.md`
- `docs/30_PERMISSION_MATRIX.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`
- `docs/05_features/05_04_QUIZ_FEATURES.md`
- `docs/07_database/07_04_QUIZ.md`
- `docs/08_api/08_05_QUIZ_API.md`

## API cần triển khai

### Quiz admin API
Base path đề xuất:

```http
/api/v1/admin/quizzes
```

Endpoints:

```http
GET    /api/v1/admin/quizzes
POST   /api/v1/admin/quizzes
GET    /api/v1/admin/quizzes/{id}
PUT    /api/v1/admin/quizzes/{id}
DELETE /api/v1/admin/quizzes/{id}
PUT    /api/v1/admin/quizzes/{id}/publish
PUT    /api/v1/admin/quizzes/{id}/hide
```

### Question admin API
Endpoints:

```http
POST   /api/v1/admin/quizzes/{quizId}/questions
GET    /api/v1/admin/quizzes/{quizId}/questions
GET    /api/v1/admin/questions/{id}
PUT    /api/v1/admin/questions/{id}
DELETE /api/v1/admin/questions/{id}
```

### Answer admin API
Endpoints:

```http
POST   /api/v1/admin/questions/{questionId}/answers
GET    /api/v1/admin/questions/{questionId}/answers
PUT    /api/v1/admin/answers/{id}
DELETE /api/v1/admin/answers/{id}
```

## DTO đề xuất
Tạo package DTO trong `module_quiz/dto`.

### Quiz DTO
- `QuizCreateReq`
- `QuizUpdateReq`
- `QuizRes`

Field chính:
- `courseId`
- `lessonId`
- `title`
- `description`
- `timeLimitMinutes`
- `passingScore`
- `maxAttempts`
- `status`

Rule:
- `title` bắt buộc.
- `courseId` hoặc `lessonId` nên có ít nhất một cái.
- Nếu có `lessonId`, backend có thể suy ra course từ lesson nếu cần.
- `passingScore` không âm.
- `maxAttempts` nếu có thì phải lớn hơn 0.

### Question DTO
- `QuestionCreateReq`
- `QuestionUpdateReq`
- `QuestionRes`

Field chính:
- `questionType`
- `content`
- `audioUrl`
- `imageUrl`
- `explanation`
- `points`
- `sortOrder`

Rule:
- `questionType` bắt buộc.
- `content` bắt buộc.
- `points` phải lớn hơn 0.
- `sortOrder` không âm.

### Answer DTO
- `AnswerCreateReq`
- `AnswerUpdateReq`
- `AnswerRes`

Field chính:
- `content`
- `isCorrect`
- `sortOrder`

Rule:
- `content` bắt buộc.
- `sortOrder` không âm.

## Service cần tạo
Tạo service trong `module_quiz/service`:
- `QuizAdminService`
- `QuizAdminServiceImpl`

Service nên xử lý:
- Tạo/sửa/xóa/publish/hide quiz.
- Tạo/sửa/xóa question.
- Tạo/sửa/xóa answer.
- Map entity sang response DTO.
- Validate course/lesson/question/answer tồn tại.
- Validate ownership/data isolation cho teacher nếu role TEACHER được phép quản lý quiz.

## Permission
- `ADMIN` và `SUPER_ADMIN` được quản lý toàn bộ quiz.
- Nếu cho `TEACHER` quản lý quiz, phải áp dụng data isolation:
  - Teacher chỉ quản lý quiz thuộc course của mình.
  - Không được sửa quiz/câu hỏi/đáp án thuộc course người khác.
- Nếu chưa chắc teacher rule, ưu tiên chỉ mở cho `ADMIN`, `SUPER_ADMIN` để scope gọn, và ghi TODO cho teacher.

## Business rule tối thiểu
- Không publish quiz nếu quiz chưa có câu hỏi.
- Có thể chưa validate sâu theo từng `QuestionType` trong task này.
- Nếu xóa question thì cần xử lý answers liên quan:
  - dùng cascade/orphanRemoval có kiểm soát, hoặc
  - delete answers trước trong service.
- Không xóa quiz nếu đã có attempt, hoặc để task sau xử lý nếu chưa có attempt data thực tế.
- Soft delete chưa cần trong task này; có thể dùng `HIDDEN` cho hide.

## File cần tạo hoặc chỉnh sửa

### Backend
- `backend/src/main/java/com/japaneselearning/module_quiz/controller/QuizAdminController.java`
- `backend/src/main/java/com/japaneselearning/module_quiz/dto/*`
- `backend/src/main/java/com/japaneselearning/module_quiz/service/QuizAdminService.java`
- `backend/src/main/java/com/japaneselearning/module_quiz/service/QuizAdminServiceImpl.java`
- `backend/src/main/java/com/japaneselearning/module_quiz/repository/*`
- `backend/src/main/java/com/japaneselearning/common/config/SecurityConfig.java`
- `backend/src/main/java/com/japaneselearning/common/exception/ErrorCode.java`

### Tests nếu phù hợp
- Repository/service/controller tests cho admin quiz CRUD nếu project pattern hiện có thuận tiện.

## Checklist
- [ ] Admin list quizzes được.
- [ ] Admin create quiz được.
- [ ] Admin get quiz detail được.
- [ ] Admin update quiz được.
- [ ] Admin delete quiz được nếu chưa có attempt hoặc theo rule đã chọn.
- [ ] Admin publish/hide quiz được.
- [ ] Không publish quiz rỗng.
- [ ] Admin create/list/update/delete question được.
- [ ] Admin create/list/update/delete answer được.
- [ ] DTO validation rõ ràng.
- [ ] Error code quiz/question/answer phù hợp.
- [ ] Permission `/api/v1/admin/quizzes/**`, `/api/v1/admin/questions/**`, `/api/v1/admin/answers/**` được cấu hình.
- [ ] Không làm student submit/result trong task này.
- [ ] Backend package/test pass hoặc blocker được ghi rõ.

## Cách test sau khi hoàn thành
1. Login admin lấy access token.
2. Tạo quiz gắn với course hoặc lesson.
3. Lấy danh sách quiz.
4. Lấy chi tiết quiz.
5. Cập nhật quiz.
6. Thử publish quiz chưa có câu hỏi, kỳ vọng bị chặn.
7. Tạo question cho quiz.
8. Tạo answer cho question.
9. Publish quiz sau khi có question.
10. Hide quiz.
11. Delete answer/question/quiz theo rule.
12. Test student token không gọi được admin quiz APIs.
13. Chạy backend package/test phù hợp:

```bash
cd backend
mvn clean verify
```

## Kết quả mong muốn
Backend có bộ API admin đủ để tạo và quản lý dữ liệu quiz cơ bản. Đây là nền tảng để task tiếp theo làm student quiz start/submit/result hoặc frontend admin quiz UI.
