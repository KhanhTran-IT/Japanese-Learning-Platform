> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

# 10.3. Course API Public

```http
GET /api/courses
GET /api/courses/{slug}
GET /api/courses/{id}/sections
GET /api/courses/{id}/lessons
GET /api/courses/{id}/reviews
POST /api/courses/{id}/reviews
POST /api/courses/{id}/enroll
```

Query mẫu:

```http
GET /api/courses?page=0&size=12&level=N5&type=PAID&keyword=kanji
```

---
