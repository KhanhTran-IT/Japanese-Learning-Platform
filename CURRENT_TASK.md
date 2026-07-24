# CURRENT TASK

## Task hiện tại
Backend Public Course List Search & Filter API

## Trạng thái
TODO

## Mục tiêu
Hoàn thiện API danh sách khóa học public `GET /api/v1/courses` để hỗ trợ đầy đủ nhu cầu MVP: chỉ trả khóa học `PUBLISHED`, có phân trang, filter theo level, filter theo loại khóa học `FREE/PAID`, và tìm kiếm theo keyword trong title/shortDescription.

## Vì sao làm task này?
Admin đã có thể tạo khóa học, tạo chương và bài học. Bước tiếp theo là đưa dữ liệu khóa học ra public để guest/student xem. Backend public course list hiện mới hỗ trợ filter theo `level`, chưa có search keyword và filter `courseType`, trong khi MVP yêu cầu danh sách khóa học có tìm kiếm và lọc miễn phí/trả phí. Cần hoàn thiện API trước khi làm frontend `CourseListPage`.

## Không làm trong task này
- Không làm frontend CourseListPage.
- Không làm course detail API vì endpoint detail theo slug đã có.
- Không làm enrollment.
- Không làm lesson learning page.
- Không làm sort nâng cao theo rating/price nếu chưa cần.
- Không trả course `DRAFT`, `HIDDEN`, `ARCHIVED` ra public.
- Không thay đổi response DTO nếu `CoursePublicRes` hiện tại đã đủ.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/24_USER_FLOWS.md`
- `docs/25_SCREEN_LIST.md`
- `docs/26_API_PRIORITY.md`
- `docs/28_ENUM_DEFINITIONS.md`
- `docs/29_ERROR_CODE_STANDARD.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/05_features/05_02_COURSE_FEATURES.md`
- `docs/07_database/07_02_COURSE_LESSON.md`
- `docs/08_api/08_03_COURSE_PUBLIC_API.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`

## API cần làm
```http
GET /api/v1/courses?page=0&size=12&keyword=n5&level=N5&courseType=FREE
```

Query params:
- `page`: số trang, mặc định 0.
- `size`: số item mỗi trang, mặc định theo pageable hiện có.
- `keyword`: optional, tìm theo `title` hoặc `shortDescription`.
- `level`: optional, enum `CourseLevel`.
- `courseType`: optional, enum `CourseType`.

## Request mẫu
```http
GET /api/v1/courses?keyword=n5&level=N5&courseType=FREE&page=0&size=12
```

```http
GET /api/v1/courses?courseType=PAID&page=0&size=12
```

```http
GET /api/v1/courses
```

## Response mong muốn
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

## Logic xử lý
- Cập nhật `CoursePublicController.getPublishedCourses()` để nhận thêm query param `keyword` và `courseType`.
- Parse `level` sang `CourseLevel` nếu backend hiện đang nhận String.
- Parse `courseType` sang `CourseType`.
- Nếu enum không hợp lệ, trả lỗi validation/bad request rõ ràng, không để lỗi 500.
- Cập nhật `CoursePublicService` và `CoursePublicServiceImpl` để truyền keyword/level/courseType xuống repository.
- Cập nhật `CourseRepository` bằng JPQL query hoặc method phù hợp:
  - luôn filter `status = PUBLISHED`.
  - nếu `keyword` rỗng/null thì bỏ qua keyword.
  - nếu `level` null thì bỏ qua level.
  - nếu `courseType` null thì bỏ qua courseType.
  - keyword tìm trong `title` hoặc `shortDescription`, không phân biệt hoa thường.
- Giữ `@EntityGraph(attributePaths = {"teacher"})` hoặc query fetch teacher nếu cần để tránh N+1 khi map `teacherName`.
- Response vẫn dùng `ApiResponse<Page<CoursePublicRes>>`.
- Không trả Entity trực tiếp.

## Cần tạo hoặc chỉnh sửa
- `backend/src/main/java/com/japaneselearning/module_course/controller/CoursePublicController.java`
- `backend/src/main/java/com/japaneselearning/module_course/service/CoursePublicService.java`
- `backend/src/main/java/com/japaneselearning/module_course/service/CoursePublicServiceImpl.java`
- `backend/src/main/java/com/japaneselearning/module_course/repository/CourseRepository.java`
- Có thể chỉnh `backend/src/main/java/com/japaneselearning/common/exception/ErrorCode.java` nếu cần error rõ hơn cho enum query param không hợp lệ.

## Error code cần dùng
- `VALIDATION_ERROR` hoặc `INVALID_REQUEST` khi query param enum không hợp lệ.
- `UNCATEGORIZED_EXCEPTION` không nên xảy ra cho input sai từ client.

## Checklist
- [ ] Controller nhận `keyword`, `level`, `courseType`, `page`, `size`.
- [ ] Service nhận và xử lý filter public course list.
- [ ] Repository query chỉ trả course `PUBLISHED`.
- [ ] Filter keyword theo title/shortDescription không phân biệt hoa thường.
- [ ] Filter level hoạt động.
- [ ] Filter courseType `FREE/PAID` hoạt động.
- [ ] Query không truyền filter vẫn trả danh sách published courses.
- [ ] Response vẫn là `ApiResponse<Page<CoursePublicRes>>`.
- [ ] Không trả Entity trực tiếp.
- [ ] Chạy `mvn test`.

## Cách test sau khi hoàn thành
1. Chạy backend Spring Boot.
2. Tạo dữ liệu course gồm `PUBLISHED`, `DRAFT`, `HIDDEN`.
3. Gọi `GET /api/v1/courses`, kỳ vọng chỉ thấy `PUBLISHED`.
4. Gọi `GET /api/v1/courses?level=N5`, kỳ vọng chỉ thấy khóa học N5 published.
5. Gọi `GET /api/v1/courses?courseType=FREE`, kỳ vọng chỉ thấy khóa học miễn phí published.
6. Gọi `GET /api/v1/courses?keyword=n5`, kỳ vọng tìm theo title/shortDescription.
7. Gọi kết hợp `keyword + level + courseType`, kỳ vọng filter đúng.
8. Gọi `level=INVALID`, kỳ vọng lỗi 400 rõ ràng, không phải 500.
9. Chạy `mvn test`.

## Kết quả mong muốn
Public Course List API hỗ trợ đầy đủ search/filter cơ bản cho MVP, sẵn sàng để frontend `CourseListPage` tích hợp danh sách khóa học public.
