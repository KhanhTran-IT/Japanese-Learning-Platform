> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

# 10.8. Payment/Order API

```http
POST /api/orders
GET /api/orders/me
GET /api/orders/{id}
POST /api/payments/create
POST /api/payments/vnpay/callback
POST /api/payments/momo/callback
POST /api/payments/webhook
```

Create order request:

```json
{
  "courseIds": [1, 2],
  "couponCode": "SALE20"
}
```

---
