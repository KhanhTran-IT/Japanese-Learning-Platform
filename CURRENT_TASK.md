# CURRENT TASK

## Task hiện tại
Admin Lesson CRUD API (Quản lý Bài học)

## Trạng thái
DONE
Ngày hoàn thành: 24/06/2026

## Mục tiêu
Xây dựng hệ thống các API hoàn chỉnh với prefix `/api/v1/` để hỗ trợ Admin/Teacher thực hiện quản lý, đăng tải nội dung chi tiết của các Bài học (`Lesson`) nằm trong một Chương học (`CourseSection`) cụ thể.

## Vì sao làm task này?
Đây là mảnh ghép cuối cùng để hoàn thiện chu trình quản trị nội dung cốt lõi của module Khóa học (Aggregate Root). Hoàn thành API CRUD cho Bài học giúp hệ thống có đầy đủ dữ liệu nội dung (Video, tài liệu, text) để sẵn sàng cung cấp dữ liệu cho luồng hiển thị màn hình học tập của Học viên (Student Flow) ở các giai đoạn sau.

## Không làm trong task này
- Không làm API CRUD cho các câu hỏi Quiz đi kèm bài học.
- Không làm Upload file/video trực tiếp lên Cloud (chỉ nhận String URL dạng text tạm thời).
- Không làm logic tracking tiến độ học (`LessonProgress`).

## File tài liệu cần dùng
- Định hướng kiến trúc: `docs/09_BACKEND_STRUCTURE.md`
- Chuẩn hóa Response JSON: `docs/00_API_RESPONSE_STANDARD.md`
- Cấu trúc thực thể: File Entity `Lesson.java` và `CourseSection.java` đã định nghĩa.

## API cần làm
Tất cả API bắt buộc chạy qua prefix `/api/v1/admin`.
- `POST /api/v1/admin/sections/{sectionId}/lessons`: Tạo một bài học mới vào chương học `{sectionId}`.
- `GET /api/v1/admin/sections/{sectionId}/lessons`: Lấy toàn bộ bài học thuộc chương `{sectionId}` (Sắp xếp tăng dần theo trường `sortOrder`).
- `GET /api/v1/admin/lessons/{id}`: Xem chi tiết một bài học cụ thể.
- `PUT /api/v1/admin/lessons/{id}`: Cập nhật thông tin chi tiết bài học (Tiêu đề, content, video URL, trạng thái học thử `isPreview`, thứ tự sắp xếp `sortOrder`).
- `DELETE /api/v1/admin/lessons/{id}`: Xóa bài học khỏi hệ thống.

## Logic xử lý kiến trúc & Nghiệp vụ
1. **Security Layer:** Giới hạn truy cập nghiêm ngặt đầu các phương thức bằng `@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")`.
2. **Data Isolation sâu:** 
   - Với vai trò `TEACHER`: Khi một giáo viên gọi bất kỳ tác vụ nào tác động đến Bài học (Thêm/Sửa/Xóa/Xem), hệ thống phải truy vấn ngược từ `Section -> Course` để lấy ra `teacher_id` của Khóa học cha. Nếu `teacher_id` này không trùng với ID của Giáo viên đang đăng nhập, lập tức chặn đứng hành vi và trả về mã lỗi `AUTH_003` (Forbidden).
   - Với vai trò `ADMIN`: Bỏ qua bộ lọc sở hữu dữ liệu, toàn quyền thao tác.
3. **Tự động sinh mã định danh (Slug):** Logic giống với phần Khóa học. Nếu Request tạo mới không truyền `slug`, hệ thống tự động gọi lớp tiện ích `SlugUtils` để tạo chuỗi slug sạch từ tiêu đề bài học. Đồng thời kiểm tra tính duy nhất của slug trong phạm vi Khóa học.
4. **Tự động hóa Thứ tự sắp xếp bài học:** Khi thêm mới bài học vào một Section, nếu không truyền chỉ số `sortOrder`, hệ thống tự động truy vấn tìm `sortOrder` lớn nhất hiện tại của các bài học nằm trong riêng Section đó và cộng thêm 1.

## Cần tạo hoặc chỉnh sửa (Trong package module_course)
- `LessonCreateReq` & `LessonUpdateReq` (DTO Requests bổ sung `@NotBlank`, `@Size`, xử lý chặt các thuộc tính Boolean như `isPreview`).
- `LessonRes` (DTO dữ liệu đầu ra không chứa thông tin thực thể nhạy cảm).
- `LessonMapper` (Định nghĩa map cấu trúc).
- `LessonAdminService` & `LessonAdminServiceImpl`.
- `LessonAdminController`.
- Bổ sung các hàm truy vấn tìm Max thứ tự hoặc tìm danh sách sắp xếp tăng dần vào `LessonRepository`.

## Error code cần dùng (Theo chuẩn PREFIX_00X)
- `SECTION_001`: Section not found (404)
- `LESSON_001`: Lesson not found (404)
- `LESSON_002`: Lesson slug already exists within this course (409)
- `AUTH_003`: Forbidden (Không có quyền can thiệp vào tài nguyên của giáo viên khác)
- `VALID_001`: Validation Error

## Checklist
- [ ] Khởi tạo đầy đủ các lớp DTO Request/Response kèm chú thích validation dữ liệu đầu vào.
- [ ] Thiết lập file Mapper chuyển đổi cấu trúc đối tượng dữ liệu.
- [ ] Viết các câu lệnh custom query bổ trợ sắp xếp bài học vào `LessonRepository`.
- [ ] Cài đặt logic Service xử lý nghiệp vụ tự động điền `slug`, tính toán `sortOrder` và bộ lọc Data Isolation lội ngược dòng thực thể (`Lesson -> Section -> Course`).
- [ ] Hoàn thiện Controller đóng gói dữ liệu phản hồi thông qua wrapper `ApiResponse`.
- [ ] Khởi động ứng dụng, test kịch bản tạo thành công bài học bằng tài khoản Giáo viên hợp lệ.
- [ ] Test kịch bản giả mạo ID Section của khóa học khác bằng tài khoản Giáo viên nhằm kiểm tra tính chính xác của bộ lọc bảo mật (Yêu cầu báo lỗi 403 thành công).
- [ ] Kiểm tra API lấy danh sách bài học đảm bảo đầu ra tuân thủ đúng thứ tự sắp xếp tăng dần của chương học.

## Cách test sau khi hoàn thành
1. Sử dụng công cụ Postman gửi request tạo 2 bài học liên tiếp vào cùng một Chương học để xem trường `sortOrder` tự động nhảy chỉ số (1, 2).
2. Gọi API chi tiết `GET /api/v1/admin/lessons/{id}` kiểm tra cấu trúc JSON trả về.
3. Thử chỉnh sửa trường `isPreview` thành `true` để xác nhận chế độ học thử hoạt động đúng cấu trúc lưu trữ.