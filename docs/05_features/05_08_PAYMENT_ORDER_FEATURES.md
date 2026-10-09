> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

## 5.8. Nhóm chức năng thanh toán

### 5.8.1. Đơn hàng

Đơn hàng gồm:

```text
Người mua
Danh sách khóa học
Tổng tiền
Mã giảm giá
Số tiền giảm
Số tiền phải trả
Trạng thái đơn hàng
Ngày tạo
```

### 5.8.2. Thanh toán

Tích hợp cổng thanh toán:

```text
VNPay
Momo
ZaloPay
PayPal, nếu bán quốc tế
Stripe, nếu bán quốc tế
```

Luồng thanh toán:

```text
User chọn khóa học
Tạo order pending
Tạo payment request
Chuyển user sang cổng thanh toán
Cổng thanh toán gọi webhook/callback
Backend xác thực giao dịch
Cập nhật payment success
Cập nhật order paid
Mở khóa học cho user
Gửi email xác nhận
```

### 5.8.3. Mã giảm giá

Coupon gồm:

```text
Mã coupon
Loại giảm: percent/fixed
Giá trị giảm
Ngày bắt đầu
Ngày kết thúc
Số lần sử dụng tối đa
Số lần mỗi user được dùng
Áp dụng cho khóa học nào
Trạng thái
```

---
