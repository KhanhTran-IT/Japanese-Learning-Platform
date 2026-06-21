# TỔNG HỢP CHI TIẾT CẤU TRÚC VÀ CHỨC NĂNG TỪNG FILE TRONG `docs/`

Tài liệu này đóng vai trò như một "Bộ não" (Knowledge Base) thu nhỏ của toàn bộ dự án **Japanese Learning Platform**. Nó được sinh ra để cung cấp bối cảnh toàn diện cho bất kỳ AI hoặc lập trình viên nào tham gia vào dự án mà không cần truy cập trực tiếp vào từng file trong thư mục `docs/`.

Dưới đây là chi tiết chức năng, cấu trúc và nội dung cốt lõi của TỪNG FILE tài liệu đã được định nghĩa.

---

## PHẦN 1: TỔNG QUAN, KIẾN TRÚC VÀ CÔNG NGHỆ (00 - 04, 09 - 11, 21, 22)

### 1. `00_MASTER_CONTEXT.md` & `01_PROJECT_OVERVIEW.md` & `02_PRODUCT_VISION.md`
*   **Chức năng:** Định nghĩa bối cảnh, tên dự án (`Japanese Learning Platform` / `Nihongo Master`), và tầm nhìn sản phẩm.
*   **Nội dung cốt lõi:** Hệ thống hỗ trợ học viên từ N5 đến N1. Có 5 trụ cột: Học qua khóa học (video/text), Tương tác (Flashcard/Quiz), Game hóa (XP/Leaderboard), Kinh doanh (Thanh toán), Quản trị (Admin).

### 2. `03_TECH_STACK.md` & `04_SYSTEM_ARCHITECTURE.md`
*   **Chức năng:** Chốt bộ công nghệ và kiến trúc hệ thống.
*   **Stack:** 
    *   **Frontend**: Vue 3, Vite, Pinia, Axios, Tailwind CSS.
    *   **Backend**: Java Spring Boot, Spring Security, JWT, Spring Data JPA, Hibernate.
    *   **Database**: MariaDB. **Cache**: Redis. **Storage**: Cloudflare R2 / AWS S3. **DevOps**: Docker, Nginx, GitHub Actions.
*   **Kiến trúc**: **Modular Monolith** (Đơn nguyên nguyên khối có module hóa). Không dùng Microservices. Gom chung vào 1 project Spring Boot nhưng chia package (`module_auth`, `module_course`, v.v.).

### 3. `09_BACKEND_STRUCTURE.md` & `10_FRONTEND_STRUCTURE.md` & `11_BACKEND_FRONTEND_CONFIG.md`
*   **Chức năng:** Định nghĩa cấu trúc thư mục tiêu chuẩn.
*   **Backend Layer**: Luồng xử lý bắt buộc đi theo: `Controller` -> `Service` -> `Repository`. Dữ liệu giao tiếp qua `DTO`. KHÔNG trả `Entity` trực tiếp ra API.
*   **Frontend**: Chia thành `assets`, `components`, `layouts` (Auth, Student, Admin, Main), `pages`, `stores`, `services`.
*   **Config**: `application.yml` chứa cấu hình DB, JWT, Redis. `.env` chứa URL kết nối API cho frontend.

### 4. `21_AI_WORKING_GUIDE.md` & `22_ARCHITECTURE_CONCLUSION.md`
*   **Chức năng:** Hướng dẫn làm việc với AI. AI phải đóng vai Senior, code từng bước: Entity -> Repo -> DTO -> Service -> Controller. Yêu cầu có GlobalExceptionHandler, Validation, DTO separation.

---

## PHẦN 2: ĐẶC TẢ TÍNH NĂNG VÀ DATABASE (05_features & 07_database)
Phần này kết hợp tính năng (Feature) và các bảng Cơ sở dữ liệu (Database Tables) tương ứng.

### 1. Nhóm Auth & User (`05_01_USER_FEATURES.md`, `07_01_AUTH_USER.md`)
*   **Tính năng:** Đăng ký, đăng nhập bằng email, mã hóa BCrypt. Quên mật khẩu, hồ sơ cá nhân.
*   **Database Tables:**
    *   `roles` (id, name, description)
    *   `users` (id, full_name, email, password_hash, phone, avatar_url, status, email_verified)
    *   `user_roles` (user_id, role_id)
    *   `refresh_tokens` (token, expired_at, revoked)
    *   `password_reset_tokens`, `email_verification_tokens`

### 2. Nhóm Khóa học & Bài học (`05_02_COURSE_FEATURES.md`, `07_02_COURSE_LESSON.md`)
*   **Tính năng:** Khóa học phân theo level (N5-N1), có chương học, bài học (video, audio, tài liệu). Có chức năng học thử. Người dùng ghi danh (enroll).
*   **Database Tables:**
    *   `courses` (id, teacher_id, title, slug, level, course_type, price, status, total_duration_minutes)
    *   `course_sections` (id, course_id, title, sort_order)
    *   `lessons` (id, course_id, section_id, title, slug, content, video_url, is_preview)
    *   `lesson_resources` (file_url, resource_type)
    *   `course_enrollments` (user_id, course_id, progress_percent, status) - Bảng lưu việc mua/ghi danh khóa học.

