# Japanese Learning Platform (BrianJP)

BrianJP là nền tảng học tiếng Nhật online được xây dựng theo hướng **Modular Monolith** với backend Spring Boot và frontend Vue 3. Dự án hiện tập trung vào MVP học online: đăng ký/đăng nhập, quản lý khóa học, học bài, lưu tiến độ, quiz, flashcard cơ bản và màn quản trị nội dung.

## Trạng thái hiện tại

Tính đến thời điểm hiện tại, project đã có nền tảng full-stack chạy được với các module chính:

- **Auth/User**: đăng ký, đăng nhập, refresh token bằng cookie, logout, JWT access token, `/api/users/me`, cập nhật profile và đổi mật khẩu.
- **RBAC/Security**: phân quyền theo role, guard frontend, Spring Security backend, xử lý lỗi chuẩn, CORS/cookie config, rate limit login/refresh.
- **Course/Lesson**: public course list/detail, lọc/tìm kiếm course, admin quản lý course/section/lesson/resource, publish/hide/archive.
- **Enrollment/Learning Progress**: student enroll khóa học miễn phí, xem khóa học của mình, học lesson, cập nhật progress, complete lesson, tính lại progress khóa học.
- **Admin Dashboard/User Management**: dashboard thống kê, danh sách user, lock/unlock account, lọc/phân trang.
- **Quiz**: backend admin CRUD quiz/question/answer, publish/hide validation, student làm quiz, submit, xem result, giới hạn attempt/time limit; frontend đã có trang làm quiz, result và nền tảng admin quiz management/builder.
- **Flashcard**: backend deck/study/review, frontend flashcard page, thuật toán review cơ bản.
- **Testing**: có unit/integration test backend và Vitest frontend cho auth, route guard, course, dashboard, learning, quiz, flashcard.

Các phần như payment thật, gamification đầy đủ, JLPT content nâng cao, notification, AI tutor và mobile app vẫn thuộc roadmap/tài liệu thiết kế, chưa phải phạm vi đã hoàn thiện.

## Tech Stack

### Backend

- Java 21
- Spring Boot 3.3.6
- Spring Web, Spring Security, Spring Data JPA, Hibernate
- JWT với access token + refresh token
- MariaDB
- Flyway migration
- Redis cho rate limiting production
- Swagger/OpenAPI
- JUnit, Spring Boot Test, H2 test database

### Frontend

- Vue 3 + Vite
- Vue Router
- Pinia
- Axios với interceptor refresh token
- Tailwind CSS
- Vitest + Vue Test Utils + jsdom

### Infrastructure

- Docker / Docker Compose
- MariaDB 10.11
- Redis 7
- Production compose có Redis password, prod profile và logging config

## Cấu trúc thư mục

```text
.
├── backend/                 # Spring Boot API
│   ├── src/main/java/        # Modules: auth, user, course, learning, quiz, flashcard...
│   ├── src/main/resources/   # application.yml, Flyway migrations
│   └── src/test/java/        # Unit + integration tests
├── frontend/                # Vue 3 app
│   ├── src/pages/            # Public, auth, student, admin pages
│   ├── src/services/         # Axios service layer
│   ├── src/router/           # Routes + guards
│   └── src/components/       # UI/admin/student/lesson components
├── docs/                    # Product, architecture, API, DB, learning docs
├── sql/                     # Schema reference from planning docs
├── CURRENT_TASK.md          # Task hiện tại
└── docker-compose.prod.yml  # Production-like compose
```

## Chạy local

### 1. Backend

Yêu cầu:

- JDK 21
- Maven 3.8+
- MariaDB local hoặc Docker

```bash
cd backend
cp .env.example .env
```

Cập nhật các biến trong `.env`:

```text
DB_PASSWORD=...
ADMIN_PASSWORD=...
JWT_ACCESS_SECRET=...
JWT_REFRESH_SECRET=...
```

Chạy backend với profile dev:

```bash
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

Backend chạy tại:

- API: `http://localhost:8080`
- Health check: `http://localhost:8080/api/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

### 2. Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend chạy tại `http://localhost:5173`.

Vite proxy đang chuyển `/api` sang `http://localhost:8080`, nên frontend mặc định có thể gọi backend local mà không cần cấu hình thêm.

## Chạy bằng Docker

### Development compose trong backend

```bash
cd backend
cp .env.example .env
docker compose up --build
```

Compose này chạy MariaDB, Redis và backend.

### Production-like compose

```bash
docker compose -f docker-compose.prod.yml up --build
```

Lưu ý: production cần cấu hình secret thật cho database, Redis và JWT. Không commit file `.env` hoặc `.env.prod`.

## Kiểm thử

Backend:

```bash
cd backend
mvn clean verify
```

Frontend:

```bash
cd frontend
npm run test
npm run build
```

## API/Module chính

Một số nhóm endpoint đã có:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh-token`
- `POST /api/auth/logout`
- `GET /api/users/me`
- `GET /api/v1/courses`
- `GET /api/v1/courses/{slug}`
- `POST /api/v1/courses/{courseId}/enroll`
- `GET /api/v1/lessons/{id}`
- `POST /api/v1/lessons/{id}/progress`
- `GET /api/v1/admin/dashboard`
- `GET /api/v1/admin/users`
- `GET /api/v1/admin/courses`
- `GET /api/v1/admin/quizzes`
- `GET /api/v1/quizzes/{id}`
- `POST /api/v1/quizzes/{id}/start`
- `POST /api/v1/quizzes/{id}/submit`
- `GET /api/v1/flashcards/decks`

Chi tiết contract nằm trong `docs/08_api/` và Swagger UI.

## Tài liệu quan trọng

- `docs/00_MASTER_CONTEXT.md`: ngữ cảnh tổng thể.
- `docs/23_MVP_SCOPE.md`: phạm vi MVP.
- `docs/24_USER_FLOWS.md`: luồng người dùng.
- `docs/26_API_PRIORITY.md`: ưu tiên API.
- `docs/28_ENUM_DEFINITIONS.md`: enum chuẩn.
- `docs/29_ERROR_CODE_STANDARD.md`: chuẩn error code.
- `docs/30_PERMISSION_MATRIX.md`: ma trận quyền.
- `docs/31_DETAILED_TESTING_PLAN.md`: kế hoạch test.
- `docs/learning/LEARNING_LOG.md`: nhật ký học tập và tiến độ.
- `docs/learning/CONCEPTS_EXPLAINED.md`: khái niệm kỹ thuật đã học.

## Roadmap gần

- Hoàn thiện và test sâu hơn Admin Quiz Management/Builder UI.
- Làm mượt flow admin tạo quiz -> student học lesson -> làm quiz -> xem result.
- Bổ sung test frontend cho các màn admin quiz.
- Tiếp tục harden security/rate limit/media URL theo tài liệu vận hành.
- Sau MVP mới mở rộng payment, gamification, JLPT content nâng cao, notification và AI tutor.

## License

To be updated.
