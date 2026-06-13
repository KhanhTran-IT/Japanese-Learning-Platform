# Ghi Chép Phỏng Vấn với AI

Tài liệu lưu trữ những câu hỏi, câu trả lời và những insight khi làm việc với AI trong quá trình phát triển dự án.

## Hướng dẫn ghi chép

- **Ghi câu hỏi cụ thể** mà bạn hỏi AI
- **Ghi lại các câu trả lời chính** hoặc những insight quan trọng
- **Ghi context** để hiểu lại sau (task/feature nào được thảo luận)
- **Ghi solution hoặc code snippet** nếu AI cung cấp
- **Đánh giá câu trả lời** (helpful, partially helpful, not helpful)
- **Theo dõi những câu hỏi cần follow-up**

## Template mẫu

```markdown
### [Ngày] - [Chủ đề/Task]

**Context:** Đang làm gì/tình huống gì

**Câu hỏi:**

> Câu hỏi cụ thể hỏi AI

**Câu trả lời chính:**

- Point 1
- Point 2

**Code/Solution được cung cấp:**
\`\`\`java
// Code từ AI
\`\`\`

**Đánh giá:** ⭐⭐⭐⭐⭐ (5/5 hoặc mức độ)

**Follow-up cần hỏi:** Câu hỏi tiếp theo hoặc cần tìm hiểu thêm
```

## Ghi chép

### 12/06/2026 - Setup Spring Boot Project

**Context:** Bắt đầu tạo dự án, cần hỏi về cấu trúc tốt nhất

**Câu hỏi:**

> Cách tổ chức folder structure tốt nhất cho Spring Boot project với Java 17?

**Câu trả lời chính:**

- Sử dụng package by feature
- Tách biệt service, controller, repository, entity
- Tạo config folder cho cấu hình

**Đánh giá:** ⭐⭐⭐⭐⭐

**Follow-up cần hỏi:** Cách setup Spring Security?

---

### 12/06/2026 - VueJS Components

**Context:** Thiết kế UI cho trang dashboard

**Câu hỏi:**

> Cách tổ chức components trong VueJS project lớn?

**Câu trả lời chính:**

- Chia thành Base Components, Feature Components, Layout Components
- Sử dụng composables cho logic tái sử dụng

**Đánh giá:** ⭐⭐⭐⭐

**Follow-up cần hỏi:** Composables vs mixins?

---

## Backend Foundation - Spring Boot

Kiến thức nền tảng về Spring Boot backend foundation - những gì cần biết để xây dựng API server chất lượng.

### 1. Spring Boot Backend Foundation là gì?

**Định nghĩa:** Là quá trình xây dựng nền tảng cơ bản cho backend Spring Boot project, bao gồm:

- Cấu trúc project rõ ràng (controller, service, repository, entity)
- Setup dependencies cần thiết (Web, Data JPA, Swagger)
- Tạo API endpoints đầu tiên (Health Check)
- Cấu hình ứng dụng (application.yml)
- Cài đặt Swagger để document API

**Tại sao quan trọng:**

- Đặt nền tảng tốt từ đầu giúp project dễ scale, maintain về sau
- Không cần refactor lại cấu trúc khi project lớn

---

### 2. Vì sao cần cấu trúc project rõ ràng ngay từ đầu?

**Lợi ích:**

| Lợi ích            | Giải thích                                    |
| ------------------ | --------------------------------------------- |
| **Dễ maintain**    | Mỗi layer có trách nhiệm rõ ràng, dễ tìm code |
| **Dễ test**        | Có thể mock từng layer độc lập                |
| **Dễ scale**       | Thêm feature mới không ảnh hưởng code cũ      |
| **Dễ collaborate** | Team members biết code nằm ở đâu              |
| **Dễ debug**       | Lỗi từ layer nào là rõ ràng                   |

**Ví dụ:**

- Nếu không có cấu trúc rõ ràng: Code bị mix lẫn, khó tìm lỗi, khó thêm feature mới
- Có cấu trúc rõ ràng: API logic ở Controller, Business logic ở Service, Database query ở Repository - rõ ràng và dễ quản lý

---

### 3. ApiResponse dùng để làm gì?

**Mục đích:** Chuẩn hóa format response trả về từ API, để client luôn biết format data nhận được.

**Tại sao cần:**

- Không chuẩn: API này trả `{"user": {...}}`, API khác trả `[{...}]`, client phải xử lý từng cách
- Chuẩn: Tất cả trả về `{"code": 200, "message": "Success", "data": {...}}`, client xử lý 1 cách

