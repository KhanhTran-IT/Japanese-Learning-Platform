INSERT INTO flashcard_decks (name, level, icon, created_at, updated_at) VALUES 
('Từ vựng N5 cơ bản', 'N5', '📗', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Kanji N5 — 100 chữ', 'N5', '🈳', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Từ vựng N4 hằng ngày', 'N4', '📘', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Ngữ pháp N5', 'N5', '📝', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Get deck id for "Từ vựng N5 cơ bản" (assume it's 1 since it's a new table, but let's use a subquery if possible. Flyway allows standard SQL.)
-- For simplicity, since it's auto-increment, IDs will be 1, 2, 3, 4.
INSERT INTO flashcards (deck_id, front_text, front_reading, back_meaning, back_example, back_example_meaning, created_at, updated_at) VALUES
(1, '食べる', 'たべる', 'ăn', '朝ご飯を食べる', 'Ăn bữa sáng', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '飲む', 'のむ', 'uống', '水を飲む', 'Uống nước', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '見る', 'みる', 'nhìn, xem', 'テレビを見る', 'Xem TV', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '行く', 'いく', 'đi', '学校に行く', 'Đi học', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '来る', 'くる', 'đến', '友達が来る', 'Bạn đến', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '話す', 'はなす', 'nói chuyện', '日本語で話す', 'Nói bằng tiếng Nhật', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '書く', 'かく', 'viết', '漢字を書く', 'Viết chữ Hán', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '読む', 'よむ', 'đọc', '本を読む', 'Đọc sách', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
