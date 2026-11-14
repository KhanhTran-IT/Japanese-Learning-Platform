# Chính Sách Lưu Trữ Và Kiểm Soát Truy Cập Nhật Ký Kiểm Toán (Audit Retention & Access Policy)

## 1. Mục Đích & Nguyên Tắc Thiết Kế (Purpose & Core Principles)

Hệ thống nhật ký kiểm toán (Audit Trail) của nền tảng học tiếng Nhật trực tuyến được thiết kế nhằm mục đích:
- Ghi vết độc lập, bảo mật và toàn vẹn mọi hành vi nhạy cảm liên quan đến xác thực, phân quyền, quản trị người dùng, giao dịch tài chính (đơn hàng & thanh toán), và quản lý nội dung khóa học.
- Đảm bảo tính pháp lý, tuân thủ các tiêu chuẩn bảo mật (SOC 2, ISO 27001, GDPR, PCI-DSS).
- Hỗ trợ điều tra sự cố an ninh mạng (forensic analysis) và truy vết luồng phân tán thông qua Correlation ID.

### Nguyên tắc cốt lõi:
1. **Append-Only (Bất biến & Chỉ thêm)**: Nhật ký kiểm toán là dữ liệu bất biến. Nghiêm cấm mọi hành vi cập nhật (UPDATE) hoặc xóa (DELETE) bản ghi thông qua ứng dụng nghiệp vụ.
2. **Data Minimization & Redaction (Bảo mật thông tin nhạy cảm)**: Tuyệt đối không lưu trữ plaintext mật khẩu, JWT token, cookie phiên, khóa bí mật cổng thanh toán hoặc thông tin thẻ tín dụng/CVV trong metadata. Mọi dữ liệu nhạy cảm phải được che mờ (redact) trước khi ghi xuống ổ đĩa.
3. **Consistency-Aware Persistence (Kiên định theo mức độ rủi ro)**: Phân tách rõ ràng giữa ghi đồng bộ (Synchronous với transaction độc lập `REQUIRES_NEW`) cho các hành động thay đổi trạng thái tài chính/bảo mật cốt lõi và ghi bất đồng bộ (Asynchronous) cho các sự kiện tần suất cao nhằm không làm suy giảm hiệu năng API.

---

## 2. Danh Mục Các Hành Vi Được Kiểm Toán (Audited Actions Matrix)

| Phân Loại | Mã Hành Động (`AuditAction`) | Đối Tượng (`targetType`) | Cơ Chế Ghi | Mô Tả & Mục Đích |
|-----------|------------------------------|--------------------------|------------|------------------|
| **Xác thực** | `AUTH_LOGIN` | `USER` | Asynchronous | Ghi nhận đăng nhập thành công hoặc thất bại (sai pass, lock). |
| **Xác thực** | `AUTH_LOGOUT` | `USER` | Asynchronous | Ghi nhận đăng xuất và chấm dứt phiên làm việc. |
| **Xác thực** | `AUTH_REGISTER` | `USER` | Synchronous | Ghi nhận tạo tài khoản học viên mới. |
| **Xác thực** | `AUTH_REFRESH_TOKEN` | `USER` | Asynchronous | Ghi nhận cấp mới access token qua refresh token rotation. |
| **Bảo mật** | `AUTH_TOKEN_REVOKED` | `USER` | Synchronous | Ghi nhận phát hiện gian lận/sử dụng lại refresh token (Token Reuse). |
| **Bảo mật** | `PASSWORD_CHANGE` | `USER` | Synchronous | Ghi nhận đổi mật khẩu và thu hồi các phiên truy cập cũ. |
| **Quản trị** | `USER_LOCK` | `USER` | Synchronous | Quản trị viên khóa tài khoản người dùng vi phạm. |
| **Quản trị** | `USER_UNLOCK` | `USER` | Synchronous | Quản trị viên mở khóa tài khoản. |
| **Quản trị** | `USER_DELETE` | `USER` | Synchronous | Quản trị viên xóa (soft delete) tài khoản người dùng. |
| **Quản trị** | `USER_ROLE_UPDATE` | `USER` | Synchronous | Cập nhật phân quyền (Roles/Authorities) của tài khoản. |
| **Người dùng**| `USER_PROFILE_UPDATE` | `USER` | Synchronous | Học viên/giáo viên thay đổi thông tin cá nhân. |
| **Khóa học** | `COURSE_CREATE` | `COURSE` | Synchronous | Giảng viên/Admin tạo mới khóa học. |
| **Khóa học** | `COURSE_UPDATE` | `COURSE` | Synchronous | Cập nhật cấu hình, giá, hoặc mô tả khóa học. |
| **Khóa học** | `COURSE_PUBLISH` | `COURSE` | Synchronous | Xuất bản khóa học lên hệ thống công khai. |
| **Khóa học** | `COURSE_DELETE` | `COURSE` | Synchronous | Lưu trữ (archive) hoặc gỡ khóa học. |
| **Ghi danh** | `COURSE_ENROLLMENT_CREATE` | `ENROLLMENT` | Synchronous | Kích hoạt quyền vào học khóa học (miễn phí hoặc sau thanh toán). |
| **Đơn hàng** | `ORDER_CREATE` | `ORDER` | Synchronous | Tạo đơn hàng mua khóa học kèm giá snapshot và idempotency key. |
| **Đơn hàng** | `ORDER_STATUS_UPDATE` | `ORDER` | Synchronous | Thay đổi trạng thái đơn hàng (PAID, CANCELLED, EXPIRED). |
| **Thanh toán**| `PAYMENT_INITIATED` | `PAYMENT` | Synchronous | Khởi tạo giao dịch thanh toán (VNPay redirect). |
| **Thanh toán**| `PAYMENT_COMPLETED` | `PAYMENT` | Synchronous | Ghi nhận giao dịch thanh toán thành công từ Webhook/IPN. |
| **Thanh toán**| `PAYMENT_CANCELLED` | `PAYMENT` | Synchronous | Khách hàng hủy giao dịch thanh toán. |
| **Thanh toán**| `PAYMENT_FAILED` | `PAYMENT` | Synchronous | Giao dịch thanh toán thất bại từ cổng thanh toán. |
| **Thanh toán**| `PAYMENT_WEBHOOK_RECEIVED` | `PAYMENT` | Asynchronous | Tiếp nhận callback/IPN thô từ cổng thanh toán đối tác. |

