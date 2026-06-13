# CURRENT TASK

## Task hiện tại

Seed Roles + Admin User

## Trạng thái

TODO

## Mục tiêu

Tạo dữ liệu nền ban đầu cho hệ thống Auth/User, bao gồm các role mặc định và một tài khoản admin mặc định để chuẩn bị cho Register API, Login API và phân quyền sau này.

## Vì sao làm task này?

Hệ thống cần có sẵn role trong database trước khi tạo user mới. Khi Register API hoạt động, user mới sẽ được gán role STUDENT mặc định. Khi cần vào trang admin, hệ thống cần có sẵn tài khoản ADMIN hoặc SUPER_ADMIN để quản trị.

## Không làm trong task này

* Không làm Register API
* Không làm Login API
* Không làm JWT
* Không làm Refresh Token API
* Không làm frontend
* Không làm Course
* Không làm Lesson
* Không làm Payment
* Không làm Quiz

## File tài liệu cần dùng

* docs/00_MASTER_CONTEXT.md
* docs/23_MVP_SCOPE.md
* docs/27_DATABASE_PHASES.md
* docs/28_ENUM_DEFINITIONS.md
* docs/29_ERROR_CODE_STANDARD.md
* docs/30_PERMISSION_MATRIX.md
* docs/32_SEED_DATA.md
* docs/18_CODE_CONVENTIONS.md

## Cần tạo hoặc chỉnh sửa

* DataSeeder hoặc DatabaseSeeder
* Role seed logic
* Admin user seed logic
* PasswordEncoder bean nếu chưa có
* Cấu hình admin mặc định trong application.yml hoặc class config nếu cần

## Role mặc định cần seed

* SUPER_ADMIN
* ADMIN
* TEACHER
* CONTENT_EDITOR
* STUDENT

## Admin user mặc định

Email: [admin@example.com](mailto:admin@example.com)
Password dev: Password@123
Role: ADMIN

Lưu ý:

* Password phải được hash bằng BCrypt.
* Không lưu password plain text vào database.
* Seeder phải kiểm tra tồn tại trước khi tạo để tránh trùng dữ liệu khi chạy lại project.

## Checklist

* [ ] Kiểm tra RoleName enum đã có đủ role chưa
* [ ] Kiểm tra RoleRepository có hàm tìm role theo name chưa
* [ ] Kiểm tra UserRepository có hàm tìm user theo email chưa
* [ ] Tạo PasswordEncoder bean nếu chưa có
* [ ] Tạo DataSeeder hoặc DatabaseSeeder
* [ ] Seed các role mặc định
* [ ] Seed admin user mặc định
* [ ] Gán role ADMIN cho admin user
* [ ] Chạy backend không lỗi
* [ ] Kiểm tra database có dữ liệu roles
* [ ] Kiểm tra database có admin user
* [ ] Kiểm tra password admin đã được hash
* [ ] Chạy lại backend lần 2 không bị tạo trùng dữ liệu
* [ ] Ghi learning notes
* [ ] Commit Git

## Cách test sau khi hoàn thành

* Chạy backend
* Kiểm tra bảng roles có đủ role mặc định
* Kiểm tra bảng users có [admin@example.com](mailto:admin@example.com)
* Kiểm tra bảng user_roles có liên kết admin với role ADMIN
* Kiểm tra password_hash không phải Password@123 dạng plain text
* Tắt app và chạy lại lần nữa để chắc chắn không tạo trùng role/user
* Gọi lại GET /api/health
* Mở Swagger kiểm tra vẫn hoạt động

## Kết quả mong muốn

Backend chạy ổn, database có dữ liệu role mặc định và admin user mặc định. Seeder an toàn khi chạy lại nhiều lần.
