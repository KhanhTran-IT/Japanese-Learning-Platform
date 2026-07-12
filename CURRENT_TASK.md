# CURRENT TASK

## Task hiện tại
Student Dashboard UI & My Courses View (Giao diện Tổng quan Học tập và Khóa học của tôi)

## Trạng thái
TODO

## Mục tiêu
Xây dựng giao diện trang chủ dành cho Học viên (Student Dashboard) ngay sau khi đăng nhập. Trang này sẽ hiển thị tổng quan tiến độ học tập (số khóa học, phần trăm hoàn thành) và danh sách các khóa học mà học viên đã ghi danh, sử dụng dữ liệu từ Data Aggregation API đã xây dựng ở Backend.

## Vì sao làm task này?
Sau khi đăng nhập, học viên cần một không gian (hub) để biết mình đang đứng ở đâu và cần học tiếp bài nào. Đây là trải nghiệm cốt lõi (P0) của ứng dụng học trực tuyến, giúp nối liền mạch từ lúc xác thực đến lúc bắt đầu học.

## Không làm trong task này
- Không làm trang chi tiết nội dung bài học (Video/Quiz).
- Không làm trang Khám phá (Explore/Catalog) để tìm khóa học mới.

## File tài liệu cần dùng
- Yêu cầu MVP: `docs/23_MVP_SCOPE.md`.
- Danh sách API: Xem lại API `GET /api/users/me/courses` và `GET /api/users/me/progress`.

## Cấu trúc luồng xử lý (Logic)
1. **Fetch Dữ liệu (Lifecycle):**
   - Khi Component `StudentDashboard.vue` được mount (hàm `onMounted`), gọi action trong Pinia hoặc trực tiếp dùng `api.js` để fetch dữ liệu từ 2 API của Backend.
2. **Quản lý Trạng thái (State Management):**
   - Cần có các state hiển thị trạng thái `isLoading`, `isError`, và chứa `data`.
   - Hiển thị Skeleton Loading hoặc Spinner trong lúc chờ dữ liệu trả về để giữ UX mượt mà.
3. **Hiển thị Giao diện (Render):**
   - Phân chia Layout: Khu vực trên cùng hiển thị thẻ Thống kê (Tổng số khóa, % Hoàn thành). Khu vực dưới hiển thị Grid/List danh sách `MyCourseCard`.
   - Nếu mảng `enrolledCourses` rỗng, hiển thị trạng thái Empty State ("Bạn chưa ghi danh khóa học nào, hãy khám phá ngay").

## Cần tạo hoặc chỉnh sửa
- `src/pages/student/DashboardPage.vue`: Trang chính lắp ráp các component.
- `src/components/student/ProgressOverviewCard.vue`: Component thẻ thống kê nhỏ.
- `src/components/student/MyCourseCard.vue`: Component hiển thị 1 khóa học đang học dở (Bao gồm tên khóa, thanh progress bar, tên bài học tiếp theo).
- `src/services/student.service.js`: Tạo service chuyên gọi các API liên quan đến học viên.

## Checklist
- [ ] Tạo `student.service.js` với các hàm `getDashboardProgress()` và `getMyCourses()`.
- [ ] Khởi tạo các Component giao diện cơ bản (Thẻ thống kê, Thẻ khóa học).
- [ ] Gắn kết API vào `DashboardPage.vue`, xử lý hiệu ứng Loading/Error.
- [ ] Thiết kế Empty State cho trường hợp User mới tinh chưa có khóa học nào.
- [ ] Test hiển thị thanh Progress Bar đảm bảo render đúng số phần trăm (%).

## Kết quả mong muốn
Học viên sau khi đăng nhập sẽ được điều hướng vào Dashboard, nhìn thấy bảng tiến độ học tập và danh sách khóa học của mình được tải lên nhanh chóng, giao diện tương thích responsive tốt.