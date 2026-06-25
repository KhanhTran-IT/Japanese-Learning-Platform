# CURRENT TASK

## Task hiện tại
Student Course Public API (Danh sách và Chi tiết Khóa học cho Học viên)

## Trạng thái
DONE
Ngày hoàn thành: 25/06/2026

## Mục tiêu
Xây dựng bộ API công khai với prefix `/api/v1/courses` dành cho Học viên (`Student`) và khách vãng lai (Anonymous) để thực hiện tìm kiếm, xem danh sách khóa học (có phân trang/lọc) và xem cấu trúc chi tiết của một khóa học (Hiển thị sơ đồ chương mục và tiêu đề bài học).

## Vì sao làm task này?
Sau khi hoàn thành hệ thống API Admin CRUD ở các task trước, database hiện tại đã có dữ liệu cấu trúc nội dung. Task này giúp mở cổng hiển thị dữ liệu ra ngoài giao diện cho Học viên. Đây là bước đệm cốt lõi trước khi làm tính năng Đăng ký học (Enrollment) và Màn hình học bài chi tiết (Learning Player Flow).

## Không làm trong task này
- Không làm API Đăng ký học (`Enrollment`).
- Không trả về nội dung chi tiết bên trong bài học (như link video, nội dung text bí mật, tài liệu đính kèm) đối với các bài học không ở chế độ học thử (`isPreview = false`).
- Không tracking tiến độ học.

## File tài liệu cần dùng
- Chuẩn hóa Response JSON: `docs/00_API_RESPONSE_STANDARD.md` (Bọc dữ liệu qua `ApiResponse`).
- Phân quyền: `docs/30_PERMISSION_MATRIX.md` (Bất kỳ ai cũng có quyền gọi API này).
- Cấu trúc thực thể liên kết: `Course.java`, `CourseSection.java`, `Lesson.java`.

## API cần làm
- `GET /api/v1/courses`: Lấy danh sách khóa học hiển thị ngoài trang chủ/khóa học.
  - *Hỗ trợ:* Phân trang (`page`, `size`), sắp xếp (`sortBy`), và bộ lọc cơ bản theo cấp độ (`level` - N5, N4, N3...).
  - *Điều kiện ép buộc:* Chỉ hiển thị các khóa học có trạng thái `status = 'PUBLISHED'`. Tuyệt đối không hiển thị khóa học mang trạng thái `DRAFT` hay `ARCHIVED`.
- `GET /api/v1/courses/{slug}`: Xem chi tiết cấu trúc nội dung một khóa học bằng đường dẫn chuỗi định danh (Slug) thân thiện.
  - *Dữ liệu trả về:* Thông tin khóa học kèm theo toàn bộ danh sách Chương mục (`Sections`), và trong mỗi Chương mục chứa danh sách tiêu đề các Bài học (`Lessons`) được sắp xếp tăng dần theo `sortOrder`.

## Logic xử lý kiến trúc & Nghiệp vụ
1. **Bảo mật & Phân quyền:** Các API này hoàn toàn là Public, cấu hình trong SecurityConfig để cho phép truy cập tự do không cần Token (`.permitAll()`).
2. **Ẩn giấu nội dung khóa học (Data Protection):** Tại API chi tiết khóa học, đối với các bài học (`Lesson`) mà trường `isPreview = false`, hệ thống bắt buộc phải set các trường nhạy cảm như `videoUrl`, `content` về giá trị `null` hoặc chuỗi trống trước khi trả về cho Client để tránh việc học viên F12 lấy link video học lậu mà không mua khóa học. Chỉ giữ lại thông tin cho bài học có `isPreview = true`.
3. **Hiệu năng siêu tối ưu (Chống N+1 Cấp Độ Nặng):** API lấy chi tiết khóa học kèm theo toàn bộ Chương và Bài học (`Course -> Sections -> Lessons`) là một "mồi ngon" sinh ra lỗi N+1 Query kinh điển khiến sập hệ thống nếu có nhiều người truy cập.
  - *Yêu cầu:* Phải viết câu lệnh custom JPQL sử dụng các từ khóa `FETCH JOIN` tuần tự hoặc định nghĩa `@EntityGraph` đa tầng tại `CourseRepository` để kéo toàn bộ cây thực thể về chỉ với 1 hoặc tối đa 2 câu lệnh SQL tinh gọn.

## Cần tạo hoặc chỉnh sửa (Trong package module_course)
- `CoursePublicRes`: DTO phản hồi danh sách khóa học rút gọn ngoài trang chủ.
- `CourseDetailPublicRes`, `SectionPublicRes`, `LessonPublicRes`: Bộ các DTO phân cấp lồng nhau để trả ra cấu trúc cây nội dung sạch, an toàn cho học viên.
- `CoursePublicService` & `CoursePublicServiceImpl`.
- `CoursePublicController`.
- Bổ sung hàm tìm kiếm theo Slug kèm nạp trước dữ liệu liên kết vào `CourseRepository`.

## Error code cần dùng (Theo chuẩn PREFIX_00X)
- `COURSE_001`: Course not found (404)

## Checklist
- [ ] Thiết kế bộ DTOs Public sạch sẽ, tách biệt hoàn toàn với Admin DTOs để tránh lộ dữ liệu bảo mật.
- [ ] Viết hàm custom query dùng `FETCH JOIN` lồng nhau hoặc `@EntityGraph` sâu trong `CourseRepository` để lấy Course theo Slug kèm đầy đủ Sections và Lessons trong 1 câu SQL.
- [ ] Cài đặt logic lọc dữ liệu tại Service (Chỉ lấy trạng thái `PUBLISHED`, xóa thông tin bảo mật của bài học nếu `isPreview = false`).
- [ ] Viết `CoursePublicController` định tuyến URL `/api/v1/courses`.
- [ ] Cấu hình `SecurityConfig.java` cho phép quyền truy cập công khai endpoint này.
- [ ] Mở Postman/Swagger, thực hiện gọi API mà không đính kèm JWT Token để kiểm tra tính năng truy cập công khai.
- [ ] Xác nhận log console của Hibernate không sinh ra chuỗi vòng lặp SELECT vô tận (N+1 Query) khi lấy chi tiết khóa học.
- [ ] Kiểm tra chắc chắn các trường `videoUrl` của bài học không học thử đã bị ẩn (bằng `null`).

## Kết quả mong muốn
Hệ thống cung cấp một API hiển thị nội dung khóa học cực kỳ mượt mà, bảo mật tuyệt đối tài nguyên nội dung video trả phí, tốc độ truy vấn tối ưu cao nhờ xử lý triệt để bài toán nạp dữ liệu sâu.