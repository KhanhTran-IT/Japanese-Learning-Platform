# 29. ERROR_CODE_STANDARD - Chuẩn mã lỗi hệ thống

## 1. Mục đích của file

File này giúp chuẩn hóa lỗi backend để frontend xử lý dễ hơn, API chuyên nghiệp hơn và debug nhanh hơn.

Response lỗi vẫn theo chuẩn `ApiResponse`, nhưng `errors` hoặc `code` nên có mã lỗi rõ ràng.

## 2. Quy tắc đặt mã lỗi

```text
PREFIX_XXX
```

Trong đó:

```text
AUTH    Lỗi xác thực/đăng nhập
USER    Lỗi user/profile
ROLE    Lỗi phân quyền
COURSE  Lỗi khóa học
LESSON  Lỗi bài học
ENROLL  Lỗi ghi danh
QUIZ    Lỗi quiz
PAYMENT Lỗi thanh toán
ORDER   Lỗi đơn hàng
FILE    Lỗi upload/file
VALID   Lỗi validation
SYS     Lỗi hệ thống
```

Ví dụ:

```text
AUTH_001 Email already exists
COURSE_001 Course not found
PAYMENT_003 Invalid payment signature
```

## 3. Auth error codes

| Code | HTTP | Message đề xuất | Khi nào xảy ra |
|---|---:|---|---|
| AUTH_001 | 409 | Email đã tồn tại | Register email trùng |
| AUTH_002 | 401 | Email hoặc mật khẩu không đúng | Login sai |
| AUTH_003 | 403 | Tài khoản đã bị khóa | User status LOCKED |
| AUTH_004 | 401 | Access token không hợp lệ | JWT sai/chỉnh sửa |
| AUTH_005 | 401 | Access token đã hết hạn | JWT expired |
| AUTH_006 | 401 | Refresh token không hợp lệ | Token không tồn tại/sai |
| AUTH_007 | 401 | Refresh token đã hết hạn | Refresh expired |
| AUTH_008 | 401 | Refresh token đã bị thu hồi | revoked=true |
| AUTH_009 | 403 | Email chưa được xác thực | Nếu bật verify email |
| AUTH_010 | 400 | Mật khẩu xác nhận không khớp | Register/reset password |

## 4. User/Role error codes

| Code | HTTP | Message đề xuất |
|---|---:|---|
| USER_001 | 404 | Không tìm thấy người dùng |
| USER_002 | 400 | Trạng thái người dùng không hợp lệ |
| USER_003 | 403 | Không được cập nhật tài khoản này |
| USER_004 | 400 | Mật khẩu hiện tại không đúng |
| USER_005 | 409 | Số điện thoại/email đã được sử dụng |
| ROLE_001 | 403 | Bạn không có quyền thực hiện hành động này |
| ROLE_002 | 400 | Role không hợp lệ |
| ROLE_003 | 403 | Không được gán quyền cao hơn quyền hiện tại |

## 5. Course/Lesson error codes

| Code | HTTP | Message đề xuất |
|---|---:|---|
| COURSE_001 | 404 | Không tìm thấy khóa học |
| COURSE_002 | 409 | Slug khóa học đã tồn tại |
| COURSE_003 | 400 | Trạng thái khóa học không hợp lệ |
| COURSE_004 | 403 | Bạn không có quyền quản lý khóa học này |
| COURSE_005 | 400 | Không thể publish khóa học chưa có bài học |
| COURSE_006 | 403 | Khóa học chưa được xuất bản |
| LESSON_001 | 404 | Không tìm thấy bài học |
| LESSON_002 | 403 | Bạn chưa có quyền truy cập bài học này |
| LESSON_003 | 400 | Bài học không thuộc khóa học này |
| LESSON_004 | 409 | Slug bài học đã tồn tại trong khóa học |

## 6. Enrollment/Progress error codes

| Code | HTTP | Message đề xuất |
|---|---:|---|
| ENROLL_001 | 409 | Bạn đã ghi danh khóa học này |
| ENROLL_002 | 403 | Bạn cần mua khóa học trước khi học |
| ENROLL_003 | 404 | Không tìm thấy thông tin ghi danh |
| ENROLL_004 | 403 | Ghi danh đã hết hạn |
| PROGRESS_001 | 400 | Phần trăm tiến độ không hợp lệ |
| PROGRESS_002 | 403 | Không thể cập nhật tiến độ cho bài học chưa được truy cập |

## 7. Quiz error codes

| Code | HTTP | Message đề xuất |
|---|---:|---|
| QUIZ_001 | 404 | Không tìm thấy quiz |
| QUIZ_002 | 403 | Bạn chưa có quyền làm quiz này |
| QUIZ_003 | 400 | Quiz chưa được xuất bản |
| QUIZ_004 | 400 | Đã vượt quá số lần làm bài |
| QUIZ_005 | 404 | Không tìm thấy lần làm quiz |
| QUIZ_006 | 400 | Lần làm quiz đã được nộp |
| QUIZ_007 | 400 | Đáp án không hợp lệ |

## 8. Order/Payment error codes

| Code | HTTP | Message đề xuất |
|---|---:|---|
| ORDER_001 | 404 | Không tìm thấy đơn hàng |
| ORDER_002 | 400 | Đơn hàng không thể thanh toán |
| ORDER_003 | 400 | Đơn hàng đã hết hạn |
| ORDER_004 | 403 | Bạn không có quyền xem đơn hàng này |
| PAYMENT_001 | 404 | Không tìm thấy giao dịch thanh toán |
| PAYMENT_002 | 400 | Phương thức thanh toán không hợp lệ |
| PAYMENT_003 | 400 | Chữ ký thanh toán không hợp lệ |
| PAYMENT_004 | 409 | Giao dịch đã được xử lý |
| PAYMENT_005 | 400 | Số tiền thanh toán không khớp |
| PAYMENT_006 | 502 | Cổng thanh toán không phản hồi |

## 9. File upload error codes

| Code | HTTP | Message đề xuất |
|---|---:|---|
| FILE_001 | 400 | File không được để trống |
| FILE_002 | 400 | Dung lượng file vượt quá giới hạn |
| FILE_003 | 400 | Định dạng file không được hỗ trợ |
| FILE_004 | 500 | Upload file thất bại |
| FILE_005 | 404 | Không tìm thấy file |

## 10. Validation/System error codes

| Code | HTTP | Message đề xuất |
|---|---:|---|
| VALID_001 | 400 | Dữ liệu không hợp lệ |
| VALID_002 | 400 | Thiếu trường bắt buộc |
| VALID_003 | 400 | Giá trị không nằm trong phạm vi cho phép |
| SYS_001 | 500 | Lỗi hệ thống |
| SYS_002 | 503 | Dịch vụ tạm thời không khả dụng |
| SYS_003 | 429 | Thao tác quá nhiều lần, vui lòng thử lại sau |

## 11. Response lỗi mẫu

```json
{
  "success": false,
  "message": "Email đã tồn tại",
  "data": null,
  "errors": [
    {
      "code": "AUTH_001",
      "field": "email",
      "message": "Email đã tồn tại"
    }
  ],
  "timestamp": "2026-06-09T10:30:00"
}
```

## 12. AppException gợi ý

```java
public class AppException extends RuntimeException {
    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
```

## 13. Prompt dùng với AI

```text
Hãy đọc 29_ERROR_CODE_STANDARD.md.
Khi viết service hoặc exception, hãy dùng mã lỗi phù hợp.
Không throw RuntimeException chung chung nếu lỗi có thể map vào mã lỗi chuẩn.
```
