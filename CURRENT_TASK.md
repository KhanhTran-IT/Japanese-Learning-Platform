# CURRENT TASK

## Task hiện tại
Frontend Admin Quiz Management UI Foundation

## Trạng thái
TODO

## Mục tiêu
Xây dựng giao diện quản trị nền tảng để admin/teacher có thể tạo và quản lý quiz, question, answer ngay trên frontend. Task này giúp dữ liệu quiz không còn phải tạo bằng API thủ công, đồng thời hoàn thiện vòng quiz: admin tạo nội dung, student học lesson, student làm quiz và xem kết quả.

## Vì sao làm task này?
Backend admin quiz APIs đã có, student quiz taking UI đã có, lesson quiz discovery cũng đã có. Điểm nghẽn còn lại là admin/teacher chưa có UI để tạo quiz thật. Nếu không làm phần này, việc demo hoặc vận hành quiz vẫn phụ thuộc Swagger/Postman, chưa đủ thân thiện cho người quản trị nội dung.

## Không làm trong task này
- Không redesign toàn bộ admin dashboard.
- Không làm question builder kéo thả phức tạp.
- Không làm import Excel/CSV.
- Không làm upload media phức tạp nếu backend chưa hỗ trợ file flow.
- Không làm scoring nâng cao cho matching/reorder.
- Không đổi schema quiz nếu không bắt buộc.
- Không làm analytics quiz chuyên sâu.

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

Kiểm tra lại `QuizAdminController` trước khi code frontend, nhưng nhóm API chính hiện có:

```http
GET    /api/admin/quizzes
POST   /api/admin/quizzes
GET    /api/admin/quizzes/{id}
PUT    /api/admin/quizzes/{id}
DELETE /api/admin/quizzes/{id}
POST   /api/admin/quizzes/{id}/publish
POST   /api/admin/quizzes/{id}/hide
POST   /api/admin/quizzes/{quizId}/questions
PUT    /api/admin/questions/{id}
DELETE /api/admin/questions/{id}
POST   /api/admin/questions/{questionId}/answers
PUT    /api/admin/answers/{id}
DELETE /api/admin/answers/{id}
```

## Frontend cần triển khai

### Service
- Cập nhật `frontend/src/services/admin.service.js` hoặc tạo `quiz-admin.service.js` nếu codebase đang tách service theo domain.
- Hàm đề xuất:
  - `getQuizzes(params)`
  - `getQuiz(id)`
  - `createQuiz(payload)`
  - `updateQuiz(id, payload)`
  - `deleteQuiz(id)`
  - `publishQuiz(id)`
  - `hideQuiz(id)`
  - `createQuestion(quizId, payload)`
  - `updateQuestion(id, payload)`
  - `deleteQuestion(id)`
  - `createAnswer(questionId, payload)`
  - `updateAnswer(id, payload)`
  - `deleteAnswer(id)`

### Router
- Thêm route admin/teacher phù hợp:
  - `/admin/quizzes`
  - `/admin/quizzes/:id`
- Route phải dùng guard admin/teacher theo pattern hiện có.

### Pages/components
- Tạo hoặc cập nhật trang danh sách quiz:
  - Hiển thị title, course/lesson, status, question count nếu có, created/updated nếu API trả.
  - Filter tối thiểu theo course/lesson/status nếu API hỗ trợ.
  - Button tạo quiz mới.
  - Hành động publish/hide/delete.
- Tạo trang chi tiết hoặc builder đơn giản:
  - Form sửa quiz metadata.
  - Danh sách questions.
  - Thêm/sửa/xóa question.
  - Thêm/sửa/xóa answer trong từng question.
  - Với `SINGLE_CHOICE` và `TRUE_FALSE`, cho chọn đáp án đúng.
  - Với type khác, hiển thị field cơ bản và ghi rõ UI hỗ trợ tối thiểu.

## UX yêu cầu
- Admin/teacher có thể tạo quiz draft trước rồi publish sau.
- Không cho publish khi backend báo quiz chưa đủ câu hỏi; hiển thị lỗi thân thiện.
- Khi question/answer đã có attempt và backend chặn sửa/xóa, UI hiển thị thông báo rõ.
- Trạng thái `DRAFT`/`PUBLISHED` phải dễ nhận biết.
- Không dùng modal quá lớn nếu form question/answer dài; ưu tiên layout builder rõ ràng.
- Không hardcode course/lesson id. Nếu cần chọn course/lesson, dùng API hiện có hoặc ghi blocker nếu chưa có endpoint phù hợp.

## Checklist
- [ ] Admin quiz service gọi đúng API.
- [ ] Route danh sách quiz hoạt động.
- [ ] Route chi tiết/builder quiz hoạt động.
- [ ] Tạo/sửa quiz metadata được.
- [ ] Thêm/sửa/xóa question được.
- [ ] Thêm/sửa/xóa answer được.
- [ ] Publish/hide quiz được.
- [ ] UI hiển thị lỗi backend thân thiện.
- [ ] Teacher data isolation không bị bypass ở frontend.
- [ ] Không hardcode course/lesson id.
- [ ] Frontend build/test pass hoặc blocker được ghi rõ.

## Cách test sau khi hoàn thành
1. Login admin hoặc teacher.
2. Mở `/admin/quizzes`.
3. Tạo quiz draft gắn với course/lesson hợp lệ.
4. Thêm question và answers.
5. Publish quiz.
6. Login student đã enroll course.
7. Mở lesson có quiz và kiểm tra CTA làm quiz xuất hiện.
8. Làm quiz và xem result.
9. Quay lại admin, thử sửa/xóa question đã có attempt để kiểm tra backend error được UI hiển thị rõ.
