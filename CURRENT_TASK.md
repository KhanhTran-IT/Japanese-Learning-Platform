# CURRENT TASK

## Task hiện tại
Production Deployment Readiness & Configuration Hardening

## Trạng thái
TODO

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
- [ ] `.env.example` khớp biến app đang đọc.
- [ ] `application-prod.yml` không chứa secret thật và không có default nguy hiểm.
- [ ] `docker-compose.prod.yml` hợp lý cho production-like run.
- [ ] CORS/cookie production docs rõ và không mâu thuẫn.
- [ ] Redis/rate limiter ops docs rõ.
- [ ] Media URL security docs/config rõ.
- [ ] Backend/frontend test hoặc build được chạy, hoặc blocker được ghi rõ.
- [ ] BUG_LOG được cập nhật nếu phát hiện lỗi config.
- [ ] README/docs được cập nhật nếu phát hiện lệch trạng thái.
- [ ] Đề xuất task tiếp theo dựa trên kết quả hardening.

## Cách test sau khi hoàn thành
1. Chạy `git status` để ghi nhận worktree.
2. Kiểm tra config bằng `rg` các biến env đang đọc trong backend/frontend.
3. Chạy test/build phù hợp.
4. Nếu có thể, chạy production-like compose:
   ```bash
   docker compose -f docker-compose.prod.yml config
   ```
5. Ghi rõ:
   - pass,
   - fail,
   - blocker môi trường,
   - follow-up task.
