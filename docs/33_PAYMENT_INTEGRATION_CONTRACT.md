# 33. PAYMENT INTEGRATION CONTRACT & PROVIDER-AGNOSTIC BOUNDARY

## 1. Mục Đích & Phạm Vi (Purpose & Scope)

Tài liệu này đặc tả kiến trúc tích hợp thanh toán cho hệ thống Japanese Learning Web:
1. Thiết kế ranh giới cổng thanh toán độc lập với nhà cung cấp (Provider-Agnostic Payment Boundary).
2. Đặc tả hợp đồng tích hợp chính thức với nhà cung cấp thanh toán được chọn: **VNPay (Vietnam Payment Solution)**.
3. Cơ chế xác thực chữ ký số (Signed Webhook Verification) chống giả mạo payload và timing attack.
4. Xử lý sự kiện bất biến / lũy thừa (Idempotent Event Processing) khi cổng thanh toán gọi lại nhiều lần.
5. Quản lý chuyển dịch trạng thái đơn hàng mang tính giao dịch (Transactional Order State Transitions).
6. Kích hoạt quyền học viên an toàn (Safe Enrollment Activation) với khóa bi quan (Pessimistic Locking).
7. Cô lập môi trường: Giả lập thanh toán (Simulation) chỉ được phép chạy trên `dev` và `test`, tuyệt đối bị vô hiệu hóa trên `prod`.

---

## 2. Kiến Trúc Ranh Giới Thanh Toán (Provider-Agnostic Payment Boundary)

```text
+---------------------------------------------------------------------------------------+
|                                    APPLICATION CORE                                    |
|                                                                                       |
|   +---------------------+        +--------------------+        +------------------+   |
|   |  PaymentController  | -----> |   PaymentService   | -----> | Order / Enroll   |   |
|   +---------------------+        +--------------------+        +------------------+   |
|              |                             |                                          |
|              |                             v                                          |
|              |                 +-----------------------+                              |
|              +---------------> | PaymentGatewayRegistry|                              |
|                                +-----------------------+                              |
|                                            |                                          |
+--------------------------------------------|------------------------------------------+
                                             | SPI Boundary
                                             v
                             +-------------------------------+
                             |      <<PaymentGateway>>       |
                             +-------------------------------+
                             | + initPayment(cmd)            |
                             | + processWebhook(payload)     |
                             | + getSupportedMethod()        |
                             +-------------------------------+
                                     ^               ^
                                     |               |
                +--------------------+               +--------------------+
                |                                                         |
    +-----------------------+                                 +-----------------------+
    |  VnPayPaymentGateway  |                                 | SimulationGateway     |
    |  (Production Provider)|                                 | (Dev/Test Profile)    |
    +-----------------------+                                 +-----------------------+
```

### 2.1. Hợp đồng Interface `PaymentGateway`
Mọi cổng thanh toán (VNPay, MoMo, Stripe, hay Local Simulation) đều phải tuân thủ khế ước trừu tượng:

```java
public interface PaymentGateway {
    PaymentMethod getSupportedMethod();
    PaymentInitResult initPayment(PaymentInitCommand command);
    PaymentWebhookResult processWebhook(PaymentWebhookPayload payload);
}
```

### 2.2. DTOs Ranh Giới
- `PaymentInitCommand`: Chứa `orderId`, `orderCode`, `amount`, `orderInfo`, `clientIp`, `returnUrl`.
- `PaymentInitResult`: Chứa `paymentUrl` (URL chuyển hướng người dùng sang cổng thanh toán), `transactionCode`, `paymentMethod`, `paymentProvider`.
- `PaymentWebhookPayload`: Chứa `Map<String, String> params`, `rawBody`, `signatureHeader`, `clientIp`.
- `PaymentWebhookResult`: Chứa `signatureValid`, `eventStatus` (`SUCCESS`, `FAILED`, `CANCELLED`, `EXPIRED`), `orderCode`, `transactionCode`, `amount`, `responseCode`, `message`, `rawResponse`.

---

## 3. Đặc Tả Tích Hợp Cổng Thanh Toán VNPay (Selected Production Provider)

VNPay là cổng thanh toán trung gian phổ biến nhất tại thị trường Việt Nam, hỗ trợ thẻ ATM nội địa, QR Code ngân hàng (VNPAY-QR), và thẻ quốc tế (Visa/Mastercard/JCB).

