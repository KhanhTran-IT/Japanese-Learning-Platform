> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

# 10.9. Japanese Content API

```http
GET /api/jlpt-levels
GET /api/vocabularies
GET /api/vocabularies/{id}
GET /api/kanjis
GET /api/kanjis/{id}
GET /api/grammar-points
GET /api/grammar-points/{id}
```

Query mẫu:

```http
GET /api/vocabularies?level=N5&topic=food&page=0&size=20
GET /api/kanjis?level=N4&page=0&size=20
GET /api/grammar-points?level=N3&keyword=ながら
```

---
