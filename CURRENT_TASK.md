# CURRENT TASK

## Task hiện tại
Backend Paid Course Order Foundation

## Trạng thái
TODO

## Mục tiêu
Tạo nền tảng backend cho luồng mua một khóa học trả phí: lưu order và order item theo snapshot giá/tên khóa học, cho học viên tạo order `PENDING` an toàn và xem order của chính mình. Task này chuẩn bị contract chắc chắn để task payment simulation hoặc tích hợp payment provider sau đó không phải sửa lại domain order.

## Vì sao làm task này?
Các public/student flows và UI navigation đã được harden. Hiện Course Detail đã phân biệt FREE/PAID nhưng CTA "Mua khóa học" vẫn chưa có business flow phía sau. Theo roadmap sau Enrollment, bước hợp lý nhất là Order Foundation trước khi làm payment provider:
- Order lưu được giá và tên khóa học tại thời điểm mua, không phụ thuộc dữ liệu course bị sửa sau này.
- Payment task sau có một `PENDING` order rõ ràng để xử lý.
- Enrollment chỉ được tạo khi order đã thanh toán thành công, không dựa vào thông báo từ frontend.
- Scope nhỏ, testable, không cần secret hoặc cổng thanh toán thật.

## Không làm trong task này
- Không tích hợp VNPay, MoMo, ZaloPay, Stripe hoặc bank transfer.
- Không tạo payment callback/webhook.
- Không đánh dấu order là `PAID` qua frontend.
- Không tự tạo enrollment cho khóa trả phí.
- Không làm coupon, cart nhiều khóa học hoặc admin order management.
- Không làm frontend checkout page hoàn chỉnh; chỉ chốt API contract cho task frontend/payment tiếp theo.

## Tài liệu cần đọc
- `docs/00_MASTER_CONTEXT.md`
- `docs/05_features/05_08_PAYMENT_ORDER_FEATURES.md`
- `docs/07_database/07_03_PAYMENT_ORDER.md`
- `docs/08_api/08_08_PAYMENT_ORDER_API.md`
- `docs/24_USER_FLOWS.md`
- `docs/26_API_PRIORITY.md`
- `docs/30_PERMISSION_MATRIX.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/18_CODE_CONVENTIONS.md`

## File dự kiến tạo/chỉnh sửa
- `backend/src/main/resources/db/migration/V7__create_order_tables.sql`
- `backend/src/main/java/com/japaneselearning/module_order/**`
- `backend/src/main/java/com/japaneselearning/common/exception/ErrorCode.java`
- `backend/src/main/java/com/japaneselearning/common/config/SecurityConfig.java` nếu cần khai báo route permission rõ hơn
- `backend/src/test/java/com/japaneselearning/module_order/**`
- `docs/learning/LEARNING_LOG.md`
- `docs/learning/INTERVIEW_NOTES.md`
- `docs/learning/CONCEPTS_EXPLAINED.md`
- `docs/learning/BUG_LOG.md` nếu phát hiện lỗi

## Phạm vi thực hiện

### 1. Data Model & Migration
- Tạo migration `V7__create_order_tables.sql` cho `orders` và `order_items`.
- `orders` cần có tối thiểu: user, unique order code, total amount, discount amount, final amount, status, created/updated timestamps.
- `order_items` cần lưu course reference, `course_title` snapshot, `price` snapshot và quantity.
- Tạo indexes/unique constraints cần thiết, tương thích MariaDB và H2 integration tests.
- Không tạo bảng `payments`, `coupons`, `coupon_usages` trong task này để giữ scope order foundation rõ ràng.

### 2. Domain & API Contract
- Tạo `Order`, `OrderItem`, `OrderStatus` và DTO request/response theo convention module hiện có.
- `POST /api/v1/orders` nhận một `courseId` cho checkout từ Course Detail.
- `GET /api/v1/orders/me` trả danh sách order của current user.
- `GET /api/v1/orders/{id}` chỉ cho phép owner xem order của mình; admin permission sẽ được làm ở task quản lý order riêng.
- Response phải dùng `ApiResponse`, không trả Entity trực tiếp.

### 3. Business & Security Rules
- Chỉ user đã authenticated có role STUDENT mới tạo/xem order của mình.
- Course phải tồn tại, `PUBLISHED` và có type `PAID`.
- Snapshot title/giá ở thời điểm tạo order; không tin giá từ client.
- Không tạo order nếu user đã có enrollment active/completed cho course.
- Xác định policy cho duplicate pending order cùng course (tái sử dụng pending order còn hiệu lực hoặc trả lỗi rõ ràng) và khóa bằng test.
- Mọi order mới bắt đầu ở `PENDING`; task này tuyệt đối không có API client-side chuyển order sang `PAID`.

### 4. Testing
- Unit test service: course không tồn tại, course không published, free course, user đã enrolled, duplicate pending policy, price snapshot và order code uniqueness.
- Integration test controller: unauthenticated, non-student forbidden, owner đọc được order, user khác không đọc được order.
- Chạy `mvn clean verify` bằng JDK 21.
- Ghi rõ blocker nếu local environment không có JDK 21.

## Checklist
- [ ] Migration V7 tạo được `orders` và `order_items` trên H2/MariaDB.
- [ ] Order entity không expose trực tiếp qua API.
- [ ] Student tạo được `PENDING` order cho một paid published course.
- [ ] Giá và title được snapshot từ backend course data.
- [ ] Free/draft/hidden course không tạo được order.
- [ ] User đã enrolled không tạo order mới cho cùng course.
- [ ] Duplicate pending order policy rõ ràng và có test.
- [ ] User chỉ xem được order của chính mình.
- [ ] Không có endpoint cho frontend đánh dấu order `PAID`.
- [ ] Backend tests pass hoặc blocker JDK 21 được ghi rõ.
- [ ] Learning docs được cập nhật sau khi hoàn tất.
- [ ] Đề xuất task tiếp theo: Payment Simulation & Paid Enrollment Completion.

## Gợi ý branch
`feature/346-Backend-Paid-Course-Order-Foundation`
