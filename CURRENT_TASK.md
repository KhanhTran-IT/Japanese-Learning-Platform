# CURRENT TASK

## Task hiện tại
Backend Admin Dashboard API

## Trạng thái
TODO

## Mục tiêu
Xây dựng API `GET /api/v1/admin/dashboard` để trả dữ liệu tổng quan cho màn hình Admin Dashboard đã làm ở frontend, gồm tổng số user, tổng số khóa học, tổng số bài học, tổng số lượt ghi danh, danh sách user mới gần đây và danh sách khóa học mới gần đây.

## Vì sao làm task này?
Frontend Admin Dashboard hiện đã có UI và service gọi API, nhưng backend chưa có endpoint thật nên `admin.service.js` đang phải dùng mock data khi gặp 404. Task này giúp thay mock bằng dữ liệu thật từ database, hoàn thiện một phần P0 của MVP và giúp admin theo dõi hệ thống bằng số liệu thực tế.

## Không làm trong task này
- Không làm biểu đồ doanh thu, báo cáo nâng cao hoặc export Excel.
- Không làm CRUD user, khóa học, chương học hoặc bài học.
- Không làm quản lý order/payment.
- Không sửa lớn frontend, chỉ chỉnh nhẹ endpoint/response mapping nếu backend response khác hiện tại.
- Không tạo bảng database mới nếu dữ liệu có thể lấy từ các bảng đã có.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/24_USER_FLOWS.md`
- `docs/25_SCREEN_LIST.md`
- `docs/26_API_PRIORITY.md`
- `docs/27_DATABASE_PHASES.md`
- `docs/29_ERROR_CODE_STANDARD.md`
- `docs/30_PERMISSION_MATRIX.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/08_api/08_10_ADMIN_API.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`

## API cần làm
```http
GET /api/v1/admin/dashboard
Authorization: Bearer <accessToken>
```

Quyền truy cập:
- `ADMIN`
- `SUPER_ADMIN`

## Request mẫu
Không có request body.

## Response mong muốn
```json
{
  "code": 1000,
  "message": "Lấy dữ liệu dashboard thành công",
  "result": {
    "totalUsers": 156,
    "totalCourses": 24,
    "totalLessons": 142,
    "totalEnrollments": 850,
    "recentUsers": [
      {
        "id": 1,
        "fullName": "Nguyễn Văn A",
        "email": "student@example.com",
        "role": "STUDENT",
        "createdAt": "2026-07-17T10:00:00"
      }
    ],
    "recentCourses": [
      {
        "id": 1,
        "title": "N5 nhập môn cho người mới bắt đầu",
        "teacherName": "Teacher Demo",
        "isPublished": true,
        "createdAt": "2026-07-17T10:00:00"
      }
    ]
  }
}
```

## Logic xử lý
- Tạo DTO response cho dashboard, recent user và recent course.
- Tạo service `AdminDashboardService` để gom logic query và mapping DTO.
- Dùng `UserRepository.count()` để lấy tổng user.
- Dùng `CourseRepository.count()` để lấy tổng khóa học.
- Dùng `LessonRepository.count()` để lấy tổng bài học.
- Dùng `CourseEnrollmentRepository.count()` để lấy tổng lượt ghi danh.
- Query 5 user mới nhất theo `createdAt DESC`.
- Query 5 khóa học mới nhất theo `createdAt DESC`, kèm teacher để lấy `teacherName` nếu có.
- Với recent user, lấy role chính để frontend hiển thị. Nếu user có nhiều role, ưu tiên `SUPER_ADMIN`, `ADMIN`, `TEACHER`, `CONTENT_EDITOR`, rồi `STUDENT`.
- Với recent course, map `isPublished = course.status == PUBLISHED`.
- Controller chỉ nhận request và trả `ApiResponse`, không đặt logic query trong controller.
- Bảo vệ endpoint bằng `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")`.

## Cần tạo hoặc chỉnh sửa
- `backend/src/main/java/com/japaneselearning/module_admin/controller/AdminDashboardController.java`
- `backend/src/main/java/com/japaneselearning/module_admin/service/AdminDashboardService.java`
- `backend/src/main/java/com/japaneselearning/module_admin/service/AdminDashboardServiceImpl.java`
- `backend/src/main/java/com/japaneselearning/module_admin/dto/AdminDashboardRes.java`
- `backend/src/main/java/com/japaneselearning/module_admin/dto/RecentUserRes.java`
- `backend/src/main/java/com/japaneselearning/module_admin/dto/RecentCourseRes.java`
- `backend/src/main/java/com/japaneselearning/module_user/repository/UserRepository.java`
- `backend/src/main/java/com/japaneselearning/module_course/repository/CourseRepository.java`
- `frontend/src/services/admin.service.js` nếu cần bỏ mock hoặc điều chỉnh response mapping sau khi API thật chạy ổn.

## Error code cần dùng
- `ROLE_001` hoặc cơ chế Spring Security hiện có cho trường hợp không có quyền.
- `SYS_001` cho lỗi hệ thống ngoài dự kiến nếu project đang map lỗi global theo chuẩn này.

## Checklist
- [ ] Tạo module/package `module_admin` đúng cấu trúc controller/service/dto.
- [ ] Tạo DTO response, không trả Entity trực tiếp ra API.
- [ ] Bổ sung repository method để lấy recent user và recent course.
- [ ] Implement `AdminDashboardServiceImpl` để count và map dữ liệu.
- [ ] Tạo `AdminDashboardController` với endpoint `GET /api/v1/admin/dashboard`.
- [ ] Thêm `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")`.
- [ ] Kiểm tra response đúng format `ApiResponse`.
- [ ] Test ADMIN gọi API thành công.
- [ ] Test STUDENT gọi API bị 403.
- [ ] Test không có token gọi API bị 401.
- [ ] Chạy lại frontend Admin Dashboard để đảm bảo không còn dùng mock khi backend API tồn tại.

## Cách test sau khi hoàn thành
1. Chạy backend Spring Boot.
2. Đăng nhập bằng tài khoản admin để lấy access token.
3. Gọi `GET /api/v1/admin/dashboard` bằng Swagger hoặc Postman với token admin.
4. Kiểm tra response có đủ `totalUsers`, `totalCourses`, `totalLessons`, `totalEnrollments`, `recentUsers`, `recentCourses`.
5. Đăng nhập bằng tài khoản student và gọi lại API, kỳ vọng bị 403.
6. Gọi API không có token, kỳ vọng bị 401.
7. Chạy frontend, đăng nhập admin và mở `/admin/dashboard`, kỳ vọng dashboard hiển thị dữ liệu thật.

## Kết quả mong muốn
Backend có API dashboard thật cho admin, được phân quyền đúng, trả DTO sạch, không lộ Entity, frontend admin dashboard có thể dùng dữ liệu database thay cho mock data.
