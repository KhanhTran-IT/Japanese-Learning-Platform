# 31. DETAILED_TESTING_PLAN - Kế hoạch test chi tiết

## 1. Mục đích của file

File này giúp bạn test dự án có hệ thống. Dù chưa viết unit test đầy đủ, bạn vẫn cần checklist test thủ công rõ ràng để tránh lỗi khi demo.

## 2. Các loại test nên có

```text
Manual test          Test thủ công bằng trình duyệt/Postman
Unit test            Test service/helper nhỏ
Integration test     Test API controller + database test
Security test        Test phân quyền/token
Regression test      Test lại chức năng cũ sau khi sửa code
```

## 3. MVP manual test checklist

### Auth

```text
[ ] Register thành công với email mới.
[ ] Register thất bại nếu email trùng.
[ ] Register thất bại nếu password yếu.
[ ] Register thất bại nếu confirmPassword không khớp.
[ ] Login thành công với tài khoản đúng.
[ ] Login thất bại với password sai.
[ ] Login thất bại nếu account LOCKED.
[ ] /api/users/me trả đúng user hiện tại.
[ ] Refresh token tạo access token mới.
[ ] Logout revoke refresh token.
[ ] Token hết hạn không truy cập API protected được.
```

### Course public

```text
[ ] Guest xem danh sách khóa học published.
[ ] Guest không thấy khóa học DRAFT/HIDDEN.
[ ] Filter theo level hoạt động.
[ ] Filter theo FREE/PAID hoạt động.
[ ] Search keyword hoạt động.
[ ] Course detail hiển thị đúng thông tin.
[ ] Course detail hiển thị chương/bài học.
[ ] Bài học preview cho phép guest xem.
[ ] Bài học không preview yêu cầu đăng nhập/enroll.
```

### Admin course/lesson

```text
[ ] ADMIN tạo khóa học DRAFT.
[ ] ADMIN sửa khóa học.
[ ] ADMIN publish khóa học.
[ ] ADMIN hide khóa học.
[ ] ADMIN tạo chương học.
[ ] ADMIN sửa/xóa chương học.
[ ] ADMIN tạo bài học.
[ ] ADMIN upload resource cho bài học.
[ ] STUDENT không truy cập được API admin.
[ ] Guest không truy cập được API admin.
```

### Enrollment/progress

```text
[ ] STUDENT enroll khóa FREE thành công.
[ ] STUDENT không enroll trùng cùng khóa.
[ ] STUDENT xem được khóa học của mình.
[ ] STUDENT mở được bài học trong khóa đã enroll.
[ ] STUDENT cập nhật watchedPercent hợp lệ.
[ ] watchedPercent < 0 hoặc > 100 bị từ chối.
[ ] STUDENT complete lesson thành công.
[ ] course_enrollments.progress_percent được tính lại.
[ ] STUDENT không cập nhật progress bài học của khóa chưa enroll.
```

## 4. P1 manual test checklist

### Forgot password

```text
[ ] Nhập email tồn tại tạo reset token.
[ ] Nhập email không tồn tại không lộ thông tin nhạy cảm.
[ ] Reset token hết hạn không dùng được.
[ ] Reset token đã dùng không dùng lại được.
[ ] Đổi mật khẩu xong login bằng mật khẩu mới được.
```

### Quiz

```text
[ ] Student start quiz tạo attempt IN_PROGRESS.
[ ] Submit quiz tính đúng điểm.
[ ] SINGLE_CHOICE chấm đúng.
[ ] TRUE_FALSE chấm đúng.
[ ] FILL_BLANK xử lý khoảng trắng/case theo rule.
[ ] Attempt đã submit không submit lại được.
[ ] Vượt max_attempts bị chặn.
```

### Payment basic

```text
[ ] Tạo order PENDING.
[ ] Order item lưu đúng course_title và price tại thời điểm mua.
[ ] Payment success cập nhật order PAID.
[ ] Sau payment success tạo enrollment.
[ ] Callback sai chữ ký không mở khóa học.
[ ] Callback trùng không xử lý lại hai lần.
```

## 5. Backend unit test nên viết trước

Ưu tiên test service quan trọng:

```text
AuthService.register
AuthService.login
TokenService.refreshToken
CourseService.createCourse
CourseService.publishCourse
EnrollmentService.enrollFreeCourse
LessonProgressService.completeLesson
```

## 6. Security test checklist

```text
[ ] API /api/admin/** không có token → 401.
[ ] API /api/admin/** token STUDENT → 403.
[ ] API /api/admin/** token ADMIN → 200 nếu hợp lệ.
[ ] Student A không xem/sửa dữ liệu Student B.
[ ] User không học khóa PAID nếu chưa mua.
[ ] User không update progress bài học chưa enroll.
[ ] Token giả/chỉnh sửa bị từ chối.
[ ] Refresh token revoked không dùng được.
```

## 7. Frontend test checklist

```text
[ ] Route /admin redirect nếu chưa login.
[ ] Route /admin báo unauthorized nếu là STUDENT.
[ ] Route /student redirect nếu chưa login.
[ ] Axios tự gắn access token.
[ ] Gặp 401 thì thử refresh token.
[ ] Refresh token fail thì logout.
[ ] Form login hiển thị lỗi dễ hiểu.
[ ] Form register validate trước khi gọi API.
[ ] Loading state hiển thị khi gọi API.
[ ] Empty state hiển thị khi không có dữ liệu.
```

## 8. Postman collection nên tạo

Nhóm request:

```text
Auth
User
Public Course
Admin Course
Admin Lesson
Student Enrollment
Lesson Progress
Quiz
Order/Payment
```

Environment variables:

```text
baseUrl=http://localhost:8080/api
accessToken=
refreshToken=
courseId=
lessonId=
```

## 9. Definition of Done cho một module

Một module chỉ được xem là xong khi:

```text
[ ] API chạy được bằng Postman.
[ ] Request có validate.
[ ] Response đúng ApiResponse.
[ ] Lỗi nghiệp vụ dùng ErrorCode.
[ ] API protected có phân quyền.
[ ] Service không chứa code trùng lặp quá nhiều.
[ ] Controller không chứa logic nghiệp vụ.
[ ] Frontend gọi API được.
[ ] Có test thủ công các case chính.
[ ] Code đã commit Git.
```

## 10. Prompt dùng với AI

```text
Hãy đọc 31_DETAILED_TESTING_PLAN.md.
Tôi vừa code xong module [tên module].
Hãy tạo checklist test Postman + test frontend + security test cho module này.
```
