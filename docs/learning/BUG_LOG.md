# Nhật Ký Bug và Lỗi

Tài liệu ghi lại các bug, lỗi gặp phải và cách khắc phục. Giúp tránh lặp lại lỗi và làm tài liệu tham khảo.

## Hướng dẫn ghi chép

- **Ghi tên bug rõ ràng** dễ search lại sau
- **Mô tả triệu chứng chi tiết** - bug biểu hiện như thế nào?
- **Ghi nguyên nhân** nếu tìm được
- **Ghi cách fix** cụ thể, có thể reproduce lại
- **Ghi error log/stack trace** nếu có
- **Đánh giá mức độ nghiêm trọng**: 🔴 Critical | 🟠 High | 🟡 Medium | 🟢 Low
- **Ghi status**: ❌ Chưa fix | ✅ Đã fix | 🔄 Đang debug

## Template mẫu

```markdown
### [Bug ID: #001] - [Tên bug ngắn gọn]

**Status:** ❌/✅/🔄
**Mức độ:** 🔴/🟠/🟡/🟢

**Triệu chứng:**

- Hiện tượng 1
- Hiện tượng 2

**Nguyên nhân:**
Mô tả chi tiết nguyên nhân gây ra bug

**Cách fix:**
\`\`\`java
// Code fix
\`\`\`

**Error log:**
\`\`\`
stack trace hoặc error message
\`\`\`

**Test lại:** Cách để verify bug đã fix

**Ghi chú:** Những điểm cần lưu ý
```

## Bug Log

### [Bug ID: #001] - NullPointerException khi fetch user từ database

**Status:** ✅
**Mức độ:** 🔴

**Triệu chứng:**

- API `/api/users/{id}` trả về 500 Internal Server Error
- Không thể lấy thông tin user

**Nguyên nhân:**
Query database không kiểm tra null, entity user không tồn tại nhưng code vẫn cố access property

**Cách fix:**

```java
Optional<User> user = userRepository.findById(id);
if (user.isEmpty()) {
    throw new UserNotFoundException("User not found");
}
return user.get();
```

**Error log:**

```
java.lang.NullPointerException
    at com.example.service.UserService.getUser(UserService.java:45)
```

**Test lại:** Gọi API với user ID không tồn tại, kiểm tra status 404

**Ghi chú:** Luôn sử dụng Optional khi làm việc với database query

---

### [Bug ID: #002] - VueJS component không re-render khi props thay đổi

**Status:** ✅
**Mức độ:** 🟠

**Triệu chứng:**

- Thay đổi data từ parent component
- Child component không update UI

**Nguyên nhân:**
Đang mutate array/object trực tiếp thay vì tạo reference mới

**Cách fix:**

```javascript
// ❌ Sai
this.items.push(newItem);

// ✅ Đúng
this.items = [...this.items, newItem];
```

**Test lại:** Thêm item vào list, kiểm tra UI cập nhật

**Ghi chú:** Vue chỉ detect thay đổi nếu tạo reference mới

---

## 2026-07-17 - Admin login bị redirect sai dashboard

### 1. Lỗi xảy ra khi nào?

Sau khi đăng nhập bằng tài khoản admin, frontend vẫn điều hướng cứng về `/student/dashboard` thay vì `/admin/dashboard`.

### 2. Log lỗi chính

```text
Không có stack trace backend.
Triệu chứng ở frontend: user role ADMIN đăng nhập thành công nhưng bị đưa về dashboard của STUDENT hoặc bị route guard điều hướng lại.
```

### 3. Nguyên nhân

Logic redirect trong `LoginPage.vue` chưa dựa trên role của user sau khi gọi `/api/users/me`. Code cũ giả định mọi user đăng nhập xong đều đi đến khu vực student.

### 4. Cách sửa

Sau khi lấy `userData`, đọc `userData.roles`. Nếu roles có `ADMIN` hoặc `SUPER_ADMIN` thì chuyển đến `/admin/dashboard`, ngược lại chuyển đến `/student/dashboard`.

### 5. Tôi học được gì?

Khi hệ thống có nhiều role, redirect sau đăng nhập phải dựa vào dữ liệu user thật từ backend. Không nên hard-code một dashboard mặc định cho mọi tài khoản.

---

### [Bug ID: #003] - RuntimeException: ADMIN role not found khi chạy Integration Test

**Status:** ✅
**Mức độ:** 🟠

**Triệu chứng:**
- Khi chạy `mvn verify` hoặc các Integration Test (`*IT.java`) có sử dụng `@SpringBootTest`, ApplicationContext bị crash và báo lỗi `ADMIN role not found. Cannot seed admin user.`
- Nguyên nhân: Các file test (ví dụ `AuthControllerIT`) có sử dụng `@MockBean` để mock `RoleRepository` và `UserRepository`. Tuy nhiên, Bean `DatabaseSeeder` (một `CommandLineRunner`) vẫn được load bởi Spring Boot và cố gắng gọi `roleRepository.findByName()`. Do repository đã bị mock (trả về Optional.empty), việc tìm role ADMIN bị thất bại dẫn tới exception.

**Nguyên nhân:**
Spring Boot mặc định load toàn bộ các bean trong context, bao gồm cả `DatabaseSeeder`, trong khi môi trường mock test không có dữ liệu thực tế.

**Cách fix:**
Sử dụng `@ConditionalOnProperty` để vô hiệu hóa `DatabaseSeeder` nếu property `app.seeder.enabled` = false. Trong file cấu hình `application.yml` dành riêng cho môi trường test (`src/test/resources/application.yml`), ta set `app.seeder.enabled: false`.

```java
// Trong DatabaseSeeder.java
@ConditionalOnProperty(name = "app.seeder.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner { ... }
```

**Test lại:** Chạy `mvn verify`, ApplicationContext load thành công và không bị dính exception của Seeder.

**Ghi chú:** Khi viết Integration Test với `@SpringBootTest`, cần cẩn thận với các Bean khởi tạo dữ liệu ban đầu (Seeder, Runner). Luôn cấp cơ chế bật/tắt chúng qua cấu hình để không xung đột với các `@MockBean`.
