# 28. ENUM_DEFINITIONS - Chuẩn enum toàn hệ thống

## 1. Mục đích của file

File này gom toàn bộ enum để code Java, database và frontend không bị lệch tên.

Nguyên tắc:

```text
Enum dùng UPPER_SNAKE_CASE.
Database lưu enum dạng VARCHAR.
Backend dùng Java enum.
Frontend dùng constant map để hiển thị label tiếng Việt.
Không hard-code enum rải rác trong code.
```

## 2. UserStatus

```text
ACTIVE      Tài khoản hoạt động bình thường
INACTIVE    Tài khoản chưa hoạt động hoặc tạm ẩn
LOCKED      Tài khoản bị khóa
DELETED     Tài khoản đã xóa mềm
```

Dùng cho bảng:

- `users.status`

## 3. RoleName

```text
SUPER_ADMIN
ADMIN
TEACHER
CONTENT_EDITOR
STUDENT
GUEST
```

Ghi chú:

- `GUEST` thường không cần lưu database role, vì là người chưa đăng nhập.
- MVP chỉ cần `ADMIN`, `STUDENT`, có thể thêm `SUPER_ADMIN` nếu cần.

## 4. CourseLevel

```text
N5
N4
N3
N2
N1
BASIC
ADVANCED
```

Dùng cho:

- `courses.level`
- filter course
- Japanese content mapping nếu cần

## 5. CourseType

```text
FREE              Khóa học miễn phí
PAID              Khóa học trả phí
MEMBERSHIP_ONLY   Chỉ dành cho membership
```

Dùng cho:

- `courses.course_type`

## 6. CourseStatus

```text
DRAFT       Nháp, chưa public
PUBLISHED   Đã xuất bản
HIDDEN      Ẩn khỏi public
ARCHIVED    Lưu trữ, không còn sử dụng
```

Dùng cho:

- `courses.status`
- `course_sections.status`
- `lessons.status`

## 7. ResourceType

```text
PDF
IMAGE
AUDIO
VIDEO
DOCUMENT
LINK
```

Dùng cho:

- `lesson_resources.resource_type`

## 8. EnrollmentStatus

```text
ACTIVE       Đang học
EXPIRED      Hết hạn truy cập
COMPLETED    Hoàn thành khóa học
CANCELLED    Đã hủy
```

Dùng cho:

- `course_enrollments.status`

## 9. ReviewStatus

```text
VISIBLE
HIDDEN
PENDING
REJECTED
```

Dùng cho:

- `course_reviews.status`

## 10. OrderStatus

```text
PENDING     Đơn mới tạo, chưa thanh toán
PAID        Đã thanh toán
CANCELLED   User/admin hủy
FAILED      Thanh toán thất bại
REFUNDED    Đã hoàn tiền
EXPIRED     Quá hạn thanh toán
```

Dùng cho:

- `orders.status`

## 11. PaymentMethod / PaymentProvider

```text
VNPAY
MOMO
ZALOPAY
PAYPAL
STRIPE
BANK_TRANSFER
MANUAL
```

Dùng cho:

- `payments.payment_method`
- `payments.payment_provider`

## 12. PaymentStatus

```text
PENDING
SUCCESS
FAILED
CANCELLED
REFUNDED
```

Dùng cho:

- `payments.status`

## 13. DiscountType

```text
PERCENT
FIXED
```

Dùng cho:

- `coupons.discount_type`

## 14. CouponStatus

```text
ACTIVE
INACTIVE
EXPIRED
USED_UP
```

Dùng cho:

- `coupons.status`

## 15. QuestionType

```text
SINGLE_CHOICE
MULTIPLE_CHOICE
TRUE_FALSE
FILL_BLANK
MATCHING
LISTENING
REORDER
```

Dùng cho:

- `questions.question_type`

## 16. QuizStatus

```text
DRAFT
PUBLISHED
HIDDEN
ARCHIVED
```

Dùng cho:

- `quizzes.status`

## 17. QuizAttemptStatus

```text
IN_PROGRESS
SUBMITTED
EXPIRED
CANCELLED
```

Dùng cho:

- `quiz_attempts.status`

## 18. DeckType

```text
VOCABULARY
KANJI
GRAMMAR
CUSTOM
```

Dùng cho:

- `flashcard_decks.deck_type`

## 19. MemoryLevel

```text
NEW
HARD
MEDIUM
EASY
MASTERED
```

Dùng cho:

- `user_flashcard_progress.memory_level`

## 20. GameType

```text
VOCAB_MATCHING
KANJI_CHOICE
LISTENING_CHOICE
HIRAGANA_ROMAJI
SENTENCE_REORDER
```

Dùng cho:

- `games.game_type`

## 21. Difficulty

```text
EASY
MEDIUM
HARD
EXPERT
```

Dùng cho:

- `game_levels.difficulty`

## 22. GameSessionStatus

```text
IN_PROGRESS
COMPLETED
CANCELLED
EXPIRED
```

Dùng cho:

- `game_sessions.status`

## 23. XpSourceType

```text
LESSON_COMPLETE
QUIZ_PASS
GAME_PLAY
DAILY_TASK
STREAK
BADGE
```

Dùng cho:

- `user_xp_logs.source_type`

## 24. NotificationType

```text
SYSTEM
COURSE
PAYMENT
PROMOTION
LEARNING_REMINDER
```

## 25. NotificationTargetType

```text
ALL
ROLE
USER
COURSE_ENROLLED_USERS
```

## 26. SettingType

```text
TEXT
NUMBER
BOOLEAN
JSON
IMAGE_URL
URL
HTML
```

Dùng cho:

- `site_settings.setting_type`

## 27. BannerPosition

```text
HOME_TOP
HOME_MIDDLE
COURSE_LIST_TOP
SIDEBAR
POPUP
```

## 28. Java enum mẫu

```java
public enum CourseStatus {
    DRAFT,
    PUBLISHED,
    HIDDEN,
    ARCHIVED
}
```

Trong Entity:

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 30)
private CourseStatus status = CourseStatus.DRAFT;
```

## 29. Prompt dùng với AI

```text
Hãy đọc 28_ENUM_DEFINITIONS.md.
Khi tạo Entity hoặc DTO, chỉ được dùng enum đã định nghĩa trong file này.
Nếu cần enum mới, hãy giải thích lý do trước khi thêm.
```
