# CURRENT TASK

## Task hiện tại
Admin Dashboard UI & API Integration (Giao diện và Tích hợp API Quản trị viên)

## Trạng thái
TODO

## Mục tiêu
Xây dựng giao diện tổng quan cho Quản trị viên (Admin Dashboard) hiển thị các chỉ số cốt lõi của hệ thống (tổng User, Course, Lesson, Enrollment) và danh sách các hoạt động mới nhất. Tích hợp với API `GET /api/v1/admin/dashboard` đã hoàn thiện ở Backend.

## Vì sao làm task này?
Admin cần một trung tâm điều khiển để theo dõi sức khỏe và sự tăng trưởng của nền tảng ngay khi đăng nhập. Việc có một màn hình Dashboard trực quan là yêu cầu P0 trong MVP để đáp ứng nghiệp vụ quản trị hệ thống.

## Không làm trong task này
- Không vẽ các biểu đồ phức tạp (Line chart, Bar chart) bằng thư viện bên thứ ba, chỉ dùng các thẻ số liệu cơ bản.
- Không làm các tính năng Create/Update/Delete (CRUD) cho User hay Course trong task này.
ChatGPT gets less accurate and may forget details in long conversations. Upgrade to chat longer with better memory.

## File tài liệu cần dùng
- Yêu cầu MVP: `docs/23_MVP_SCOPE.md` (Mục 3.5).
- Layout: Tái sử dụng hoặc tạo layout riêng nếu cần, nhưng phải đảm bảo tách biệt với Student Layout.

## Cấu trúc luồng xử lý (Logic)
1. **Bảo mật & Điều hướng:** 
   - Route `/admin/dashboard` phải được bảo vệ chặt chẽ bởi Navigation Guards, chỉ cho phép user có role `ADMIN` truy cập.
2. **Fetch Dữ liệu:**
   - Gọi API lấy dữ liệu thống kê từ Backend khi component mounted.
   - Xử lý mượt mà các trạng thái Loading (hiển thị khung skeleton) và Error (hiển thị thông báo nếu bị 403 Forbidden hoặc lỗi mạng).
3. **Hiển thị Giao diện (Render):**
   - **Top Cards:** 4 thẻ hiển thị số lượng tổng (Người dùng, Khóa học, Bài học, Lượt ghi danh) có icon minh họa.
   - **Recent Tables:** 2 bảng (Table) hoặc danh sách (List) đặt song song hoặc trên dưới để hiển thị `recentUsers` và `recentCourses`.

## Cần tạo hoặc chỉnh sửa
- `src/pages/admin/AdminDashboardPage.vue`: Trang chính của Admin.
- `src/components/admin/StatCard.vue`: Component thẻ số liệu có thể tái sử dụng.
- `src/services/admin.service.js`: Tạo service xử lý các API gọi dưới quyền Admin.
- `src/router/index.js`: Khai báo route cho admin và cập nhật layout (nếu sử dụng AdminLayout).

## Checklist
- [ ] Thiết lập file route cho admin dashboard và đảm bảo có meta `requiresAuth` và `role: 'ADMIN'`.
- [ ] Tạo `admin.service.js` với hàm `getDashboardStats()`.
- [ ] Thiết kế và cắt HTML/CSS cho `StatCard.vue` và các bảng dữ liệu gần đây.
- [ ] Tích hợp API vào `AdminDashboardPage.vue`, xử lý đầy đủ try/catch.
- [ ] Test trường hợp 1: Đăng nhập bằng Admin -> Vào được Dashboard và thấy số liệu.
- [ ] Test trường hợp 2: Đăng nhập bằng Student -> Cố tình truy cập `/admin/dashboard` -> Bị đá văng ra ngoài hoặc hiện màn hình 403.

## Kết quả mong muốn
Hoàn thiện giao diện điều khiển cho Quản trị viên, dữ liệu hiển thị chính xác, layout sắc nét và hệ thống phân quyền Frontend hoạt động hiệu quả.