### 3.1. Cấu Hình Hệ Thống (`application.yml`)
```yaml
app:
  payment:
    simulation-enabled: false # Mặc định tắt, chỉ bật ở dev/test
    vnpay:
      tmn-code: ${VNPAY_TMN_CODE:DEMOTMN1}
      hash-secret: ${VNPAY_HASH_SECRET:DEMOHASHSECRET1234567890ABCDEF}
      pay-url: ${VNPAY_PAY_URL:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}
      return-url: ${VNPAY_RETURN_URL:http://localhost:5173/student/checkout/vnpay-return}
```

### 3.2. Thuật Toán Tạo URL Thanh Toán (Payment Initiation)
1. Thu thập các tham số bắt buộc của VNPay:
   - `vnp_Version`: `2.1.0`
   - `vnp_Command`: `pay`
   - `vnp_TmnCode`: Mã website cấp bởi VNPay
   - `vnp_Amount`: Số tiền nhân 100 (VNPay yêu cầu đơn vị xu, ví dụ: 500,000 VND $\rightarrow$ `50000000`)
   - `vnp_CurrCode`: `VND`
   - `vnp_TxnRef`: Mã tham chiếu đơn hàng (`orderCode`)
   - `vnp_OrderInfo`: Nội dung thanh toán (ví dụ: `Thanh toan don hang ORD-xxx`)
   - `vnp_OrderType`: `other`
   - `vnp_Locale`: `vn`
   - `vnp_ReturnUrl`: URL frontend nhận kết quả redirect từ trình duyệt của học viên
   - `vnp_IpAddr`: Địa chỉ IP của client gọi thanh toán
   - `vnp_CreateDate`: Định dạng `yyyyMMddHHmmss` theo giờ GMT+7
   - `vnp_ExpireDate`: Định dạng `yyyyMMddHHmmss` (thời hạn 15 phút)
2. Sắp xếp danh sách tham số theo thứ tự bảng chữ cái (Alphabetical / ASCII sort theo tên trường).
3. Nối các tham số thành chuỗi truy vấn (query string dạng `key=value&key2=value2`).
4. Ký số bằng thuật toán **HMAC-SHA512** với khóa bí mật `hash-secret`:
   $$\text{SecureHash} = \text{HMAC-SHA512}(\text{hashSecret}, \text{hashData})$$
5. Nối `&vnp_SecureHash=` vào chuỗi truy vấn và trả về redirect URL hoàn chỉnh:
   `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=...&vnp_SecureHash=...`

---

## 4. Xác Thực Chữ Ký Webhook & Bảo Mật Giao Dịch (Signed Webhook Verification)

```text
[VNPay Gateway Server]
          |
          | 1. HTTP GET / POST (IPN / Webhook Callback)
          v
[PaymentController: /api/v1/payments/vnpay/ipn hoặc /api/v1/payments/webhook]
          |
          | 2. Tách vnp_SecureHash & vnp_SecureHashType khỏi params
          | 3. Sắp xếp các tham số còn lại theo thứ tự chữ cái A-Z
          | 4. Tạo hashData: URLEncoder.encode(key) + "=" + URLEncoder.encode(value)
          | 5. Tính toán HMAC-SHA512 với vnpay.hashSecret
          | 6. So sánh bằng MessageDigest.isEqual (Constant-time check)
          v
   +--------------+
   | Chữ ký hợp lệ? |
   +--------------+
       /        \
   (Sai)        (Đúng)
     /            \
    v              v
[Trả lỗi 97]     [Pessimistic Lock Order & Idempotent Processing]
```

### 4.1. Quy Tắc Chống Tấn Công (Anti-Tampering & Timing Attack Prevention)
- **Tách riêng trường chữ ký**: Loại bỏ `vnp_SecureHash` và `vnp_SecureHashType` trước khi ký.
- **So sánh thời gian hằng số (Constant-time Comparison)**: Sử dụng `java.security.MessageDigest.isEqual(...)` để chống timing attack dò tìm chuỗi hash.
- **Kiểm tra số tiền (Amount Tampering Prevention)**: So sánh `vnp_Amount / 100` với `order.getFinalAmount()`. Nếu sai lệch $\rightarrow$ từ chối xử lý, báo lỗi mã `04` (Invalid Amount).
- **Endpoint công khai không cần token Bearer**: Endpoint webhook `/api/v1/payments/vnpay/ipn` và `/api/v1/payments/webhook` được mở trong `SecurityConfig` (`permitAll()`) vì server VNPay gọi đến không sở hữu token JWT của học viên; tính xác thực được đảm bảo 100% bằng chữ ký mật mã HMAC-SHA512.

