# CURRENT TASK

## Task hiện tại
Frontend Public Course Detail Page & API Integration

## Trạng thái
TODO

## Mục tiêu
Hoàn thiện trang chi tiết khóa học public `/courses/:slug` bằng Vue 3 và tích hợp API thật `GET /api/v1/courses/{slug}`. Trang cần hiển thị thông tin khóa học, giảng viên, giá, thống kê cơ bản, mô tả chi tiết và danh sách chương/bài học nếu response backend có dữ liệu.

## Vì sao làm task này?
CourseListPage đã có link sang `/courses/:slug`, nhưng `CourseDetailPage.vue` hiện mới là placeholder. Người dùng cần một trang chi tiết để xem nội dung khóa học trước khi đăng ký học miễn phí hoặc mua khóa học trả phí. Đây là bước bắt buộc trước enrollment/payment flow.

## Không làm trong task này
- Không làm enrollment API.
- Không làm payment/checkout.
- Không làm lesson learning page.
- Không làm review/rating CRUD.
- Không sửa backend nếu endpoint detail theo slug đã hoạt động đúng.
- Không tạo mock data cố định thay cho API thật.
- Không bắt buộc hiển thị curriculum nếu backend detail response chưa trả sections/lessons.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/24_USER_FLOWS.md`
- `docs/25_SCREEN_LIST.md`
- `docs/26_API_PRIORITY.md`
- `docs/10_FRONTEND_STRUCTURE.md`
- `docs/11_BACKEND_FRONTEND_CONFIG.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/05_features/05_02_COURSE_FEATURES.md`
- `docs/07_database/07_02_COURSE_LESSON.md`
- `docs/08_api/08_03_COURSE_PUBLIC_API.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`

## API cần tích hợp
```http
GET /api/v1/courses/{slug}
```

Ví dụ:
```http
GET /api/v1/courses/n5-nhap-mon-cho-nguoi-moi-bat-dau
```

## Response mong muốn
Response dự kiến là `ApiResponse<CourseDetailRes>` hoặc DTO tương đương backend hiện có.

Frontend cần ưu tiên map các field nếu có:
- `id`
- `title`
- `slug`
- `shortDescription`
- `description`
- `thumbnailUrl`
- `level`
- `courseType`
- `originalPrice`
- `salePrice`
- `averageRating`
- `totalStudents`
- `teacherName`
- `teacherAvatarUrl`
- `totalDurationMinutes`
- `totalLessons`
- `sections` hoặc `curriculum` nếu backend trả về

## Logic xử lý
- Lấy `slug` từ route params.
- Gọi `CourseService.getCourseBySlug(slug)` khi page mounted hoặc khi slug thay đổi.
- Hiển thị loading state khi đang gọi API.
- Hiển thị error state khi API lỗi, có nút thử lại và link quay lại danh sách khóa học.
- Hiển thị 404/not found friendly state nếu backend trả lỗi không tìm thấy.
- Render hero/course overview với thumbnail, title, short description, level và course type.
- Render price box:
  - `FREE` hiển thị "Miễn phí".
  - `PAID` hiển thị `salePrice` nếu có, kèm `originalPrice` gạch ngang nếu giảm giá.
- Render thông tin giảng viên nếu có `teacherName`/`teacherAvatarUrl`.
- Render stats như số bài học, tổng thời lượng, số học viên, rating nếu có dữ liệu.
- Render mô tả chi tiết bằng text/html an toàn theo dữ liệu hiện có.
- Render curriculum/sections nếu response có dữ liệu; nếu chưa có thì hiển thị thông tin tổng quan số bài học.
- Nút CTA:
  - Course `FREE`: hiển thị "Đăng ký học miễn phí" nhưng có thể disabled/placeholder nếu enrollment chưa làm.
  - Course `PAID`: hiển thị "Mua khóa học" nhưng có thể disabled/placeholder nếu payment chưa làm.
- Có link quay lại `/courses`.

## Cần tạo hoặc chỉnh sửa
- `frontend/src/pages/public/CourseDetailPage.vue`
- `frontend/src/services/course.service.js` nếu cần chỉnh mapping hoặc tên method.
- `frontend/src/router/index.js` nếu route detail chưa đúng.
- Có thể tạo component nhỏ trong `frontend/src/components/course/` nếu giúp page gọn hơn.

## Error code cần xử lý
- `COURSE_NOT_FOUND` hoặc lỗi 404 tương đương nếu backend có.
- `INVALID_REQUEST` nếu slug không hợp lệ.
- Lỗi network/server chung qua `getApiErrorMessage`.

## Checklist
- [ ] Route `/courses/:slug` hiển thị page detail thật, không còn placeholder.
- [ ] Page lấy `slug` từ route params.
- [ ] Page gọi đúng `GET /api/v1/courses/{slug}` qua `CourseService`.
- [ ] Loading state hoạt động.
- [ ] Error/not found state hoạt động.
- [ ] Hiển thị thông tin chính của course.
- [ ] Hiển thị giá/free badge đúng.
- [ ] Hiển thị teacher/stats nếu có dữ liệu.
- [ ] Có CTA phù hợp nhưng không làm enrollment/payment thật.
- [ ] Có link quay lại `/courses`.
- [ ] Không dùng mock data cố định.
- [ ] Chạy `npm run build`.

## Cách test sau khi hoàn thành
1. Chạy backend Spring Boot.
2. Chạy frontend dev server.
3. Mở `/courses`.
4. Click một course để vào `/courses/:slug`.
5. Kiểm tra page detail gọi API thật và hiển thị đúng dữ liệu.
6. Mở một slug không tồn tại để kiểm tra error/not found state.
7. Kiểm tra course `FREE` và `PAID` nếu có dữ liệu.
8. Chạy `npm run build`.

## Kết quả mong muốn
Người dùng public có thể xem chi tiết một khóa học từ danh sách course, hiểu nội dung/giá/giảng viên trước khi đăng ký hoặc mua. Trang detail sẵn sàng làm nền cho task tiếp theo là Free Course Enrollment API/UI.
