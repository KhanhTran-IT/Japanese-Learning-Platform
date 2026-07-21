# CURRENT TASK

## Task hiện tại
Backend Admin Course Publish/Hide API

## Trạng thái
TODO

## Mục tiêu
Hoàn thiện API publish/hide khóa học cho Admin/Teacher tại backend, gồm `PUT /api/v1/admin/courses/{id}/publish` và `PUT /api/v1/admin/courses/{id}/hide`. API phải cập nhật `CourseStatus`, trả `CourseRes`, giữ đúng phân quyền và không phá vỡ các API Course CRUD hiện có.

## Vì sao làm task này?
MVP yêu cầu admin có thể publish/hide khóa học. Backend hiện đã có CRUD khóa học nhưng chưa có endpoint riêng cho publish/hide theo tài liệu `docs/26_API_PRIORITY.md`. Trước khi làm màn frontend `CourseManagementPage`, backend cần có đủ API trạng thái để admin vận hành khóa học an toàn.

## Không làm trong task này
- Không làm frontend quản lý khóa học.
- Không làm create/update/delete course vì đã có API cơ bản.
- Không làm quản lý section/lesson.
- Không làm upload thumbnail/file.
- Không làm payment, enrollment hoặc progress.
- Không đổi cấu trúc Entity nếu không cần thiết.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/24_USER_FLOWS.md`
- `docs/26_API_PRIORITY.md`
- `docs/27_DATABASE_PHASES.md`
- `docs/28_ENUM_DEFINITIONS.md`
- `docs/29_ERROR_CODE_STANDARD.md`
- `docs/30_PERMISSION_MATRIX.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/05_features/05_02_COURSE_FEATURES.md`
- `docs/07_database/07_02_COURSE_LESSON.md`
- `docs/08_api/08_10_ADMIN_API.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`

## API cần làm
```http
PUT /api/v1/admin/courses/{id}/publish
PUT /api/v1/admin/courses/{id}/hide
```

Quyền truy cập:
- `ADMIN`
- `SUPER_ADMIN`
- `TEACHER` nếu là teacher sở hữu khóa học, theo logic `checkTeacherPermission()` hiện có.

## Request mẫu
```http
PUT /api/v1/admin/courses/1/publish
Authorization: Bearer <accessToken>
```

```http
PUT /api/v1/admin/courses/1/hide
Authorization: Bearer <accessToken>
```

Không có request body.

## Response mong muốn
```json
{
  "code": 1000,
  "message": "Xuất bản khóa học thành công",
  "result": {
    "id": 1,
    "title": "N5 nhập môn cho người mới bắt đầu",
    "slug": "n5-nhap-mon-cho-nguoi-moi-bat-dau",
    "status": "PUBLISHED"
  }
}
```

```json
{
  "code": 1000,
  "message": "Ẩn khóa học thành công",
  "result": {
    "id": 1,
    "title": "N5 nhập môn cho người mới bắt đầu",
    "slug": "n5-nhap-mon-cho-nguoi-moi-bat-dau",
    "status": "HIDDEN"
  }
}
```

## Logic xử lý
- Bổ sung method `publishCourse(Long id)` và `hideCourse(Long id)` trong `CourseAdminService`.
- Implement trong `CourseAdminServiceImpl`.
- Tìm course theo id, nếu không có thì throw `AppException(ErrorCode.COURSE_NOT_FOUND)`.
- Gọi lại `checkTeacherPermission(course)` để giữ rule data isolation hiện có.
- Khi publish:
  - đổi `course.status` thành `CourseStatus.PUBLISHED`.
  - nên kiểm tra khóa học có ít nhất 1 bài học trước khi publish nếu dữ liệu hiện có hỗ trợ kiểm tra rõ ràng.
  - nếu chưa đủ điều kiện publish, dùng `INVALID_REQUEST` hoặc thêm ErrorCode rõ hơn như `COURSE_CANNOT_PUBLISH_EMPTY`.
- Khi hide:
  - đổi `course.status` thành `CourseStatus.HIDDEN`.
  - không xóa course, không đụng enrollment/progress.
- Sau khi save, map sang `CourseRes`.
- Bổ sung endpoint trong `CourseAdminController`.
- Dùng `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")` giống API CRUD course hiện tại.
- Không trả Entity trực tiếp.

## Cần tạo hoặc chỉnh sửa
- `backend/src/main/java/com/japaneselearning/module_course/controller/CourseAdminController.java`
- `backend/src/main/java/com/japaneselearning/module_course/service/CourseAdminService.java`
- `backend/src/main/java/com/japaneselearning/module_course/service/CourseAdminServiceImpl.java`
- Có thể chỉnh `backend/src/main/java/com/japaneselearning/common/exception/ErrorCode.java` nếu cần ErrorCode riêng cho publish khóa học chưa đủ điều kiện.

## Error code cần dùng
- `COURSE_NOT_FOUND` khi không tìm thấy khóa học.
- `DATA_ISOLATION_FORBIDDEN` khi teacher thao tác khóa học không thuộc quyền.
- `INVALID_REQUEST` hoặc ErrorCode mới `COURSE_CANNOT_PUBLISH_EMPTY` nếu khóa học chưa đủ điều kiện publish.

## Checklist
- [ ] Thêm `publishCourse(Long id)` vào `CourseAdminService`.
- [ ] Thêm `hideCourse(Long id)` vào `CourseAdminService`.Spring Bean Life Cycle: Vòng đời của một Bean từ lúc khởi tạo đến lúc bị hủy. Phân biệt các Bean Scope (Singleton, Prototype, Request, Session) và cách chúng hoạt động trong môi trường đa luồng.
- [ ] Implement publish trong `CourseAdminServiceImpl`.
- [ ] Implement hide trong `CourseAdminServiceImpl`.
- [ ] Giữ lại kiểm tra quyền sở hữu bằng `checkTeacherPermission(course)`.
- [ ] Thêm endpoint `PUT /api/v1/admin/courses/{id}/publish`.
- [ ] Thêm endpoint `PUT /api/v1/admin/courses/{id}/hide`.
- [ ] Response trả `ApiResponse<CourseRes>`.
- [ ] Không trả Entity trực tiếp.
- [ ] Chạy `mvn test`.

## Cách test sau khi hoàn thành
1. Chạy backend Spring Boot.
2. Đăng nhập bằng admin và lấy access token.
3. Tạo hoặc chọn một course đang `DRAFT`.
4. Gọi `PUT /api/v1/admin/courses/{id}/publish`, kỳ vọng status thành `PUBLISHED`.
5. Gọi `PUT /api/v1/admin/courses/{id}/hide`, kỳ vọng status thành `HIDDEN`.
6. Gọi với course id không tồn tại, kỳ vọng lỗi `COURSE_NOT_FOUND`.
7. Dùng token student gọi publish/hide, kỳ vọng bị 403.
8. Nếu test teacher, teacher chỉ được publish/hide khóa học của chính mình.
9. Chạy `mvn test`.

## Kết quả mong muốn
Backend có đủ API publish/hide khóa học theo MVP, phân quyền đúng, giữ data isolation cho teacher và sẵn sàng để frontend `CourseManagementPage` tích hợp trạng thái khóa học.