**Ví dụ ApiResponse structure:**

```java
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "name": "John",
    "email": "john@example.com"
  },
  "timestamp": "2026-06-12T10:30:00"
}
```

**Lợi ích:**

- Frontend developer biết chính xác structure response
- Dễ log, dễ debug
- Dễ thêm feature (pagination, metadata, etc.)

---

### 4. GlobalExceptionHandler dùng để làm gì?

**Mục đích:** Bắt tất cả lỗi xảy ra trong application và trả về response chuẩn (không bị lỗi 500 lộn xộn).

**Tại sao cần:**

- Không có: Lỗi xảy ra → Stack trace dài → Client nhận 500 lộn xộn
- Có GlobalExceptionHandler: Bắt lỗi → Format chuẩn → Trả về `{"code": 400, "message": "Invalid input"}`

**Ví dụ:**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(404)
            .body(ApiResponse.error(404, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(500)
            .body(ApiResponse.error(500, "Internal server error"));
    }
}
```

**Lợi ích:**

- Tất cả lỗi trả về chuẩn
- Không expose sensitive information
- Dễ log và track lỗi
- Frontend dễ xử lý (biết khi nào retry, khi nào hiện lỗi)

---

### 5. Swagger/OpenAPI dùng để làm gì?

**Mục đích:** Tự động generate documentation cho API, cho phép test API trực tiếp từ browser.

**Tại sao cần:**

- Không có Swagger: Frontend dev phải hỏi Backend dev "endpoint nào?", "param gì?", "response format như nào?"
- Có Swagger: Mở `http://localhost:8080/swagger-ui/index.html` → thấy tất cả endpoints, params, responses

**Lợi ích:**

- API documentation luôn up-to-date (từ code)
- Frontend dev tự khám phá API mà không cần hỏi
- Có thể test API trực tiếp từ UI
- Generate client SDK tự động

**Ví dụ Swagger annotations:**

```java
@GetMapping("/users/{id}")
@Operation(summary = "Get user by ID", description = "Lấy thông tin user theo ID")
@ApiResponse(responseCode = "200", description = "User found")
@ApiResponse(responseCode = "404", description = "User not found")
public ResponseEntity<ApiResponse> getUser(@PathVariable Long id) {
    // ...
}
```

---

### 6. HealthCheck API dùng để làm gì?

**Mục đích:** Kiểm tra xem backend có đang chạy bình thường hay không.

**Tại sao cần:**

- DevOps/Production cần biết server có sống hay chết
- Load balancer dùng để biết route request vào server nào (healthy hoặc failed)
- Monitoring system dùng để alert khi server down

**Ví dụ:**

```java
@GetMapping("/health")
public ResponseEntity<Map<String, Object>> health() {
    return ResponseEntity.ok(Map.of(
        "status", "UP",
        "timestamp", LocalDateTime.now(),
        "database", "CONNECTED"  // có thể kiểm tra database connection
    ));
}
```

**Lợi ích:**

- Xác nhận server đang chạy
- Có thể extend để kiểm tra database connection, cache, etc.
- Dùng cho health checks trong Kubernetes, Docker, Load Balancer

---

### 7. Controller trong Spring Boot có nhiệm vụ gì?

**Nhiệm vụ chính:**

1. **Nhận request từ client** - HTTP GET, POST, PUT, DELETE
2. **Validate input** - Kiểm tra dữ liệu hợp lệ
3. **Gọi Service** - Delegate business logic
4. **Trả về response** - JSON format chuẩn

**Nguyên tắc Controller:**

- ❌ Không viết business logic trong Controller (để cho Service)
- ❌ Không trực tiếp query database (để cho Repository)
- ✅ Chỉ handle HTTP request/response và validation

