> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

# 10.6. Flashcard API

```http
GET /api/flashcard-decks
GET /api/flashcard-decks/{id}
GET /api/flashcard-decks/{id}/items
POST /api/flashcards/{id}/review
GET /api/users/me/flashcards/review-today
```

Review request:

```json
{
  "memoryLevel": "HARD"
}
```

---
