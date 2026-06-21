# CURRENT TASK

## Task hiện tại
Admin Course CRUD API

## Trạng thái
DONE
Ngày hoàn thành: 21/06/2026

## Mục tiêu
Xây dựng các API cơ bản cho Admin/Teacher để quản lý Khóa học (Course). Bao gồm: Tạo mới, Cập nhật, Xem danh sách (có phân trang), Xem chi tiết và Xóa (Chuyển trạng thái sang ARCHIVED).

## Vì sao làm task này?
Nằm trong chu trình MVP bắt buộc. Sau khi đã thiết kế xong database cho module `Course/Lesson` và luồng Auth/User, hệ thống cần cổng API CRUD quản trị khóa học. Đây là tiền đề bắt buộc (Aggregate Root) để có thể tạo Section và Lesson ở các bước tiếp theo, đồng thời chuẩn bị dữ liệu cho luồng Student Enroll.

## Không làm trong task này
- Không làm API CRUD cho Section hay Lesson.
- Không làm Upload file/ảnh thật lên Cloud/S3 (chỉ truyền string URL tạm).
- Không làm logic tính toán giá tiền hay discount phức tạp.
- Không can thiệp vào module Auth hay logic User ngoài việc lấy thông tin người dùng hiện tại.

## File tài liệu cần dùng
- Tham chiếu cấu trúc: `docs/09_BACKEND_STRUCTURE.md` (Luồng Controller -> Service -> Repository, dùng DTO).
- Response chuẩn: `docs/00_API_RESPONSE_STANDARD.md` (Bọc kết quả trong `ApiResponse`).
- Quyền hạn: `docs/30_PERMISSION_MATRIX.md` (Chỉ Admin/Teacher).
- Bảng thiết kế Database & Enums từ task `Course/Lesson Database Foundation` trước đó.

## API cần làm
Tất cả API phải có prefix `/api/v1/`.
- `POST /api/v1/admin/courses`: Tạo khóa học mới (Teacher mặc định là người đang đăng nhập).
- `GET /api/v1/admin/courses`: Lấy danh sách khóa học (Hỗ trợ Pagination, filter cơ bản).
- `GET /api/v1/admin/courses/{id}`: Lấy chi tiết 1 khóa học.
- `PUT /api/v1/admin/courses/{id}`: Cập nhật thông tin khóa học.
- `DELETE /api/v1/admin/courses/{id}`: Soft Delete (Đổi trạng thái `status` thành `ARCHIVED`).

## Logic xử lý kiến trúc & Nghiệp vụ
1. **Security & Role:** Giới hạn quyền truy cập bằng `@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")`.
2. **Current User:** Ở API POST, KHÔNG nhận `teacher_id` từ body. Phải lấy `id` của user đang đăng nhập từ `SecurityContextHolder`.
3. **Data Isolation:** Teacher chỉ được phép PUT/DELETE/GET khóa học do chính mình tạo ra. Admin được toàn quyền.
4. **Performance:** Ở API GET chi tiết hoặc danh sách, nếu cần lấy thông tin Teacher, phải dùng `@EntityGraph` hoặc `FETCH JOIN` trong Repository để tránh lỗi N+1 Query.
5. **Slug:** Nếu request tạo mới không gửi `slug`, hệ thống tự generate từ `title`. Nếu request có `slug`, kiểm tra tính unique.

## Cần tạo hoặc chỉnh sửa (Trong package module_course)
- `CourseCreateReq` & `CourseUpdateReq` (DTO Request có @Valid).
- `CourseRes` (DTO Response không chứa thông tin nhạy cảm của Teacher).
- `CourseMapper` (MapStruct hoặc manual).
- `CourseAdminService` & `CourseAdminServiceImpl`.
- `CourseAdminController`.
- Bổ sung Custom Query / EntityGraph vào `CourseRepository`.

## Error code cần dùng (Theo chuẩn PREFIX_00X)
- `COURSE_001`: Course not found (404)
- `COURSE_002`: Course slug already exists (409)
- `AUTH_003`: Forbidden (Người dùng không có quyền sửa khóa học của người khác)
- `VALID_001`: Validation Error (Xử lý bởi GlobalExceptionHandler)

## Checklist
- [ ] Khởi tạo các class DTO request/response.
- [ ] Viết Mapper để chuyển đổi Entity <-> DTO.
- [ ] Viết Repository methods (có `@EntityGraph` để tránh N+1).
- [ ] Viết logic Service (Check unique slug, phân quyền data Teacher/Admin).
- [ ] Viết Controller với URL `/api/v1/admin/courses`.
- [ ] Bọc tất cả response bằng class `ApiResponse` chuẩn của dự án.
- [ ] Chạy server, test POST tạo khóa học qua Postman/Swagger bằng token Admin.
- [ ] Test lỗi 403 khi dùng token Student.
- [ ] Test lấy danh sách không bị lỗi N+1 query trên log console Hibernate.
- [ ] Test các API cũ còn chạy đúng không.

## Kết quả mong muốn
Hoàn thành trọn vẹn bộ API quản trị khóa học. Controller gọn gàng, Service chứa toàn bộ business logic. API tuân thủ đúng chuẩn response JSON, chuẩn Prefix Error Code và bảo mật tốt dữ liệu người dùng.