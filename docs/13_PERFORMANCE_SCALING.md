> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

## 16. Hiệu năng và khả năng mở rộng

### 16.1. Cache Redis

Nên cache:

```text
Trang chủ
Danh sách khóa học
Chi tiết khóa học
Danh sách chương/bài học public
Cấu hình website
Banner
Leaderboard
JLPT vocabulary/kanji/grammar phổ biến
```

### 16.2. Pagination

Tất cả API danh sách phải phân trang:

```http
GET /api/courses?page=0&size=12
GET /api/admin/users?page=0&size=20
GET /api/admin/orders?page=0&size=20
GET /api/vocabularies?page=0&size=50
```

### 16.3. Tối ưu database

Cần:

```text
Index các cột tìm kiếm/lọc/join
Không SELECT * nếu không cần
Dùng DTO projection cho danh sách lớn
Tối ưu query N+1 trong JPA
Theo dõi slow query
Backup định kỳ
```

### 16.4. Tối ưu video/audio

Không nên:

```text
Upload video trực tiếp vào server backend
Lưu video trong database
Stream video qua Spring Boot nếu không cần
```

Nên:

```text
Upload video lên object storage/CDN
Backend chỉ lưu URL/key
Dùng CDN để phân phối video
Có thể dùng Bunny Stream/Mux/Vimeo nếu cần bảo vệ video tốt hơn
```

### 16.5. Tối ưu frontend

Cần:

```text
Lazy load route
Code splitting
Compress ảnh
Dùng CDN cho asset tĩnh
Tối ưu bundle size
Dùng loading skeleton
Dùng pagination/infinite scroll hợp lý
```

---
