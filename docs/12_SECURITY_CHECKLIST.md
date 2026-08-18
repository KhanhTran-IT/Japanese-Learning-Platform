> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

## 15. Bảo mật hệ thống

### 15.1. Backend security checklist

```text
Dùng Spring Security
Dùng BCrypt để mã hóa mật khẩu
Dùng JWT access token ngắn hạn
Dùng refresh token dài hạn
Lưu refresh token trong database
Có chức năng revoke refresh token khi logout
Validate request bằng DTO
Không trả entity trực tiếp ra frontend
Dùng role-based authorization
Dùng CORS đúng domain frontend
Rate limit login/register
Chống brute force
Không để secret trong source code
Không log password/token
Kiểm tra quyền trước khi truy cập khóa học trả phí
Kiểm tra webhook thanh toán thật kỹ
```

### 15.2. Frontend security checklist

```text
Không tin dữ liệu từ client
Không hiển thị route admin nếu không có quyền
Dùng route guard
Không lưu thông tin nhạy cảm không cần thiết
Xử lý token hết hạn
Validate form phía client
Escape nội dung HTML nếu có user-generated content
```

### 15.3. File upload security checklist

```text
Giới hạn dung lượng file
Kiểm tra MIME type
Đổi tên file khi upload
Không cho upload file nguy hiểm
Không lưu file upload trong thư mục có thể thực thi code
Lưu file lớn ở object storage
Dùng signed URL nếu file cần bảo vệ
```

---
