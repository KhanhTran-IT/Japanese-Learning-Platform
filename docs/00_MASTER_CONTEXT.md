# MASTER CONTEXT - Japanese Learning Platform

> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

## Mục tiêu
Xây dựng nền tảng học tiếng Nhật thực tế, có thể vận hành lâu dài, gồm khóa học miễn phí/trả phí, bài học video/audio/text/PDF, quiz, flashcard, mini game, theo dõi tiến độ, thanh toán, admin dashboard và tối ưu nhiều người dùng.

## Stack chính
- Frontend: Vue 3, Vite, Vue Router, Pinia, Axios, Tailwind CSS, Element Plus hoặc Naive UI.
- Backend: Java Spring Boot, Spring Security, JWT, Spring Data JPA, Hibernate, Validation, Mail, Scheduler, Actuator, Swagger/OpenAPI.
- Database: MariaDB giai đoạn đầu.
- Cache: Redis.
- Storage: Cloudflare R2, AWS S3, Google Cloud Storage, Supabase Storage hoặc MinIO.
- Deploy: Docker, Docker Compose, Nginx, HTTPS, GitHub Actions, Cloudflare CDN.

## Kiến trúc ưu tiên
Modular Monolith: một backend Spring Boot nhưng chia module rõ ràng. Không dùng microservices quá sớm.

## Thứ tự triển khai tốt nhất
Nghiệp vụ → Database → API base → Auth → Course → Lesson → Enrollment → Payment → Progress → Quiz → Flashcard → Game → Admin → Cache → Deploy production → Monitoring → Backup.

## Luật làm việc với AI
- Không yêu cầu AI code cả hệ thống một lần.
- Luôn đưa bối cảnh module, database liên quan, API liên quan và quy chuẩn code.
- Bắt AI phân tích trước, sau đó mới code.
- Sau khi code, bắt AI review bảo mật, phân quyền, transaction, validate và khả năng mở rộng.
