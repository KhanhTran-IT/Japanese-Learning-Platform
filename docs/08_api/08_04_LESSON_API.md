> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

# 10.4. Lesson API

```http
GET /api/lessons/{id}
POST /api/lessons/{id}/progress
POST /api/lessons/{id}/complete
GET /api/lessons/{id}/resources
GET /api/lessons/{id}/quiz
```

Progress request:

```json
{
  "watchedPercent": 75.5
}
```

---