### 3. Nhóm Tiến độ học tập (`05_03_LEARNING_PROGRESS_FEATURES.md`, `07_02_COURSE_LESSON.md`)
*   **Tính năng:** Tracking phần trăm xem video, đánh dấu hoàn thành bài học.
*   **Database Tables:**
    *   `lesson_progress` (user_id, lesson_id, watched_percent, completed, last_accessed_at)

### 4. Nhóm Trắc nghiệm / Quiz (`05_04_QUIZ_FEATURES.md`, `07_04_QUIZ.md`)
*   **Tính năng:** Quiz cuối bài, các loại câu hỏi (Trắc nghiệm 1/nhiều đáp án, Điền từ, Đúng/Sai).
*   **Database Tables:**
    *   `quizzes` (course_id, lesson_id, title, time_limit, passing_score)
    *   `questions` (quiz_id, question_type, content, points)
    *   `answers` (question_id, content, is_correct)
    *   `quiz_attempts` (user_id, quiz_id, score, passed) - Bảng lưu lượt làm bài.
    *   `quiz_attempt_answers` - Bảng lưu chi tiết từng câu trả lời.

### 5. Nhóm Flashcard (`05_05_FLASHCARD_FEATURES.md`, `07_06_FLASHCARD.md`)
*   **Tính năng:** Thẻ từ, Kanji, thuật toán lặp lại ngắt quãng (Spaced Repetition).
*   **Database Tables:** `flashcard_decks`, `flashcard_items`, `user_flashcard_progress` (lưu memory_level: HARD, EASY...).

### 6. Nhóm Gamification (`05_06_GAME_GAMIFICATION_FEATURES.md`, `07_07_GAME_GAMIFICATION.md`)
*   **Tính năng:** Tích XP, chuỗi ngày học liên tục (Streak), Leaderboard, Huy hiệu (Badges), Mini-games.
*   **Database Tables:** `games`, `game_levels`, `game_sessions`, `user_stats` (lưu total_xp, streak), `badges`, `user_xp_logs`.

### 7. Nhóm JLPT Content (`05_07_JLPT_CONTENT_FEATURES.md`, `07_05_JAPANESE_CONTENT.md`)
*   **Tính năng:** Kho lưu trữ từ vựng, Kanji, ngữ pháp theo cấp độ JLPT.
*   **Database Tables:** `jlpt_levels` (N5-N1), `vocabularies`, `kanjis`, `grammar_points`.

### 8. Nhóm Thanh toán (`05_08_PAYMENT_ORDER_FEATURES.md`, `07_03_PAYMENT_ORDER.md`)
*   **Tính năng:** Đơn hàng, tích hợp thanh toán VNPAY/MOMO, Mã giảm giá (Coupon).
*   **Database Tables:** `orders` (tổng tiền, trạng thái), `order_items`, `payments` (transaction_code, provider), `coupons`, `coupon_usages`.

### 9. Nhóm Cấu hình CMS (`05_09_ADMIN_FEATURES.md`, `07_08_NOTIFICATION.md`, `07_09_CMS_CONFIG.md`)
*   **Tính năng:** Quản lý cấu hình web (banner, thông tin liên hệ), blog, FAQ, thông báo.
*   **Database Tables:** `site_settings`, `banners`, `blog_posts`, `faqs`, `notifications`.

---

## PHẦN 3: ĐẶC TẢ API (08_api)

**1. Chuẩn Response (`00_API_RESPONSE_STANDARD.md`)**
Tất cả API trả về chuẩn JSON:
```json
{
  "success": true,
  "message": "...",
  "data": {},
  "errors": [{"field": "email", "message": "Lỗi..."}],
  "timestamp": "..."
}
```

**2. Các Endpoint Cốt Lõi (Từ `08_01` đến `08_10`)**
*   **Auth**: `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/refresh-token`
*   **User**: `GET /api/users/me`, `GET /api/users/me/courses`
*   **Course (Public)**: `GET /api/courses`, `GET /api/courses/{slug}`, `GET /api/courses/{id}/lessons`
*   **Learning**: `POST /api/courses/{id}/enroll`, `POST /api/lessons/{id}/progress` (Lưu %), `POST /api/lessons/{id}/complete`
*   **Admin**: `GET, POST, PUT, DELETE /api/admin/courses` (Tạo khóa học), `/api/admin/users`, `/api/admin/orders`.
*   **Payment**: `POST /api/orders`, `POST /api/payments/create`, `POST /api/payments/webhook`.

---

