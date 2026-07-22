# CURRENT TASK

## Task hiện tại
Frontend Admin Course Create/Update Form Module

## Trạng thái
TODO

## Mục tiêu
Hoàn thiện module form tạo/sửa khóa học trong khu vực Admin, thay cho nút placeholder "Chức năng Tạo Khóa học đang được phát triển!". Form cần dùng đúng payload backend `CourseCreateReq` và `CourseUpdateReq`, tích hợp với `AdminService.createCourse()` và `AdminService.updateCourse()`.

## Vì sao làm task này?
Màn `AdminCourseManagementPage` đã có danh sách khóa học, pagination và các action publish/hide/delete. Tuy nhiên create/update course còn để placeholder vì form khóa học có nhiều input field và cần xử lý kỹ. Tách riêng task này giúp code form gọn, dễ test và tránh làm màn quản lý khóa học quá lớn.

## Không làm trong task này
- Không làm upload file thật lên server/storage.
- Không làm crop ảnh, preview file local nâng cao hoặc quản lý media library.
- Không làm quản lý section/lesson.
- Không làm publish/hide/delete vì đã có trong màn course management.
- Không làm public course pages.
- Không thêm thư viện form/UI mới nếu project chưa dùng.
- Không thay đổi backend API nếu payload hiện tại đã đủ.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/25_SCREEN_LIST.md`
- `docs/26_API_PRIORITY.md`
- `docs/28_ENUM_DEFINITIONS.md`
- `docs/30_PERMISSION_MATRIX.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/05_features/05_02_COURSE_FEATURES.md`
- `docs/08_api/08_10_ADMIN_API.md`
- `docs/10_FRONTEND_STRUCTURE.md`
- `docs/11_BACKEND_FRONTEND_CONFIG.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`

## API cần làm
Frontend gọi các API backend hiện có:

```http
POST /api/v1/admin/courses
PUT  /api/v1/admin/courses/{id}
GET  /api/v1/admin/courses/{id}
```

## Request mẫu
Create course:

```json
{
  "title": "N5 nhập môn cho người mới bắt đầu",
  "slug": "n5-nhap-mon-cho-nguoi-moi-bat-dau",
  "shortDescription": "Khóa học nền tảng cho người mới bắt đầu.",
  "description": "Nội dung mô tả chi tiết khóa học...",
  "thumbnailUrl": "https://example.com/thumbnail.jpg",
  "level": "N5",
  "courseType": "FREE",
  "originalPrice": 0,
  "salePrice": 0
}
```

Update course:

```json
{
  "title": "N5 nhập môn cho người mới bắt đầu",
  "slug": "n5-nhap-mon-cho-nguoi-moi-bat-dau",
  "shortDescription": "Khóa học nền tảng cho người mới bắt đầu.",
  "description": "Nội dung mô tả chi tiết khóa học...",
  "thumbnailUrl": "https://example.com/thumbnail.jpg",
  "level": "N5",
  "courseType": "FREE",
  "originalPrice": 0,
  "salePrice": 0,
  "status": "DRAFT"
}
```

## Response mong muốn
```json
{
  "code": 1000,
  "message": "Tạo khóa học thành công",
  "result": {
    "id": 1,
    "title": "N5 nhập môn cho người mới bắt đầu",
    "slug": "n5-nhap-mon-cho-nguoi-moi-bat-dau",
    "level": "N5",
    "courseType": "FREE",
    "status": "DRAFT"
  }
}
```

## Logic xử lý
- Tạo component form tái sử dụng cho cả create và update, ưu tiên `CourseFormModal.vue` hoặc đặt trực tiếp trong page nếu code vẫn gọn.
- Từ `AdminCourseManagementPage.vue`:
  - nút "Tạo Khóa Học" mở form create.
  - thêm action "Sửa" trên mỗi course để mở form update.
  - khi update, có thể dùng data row hiện có hoặc gọi `getCourseDetail(id)` để lấy dữ liệu mới nhất.
- Form fields cần có:
  - `title` bắt buộc.
  - `slug` tùy chọn, có thể auto-generate từ title nếu để trống.
  - `shortDescription`.
  - `description`.
  - `thumbnailUrl` dạng URL text input, chưa upload file thật.
  - `level`: `N5`, `N4`, `N3`, `N2`, `N1`, `BASIC`, `ADVANCED`.
  - `courseType`: `FREE`, `PAID`, `MEMBERSHIP_ONLY`.
  - `originalPrice`.
  - `salePrice`.
  - `status` chỉ cần trong update: `DRAFT`, `PUBLISHED`, `HIDDEN`, `ARCHIVED`.
- Validate frontend cơ bản:
  - title không được rỗng.
  - level không được rỗng.
  - courseType không được rỗng.
  - originalPrice và salePrice không âm.
  - nếu `courseType = FREE`, có thể tự set price về 0 hoặc cảnh báo nếu price lớn hơn 0.
  - salePrice không nên lớn hơn originalPrice nếu cả hai có giá trị.
- Khi submit:
  - hiển thị loading/disable submit button.
  - gọi `createCourse` hoặc `updateCourse`.
  - nếu thành công, đóng form và reload danh sách course.
  - nếu lỗi 409 slug trùng, hiển thị message rõ ràng.
  - nếu lỗi validation backend, hiển thị message bằng `getApiErrorMessage`.
- Không dùng `alert` cho success/error trong form; ưu tiên inline message.
- Giữ UI nhất quán với admin pages hiện có.

## Cần tạo hoặc chỉnh sửa
- `frontend/src/pages/admin/AdminCourseManagementPage.vue`
- `frontend/src/services/admin.service.js` nếu cần chỉnh method hiện có.
- Có thể tạo:
  - `frontend/src/components/admin/CourseFormModal.vue`
  - `frontend/src/components/admin/CourseStatusBadge.vue` nếu muốn tách badge.

## Error code cần dùng
Không tạo error code frontend riêng. Frontend cần xử lý:
- 400: validation lỗi hoặc request không hợp lệ.
- 401: chưa đăng nhập/token hết hạn.
- 403: không có quyền admin/teacher.
- 404: course không tồn tại khi update.
- 409: slug khóa học đã tồn tại.

## Checklist
- [ ] Thay placeholder create bằng form/modal thật.
- [ ] Thêm action edit cho từng row course.
- [ ] Tạo form create course đúng `CourseCreateReq`.
- [ ] Tạo form update course đúng `CourseUpdateReq`.
- [ ] Validate frontend cơ bản.
- [ ] Auto-generate slug từ title nếu slug để trống.
- [ ] Submit create gọi `AdminService.createCourse`.
- [ ] Submit update gọi `AdminService.updateCourse`.
- [ ] Xử lý loading/error/success trong form.
- [ ] Reload danh sách sau khi create/update thành công.
- [ ] Không làm upload file thật, chỉ dùng `thumbnailUrl`.
- [ ] Chạy `npm run build`.

## Cách test sau khi hoàn thành
1. Chạy backend Spring Boot.
2. Chạy frontend Vue.
3. Đăng nhập bằng admin.
4. Mở `/admin/courses`.
5. Bấm "Tạo Khóa Học", kỳ vọng form mở ra.
6. Submit form thiếu title/level/type, kỳ vọng báo lỗi frontend.
7. Tạo course hợp lệ, kỳ vọng form đóng và danh sách reload có course mới.
8. Tạo course có slug trùng, kỳ vọng hiển thị lỗi rõ ràng.
9. Bấm sửa một course, kỳ vọng form có dữ liệu cũ.
10. Cập nhật course hợp lệ, kỳ vọng danh sách reload và hiển thị dữ liệu mới.
11. Chạy `npm run build`.

## Kết quả mong muốn
Admin có thể tạo và sửa khóa học bằng form frontend thật, payload khớp backend, validate rõ ràng, lỗi hiển thị dễ hiểu và màn quản lý khóa học không còn nút placeholder "Đang phát triển".
