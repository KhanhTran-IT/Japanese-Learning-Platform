# Hướng Dẫn Vận Hành Hệ Thống Giới Hạn Tốc Độ (Rate Limiter Operations Guide)

Tài liệu này dành cho đội ngũ Ops/DevOps để quản lý và vận hành tính năng bảo vệ (Rate Limiting) các endpoint xác thực trên môi trường Production.

## 1. Cơ chế hoạt động (Sliding Window bằng Redis)
Ứng dụng sử dụng thuật toán **Sliding Window** thông qua Redis Sorted Sets để quản lý số lượng request theo IP và Email.
- Dữ liệu được tập trung qua Redis, do đó các instance của backend (horizontally scaled) đều chia sẻ chung một bộ đếm.
- Prefix của keys trong Redis là: `rate_limit:`.

## 2. Cấu hình Failure Policy (Chính sách xử lý khi Redis sập)

Trong trường hợp Redis gặp sự cố (ví dụ: mất kết nối, timeout, OOM), hệ thống Rate Limiter cần một chính sách rõ ràng để quyết định liệu có cho phép các request đi qua hay không. Bạn có thể cấu hình thông qua biến môi trường hoặc trong `application.yml`:

```yaml
app:
  rate-limit:
    redis-failure-policy: fail-open # Hoặc fail-closed
```

### Chế độ `fail-open` (Mặc định)
- **Hành vi:** Nếu Redis sập, hệ thống sẽ bỏ qua bước giới hạn tốc độ và **cho phép tất cả request đi qua**.
- **Ưu điểm:** Đảm bảo hệ thống vẫn phục vụ được người dùng hợp lệ, tối đa hóa High Availability (HA).
- **Nhược điểm:** Hệ thống tạm thời mất đi lớp bảo vệ trước các cuộc tấn công Brute-force.
- **Khuyến nghị:** Sử dụng làm mặc định cho hầu hết các trường hợp.

### Chế độ `fail-closed`
- **Hành vi:** Nếu Redis sập, hệ thống sẽ **chặn tất cả request** gọi đến endpoint xác thực (trả về lỗi HTTP 503 Service Unavailable với ErrorCode `RATE_LIMIT_UNAVAILABLE`).
- **Ưu điểm:** Đảm bảo tính bảo mật tuyệt đối, ngăn chặn triệt để mọi luồng traffic nếu lớp phòng ngự đã hỏng.
- **Nhược điểm:** Gây sập dịch vụ xác thực cục bộ (Denial of Service tự nguyện) cho đến khi Redis được khôi phục.
- **Khuyến nghị:** Sử dụng cho các hệ thống yêu cầu bảo mật cực cao (như ngân hàng, cổng thi cử) nơi mà việc lộ mật khẩu nguy hiểm hơn việc hệ thống ngừng hoạt động.

## 3. Giám sát và Cảnh báo (Monitoring & Alerts)

### Các Log Keyword cần bắt (Logging)
Hệ thống sử dụng Structured Logging. Hãy thiết lập các cảnh báo (Alerts) trong ELK/Datadog khi thấy các log sau:

- Khi Redis gặp lỗi Exception (Connection Refused, Timeout):
  ```
  [ERROR] Redis rate limiter error. key={key}, policy={policy}, action={action}, exception={exceptionType}
  ```
- Khi Redis trả về kết quả không mong muốn (ví dụ: thiếu response từ MULTI/EXEC):
  ```
  [WARN] Redis rate limiter unexpected result. key={key}, policy={policy}, action={action}
  ```

### Kịch bản xử lý sự cố (Runbook)

**Nếu nhận được cảnh báo lỗi Redis Rate Limiter:**
1. **Kiểm tra trạng thái Redis:** Xem lại dashboard của Redis (memory usage, connections, CPU).
2. **Đánh giá mức độ:** 
   - Nếu lỗi lẻ tẻ: Có thể do network glitch tạm thời.
   - Nếu lỗi liên tục: Redis có thể đã chết hoặc quá tải.
3. **Phản ứng nhanh (Tùy chọn):**
   - Nếu đang dùng `fail-closed` và muốn khôi phục dịch vụ khẩn cấp cho khách hàng (hy sinh bảo mật tạm thời), hãy đổi môi trường thành `APP_RATE_LIMIT_REDIS_FAILURE_POLICY=fail-open` và restart các pods.
4. **Sửa chữa:** Restart/Scale up Redis server.

## 4. Các thông số cấu hình chung

Ngoài chính sách khi sập, bạn có thể điều chỉnh các hạn mức tốc độ:

| Biến môi trường | Giải thích | Mặc định |
|---|---|---|
| `APP_RATE_LIMIT_LOGIN_MAX_ATTEMPTS_PER_IP` | Số lần thử đăng nhập tối đa trên 1 IP trong window. | 20 |
| `APP_RATE_LIMIT_LOGIN_MAX_ATTEMPTS_PER_EMAIL` | Số lần thử đăng nhập tối đa trên 1 Email trong window. | 10 |
| `APP_RATE_LIMIT_LOGIN_WINDOW_MINUTES` | Cửa sổ thời gian (phút) cho đăng nhập. | 15 |
| `APP_RATE_LIMIT_REFRESH_MAX_ATTEMPTS_PER_IP` | Số lần refresh token tối đa trên 1 IP trong window. | 30 |
| `APP_RATE_LIMIT_REFRESH_WINDOW_MINUTES` | Cửa sổ thời gian (phút) cho refresh token. | 15 |
