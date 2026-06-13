# 30. PERMISSION_MATRIX - Ma trận phân quyền chi tiết

## 1. Mục đích của file

File này dùng để kiểm soát quyền hạn ở backend Spring Security và frontend route guard.

Nguyên tắc:

```text
Frontend chỉ ẩn/hiện UI để tăng UX.
Backend mới là nơi bắt buộc kiểm tra quyền thật sự.
```

## 2. Role trong hệ thống

```text
GUEST            Người chưa đăng nhập
STUDENT          Học viên
CONTENT_EDITOR   Biên tập nội dung
TEACHER          Giáo viên
ADMIN            Quản trị viên
SUPER_ADMIN      Toàn quyền hệ thống
```

## 3. Public permissions

| Chức năng | GUEST | STUDENT | TEACHER | CONTENT_EDITOR | ADMIN | SUPER_ADMIN |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| Xem trang chủ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Xem danh sách khóa học | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Xem chi tiết khóa học published | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Xem bài học preview | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Đăng ký tài khoản | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Đăng nhập | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

## 4. Student permissions

| Chức năng | STUDENT | TEACHER | ADMIN | SUPER_ADMIN |
|---|:---:|:---:|:---:|:---:|
| Enroll khóa miễn phí | ✅ | ❌ | ✅ | ✅ |
| Mua khóa học | ✅ | ❌ | ✅ | ✅ |
| Xem khóa học đã mua/enroll | ✅ chỉ của mình | ❌ | ✅ tất cả | ✅ tất cả |
| Học bài trong khóa đã enroll | ✅ | ❌ | ✅ | ✅ |
| Cập nhật tiến độ của mình | ✅ | ❌ | ❌ | ❌ |
| Làm quiz của khóa đã enroll | ✅ | ❌ | ✅ | ✅ |
| Xem lịch sử học của mình | ✅ | ❌ | ✅ | ✅ |
| Cập nhật hồ sơ cá nhân | ✅ của mình | ✅ của mình | ✅ của mình | ✅ của mình |
| Đổi mật khẩu | ✅ của mình | ✅ của mình | ✅ của mình | ✅ của mình |

## 5. Teacher permissions

| Chức năng | TEACHER | ADMIN | SUPER_ADMIN |
|---|:---:|:---:|:---:|
| Tạo khóa học | ✅ | ✅ | ✅ |
| Sửa khóa học | ✅ khóa của mình | ✅ tất cả | ✅ tất cả |
| Xóa/ẩn khóa học | ✅ khóa của mình nếu được cấp | ✅ | ✅ |
| Tạo chương/bài học | ✅ khóa của mình | ✅ | ✅ |
| Tạo quiz | ✅ khóa của mình | ✅ | ✅ |
| Xem học viên trong khóa | ✅ khóa của mình | ✅ | ✅ |
| Xem doanh thu toàn hệ thống | ❌ | ✅ giới hạn | ✅ |
| Cấu hình thanh toán | ❌ | ❌ | ✅ |

## 6. Content editor permissions

| Chức năng | CONTENT_EDITOR | ADMIN | SUPER_ADMIN |
|---|:---:|:---:|:---:|
| Quản lý blog | ✅ | ✅ | ✅ |
| Quản lý FAQ | ✅ | ✅ | ✅ |
| Quản lý từ vựng JLPT | ✅ | ✅ | ✅ |
| Quản lý Kanji | ✅ | ✅ | ✅ |
| Quản lý ngữ pháp | ✅ | ✅ | ✅ |
| Quản lý banner | ✅ nếu được cấp | ✅ | ✅ |
| Quản lý payment/order | ❌ | ✅ | ✅ |
| Gán role user | ❌ | ❌ | ✅ |

## 7. Admin permissions

| Chức năng | ADMIN | SUPER_ADMIN |
|---|:---:|:---:|
| Xem admin dashboard | ✅ | ✅ |
| Quản lý user | ✅ | ✅ |
| Khóa/mở user | ✅ | ✅ |
| Xóa mềm user | ✅ nếu được cấp | ✅ |
| Gán role thường | ❌ hoặc giới hạn | ✅ |
| Quản lý khóa học | ✅ | ✅ |
| Quản lý bài học | ✅ | ✅ |
| Quản lý quiz | ✅ | ✅ |
| Quản lý đơn hàng | ✅ | ✅ |
| Xem payment | ✅ | ✅ |
| Cấu hình website | ✅ phần thường | ✅ toàn bộ |
| Cấu hình payment secret | ❌ | ✅ |
| Quản lý admin khác | ❌ | ✅ |

## 8. API permission mapping MVP

### Public API

```text
GET /api/courses                         permitAll
GET /api/courses/{slug}                  permitAll
GET /api/courses/{id}/sections           permitAll
GET /api/courses/{id}/lessons            permitAll hoặc lọc bài preview
POST /api/auth/register                  permitAll
POST /api/auth/login                     permitAll
POST /api/auth/refresh-token             permitAll
```

### Student API

```text
GET  /api/users/me                       authenticated
PUT  /api/users/me                       authenticated
POST /api/courses/{id}/enroll            hasRole(STUDENT)
GET  /api/users/me/courses               hasRole(STUDENT)
GET  /api/lessons/{id}                   authenticated + check enrollment/preview
POST /api/lessons/{id}/progress          hasRole(STUDENT) + check enrollment
POST /api/lessons/{id}/complete          hasRole(STUDENT) + check enrollment
```

### Admin API

```text
GET    /api/admin/**                     hasAnyRole(ADMIN, SUPER_ADMIN)
POST   /api/admin/**                     hasAnyRole(ADMIN, SUPER_ADMIN)
PUT    /api/admin/**                     hasAnyRole(ADMIN, SUPER_ADMIN)
DELETE /api/admin/**                     hasAnyRole(ADMIN, SUPER_ADMIN)
```

Sau này có Teacher:

```text
/api/teacher/**                          hasAnyRole(TEACHER, ADMIN, SUPER_ADMIN)
```

## 9. Method-level security nên dùng

Ví dụ:

```java
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public CourseResponse createCourse(CreateCourseRequest request) { ... }
```

Với quyền sở hữu:

```java
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN') or @courseSecurity.isOwner(#courseId, authentication)")
public CourseResponse updateCourse(Long courseId, UpdateCourseRequest request) { ... }
```

## 10. Frontend route guard

```text
/admin/**      yêu cầu ADMIN hoặc SUPER_ADMIN
/student/**    yêu cầu STUDENT hoặc role cao hơn tùy route
/teacher/**    yêu cầu TEACHER hoặc ADMIN/SUPER_ADMIN
/login         nếu đã login thì chuyển dashboard
```

## 11. Prompt dùng với AI

```text
Hãy đọc 30_PERMISSION_MATRIX.md.
Khi viết API này, hãy chỉ rõ endpoint cần role nào, có cần check owner/enrollment không, và nên đặt @PreAuthorize như thế nào.
```
