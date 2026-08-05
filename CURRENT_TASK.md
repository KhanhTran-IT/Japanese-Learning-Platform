# CURRENT TASK

## Task hiện tại
Frontend Student My Courses Page & Navigation

## Trạng thái
TODO

## Mục tiêu
Tạo trang riêng `/student/my-courses` để student xem danh sách khóa học đã ghi danh bằng API thật `GET /api/users/me/courses`, có loading/error/empty state, course cards có tiến độ học và nút tiếp tục học. Cập nhật navigation trong StudentLayout để trỏ đúng route này.

## Vì sao làm task này?
Sau khi student ghi danh khóa học miễn phí, hệ thống cần một nơi rõ ràng để họ xem các khóa học đã enroll và tiếp tục học. Hiện StudentDashboard đã có section nhỏ "Khóa học của tôi", nhưng MVP screen list cần MyCoursesPage riêng để chuẩn bị cho LearningPage và progress flow.

## Không làm trong task này
- Không làm lesson learning page mới.
- Không làm update progress/complete lesson.
- Không làm enrollment backend.
- Không làm payment/order.
- Không làm filter/search nâng cao cho my courses nếu API chưa hỗ trợ.
- Không sửa backend nếu `GET /api/users/me/courses` đã hoạt động đúng.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/24_USER_FLOWS.md`
- `docs/25_SCREEN_LIST.md`
- `docs/26_API_PRIORITY.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/05_features/05_02_COURSE_FEATURES.md`
- `docs/07_database/07_02_COURSE_LESSON.md`
- `docs/10_FRONTEND_STRUCTURE.md`
- `docs/11_BACKEND_FRONTEND_CONFIG.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`

## API cần tích hợp
```http
GET /api/users/me/courses
Authorization: Bearer <accessToken>
```

Service hiện có:
```js
StudentService.getMyCourses()
```

## Response mong muốn
Response dự kiến là `ApiResponse<List<MyCourseRes>>`.

Frontend cần ưu tiên map các field nếu có:
- `courseId`
- `courseName`
- `slug`
- `thumbnailUrl`
- `progressPercent`
- `completedLessons`
- `totalLessons`
- `lastLessonName`
- `lastLessonSlug`
- `enrolledAt`

## Logic xử lý
- Tạo `frontend/src/pages/student/MyCoursesPage.vue`.
- Thêm route `/student/my-courses` dưới `StudentLayout`, yêu cầu auth role `STUDENT`.
- Cập nhật `StudentLayout.vue` menu "Khóa học của tôi" trỏ tới `/student/my-courses`.
- Gọi `StudentService.getMyCourses()` khi mounted.
- Hiển thị loading state khi đang tải.
- Hiển thị error state khi API lỗi, có nút thử lại.
- Hiển thị empty state nếu chưa enroll course nào, có link sang `/courses`.
- Hiển thị grid/list course đã ghi danh, có progress bar.
- Reuse `MyCourseCard.vue` nếu phù hợp; chỉnh component nếu cần để không phụ thuộc dashboard.
- Nút "Học tiếp"/"Bắt đầu học":
  - Nếu có `lastLessonSlug`, đi tới `/student/lessons/{lastLessonSlug}`.
  - Nếu chưa có lesson, đi tới `/courses/{slug}` hoặc hiển thị message phù hợp.
- Không dùng mock data cố định.

## Cần tạo hoặc chỉnh sửa
- `frontend/src/pages/student/MyCoursesPage.vue`
- `frontend/src/router/index.js`
- `frontend/src/layouts/StudentLayout.vue`
- `frontend/src/components/student/MyCourseCard.vue` nếu cần chỉnh nhỏ.
- `frontend/src/pages/student/StudentDashboardPage.vue` nếu cần link sang page mới.

## Error code cần xử lý
- Lỗi 401/403 nếu token hết hạn hoặc không đúng role.
- Lỗi network/server chung qua `getApiErrorMessage` nếu page đã dùng helper.

## Checklist
- [ ] Route `/student/my-courses` hoạt động.
- [ ] StudentLayout menu trỏ đúng `/student/my-courses`.
- [ ] Page gọi `StudentService.getMyCourses()`.
- [ ] Loading state hoạt động.
- [ ] Error state hoạt động.
- [ ] Empty state có link sang `/courses`.
- [ ] Course cards hiển thị progress rõ ràng.
- [ ] Nút continue điều hướng theo `lastLessonSlug` hoặc fallback hợp lý.
- [ ] Không dùng mock data cố định.
- [ ] Chạy `npm run build`.

## Cách test sau khi hoàn thành
1. Chạy backend Spring Boot.
2. Chạy frontend dev server.
3. Đăng nhập bằng tài khoản STUDENT.
4. Mở `/student/my-courses`.
5. Nếu chưa có course, kiểm tra empty state và link khám phá khóa học.
6. Ghi danh một course FREE rồi quay lại `/student/my-courses`.
7. Kiểm tra course card hiển thị tiến độ/số bài học.
8. Click "Học tiếp" hoặc "Bắt đầu học" và kiểm tra điều hướng.
9. Chạy `npm run build`.

## Kết quả mong muốn
Student có một trang riêng để xem toàn bộ khóa học đã ghi danh và tiếp tục học. Luồng sau enrollment rõ ràng hơn và sẵn sàng nối tiếp sang LearningPage/progress.
