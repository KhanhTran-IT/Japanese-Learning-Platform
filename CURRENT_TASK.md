# CURRENT TASK

## Task hiện tại
Free Course Enrollment API (API Ghi danh Khóa học Miễn phí)

## Trạng thái
DONE
Ngày hoàn thành: 27/06/2026

## Mục tiêu
Xây dựng API cho phép Học viên (`STUDENT`) thực hiện ghi danh vào một Khóa học có trạng thái giá là Miễn phí (`FREE`). Dữ liệu sẽ được lưu vào bảng `course_enrollments` để mở khóa tính năng học tập cho người dùng.

## Vì sao làm task này?
Đây là chìa khóa để chuyển người dùng từ trạng thái Khách tham quan sang trạng thái Học viên chính thức của một khóa học. Bảng `course_enrollments` đóng vai trò là cầu nối (Bảng trung gian) cấp quyền cho Học viên bắt đầu truy cập vào các video bị khóa và lưu tiến độ học tập.

## Không làm trong task này
- Không xử lý thanh toán (Payment Gateway).
- Không xử lý ghi danh cho khóa học trả phí (`PAID`).
- Không tạo API tracking tiến độ (Lesson Progress) ở task này.

## File tài liệu cần dùng
- Ràng buộc nghiệp vụ: Bảng `course_enrollments` trong file `07_02_COURSE_LESSON.md`.
- Chuẩn hóa Response JSON: `docs/00_API_RESPONSE_STANDARD.md`.

## API cần làm
- `POST /api/v1/courses/{courseId}/enroll`: Thực hiện ghi danh vào khóa học. Không cần truyền body, hệ thống tự động xác thực qua token.

## Logic xử lý kiến trúc & Nghiệp vụ
1. **Bảo mật & Phân quyền:** Endpoint này giới hạn cho người dùng có token hợp lệ. Yêu cầu `@PreAuthorize("hasRole('STUDENT')")` (Hoặc cho phép cả Teacher nếu cần test, nhưng logic thực tế là Student). Lấy `user_id` trực tiếp từ `SecurityContextHolder`.
2. **Kiểm tra tính hợp lệ của Khóa học:**
   - Khóa học (`courseId`) phải tồn tại.
   - Khóa học phải có trạng thái đang mở bán/phát hành (`status == 'PUBLISHED'`). Nếu khóa đang DRAFT hoặc ARCHIVED, chặn và ném lỗi `COURSE_003` (Course not available for enrollment).
   - Khóa học bắt buộc phải là loại Miễn phí (`courseType == 'FREE'`). Nếu là `PAID`, ném lỗi `COURSE_004` (Cannot directly enroll in a paid course).
3. **Chống Duplicate Enrollment:**
   - Một user chỉ được đăng ký một khóa học duy nhất 1 lần. Trước khi lưu, phải kiểm tra trong bảng `course_enrollments` xem đã tồn tại cặp `(user_id, course_id)` chưa. Nếu có, ném lỗi `ENROLL_001` (User already enrolled in this course).
4. **Lưu trữ dữ liệu:**
   - Tạo bản ghi mới trong bảng `course_enrollments` với `user_id`, `course_id`, `enrolled_at` = thời gian hiện tại, `status` = 'ACTIVE', `progress_percent` = 0.

## Cần tạo hoặc chỉnh sửa
- Khởi tạo `CourseEnrollment` Entity, `CourseEnrollmentRepository` (Đảm bảo có package `module_course` hoặc `module_enrollment` tùy cấu trúc).
- Khởi tạo các DTOs nếu cần (Tuy nhiên API này có thể chỉ trả về message thành công hoặc DTO cơ bản chứa ngày giờ enroll).
- Tạo/Cập nhật `CourseEnrollmentService` & `CourseEnrollmentServiceImpl`.
- Tạo/Cập nhật `CourseEnrollmentController`.

## Error code cần dùng (Theo chuẩn PREFIX_00X)
- `COURSE_001`: Course not found (404)
- `COURSE_003`: Course not available for enrollment (400)
- `COURSE_004`: Cannot directly enroll in a paid course (400)
- `ENROLL_001`: User already enrolled in this course (409)

## Checklist
- [ ] Thiết lập cấu trúc Entity `CourseEnrollment` đúng mapping với bảng trong DB (chú ý Composite Unique Key).
- [ ] Xây dựng logic Service xử lý chặt chẽ 4 bước kiểm tra (Tồn tại, Published, Free, Đã Enroll).
- [ ] Đóng gói Controller chuẩn hóa `ApiResponse`.
- [ ] Cấu hình Security cho API.
- [ ] Test trường hợp ghi danh thành công với khóa FREE.
- [ ] Test trường hợp cố tình gọi API ghi danh cho khóa PAID (Mong đợi lỗi 400).
- [ ] Test trường hợp ghi danh 2 lần cùng 1 khóa học (Mong đợi lỗi 409).

## Kết quả mong muốn
Học viên có thể "Mua" miễn phí các khóa học cơ bản để bắt đầu hành trình học tập. Dữ liệu bảng `course_enrollments` được lưu trữ chính xác, chống lặp lặp dữ liệu thành công.