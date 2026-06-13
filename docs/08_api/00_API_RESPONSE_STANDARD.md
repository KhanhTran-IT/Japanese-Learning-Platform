> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

## 9. API response chuẩn

### 9.1. Response thành công

```json
{
  "success": true,
  "message": "Lấy dữ liệu thành công",
  "data": {},
  "errors": null,
  "timestamp": "2026-06-07T10:30:00"
}
```

### 9.2. Response lỗi validation

```json
{
  "success": false,
  "message": "Dữ liệu không hợp lệ",
  "data": null,
  "errors": [
    {
      "field": "email",
      "message": "Email không đúng định dạng"
    }
  ],
  "timestamp": "2026-06-07T10:30:00"
}
```

### 9.3. Response phân trang

```json
{
  "success": true,
  "message": "Lấy danh sách thành công",
  "data": {
    "items": [],
    "page": 0,
    "size": 10,
    "totalItems": 100,
    "totalPages": 10,
    "hasNext": true,
    "hasPrevious": false
  },
  "errors": null,
  "timestamp": "2026-06-07T10:30:00"
}
```

---
