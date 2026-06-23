# CURRENT TASK

## Task hiện tại
Admin Section CRUD API (Quản lý Chương học)

## Trạng thái
DONE
Ngày hoàn thành: 23/06/2026

## Mục tiêu
Xây dựng bộ các API với prefix `/api/v1/` dành cho Admin/Teacher để thực hiện các tác vụ CRUD quản trị các Chương học (`CourseSection`) thuộc về một Khóa học (`Course`) cụ thể.

## Vì sao làm task này?
Theo kiến trúc phân cấp dữ liệu của nền tảng: `Course (1) -> (N) CourseSection (1) -> (N) Lesson`. Sau khi đã tạo được Khóa học ở task trước, hệ thống cần tính năng quản lý cấu trúc chương học để làm khung sườn dữ liệu trước khi Admin/Teacher tiến hành thêm nội dung bài học chi tiết.

## Không làm trong task này
- Không làm API CRUD cho Bài học (`Lesson`) hoặc Tài nguyên bài học (`LessonResource`).
- Không làm giao diện Frontend.
- Không can thiệp vào logic tính toán tiến độ học tập (`Learning Progress`).

## File tài liệu cần dùng
- Cấu trúc thư mục: `docs/09_BACKEND_STRUCTURE.md`
- Chuẩn hóa đầu ra: `docs/00_API_RESPONSE_STANDARD.md` (Bọc qua `ApiResponse`)
- Ràng buộc thực thể: File Entity `CourseSection.java` và `Course.java` đã tạo ở module dữ liệu foundation.

## API cần làm
- `POST /api/v1/admin/courses/{courseId}/sections`: Tạo một chương học mới thuộc khóa học `{courseId}`.
- `GET /api/v1/admin/courses/{courseId}/sections`: Lấy toàn bộ danh sách các chương học của khóa học `{courseId}` (Sắp xếp tăng dần theo trường `sortOrder`).
- `PUT /api/v1/admin/sections/{id}`: Cập nhật thông tin chi tiết (Tiêu đề, mô tả, thứ tự sắp xếp `sortOrder`, trạng thái) của chương học có mã `{id}`.
- `DELETE /api/v1/admin/sections/{id}`: Xóa chương học (Xem chi tiết logic ràng buộc ở phần dưới).

## Logic xử lý kiến trúc & Nghiệp vụ
1. **Phân quyền và Bảo mật:** Toàn bộ Controller giới hạn quyền bằng `@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")`.
2. **Kiểm tra tồn tại gốc:** Khi thực hiện thao tác tác động qua `courseId`, phải kiểm tra Khóa học có tồn tại không (`COURSE_001`).
3. **Data Isolation (Cô lập dữ liệu Giáo viên):** - Với vai trò `TEACHER`: Khi thêm Section vào một `Course`, hoặc cập nhật/xóa một Section hiện tại, hệ thống bắt buộc phải kiểm tra xem Khóa học đó có phải do chính Teacher đang đăng nhập tạo ra hay không. Nếu không, ném ra mã lỗi `AUTH_003` (Forbidden).
   - Với vai trò `ADMIN`: Bỏ qua bước kiểm tra sở hữu, được quyền cấu hình mọi khóa học.
4. **Tự động xử lý Thứ tự (`sortOrder`):** Khi tạo mới một Section thông qua API `POST` mà client không truyền lên thuộc tính `sortOrder`, hệ thống tự động tìm kiếm giá trị `sortOrder` lớn nhất hiện tại của khóa học đó và cộng thêm 1.
5. **Ràng buộc Xóa (Safety Business Rule):** Khi thực hiện API `DELETE` một Section, hệ thống cần kiểm tra xem Section đó hiện tại có chứa bất kỳ Bài học (`Lesson`) nào không. 
   - Nếu có: Từ chối xóa và ném lỗi `SECTION_002` (Không thể xóa chương học đang chứa bài học).
   - Nếu trống: Cho phép thực hiện xóa vật lý khỏi hệ thống.

## Cần tạo hoặc chỉnh sửa (Trong package module_course)
- `SectionCreateReq` & `SectionUpdateReq` (DTO Requests có kèm Validation như `@NotBlank`, `@Min`).
- `SectionRes` (DTO Response trả ra dữ liệu sạch).
- `SectionMapper` (MapStruct hoặc manual method).
- `SectionAdminService` & `SectionAdminServiceImpl`.
- `SectionAdminController`.
- Bổ sung các hàm truy vấn bổ trợ vào `CourseSectionRepository` (ví dụ: `findByCourseIdOrderBySortOrderAsc`).

## Error code cần dùng (Theo chuẩn PREFIX_00X)
- `COURSE_001`: Course not found (404)
- `SECTION_001`: Section not found (404)
- `SECTION_002`: Section contains lessons, cannot delete (400)
- `AUTH_003`: Forbidden (Không có quyền can thiệp vào khóa học của người khác)
- `VALID_001`: Validation Error

## Checklist
- [ ] Định nghĩa các class DTO Request và DTO Response cho Section.
- [ ] Thiết lập phương thức Mapper chuyển đổi dữ liệu.
- [ ] Bổ sung custom query sắp xếp theo `sortOrder` vào `CourseSectionRepository`.
- [ ] Cài đặt logic nghiệp vụ trong tầng Service (Bao gồm kiểm tra tồn tại, tính toán `sortOrder` tự động và áp dụng Data Isolation).
- [ ] Hiện thực hóa logic kiểm tra bài học bên trong trước khi thực thi lệnh Xóa.
- [ ] Triển khai Controller bọc dữ liệu trả về qua `ApiResponse`.
- [ ] Khởi động ứng dụng, dùng Postman đăng nhập tài khoản Teacher A để test việc tạo Section trên Khóa học của chính mình -> Thành công.
- [ ] Dùng tài khoản Teacher A cố tình POST/PUT/DELETE trên Khóa học của Teacher B -> Hệ thống trả về 403 Forbidden chuẩn xác.
- [ ] Test API lấy danh sách Section đảm bảo hiển thị đúng thứ tự tăng dần của cấu trúc chương học.

## Cách test sau khi hoàn thành
1. Chạy Spring Boot backend ứng dụng.
2. Dùng Postman tạo cấu trúc 3 Section liên tiếp cho một Khóa học có sẵn mà không gửi trường `sortOrder` để kiểm tra tính năng tự động tăng chỉ số (1, 2, 3) dưới DB.
3. Gọi API `GET /api/v1/admin/courses/{courseId}/sections` kiểm tra định dạng mảng JSON trả về.
4. Thử gọi API `DELETE` trên một Section trống để đảm bảo xóa thành công.

## Kết quả mong muốn
Module quản lý Chương học vận hành mượt mà, áp dụng chuẩn chỉ quy tắc cô lập dữ liệu và bảo vệ cấu trúc dữ liệu tránh việc xóa nhầm khi có bài học ràng buộc bên trong.