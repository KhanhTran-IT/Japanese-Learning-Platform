# CURRENT_TASK_TEMPLATE - Mẫu quản lý task hiện tại

## 1. Tên task

Ví dụ: Register API

## 2. Mục tiêu

Ví dụ: Cho phép người dùng đăng ký tài khoản STUDENT bằng email/password.

## 3. File tài liệu cần dùng

```text
- docs/00_MASTER_CONTEXT.md
- docs/23_MVP_SCOPE.md
- docs/24_USER_FLOWS.md
- docs/26_API_PRIORITY.md
- docs/27_DATABASE_PHASES.md
- docs/28_ENUM_DEFINITIONS.md
- docs/29_ERROR_CODE_STANDARD.md
- docs/30_PERMISSION_MATRIX.md
- docs/18_CODE_CONVENTIONS.md
```

## 4. API liên quan

```http
POST /api/auth/register
```

## 5. Database liên quan

```text
users
roles
user_roles
```

## 6. Checklist code

```text
[ ] Request DTO
[ ] Response DTO
[ ] Entity liên quan
[ ] Repository
[ ] Service
[ ] Controller
[ ] Validation
[ ] ErrorCode
[ ] Security rule nếu cần
[ ] Test Postman
[ ] Commit Git
```

## 7. Ghi chú debug

Ghi lại lỗi gặp phải và cách sửa.
