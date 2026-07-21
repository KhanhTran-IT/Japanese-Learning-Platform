# CURRENT TASK

## Task hiện tại
Frontend Admin Course Management UI & API Integration

## Trạng thái
TODO

## Mục tiêu
Xây dựng màn hình quản lý khóa học cho Admin tại route `/admin/courses`, tích hợp với các API backend course admin hiện có để admin có thể xem danh sách khóa học, xem trạng thái, tạo/sửa/xóa mềm cơ bản nếu phù hợp, và publish/hide khóa học.

## Vì sao làm task này?
Course Management là màn P0 trong MVP. Backend đã có API Course CRUD và publish/hide, nên frontend cần có giao diện quản trị để admin vận hành nội dung khóa học trước khi đi tiếp sang quản lý section/lesson.

## Không làm trong task này
- Không làm quản lý section/lesson trong màn này.
- Không làm upload file/thumbnail thật.
- Không làm rich text editor nâng cao.
- Không làm quản lý enrollment/progress/payment.
- Không làm public course list/detail.
- Không thêm thư viện UI mới nếu project chưa dùng.
- Không refactor lớn layout admin hiện có.

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
- `docs/08_api/08_10_ADMIN_API.md`
- `docs/10_FRONTEND_STRUCTURE.md`
- `docs/11_BACKEND_FRONTEND_CONFIG.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`

## API cần làm
Frontend gọi các API backend sau:

```http
GET    /api/v1/admin/courses?page=0&size=10
GET    /api/v1/admin/courses/{id}
POST   /api/v1/admin/courses
PUT    /api/v1/admin/courses/{id}
DELETE /api/v1/admin/courses/{id}
PUT    /api/v1/admin/courses/{id}/publish
PUT    /api/v1/admin/courses/{id}/hide
```

## Request mẫu
```javascript
AdminService.getCourses({ page: 0, size: 10 })
AdminService.getCourseDetail(1)
AdminService.createCourse(payload)
AdminService.updateCourse(1, payload)
AdminService.deleteCourse(1)
AdminService.publishCourse(1)
AdminService.hideCourse(1)
```

## Response mong muốn
Danh sách course dùng response hiện có từ backend:

```json
{
  "code": 1000,
  "message": "Lấy danh sách khóa học thành công",
  "result": {
    "content": [
      {
        "id": 1,
        "title": "N5 nhập môn",
        "slug": "n5-nhap-mon",
        "level": "N5",
        "courseType": "FREE",
        "status": "PUBLISHED",
        "teacherName": "Teacher Demo",
        "totalLessons": 10,
        "totalStudents": 25,
        "createdAt": "2026-07-21T10:00:00"
      }
    ],
    "number": 0,
    "size": 10,
    "totalPages": 3,
    "totalElements": 25
  }
}
```

Lưu ý: backend Course Admin hiện trả `Page<CourseRes>` trực tiếp trong `ApiResponse`, không phải `PageResponse` custom. Frontend cần map đúng theo `result.content`, `result.number`, `result.totalPages`, `result.totalElements`.

## Logic xử lý
- Khai báo route `/admin/courses` dưới `AdminLayout`.
- Thêm menu "Khóa học" vào `AdminLayout.vue`.
- Mở rộng `AdminService` với nhóm hàm course admin.
- Tạo `AdminCourseManagementPage.vue`.
- UI cần có:
  - header "Quản lý khóa học"
  - nút "Tạo khóa học"
  - bảng danh sách course
  - status badge: `DRAFT`, `PUBLISHED`, `HIDDEN`, `ARCHIVED`
  - level/type badge: `N5`, `N4`, `FREE`, `PAID`
  - pagination
  - loading/error/empty state
  - action publish/hide/delete
- Tạo form modal đơn giản cho create/update course nếu phạm vi vừa đủ:
  - title
  - slug
  - shortDescription
  - description
  - thumbnailUrl
  - level
  - courseType
  - originalPrice
  - salePrice
  - status
- Nếu form create/update quá lớn, ưu tiên list + publish/hide/delete trước, nhưng cần ghi rõ phần create/update còn lại.
- Khi publish/hide/delete, phải confirm trước.
- Nếu publish thất bại do course chưa có bài học, hiển thị lỗi backend rõ ràng.
- Không trả hoặc hiển thị dữ liệu nhạy cảm.
- Format ngày theo `vi-VN`.

## Cần tạo hoặc chỉnh sửa
- `frontend/src/pages/admin/AdminCourseManagementPage.vue`
- `frontend/src/services/admin.service.js`
- `frontend/src/router/index.js`
- `frontend/src/layouts/AdminLayout.vue`
- Có thể tạo component nhỏ nếu thật sự cần:
  - `frontend/src/components/admin/CourseStatusBadge.vue`
  - `frontend/src/components/admin/CourseFormModal.vue`

## Error code cần dùng
Không tạo error code frontend riêng. Frontend cần xử lý:
- 401: chưa đăng nhập hoặc token hết hạn.
- 403: không có quyền admin/teacher.
- 404: course không tồn tại.
- 400: course chưa đủ điều kiện publish, ví dụ chưa có bài học.
- 409: slug course đã tồn tại khi create/update.

## Checklist
- [ ] Khai báo route `/admin/courses`.
- [ ] Thêm menu "Khóa học" trong `AdminLayout.vue`.
- [ ] Thêm course methods vào `admin.service.js`.
- [ ] Tạo `AdminCourseManagementPage.vue`.
- [ ] Render bảng course với title, teacher, level, type, status, lessons, students, createdAt, actions.
- [ ] Thêm pagination theo response `Page<CourseRes>`.
- [ ] Xử lý loading/error/empty state.
- [ ] Thêm publish action có confirm.
- [ ] Thêm hide action có confirm.
- [ ] Thêm delete/archive action có confirm nếu dùng API delete hiện có.
- [ ] Thêm create/update modal nếu đủ thời gian trong phạm vi task.
- [ ] Chạy `npm run build`.

## Cách test sau khi hoàn thành
1. Chạy backend Spring Boot.
2. Chạy frontend Vue.
3. Đăng nhập bằng admin.
4. Mở `/admin/courses`, kỳ vọng thấy bảng danh sách khóa học.
5. Chuyển trang pagination, kỳ vọng dữ liệu thay đổi đúng.
6. Publish một course có bài học, kỳ vọng status thành `PUBLISHED`.
7. Publish course chưa có bài học, kỳ vọng hiển thị lỗi rõ ràng.
8. Hide course, kỳ vọng status thành `HIDDEN`.
9. Delete/archive course nếu có nút, kỳ vọng course chuyển `ARCHIVED` hoặc biến khỏi list tùy backend trả.
10. Đăng nhập bằng student rồi cố vào `/admin/courses`, kỳ vọng bị route guard chặn.

## Kết quả mong muốn
Admin có màn quản lý khóa học cơ bản, gọi API thật, hiển thị trạng thái khóa học rõ ràng, thao tác publish/hide/delete an toàn và sẵn sàng nối tiếp sang task quản lý section/lesson.