## PHẦN 4: ENUM, QUYỀN HẠN VÀ MÃ LỖI (28, 29, 30, 06)

*   **`28_ENUM_DEFINITIONS.md` (Các ENUM bắt buộc)**:
    *   `RoleName`: SUPER_ADMIN, ADMIN, TEACHER, CONTENT_EDITOR, STUDENT, GUEST.
    *   `UserStatus`: ACTIVE, INACTIVE, LOCKED.
    *   `CourseLevel`: N5, N4, N3, N2, N1.
    *   `CourseType`: FREE, PAID.
    *   `CourseStatus`: DRAFT, PUBLISHED, HIDDEN, ARCHIVED.
    *   `OrderStatus`: PENDING, PAID, CANCELLED.
    *   `PaymentProvider`: VNPAY, MOMO, STRIPE.
*   **`29_ERROR_CODE_STANDARD.md`**:
    *   Mã lỗi format kiểu `PREFIX_00X` (VD: `AUTH_001`: Email tồn tại, `COURSE_002`: Không tìm thấy khóa, `VALID_001`: Lỗi validation 400). Bắt bằng `GlobalExceptionHandler`.
*   **`30_PERMISSION_MATRIX.md` & `06_ROLES_AND_PERMISSIONS.md`**:
    *   Admin toàn quyền. Teacher chỉ thao tác với Khóa học của chính mình. Student bị cấm truy cập các route `/api/admin/**`.

---

## PHẦN 5: LUỒNG NGHIỆP VỤ VÀ MVP SCOPE (23, 24, 25, 26, 27)

### 1. Phạm vi MVP (`23_MVP_SCOPE.md`)
*   Phiên bản đầu tiên **CHỈ TẬP TRUNG VÀO LÕI**: Đăng ký/Đăng nhập -> Admin tạo Khóa/Bài học -> Học viên Ghi danh khóa MIỄN PHÍ -> Học viên xem video lưu % tiến độ.
*   **Bỏ qua trong MVP**: Thanh toán, Game, Flashcard, JLPT, Quiz.

### 2. Luồng Người Dùng (`24_USER_FLOWS.md`)
*   **Student Flow**: Đăng nhập -> Vào danh sách khóa học -> Bấm Enroll -> Chuyển vào màn hình Học tập (Learning Page) -> Xem video, mỗi 10s frontend gọi API update `watched_percent` -> Cuối bài bấm Hoàn thành gọi API `complete`.
*   **Admin Flow**: Đăng nhập -> Vào trang Quản trị -> Khóa học -> Tạo mới (DRAFT) -> Thêm Chương -> Thêm Bài (có đính kèm file/video URL) -> Đổi status Khóa học thành PUBLISHED.

### 3. Ưu Tiên API & Màn Hình (`26_API_PRIORITY.md`, `25_SCREEN_LIST.md`)
*   Làm API Auth và User trước (P0). Sau đó là Course Public và Lesson Learning (P0). Cuối cùng là API Admin (P0). Frontend thiết kế tương ứng.

### 4. Database Phases (`27_DATABASE_PHASES.md`)
*   Phase 1 (MVP): Chỉ tạo các bảng `users`, `roles`, `user_roles`, `courses`, `course_sections`, `lessons`, `course_enrollments`, `lesson_progress`. Không tạo dư thừa.

---

## PHẦN 6: QUY CHUẨN KỸ THUẬT VÀ PROMPT MẪU (12 - 20, learning, templates)

*   **Bảo mật (`12_SECURITY_CHECKLIST.md`)**:
    *   Không gửi ID nhạy cảm qua body nếu có thể lấy từ SecurityContext (`user_id` của người đang đăng nhập). Validate input chặt chẽ.
*   **Hiệu năng (`13_PERFORMANCE_SCALING.md`)**:
    *   Dùng `@EntityGraph` hoặc `FETCH JOIN` để tránh lỗi N+1 Query (VD: Query Khóa học kèm Giáo viên).
*   **Prompt Mẫu (`templates/`)**:
    *   Hệ thống quy định rõ cách chia task cho AI: Task phải nhỏ, rõ ràng, luôn yêu cầu AI phân tích DTO, exception và flow trước khi sinh code. `AFTER_CODE_TASK_PROMPT.md` chứa quy trình tổng kết bài học, sinh README sau khi xong code.

---

**LƯU Ý DÀNH CHO AI BÊN NGOÀI KHI ĐỌC TÀI LIỆU NÀY:**
*   **Tuyệt đối không** sáng tạo cấu trúc Database hoặc API chệch khỏi thiết kế trên.
*   Khi code chức năng gì, hãy xác định nó thuộc Module nào và tạo Controller/Service/DTO/Repository tương ứng.
*   Luôn nhớ kiến trúc dự án là **Modular Monolith**, dữ liệu trả về phải qua **DTO** và lỗi phải được ném qua **Exception Custom** kèm theo mã lỗi định nghĩa ở phần 4.
