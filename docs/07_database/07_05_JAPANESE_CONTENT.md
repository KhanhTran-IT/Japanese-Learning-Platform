> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

# 7.5. Nhóm Japanese Learning Content

## Bảng `jlpt_levels`

```sql
CREATE TABLE jlpt_levels (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(255),
    sort_order INT DEFAULT 0
);
```

Dữ liệu mẫu:

```sql
INSERT INTO jlpt_levels (name, description, sort_order) VALUES
('N5', 'Cơ bản nhất', 1),
('N4', 'Sơ cấp', 2),
('N3', 'Trung cấp', 3),
('N2', 'Trung cao cấp', 4),
('N1', 'Cao cấp', 5);
```

## Bảng `vocabularies`

```sql
CREATE TABLE vocabularies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    jlpt_level_id BIGINT,
    word VARCHAR(255) NOT NULL,
    kana VARCHAR(255),
    romaji VARCHAR(255),
    meaning_vi VARCHAR(500) NOT NULL,
    meaning_en VARCHAR(500),
    example_jp TEXT,
    example_vi TEXT,
    audio_url VARCHAR(500),
    topic VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_vocab_level FOREIGN KEY (jlpt_level_id) REFERENCES jlpt_levels(id)
);
```

## Bảng `kanjis`

```sql
CREATE TABLE kanjis (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    jlpt_level_id BIGINT,
    character_value VARCHAR(10) NOT NULL,
    onyomi VARCHAR(255),
    kunyomi VARCHAR(255),
    meaning_vi VARCHAR(500),
    meaning_en VARCHAR(500),
    stroke_count INT,
    example_words TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_kanji_level FOREIGN KEY (jlpt_level_id) REFERENCES jlpt_levels(id)
);
```

## Bảng `grammar_points`

```sql
CREATE TABLE grammar_points (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    jlpt_level_id BIGINT,
    pattern VARCHAR(255) NOT NULL,
    meaning_vi VARCHAR(500),
    explanation TEXT,
    structure_text TEXT,
    example_jp TEXT,
    example_vi TEXT,
    note TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_grammar_level FOREIGN KEY (jlpt_level_id) REFERENCES jlpt_levels(id)
);
```

---