---

## 3. Kiến Trúc Kỹ Thuật Đảm Bảo Append-Only (Append-Only Guarantees)

Tính bất biến và append-only được bảo vệ chặt chẽ qua 3 tầng:

1. **Tầng Thực Thể (Domain Entity Layer)**:
   - Thực thể `AuditLog` được gắn annotation `@Immutable` của Hibernate ORM. Bất kỳ thao tác dirty-checking nào trong persistence context cũng sẽ không sinh ra câu lệnh `UPDATE`.
   - Lớp `AuditLog` không cung cấp setter methods, chỉ khởi tạo qua Builder hoặc Constructor.
2. **Tầng Lưu Trữ (Repository Layer)**:
   - `AuditLogRepository` ghi đè toàn bộ các phương thức `delete()`, `deleteById()`, `deleteAll()` và ném ra `UnsupportedOperationException("Audit logs are append-only and cannot be deleted")`.
3. **Tầng Giao Diện Lập Trình (API Layer)**:
   - Bộ điều khiển `AdminAuditLogController` chỉ cung cấp duy nhất phương thức HTTP `GET` để tra cứu và lọc dữ liệu. Không cung cấp bất kỳ API nào cho phép `POST`, `PUT`, `PATCH`, hoặc `DELETE` đối với nhật ký kiểm toán.
4. **Tầng Cơ Sở Dữ Liệu (Database Layer)**:
   - Trong môi trường sản xuất, tài khoản kết nối của ứng dụng đối với bảng `audit_logs` có thể được cấp quyền `INSERT, SELECT` mà không có quyền `UPDATE, DELETE, TRUNCATE` (Least Privilege Database Role).

---

## 4. Chính Sách Khử Nhạy & Che Mờ Dữ Liệu (Data Redaction Policy)

Mọi dữ liệu bổ trợ (`metadata`) gắn kèm audit event đều phải đi qua `AuditDataRedactor` trước khi tuần tự hóa sang JSON và lưu vào CSDL:

- **Thông tin xác thực (Credentials)**:
  - Các trường: `password`, `currentPassword`, `newPassword`, `confirmPassword`, `passwordHash` đều bị thay thế bởi `[REDACTED]`.
- **Token phiên & Mã ủy quyền (Session & Authorization)**:
  - Các trường: `token`, `accessToken`, `refreshToken`, `tokenHash`, `jwt`, `cookie`, `set-cookie`, `authorization` đều bị thay thế bởi `[REDACTED]`.
  - Mọi chuỗi ký tự khớp mẫu định dạng JWT (`Bearer eyJ...` hoặc chuỗi base64url 3 đoạn) đều được tự động thay thế bằng `[REDACTED_JWT]`.