**Ví dụ:**

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getUser(@PathVariable Long id) {
        // Controller: Nhận request, validate, gọi service, trả response
        User user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
```

---

### 8. Những câu hỏi phỏng vấn có thể gặp từ task này

**Từ Interviewer:**

1. ❓ "Cấu trúc project Spring Boot của bạn thế nào? Tại sao phải chia layer như vậy?"
2. ❓ "Controller, Service, Repository khác nhau ở điểm nào?"
3. ❓ "Tại sao cần ApiResponse wrapper? Không trả về object trực tiếp được?"
4. ❓ "GlobalExceptionHandler làm gì? Nó xử lý lỗi thế nào?"
5. ❓ "API Documentation là gì? Bạn dùng tools gì?"
6. ❓ "HealthCheck endpoint dùng để làm gì trong production?"
7. ❓ "Bạn setup Spring Boot project như thế nào?"
8. ❓ "Swagger annotations quan trọng nhất là gì?"

---

### 9. Câu trả lời mẫu ngắn gọn cho từng câu hỏi

#### 1️⃣ "Cấu trúc project Spring Boot của bạn thế nào?"

**Trả lời:**

> "Mình chia thành 4 layers:
>
> - **Controller layer**: Handle HTTP requests, validation
> - **Service layer**: Business logic (tính toán, quy tắc nghiệp vụ)
> - **Repository layer**: Database queries (CRUD operations)
> - **Entity layer**: Database models
>
> Cách này giúp code dễ test, dễ maintain, và mỗi layer có trách nhiệm rõ ràng."

---

#### 2️⃣ "Controller, Service, Repository khác nhau ở điểm nào?"

**Trả lời:**

> "**Controller**: Nhận HTTP request, validate input, gọi service, trả response
> **Service**: Xử lý business logic, tính toán, gọi repository lấy dữ liệu
> **Repository**: Query database, lưu/lấy/cập nhật/xóa dữ liệu
>
> Ví dụ: Tạo user mới:
>
> - Controller nhận request `/users` + data từ client
> - Service kiểm tra user đã tồn tại chưa, hash password
> - Repository lưu user vào database"

---

#### 3️⃣ "Tại sao cần ApiResponse wrapper?"

**Trả lời:**

> "Để chuẩn hóa response format. Tất cả API trả về cùng structure:
>
> ```json
> {
>   "code": 200,
>   "message": "Success",
>   "data": {...}
> }
> ```
>
> Lợi ích: Frontend biết format chuẩn, dễ xử lý lỗi, dễ log, dễ thêm feature (pagination, metadata)."

---

#### 4️⃣ "GlobalExceptionHandler làm gì?"

**Trả lời:**

> "Bắt tất cả exceptions xảy ra trong application và trả về ApiResponse chuẩn.
>
> Ví dụ:
>
> - Nếu user không tìm thấy → Trả `{code: 404, message: 'User not found'}`
> - Nếu input invalid → Trả `{code: 400, message: 'Invalid input'}`
> - Nếu lỗi khác → Trả `{code: 500, message: 'Internal error'}`
>
> Lợi ích: Không expose sensitive info, dễ log, tất cả lỗi trả về chuẩn."

---

#### 5️⃣ "API Documentation là gì? Bạn dùng tools gì?"

**Trả lời:**

> "API Documentation là tài liệu mô tả tất cả endpoints, parameters, responses của API.
>
> Mình dùng **Swagger/OpenAPI** - tự động generate từ code annotations:
>
> - `@RestController` - đánh dấu REST controller
> - `@GetMapping`, `@PostMapping` - đánh dấu HTTP method
> - `@Operation` - mô tả endpoint
> - `@ApiResponse` - mô tả response
>
> Mở tại `http://localhost:8080/swagger-ui/index.html` để test API."

---

#### 6️⃣ "HealthCheck endpoint dùng để làm gì?"

**Trả lời:**

> "Để kiểm tra xem backend có đang chạy bình thường hay không.
>
> Dùng trong:
>
> - **Load Balancer**: Biết server nào healthy, route request vào đó
> - **Kubernetes/Docker**: Tự động restart container nếu health check fail
> - **Monitoring**: Alert khi server down
>
> Ví dụ: GET `/api/health` → `{status: 'UP', database: 'CONNECTED'}`"

---

#### 7️⃣ "Bạn setup Spring Boot project như thế nào?"

**Trả lời:**

> "Bước 1: Tạo pom.xml với dependencies:
>
> - `spring-boot-starter-web` - REST API
> - `spring-boot-starter-data-jpa` - Database ORM
> - `springdoc-openapi-starter-webmvc-ui` - Swagger
>
> Bước 2: Tạo cấu trúc folder: `controller`, `service`, `repository`, `entity`, `config`
>
> Bước 3: Tạo HealthController endpoint đầu tiên
>
> Bước 4: Cấu hình application.yml (server port, database, logging)
>
> Bước 5: Chạy `mvn spring-boot:run` → kiểm tra Swagger UI"

---

#### 8️⃣ "Swagger annotations quan trọng nhất là gì?"

**Trả lời:**

> "Năm annotations quan trọng:
>
> 1. `@RestController` - Đánh dấu REST controller
> 2. `@GetMapping`, `@PostMapping`, etc. - Đánh dấu HTTP method
> 3. `@Operation` - Mô tả endpoint (summary, description)
> 4. `@ApiResponse` - Mô tả response (responseCode, description)
> 5. `@Parameter` - Mô tả request parameters
>
> Ví dụ:
>
> ````java
> @GetMapping('/{id}')
> @Operation(summary = 'Get user by ID')
> @ApiResponse(responseCode = '200', description = 'User found')
> public User getUser(@Parameter(description = 'User ID') @PathVariable Long id)
> ```"
> ````

---

**💡 Mẹo trả lời:**

- Trả lời ngắn gọn, không quá dài
- Nếu bị hỏi thêm, sẽ giải thích chi tiết hơn
- Dùng ví dụ cụ thể từ dự án của bạn
- Nếu không biết, nói thẳng "Mình chưa bao gặp trường hợp này, nhưng theo hiểu biết thì..."

---

## Auth/User Database Foundation - JPA & Entities

Kiến thức về tạo database entities, repositories, và quan hệ dữ liệu trong Spring Boot + JPA.

### Câu hỏi phỏng vấn có thể gặp

1. ❓ "JPA Entity là gì? Tại sao phải dùng @Entity?"
2. ❓ "ManyToMany relationship khác OneToMany ở điểm nào?"
3. ❓ "Tại sao phải dùng FetchType.LAZY? Nếu không dùng sao?"
4. ❓ "CascadeType.ALL có phải lúc nào cũng dùng được không?"
5. ❓ "Vì sao Infinite Recursion xảy ra khi dùng @Data trên Entity?"
6. ❓ "RefreshToken dùng để làm gì trong authentication?"
7. ❓ "Spring Data JPA Repository là gì? Nó tự động generate queries như thế nào?"
8. ❓ "@JoinTable dùng để làm gì?"
9. ❓ "Enum trong database dùng để làm gì?"
10. ❓ "N+1 Query problem là gì? Làm sao tránh?"

---

### Câu trả lời mẫu ngắn gọn

#### 1️⃣ "JPA Entity là gì? Tại sao phải dùng @Entity?"

**Trả lời:**

> "JPA Entity là Java class đại diện cho 1 table trong database. @Entity annotation bảo cho Hibernate biết class này cần được map với 1 table. Hibernate sẽ tự động:
>
> - Tạo table nếu không tồn tại
> - Map các field của class với các column của table
> - Giúp bạn query/save dữ liệu mà không cần viết SQL"

---

#### 2️⃣ "ManyToMany relationship khác OneToMany ở điểm nào?"

**Trả lời:**

> "**OneToMany**: 1 User có nhiều Orders. 1 table Orders có foreign key user_id pointing đến User.
> **ManyToMany**: Nhiều Users có nhiều Roles, và 1 Role có nhiều Users. Cần 1 join table (user_roles) ở giữa.
>
> Ví dụ User-Role:
>
> - User 1 có Role {ADMIN, TEACHER}
> - User 2 có Role {STUDENT}
> - Role ADMIN có Users {User1, User3}
>
> Cần join table user_roles để map: (user_id, role_id)."

---

#### 3️⃣ "Tại sao phải dùng FetchType.LAZY? Nếu không dùng sao?"

**Trả lời:**

> "**FetchType.LAZY**: Khi query User, Roles không được load. Chỉ load khi gọi `user.getRoles()`.
> **FetchType.EAGER**: Khi query User, Roles luôn được load cùng (dùng JOIN).
>
> Nếu không dùng LAZY:
>
> - Query 100 users → Hibernate thực thi 100 queries để load roles (N+1 problem)
> - Performance rất tệ
>
> Với LAZY:
>
> - Query 100 users → 1 query duy nhất
> - Nếu cần roles, gọi `user.getRoles()` thêm 1 query riêng
> - LAZY tốt hơn vì mỗi lần query ta thường chỉ cần subset columns."

---

#### 4️⃣ "CascadeType.ALL có phải lúc nào cũng dùng được không?"

**Trả lời:**

> "Không. CascadeType.ALL ý là:
>
> - Khi delete User → xóa luôn tất cả Roles của User
> - Điều này rất nguy hiểm cho ManyToMany!
>
> Ví dụ: Bạn xóa 1 User vì user này inactive, Hibernate xóa luôn Role ADMIN trong database → tất cả users khác mất Role ADMIN!
>
> Nên dùng:
>
> - **CascadeType.PERSIST, MERGE** cho ManyToMany (an toàn)
> - **CascadeType.ALL** chỉ cho OneToMany (như User → RefreshTokens, xóa user thì xóa tokens)"

---

#### 5️⃣ "Vì sao Infinite Recursion xảy ra khi dùng @Data trên Entity?"

**Trả lời:**

> "@Data generate `toString()`, `equals()`, `hashCode()` tự động. Vấn đề:
>
> - User có trường `Set<Role> roles`
> - Role có trường `Set<User> users` (inverse side)
> - Khi gọi `user.toString()` → gọi `role.toString()` → gọi `user.toString()` → vòng lặp vô hạn!
>
> Giải pháp:
>
> - Dùng `@Getter`, `@Setter` thay vì `@Data`
> - Hoặc dùng `@ToString(exclude = "roles")` để loại trừ trường gây lặp
> - Hoặc viết toString() tay theo cách an toàn."

---

#### 6️⃣ "RefreshToken dùng để làm gì trong authentication?"

**Trả lời:**

> "Khi user login:
>
> 1. Server sinh AccessToken (ngắn hạn, 15 phút)
> 2. Server sinh RefreshToken (dài hạn, 7 ngày)
> 3. Client lưu cả 2 token
> 4. Khi AccessToken expire, client dùng RefreshToken để request token mới
>
> RefreshToken được lưu trong database để:
>
> - Kiểm tra validity
> - Có thể revoke token (logout) bằng cách xóa RefreshToken từ DB
> - Tính toàn vẹn - prevent token tampering."

---

#### 7️⃣ "Spring Data JPA Repository là gì? Nó tự động generate queries như thế nào?"

**Trả lời:**

> "Spring Data JPA Repository là interface cho phép query database mà không cần viết SQL.
>
> Ví dụ:
>
> ```java
> public interface UserRepository extends JpaRepository<User, Long> {
>     User findByEmail(String email);
>     List<User> findByStatus(UserStatus status);
> }
> ```
>
> Spring tự động:
>
> - Generate implementation class (proxy)
> - Parse tên method: `findByEmail` → SQL `SELECT * FROM users WHERE email = ?`
> - Inject vào @Service
>
> Quy ước tên:
>
> - `findBy*` → WHERE clause
> - `*OrderBy*` → ORDER BY
> - `*And*`, `*Or*` → AND, OR operators."

---

#### 8️⃣ "@JoinTable dùng để làm gì?"

**Trả lời:**

> "@JoinTable định nghĩa join table cho ManyToMany relationship.
>
> ```java
> @JoinTable(
>     name = "user_roles",  // tên join table
>     joinColumns = @JoinColumn(name = "user_id"),  // FK pointing to User
>     inverseJoinColumns = @JoinColumn(name = "role_id")  // FK pointing to Role
> )
> ```
>
> Hibernate tự động:
>
> - Tạo table `user_roles` với columns: user_id, role_id
> - Tạo foreign keys pointing đến users, roles tables
> - Mapping khi save/query."

---

#### 9️⃣ "Enum trong database dùng để làm gì?"

**Trả lời:**

> "Enum ràng buộc các giá trị có thể của 1 field:
>
> ```java
> public enum UserStatus {
>     ACTIVE, INACTIVE, SUSPENDED, DELETED
> }
>
> @Enumerated(EnumType.STRING)  // hoặc ORDINAL
> private UserStatus status;
> ```
>
> Lợi ích:
>
> - Type-safe (không thể gán giá trị random)
> - Database level constraint (MySQL dùng ENUM type)
> - Code dễ hiểu, validation tự động."

---

#### 🔟 "N+1 Query problem là gì? Làm sao tránh?"

**Trả lời:**

> "N+1 problem: Query 1 lần lấy N records, sau đó query N lần để lấy related data.
>
> Ví dụ:
>
> ```java
> // ❌ N+1 problem
> List<User> users = userRepository.findAll();  // 1 query
> users.forEach(u -> System.out.println(u.getRoles()));  // N queries
> ```
>
> Giải pháp:
>
> 1. Dùng FetchType.EAGER (tự động JOIN)
> 2. Dùng @Query với LEFT JOIN FETCH
> 3. Dùng EntityGraph annotation
>
> Ví dụ fix:
>
> ````java
> @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles")
> List<User> findAllWithRoles();
> ```"
> ````

---

**💡 Những điểm cần nhớ khi phỏng vấn:**

- Luôn giải thích **tại sao** không phải chỉ **là cái gì**
- Dùng ví dụ từ User-Role-RefreshToken project của bạn
- Nêu ra performance impact (N+1, Infinite Recursion)
- Biết được best practices (FetchType.LAZY, CascadeType.PERSIST + MERGE)
- Có thể vẽ diagram nếu cần (table structure, relationships)
