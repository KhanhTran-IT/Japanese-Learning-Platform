# CURRENT TASK

## Task hiện tại
Backend Environment Example & Secret Hygiene Cleanup

## Trạng thái
TODO

## Mục tiêu
Dọn lại cấu hình môi trường backend cho an toàn và dễ setup: `.env` thật phải nằm ngoài git, `.env.example` an toàn nên được commit làm mẫu, `.gitignore` phải đúng, và tài liệu setup local phải rõ ràng.

## Vì sao làm task này?
Trong các task hardening gần đây, backend đã chuyển sang dùng biến môi trường và `spring-dotenv`. Đây là hướng đúng, nhưng cần chốt lại cho sạch:
- `.env` thật không được commit vì có thể chứa database password, admin password và JWT secret.
- `.env.example` nên được giữ trong git để người clone project biết cần cấu hình biến nào.
- Hiện `backend/.gitignore` có thể đang ignore cả `.env.example`, làm file mẫu không được track.
- Secret từng xuất hiện trong git history cần được nhận diện để có quyết định rotate hoặc cleanup phù hợp.

Task này giúp project an toàn hơn trước khi tiếp tục feature mới như quiz/payment.

## Không làm trong task này
- Không đổi flow auth/JWT.
- Không đổi database schema.
- Không đổi backend API.
- Không rewrite git history nếu chưa được xác nhận rõ.
- Không commit secret thật.
- Không xóa file `.env` local của developer nếu không cần.
- Không làm quiz/payment/upload.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/11_BACKEND_FRONTEND_CONFIG.md`
- `docs/15_DOCKER_DEPLOYMENT.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`
- `docs/29_ERROR_CODE_STANDARD.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/learning/CONCEPTS_EXPLAINED.md`
- `docs/learning/LEARNING_LOG.md`
- `docs/learning/INTERVIEW_NOTES.md`

## File cần rà soát
- `.gitignore`
- `backend/.gitignore`
- `backend/.env`
- `backend/.env.example`
- `backend/pom.xml`
- `backend/src/main/resources/application-dev.yml`
- `backend/src/main/resources/application-prod.yml`
- `backend/docker-compose.yml`
- `backend/README.md`
- `docs/11_BACKEND_FRONTEND_CONFIG.md`
- `docs/15_DOCKER_DEPLOYMENT.md`

## Vấn đề cần xử lý

### 1. `.env` thật phải được ignore
- Đảm bảo `backend/.env` không bị git track.
- Nếu file `.env` local đang tồn tại, giữ lại cho máy local nhưng không đưa vào commit.
- Không in nội dung secret thật ra commit message hoặc docs.

### 2. `.env.example` phải được track
- `backend/.env.example` nên tồn tại trong repo.
- `backend/.gitignore` không được ignore `.env.example`.
- File example chỉ chứa placeholder hoặc giá trị local demo không nhạy cảm.
- Có thể dùng format:

```env
DB_PASSWORD=change-me
ADMIN_PASSWORD=change-me
JWT_ACCESS_SECRET=change-me-use-a-long-random-secret
JWT_REFRESH_SECRET=change-me-use-a-long-random-secret
```

### 3. Cấu hình Spring Boot phải đọc env rõ ràng
- Kiểm tra `application-dev.yml` và `application-prod.yml` đang dùng `${ENV_VAR}` đúng.
- Nếu cần default value cho local, cân nhắc kỹ để không vô tình hardcode secret thật.
- `prod` không nên có fallback secret/password mặc định.

### 4. Docker/local setup phải rõ
- Nếu `docker-compose.yml` cần biến môi trường, docs phải nói rõ cách tạo `.env`.
- README hoặc docs config cần ghi:
  - copy `backend/.env.example` thành `backend/.env`.
  - điền secret local.
  - không commit `.env`.

### 5. Git history secret awareness
- Kiểm tra xem secret thật từng bị commit hay chưa.
- Nếu đã từng commit secret, không tự rewrite history trong task này.
- Ghi rõ khuyến nghị:
  - rotate local/prod secrets nếu đã lộ.
  - chỉ rewrite history khi repo public và owner xác nhận.

## Hướng triển khai đề xuất

### Bước 1 - Kiểm tra trạng thái git ignore
- Chạy `git status --short`.
- Kiểm tra `.gitignore` và `backend/.gitignore`.
- Đảm bảo `.env` bị ignore, `.env.example` không bị ignore.

### Bước 2 - Khôi phục `.env.example` an toàn
- Tạo/cập nhật `backend/.env.example`.
- Không dùng secret thật.
- Chỉ dùng placeholder rõ nghĩa.

### Bước 3 - Cập nhật docs setup
- Cập nhật `backend/README.md` hoặc docs config phù hợp.
- Ghi cách tạo `.env` local từ `.env.example`.
- Ghi rõ `.env` không được commit.

### Bước 4 - Kiểm tra build/test
- Chạy backend package phù hợp.
- Nếu test bị blocker môi trường Mockito/Byte Buddy thì ghi rõ.
- Frontend không cần test nếu không chỉnh frontend, nhưng có thể chạy nếu muốn đảm bảo repo vẫn ổn.

## Checklist
- [ ] `backend/.env` không được track bởi git.
- [ ] `backend/.env` vẫn có thể tồn tại local để chạy app.
- [ ] `backend/.env.example` tồn tại và được git track.
- [ ] `backend/.env.example` không chứa secret thật.
- [ ] `backend/.gitignore` ignore `.env` nhưng không ignore `.env.example`.
- [ ] Docs hướng dẫn copy `.env.example` -> `.env`.
- [ ] Docs nhắc không commit `.env`.
- [ ] `application-dev.yml`/`application-prod.yml` vẫn đọc env đúng.
- [ ] Backend package chạy được hoặc blocker được ghi rõ.
- [ ] Nếu phát hiện secret từng vào history, ghi rõ khuyến nghị rotate/rewrite history riêng.

## Cách test sau khi hoàn thành
1. Chạy `git status --short` để đảm bảo `.env` không xuất hiện.
2. Chạy `git check-ignore -v backend/.env` để xác nhận `.env` bị ignore.
3. Chạy `git check-ignore -v backend/.env.example`; kỳ vọng file này không bị ignore.
4. Xóa tạm `.env` ở môi trường test local hoặc đổi tên, copy lại từ `.env.example`.
5. Điền secret local vào `.env`.
6. Chạy backend package hoặc start backend.
7. Đảm bảo không có secret thật trong diff.

## Kết quả mong muốn
Repo có cấu hình môi trường sạch: developer mới có file mẫu để setup, secret thật không bị commit, và team hiểu rõ rủi ro nếu secret từng xuất hiện trong git history.
