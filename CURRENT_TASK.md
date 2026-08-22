# CURRENT TASK

## Task hiện tại
Frontend Lesson Resource Integration

## Trạng thái
TODO

## Mục tiêu
Tích hợp frontend với Backend Lesson Resource API để admin/teacher có thể quản lý tài liệu đính kèm của lesson bằng metadata URL, và student có thể xem danh sách tài liệu khi học bài.

## Vì sao làm task này?
Backend đã có API nền cho lesson resources. Frontend cần thêm service methods, UI quản lý resource trong trang cấu trúc khóa học, và UI hiển thị resource trong trang học bài để tính năng tài liệu đính kèm trở nên dùng được từ đầu đến cuối.

## Không làm trong task này
- Không làm upload file thật.
- Không làm multipart upload.
- Không tích hợp cloud storage/S3/local storage.
- Không làm quiz.
- Không làm payment.
- Không làm drag-and-drop reorder.
- Không redesign toàn bộ admin course structure hoặc learning page.

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

## API cần tích hợp

### Admin/Teacher
```http
POST /api/v1/admin/lessons/{lessonId}/resources
GET /api/v1/admin/lessons/{lessonId}/resources
GET /api/v1/admin/resources/{id}
PUT /api/v1/admin/resources/{id}
DELETE /api/v1/admin/resources/{id}
```

### Student
```http
GET /api/v1/lessons/{lessonId}/resources
```

## Resource payload
```json
{
  "title": "Tài liệu luyện đọc",
  "resourceType": "PDF",
  "fileUrl": "https://example.com/n5-reading.pdf",
  "fileSize": 1024000,
  "sortOrder": 1
}
```

## Resource types
```text
PDF
DOCUMENT
AUDIO
VIDEO
EXTERNAL_LINK
```

## Logic cần làm

### Admin service
- Thêm methods vào `AdminService`:
  - `getLessonResources(lessonId)`
  - `getResourceDetail(id)`
  - `createLessonResource(lessonId, payload)`
  - `updateLessonResource(id, payload)`
  - `deleteLessonResource(id)`
- Endpoint phải dùng `/v1/admin/...` vì Axios base URL đã là `/api`.

### Learning service
- Thêm method `getLessonResources(lessonId)` gọi:
  - `GET /v1/lessons/{lessonId}/resources`

### Admin resource UI
- Tạo component modal form nếu cần:
  - `frontend/src/components/admin/ResourceFormModal.vue`
- Trong `AdminCourseStructurePage.vue`, thêm cách quản lý resources cho từng lesson:
  - nút hoặc panel "Tài liệu" ở mỗi lesson.
  - load resources theo lesson khi admin mở/xem.
  - tạo resource mới.
  - sửa resource.
  - xóa resource có confirm.
  - reload resources của đúng lesson sau khi save/delete.
- Form validation:
  - `title` bắt buộc, tối đa 255 ký tự.
  - `resourceType` bắt buộc.
  - `fileUrl` bắt buộc, tối đa 1000 ký tự.
  - `fileSize >= 0` nếu có nhập.
  - `sortOrder >= 0`.
- Không upload file, chỉ nhập `fileUrl`.

### Student learning UI
- Trong `LessonLearningPage.vue`, load resources sau khi load lesson thành công.
- Hiển thị panel tài liệu đính kèm:
  - loading state.
  - empty state khi chưa có resource.
  - list title/type/fileSize nếu có.
  - link mở `fileUrl` ở tab mới.
- Nếu gọi resource API lỗi, hiển thị lỗi gọn và không làm hỏng phần học bài chính.

## Cần tạo hoặc chỉnh sửa
- `frontend/src/services/admin.service.js`
- `frontend/src/services/learning.service.js`
- `frontend/src/pages/admin/AdminCourseStructurePage.vue`
- `frontend/src/pages/student/LessonLearningPage.vue`
- Có thể tạo `frontend/src/components/admin/ResourceFormModal.vue`

## Checklist
- [ ] AdminService có đủ resource CRUD methods.
- [ ] LearningService có method lấy lesson resources cho student.
- [ ] Admin có thể xem resources của từng lesson.
- [ ] Admin có thể tạo resource bằng URL metadata.
- [ ] Admin có thể sửa resource.
- [ ] Admin có thể xóa resource có confirm.
- [ ] Resource form validate title/resourceType/fileUrl/fileSize/sortOrder.
- [ ] Student lesson page hiển thị danh sách resources.
- [ ] Resource link mở đúng `fileUrl`.
- [ ] Lỗi resource không làm hỏng lesson content/progress.
- [ ] Chạy `npm run build`.
- [ ] Chạy `npm test`.

## Cách test sau khi hoàn thành
1. Đăng nhập bằng ADMIN.
2. Vào `/admin/courses`.
3. Bấm "Cấu trúc" ở một course.
4. Mở một lesson và vào phần "Tài liệu".
5. Tạo resource thiếu title/resourceType/fileUrl, kỳ vọng hiện validation.
6. Tạo resource hợp lệ, kỳ vọng resource xuất hiện trong list.
7. Sửa resource, kỳ vọng dữ liệu cập nhật đúng.
8. Xóa resource, kỳ vọng resource biến mất khỏi list.
9. Đăng nhập STUDENT có quyền học lesson.
10. Vào trang học bài, kỳ vọng thấy panel tài liệu đính kèm.
11. Click resource link, kỳ vọng mở URL đúng.
12. Chạy `npm run build`.
13. Chạy `npm test`.

## Kết quả mong muốn
Frontend sử dụng được lesson resources từ đầu đến cuối: admin/teacher quản lý metadata tài liệu trong course structure, student xem được tài liệu ngay trong trang học bài, chưa cần upload file thật.
