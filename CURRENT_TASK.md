# CURRENT TASK

## Task hiện tại
Admin Course Structure Section/Lesson Contract & UX Hardening

## Trạng thái
TODO

## Mục tiêu
Kiểm tra và hoàn thiện trang quản lý cấu trúc khóa học để `AdminCourseStructurePage.vue`, `SectionFormModal.vue` và `LessonFormModal.vue` khớp backend contract, xử lý validation rõ ràng, reload dữ liệu đúng phạm vi và có UX ổn định khi admin tạo/sửa/xóa chương học hoặc bài học.

## Vì sao làm task này?
Sau khi form tạo/sửa khóa học đã được harden, bước tự nhiên tiếp theo là làm chắc phần cấu trúc bên trong khóa học. Đây là luồng P0 trong admin: course có thể tạo được, nhưng để xuất bản/học được thì cần quản lý section và lesson ổn định.

## Không làm trong task này
- Không sửa backend nếu frontend đã có thể khớp contract hiện tại.
- Không làm lesson resources.
- Không làm quiz trong lesson.
- Không làm upload video/audio/file; chỉ giữ URL text hiện có.
- Không làm drag-and-drop reorder.
- Không redesign toàn bộ trang admin course structure.
- Không làm public course detail hoặc student learning page.

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

## Backend contract cần đối chiếu

### Section create
```http
POST /api/v1/admin/courses/{courseId}/sections
```

Payload theo `SectionCreateReq`:
```json
{
  "title": "Tài liệu luyện đọc",
  "resourceType": "PDF",
  "fileUrl": "https://example.com/n5-reading.pdf",
  "fileSize": 1024000,
  "sortOrder": 1
}
```

### Section update
```http
PUT /api/v1/admin/sections/{id}
```

Payload theo `SectionUpdateReq`, giống create nhưng có thêm:
```json
{
  "status": "DRAFT"
}
```

### Lesson create
```http
POST /api/v1/admin/sections/{sectionId}/lessons
```

Payload theo `LessonCreateReq`:
```json
{
  "title": "Bài 1: Hiragana",
  "slug": "bai-1-hiragana",
  "content": "Nội dung bài học",
  "videoUrl": "https://example.com/video.mp4",
  "isPreview": false,
  "sortOrder": 1,
  "durationMinutes": 15
}
```

### Lesson update
```http
PUT /api/v1/admin/lessons/{id}
```

Payload theo `LessonUpdateReq`, giống create nhưng có thêm:
```json
{
  "status": "DRAFT"
}
```

## Logic cần kiểm tra/hoàn thiện
- `AdminCourseStructurePage.vue`:
  - load course detail để hiển thị title.
  - load danh sách section theo course.
  - lazy load lessons khi mở section.
  - sau khi save section, reload section list hợp lý.
  - sau khi save lesson, reload lessons của đúng section.
  - xóa section/lesson có confirm và error handling rõ.
  - không để action error cũ gây nhiễu sau thao tác mới.
- `SectionFormModal.vue`:
  - create mode không gửi `status`.
  - update mode có gửi `status`.
  - validate `title` bắt buộc, tối đa 255 ký tự.
  - validate `sortOrder >= 0`.
  - có loading state và API error trong modal.
- `LessonFormModal.vue`:
  - create mode không gửi `status`.
  - update mode có gửi `status`.
  - validate `title` bắt buộc, tối đa 255 ký tự.
  - validate `slug` tối đa 255 ký tự nếu có nhập.
  - validate `durationMinutes >= 0`.
  - validate `sortOrder >= 0`.
  - đảm bảo `isPreview` luôn gửi boolean.
  - có loading state và API error trong modal.
- `AdminService`:
  - xác nhận các endpoint section/lesson đúng `/v1/admin/...` vì Axios base URL đã là `/api`.

## Cần tạo hoặc chỉnh sửa
- `frontend/src/pages/admin/AdminCourseStructurePage.vue`
- `frontend/src/components/admin/SectionFormModal.vue`
- `frontend/src/components/admin/LessonFormModal.vue`
- Có thể chỉnh `frontend/src/services/admin.service.js` nếu phát hiện endpoint hoặc method chưa đúng.

## Checklist
- [ ] Section create gọi đúng API và payload.
- [ ] Section update gọi đúng API và payload.
- [ ] Lesson create gọi đúng API và payload.
- [ ] Lesson update gọi đúng API và payload.
- [ ] Create section/lesson không gửi `status`.
- [ ] Update section/lesson có gửi `status`.
- [ ] Validate title max 255 cho section và lesson.
- [ ] Validate lesson slug max 255 nếu có nhập.
- [ ] Validate sortOrder và durationMinutes không âm.
- [ ] Save section reload danh sách section hợp lý.
- [ ] Save lesson reload đúng lessons của section đang thao tác.
- [ ] Delete section/lesson có confirm và xử lý lỗi rõ.
- [ ] Chạy `npm run build`.
- [ ] Chạy `npm test`.

## Cách test sau khi hoàn thành
1. Đăng nhập bằng ADMIN.
2. Vào `/admin/courses`.
3. Bấm "Cấu trúc" ở một course.
4. Tạo section thiếu title, kỳ vọng hiện validation.
5. Tạo section hợp lệ, kỳ vọng section xuất hiện trong danh sách.
6. Sửa section, đổi title/status/sortOrder, kỳ vọng reload đúng.
7. Mở một section, bấm "+ Bài học".
8. Tạo lesson thiếu title, kỳ vọng hiện validation.
9. Tạo lesson hợp lệ, kỳ vọng lesson xuất hiện trong đúng section.
10. Sửa lesson, đổi title/status/isPreview/duration/sortOrder, kỳ vọng reload đúng.
11. Xóa lesson và section, kiểm tra confirm/error state.
12. Chạy `npm run build`.
13. Chạy `npm test`.

## Kết quả mong muốn
Trang admin course structure hoạt động ổn định cho các thao tác CRUD section/lesson cơ bản, khớp backend DTO và sẵn sàng làm nền cho các task nâng cao như resources, quiz hoặc reorder sau này.
