# CURRENT TASK

## Task hiện tại
Frontend Public Course List Page & API Integration

## Trạng thái
TODO

## Mục tiêu
Xây dựng trang danh sách khóa học public `/courses` bằng Vue 3 và tích hợp API thật `GET /api/v1/courses`. Trang cần hiển thị khóa học đã publish, có search keyword, filter theo level, filter theo loại khóa học `FREE/PAID`, phân trang, loading/error/empty state và card khóa học rõ ràng cho guest/student.

## Vì sao làm task này?
Backend public course list đã hỗ trợ `keyword`, `level`, `courseType` và chỉ trả course `PUBLISHED`. Bước tiếp theo là đưa dữ liệu này lên giao diện public để người học có thể xem, tìm kiếm và lọc khóa học thật thay vì dùng dữ liệu mock hoặc placeholder.

## Không làm trong task này
- Không làm course detail page đầy đủ.
- Không làm enrollment.
- Không làm payment/checkout.
- Không làm lesson learning page.
- Không làm advanced sort theo rating/price nếu chưa có trong backend.
- Không tạo mock data thay cho API thật.
- Không sửa backend nếu API `GET /api/v1/courses` đã hoạt động đúng.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/24_USER_FLOWS.md`
- `docs/25_SCREEN_LIST.md`
- `docs/26_API_PRIORITY.md`
- `docs/27_FRONTEND_STRUCTURE.md`
- `docs/30_BACKEND_FRONTEND_API_CONTRACT.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/05_features/05_02_COURSE_FEATURES.md`
- `docs/08_api/08_03_COURSE_PUBLIC_API.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`

## API cần tích hợp
```http
GET /api/v1/courses?page=0&size=12&keyword=&level=&courseType=
```

Query params:
- `page`: số trang, mặc định 0.
- `size`: số item mỗi trang, nên dùng 12 cho grid.
- `keyword`: optional, tìm theo `title` hoặc `shortDescription`.
- `level`: optional, enum `CourseLevel`.
- `courseType`: optional, enum `CourseType`.

Response là `ApiResponse<Page<CoursePublicRes>>`, cần map Spring Page:
```json
{
  "code": 1000,
  "message": "Lấy danh sách khóa học thành công",
  "result": {
    "content": [
      {
        "id": 1,
        "title": "N5 nhập môn cho người mới bắt đầu",
        "slug": "n5-nhap-mon-cho-nguoi-moi-bat-dau",
        "shortDescription": "Khóa học nền tảng cho người mới.",
        "thumbnailUrl": "https://example.com/thumb.jpg",
        "level": "N5",
        "courseType": "FREE",
        "originalPrice": 0,
        "salePrice": 0,
        "averageRating": 0,
        "totalStudents": 10,
        "teacherName": "Teacher Demo",
        "teacherAvatarUrl": null,
        "totalDurationMinutes": 120,
        "totalLessons": 8
      }
    ],
    "number": 0,
    "size": 12,
    "totalPages": 1,
    "totalElements": 1
  }
}
```

Các field frontend cần dùng:
- `result.content`: danh sách course.
- `result.number`: page hiện tại.
- `result.size`: page size.
- `result.totalPages`: tổng số trang.
- `result.totalElements`: tổng số course.

## UI cần có
- Route public `/courses` trong router hiện tại, dùng layout public/main hiện có.
- Search input cho `keyword`.
- Select/segmented filter cho `level`: Tất cả, N5, N4, N3, N2, N1.
- Select/segmented filter cho `courseType`: Tất cả, FREE, PAID.
- Grid card khóa học responsive.
- Card hiển thị:
  - thumbnail.
  - title.
  - shortDescription.
  - level.
  - courseType/free badge.
  - giá hoặc label miễn phí.
  - teacherName.
  - totalLessons.
  - totalDurationMinutes.
  - totalStudents/averageRating nếu có dữ liệu.
- Loading state khi đang gọi API.
- Error state khi API lỗi, có nút thử lại.
- Empty state khi không có khóa học phù hợp.
- Pagination: Previous/Next và số trang hiện tại/tổng trang.
- Khi đổi keyword/filter, reset page về 0.
- Có link/nút "Xem chi tiết" trỏ tới `/courses/:slug` hoặc placeholder hợp lý nếu detail page chưa làm.

## Cần tạo hoặc chỉnh sửa
- `frontend/src/pages/public/CourseListPage.vue`
- `frontend/src/services/course.service.js`
- `frontend/src/router/index.js`
- Có thể tạo `frontend/src/components/course/CourseCard.vue` nếu phù hợp với cấu trúc hiện tại.
- Có thể cập nhật `frontend/src/pages/public/HomePage.vue` để link tới `/courses`.

## Checklist
- [ ] Route `/courses` hoạt động.
- [ ] Course service gọi đúng `GET /api/v1/courses`.
- [ ] Page map đúng `result.content`, `result.number`, `result.totalPages`, `result.totalElements`.
- [ ] Search keyword gọi API thật.
- [ ] Filter level gọi API thật.
- [ ] Filter courseType gọi API thật.
- [ ] Đổi filter reset page về 0.
- [ ] Pagination hoạt động.
- [ ] Loading/error/empty state rõ ràng.
- [ ] Card course hiển thị đủ thông tin chính.
- [ ] Không dùng mock data cố định nếu API available.
- [ ] Chạy `npm run build`.

## Cách test sau khi hoàn thành
1. Chạy backend Spring Boot.
2. Chạy frontend dev server.
3. Mở `/courses`.
4. Kiểm tra danh sách course hiển thị từ API thật.
5. Search keyword, kỳ vọng URL/API query thay đổi và list cập nhật.
6. Filter `level=N5`, kỳ vọng chỉ thấy khóa học N5 published.
7. Filter `courseType=FREE`, kỳ vọng chỉ thấy khóa học miễn phí published.
8. Kết hợp keyword + level + courseType.
9. Click pagination Previous/Next.
10. Tắt backend hoặc gây lỗi API để kiểm tra error state.
11. Chạy `npm run build`.

## Kết quả mong muốn
Guest/student có thể vào `/courses` để xem danh sách khóa học public từ backend thật, tìm kiếm/lọc/phân trang ổn định. Task này tạo nền cho task kế tiếp là public course detail page.

## Task kế tiếp dự kiến
Frontend Public Course Detail Page & API Integration.
