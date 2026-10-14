# CURRENT TASK

## Task hiện tại
CI/CD Pipeline Foundation

## Trạng thái
DONE

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
- [x] Có workflow CI chạy backend Maven verify.
- [x] Có workflow CI chạy frontend test.
- [x] Có workflow CI chạy frontend build.
- [x] Có bước kiểm tra Docker Compose production config hoặc ghi blocker rõ nếu GitHub runner/config không hỗ trợ.
- [x] Pipeline không cần secret thật.
- [x] Workflow dùng Java 21.
- [x] Workflow dùng install command reproducible cho frontend.
- [x] Docs có hướng dẫn chạy local tương đương.
- [x] Learning docs được cập nhật sau khi task hoàn tất.
- [x] Đề xuất task tiếp theo dựa trên kết quả CI.

## Kết quả kiểm thử & Hoàn thành
1. **GitHub Actions Workflow ([.github/workflows/ci.yml](.github/workflows/ci.yml))**:
   - Tạo workflow với 3 jobs chạy song song:
     - `backend`: Java 21 Temurin, Maven cache, `mvn -B clean verify`.
     - `frontend`: Node.js 22, npm cache, `npm ci`, `npm run test`, `npm run build`.
     - `docker-config`: Dry-run validation cho `docker-compose.prod.yml` qua `.env.prod.example`.
   - Cú pháp YAML đã được kiểm tra tính hợp lệ bằng Python yaml parser (`YAML is valid`).
   - Cấu hình permissions tối thiểu (`contents: read`) và concurrency `cancel-in-progress: true`.
2. **Xử lý xung đột Dependency ([Bug ID: #018](docs/learning/BUG_LOG.md))**:
   - `npm ci` ban đầu thất bại vì xung đột peerDependency giữa `@pinia/testing@2.0.1` (yêu cầu `pinia >=4.0.2`) và `pinia@3.0.4`.
   - Đã hạ `@pinia/testing` về phiên bản `^1.0.3` phù hợp chính thức với Pinia 3.x, cập nhật `package-lock.json`.
   - `npm ci` hoạt động hoàn hảo, cài đặt 309 packages trong 6 giây không còn lỗi ERESOLVE.
3. **Kiểm thử cục bộ tương đương**:
   - Backend `mvn clean verify`: 100% BUILD SUCCESS (68 unit tests, 53 integration tests).
   - Frontend `npm ci`: PASS (exit code 0).
   - Frontend `npm run test`: 100% PASS (11 suites, 47 tests).
   - Frontend `npm run build`: 100% PASS (dist artifacts tạo sạch sẽ).
4. **Tài liệu & Learning Docs**:
   - Cập nhật `docs/PRODUCTION_DEPLOYMENT.md` với tài liệu CI và các lệnh local tương đương.
   - Ghi nhận `Bug #018` vào `docs/learning/BUG_LOG.md`.
   - Cập nhật `docs/learning/LEARNING_LOG.md`, `docs/learning/INTERVIEW_NOTES.md`, `docs/learning/CONCEPTS_EXPLAINED.md` (Concepts 116, 117, 118).
5. **Đề xuất task tiếp theo**:
   - **Post-MVP Milestone 1 - Course Search & Filter Enhancement**: Thêm full-text search theo từ khóa bài học/khóa học, lọc nhiều cấp độ JLPT đồng thời, và phân trang nâng cao cho học viên.