- **Bí mật cổng thanh toán (Payment Secrets & Financial PII)**:
  - Các trường: `vnp_HashSecret`, `hashSecret`, `vnp_SecureHash`, `signature`, `secret`, `secretKey`, `cvv`, `cvv2` đều bị thay thế bởi `[REDACTED]`.
  - Các chuỗi số khớp định dạng số thẻ tín dụng (Visa, MasterCard, JCB, Amex: 13-19 chữ số) đều được che mờ thành `[REDACTED_CARD]`.
- **Thông tin định danh người dùng (Personal Identifiable Information - PII)**:
  - Chỉ lưu trữ `actor_email` và `actor_id` để phục vụ truy vết trách nhiệm giải trình.
  - Số điện thoại cá nhân trong metadata được rút gọn hoặc che mờ.

---

## 5. Thời Hạn Lưu Trữ Dữ Liệu (Retention Schedule)

Dữ liệu nhật ký kiểm toán được phân loại và lưu trữ theo thời hạn quy định:

| Nhóm Nhật Ký | Thời Hạn Lưu Trữ Tối Thiểu | Căn Cứ Pháp Lý & Lý Do Nghiệp Vụ | Hành Động Sau Hết Hạn |
|--------------|---------------------------|----------------------------------|------------------------|
| **Tài chính & Thanh toán** (`ORDER_*`, `PAYMENT_*`) | **07 năm** | Luật Kế toán & Quy định về chứng từ giao dịch thương mại điện tử | Xuất nén lưu trữ lạnh (Cold Storage - S3 Glacier) |
| **Bảo mật & Phân quyền** (`AUTH_*`, `USER_LOCK`, `USER_ROLE_*`, `PASSWORD_CHANGE`) | **03 năm** | Tiêu chuẩn an ninh thông tin ISO 27001 & Truy vết an ninh mạng | Chuyển sang kho lưu trữ nén bảo mật |
| **Nghiệp vụ khóa học & nội dung** (`COURSE_*`, `LESSON_*`, `ENROLLMENT_*`) | **01 năm** | Quản lý vòng đời sản phẩm đào tạo | Lưu trữ tổng hợp (Aggregated Reports) |
| **Nhật ký kết nối mạng & User-Agent** | **180 ngày** | Quy định về an ninh mạng đối với lưu trữ IP/User-Agent | Tẩy bỏ thông tin IP sau thời hạn |

---

## 6. Chính Sách Kiểm Soát Quyền Truy Cập (Access Control Policy)

1. **Phân quyền tối thiểu (Role-Based Access Control - RBAC)**:
   - Chỉ người dùng có vai trò `ROLE_ADMIN` hoặc `ROLE_SUPER_ADMIN` mới có quyền gọi API tra cứu nhật ký kiểm toán (`GET /api/v1/admin/audit-logs`).
   - Mọi nỗ lực truy cập từ tài khoản `ROLE_STUDENT`, `ROLE_TEACHER` hoặc chưa xác thực đều bị Spring Security từ chối với mã HTTP `401 Unauthorized` hoặc `403 Forbidden`.
2. **Kiểm toán chính người kiểm toán (Auditing the Auditors)**:
   - Các truy vấn tìm kiếm nhật ký kiểm toán của quản trị viên được ghi nhận lại trong log hệ thống cùng với `correlationId` và `clientIp` để ngăn chặn việc lạm quyền tra cứu thông tin học viên.
3. **Truy xuất phục vụ điều tra (Forensic Investigation)**:
   - Dữ liệu audit logs có thể được liên kết chéo với ứng dụng giám sát tập trung (Prometheus, Grafana, OpenSearch/ELK) thông qua trường `correlation_id` (được sinh tự động từ `CorrelationIdFilter`).

---

## 7. Quy Trình Lưu Trữ Lạnh & Dọn Dẹp (Archival & Purging Procedures)

- **Phân vùng CSDL (Partitioning)**:
  - Khi quy mô dữ liệu vượt quá 10 triệu bản ghi, bảng `audit_logs` được khuyến nghị cấu hình phân vùng dạng `RANGE PARTITIONING BY (YEAR(created_at), MONTH(created_at))`.
- **Quy trình lưu trữ lạnh (Cold Storage Archival)**:
  - Định kỳ hàng quý, một cronjob chuyên dụng xuất các bản ghi cũ hơn thời hạn quy định ra file nén định dạng Parquet/CSV có mã hóa AES-256 và chữ ký số SHA-256 checksum.
  - Tải file nén lên vùng lưu trữ chống ghi đè (AWS S3 Object Lock / WORM Storage - Write Once, Read Many).
- **Loại bỏ dữ liệu (Safe Purge)**:
  - Chỉ được thực thi lệnh `DROP PARTITION` đối với các phân vùng đã hoàn tất việc lưu trữ lạnh và có biên bản phê duyệt từ cán bộ phụ trách an toàn thông tin (CISO / DPO).
