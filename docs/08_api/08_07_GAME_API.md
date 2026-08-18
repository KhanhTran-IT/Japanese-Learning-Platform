> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

# 10.7. Game API

```http
GET /api/games
GET /api/games/{slug}
POST /api/games/{id}/start
POST /api/games/{id}/submit
GET /api/games/{id}/leaderboard
GET /api/games/leaderboard/daily
GET /api/games/leaderboard/weekly
GET /api/games/leaderboard/monthly
```

Submit game request:

```json
{
  "sessionId": 10,
  "score": 850,
  "correctCount": 17,
  "wrongCount": 3
}
```

---
