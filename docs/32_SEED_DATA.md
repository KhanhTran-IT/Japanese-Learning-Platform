# 32. SEED_DATA - Dữ liệu mẫu để demo và phát triển

## 1. Mục đích của file

File này giúp bạn có dữ liệu mẫu khi phát triển frontend/backend. Không có seed data, frontend sẽ rất khó làm vì không có nội dung để hiển thị.

Nguyên tắc:

```text
Seed data chỉ dùng cho dev/demo.
Không commit mật khẩu thật.
Mật khẩu mẫu phải được hash bằng BCrypt trong database thật.
Không dùng email/password production.
```

## 2. Roles seed

```sql
INSERT INTO roles (name, description) VALUES
('SUPER_ADMIN', 'Toàn quyền hệ thống'),
('ADMIN', 'Quản trị viên'),
('TEACHER', 'Giảng viên'),
('CONTENT_EDITOR', 'Biên tập nội dung'),
('STUDENT', 'Học viên');
```

## 3. Users seed đề xuất

Tài khoản demo:

```text
Super Admin:
email: superadmin@example.com
password: Password@123
role: SUPER_ADMIN

Admin:
email: admin@example.com
password: Password@123
role: ADMIN

Teacher:
email: teacher@example.com
password: Password@123
role: TEACHER

Student:
email: student@example.com
password: Password@123
role: STUDENT
```

Lưu ý:

- Khi insert thật, `password_hash` phải là BCrypt hash.
- Có thể tạo bằng CommandLineRunner trong Spring Boot thay vì SQL raw.

## 4. JLPT levels seed

```sql
INSERT INTO jlpt_levels (name, description, sort_order) VALUES
('N5', 'Cơ bản nhất', 1),
('N4', 'Sơ cấp', 2),
('N3', 'Trung cấp', 3),
('N2', 'Trung cao cấp', 4),
('N1', 'Cao cấp', 5);
```

## 5. Course seed đề xuất

### Course 1

```text
Title: N5 nhập môn cho người mới bắt đầu
Slug: n5-nhap-mon-cho-nguoi-moi-bat-dau
Level: N5
Type: FREE
Status: PUBLISHED
Short description: Khóa học nền tảng giúp người mới làm quen bảng chữ cái, phát âm và mẫu câu cơ bản.
Teacher: teacher@example.com
```

Sections:

```text
1. Làm quen tiếng Nhật
2. Hiragana cơ bản
3. Katakana cơ bản
4. Mẫu câu chào hỏi
```

Lessons:

```text
1. Tiếng Nhật là gì?
2. Cách học bảng chữ cái hiệu quả
3. Hiragana hàng あ
4. Hiragana hàng か
5. Chào hỏi cơ bản
```

### Course 2

```text
Title: Từ vựng N5 nền tảng
Slug: tu-vung-n5-nen-tang
Level: N5
Type: FREE
Status: PUBLISHED
Short description: Học các nhóm từ vựng N5 thường gặp trong đời sống hằng ngày.
```

Sections:

```text
1. Gia đình
2. Trường học
3. Thời gian
4. Đồ ăn
```

### Course 3

```text
Title: Ngữ pháp N5 từ cơ bản đến ứng dụng
Slug: ngu-phap-n5-tu-co-ban-den-ung-dung
Level: N5
Type: PAID
Original price: 499000
Sale price: 299000
Status: PUBLISHED
Short description: Hệ thống hóa ngữ pháp N5 với ví dụ dễ hiểu và bài tập thực hành.
```

### Course 4

```text
Title: Kanji N5 dễ nhớ
Slug: kanji-n5-de-nho
Level: N5
Type: PAID
Original price: 399000
Sale price: 199000
Status: PUBLISHED
```

## 6. Vocabulary seed mẫu

```text
word: 学生
kana: がくせい
romaji: gakusei
meaning_vi: học sinh, sinh viên
level: N5
topic: school
example_jp: 私は学生です。
example_vi: Tôi là học sinh/sinh viên.
```

```text
word: 先生
kana: せんせい
romaji: sensei
meaning_vi: giáo viên
level: N5
topic: school
example_jp: 田中先生は日本人です。
example_vi: Thầy/Cô Tanaka là người Nhật.
```

```text
word: 水
kana: みず
romaji: mizu
meaning_vi: nước
level: N5
topic: daily
example_jp: 水を飲みます。
example_vi: Tôi uống nước.
```

## 7. Kanji seed mẫu

```text
character_value: 日
onyomi: ニチ, ジツ
kunyomi: ひ, か
meaning_vi: ngày, mặt trời
stroke_count: 4
level: N5
example_words: 日本, 日曜日
```

```text
character_value: 人
onyomi: ジン, ニン
kunyomi: ひと
meaning_vi: người
stroke_count: 2
level: N5
example_words: 日本人, 人口
```

```text
character_value: 学
onyomi: ガク
kunyomi: まなぶ
meaning_vi: học
stroke_count: 8
level: N5
example_words: 学生, 学校
```

## 8. Grammar seed mẫu

```text
pattern: A は B です
meaning_vi: A là B
structure_text: Danh từ A + は + Danh từ B + です
example_jp: 私は学生です。
example_vi: Tôi là học sinh/sinh viên.
level: N5
```

```text
pattern: これ/それ/あれ は N です
meaning_vi: Cái này/cái đó/cái kia là N
example_jp: これは本です。
example_vi: Đây là quyển sách.
level: N5
```

## 9. Site settings seed

```text
site_name = Japanese Learning Platform
site_logo = /assets/logo.png
site_favicon = /favicon.ico
contact_email = support@example.com
contact_phone = 0123456789
facebook_url = https://facebook.com/example
zalo_url = https://zalo.me/example
seo_title = Học tiếng Nhật online từ N5 đến N1
seo_description = Nền tảng học tiếng Nhật với khóa học, quiz, flashcard và mini game.
payment_enabled = false
maintenance_mode = false
```

## 10. Banner seed

```text
Title: Bắt đầu học tiếng Nhật hôm nay
Subtitle: Lộ trình từ N5 đến N1, bài học dễ hiểu, luyện tập mỗi ngày.
Position: HOME_TOP
Status: ACTIVE
```

```text
Title: Khóa học N5 miễn phí
Subtitle: Làm quen tiếng Nhật từ con số 0.
Position: HOME_MIDDLE
Status: ACTIVE
```

## 11. Coupon seed cho giai đoạn payment

```text
Code: WELCOME20
Name: Giảm 20% cho học viên mới
Discount type: PERCENT
Discount value: 20
Max discount amount: 100000
Min order amount: 100000
Per user limit: 1
Status: ACTIVE
```

## 12. CommandLineRunner seed gợi ý

```java
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedRoles();
        seedAdminUser();
    }

    private void seedRoles() {
        // kiểm tra tồn tại trước khi tạo
    }

    private void seedAdminUser() {
        // tạo admin@example.com nếu chưa tồn tại
    }
}
```

## 13. Prompt dùng với AI

```text
Hãy đọc 32_SEED_DATA.md.
Hãy tạo DataSeeder cho Spring Boot để seed roles, admin user, teacher user, student user và một vài khóa học mẫu.
Code phải kiểm tra tồn tại trước khi insert để chạy nhiều lần không bị trùng.
```
