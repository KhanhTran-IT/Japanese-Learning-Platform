> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

# 10.5. Quiz API

```http
GET /api/v1/quizzes/{id}
POST /api/v1/quizzes/{id}/start
POST /api/v1/quizzes/{id}/submit
GET /api/v1/quizzes/{id}/result/{attemptId}
GET /api/v1/lessons/{lessonId}/quizzes
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
