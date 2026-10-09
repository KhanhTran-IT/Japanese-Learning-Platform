> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

# 7.6. Nhóm Flashcard

## Bảng `flashcard_decks`

```sql
CREATE TABLE flashcard_decks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    deck_type VARCHAR(50),
    jlpt_level_id BIGINT,
    status VARCHAR(30) DEFAULT 'PUBLISHED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_flashcard_deck_level FOREIGN KEY (jlpt_level_id) REFERENCES jlpt_levels(id)
);
```

Deck type:

```text
VOCABULARY
KANJI
GRAMMAR
CUSTOM
```

## Bảng `flashcard_items`

```sql
CREATE TABLE flashcard_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    deck_id BIGINT NOT NULL,
    front_text TEXT NOT NULL,
    back_text TEXT NOT NULL,
    audio_url VARCHAR(500),
    image_url VARCHAR(500),
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_flashcard_items_deck FOREIGN KEY (deck_id) REFERENCES flashcard_decks(id)
);
```

## Bảng `user_flashcard_progress`

```sql
CREATE TABLE user_flashcard_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    flashcard_item_id BIGINT NOT NULL,
    memory_level VARCHAR(30) DEFAULT 'NEW',
    review_count INT DEFAULT 0,
    next_review_at DATETIME,
    last_reviewed_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_flashcard_progress_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_flashcard_progress_item FOREIGN KEY (flashcard_item_id) REFERENCES flashcard_items(id),
    UNIQUE KEY uk_user_flashcard_item (user_id, flashcard_item_id)
);
```

Memory level:

```text
NEW
HARD
MEDIUM
EASY
MASTERED
```

---
