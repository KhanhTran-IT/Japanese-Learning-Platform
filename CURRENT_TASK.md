# CURRENT TASK

## Task hiện tại

Student Dashboard & My Learning APIs

## Trạng thái

DONE
Ngày hoàn thành: 01/07/2026

## Mục tiêu

Xây dựng các API backend cho màn hình học viên tổng quan, cho phép user xem danh sách khóa học đã ghi danh, tổng quan tiến độ học tập, và trạng thái bài học gần nhất. Task này là nền tảng cho các màn hình Student Dashboard, My Courses, và Learning Overview.

## Vì sao làm task này?

Sau khi lesson progress API đã sẵn sàng, hệ thống cần cung cấp dữ liệu “tổng quan” cho học viên để họ thấy mình đang học gì, đã hoàn thành bao nhiêu phần, và tiếp tục bài nào. Đây là bước nối giữa backend learning flow và giao diện student, đồng thời là phần quan trọng trong MVP để tạo trải nghiệm học tập liên tục.

## Không làm trong task này

- Không làm quiz, payment, gamification.
- Không làm admin dashboard analytics.
- Không làm real-time notification.
- Không làm chỉnh sửa profile hay đổi mật khẩu.

## File tài liệu cần dùng

- docs/08_api/08_02_USER_API.md
- docs/05_features/05_03_LEARNING_PROGRESS_FEATURES.md
- docs/25_SCREEN_LIST.md
- docs/26_API_PRIORITY.md

## API cần làm

- GET /api/users/me/courses
  - Trả về danh sách khóa học mà student đã enroll/đang học.
- GET /api/users/me/progress
  - Trả về tổng quan tiến độ học tập: số khóa đang học, số bài đã hoàn thành, tiến độ từng khóa, bài học gần nhất.

## Request mẫu

```http
GET /api/users/me/courses
Authorization: Bearer <access_token>
```

```http
GET /api/users/me/progress
Authorization: Bearer <access_token>
```

## Response mong muốn

```json
{
  "data": {
    "enrolledCourses": [
      {
        "courseId": 1,
        "courseName": "N5 Grammar Basics",
        "slug": "n5-grammar-basics",
        "progressPercent": 45.5,
        "completedLessons": 9,
        "totalLessons": 20,
        "lastLessonName": "Lesson 10",
        "enrolledAt": "2026-06-29"
      }
    ]
  }
}
```

## Logic xử lý

- Lấy `userId` hiện tại từ `SecurityContext` thay vì nhận từ request body.
- Query các khóa học mà student đã ghi danh (`course_enrollments`).
- Với mỗi khóa học, tính tiến độ dựa trên số bài học đã hoàn thành và tổng số bài học của khóa.
- Nếu học viên chưa có progress nào thì giá trị tiến độ là `0`.
- Trả về dữ liệu theo đúng phạm vi của user hiện tại, không leak dữ liệu của user khác.

## Cần tạo hoặc chỉnh sửa

- `StudentDashboardController`
- `StudentDashboardService` / `StudentProgressService`
- DTO response cho `/api/users/me/courses` và `/api/users/me/progress`
- Repository query cho `course_enrollments`, `lesson_progress`, `lessons`
- Nếu cần, custom mapper hoặc projection để tính progress hiệu quả

## Error code cần dùng

- `AUTH_003`: Forbidden / không có quyền truy cập dữ liệu học viên
- `VALID_001`: Validation error nếu có request param không hợp lệ
- `COURSE_001`: Course not found nếu cần truy xuất chi tiết cụ thể

## Checklist

- [ ] Xây dựng endpoint lấy danh sách khóa học đã enroll của current user
- [ ] Xây dựng endpoint tổng quan tiến độ học tập cho current user
- [ ] Tính progress dựa trên lesson_progress và tổng số lesson thực tế
- [ ] Đảm bảo dữ liệu thuộc đúng user hiện tại và không lộ cho user khác
- [ ] Test với account chưa enroll, account đã enroll, và account chưa có progress

## Cách test sau khi hoàn thành

1. Đăng nhập bằng tài khoản student đã enroll ít nhất 1 khóa.
2. Gọi GET /api/users/me/courses và kiểm tra danh sách khóa học trả về đúng.
3. Gọi GET /api/users/me/progress và kiểm tra số liệu progress có logic hợp lý.
4. Kiểm tra 1 account khác không thể xem dữ liệu của account kia.

## Kết quả mong muốn

Học viên có thể xem được dashboard học tập cá nhân, biết mình đang học khóa nào, đã tiến bộ đến đâu, và có cơ sở để tiếp tục học trên UI.
