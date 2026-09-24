# Hướng Dẫn Cấu Hình Bảo Mật Media URL (CDN & Storage)

Tài liệu này dành cho đội ngũ Ops/DevOps để cấu hình tính năng kiểm duyệt Media URL (`@ValidMediaUrl`) trên môi trường Production. Tính năng này ngăn chặn XSS, SSRF và lỗi Mixed Content.

## 1. Yêu cầu bắt buộc HTTPS (Require HTTPS)
Trên môi trường Production, nếu trang web của bạn chạy trên `https://`, việc nhúng các ảnh/video qua giao thức `http://` sẽ gây ra lỗi **Mixed Content**. Trình duyệt (Chrome, Safari, Firefox) sẽ chặn tải các tài nguyên này, khiến giao diện bị vỡ hoặc mất nội dung.

- **Môi trường Dev (Mặc định):** Cho phép cả `http` và `https` để dev dễ dàng test với `localhost` hoặc các server nội bộ.
  ```yaml
  # application.yml
  app:
    media:
      require-https: false
  ```
- **Môi trường Prod:** Bắt buộc phải là `https`.
  ```yaml
  # application-prod.yml
  app:
    media:
      require-https: true
  ```

## 2. Cấu hình danh sách Domain tin cậy (Trusted Domain Allowlist)
Để ngăn kẻ xấu chèn link hình ảnh từ các server chứa mã độc (phục vụ cho XSS, tracking hoặc SEO spam), hệ thống hỗ trợ chỉ cho phép các domain được định trước (ví dụ: S3, CloudFront, Cloudinary).

- Khai báo biến môi trường hoặc cấu hình trong YAML:
  ```yaml
  app:
    media:
      trusted-domains: s3.amazonaws.com, cloudinary.com, res.cloudinary.com
  ```
- Hệ thống hỗ trợ **Subdomain Matching**: Nếu bạn cấu hình `s3.amazonaws.com`, hệ thống tự động cho phép các URL như `https://my-bucket.s3.amazonaws.com/...`.
- **Nếu bỏ trống:** Hệ thống sẽ cho phép mọi domain hợp lệ. Điều này chỉ nên dùng ở môi trường Dev.

## 3. Các nhà cung cấp (Providers) phổ biến

### 3.1. AWS S3 / CloudFront
Nếu bạn lưu trữ ảnh trên S3 bucket hoặc dùng CloudFront CDN:
```yaml
app:
  media:
    trusted-domains: s3.amazonaws.com, cloudfront.net
```

### 3.2. Cloudinary
Nếu dự án dùng Cloudinary để tối ưu ảnh:
```yaml
app:
  media:
    trusted-domains: cloudinary.com, res.cloudinary.com
```

### 3.3. Google Cloud Storage
```yaml
app:
  media:
    trusted-domains: storage.googleapis.com
```

## 4. Runbook Xử Lý Sự Cố
- **Hiện tượng:** Admin đăng một bài học mới nhưng bị báo lỗi `URL không hợp lệ` dù link là ảnh thật.
- **Nguyên nhân 1:** Link bắt đầu bằng `http://` nhưng môi trường Prod đang yêu cầu `https://`. (Bảo người dùng đổi link sang HTTPS).
- **Nguyên nhân 2:** Domain chứa ảnh chưa được liệt kê trong `trusted-domains`. (Liên hệ Ops bổ sung domain vào file cấu hình hoặc biến môi trường `APP_MEDIA_TRUSTED_DOMAINS` và restart server).
- **Nguyên nhân 3:** Kẻ xấu cố gắng truyền `javascript:alert(1)`. Validator sẽ âm thầm chặn đứng và trả về 400 Bad Request. Hệ thống an toàn tuyệt đối.
