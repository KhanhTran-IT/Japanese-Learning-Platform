# CURRENT TASK

## Task hiện tại
Backend Lesson Resource API Foundation

## Trạng thái
TODO

## Mục tiêu
Xây dựng API nền tảng cho tài liệu đính kèm bài học (`lesson_resources`) để admin/teacher có thể quản lý resource của lesson và student có thể xem danh sách resource khi có quyền học lesson.

## Vì sao làm task này?
Project đã có entity `LessonResource`, enum `ResourceType` và `LessonResourceRepository`, nhưng chưa có controller/service/DTO rõ ràng cho lesson resources. Sau khi admin đã quản lý được course, section và lesson, bước tiếp theo hợp lý là thêm API quản lý tài liệu đính kèm cho lesson trước khi làm frontend hiển thị/download resource.

## Không làm trong task này
- Không làm upload file thật.
- Không tích hợp cloud storage/S3/local multipart upload.
- Không làm frontend resource UI.
- Không làm quiz.
- Không làm payment.
- Không làm reorder kéo thả.
- Không thay đổi kiến trúc lesson progress.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/25_SCREEN_LIST.md`
- `docs/26_API_PRIORITY.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`
- `docs/05_features/05_02_COURSE_FEATURES.md`
- `docs/07_database/07_02_COURSE_LESSON.md`
- `docs/08_api/08_04_LESSON_API.md`

## Entity hiện có
`LessonResource` đang có các field chính:

```text
id
lesson
title
resourceType
fileUrl
fileSize
sortOrder
createdAt
```

`ResourceType` hiện có:

```text
PDF
DOCUMENT
AUDIO
VIDEO
EXTERNAL_LINK
```

## API cần làm

### Admin/Teacher - tạo resource cho lesson
```http
POST /api/v1/admin/lessons/{lessonId}/resources
Authorization: Bearer <accessToken>
```

Request body:
```json
{
  "title": "Tài liệu luyện đọc",
  "resourceType": "PDF",
  "fileUrl": "https://example.com/n5-reading.pdf",
  "fileSize": 1024000,
  "sortOrder": 1
}
```

### Admin/Teacher - lấy resources của lesson
```http
GET /api/v1/admin/lessons/{lessonId}/resources
Authorization: Bearer <accessToken>
```

### Admin/Teacher - cập nhật resource
```http
PUT /api/v1/admin/lesson-resources/{id}
Authorization: Bearer <accessToken>
```

### Admin/Teacher - xóa resource
```http
DELETE /api/v1/admin/lesson-resources/{id}
Authorization: Bearer <accessToken>
```

### Student - lấy resources khi học lesson
```http
GET /api/v1/lessons/{lessonId}/resources
Authorization: Bearer <accessToken>
```

## Logic xử lý
- Tạo DTO:
  - `LessonResourceCreateReq`
  - `LessonResourceUpdateReq`
  - `LessonResourceRes`
- Validation DTO:
  - `title` bắt buộc, tối đa 255 ký tự.
  - `resourceType` bắt buộc.
  - `fileUrl` bắt buộc, tối đa 1000 ký tự.
  - `fileSize >= 0` nếu có gửi.
  - `sortOrder >= 0` nếu có gửi.
- Tạo service admin:
  - create resource theo `lessonId`.
  - list resources theo lesson, order by `sortOrder`.
  - update resource theo id.
  - delete resource theo id.
- Admin/Teacher permission:
  - `ADMIN`/`SUPER_ADMIN` được quản lý mọi resource.
  - `TEACHER` chỉ được quản lý lesson resource nếu là teacher sở hữu course chứa lesson đó.
  - Có thể reuse logic data isolation tương tự `LessonAdminServiceImpl`.
- Tạo student read API:
  - student chỉ xem resources nếu có quyền học lesson.
  - Reuse logic tương tự `LearningServiceImpl`: lesson thuộc course `PUBLISHED`; nếu lesson không preview thì student phải enroll.
  - Chỉ trả list resource, không update progress.
- Response dùng `ApiResponse.success(...)` theo pattern hiện có.
- Không đưa business logic vào controller.

## Cần tạo hoặc chỉnh sửa
- `backend/src/main/java/com/japaneselearning/module_course/dto/LessonResourceCreateReq.java`
- `backend/src/main/java/com/japaneselearning/module_course/dto/LessonResourceUpdateReq.java`
- `backend/src/main/java/com/japaneselearning/module_course/dto/LessonResourceRes.java`
- `backend/src/main/java/com/japaneselearning/module_course/service/LessonResourceAdminService.java`
- `backend/src/main/java/com/japaneselearning/module_course/service/LessonResourceAdminServiceImpl.java`
- `backend/src/main/java/com/japaneselearning/module_course/controller/LessonResourceAdminController.java`
- `backend/src/main/java/com/japaneselearning/module_learning/service/LearningService.java`
- `backend/src/main/java/com/japaneselearning/module_learning/service/LearningServiceImpl.java`
- `backend/src/main/java/com/japaneselearning/module_learning/controller/LearningController.java`
- Có thể chỉnh `LessonResourceRepository` nếu cần query helper.

## Error code cần dùng
- `LESSON_NOT_FOUND`
- `RESOURCE_NOT_FOUND`
- `DATA_ISOLATION_FORBIDDEN`
- `FORBIDDEN_ACCESS`
- `USER_NOT_FOUND`

Nếu thiếu error code chuyên biệt và project đã có pattern thêm `ErrorCode`, có thể bổ sung thật gọn.

## Checklist
- [ ] Có DTO create/update/res cho lesson resource.
- [ ] Admin create resource hoạt động.
- [ ] Admin list resources theo lesson hoạt động.
- [ ] Admin update resource hoạt động.
- [ ] Admin delete resource hoạt động.
- [ ] Teacher chỉ quản lý resource của course mình sở hữu.
- [ ] Student list resources kiểm tra quyền học lesson.
- [ ] Resource được trả theo `sortOrder` tăng dần.
- [ ] Controller không chứa business logic.
- [ ] Chạy backend test phù hợp.

## Cách test sau khi hoàn thành
1. Đăng nhập ADMIN.
2. Tạo resource cho một lesson bằng `POST /api/v1/admin/lessons/{lessonId}/resources`.
3. Gọi admin list resource, kỳ vọng thấy resource vừa tạo.
4. Update title/resourceType/fileUrl/sortOrder, kỳ vọng dữ liệu đổi đúng.
5. Delete resource, kỳ vọng list không còn resource đó.
6. Đăng nhập TEACHER không sở hữu course, thử quản lý resource, kỳ vọng bị chặn.
7. Đăng nhập STUDENT đã enroll course, gọi `GET /api/v1/lessons/{lessonId}/resources`, kỳ vọng xem được.
8. Student chưa enroll gọi lesson non-preview, kỳ vọng bị chặn.
9. Chạy backend test phù hợp.

## Kết quả mong muốn
Backend có API lesson resource foundation rõ ràng, có kiểm soát quyền admin/teacher/student và sẵn sàng cho task frontend hiển thị hoặc quản lý tài liệu đính kèm ở bước tiếp theo.
