> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

# 10.5. Quiz API

```http
GET /api/quizzes/{id}
POST /api/quizzes/{id}/start
POST /api/quizzes/{id}/submit
GET /api/quizzes/{id}/result/{attemptId}
GET /api/users/me/quiz-attempts
```

Submit request:

```json
{
  "attemptId": 100,
  "answers": [
    {
      "questionId": 1,
      "answerId": 3
    },
    {
      "questionId": 2,
      "userAnswerText": "日本語"
    }
  ]
}
```

---
