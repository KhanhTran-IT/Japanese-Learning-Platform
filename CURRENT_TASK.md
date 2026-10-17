# CURRENT TASK

## Task hiện tại
Backend Paid Course Order Foundation

## Trạng thái
DONE

## Mục tiêu
Tạo nền tảng backend cho luồng mua một khóa học trả phí: lưu order và order item theo snapshot giá/tên khóa học, cho học viên tạo order `PENDING` an toàn và xem order của chính mình. Task này chuẩn bị contract chắc chắn để task payment simulation hoặc tích hợp payment provider sau đó không phải sửa lại domain order.

## Kết quả đạt được
1. **Migration & Data Model:**
   - Tạo Flyway migration `V7__create_order_tables.sql` tạo hai bảng `orders` và `order_items`.
   - Đã migrate và kiểm tra thành công trên cả H2 (in-memory test) và MariaDB 11.8 (dev database).
   - Entity `Order` và `OrderItem` ánh xạ đầy đủ constraints, index và snapshot fields.
2. **Domain & API Contract:**
   - Enum `OrderStatus`: `PENDING`, `PAID`, `CANCELLED`, `FAILED`, `REFUNDED`, `EXPIRED`.
   - DTOs: `OrderCreateReq`, `OrderItemRes`, `OrderRes`.
   - Endpoints:
     - `POST /api/v1/orders`: Tạo order với status `PENDING`, snapshot title và effective price (`salePrice > 0 ? salePrice : originalPrice`).
     - `GET /api/v1/orders/me`: Danh sách order của current student.
     - `GET /api/v1/orders/{id}`: Xem chi tiết order (kiểm tra ownership, chặn cross-user access với 403 Forbidden).
   - Phản hồi chuẩn hóa qua `ApiResponse<T>`, không expose Entity trực tiếp.
3. **Business & Security Rules:**
   - Phân quyền chặt chẽ: `SecurityConfig` và `@PreAuthorize("hasRole('STUDENT')")` đảm bảo chỉ học viên mới được thao tác với orders. Unauthenticated trả về 401, non-student (TEACHER, ADMIN) trả về 403.
   - Khóa học phải tồn tại (`4001 COURSE_NOT_FOUND`), phải `PUBLISHED` (`4007 COURSE_NOT_AVAILABLE_FOR_ENROLLMENT`), và phải là `PAID` (`5003 ORDER_COURSE_NOT_PAID`).
   - Kiểm tra học viên đã ghi danh: Nếu có enrollment `ACTIVE` hoặc `COMPLETED`, từ chối tạo order với `4009 USER_ALREADY_ENROLLED`.
   - **Chốt policy duplicate pending order:** Nếu học viên đã có order đang ở trạng thái `PENDING` chứa cùng khóa học, từ chối tạo mới với mã lỗi rõ ràng `5004 ORDER_DUPLICATE_PENDING` (409 Conflict) nhằm chống double-order và bảo toàn tính nhất quán nghiệp vụ.
   - Bảo mật dữ liệu: Cross-user access bị từ chối với `5005 ORDER_ACCESS_DENIED` (403 Forbidden).
4. **Testing & Quality:**
   - `OrderServiceImplTest`: 13 unit tests bao phủ 100% các kịch bản business rule, edge cases, snapshotting, format orderCode và duplicate pending policy.
   - `OrderControllerIT`: 11 integration tests kiểm tra toàn diện bảo mật (401 unauthenticated, 403 non-student, 200 owner, 403 cross-user access).
   - `mvn clean verify`: **PASS 67/67 tests**, `BUILD SUCCESS` trên OpenJDK 21.

## Checklist
- [x] Migration V7 tạo được `orders` và `order_items` trên H2/MariaDB.
- [x] Order entity không expose trực tiếp qua API.
- [x] Student tạo được `PENDING` order cho một paid published course.
- [x] Giá và title được snapshot từ backend course data.
- [x] Free/draft/hidden course không tạo được order.
- [x] User đã enrolled không tạo order mới cho cùng course.
- [x] Duplicate pending order policy rõ ràng và có test.
- [x] User chỉ xem được order của chính mình.
- [x] Không có endpoint cho frontend đánh dấu order `PAID`.
- [x] Backend tests pass hoặc blocker JDK 21 được ghi rõ (`mvn clean verify` 67/67 tests passed).
- [x] Learning docs được cập nhật sau khi hoàn tất.
- [x] Đề xuất task tiếp theo: Payment Simulation & Paid Enrollment Completion.

## Gợi ý branch
`feature/348-Backend-Paid-Course-Order-Foundation`
