# CURRENT TASK

## Task hiện tại
Frontend Admin Course Structure Management UI & API Integration

## Trạng thái
TODO

## Mục tiêu
Xây dựng màn hình quản lý cấu trúc khóa học tại route `/admin/courses/:id/structure`, cho phép Admin/Teacher xem danh sách chương học, xem bài học trong từng chương, tạo/sửa/xóa chương học và tạo/sửa/xóa bài học cơ bản bằng các API backend Section/Lesson Admin hiện có.

## Vì sao làm task này?
Sau khi admin đã có màn quản lý khóa học và form tạo/sửa khóa học, bước tiếp theo trong MVP là quản lý nội dung bên trong khóa học. Course chỉ là khung; Section và Lesson mới là phần học thật để học viên xem. Đây là bước bắt buộc trước khi public course hoàn chỉnh và trước khi cải thiện flow học bài.

## Không làm trong task này
- Không upload file/video/audio/pdf thật.
- Không làm quản lý `LessonResource`.
- Không làm rich text editor nâng cao.
- Không làm kéo thả sắp xếp bằng drag/drop.
- Không làm quiz, flashcard hoặc payment.
- Không làm public learning page.
- Không thay đổi backend API nếu API hiện tại đã đủ.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/24_USER_FLOWS.md`
- `docs/25_SCREEN_LIST.md`
- `docs/26_API_PRIORITY.md`
- `docs/28_ENUM_DEFINITIONS.md`
- `docs/30_PERMISSION_MATRIX.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/05_features/05_02_COURSE_FEATURES.md`
- `docs/07_database/07_02_COURSE_LESSON.md`
- `docs/08_api/08_04_LESSON_API.md`
- `docs/08_api/08_10_ADMIN_API.md`
- `docs/10_FRONTEND_STRUCTURE.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`

## API cần làm
Frontend gọi các API backend hiện có:

```http
GET    /api/v1/admin/courses/{courseId}
GET    /api/v1/admin/courses/{courseId}/sections
POST   /api/v1/admin/courses/{courseId}/sections
PUT    /api/v1/admin/sections/{id}
DELETE /api/v1/admin/sections/{id}

