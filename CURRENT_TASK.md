# CURRENT TASK

## Task hiện tại
Backend Student Quiz Taking API Foundation

## Trạng thái
TODO

## Mục tiêu
Xây dựng API backend nền tảng cho student làm quiz: xem quiz đã publish, bắt đầu attempt, submit đáp án, chấm điểm cơ bản và xem kết quả. Task này chỉ làm backend student quiz API, chưa làm frontend quiz UI.

## Vì sao làm task này?
Hai task trước đã tạo data model quiz và admin APIs để tạo/publish quiz. Bước tiếp theo hợp lý là student quiz taking API, vì đây là phần biến dữ liệu quiz thành trải nghiệm học thật. Sau task này frontend mới có contract rõ để xây màn hình làm quiz và kết quả.

## Không làm trong task này
- Không làm frontend student quiz UI.
- Không làm frontend admin quiz UI.
- Không làm random question.
- Không làm timer enforcement nghiêm ngặt.
- Không làm auto expire bằng scheduler.
- Không làm giải thích từng đáp án nâng cao nếu chưa cần.
- Không làm matching/reorder scoring phức tạp.
- Không làm payment/order.
- Không đổi schema nếu không bắt buộc.

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
- `docs/07_database/07_04_QUIZ.md`
- `docs/08_api/08_05_QUIZ_API.md`

## API cần triển khai

Base path đề xuất:

```http
/api/v1/quizzes
```

Endpoints:

```http
GET  /api/v1/quizzes/{id}
POST /api/v1/quizzes/{id}/start
POST /api/v1/quizzes/{id}/submit
GET  /api/v1/quizzes/{id}/result/{attemptId}
GET  /api/users/me/quiz-attempts
```

## DTO đề xuất

### Quiz detail cho student
- `QuizLearningRes`
- `QuestionLearningRes`
- `AnswerLearningRes`

Lưu ý:
- Không trả `isCorrect` cho student khi chưa submit.
- Có thể trả:
  - quiz id/title/description/timeLimitMinutes/passingScore/maxAttempts.
  - questions.
  - answers nhưng ẩn đáp án đúng.

### Start attempt
- `QuizAttemptStartRes`

Field đề xuất:
- `attemptId`
- `quizId`
- `startedAt`
- `status`
- `maxAttempts`
- `remainingAttempts` nếu tính được.

### Submit request
- `QuizSubmitReq`
- `QuizSubmitAnswerReq`

Format bám docs:

```json
{
  "attemptId": 100,
  "answers": [
    {
      "questionId": 1,
      "answerId": 3
    },
    {
      "questionId": 2,
      "userAnswerText": "日本語"
    }
  ]
}
```

### Result response
- `QuizResultRes`
- `QuizResultAnswerRes`

Field đề xuất:
- `attemptId`
- `quizId`
- `score`
- `totalQuestions`
- `correctCount`
- `wrongCount`
- `passed`
- `status`
- `startedAt`
- `submittedAt`
- answers detail.

Sau submit/result có thể trả correct answer và explanation nếu dữ liệu có.

## Service cần tạo
Tạo service:
- `QuizLearningService`
- `QuizLearningServiceImpl`

Service xử lý:
- Lấy quiz đã `PUBLISHED`.
- Kiểm tra course/lesson access:
  - Nếu quiz gắn với lesson/course non-preview thì student phải enroll.
  - Reuse hoặc bám logic access của `LearningServiceImpl` nếu phù hợp.
- Start attempt:
  - Kiểm tra `maxAttempts`.
  - Không tạo attempt nếu vượt số lần cho phép.
  - Có thể cho phép nhiều attempt nếu chưa vượt limit.
- Submit:
  - Kiểm tra attempt thuộc user hiện tại.
  - Kiểm tra attempt thuộc quiz.
  - Chỉ submit attempt `IN_PROGRESS`.
  - Tính điểm cơ bản cho `SINGLE_CHOICE`, `TRUE_FALSE`, có thể `MULTIPLE_CHOICE` nếu dữ liệu hiện tại hỗ trợ.
  - Với `FILL_BLANK`, `MATCHING`, `LISTENING`, `REORDER`, nếu chưa làm scoring sâu thì ghi rõ chỉ support basic/unsupported trong task này.
  - Lưu `QuizAttemptAnswer`.
  - Cập nhật `QuizAttempt`: score, totalQuestions, correctCount, wrongCount, passed, submittedAt, status.
- Result:
  - Chỉ user sở hữu attempt hoặc admin/super admin được xem.

