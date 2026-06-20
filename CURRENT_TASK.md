# CURRENT_TASK.md mới

# CURRENT TASK

## Task hiện tại

Course/Lesson Database Foundation

## Trạng thái

DONE
Ngày hoàn thành: 20/06/2026

## Mục tiêu

Tạo nền tảng database/entity/repository cho module Course và Lesson. Đây là module lõi của website học tiếng Nhật, dùng để quản lý khóa học, chương học, bài học và tài nguyên bài học.

## Vì sao làm task này?

Sau khi Auth và Security core đã hoàn thành, hệ thống đã có user, role, JWT authentication và role-based authorization. Bước tiếp theo là xây dựng module chính của nền tảng học online: Course/Lesson.

Course/Lesson Database Foundation là nền để làm các task tiếp theo:

* Admin Course CRUD API.
* Admin Section CRUD API.
* Admin Lesson CRUD API.
* Public Course List API.
* Public Course Detail API.
* Enrollment.
* Learning Progress.
* Quiz và Flashcard sau này.

## Không làm trong task này

* Không làm API CRUD Course.
* Không làm API CRUD Section.
* Không làm API CRUD Lesson.
* Không làm frontend.
* Không làm Enrollment.
* Không làm Lesson Progress.
* Không làm Quiz.
* Không làm Payment.
* Không làm Upload file thật.
* Không làm public course listing.
* Không làm admin dashboard.

## File tài liệu cần dùng

* docs/00_MASTER_CONTEXT.md
* docs/23_MVP_SCOPE.md
* docs/26_API_PRIORITY.md
* docs/27_DATABASE_PHASES.md
* docs/28_ENUM_DEFINITIONS.md
* docs/29_ERROR_CODE_STANDARD.md
* docs/30_PERMISSION_MATRIX.md
* docs/05_features/05_02_COURSE_FEATURES.md
* docs/07_database/07_02_COURSE_LESSON.md
* docs/08_api/08_03_COURSE_PUBLIC_API.md
* docs/08_api/08_04_LESSON_API.md
* docs/09_BACKEND_STRUCTURE.md
* docs/18_CODE_CONVENTIONS.md
* docs/31_DETAILED_TESTING_PLAN.md

## Bảng/entity cần chuẩn bị

* Course
* CourseSection
* Lesson
* LessonResource

## Enum cần tạo

* CourseLevel
* CourseType
* CourseStatus
* ResourceType

Có thể dùng `CourseStatus` cho cả course, section và lesson nếu phù hợp. Nếu cần tách enum riêng, phải giải thích lý do trước.

## Quan hệ dữ liệu dự kiến

```text
User(teacher) 1 - n Course

Course 1 - n CourseSection

Course 1 - n Lesson

CourseSection 1 - n Lesson

Lesson 1 - n LessonResource
```

## Entity yêu cầu chính

### Course

Các field chính:

* id
* teacher
* title
* slug
* shortDescription
* description
* thumbnailUrl
* level
* courseType
* originalPrice
* salePrice
* status
* totalDurationMinutes
* totalLessons
* averageRating
* totalStudents
* createdAt
* updatedAt

### CourseSection

Các field chính:

* id
* course
* title
* description
* sortOrder
* status
* createdAt
* updatedAt

### Lesson

Các field chính:

* id
* course
* section
* title
* slug
* content
* videoUrl
* audioUrl
* durationMinutes
* sortOrder
* isPreview
* status
* createdAt
* updatedAt

### LessonResource

Các field chính:

* id
* lesson
* title
* resourceType
* fileUrl
* fileSize
* sortOrder
* createdAt

## Cần tạo hoặc chỉnh sửa

* Course entity
* CourseSection entity
* Lesson entity
* LessonResource entity
* CourseLevel enum
* CourseType enum
* CourseStatus enum
* ResourceType enum
* CourseRepository
* CourseSectionRepository
* LessonRepository
* LessonResourceRepository
* Slug unique constraint nếu cần
* Index hoặc repository method cơ bản nếu cần
* Kiểm tra quan hệ JPA mapping

## Error code có thể chuẩn bị nếu cần

* COURSE_001: Course not found
* COURSE_002: Course slug already exists
* LESSON_001: Lesson not found
* LESSON_003: Lesson does not belong to course

Task này chỉ chuẩn bị error code nếu thật sự cần, chưa bắt buộc dùng trong service vì chưa code API.

## Checklist

* [ ] Tạo package module_course nếu chưa có
* [ ] Tạo package module_lesson nếu tách riêng lesson
* [ ] Tạo enum CourseLevel
* [ ] Tạo enum CourseType
* [ ] Tạo enum CourseStatus
* [ ] Tạo enum ResourceType
* [ ] Tạo Course entity
* [ ] Tạo CourseSection entity
* [ ] Tạo Lesson entity
* [ ] Tạo LessonResource entity
* [ ] Cấu hình quan hệ User teacher với Course
* [ ] Cấu hình quan hệ Course với CourseSection
* [ ] Cấu hình quan hệ Course/CourseSection với Lesson
* [ ] Cấu hình quan hệ Lesson với LessonResource
* [ ] Tạo unique constraint cho course slug
* [ ] Tạo unique constraint cho lesson slug trong cùng course nếu phù hợp
* [ ] Tạo CourseRepository
* [ ] Tạo CourseSectionRepository
* [ ] Tạo LessonRepository
* [ ] Tạo LessonResourceRepository
* [ ] Chạy backend không lỗi
* [ ] Kiểm tra database sinh bảng đúng
* [ ] Kiểm tra foreign key đúng
* [ ] Kiểm tra Swagger vẫn hoạt động
* [ ] Kiểm tra Auth APIs vẫn hoạt động
* [ ] Ghi learning notes

## Cách test sau khi hoàn thành

1. Chạy backend.
2. Kiểm tra console không có lỗi Hibernate/JPA.
3. Kiểm tra database có các bảng:

   * courses
   * course_sections
   * lessons
   * lesson_resources
4. Kiểm tra foreign key:

   * courses.teacher_id → users.id
   * course_sections.course_id → courses.id
   * lessons.course_id → courses.id
   * lessons.section_id → course_sections.id
   * lesson_resources.lesson_id → lessons.id
5. Kiểm tra unique constraint cho slug nếu có.
6. Mở Swagger kiểm tra vẫn hoạt động.
7. Test lại `GET /api/health`.
8. Test lại `POST /api/auth/login`.
9. Test lại `GET /api/users/me`.

## Kết quả mong muốn

Backend có đầy đủ entity và repository nền tảng cho Course/Lesson. Database sinh bảng đúng, quan hệ đúng, backend chạy ổn và chưa có API CRUD Course/Lesson trong task này.

---

