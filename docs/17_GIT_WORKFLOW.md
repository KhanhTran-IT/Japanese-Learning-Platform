> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

## 22. Git workflow đề xuất

```text
main        -> code production ổn định
develop     -> code tích hợp để test
feature/*   -> nhánh phát triển chức năng
bugfix/*    -> nhánh sửa lỗi
hotfix/*    -> sửa lỗi gấp production
release/*   -> chuẩn bị phát hành
```

Ví dụ:

```text
feature/auth
feature/course-management
feature/payment
feature/quiz
feature/game-flashcard
bugfix/login-token-expired
hotfix/payment-webhook-error
```

---