## Scoring rule tối thiểu
Ưu tiên scope đơn giản:
- `SINGLE_CHOICE`: đúng nếu `answerId` trỏ tới answer có `isCorrect = true`.
- `TRUE_FALSE`: dùng giống single choice nếu true/false được lưu dưới dạng answers.
- `MULTIPLE_CHOICE`: có thể chưa support đầy đủ nếu schema submit chưa hỗ trợ nhiều `answerIds`; ghi TODO cho task sau.
- Text answer: có thể lưu `userAnswerText` nhưng chưa tự chấm nếu chưa có field correct text.

Nếu question type chưa support scoring, có thể tính sai/0 điểm và ghi rõ trong docs/API comment, hoặc reject bằng `INVALID_REQUEST` để tránh hiểu nhầm.

## Permission
- `GET /api/v1/quizzes/{id}` yêu cầu authenticated student nếu quiz không public.
- `POST start/submit` yêu cầu `STUDENT`.
- `GET /result` yêu cầu attempt owner hoặc admin/super admin.
- `GET /api/users/me/quiz-attempts` yêu cầu `STUDENT`.

## Error code có thể cần
- `QUIZ_NOT_FOUND`
- `QUIZ_NOT_PUBLISHED`
- `QUIZ_MAX_ATTEMPTS_REACHED`
- `QUIZ_ATTEMPT_NOT_FOUND`
- `QUIZ_ATTEMPT_ALREADY_SUBMITTED`
- `QUIZ_ATTEMPT_FORBIDDEN`
- `QUIZ_UNSUPPORTED_QUESTION_TYPE`

Ưu tiên không tạo quá nhiều nếu error code hiện có đã đủ, nhưng các lỗi attempt nên rõ.

## File cần tạo hoặc chỉnh sửa

### Backend
- `backend/src/main/java/com/japaneselearning/module_quiz/controller/QuizLearningController.java`
- `backend/src/main/java/com/japaneselearning/module_quiz/dto/*`
- `backend/src/main/java/com/japaneselearning/module_quiz/service/QuizLearningService.java`
- `backend/src/main/java/com/japaneselearning/module_quiz/service/QuizLearningServiceImpl.java`
- `backend/src/main/java/com/japaneselearning/module_quiz/repository/*`
- `backend/src/main/java/com/japaneselearning/module_learning/service/LearningServiceImpl.java` nếu cần reuse helper access.
- `backend/src/main/java/com/japaneselearning/common/config/SecurityConfig.java`
- `backend/src/main/java/com/japaneselearning/common/exception/ErrorCode.java`
- `backend/src/main/java/com/japaneselearning/module_user/controller/UserController.java` nếu thêm `/me/quiz-attempts`.

### Tests nếu phù hợp
- Test start attempt.
- Test max attempts.
- Test submit correct/incorrect single choice.
- Test cannot submit other user's attempt.
- Test cannot submit already submitted attempt.
- Test student chưa enroll không được start quiz của non-preview lesson/course.

## Checklist
- [ ] Student lấy quiz detail đã publish được.
- [ ] Quiz detail không lộ `isCorrect` trước submit.
- [ ] Student start attempt được.
- [ ] Max attempts được kiểm tra.
- [ ] Student submit attempt được.
- [ ] Submit chỉ cho attempt owner.
- [ ] Không submit lại attempt đã submitted.
- [ ] Chấm điểm cơ bản single choice/true false hoạt động.
- [ ] Attempt lưu score, counts, passed, submittedAt, status.
- [ ] Result trả đúng kết quả attempt.
- [ ] `/api/users/me/quiz-attempts` trả lịch sử làm quiz của user.
- [ ] Access rule enrollment không bị nới lỏng.
- [ ] Không làm frontend quiz UI trong task này.
- [ ] Backend package/test pass hoặc blocker được ghi rõ.

## Cách test sau khi hoàn thành
1. Dùng admin API tạo quiz, question, answers và publish quiz.
2. Login student đã enroll course.
3. Gọi `GET /api/v1/quizzes/{id}`, đảm bảo không lộ `isCorrect`.
4. Gọi `POST /api/v1/quizzes/{id}/start`, nhận `attemptId`.
5. Submit đáp án đúng, kiểm tra score/passed.
6. Submit đáp án sai, kiểm tra correct/wrong count.
7. Submit lại cùng attempt, kỳ vọng bị chặn.
8. Dùng student khác xem result attempt, kỳ vọng bị chặn.
9. Vượt max attempts, kỳ vọng bị chặn.
10. Gọi `/api/users/me/quiz-attempts`.
11. Chạy backend package/test:

```bash
cd backend
mvn clean verify
```

## Kết quả mong muốn
Backend có API student quiz taking cơ bản, đủ để frontend task sau xây màn hình làm quiz và xem kết quả mà không cần thay đổi lớn ở schema hoặc admin quiz data flow.