GET    /api/v1/admin/sections/{sectionId}/lessons
POST   /api/v1/admin/sections/{sectionId}/lessons
GET    /api/v1/admin/lessons/{id}
PUT    /api/v1/admin/lessons/{id}
DELETE /api/v1/admin/lessons/{id}
```

## Request mẫu
Create section:

```json
{
  "title": "Chương 1: Làm quen tiếng Nhật",
  "description": "Các bài học nhập môn",
  "sortOrder": 1
}
```

Update section:

```json
{
  "title": "Chương 1: Làm quen tiếng Nhật",
  "description": "Các bài học nhập môn",
  "sortOrder": 1,
  "status": "DRAFT"
}
```

Create lesson:

```json
{
  "title": "Bài 1: Tiếng Nhật là gì?",
  "slug": "bai-1-tieng-nhat-la-gi",
  "content": "Nội dung bài học dạng text...",
  "videoUrl": "https://example.com/video.mp4",
  "isPreview": true,
  "sortOrder": 1,
  "durationMinutes": 15
}
```

Update lesson:

```json
{
  "title": "Bài 1: Tiếng Nhật là gì?",
  "slug": "bai-1-tieng-nhat-la-gi",
  "content": "Nội dung bài học dạng text...",
  "videoUrl": "https://example.com/video.mp4",
  "isPreview": true,
  "sortOrder": 1,
  "status": "DRAFT",
  "durationMinutes": 15
}
```

## Response mong muốn
Section response:

```json
{
  "code": 1000,
  "message": "Lấy danh sách chương học thành công",
  "result": [
    {
      "id": 1,
      "courseId": 10,
      "title": "Chương 1",
      "description": "Mô tả chương",
      "sortOrder": 1,
      "status": "DRAFT"
    }
  ]
}
```

Lesson response:

```json
{
  "code": 1000,
  "message": "Lấy danh sách bài học thành công",
  "result": [
    {
      "id": 1,
      "sectionId": 1,
      "title": "Bài 1",
      "slug": "bai-1",
      "content": "Nội dung bài học",
      "videoUrl": "https://example.com/video.mp4",
      "isPreview": true,
      "sortOrder": 1,
      "status": "DRAFT",
      "durationMinutes": 15
    }
  ]
}
```

## Logic xử lý
- Thêm route `/admin/courses/:id/structure` dưới `AdminLayout`.
- Thêm action "Cấu trúc" hoặc "Bài học" trong `AdminCourseManagementPage.vue` để đi tới route structure của khóa học.
- Mở rộng `AdminService` với các hàm section/lesson admin.
- Tạo `AdminCourseStructurePage.vue`.
- Khi page mounted:
  - lấy course detail để hiển thị tên khóa học.
  - lấy danh sách sections theo `courseId`.
  - với mỗi section, lấy lessons theo `sectionId` hoặc lazy load lessons khi mở section.
- UI cần có:
  - header course title + nút quay lại danh sách khóa học.
  - danh sách section dạng accordion/list.
  - mỗi section hiển thị title, sortOrder, status, số bài học và action sửa/xóa.
  - mỗi lesson hiển thị title, sortOrder, duration, preview badge, status và action sửa/xóa.
  - button tạo section.
  - button tạo lesson trong từng section.
  - loading/error/empty state.
- Tạo form modal section:
  - title bắt buộc.
  - description.
  - sortOrder không âm.
  - status chỉ trong update.
- Tạo form modal lesson:
  - title bắt buộc.
  - slug tùy chọn.
  - content.
  - videoUrl dạng text URL, chưa upload file thật.
  - isPreview boolean.
  - sortOrder không âm.
  - durationMinutes không âm.
  - status chỉ trong update.
- Khi xóa section/lesson phải confirm trước.
- Nếu xóa section đang có lesson và backend trả lỗi, hiển thị message rõ ràng.
- Không dùng alert cho lỗi form; dùng inline message.
- Không làm drag/drop, chỉ nhập `sortOrder` thủ công.

## Cần tạo hoặc chỉnh sửa
- `frontend/src/pages/admin/AdminCourseStructurePage.vue`
- `frontend/src/pages/admin/AdminCourseManagementPage.vue`
- `frontend/src/services/admin.service.js`
- `frontend/src/router/index.js`
- Có thể tạo:
  - `frontend/src/components/admin/SectionFormModal.vue`
  - `frontend/src/components/admin/LessonFormModal.vue`

## Error code cần dùng
Không tạo error code frontend riêng. Frontend cần xử lý:
- 400: validation lỗi hoặc không thể xóa section đang có lesson.
- 401: chưa đăng nhập/token hết hạn.
- 403: không có quyền admin/teacher hoặc teacher không sở hữu course.
- 404: course/section/lesson không tồn tại.
- 409: slug lesson trùng nếu backend trả lỗi.

## Checklist
- [ ] Thêm route `/admin/courses/:id/structure`.
- [ ] Thêm action đi tới cấu trúc khóa học từ bảng course.
- [ ] Thêm section/lesson methods vào `admin.service.js`.
- [ ] Tạo `AdminCourseStructurePage.vue`.
- [ ] Load course detail và sections.
- [ ] Load lessons theo từng section.
- [ ] Tạo form create/update section.
- [ ] Tạo form create/update lesson.
- [ ] Validate frontend cơ bản cho section/lesson.
- [ ] Implement delete section có confirm.
- [ ] Implement delete lesson có confirm.
- [ ] Xử lý loading/error/empty state.
- [ ] Chạy `npm run build`.

## Cách test sau khi hoàn thành
1. Chạy backend Spring Boot.
2. Chạy frontend Vue.
3. Đăng nhập bằng admin.
4. Mở `/admin/courses`, bấm action "Cấu trúc" của một course.
5. Kỳ vọng vào được `/admin/courses/{id}/structure` và thấy tên khóa học.
6. Tạo một section hợp lệ, kỳ vọng section xuất hiện trong danh sách.
7. Sửa section, kỳ vọng dữ liệu cập nhật.
8. Tạo lesson trong section, kỳ vọng lesson xuất hiện dưới section đó.
9. Sửa lesson, kỳ vọng dữ liệu cập nhật.
10. Xóa lesson, kỳ vọng lesson biến khỏi danh sách.
11. Thử xóa section có lesson, kỳ vọng frontend hiển thị lỗi backend rõ ràng nếu backend chặn.
12. Chạy `npm run build`.

## Kết quả mong muốn
Admin/Teacher có thể quản lý cấu trúc khóa học cơ bản gồm chương học và bài học text/video URL, tạo nền tảng để public course detail và learning page hiển thị nội dung học thật.
