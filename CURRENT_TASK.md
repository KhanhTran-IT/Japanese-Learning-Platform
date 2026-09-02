# CURRENT TASK

## Task hiện tại
Backend Quiz Data Model Foundation

## Trạng thái
TODO

## Mục tiêu
Xây dựng nền tảng dữ liệu backend cho module quiz P1: tạo entity, enum, repository và Flyway migration cho quiz, question, answer, quiz attempt và attempt answer. Task này chỉ dựng data model/foundation, chưa làm API làm bài/chấm điểm đầy đủ.

## Vì sao làm task này?
P0 của MVP đã được hoàn thiện và harden qua các luồng auth, course, lesson, progress, profile, frontend UX và config security. Theo `docs/26_API_PRIORITY.md`, sau P0 thì P1 hợp lý nhất là quiz cơ bản. Trước khi làm API quiz, cần có data model chắc để tránh vừa code API vừa sửa schema liên tục.

## Không làm trong task này
- Không làm frontend quiz UI.
- Không làm admin quiz UI.
- Không làm API start/submit/result đầy đủ.
- Không làm logic chấm điểm phức tạp.
- Không làm random question.
- Không làm timer enforcement.
- Không làm certificate.
- Không làm payment/order.
- Không thay đổi các flow P0 đã ổn định.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/26_API_PRIORITY.md`
- `docs/27_DATABASE_PHASES.md`
- `docs/28_ENUM_DEFINITIONS.md`
- `docs/29_ERROR_CODE_STANDARD.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`
- `docs/05_features/05_04_QUIZ_FEATURES.md`
- `docs/07_database/07_04_QUIZ.md`
- `docs/08_api/08_05_QUIZ_API.md`
- `docs/10_FRONTEND_STRUCTURE.md`
- `docs/11_BACKEND_FRONTEND_CONFIG.md`

## Database model cần bám theo

### Bảng `quizzes`
- `id`
- `course_id`
- `lesson_id`
- `title`
- `description`
- `time_limit_minutes`
- `passing_score`
- `max_attempts`
- `status`
- `created_at`
- `updated_at`

### Bảng `questions`
- `id`
- `quiz_id`
- `question_type`
- `content`
- `audio_url`
- `image_url`
- `explanation`
- `points`
- `sort_order`
- `created_at`
- `updated_at`

### Bảng `answers`
- `id`
- `question_id`
- `content`
- `is_correct`
- `sort_order`
- `created_at`

### Bảng `quiz_attempts`
- `id`
- `user_id`
- `quiz_id`
- `started_at`
- `submitted_at`
- `score`
- `total_questions`
- `correct_count`
- `wrong_count`
- `passed`
- `status`

### Bảng `quiz_attempt_answers`
- `id`
- `attempt_id`
- `question_id`
- `answer_id`
- `user_answer_text`
- `is_correct`
- `points_earned`
- `created_at`

## Enum đề xuất
Tạo enum trong package quiz, ví dụ `module_quiz/enums`:
- `QuizStatus`
  - `DRAFT`
  - `PUBLISHED`
  - `HIDDEN`
- `QuestionType`
  - `SINGLE_CHOICE`
  - `MULTIPLE_CHOICE`
  - `TRUE_FALSE`
  - `FILL_BLANK`
  - `MATCHING`
  - `LISTENING`
  - `REORDER`
- `QuizAttemptStatus`
  - `IN_PROGRESS`
  - `SUBMITTED`
  - `EXPIRED`
  - `CANCELLED`

Nếu `docs/28_ENUM_DEFINITIONS.md` đã có naming khác, ưu tiên tài liệu enum hiện có.

## Hướng triển khai đề xuất

### 1. Tạo module package
Tạo package backend:

```text
backend/src/main/java/com/japaneselearning/module_quiz/
```

Gợi ý cấu trúc:
- `entity`
- `enums`
- `repository`

Chưa cần tạo controller/service nếu task chỉ là data model foundation.

### 2. Tạo entity JPA
Tạo các entity:
- `Quiz`
- `Question`
- `Answer`
- `QuizAttempt`
- `QuizAttemptAnswer`

Yêu cầu:
- Dùng `@Entity`, `@Table`.
- Dùng `@ManyToOne(fetch = FetchType.LAZY)` cho quan hệ tới `Course`, `Lesson`, `User`, `Quiz`, `Question`, `Answer`.
- Dùng `@Enumerated(EnumType.STRING)` cho enum.
- Dùng `BigDecimal` cho score/points/passingScore.
- Dùng `LocalDateTime` cho timestamp.
- Dùng `@CreationTimestamp` và `@UpdateTimestamp` theo pattern hiện có.
- Tránh cascade nguy hiểm từ child về parent.

### 3. Tạo repository
Tạo repository:
- `QuizRepository`
- `QuestionRepository`
- `AnswerRepository`
- `QuizAttemptRepository`
- `QuizAttemptAnswerRepository`

Method nền tảng nên có:
- find quizzes theo course/lesson/status.
- find questions theo quiz order by sort order.
- find answers theo question order by sort order.
- find attempts theo user/quiz.
- find attempt answers theo attempt.

### 4. Tạo Flyway migration
Vì project đã dùng Flyway, thêm migration mới:

```text
backend/src/main/resources/db/migration/V2__create_quiz_tables.sql
```

Yêu cầu migration:
- Tạo đúng 5 bảng quiz.
- Khớp entity JPA để `ddl-auto=validate` pass.
- Tạo foreign key tới:
  - `courses(id)`
  - `lessons(id)`
  - `users(id)`
  - `quizzes(id)`
  - `questions(id)`
  - `answers(id)`
- Thêm indexes cho query thường dùng:
  - `idx_quizzes_course_status`
  - `idx_quizzes_lesson_status`
  - `idx_questions_quiz_sort`
  - `idx_answers_question_sort`
  - `idx_quiz_attempts_user_quiz`
  - `idx_attempt_answers_attempt`

### 5. Error code nếu cần
Chỉ thêm error code nếu cần cho foundation, ví dụ:
- `QUIZ_NOT_FOUND`
- `QUESTION_NOT_FOUND`
- `ANSWER_NOT_FOUND`
- `QUIZ_ATTEMPT_NOT_FOUND`

Nếu chưa có service/API sử dụng, có thể để task API sau thêm error code.

## Checklist
- [ ] Tạo package `module_quiz`.
- [ ] Tạo enum `QuizStatus`.
- [ ] Tạo enum `QuestionType`.
- [ ] Tạo enum `QuizAttemptStatus`.
- [ ] Tạo entity `Quiz`.
- [ ] Tạo entity `Question`.
- [ ] Tạo entity `Answer`.
- [ ] Tạo entity `QuizAttempt`.
- [ ] Tạo entity `QuizAttemptAnswer`.
- [ ] Tạo repository cho 5 entity.
- [ ] Tạo Flyway migration `V2__create_quiz_tables.sql`.
- [ ] Migration khớp entity để Hibernate validate pass.
- [ ] Không làm API quiz ngoài phạm vi foundation.
- [ ] Không làm frontend quiz UI.
- [ ] Chạy backend package/test phù hợp.
- [ ] Nếu test bị blocker môi trường, ghi rõ.

## Cách test sau khi hoàn thành
1. Chạy backend với database sạch hoặc database local có Flyway.
2. Kiểm tra Flyway chạy `V2__create_quiz_tables.sql`.
3. Kiểm tra 5 bảng quiz được tạo.
4. Chạy backend package:

```bash
cd backend
mvn clean package
```

5. Chạy backend tests phù hợp:

```bash
cd backend
mvn test
```

6. Nếu có integration test, chạy verify:

```bash
cd backend
mvn clean verify
```

## Kết quả mong muốn
Backend có nền tảng data model quiz sạch, có migration rõ ràng, repository sẵn sàng để task tiếp theo xây dựng API quản lý quiz hoặc API làm quiz cho student.