---

## 5. Máy Trạng Thái Đơn Hàng & Tính Lũy Thừa (State Transitions & Idempotency)

### 5.1. Bảng Chuyển Dịch Trạng Thái Hợp Lệ

| Trạng thái hiện tại | Sự kiện kích hoạt | Trạng thái đích | Ghi chú |
|---|---|---|---|
| `PENDING` | Webhook báo thành công (vnp_ResponseCode = "00") | `PAID` | Ghi nhận Payment SUCCESS, kích hoạt Enrollment, tăng học viên |
| `PENDING` | Webhook báo thất bại (vnp_ResponseCode $\neq$ "00") | `FAILED` | Ghi nhận Payment FAILED, không kích hoạt Enrollment |
| `PENDING` | Webhook báo hết hạn hoặc hủy | `EXPIRED` / `CANCELLED` | Ghi nhận Payment FAILED/CANCELLED |
| `PAID` | Nhận lại Webhook thành công (Duplicate Webhook) | `PAID` | **IDEMPOTENT NO-OP**: Giữ nguyên trạng thái, không kích hoạt lại, trả RspCode 02/00 |
| `PAID` | Nhận Webhook thất bại đến muộn (Late Failed Event) | `PAID` | **Bảo vệ trạng thái**: Đơn hàng đã PAID không bao giờ bị ghi đè về FAILED |
| `FAILED` / `EXPIRED` | Nhận Webhook thành công hợp lệ | Chặn hoặc xử lý kiểm toán | Không chuyển đổi trực tiếp nếu vi phạm quy tắc đơn hàng |

