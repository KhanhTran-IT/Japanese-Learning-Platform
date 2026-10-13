# CURRENT TASK

## Task hiện tại
CI/CD Pipeline Foundation

## Trạng thái
TODO

## Mục tiêu
Thiết lập pipeline CI nền tảng để mỗi Pull Request và mỗi lần push lên nhánh chính đều tự động chạy các kiểm tra quan trọng của MVP: backend Maven verify, frontend test, frontend production build và kiểm tra cấu hình Docker Compose production ở mức static config.

## Vì sao làm task này?
MVP hiện đã pass full regression và production hardening. Tuy nhiên nếu các bước kiểm thử vẫn phải chạy thủ công, project rất dễ bị regression khi tiếp tục thêm feature mới như quiz nâng cao, payment, admin CMS hoặc redesign frontend. CI/CD foundation giúp khóa chất lượng P0 bằng automation:
- Backend luôn chạy với Java 21 đúng phiên bản production.
- Frontend luôn được test và build trước khi merge.
- Docker Compose production config được kiểm tra sớm.
- Không cần secret thật để chạy pipeline.
- Pull Request có tín hiệu pass/fail rõ ràng trước khi review.

## Không làm trong task này
- Không deploy lên cloud thật.
- Không publish Docker image lên registry.
- Không cấu hình domain/SSL thật.
- Không thêm secret production vào GitHub.
- Không sửa business feature nếu không phát hiện lỗi CI bắt buộc.
- Không làm pipeline quá phức tạp nhiều môi trường.

## File cần kiểm tra/chỉnh sửa
- `.github/workflows/ci.yml`
- `backend/pom.xml`
- `frontend/package.json`
- `docker-compose.prod.yml`
- `.env.prod.example`
- `docs/PRODUCTION_DEPLOYMENT.md`
- `docs/learning/LEARNING_LOG.md`
- `docs/learning/INTERVIEW_NOTES.md`
- `docs/learning/CONCEPTS_EXPLAINED.md`
- `docs/learning/BUG_LOG.md` nếu phát hiện lỗi mới

## Phạm vi thực hiện

### 1. Backend CI
- Dùng GitHub Actions runner Ubuntu.
- Setup Java 21 Temurin.
- Cache Maven dependencies nếu phù hợp.
- Chạy `mvn -B clean verify` trong thư mục `backend`.
- Đảm bảo lệnh không phụ thuộc secret production thật.

### 2. Frontend CI
- Setup Node.js theo version phù hợp với project.
- Dùng `npm ci` để install reproducible dependencies.
- Chạy `npm run test`.
- Chạy `npm run build`.
- Cache npm nếu phù hợp.

### 3. Docker Compose Config Check
- Chạy `docker compose -f docker-compose.prod.yml --env-file .env.prod.example config` hoặc phương án tương đương không cần secret thật.
- Nếu `.env.prod.example` có placeholder không hợp lệ cho Docker config, điều chỉnh template để CI có thể validate static config.

### 4. Workflow Trigger & Permissions
- Trigger trên Pull Request.
- Trigger trên push vào nhánh chính đang dùng trong repo.
- Dùng permissions tối thiểu.
- Không in secret ra log.

### 5. Documentation
- Ghi rõ trong docs cách đọc pipeline và các lệnh local tương đương.
- Cập nhật learning docs sau khi hoàn tất task.

## Checklist
- [ ] Có workflow CI chạy backend Maven verify.
- [ ] Có workflow CI chạy frontend test.
- [ ] Có workflow CI chạy frontend build.
- [ ] Có bước kiểm tra Docker Compose production config hoặc ghi blocker rõ nếu GitHub runner/config không hỗ trợ.
- [ ] Pipeline không cần secret thật.
- [ ] Workflow dùng Java 21.
- [ ] Workflow dùng install command reproducible cho frontend.
- [ ] Docs có hướng dẫn chạy local tương đương.
- [ ] Learning docs được cập nhật sau khi task hoàn tất.
- [ ] Đề xuất task tiếp theo dựa trên kết quả CI.

## Gợi ý branch
`ci/336-CI-CD-Pipeline-Foundation`
