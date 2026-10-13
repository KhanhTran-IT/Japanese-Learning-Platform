# CURRENT TASK

## Task hiện tại
Production Deployment Readiness & Configuration Hardening

## Trạng thái
DONE

## Mục tiêu
Rà soát và harden cấu hình production/deployment của project sau khi MVP demo đã pass full regression. Task này tập trung đảm bảo backend/frontend có thể chạy ổn trong môi trường production-like: Docker Compose, biến môi trường, CORS/cookie, Redis rate limiting, media URL security, logging, health check và tài liệu vận hành.

## Vì sao làm task này?
Project hiện đã demo-ready về mặt feature và test. Bước hợp lý tiếp theo không phải thêm feature lớn ngay, mà là chuẩn bị triển khai thật hoặc production-like. Nếu cấu hình production sai, các flow đã pass local vẫn có thể lỗi khi deploy do:
- Cookie refresh token bị browser chặn vì `SameSite/Secure` sai.
- CORS không cho frontend gọi backend.
- Redis/rate limiter cấu hình thiếu secret hoặc fail policy không rõ.
- `.env.example` thiếu biến mới.
- Docker compose không khớp app config.
- Media/avatar URL cho phép domain không an toàn.
- Logs/health check không đủ để debug.

## Không làm trong task này
- Không thêm business feature mới.
- Không làm payment thật.
- Không đổi schema nếu không cần.
- Không redesign UI.
- Không triển khai lên cloud thật nếu chưa có thông tin hạ tầng.
- Không commit secret thật.
- Không thay đổi security rule nghiệp vụ nếu không phát hiện bug cụ thể.

## File tài liệu cần dùng
- `README.md`
- `backend/README.md`
- `docs/PRODUCTION_DEPLOYMENT.md`
- `docs/RATE_LIMITER_OPS.md`
- `docs/MEDIA_URL_SECURITY.md`
- `docs/12_SECURITY_CHECKLIST.md`
- `docs/15_DOCKER_DEPLOYMENT.md`
- `docs/14_MONITORING_LOGGING_BACKUP.md`
- `docs/11_BACKEND_FRONTEND_CONFIG.md`
- `docs/learning/BUG_LOG.md`
- `backend/.env.example`
- `docker-compose.prod.yml`
- `backend/docker-compose.yml`
- `backend/src/main/resources/application-prod.yml`
- `backend/src/main/resources/application-dev.yml`
- `frontend/vite.config.js`
- `frontend/package.json`

## Phạm vi kiểm tra

### 1. Environment variables
- Kiểm tra `.env.example` có đủ biến đang được app đọc.
- Kiểm tra biến production trong `application-prod.yml`.
- Không để secret thật trong repo.
- Các biến quan trọng cần chú ý:
  - database URL/user/password.
  - JWT access/refresh secret.
  - refresh cookie secure/sameSite/domain.
  - CORS allowed origins.
  - Redis host/port/password.
  - media allowed domains.

### 2. Docker/Compose
- Kiểm tra `docker-compose.prod.yml` có đủ backend, database, Redis.
- Kiểm tra health check và dependency startup nếu có.
- Kiểm tra volume/logging config.
- Kiểm tra port mapping không mâu thuẫn.
- Kiểm tra frontend production build/deploy expectation được ghi rõ.

### 3. CORS & Cookie
- Đảm bảo production cross-domain flow được docs rõ:
  - `SameSite=None` phải đi cùng `Secure=true`.
  - allowed origins không dùng wildcard nếu credential cookie.
  - refresh-token cookie path/domain hợp lý.
- Nếu backend có fast-fail config, xác nhận docs nói rõ.

### 4. Redis Rate Limiting
- Kiểm tra rate limiter dùng Redis ở production.
- Kiểm tra fail-open/fail-closed policy có docs.
- Kiểm tra Redis password/config trong compose.
- Kiểm tra ops docs có cách debug/reset limit nếu cần.

### 5. Media URL Security
- Kiểm tra validator media URL.
- Kiểm tra allowed domains/config docs.
- Đảm bảo không khuyến khích hotlink production không kiểm soát.

### 6. Test/build production-like
- Backend:
  - `mvn clean verify` bằng JDK 21 nếu môi trường hỗ trợ.
  - Hoặc test config/security liên quan nếu full verify đã pass gần đây.
- Frontend:
  - `npm run test`
  - `npm run build`
- Docker:
  - Không bắt buộc chạy full compose nếu môi trường không đủ, nhưng phải kiểm tra config/static syntax.

## Checklist
- [x] `.env.example` khớp biến app đang đọc.
- [x] `application-prod.yml` không chứa secret thật và không có default nguy hiểm.
- [x] `docker-compose.prod.yml` hợp lý cho production-like run.
- [x] CORS/cookie production docs rõ và không mâu thuẫn.
- [x] Redis/rate limiter ops docs rõ.
- [x] Media URL security docs/config rõ.
- [x] Backend/frontend test hoặc build được chạy, hoặc blocker được ghi rõ.
- [x] BUG_LOG được cập nhật nếu phát hiện lỗi config (Bug #017).
- [x] README/docs được cập nhật nếu phát hiện lệch trạng thái.
- [x] Đề xuất task tiếp theo dựa trên kết quả hardening.

## Kết quả kiểm thử & Audit
1. **Kiểm tra Secret & Gitignore**:
   - Quét regex toàn repo: không có mật khẩu/secret thật nào bị commit.
   - Bổ sung quy tắc ignore `*.env`, `.env`, `.env.*` (ngoại trừ `.env*.example`) vào `.gitignore` gốc để ngăn chặn hoàn toàn nguy cơ vô tình commit `.env.prod`.
2. **Hardening Cấu hình Production**:
   - `docker-compose.prod.yml`: Đóng port MariaDB và Redis về `127.0.0.1` (thay vì `0.0.0.0`), bổ sung cấu hình chuyển tiếp các biến môi trường CORS, Cookie, Rate Limit, Media.
   - `application-prod.yml`: Khai báo đầy đủ placeholder cho `APP_CORS_ALLOWED_ORIGINS`, `APP_COOKIE_SECURE`, `APP_COOKIE_SAME_SITE`, `APP_RATE_LIMIT_REDIS_FAILURE_POLICY`, `APP_MEDIA_TRUSTED_DOMAINS`.
   - `RedisRateLimiterService`: Hỗ trợ cả định dạng `fail-open` lẫn `fail_open` (không phân biệt hoa thường).
3. **Kiểm thử tự động**:
   - Backend `mvn clean verify` (JDK 21): PASS 100% (68 unit tests, 53 integration tests).
   - Frontend `npm run test` (Vitest): PASS 100% (11 suites, 47 tests).
   - Frontend `npm run build`: Thành công sạch sẽ (bundle dist/index.html & assets hoàn tất).
4. **Blocker môi trường**:
   - `docker` CLI không được cài đặt trực tiếp trong môi trường sandbox hiện tại, do đó lệnh `docker compose ... config` không thể thực thi trực tiếp, nhưng cú pháp docker compose YAML đã được đối chiếu và chuẩn hóa chuẩn xác.
5. **Đề xuất task tiếp theo**:
   - Thiết lập CI/CD pipeline (GitHub Actions) để tự động hóa quy trình `mvn verify`, `npm test`, `npm build` và Docker container image scanning khi tạo PR.
