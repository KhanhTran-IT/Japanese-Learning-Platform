# Japanese Learning Platform - Bộ tài liệu vibe code

Bộ tài liệu này được tách từ kế hoạch tổng thể thành các file nhỏ để làm việc với AI, code từng module, review kiến trúc và quản lý tiến độ dễ hơn.

## Cách dùng nhanh

1. Mở `docs/00_MASTER_CONTEXT.md` trước mỗi phiên làm việc với AI.
2. Khi làm backend, gửi thêm các file trong `docs/07_database`, `docs/08_api`, `docs/09_BACKEND_STRUCTURE.md`, `docs/18_CODE_CONVENTIONS.md`.
3. Khi làm frontend, gửi thêm `docs/10_FRONTEND_STRUCTURE.md`, `docs/08_api`, `docs/18_CODE_CONVENTIONS.md`.
4. Khi bắt đầu sprint, dùng `docs/16_ROADMAP_AND_SPRINTS.md` và `docs/templates/TASK_PROMPT_TEMPLATE.md`.
5. Khi AI code xong, dùng `docs/templates/CODE_REVIEW_PROMPT.md` để bắt AI tự review.

## Cấu trúc chính

```text
docs/
  00_MASTER_CONTEXT.md
  01_PROJECT_OVERVIEW.md
  02_PRODUCT_VISION.md
  03_TECH_STACK.md
  04_SYSTEM_ARCHITECTURE.md
  05_features/
  06_ROLES_AND_PERMISSIONS.md
  07_database/
  08_api/
  09_BACKEND_STRUCTURE.md
  10_FRONTEND_STRUCTURE.md
  11_BACKEND_FRONTEND_CONFIG.md
  12_SECURITY_CHECKLIST.md
  13_PERFORMANCE_SCALING.md
  14_MONITORING_LOGGING_BACKUP.md
  15_DOCKER_DEPLOYMENT.md
  16_ROADMAP_AND_SPRINTS.md
  17_GIT_WORKFLOW.md
  18_CODE_CONVENTIONS.md
  19_TESTING_PLAN.md
  20_HANDOVER_CHECKLIST.md
  21_AI_WORKING_GUIDE.md
  templates/
sql/
  full_schema_from_plan.sql
```


## Bộ file bổ sung để kiểm soát triển khai

Sau khi đã có tài liệu tổng thể, nhóm file `23` đến `32` giúp bạn biến kế hoạch lớn thành công việc có thể code từng ngày:

1. Đọc `23_MVP_SCOPE.md` để biết phiên bản đầu tiên chỉ làm gì.
2. Đọc `24_USER_FLOWS.md` để hiểu luồng người dùng.
3. Đọc `25_SCREEN_LIST.md` khi làm frontend Vue.
4. Đọc `26_API_PRIORITY.md` khi chọn API cần code trước.
5. Đọc `27_DATABASE_PHASES.md` khi tạo Entity/database.
6. Đọc `28_ENUM_DEFINITIONS.md` để thống nhất enum.
7. Đọc `29_ERROR_CODE_STANDARD.md` để chuẩn hóa lỗi backend.
8. Đọc `30_PERMISSION_MATRIX.md` khi làm Spring Security/route guard.
9. Đọc `31_DETAILED_TESTING_PLAN.md` để test module sau khi code.
10. Đọc `32_SEED_DATA.md` để tạo dữ liệu demo.