### 5.2. Khóa Bi Quan (Pessimistic Locking) Chống Race Condition
Khi Webhook đến hoặc người dùng vừa kích hoạt đồng thời, hệ thống sử dụng:
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT o FROM Order o WHERE o.orderCode = :orderCode")
Optional<Order> findByOrderCodeForUpdate(@Param("orderCode") String orderCode);
```
Điều này đảm bảo tại một thời điểm chỉ có 1 thread duy nhất được cập nhật trạng thái đơn hàng và kích hoạt ghi danh khóa học.

### 5.3. Kích Hoạt Ghi Danh Học Viên An Toàn (Safe Enrollment Activation)
- Kiểm tra xem bản ghi `CourseEnrollment` đã tồn tại giữa User và Course chưa:
  - Nếu đã tồn tại ở trạng thái `ACTIVE`: Không làm gì thêm, không tăng `totalStudents`.
  - Nếu đã tồn tại nhưng ở trạng thái khác: Cập nhật thành `ACTIVE`, tăng `totalStudents` đúng 1 lần.
  - Nếu chưa có: Tạo mới `CourseEnrollment` với trạng thái `ACTIVE`, tăng `totalStudents` đúng 1 lần.
- Toàn bộ thao tác cập nhật Order + Payment + Enrollment nằm trọn vẹn trong một giao dịch cơ sở dữ liệu (`@Transactional`). Nếu có bất kỳ lỗi nào xảy ra, toàn bộ sẽ rollback để đảm bảo tính nhất quán dữ liệu.

---

## 6. Cô Lập Môi Trường Giả Lập (Simulation Guarding)

1. **Chỉ kích hoạt trên `dev` và `test`**:
   - Biến cấu hình `app.payment.simulation-enabled` chỉ nhận giá trị `true` khi chạy cấu hình phát triển cục bộ hoặc test suite.
2. **Ngăn chặn tuyệt đối trên `prod`**:
   - `application-prod.yml` khóa cứng `app.payment.simulation-enabled: false`.
   - `ProductionSecurityValidator` chạy tại `@PostConstruct` trong profile `prod`: Nếu phát hiện `paymentSimulationEnabled == true` $\rightarrow$ Ném ngoại lệ `IllegalStateException` và hủy quá trình khởi động máy chủ (Fail-fast).
   - `PaymentServiceImpl`: Kiểm tra tại runtime:
     ```java
     if (Arrays.asList(environment.getActiveProfiles()).contains("prod") || !simulationEnabled) {
         throw new AppException(ErrorCode.PAYMENT_SIMULATION_DISABLED);
     }
     ```
     Đảm bảo ngay cả khi cố tình cấu hình sai, phương thức simulate payment vẫn bị từ chối 100% trên production.

---

## 7. Khế Ước API (API Contracts)

### 7.1. Khởi Tạo Thanh Toán
- **Endpoint**: `POST /api/v1/payments/create`
- **Quyền**: `hasRole('STUDENT')`
- **Request Body**:
  ```json
  {
    "orderId": 123,
    "paymentMethod": "VNPAY",
    "returnUrl": "http://localhost:5173/student/checkout/vnpay-return"
  }
  ```
- **Response Body**:
  ```json
  {
    "code": 1000,
    "message": "Khởi tạo thanh toán thành công",
    "result": {
      "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=...",
      "transactionCode": "TXN-ORD-123456-ABCDEF",
      "paymentMethod": "VNPAY",
      "paymentProvider": "VNPAY",
      "amount": 500000.00
    }
  }
  ```

### 7.2. VNPay IPN Webhook
- **Endpoint**: `GET /api/v1/payments/vnpay/ipn` hoặc `POST /api/v1/payments/vnpay/ipn`
- **Quyền**: Public (`permitAll()`), xác thực bằng chữ ký HMAC-SHA512
- **Parameters**: `vnp_TxnRef`, `vnp_Amount`, `vnp_ResponseCode`, `vnp_TransactionNo`, `vnp_SecureHash`, ...
- **Response Format (Chuẩn VNPay IPN)**:
  ```json
  {
    "RspCode": "00",
    "Message": "Confirm Success"
  }
  ```
  Các mã lỗi phản hồi VNPay:
  - `"00"`: Giao dịch thành công hoặc đã xử lý xong.
  - `"01"`: Order không tồn tại (`Order not found`).
  - `"02"`: Đơn hàng đã được xác nhận trước đó (`Order already confirmed` - Idempotent ACK).
  - `"04"`: Số tiền không hợp lệ (`Invalid Amount`).
  - `"97"`: Sai chữ ký kiểm tra (`Invalid Checksum`).

### 7.3. Generic Payment Webhook
- **Endpoint**: `POST /api/v1/payments/webhook`
- **Quyền**: Public (`permitAll()`), xác thực bằng chữ ký header hoặc signature trong payload.

---

## 8. Ma Trận Kịch Bản Kiểm Thử Tích Hợp (Integration Test Scenarios)

| STT | Kịch bản kiểm thử | Dữ liệu đầu vào | Kết quả mong đợi |
|:---:|---|---|---|
| 1 | **Valid Payment Event** | Webhook hợp lệ, đúng chữ ký HMAC-SHA512, responseCode = "00", số tiền khớp đơn hàng | Order chuyển sang `PAID`, Payment lưu `SUCCESS`, CourseEnrollment `ACTIVE`, `totalStudents` tăng 1 |
| 2 | **Duplicate Payment Event** | Webhook thành công gửi lại lần 2 cho đơn hàng đã `PAID` | Phản hồi thành công (RspCode 02/00), **không** tạo thêm Payment, **không** tạo thêm Enrollment, `totalStudents` giữ nguyên |
| 3 | **Failed Payment Event** | Webhook hợp lệ, responseCode = "24" (khách hủy) hoặc "99" (lỗi) | Order chuyển sang `FAILED` hoặc `CANCELLED`, Payment lưu `FAILED`, **không** kích hoạt Enrollment, `totalStudents` không tăng |
| 4 | **Expired Payment Event** | Webhook hợp lệ, responseCode = "11" (hết hạn giao dịch) | Order chuyển sang `EXPIRED`, Payment lưu `FAILED`, **không** kích hoạt Enrollment |
| 5 | **Forged Payment Event** | Webhook bị sai chữ ký HMAC-SHA512 hoặc bị sửa đổi số tiền `vnp_Amount` | Trả về lỗi sai chữ ký (RspCode 97), Order giữ nguyên `PENDING`, không cập nhật DB |
| 6 | **Production Simulation Guard** | Gọi `/simulate` khi cấu hình profile `prod` hoặc `simulation-enabled=false` | Trả về lỗi 403 / `PAYMENT_SIMULATION_DISABLED`, startup bị chặn nếu bật cờ trên prod |
