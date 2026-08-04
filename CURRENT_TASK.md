# CURRENT TASK

## Task hiện tại
Frontend Free Course Enrollment Integration

## Trạng thái
TODO

## Mục tiêu
Tích hợp chức năng ghi danh khóa học miễn phí trên `CourseDetailPage.vue` bằng API thật `POST /api/v1/courses/{courseId}/enroll`. Khi người dùng là STUDENT đã đăng nhập và khóa học là `FREE`, nút "Đăng ký học miễn phí" cần gọi API, hiển thị trạng thái xử lý/thành công/lỗi và điều hướng hợp lý sau khi ghi danh.

## Vì sao làm task này?
CourseDetailPage đã hiển thị thông tin khóa học nhưng CTA ghi danh đang disabled. Backend enrollment API đã tồn tại và có rule nghiệp vụ cho course `PUBLISHED`, `FREE`, chống ghi danh trùng. Bước tiếp theo là nối UI với API này để student thật sự bắt đầu học khóa miễn phí.

## Không làm trong task này
- Không làm payment/checkout cho khóa `PAID`.
- Không làm lesson learning page mới.
- Không làm backend enrollment API nếu endpoint hiện tại đã hoạt động.
- Không làm review/rating.
- Không làm quản lý đơn hàng.
- Không làm kiểm tra trạng thái đã ghi danh nâng cao nếu backend chưa có endpoint riêng cho detail state.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/24_USER_FLOWS.md`
- `docs/25_SCREEN_LIST.md`
- `docs/26_API_PRIORITY.md`
- `docs/30_PERMISSION_MATRIX.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/05_features/05_02_COURSE_FEATURES.md`
- `docs/07_database/07_02_COURSE_LESSON.md`
- `docs/08_api/08_03_COURSE_PUBLIC_API.md`
- `docs/10_FRONTEND_STRUCTURE.md`
- `docs/11_BACKEND_FRONTEND_CONFIG.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`

## API cần tích hợp
```http
POST /api/v1/courses/{courseId}/enroll
Authorization: Bearer <accessToken>
```

Ví dụ:
```http
POST /api/v1/courses/1/enroll
```

## Response mong muốn
```json
{
  "code": 1000,
  "message": "Ghi danh thành công",
  "result": null
}
```

## Logic xử lý
- Thêm method public service, ví dụ `CourseService.enrollFreeCourse(courseId)`.
- Trong `CourseDetailPage.vue`, bật CTA chỉ khi:
  - đã load được course.
  - `course.courseType === 'FREE'`.
  - có `course.id`.
- Nếu user chưa đăng nhập:
  - chuyển sang `/login`.
  - nên giữ redirect query nếu project đã có pattern, ví dụ `/login?redirect=/courses/{slug}`.
- Nếu user đã đăng nhập nhưng không phải STUDENT:
  - hiển thị thông báo phù hợp hoặc không cho ghi danh.
- Khi bấm ghi danh:
  - disable nút trong lúc request đang chạy.
  - gọi `POST /api/v1/courses/{courseId}/enroll`.
  - nếu thành công, hiển thị success message.
  - có thể đổi CTA thành "Vào học" hoặc "Xem khóa học của tôi".
  - điều hướng hợp lý tới `/student/dashboard` hoặc route học hiện có nếu project đã có.
- Nếu backend trả `USER_ALREADY_ENROLLED`, hiển thị message rõ và cho user đi tới dashboard/my courses.
- Nếu backend trả `COURSE_CANNOT_ENROLL_PAID`, không gọi API cho course `PAID`; giữ CTA "Mua khóa học" ở placeholder.
- Nếu backend trả `COURSE_NOT_AVAILABLE_FOR_ENROLLMENT`, hiển thị lỗi rõ.
- Không nuốt lỗi API, dùng `getApiErrorMessage`.

## Cần tạo hoặc chỉnh sửa
- `frontend/src/services/course.service.js`
- `frontend/src/pages/public/CourseDetailPage.vue`
- Có thể chỉnh `frontend/src/stores/auth.store.js` hoặc router login redirect nếu thật sự cần và đúng pattern hiện có.

## Error code cần xử lý
- `USER_ALREADY_ENROLLED`
- `COURSE_NOT_AVAILABLE_FOR_ENROLLMENT`
- `COURSE_CANNOT_ENROLL_PAID`
- `USER_NOT_FOUND`
- Lỗi 401/403 khi chưa đăng nhập hoặc không đúng role.

## Checklist
- [ ] `CourseService` có method gọi `POST /api/v1/courses/{courseId}/enroll`.
- [ ] Nút ghi danh FREE không còn disabled cố định.
- [ ] Course PAID không gọi enroll API.
- [ ] Chưa đăng nhập thì chuyển tới login.
- [ ] Đang gọi API thì nút disable/loading.
- [ ] Thành công thì hiển thị message rõ.
- [ ] Ghi danh trùng hiển thị message rõ.
- [ ] Lỗi backend/network hiển thị qua `getApiErrorMessage`.
- [ ] Không làm payment trong task này.
- [ ] Chạy `npm run build`.

## Cách test sau khi hoàn thành
1. Chạy backend Spring Boot.
2. Chạy frontend dev server.
3. Mở một course `FREE` ở `/courses/:slug`.
4. Khi chưa đăng nhập, bấm ghi danh và kiểm tra redirect login.
5. Đăng nhập bằng STUDENT.
6. Bấm ghi danh course `FREE`, kỳ vọng API trả thành công và UI báo thành công.
7. Bấm lại course đã ghi danh, kỳ vọng UI hiển thị lỗi/notice đã ghi danh.
8. Mở course `PAID`, kỳ vọng không gọi enroll API.
9. Chạy `npm run build`.

## Kết quả mong muốn
Student có thể ghi danh khóa học miễn phí từ Course Detail bằng API thật. UI xử lý rõ các trạng thái chưa đăng nhập, đang gửi request, thành công, ghi danh trùng và lỗi backend. Chức năng sẵn sàng nối tiếp sang trang học bài/progress.
