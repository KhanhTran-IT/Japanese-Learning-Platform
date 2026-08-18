> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

## 23. Quy chuẩn code đề xuất

### 23.1. Backend

```text
Controller chỉ nhận request và trả response
Service xử lý nghiệp vụ
Repository chỉ thao tác database
Entity không trả trực tiếp ra frontend
DTO dùng cho request/response
Mapper chuyển Entity <-> DTO
Exception xử lý tập trung ở GlobalExceptionHandler
Validate bằng annotation
Không hard-code text lỗi rải rác
```

### 23.2. Frontend

```text
Component nhỏ, dễ tái sử dụng
Page chỉ điều phối component
Service riêng để gọi API
Store chỉ lưu state dùng chung
Router guard kiểm tra quyền
Form validate rõ ràng
Không gọi API lung tung trong nhiều component nếu có thể gom service
```

---
