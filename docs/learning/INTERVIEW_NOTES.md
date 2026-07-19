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

---

## Login API + JWT Token Generation

### 1. Tóm tắt ngắn gọn

Login API là endpoint `POST /api/auth/login` để xác thực user bằng email/password. Nếu đúng, server sinh 2 JWT tokens: access token (ngắn hạn, 15 phút) để call APIs và refresh token (dài hạn, 7 ngày) để lấy access token mới. Refresh token được lưu database để có thể revoke khi logout.

**Kiến trúc:** Request DTO → Controller → Service → Password verify → JWT generation → Save refresh token → Response DTO

**Công nghệ:** JJWT 0.12.5, PasswordEncoder.matches(), @Transactional, LocalDateTime

### 2. Kiến thức phỏng vấn liên quan

- **JWT (JSON Web Token):** Cấu trúc token, 3 phần (header.payload.signature), stateless authentication
- **Access Token vs Refresh Token:** Tại sao cần 2 loại token? Khi nào dùng cái nào?
- **Token Expiration:** Cách tính expiration time, khi nào throw TOKEN_EXPIRED?
- **Enumerate Attack:** Tại sao lỗi login trả chung "Email hoặc mật khẩu không đúng"?
- **Secret Key Management:** Lưu secret key ở đâu? Độ dài bao nhiêu?
- **JJWT Library:** Sự khác biệt JJWT 0.12.5 vs phiên bản cũ?
- **Password Verification:** BCryptPasswordEncoder.matches() hoạt động thế nào?
- **Last Login Tracking:** Tại sao cập nhật lastLoginAt?
- **Refresh Token Storage:** Lưu database hay stateless?

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: "JWT là gì? Cấu trúc như thế nào?"

**Trả lời:**

> "JWT (JSON Web Token) là một chuỗi kí hiệu dùng để truyền thông tin an toàn giữa client và server.
>
> **Cấu trúc 3 phần (header.payload.signature):**
>
> 1. Header: Định nghĩa loại token (JWT) và thuật toán (HS256)
> 2. Payload: Dữ liệu user (id, email, roles, expiration)
> 3. Signature: Chứng thực token (tính từ header+payload+secret key)
>
> **Ví dụ:**
>
> ```
> eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwiZW1haWwiOiJ1c2VyQGV4YW1wbGUuY29tIiwicm9sZXMiOlsiU1RVREVOVCRFSI0iLCJleHAiOjE2MjMwNDMyMDB9.signature
> ```
>
> **Decode bằng jwt.io:**
>
> - Header: `{\"alg\": \"HS256\", \"typ\": \"JWT\"}`
> - Payload: `{\"id\": 1, \"email\": \"user@example.com\", \"roles\": [\"STUDENT\"], \"exp\": 1623043200}`
> - Signature: Được server xác thực bằng secret key
>
> **Ưu điểm:** Stateless (không cần lưu database), self-contained (đủ dữ liệu decode), bảo mật (signed)."

#### Câu 2: "Access Token vs Refresh Token - tại sao cần 2 loại?"

**Trả lời:**

> "**Access Token (ngắn hạn - 15 phút):**
>
> - Dùng để xác thực mỗi request (gửi trong header Authorization)
> - Stateless: Server chỉ verify signature, không cần query database
> - Nếu bị leak: Hacker chỉ có 15 phút để dùng trước khi hết hạn
>
> **Refresh Token (dài hạn - 7 ngày):**
>
> - Dùng để lấy access token mới khi hết hạn
> - Lưu database: Server có thể revoke nếu cần (logout, change password)
> - Khi logout: Xóa refresh token khỏi DB, user phải login lại
>
> **Tại sao cần 2:**
>
> - Nếu chỉ 1 token dài hạn → bảo mật tệ (nếu leak, hacker có 7 ngày)
> - Nếu chỉ 1 token ngắn hạn → UX tệ (user phải login lại mỗi 15 phút)
> - 2 tokens = bảo mật + UX: Access token ngắn (bảo mật), Refresh token dài (UX)
>
> **Flow:**
>
> ````
> 1. User login → server trả access token (15p) + refresh token (7 ngày)
> 2. Client gọi API, gửi access token trong Authorization header
> 3. Access token hết hạn → Client dùng refresh token để lấy access token mới
> 4. Refresh token hết hạn → Cần login lại
> ```"
> ````

#### Câu 3: "Enumerate Attack là gì? Tại sao login phải trả lỗi chung?"

**Trả lời:**

> "Enumerate Attack: Hacker thử rất nhiều email để tìm email người dùng hợp lệ.
>
> **❌ Cách sai:**
>
> ```json
> POST /api/auth/login
> {\"email\": \"notexist@example.com\", \"password\": \"anything\"}
>
> Response:
> {\"error\": \"Email không tồn tại\"}  // ← Hacker biết email này không dùng
> ```
>
> → Hacker dùng danh sách email và xác định email nào có người dùng
>
> **✅ Cách đúng:**
>
> ```json
> POST /api/auth/login
> {\"email\": \"notexist@example.com\", \"password\": \"anything\"}
>
> Response:
> {\"error\": \"Email hoặc mật khẩu không đúng\", \"code\": 2002}  // ← Cùng lỗi
> ```
>
> → Hacker không biết là email không tồn tại hay password sai
>
> **Trong code:**
>
> ```java
> User user = userRepository.findByEmail(email)
>     .orElseThrow(() -> new AppException(ErrorCode.LOGIN_FAILED));  // ← Email không tìm thấy
>
> if (!passwordEncoder.matches(password, user.getPasswordHash())) {
>     throw new AppException(ErrorCode.LOGIN_FAILED);  // ← Password sai
> }
> // Cả 2 case throw cùng lỗi 2002
> ```
>
> **Best practice:** Luôn trả lỗi chung để ngăn enumerate attack."

#### Câu 4: "Secret key trong JWT phải độ dài bao nhiêu?"

**Trả lời:**

> "HMAC-SHA256 cần **ít nhất 256 bits (32 bytes)**.
>
> **Vì sao:**
>
> - Signature tạo từ header + payload + secret key
> - Nếu secret key quá ngắn (ví dụ 8 bytes) → Hacker brute-force dễ dàng
> - 256 bits ~ 43 ký tự Base64 → Đủ mạnh
>
> **Ví dụ từ application.yml:**
>
> ```yaml
> jwt:
>   secret:
>     access: \"your-super-secret-key-with-at-least-256-bits-32-bytes-long\"
>     refresh: \"your-different-secret-key-also-256-bits-minimum\"
> ```
>
> **Trong JwtUtil:**
>
> ```java
> byte[] keyBytes = Decoders.BASE64.decode(accessSecret);
> SecretKey signingKey = Keys.hmacShaKeyFor(keyBytes);
> // keyBytes phải >= 32 bytes
> ```
>
> **Best practice:**
>
> - Lưu secret key trong environment variable hoặc secrets manager
> - Không hardcode vào code
> - Khác nhau cho access token và refresh token"

#### Câu 5: "JJWT 0.12.5 khác gì phiên bản cũ?"

**Trả lời:**

> "JJWT 0.12.5 là phiên bản mới, API thay đổi:
>
> **❌ Cách cũ (JJWT 0.11.x):**
>
> ```java
> Claims claims = Jwts.parser()
>     .setSigningKey(secret)
>     .parseClaimsJws(token)
>     .getBody();
> ```
>
> **✅ Cách mới (JJWT 0.12.5+):**
>
> ```java
> Claims claims = Jwts.parser()
>     .verifyWith(secretKey)
>     .build()
>     .parseSignedClaims(token)
>     .getPayload();  // ← Lấy payload từ SignedJws
> ```
>
> **Khác biệt chính:**
>
> 1. `setSigningKey()` → `verifyWith()`
> 2. `parseClaimsJws()` → `parseSignedClaims()`
> 3. `.getBody()` → `.getPayload()`
> 4. Bắt buộc gọi `.build()` trước khi parse
>
> **Dependency mới:**
>
> ````xml
> <dependency>
>     <groupId>io.jsonwebtoken</groupId>
>     <artifactId>jjwt-api</artifactId>
>     <version>0.12.5</version>
> </dependency>
> <dependency>
>     <groupId>io.jsonwebtoken</groupId>
>     <artifactId>jjwt-impl</artifactId>
>     <version>0.12.5</version>
>     <scope>runtime</scope>
> </dependency>
> <dependency>
>     <groupId>io.jsonwebtoken</groupId>
>     <artifactId>jjwt-jackson</artifactId>
>     <version>0.12.5</version>
>     <scope>runtime</scope>
> </dependency>
> ```"
> ````

#### Câu 6: "Token expiration check như thế nào?"

**Trả lời:**

> "JWT payload chứa claim `exp` (expiration time, Unix timestamp in seconds).
>
> **Ví dụ payload:**
>
> ```json
> {
>   \"id\": 1,
>   \"email\": \"user@example.com\",
>   \"exp\": 1623043200,  // ← Unix timestamp (June 7, 2021)
>   \"iat\": 1622956800   // ← Unix timestamp khi token tạo
> }
> ```
>
> **Verify token:**
>
> ```java
> private boolean isAccessTokenExpired(String token) {
>     Date expiration = extractAccessExpiration(token);
>     return expiration.before(new Date());  // ← So sánh với now
> }
>
> private Date extractAccessExpiration(String token) {
>     return extractAccessClaim(token, Claims::getExpiration);
> }
> ```
>
> **Khi verify:**
>
> 1. Parse token (verify signature)
> 2. Extract exp claim
> 3. So sánh `exp` với current time
> 4. Nếu exp < now → TOKEN_EXPIRED
>
> **JJWT tự động verify:**
>
> - JJWT library tự động throw `ExpiredJwtException` nếu token hết hạn
> - Ta chỉ cần catch và throw AppException(ErrorCode.TOKEN_EXPIRED)"

#### Câu 7: "LastLoginAt được dùng để làm gì?"

**Trả lời:**

> "Cập nhật `lastLoginAt` giúp:
>
> 1. **Thống kê sử dụng:** Biết user hoạt động lần cuối khi nào
> 2. **Phát hiện account compromise:** Nếu user không login nhưng lastLoginAt cập nhật → bảo mật issue
> 3. **Cleanup inactive users:** Xóa hoặc disable users không login trong X ngày
> 4. **Audit logging:** Kiểm tra lịch sử truy cập
>
> **Trong code:**
>
> ```java
> @Override
> @Transactional
> public LoginResponse login(LoginRequest request) {
>     // ... verify password ...
>
>     // Cập nhật last login
>     user.setLastLoginAt(LocalDateTime.now());
>     userRepository.save(user);
>
>     // ... generate tokens ...
> }
> ```
>
> **Best practice:**
>
> - Cập nhật khi login thành công (không phải khi password sai)
> - Dùng @Transactional để bảo đảm consistency"

#### Câu 8: "Refresh token phải lưu database hay có thể stateless?"

**Trả lời:**

> "Refresh token **PHẢI lưu database** (stateful), không thể stateless như access token.
>
> **Vì sao:**
>
> - Access token: Stateless OK (ngắn hạn, verify bằng signature)
> - Refresh token: Cần database để revoke (logout, change password)
>
> **Scenario cần revoke:**
>
> 1. User logout → Xóa refresh token từ DB → Refresh token không còn hợp lệ
> 2. User change password → Xóa tất cả refresh token cũ
> 3. Admin block user → Xóa token
>
> **Nếu refresh token stateless (sai):**
>
> ```
> User logout → Xóa token ở client
> Hacker có refresh token cũ → Vẫn có thể lấy access token mới
> ❌ Logout không hiệu quả
> ```
>
> **Nếu refresh token lưu DB (đúng):**
>
> ```
> User logout → Delete refresh token từ DB
> Hacker có refresh token cũ → Query DB check → Not found
> ✅ Logout hiệu quả
> ```
>
> **Trong code:**
>
> ````java
> RefreshToken refreshTokenEntity = RefreshToken.builder()
>     .user(user)
>     .token(refreshTokenString)
>     .expiredAt(LocalDateTime.now().plusDays(7))
>     .build();
> refreshTokenRepository.save(refreshTokenEntity);  // ← Lưu DB
> ```"
> ````

#### Câu 9: "Nếu user login từ 2 device cùng lúc, cần làm gì?"

**Trả lời:**

> "Có 2 cách:
>
> **Cách 1: Multi-device (cho phép nhiều device login cùng lúc)**
>
> - User A login từ desktop → Lưu refresh token vào DB
> - User A login từ mobile → Thêm refresh token mới vào DB
> - Mỗi device có refresh token riêng
> - Logout ở 1 device không ảnh hưởng device khác
>
> **Cách 2: Single-device (chỉ cho phép 1 device login)**
>
> - User A login từ desktop → Lưu refresh token
> - User A login từ mobile → Xóa refresh token cũ, lưu token mới
> - Logout ở mobile → User phải login lại trên desktop
>
> **Implement cách 2 (đơn giản hơn):**
>
> ```java
> public LoginResponse login(LoginRequest request) {
>     User user = userRepository.findByEmail(request.getEmail())
>         .orElseThrow(...);
>
>     // Xóa token cũ
>     refreshTokenRepository.deleteByUserId(user.getId());
>
>     // Lưu token mới
>     RefreshToken token = new RefreshToken(...);
>     refreshTokenRepository.save(token);
>
>     return ...;
> }
> ```
>
> **Best practice:** Implement cách 2 trước (đơn giản). Upgrade sang cách 1 khi cần."

#### Câu 10: "Làm sao verify refresh token khi client request lấy access token mới?"

**Trả lời:**

> "Khi client gửi refresh token (thông qua POST /api/auth/refresh-token):
>
> ```java
> @PostMapping(\"/refresh-token\")
> public ApiResponse<TokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
>     // 1. Verify JWT signature (check token không bị tamper)
>     if (!jwtUtil.isRefreshTokenValid(request.getRefreshToken(), ...)) {
>         throw new AppException(ErrorCode.TOKEN_INVALID);
>     }
>
>     // 2. Query DB: token có tồn tại không? (check nó chưa bị revoke)
>     RefreshToken tokenEntity = refreshTokenRepository.findByToken(request.getRefreshToken())
>         .orElseThrow(() -> new AppException(ErrorCode.TOKEN_REVOKED));
>
>     // 3. Check token chưa hết hạn
>     if (tokenEntity.getExpiredAt().isBefore(LocalDateTime.now())) {
>         throw new AppException(ErrorCode.TOKEN_EXPIRED);
>     }
>
>     // 4. Extract user từ token
>     User user = tokenEntity.getUser();
>
>     // 5. Generate access token mới
>     String newAccessToken = jwtUtil.generateAccessToken(user);
>
>     return ApiResponse.success(new TokenResponse(newAccessToken));
> }
> ```
>
> **Verify steps:**
>
> 1. JWT signature verification (JJWT tự động)
> 2. Database lookup (check token chưa bị revoke)
> 3. Expiration check (check ngày hết hạn)
> 4. Generate new access token
>
> **Lợi ích 2 database lookups:**
>
> - JWT signature verify nhanh (stateless)
> - Database lookup bảo đảm token có thể revoke (stateful)
> - Cân bằng bảo mật + performance"

---

## Register API - Xây dựng Endpoint Đăng Ký Tài Khoản

### 1. Tóm tắt ngắn gọn

Register API là endpoint `POST /api/auth/register` cho phép user mới đăng ký tài khoản. API này validate dữ liệu đầu vào, check email trùng, hash password bằng BCrypt, assign role STUDENT mặc định, và trả response chuẩn.

**Kiến trúc:** Request DTO → Controller → Service → Repository → DB → Response DTO

**Công nghệ:** Bean Validation, BCryptPasswordEncoder, @Transactional, ErrorCode chuẩn hóa

### 2. Kiến thức phỏng vấn liên quan

- **DTO (Data Transfer Object):** Tại sao phải tách Controller/Service input-output khỏi Entity?
- **Bean Validation:** Cách Spring tự động validate request dữ liệu mà không cần `if-else` trong Controller?
- **Password Hashing:** Tại sao phải hash password? Sự khác biệt BCrypt vs MD5/SHA256?
- **HTTP Status Code:** Khi nào dùng 400, 409, 500?
- **Exception Handling:** Cách handle multiple exceptions trong một endpoint?
- **@Transactional:** Tại sao cần transaction cho register? Khi nào rollback?
- **Spring Security / PasswordEncoder:** Cách Spring cung cấp bean, dependency injection?

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: "DTO là gì? Tại sao phải tách DTO khỏi Entity khi làm API?"

**Trả lời:**

> "DTO (Data Transfer Object) là class riêng dùng để nhận/trả dữ liệu từ API, không phải Entity database.
>
> **Tại sao phải tách:**
>
> 1. **Security**: Entity có thể chứa `passwordHash`, `createdAt`, `deletedAt` - thông tin nhạy cảm không nên expose ra API.
> 2. **Flexibility**: Frontend và DB schema có thể khác. DTO cho phép ta customize input/output mà không ảnh hưởng DB.
> 3. **Infinite Recursion**: User ↔ Role là ManyToMany, nếu trả Entity trực tiếp → JSON serializer vòng lặp vô hạn.
> 4. **API Versioning**: Có thể tạo nhiều DTO khác nhau cho v1, v2 API mà cùng Entity.
>
> **Ví dụ:**
>
> ````java
> // ❌ Sai - expose Entity trực tiếp
> @PostMapping(\"/register\")
> public User register(@RequestBody User user) {
>     // ...
> }
> // → Response có `passwordHash`, `createdAt`, etc.
>
> // ✅ Đúng - dùng DTO
> @PostMapping(\"/register\")
> public ApiResponse<RegisterResponse> register(@RequestBody RegisterRequest request) {
>     // ...
> }
> // → Response chỉ có `id`, `fullName`, `email`, `roles`
> ```"
> ````

#### Câu 2: "Bean Validation là gì? Cách nó hoạt động?"

**Trả lời:**

> "Bean Validation là chuẩn Java để validate dữ liệu thông qua annotation.
>
> **Cách hoạt động:**
>
> 1. Thêm annotation vào DTO: `@NotBlank`, `@Email`, `@Size`, etc.
> 2. Thêm `@Valid` vào Controller parameter
> 3. Spring tự động kiểm tra trước khi gọi method
> 4. Nếu lỗi → MethodArgumentNotValidException → GlobalExceptionHandler xử lý
>
> **Ví dụ:**
>
> ```java
> @Data
> public class RegisterRequest {
>     @NotBlank(message = \"Email không được trống\")
>     @Email(message = \"Email không đúng định dạng\")
>     private String email;
>
>     @NotBlank
>     @Size(min = 8, message = \"Password ≥ 8 ký tự\")
>     private String password;
> }
>
> @Controller
> public ApiResponse<RegisterResponse> register(
>     @Valid @RequestBody RegisterRequest request  // ← Validation happens here
> ) {
>     // Nếu dữ liệu sai → không chạy đến đây, GlobalExceptionHandler xử lý
> }
> ```
>
> **Ưu điểm:** Không cần `if (request.getEmail() == null)` trong Controller, code sạch hơn."

#### Câu 3: "Tại sao dùng BCrypt để hash password? Sao không dùng MD5?"

**Trả lời:**

> "Lý do dùng BCrypt thay vì MD5:
>
> | Tiêu chí               | BCrypt              | MD5                    |
> | ---------------------- | ------------------- | ---------------------- |
> | **Speed**              | Chậm (intentional)  | Nhanh                  |
> | **Brute-force safety** | ✅ ~1,000 guesses/s | ❌ 1 tỷ guesses/s      |
> | **Salt**               | Tự động random salt | Không                  |
> | **Collisions**         | Hiếm                | Có lỗi MD5 collision   |
> | **Rainbow table**      | Không tồn tại       | Có pre-computed tables |
>
> **Ví dụ:**
>
> - Password: \"Password123\"
> - MD5: `482c811da5d5b4bc6d497ffa98491e38` (nhanh, dễ tấn công)
> - BCrypt: `$2a$10$N9qo8uLOickgx2ZMRZoMye...` (khác mỗi lần mặc dù cùng password, chậm)
>
> **OWASP recommend:** BCrypt, PBKDF2, hoặc Argon2 - không bao giờ MD5/SHA1."

#### Câu 4: "HTTP status code nào dùng cho error register?"

**Trả lời:**

> "**400 Bad Request**: Dữ liệu sai định dạng, validation fail
>
> - Email không đúng format
> - Password xác nhận không khớp
> - Missing required field
>
> **409 Conflict**: Tài nguyên đã tồn tại
>
> - Email đã đăng ký
> - Username trùng (nếu có)
>
> **500 Internal Server Error**: Bug server
>
> - Role STUDENT không tồn tại → SYS error
> - Database connection error
>
> **Ví dụ:**
>
> ````java
> // Email format sai → 400
> throw new AppException(ErrorCode.VALIDATION_ERROR);
>
> // Email đã tồn tại → 409
> throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
>
> // Role không tìm thấy → 500 (bug)
> throw new AppException(ErrorCode.ROLE_NOT_FOUND);
> ```"
> ````

#### Câu 5: "Cách handle multiple exceptions trong một endpoint?"

**Trả lời:**

> "Dùng `GlobalExceptionHandler` để centralize exception handling:
>
> ```java
> @RestControllerAdvice
> @Slf4j
> public class GlobalExceptionHandler {
>
>     // ❌ Validation error (Bean Validation)
>     @ExceptionHandler(MethodArgumentNotValidException.class)
>     public ApiResponse<String> handleValidationException(MethodArgumentNotValidException e) {
>         String message = e.getBindingResult().getFieldError().getDefaultMessage();
>         return ApiResponse.error(VALIDATION_ERROR, message);
>     }
>
>     // ❌ Business logic error (AppException)
>     @ExceptionHandler(AppException.class)
>     public ApiResponse<String> handleAppException(AppException e) {
>         return ApiResponse.error(e.getErrorCode());
>     }
>
>     // ❌ Unexpected error
>     @ExceptionHandler(Exception.class)
>     public ApiResponse<String> handleException(Exception e) {
>         log.error(\"Unexpected error\", e);
>         return ApiResponse.error(UNCATEGORIZED_EXCEPTION);
>     }
> }
> ```
>
> **Ưu điểm:**
>
> - Controller code sạch, chỉ có logic
> - Tất cả lỗi format chuẩn
> - Dễ bảo trì, thêm exception type mới"

#### Câu 6: "@Transactional trong register API - khi nào commit/rollback?"

**Trả lời:**

> "@Transactional đảm bảo atomicity - hoặc tất cả thành công, hoặc tất cả fail.
>
> **Khi commit:**
>
> - Tất cả database operations thành công
> - Method kết thúc bình thường (không exception)
>
> **Khi rollback:**
>
> - Bất kỳ database operation fail
> - Ném exception (checked hoặc unchecked)
> - Tất cả INSERT/UPDATE từ đầu được undo
>
> **Ví dụ:**
>
> ```java
> @Transactional
> public RegisterResponse register(RegisterRequest request) {
>     // Step 1: Check email exists
>     if (userRepository.existsByEmail(...)) {
>         throw new AppException(...);  // ← Rollback, ko lưu gì
>     }
>
>     // Step 2: Get role
>     Role role = roleRepository.findByName(...).orElseThrow(...);
>
>     // Step 3: Create user
>     User user = new User(...);
>     userRepository.save(user);  // ← Commit nếu không có exception ở dưới
>
>     return toResponse(user);
> }
> ```
>
> **Mà không @Transactional:**
>
> - Step 2, 3 có thể save partial data
> - Khó recover khi có lỗi"

#### Câu 7: "RegisterRequest có `@Valid` - điều gì sẽ xảy ra nếu quên @Valid?"

**Trả lời:**

> "Nếu quên `@Valid` trước `@RequestBody`:
>
> ```java
> // ❌ Quên @Valid
> @PostMapping(\"/register\")
> public ApiResponse<RegisterResponse> register(@RequestBody RegisterRequest request) {
>     // request có thể chứa null, blank fields
>     // Không có validation tự động
> }
> ```
>
> **Hậu quả:**
>
> - Email null → userRepository.existsByEmail(null) → Database error
> - Password blank → passwordEncoder.encode(\"\") → Lưu hash của string rỗng
> - Frontend validate không, backend không validate → Rác dữ liệu vào DB
>
> **Nếu có @Valid:**
>
> ```java
> // ✅ Có @Valid
> @PostMapping(\"/register\")
> public ApiResponse<RegisterResponse> register(
>     @Valid @RequestBody RegisterRequest request
> ) {
>     // Spring tự động validate theo annotation
>     // Nếu sai → MethodArgumentNotValidException → GlobalExceptionHandler
> }
> ```
>
> **Best practice:** Luôn thêm `@Valid` khi nhận DTO từ client."

#### Câu 8: "Service là một class, sao Spring có thể autowire được?"

**Trả lời:**

> "Vì AuthService có `@Service` annotation:
>
> ```java
> @Service
> @RequiredArgsConstructor
> public class AuthServiceImpl implements AuthService {
>     private final UserRepository userRepository;
>     private final RoleRepository roleRepository;
>     private final PasswordEncoder passwordEncoder;
> }
> ```
>
> **Cách hoạt động (Dependency Injection):**
>
> 1. Spring scan class có `@Service`, `@Controller`, `@Repository`, etc.
> 2. Spring tạo bean cho những class đó
> 3. Khi constructor có `@RequiredArgsConstructor`:
>    - Lombok tự động tạo constructor với các field `final`
>    - Spring inject beans vào constructor
> 4. Controller inject AuthService:
>
> ```java
> @RestController
> @RequiredArgsConstructor
> public class AuthController {
>     private final AuthService authService;  // Spring inject vào đây
> }
> ```
>
> **Lợi ích:**
>
> - Loose coupling (Controller không cần `new AuthServiceImpl()`)
> - Dễ test (mock AuthService)
> - Spring manage lifecycle của beans"

#### Câu 9: "Tại sao lại dùng interface AuthService thay vì trực tiếp dùng AuthServiceImpl?"

**Trả lời:**

> "**Lý do dùng interface:**
>
> 1. **Loose coupling**: Controller phụ thuộc vào interface, không phụ thuộc implementation
>
> ```java
> // ✅ Tốt - phụ thuộc interface
> @Autowired
> private AuthService authService;  // Có thể swap AuthServiceImpl bằng class khác
>
> // ❌ Tighter - phụ thuộc implementation
> @Autowired
> private AuthServiceImpl authService;  // Khó swap
> ```
>
> 2. **Dễ test**: Mock interface cho unit test
>
> ```java
> @Test
> void testRegister() {
>     AuthService mockService = mock(AuthService.class);  // ← Mock interface
>     mockService.register(...).thenReturn(...);
> }
> ```
>
> 3. **Refactor**: Có thể tạo multiple implementation (vd: AuthServiceImpl, AuthServiceWithLDAPImpl)
> 4. **Future-proof**: Nếu cần thêm behavior (logging, caching), dùng Proxy pattern với interface"

#### Câu 10: "ErrorCode là enum - tại sao không dùng String message trực tiếp?"

**Trả lời:**

> "Enum ErrorCode tập trung lỗi và giúp chuẩn hóa:
>
> ```java
> public enum ErrorCode {
>     EMAIL_ALREADY_EXISTS(2001, HttpStatus.CONFLICT, \"Email đã tồn tại\"),
>     PASSWORD_CONFIRM_NOT_MATCH(2010, HttpStatus.BAD_REQUEST, \"Mật khẩu xác nhận không khớp\"),
>     ROLE_NOT_FOUND(3002, HttpStatus.INTERNAL_SERVER_ERROR, \"Không tìm thấy role\"),
> }
> ```
>
> **Lợi ích:**
>
> 1. **Chuẩn hóa**: Mỗi error có code, message, HTTP status duy nhất
> 2. **Frontend có thể parse code**: Hiển thị khác nhau theo error code, không phải parse message string
> 3. **I18n (Internationalization)**: Code không thay đổi, chỉ message dịch
> 4. **Tránh typo**: IDE autocomplete `ErrorCode.EMAIL_ALREADY_EXISTS`, không phải type string \"EMAIL_ALREADY_EXISTS\"
> 5. **Centralize**: Tất cả error definitions ở một nơi, dễ bảo trì
>
> **Ví dụ API response:**
>
> ```json
> {
>     \"success\": false,
>     \"code\": 2001,
>     \"message\": \"Email đã tồn tại\",
>     \"data\": null
> }
> ```
>
> → Frontend biết code 2001 = email conflict, có thể highlight ô email field"

## Mạch 4: JWT Authentication & System Security (Bảo mật Hệ thống)

### 1. Kiến trúc Bảo mật Tổng thể (Security Architecture)

Trong module này, chúng ta xây dựng hệ thống bảo mật theo mô hình **Stateless JWT Authentication**. Luồng đi của dữ liệu không phụ thuộc vào Server Session, mà dựa vào 2 loại Token:

- **Access Token (Ngắn hạn - 15 phút):** Đóng vai trò như "thẻ ra vào", dùng để chứng minh danh tính khi gọi các Protected API. Không lưu ở Database.
- **Refresh Token (Dài hạn - 7 ngày):** Đóng vai trò như "chìa khóa chính", dùng để cấp lại Access Token mới khi thẻ cũ hết hạn. Được lưu ở Database bảng `refresh_tokens` để phục vụ cơ chế **Revocation** (Thu hồi quyền lực khẩn cấp).

### 2. Các Bài Toán Thực Tế (Scenario-based Interview)

#### Câu 1: "Luồng xử lý khi người dùng Login diễn ra như thế nào ở tầng Backend? Tại sao phải dùng BCrypt?"

**Trả lời:**

> "Khi nhận Request Login, Backend sẽ đi qua 4 bước:
>
> 1. **Định danh (Identification):** Truy vấn Database tìm `User` theo `email`. Nếu không thấy -> Bắn lỗi `AUTH_001` (Sai email/mật khẩu).
> 2. **Xác thực (Authentication):** Sử dụng `PasswordEncoder.matches(rawPassword, hashedPassword)` để đối chiếu. BCrypt sử dụng thuật toán băm một chiều (One-way hash) kết hợp với `Salt` ngẫu nhiên. Nhờ Salt, cùng 1 mật khẩu `123456` nhưng 2 user sẽ có 2 chuỗi Hash hoàn toàn khác nhau, chống lại kiểu tấn công Rainbow Table.
> 3. **Kiểm tra trạng thái (Status Check):** Đảm bảo `User.getStatus() == ACTIVE`. Nếu tài khoản bị khóa -> Từ chối cấp Token.
> 4. **Cấp phát (Issuance):** Sử dụng `JwtUtil` để mã hóa (Sign) Access Token chứa payload (id, email, roles). Đồng thời tạo Refresh Token lưu xuống DB, sau đó trả cả 2 về cho Client thông qua `LoginResponse` DTO. Không bao giờ trả `User Entity` để tránh rò rỉ `passwordHash`."

#### Câu 2: "Tại sao không gia hạn thẳng Access Token mà phải sinh ra Refresh Token? Có phải làm phức tạp hóa hệ thống không?"

**Trả lời:**

> "Đó là sự đánh đổi giữa **Bảo mật** và **Trải nghiệm người dùng (UX)**:
>
> - Nếu Access Token sống quá lâu (VD: 1 tháng): Nếu Hacker lấy cắp được Access Token, chúng có toàn quyền phá hoại hệ thống trong suốt 1 tháng. Vì Token này Stateless (không lưu ở Server) nên Server không thể thu hồi (Revoke) nó ngay lập tức.
> - Nếu Access Token sống quá ngắn (VD: 15 phút): Hệ thống rất an toàn, nhưng UX cực kỳ tệ vì user cứ 15 phút lại bị văng ra yêu cầu nhập lại mật khẩu.
> - **Giải pháp:** Ta sinh ra Refresh Token (sống 7 ngày, lưu ở DB). Khi Access Token 15 phút hết hạn, Client âm thầm mang Refresh Token lên Server để xin Access Token mới. Do Refresh Token nằm ở DB, Server có quyền kiểm tra xem user này có bị khóa tài khoản chưa, hoặc Token này có bị thu hồi (`revoked = true`) hay không trước khi cấp Access Token mới."

#### Câu 3: "Làm thế nào để hệ thống thực hiện chức năng Logout khi Access Token là Stateless (Server không quản lý)?"

**Trả lời:**

> "Vì Access Token không được lưu trong DB, Server không thể ép nó hết hạn ngay lập tức (trừ khi dùng cơ chế Blacklist Redis tốn kém). Giải pháp thông minh nhất trong kiến trúc JWT là **Revoke Refresh Token**:
>
> 1. Khi gọi API Logout, Client gửi kèm Refresh Token hiện tại.
> 2. Backend query DB tìm Token đó và set `revoked = true` hoặc xóa hẳn bản ghi.
> 3. Ở phía Client, Frontend sẽ xóa Access Token khỏi LocalStorage/Cookies.
> 4. **Bảo mật kép:** Nếu kẻ gian vẫn cầm Access Token cũ, chúng chỉ dùng được tối đa vài phút cho đến khi token này tự hết hạn. Khi chúng dùng Refresh Token cũ để xin Token mới, Server sẽ chặn đứng vì trạng thái đã là `revoked`. Đây gọi là kỹ thuật **Graceful Degradation** trong bảo mật."

#### Câu 4: "JwtAuthenticationFilter hoạt động như thế nào trong chuỗi Filter Chain của Spring Security?"

**Trả lời:**

> "Nó đóng vai trò là "Người gác cổng" (Gatekeeper) chạy trước khi request chạm tới Controller. Luồng thực thi:
>
> 1. Trích xuất Header `Authorization: Bearer <Token>`.
> 2. Gọi `JwtUtil.validateToken()` kiểm tra chữ ký (Signature), hạn sử dụng (Expiration).
> 3. Trích xuất `email` từ Payload.
> 4. Gọi `CustomUserDetailsService.loadUserByUsername()` truy xuất Database để tạo đối tượng `CustomUserDetails` (mang theo Role/Authority mới nhất của User).
> 5. Khởi tạo `UsernamePasswordAuthenticationToken` và bơm (Inject) vào `SecurityContextHolder`.
>    Nhờ đó, tại bất kỳ dòng code nào trong Controller/Service, em đều có thể gọi `SecurityContextHolder.getContext().getAuthentication()` để biết ai đang thao tác."

#### Câu 5: "Nếu hệ thống có hàng triệu lượt truy cập, việc Filter liên tục query Database ở bước 4 có làm nghẽn cổ chai (Bottleneck) không?"

**Trả lời:**

> "Đúng, đây là tử huyệt của Stateless JWT nếu implement không khéo. Việc query DB ở mỗi request sẽ triệt tiêu ưu điểm Stateless của JWT.
>
> - **Tối ưu cấp 1 (Stateless thuần):** Lưu thẳng `roles` và `userId` vào trong Payload của JWT. Ở Filter, thay vì gọi DB, ta parse trực tiếp các trường này từ JWT để tạo `UserDetails` giả lập và đưa vào Context.
> - **Nhược điểm của Cấp 1:** Nếu Admin tước quyền của User (từ ADMIN xuống STUDENT), Token cũ vẫn chứa Payload là ADMIN cho đến khi hết hạn.
> - **Tối ưu cấp 2 (Hybrid với Redis):** Cache lại đối tượng `UserDetails` vào Redis với TTL bằng thời hạn của Access Token. Khi phân quyền thay đổi, ta xóa Cache. Filter sẽ đọc từ Redis (1-2ms) thay vì gọi SQL Database, vừa đảm bảo tốc độ cao, vừa đảm bảo tính Consistency của dữ liệu quyền hạn."

#### Câu 6: "Phân biệt HTTP 401 Unauthorized và HTTP 403 Forbidden. Trình bày cách bắt lỗi chúng trong Spring Security."

**Trả lời:**

> - **HTTP 401 (Unauthorized):** Lỗi danh tính. Xảy ra khi Request không có Token, Token hết hạn, hoặc Token giả mạo. Server thông báo: "Tôi không biết bạn là ai".
> - **HTTP 403 (Forbidden):** Lỗi thẩm quyền. Xảy ra khi Request có Token hợp lệ, Server biết user là ai, nhưng user đó KHÔNG ĐỦ QUYỀN (VD: Student cố truy cập API của Admin). Server thông báo: "Tôi biết bạn là ai, nhưng bạn không được phép vào đây".
> - **Cách xử lý:** Em implement 2 interface: `AuthenticationEntryPoint` để bắt lỗi 401, và `AccessDeniedHandler` để bắt lỗi 403. Thay vì trả về HTML Whitelabel báo lỗi mặc định, em cấu hình chúng trả về chuẩn `ApiResponse` JSON để Frontend dễ dàng xử lý (Ví dụ: văng ra trang Login nếu gặp 401, hiển thị Toast cảnh báo nếu gặp 403).

#### Câu 7: "Giải thích cơ chế hoạt động của Annotation @PreAuthorize("hasRole('ADMIN')")?"

**Trả lời:**

> "Nó hoạt động dựa trên cơ chế **AOP (Aspect-Oriented Programming)** và **Dynamic Proxy** của Spring:
> Khi khởi động, Spring tạo ra một lớp Proxy bọc lấy Controller thật. Khi Request đi vào, Proxy sẽ chặn (Intercept) lại trước.
>
> 1. Nó đọc SpEL (Spring Expression Language) `"hasRole('ADMIN')"`.
> 2. Nó móc vào `SecurityContextHolder` lấy ra list `GrantedAuthority` của User.
> 3. Mặc định `hasRole` sẽ tự động nối chuỗi `ROLE_` thành `ROLE_ADMIN` để so sánh với tập Authority.
> 4. Nếu khớp, Proxy cho phép Request chạy tiếp vào hàm thật. Nếu trượt, Proxy ném ra `AccessDeniedException` ngay lập tức để `GlobalExceptionHandler` hoặc `AccessDeniedHandler` xử lý thành mã lỗi HTTP 403."

#### Câu 8: "Vì sao không nên dùng Check-Then-Act cho việc phân quyền bằng code cứng trong Service?"

**Trả lời:**

> "Ví dụ code cứng trong Service: `if (!user.getRole().equals("ADMIN")) throw Exception;`
> Việc này vi phạm nguyên tắc **Separation of Concerns (SoC)**. Business Logic (tính toán, xử lý dữ liệu) bị trói buộc với Security Logic. Khi có yêu cầu thay đổi phân quyền (VD: thêm Role TEACHER cũng được phép), ta phải bới tung các class Service lên để sửa IF-ELSE, dễ gây lỗi hồi quy. Bằng cách dùng Spring Security (`SecurityConfig` hoặc `@PreAuthorize`), ta tách bạch tầng Security ra thành một tấm khiên (Shield) độc lập bảo vệ vòng ngoài, giúp code Service sạch sẽ và chuyên tâm vào nghiệp vụ lõi."

## Course/Lesson Database Foundation

### 1. Tóm tắt ngắn gọn

Task thiết kế cấu trúc Database cho tính năng Khóa học và Bài học bằng JPA/Hibernate. Cấu hình các quan hệ `@OneToMany`, `@ManyToOne`, sử dụng Enum và xử lý các lỗi thường gặp của Lombok khi mapping database.

### 2. Kiến thức phỏng vấn liên quan

Spring Data JPA, Hibernate Mapping, Lombok limitations, Database Constraints, Cascade, Orphan Removal.

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: Sự khác biệt giữa `@OneToMany` và `@ManyToOne` trong JPA là gì?

Trả lời:

- `@ManyToOne`: Nhiều entity hiện tại thuộc về 1 entity khác (Ví dụ: Nhiều Lesson thuộc về 1 Course). Đây thường là bên giữ khóa ngoại (Foreign Key).
- `@OneToMany`: 1 entity hiện tại chứa nhiều entity khác. Thường đi kèm với thuộc tính `mappedBy` để chỉ định quan hệ 2 chiều (Bidirectional) và không tạo thêm bảng trung gian.

#### Câu 2: Trong JPA, `cascade = CascadeType.ALL` có ý nghĩa gì?

Trả lời:
Nó thiết lập tính lan truyền các thao tác (Persist, Merge, Remove, Refresh, Detach) từ Entity cha sang Entity con. Ví dụ: Khi lưu 1 Course có chứa danh sách Sections, Hibernate sẽ tự động lưu luôn các Sections đó mà không cần gọi `sectionRepository.save()`.

#### Câu 3: Thuộc tính `orphanRemoval = true` khác gì với `CascadeType.REMOVE`?

Trả lời:

- `CascadeType.REMOVE`: Khi xóa entity cha, entity con bị xóa theo.
- `orphanRemoval = true`: Bao gồm cả `CascadeType.REMOVE`, NHƯNG có thêm tính năng: Nếu ta chỉ gỡ 1 entity con ra khỏi collection của entity cha (không xóa entity cha), Hibernate sẽ tự động xóa entity con đó dưới database vì nó đã trở thành "trẻ mồ côi".

#### Câu 4: Tại sao phải dùng `@ToString.Exclude` khi cấu hình quan hệ 2 chiều kết hợp với Lombok?

Trả lời:
Khi Lombok sinh ra hàm `toString()`, nó sẽ gọi `toString()` của các thuộc tính. Entity cha gọi Entity con, Entity con lại gọi lại Entity cha (do mapping 2 chiều), dẫn đến vòng lặp vô hạn (Infinite Recursion) và gây lỗi `StackOverflowError`. Việc exclude sẽ chặn vòng lặp này.

#### Câu 5: Làm sao để lưu Enum vào database dưới dạng chữ (String) thay vì số (Integer)?

Trả lời:
Dùng annotation `@Enumerated(EnumType.STRING)` đặt trên thuộc tính Enum. Nếu không khai báo, mặc định Hibernate sẽ lưu dưới dạng số (ORDINAL), rất dễ gây lỗi sai lệch dữ liệu nếu sau này ta đổi thứ tự các hằng số trong class Enum.

#### Câu 6: Làm thế nào để tạo 1 ràng buộc Unique (Unique Constraint) dựa trên 2 cột trở lên trong JPA?

Trả lời:
Sử dụng annotation `@Table(uniqueConstraints = { @UniqueConstraint(columnNames = {"col1", "col2"}) })` ở đầu class Entity. Ví dụ: Ràng buộc slug của bài học không được trùng trong cùng một khóa học.

#### Câu 7: `FetchType.LAZY` và `FetchType.EAGER` khác nhau như thế nào? Bạn thường dùng cái nào ở `@ManyToOne`?

Trả lời:

- `EAGER`: Tự động join và lấy dữ liệu của bảng liên kết ngay lập tức.
- `LAZY`: Chỉ truy vấn dữ liệu của bảng liên kết khi ta thực sự gọi hàm `get()` đến nó.
  Mặc định `@ManyToOne` là `EAGER`. Trong thực tế, nên đổi tất cả thành `LAZY` để tránh lỗi N+1 Query và tối ưu hiệu suất, chỉ fetch khi cần.

## Admin Course CRUD API

### 1. Tóm tắt ngắn gọn

Xây dựng hệ thống chức năng quản trị khóa học (CRUD) phân quyền đa cấp bậc (Admin/Teacher), bọc dữ liệu chuẩn hóa RESTful API, ứng dụng giải pháp chống N+1 Query trong JPA và áp dụng mô hình cô lập dữ liệu người dùng (Data Isolation).

### 2. Kiến thức phỏng vấn liên quan

Spring Security Context, JPA Fetching (`@EntityGraph`), Business Validation, Data Isolation Layer, Soft Delete vs Hard Delete.

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: Làm thế nào để bạn lấy được thông tin của User đang đăng nhập hiện tại trong Spring Boot?

Trả lời:
Ta có thể lấy thông tin User thông qua `SecurityContextHolder.getContext().getAuthentication()`. Từ đối tượng `Authentication` này, tùy vào cách cấu hình Custom UserDetails, ta có thể cast phần `getPrincipal()` về class User mong muốn để trích xuất `id` hoặc `username`.

#### Câu 2: Lỗi N+1 Query trong JPA/Hibernate là gì và bạn giải quyết nó như thế nào trong task này?

Trả lời:
Lỗi xảy ra khi ta truy vấn 1 danh sách gồm N phần tử thuộc thực thể Cha, nhưng cấu hình JPA nạp dữ liệu thực thể Liên kết (Con) là `LAZY`. Khi lặp qua danh sách để lấy thông tin thực thể Con, Hibernate sẽ kích hoạt thêm N câu lệnh SELECT riêng lẻ nữa (tổng cộng 1 + N câu lệnh). Trong task này, em xử lý bằng cách dùng annotation `@EntityGraph(attributePaths = {"teacher"})` trên phương thức của Repository để ép Hibernate thực hiện `LEFT JOIN` lấy luôn thông tin Teacher chỉ trong 1 câu lệnh SQL duy nhất.

#### Câu 3: Bạn hiểu như thế nào là "Data Isolation" (Cô lập dữ liệu) trong tầng nghiệp vụ của một hệ thống có nhiều Teacher?

Trả lời:
Data Isolation đảm bảo tài khoản Teacher A không thể vô tình hay cố ý sửa đổi hoặc xóa khóa học thuộc về quyền sở hữu của Teacher B thông qua việc thay đổi ID trên URL. Tại lớp Service, trước khi thực hiện logic chỉnh sửa/xóa, hệ thống bắt buộc phải truy vấn thực thể lên, so sánh `teacher_id` của thực thể đó với `id` của User đang đăng nhập. Nếu không trùng khớp (và user không phải Admin), hệ thống lập tức ném ra lỗi `ForbiddenException` (403).

#### Câu 4: Tại sao trong API xóa khóa học, bạn lại chọn Soft Delete (chuyển trạng thái sang ARCHIVED) thay vì Hard Delete (xóa bản ghi khỏi DB)?

Trả lời:
Khóa học là một thực thể trung tâm (Aggregate Root). Nếu dùng Hard Delete, khi khóa học đó đã có học viên đăng ký hoặc có lịch sử thanh toán, việc xóa bản ghi sẽ làm gãy các ràng buộc khóa ngoại (Foreign Key Constraints) hoặc làm mất dữ liệu báo cáo tài chính. Soft Delete giúp ẩn khóa học khỏi giao diện tìm kiếm của học viên nhưng giữ nguyên dữ liệu lịch sử hệ thống.

#### Câu 5: Sự khác biệt giữa việc đặt điều kiện kiểm tra dữ liệu bằng Annotation (như `@NotBlank`, `@Size`) trong DTO với việc kiểm tra bằng câu lệnh `if-else` trong Service là gì?

Trả lời:

- Dùng Annotation giúp tận dụng thư viện `Jakarta Validation`, kiểm tra dữ liệu ngay tại cửa ngõ Controller (tầng Web), ngăn chặn dữ liệu rác đi sâu vào tầng nghiệp vụ (Service), giúp code gọn gàng, dễ đọc.
- Kiểm tra bằng `if-else` trong Service thường dùng cho các logic nghiệp vụ phức tạp cần tương tác với Database (ví dụ: check trùng email, trùng slug).

#### Câu 6: Làm thế nào để bạn tự động tạo ra một chuỗi Slug (URL-friendly) từ tiêu đề tiếng Việt một cách chính xác?

Trả lời:
Em xây dựng một class tiện ích `SlugUtils`. Class này sử dụng kỹ thuật loại bỏ toàn bộ dấu tiếng Việt (bằng thư viện Normalizer hoặc Regex thay thế ký tự), chuyển toàn bộ chuỗi về chữ thường, loại bỏ các ký tự đặc biệt và thay thế khoảng trắng bằng dấu gạch ngang `-`.

#### Câu 7: Annotation `@PreAuthorize` hoạt động như thế nào trong Spring Security?

Trả lời:
`@PreAuthorize` hoạt động dựa trên cơ chế Spring AOP (Aspect-Oriented Programming). Khi một request gọi vào một hàm Controller có gắn annotation này, một Spring Proxy sẽ can thiệp trước khi hàm thực sự chạy. Nó sẽ thực thi biểu thức SpEL (Spring Expression Language) bên trong annotation (ví dụ: `hasRole('ADMIN')`) để kiểm tra quyền của danh sách `Authorities` trong SecurityContext. Nếu không thỏa mãn, nó chặn cuộc gọi và ném ra `AccessDeniedException`.

## Admin Section CRUD API

### 1. Tóm tắt ngắn gọn

Thiết kế và phát triển các API quản lý cấu trúc cây thư mục (Chương học) thuộc Khóa học, áp dụng kỹ thuật tính toán chỉ số sắp xếp tự động (Auto-increment Ordering), và xây dựng bộ quy tắc kiểm soát ràng buộc thực thể nghiệp vụ (Domain Constraint Rules).

### 2. Kiến thức phỏng vấn liên quan

Ràng buộc dữ liệu ở tầng ứng dụng (Application-level Constraint Validation), Quản lý thứ tự thực thể (Ordering Logic), Phân tích rủi ro Cascade Delete.

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: Tại sao ở API PUT và DELETE chương học, bạn không yêu cầu truyền `courseId` trên URL, nhưng API POST và GET danh sách thì lại cần?

Trả lời:
Đây là quy chuẩn thiết kế RESTful API dựa trên tính định danh của tài nguyên.

- Khi tạo mới (`POST`) hoặc lấy danh sách (`GET`), thực thể Chương học chưa tồn tại hoặc cần lọc theo phạm vi, do đó ta cần `{courseId}` để xác định nó thuộc về Khóa học nào.
- Khi cập nhật (`PUT`) hoặc xóa (`DELETE`), bản thân ID của Chương học (`{id}`) đã là duy nhất toàn hệ thống (Unique Primary Key). Từ ID này, hệ thống hoàn toàn có thể tự truy vấn ra Khóa học cha liên kết. Việc bắt truyền thêm `courseId` trên URL lúc này là dư thừa và làm tăng rủi ro không đồng nhất dữ liệu nếu Client truyền nhầm ID khóa học khác.

#### Câu 2: Giả sử hệ thống có lượng truy vấn đồng thời (Concurrency) rất cao khi tạo Section, việc dùng câu lệnh tìm `MAX(sort_order)` trong Java Service có thể gặp lỗi gì và cách giải quyết triệt để là gì?

Trả lời:
Nếu hai request tạo mới Section cho cùng một khóa học diễn ra cùng một mili-giây, cả hai câu lệnh SELECT `MAX(sort_order)` có thể trả về cùng một giá trị cũ, dẫn đến việc cả hai Section mới đều có cùng một chỉ số `sortOrder` sau khi cộng 1 (Hiện tượng Race Condition).

- Trong phạm vi MVP hiện tại của dự án LMS, tần suất tạo chương của một giáo viên là rất thấp nên logic này an toàn.
- Để giải quyết triệt để nếu hệ thống mở rộng, ta có thể áp dụng cơ chế **Pessimistic Locking** (Khóa bi quan) bằng `@Lock(LockModeType.PESSIMISTIC_WRITE)` khi select Max, hoặc đẩy logic tự tăng này xuống tầng Database xử lý bằng Trigger/Stored Procedure kết hợp Unique Constraint nhóm `(course_id, sort_order)`.

#### Câu 3: Khác biệt giữa việc đặt khóa ngoại `ON DELETE CASCADE` trong Database với việc viết code Java kiểm tra bài học trước khi xóa Section là gì?

Trả lời:

- `ON DELETE CASCADE` ở DB sẽ tự động quét sạch toàn bộ các Bài học, tài liệu liên quan nằm trong Section đó ngay khi Section bị xóa. Tiện lợi nhưng cực kỳ nguy hiểm nếu người dùng bấm nhầm, làm mất dữ liệu diện rộng và không thể cứu vãn.
- Viết code Java chủ động kiểm tra dữ liệu con trước giúp ta thực thi một **Quy tắc nghiệp vụ an toàn (Safety Business Rule)**. Hệ thống có cơ hội chặn lại, phản hồi lý do chính xác cho người dùng bằng thông báo lỗi trực quan (`SECTION_002`), giúp bảo vệ an toàn toàn vẹn dữ liệu cho hệ thống.

## Admin Lesson CRUD API & Multi-level Data Isolation

### 1. Tóm tắt ngắn gọn

Xây dựng lớp API CRUD quản lý thực thể lá (Bài học - Lesson) cuối cây quan hệ, giải quyết bài toán chống tấn công IDOR bằng kỹ thuật lội ngược dòng quan hệ thực thể xác minh quyền hạn sở hữu (Multi-level Data Ownership Verification) và tối ưu hóa hiệu năng truy vấn liên kết sâu.

### 2. Kiến thức phỏng vấn liên quan

Tấn công IDOR (Insecure Direct Object References), 3-Level Data Isolation Traversal, JPA Lazy Loading Optimization, Slug Scope Management.

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: Tấn công IDOR là gì? Và bạn đã phòng chống nó như thế nào trong bài toán tạo Bài học thuộc một Chương học?

Trả lời:
IDOR xảy ra khi một hệ thống cung cấp quyền truy cập trực tiếp vào các đối tượng dựa trên ID do người dùng cung cấp, nhưng thiếu bước kiểm tra xem người dùng đó có thực sự sở hữu đối tượng đó hay không.
Trong bài toán tạo Bài học, nếu chỉ kiểm tra xem Chương học (`sectionId`) có tồn tại hay không thì chưa đủ. Kẻ tấn công mang role `TEACHER` có thể lấy một `sectionId` của một giáo viên khác và gửi request tạo bài học vào đó. Em đã phòng chống bằng cách từ `sectionId` truyền lên, lội ngược dòng tìm ra `Course`, lấy ra `teacher_id` của khóa học đó và so sánh đối chiếu trực tiếp với ID của Giáo viên đang đăng nhập hệ thống trong `SecurityContextHolder`. Nếu không trùng, hệ thống lập tức ném lỗi 403 Forbidden.

#### Câu 2: Khi thực hiện logic kiểm tra lội ngược dòng `Lesson -> Section -> Course`, nếu không cẩn thận bạn sẽ làm sụt giảm hiệu năng hệ thống như thế nào? Cách bạn tối ưu là gì?

Trả lời:
Nếu sử dụng cơ chế nạp dữ liệu mặc định là `LAZY`, câu lệnh `sectionRepository.findById(id)` chỉ lấy dữ liệu bảng Section. Khi ta gọi `section.getCourse()`, Hibernate sẽ chạy thêm câu lệnh SELECT thứ 2 để lấy Course. Tiếp tục gọi `course.getTeacher()`, Hibernate lại chạy câu lệnh SELECT thứ 3. Việc này gây ra tình trạng lãng phí tài nguyên mạng và connection.
Em đã tối ưu bằng cách khai báo một phương thức custom có gắn `@EntityGraph(attributePaths = {"course", "course.teacher"})` trong Repository. Khi gọi hàm kiểm tra, Hibernate sẽ sinh duy nhất 1 câu lệnh SQL `LEFT OUTER JOIN` gom cả 3 bảng lại để xử lý, đưa số lượng câu lệnh truy vấn từ 3 về 1.

#### Câu 3: Trường `slug` của bài học có cần phải là duy nhất (Unique) trên toàn bộ Database hệ thống hay không? Tại sao?

Trả lời:
Không nhất thiết phải unique toàn bộ Database, mà chỉ cần unique trong **phạm vi của một Khóa học (Course Scope)**. Bởi vì cấu trúc URL hiển thị phía Học viên thường có dạng: `/courses/{course-slug}/sections/{section-id}/lessons/{lesson-slug}`. Việc ép unique toàn hệ thống sẽ gây khó khăn cho giáo viên khi đặt tên các bài học phổ thông (ví dụ: Bài học "Giới thiệu", "Bài tập 1"). Do đó, câu lệnh kiểm tra trùng slug trong Repository cần truyền kèm cả mã nhận diện khóa học để quét chính xác phạm vi.

## Student Course Public API & Data Masking

### 1. Tóm tắt ngắn gọn

Xây dựng API công khai cho phép người dùng khách (Guest) và học viên (Student) xem thông tin khóa học/bài học, kết hợp kỹ thuật Data Masking (che giấu dữ liệu) ở tầng Service để bảo vệ nội dung trả phí và tối ưu truy vấn nạp dữ liệu đa collection của Hibernate.

### 2. Kiến thức phỏng vấn liên quan

Spring Security `permitAll`, Data Masking (Bảo vệ dữ liệu nhạy cảm), Hibernate `MultipleBagFetchException`, Cartesian Product trong SQL, `Set` vs `List` trong JPA OneToMany, `@EntityGraph` optimization.

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: Làm sao để một API trong Spring Boot có thể truy cập public mà không cần token xác thực, đồng thời tránh xung đột với các API yêu cầu xác thực?

Trả lời:
Trong class `SecurityConfig`, ta định nghĩa luồng cho phép truy cập công khai bằng `requestMatchers`. Điểm mấu chốt là **thứ tự cấu hình**: Các rules cụ thể và public phải được đặt lên trước rule tổng quát chặn mọi request.
Ví dụ trong dự án: `.requestMatchers(HttpMethod.GET, "/api/v1/courses", "/api/v1/courses/**").permitAll()` được đặt trước `.anyRequest().authenticated()`. Spring Security xử lý filter chain theo thứ tự từ trên xuống, nếu khớp rule trên cùng, nó sẽ bỏ qua các rule bên dưới.

#### Câu 2: Trong API trả về danh sách bài học (`CourseDetailPublicRes`) cho người chưa mua khóa, làm sao bạn bảo vệ được link video (videoUrl) khỏi việc bị lộ qua payload API?

Trả lời:
Em áp dụng kỹ thuật **Data Masking** trực tiếp tại tầng Service trước khi trả DTO về Controller.
Trong `CoursePublicServiceImpl`, khi duyệt qua danh sách Bài học (Lesson) của một Khóa học (Course):

- Code sẽ kiểm tra cờ `isPreview`: `if (Boolean.TRUE.equals(lesson.getIsPreview()))`
- Nếu là bài học học thử (preview = true): DTO sẽ chứa đầy đủ `content` và `videoUrl`.
- Nếu không phải bài học thử: Code chủ động gán `lessonRes.setContent(null);` và `lessonRes.setVideoUrl(null);`.
  Cách tiếp cận này đảm bảo dữ liệu nhạy cảm không bao giờ rời khỏi server. Ngay cả khi người dùng dùng Postman hay DevTools F12 chặn bắt API response, họ cũng chỉ nhận được giá trị `null`, loại bỏ hoàn toàn khả năng bị trích xuất nội dung trái phép.

#### Câu 3: Bạn đã bao giờ gặp lỗi `MultipleBagFetchException` trong Hibernate chưa? Nguyên nhân cốt lõi là gì?

Trả lời:
Em đã xử lý lỗi này trong dự án. Nó xảy ra khi ta cố gắng dùng `FetchType.EAGER` hoặc `@EntityGraph` để nạp cùng lúc hai hoặc nhiều tập hợp (collection) kiểu `java.util.List` từ các quan hệ `@OneToMany`.
Nguyên nhân cốt lõi: Hibernate sử dụng khái niệm `Bag` cho `List` (một collection không có thứ tự và cho phép phần tử trùng lặp). Khi query nhiều `Bag` cùng lúc, SQL engine dưới DB sẽ sinh ra một **Cartesian Product (Tích Đề-các)** khổng lồ (VD: 1 Course x 10 Sections x 5 Lessons = 50 dòng kết quả chứa rất nhiều dữ liệu lặp). Khi map ngược kết quả SQL về lại Java Object, Hibernate không có cách nào an toàn để lọc chính xác các phần tử trùng lặp vào các `List` khác nhau mà không làm sai lệch dữ liệu, do đó nó chủ động ném ra `MultipleBagFetchException` để ép lập trình viên cấu trúc lại.

#### Câu 4: Trong dự án này, bạn khắc phục `MultipleBagFetchException` như thế nào?

Trả lời:
Em giải quyết bằng cách thay đổi kiểu dữ liệu collection trong Entity từ `java.util.List` sang `java.util.Set`, cụ thể là sử dụng `java.util.LinkedHashSet`.

- **Vì sao dùng Set?** `Set` có đặc tính toán học là không cho phép các phần tử trùng lặp. Khi dùng `Set`, Hibernate tự tin biết cách loại bỏ các dòng bị lặp từ kết quả Cartesian Product (bằng cách gọi `.equals()` và `.hashCode()` của Entity).
- **Vì sao dùng LinkedHashSet?** Nếu chỉ dùng `HashSet`, thứ tự các Section hay Lesson trả ra sẽ bị lộn xộn, hiển thị sai lên UI. `LinkedHashSet` vừa thỏa mãn điều kiện của `Set`, vừa bảo toàn được thứ tự chèn (insertion order), kết hợp với field `sortOrder` giúp data hiển thị đúng thứ tự bài học một cách hoàn hảo.

## Free Course Enrollment & Anti-Race Condition

### 1. Tóm tắt ngắn gọn

Triển khai API Ghi danh khóa học miễn phí, áp dụng thiết kế Fail-Fast để bảo vệ Database và thiết lập cấu trúc Composite Unique Key để triệt tiêu hoàn toàn rủi ro Race Condition trong môi trường đồng thời (concurrent environment).

### 2. Kiến thức phỏng vấn liên quan

Race Condition, Check-Then-Act flaw, Database Constraints, Composite Unique Key, Fail-Fast Principle, HTTP 409 Conflict.

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: Nguyên tắc "Fail-Fast" được thể hiện như thế nào trong hàm `enrollFreeCourse` của `CourseEnrollmentServiceImpl`?

Trả lời:
Nguyên tắc Fail-Fast ưu tiên việc kiểm tra (validate) điều kiện và ném lỗi (throw Exception) sớm nhất có thể để giải phóng luồng xử lý và tránh các bước truy xuất tốn kém.
Trong hàm `enrollFreeCourse`:

1. Đầu tiên, em kiểm tra logic nghiệp vụ thuần túy: Khóa học có ở trạng thái `PUBLISHED` không? Có phải là khóa `FREE` không? Việc kiểm tra này dùng dữ liệu ngay trên object `course` đã fetch trên RAM.
2. Nếu vi phạm, hệ thống ném `AppException` ngay lập tức mà không đi tiếp.
3. Chỉ khi qua các rào cản trên (chi phí rẻ), hệ thống mới gọi Database (chi phí đắt) để kiểm tra xem User đã ghi danh chưa (`existsByUserIdAndCourseId`).
   Thiết kế này giúp chặn đứng các request không hợp lệ từ sớm, tránh hao phí connection DB.

#### Câu 2: Giả sử một người dùng cố tình click nút "Ghi danh" 100 lần trong 1 giây bằng tool tự động, làm sao hệ thống của bạn đảm bảo không tạo ra 100 bản ghi Ghi danh trùng lặp?

Trả lời:
Đây là bài toán **Race Condition** kinh điển dạng _Check-Then-Act_. Nếu chỉ dùng câu lệnh `if (!existsByUserIdAndCourseId)` trong Java, khi 100 request đến cùng lúc, tất cả có thể đều lọt qua lệnh `if` do chưa có bản ghi nào kịp được insert.
Để giải quyết triệt để 100% ở tầng hệ thống, em áp dụng bảo vệ kép ở tầng Database (Data Integrity). Trên Entity `CourseEnrollment`, em định nghĩa:
`@Table(uniqueConstraints = { @UniqueConstraint(columnNames = {"user_id", "course_id"}) })`
Cấu hình này tạo ra một Composite Unique Index dưới DB. Dù Java có lọt bao nhiêu luồng đi chăng nữa, Database cũng chỉ insert thành công duy nhất luồng đầu tiên. Các luồng sau khi gọi lệnh `save()` sẽ bị DB văng ra lỗi `DataIntegrityViolationException`. Ở mức UI, chỉ một lần Ghi danh được ghi nhận thực sự.

#### Câu 3: Tại sao trong API tạo Ghi danh hoặc cập nhật tiến độ, bạn không nhận `userId` từ Frontend truyền lên?

Trả lời:
Truyền `userId` từ Frontend lên (qua Body, Params hoặc URL) là một lỗ hổng bảo mật nghiêm trọng thuộc loại IDOR (Insecure Direct Object References). Một hacker có thể sửa `userId = 5` thành `userId = 1` để đăng ký khóa học, thao tác dữ liệu hoặc xem tiến độ thay cho tài khoản Admin/tài khoản khác.
Trong dự án này, em lấy định danh một cách an toàn thông qua JWT. `SecurityContextHolder.getContext().getAuthentication().getName()` trả về email trích xuất từ ruột của Token đã được filter xác thực. JWT đã bị mã hóa chữ ký (Signature), nên Frontend không thể giả mạo.

## Student Lesson Learning, Upsert & Anti-Downgrade Algorithm

### 1. Tóm tắt ngắn gọn

Triển khai module Học tập (Learning), xây dựng rào chắn nội dung bảo mật dựa trên kết quả Ghi danh, kết hợp mô hình cập nhật tiến độ (Progress) an toàn dùng nguyên lý Upsert và Anti-Downgrade logic.

### 2. Kiến thức phỏng vấn liên quan

Single Responsibility Principle (SRP) trong thiết kế Service, Upsert Pattern, Thuật toán High-water mark (Anti-downgrade), Authorization verification vs Authentication.

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: Sự khác biệt giữa việc lấy danh sách bài học qua `CoursePublicService` và việc lấy chi tiết bài học qua `LearningService` là gì?

Trả lời:

- `CoursePublicService` là API công khai phục vụ mục đích "Trưng bày" (Showcase). Nó trả về khung sườn của khóa học, danh sách tên bài học nhưng **ẩn (masking)** các nội dung nhạy cảm của các bài học không cho học thử.
- `LearningService` là API bảo mật (yêu cầu role STUDENT) phục vụ mục đích "Học thực tế". Trong service này, khi lấy chi tiết bài học (`getLessonDetail`), em triển khai một rào chắn logic ngặt nghèo: Nếu bài học không phải `isPreview`, em bắt buộc query vào `CourseEnrollmentRepository` để kiểm tra User hiện tại có sở hữu khóa học này không. Nếu không, lập tức ném lỗi `FORBIDDEN_ACCESS`.

#### Câu 2: Thuật toán "Upsert" là gì và bạn ứng dụng nó vào tính năng Lưu Tiến Độ (`updateProgress`) như thế nào?

Trả lời:
Upsert là thao tác gộp giữa Update và Insert (Cập nhật nếu đã có, Thêm mới nếu chưa có).
Trong Spring Data JPA, em hiện thực Upsert bằng cách:

1. Dùng hàm `findByUserIdAndLessonId()` để tìm kiếm bản ghi `LessonProgress`.
2. Dùng `.orElse()` để khởi tạo một Object `LessonProgress` hoàn toàn mới (thuộc tính percent = 0) nếu không tìm thấy.
3. Thay đổi các thuộc tính trên Object đó (percent mới, trạng thái hoàn thành).
4. Gọi `repository.save()`. Nếu là Object lấy từ DB, JPA tự hiểu là UPDATE. Nếu là Object mới do `.orElse()` sinh ra, JPA hiểu là INSERT.

#### Câu 3: Khi học viên đang học video tới 80%, sau đó họ tua lùi lại mức 20% và API cập nhật tiến độ tự động bắn lên server. Làm sao để tiến độ tổng của họ không bị sụt giảm từ 80% về 20%?

Trả lời:
Em sử dụng thuật toán **Anti-Downgrade** (hay còn gọi là High-water mark - giữ lại mức nước cao nhất).
Trong `LearningServiceImpl`, trước khi gán giá trị phần trăm mới, em đặt một lệnh kiểm tra:
`if (req.getWatchedPercent() != null && req.getWatchedPercent() > progress.getWatchedPercent())`
Chỉ khi giá trị Client gửi lên thực sự **lớn hơn** giá trị cao nhất đã được lưu trong DB, em mới gọi lệnh `setWatchedPercent()`. Điều này đảm bảo dữ liệu tiến độ của học viên chỉ có thể tăng lên hoặc đứng im, không bao giờ bị ghi lùi (downgrade) dù họ có tua lại để xem.

## System Security & Data Isolation (Phân lập dữ liệu)

### 1. Tóm tắt ngắn gọn

Triển khai cơ chế phân lập dữ liệu (Data Isolation) để phân quyền thao tác cho từng giảng viên (Teacher) đối với hệ thống, đảm bảo Giảng viên A không thể chỉnh sửa khóa học của Giảng viên B.

### 2. Kiến thức phỏng vấn liên quan

Data Isolation, Role-Based Access Control (RBAC), Authentication Context, IDOR prevention.

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: Làm sao để đảm bảo Giảng viên A (Teacher A) không thể xóa Bài học của Giảng viên B (Teacher B)?

Trả lời:
Trong `LessonAdminServiceImpl`, tất cả các hàm CRUD (Create, Update, Delete) đều gọi qua một hàm kiểm tra chung là `checkDataIsolation(Course course)`.
Hàm này lấy thông tin User hiện tại từ `SecurityContextHolder`. Nếu User mang role `TEACHER` (không phải ADMIN), hàm sẽ đối chiếu email của User hiện tại với `email` của người tạo khóa học (`course.getTeacher().getEmail()`).
Nếu hai email không khớp, hệ thống chủ động ném ngoại lệ `DATA_ISOLATION_FORBIDDEN`. Cách thiết kế này tạo ra một rào chắn kiên cố, hoàn toàn chống lại lỗ hổng IDOR, khi mà một giảng viên có thể cố tình gọi API xóa với một `id` bài học không thuộc quyền sở hữu của mình.

## Student Dashboard, Data Aggregation & Anti-IDOR

### 1. Tóm tắt ngắn gọn

Triển khai API tổng hợp dữ liệu (Data Aggregation) cho màn hình Student Dashboard, loại bỏ hoàn toàn ID người dùng khỏi Endpoint để ngăn chặn lỗ hổng IDOR.

### 2. Kiến thức phỏng vấn liên quan

BFF (Backend For Frontend), Data Aggregation, IDOR (Insecure Direct Object Reference), JWT Security Context.

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: Tại sao màn hình Dashboard nên dùng 1 API tổng hợp trả về toàn bộ dữ liệu thay vì Frontend tự gọi 3-4 API lẻ rồi tự ghép lại?

Trả lời:
Đây là việc áp dụng mô hình BFF (Backend For Frontend) / Data Aggregation. Việc gom dữ liệu ở Backend mang lại 3 lợi ích:

1. Giảm thiểu số lượng HTTP Request từ Client lên Server (tránh tình trạng Waterfall requests).
2. Backend có thể query trực tiếp vào Database, JOIN các bảng ở mức độ hệ thống nội bộ với tốc độ cực nhanh, thay vì truyền dữ liệu qua lại trên đường truyền mạng Internet.
3. Đồng nhất logic tính toán cho mọi nền tảng Client (Web, Android, iOS).

#### Câu 2: Lỗ hổng IDOR là gì và bạn phòng chống nó như thế nào trong các API lấy thông tin cá nhân?

Trả lời:
IDOR (Insecure Direct Object Reference) là lỗ hổng xảy ra khi hệ thống cho phép truy cập dữ liệu thông qua ID truyền trên URL hoặc Body (ví dụ: `/api/users/5/courses`) mà không kiểm tra quyền. Hacker có thể đổi số `5` thành `6` để xem trộm dữ liệu người khác.
Để phòng chống, em thiết kế endpoint là `/api/users/me/courses`. Chữ `me` mang ý nghĩa là user hiện tại. Backend sẽ lấy Token JWT từ Header, giải mã để lấy `userId` trực tiếp từ `SecurityContextHolder`. Do Token đã được mã hóa bằng Secret Key của Server, hacker không thể tự tạo hay sửa đổi Token để giả mạo người khác.

## Frontend Foundation (Vue 3, Vite, Axios, Pinia, Router)

### 1. Tóm tắt ngắn gọn

Thiết lập nền tảng frontend cho dự án: cấu trúc thư mục, route, store, API client và môi trường phát triển.

### 2. Kiến thức phỏng vấn liên quan

Vue 3, Vite, SPA routing, Pinia, Axios interceptor, environment variables.

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: Vì sao nên dùng Pinia thay cho Vuex trong Vue 3?

Trả lời:
Pinia nhẹ hơn, dễ dùng hơn với Composition API, và cú pháp đơn giản hơn. Nó cũng tích hợp tốt với TypeScript và giúp state management rõ ràng hơn.

#### Câu 2: Axios interceptor dùng để làm gì?

Trả lời:
Interceptor dùng để gắn access token vào request, bắt lỗi 401, và xử lý refresh/logout ở một nơi tập trung, tránh lặp code ở từng API call.

#### Câu 3: Tại sao cần dùng environment variables cho URL API?

Trả lời:
Để dễ đổi giữa môi trường dev và production, tránh hard-code URL, và giảm rủi ro khi deploy.

## Vue 3 Foundation, Vite Proxy & Axios Interceptors

### 1. Tóm tắt ngắn gọn
Khởi tạo dự án Vue 3 với Vite, thiết lập Pinia để quản lý State, cấu hình Router với các Layout đa tầng và cấu hình Axios Interceptors để tự động chèn token và xử lý luồng refresh token tĩnh (silent refresh).

### 2. Kiến thức phỏng vấn liên quan
CORS, Vite Proxy, Axios Interceptors, JWT Handling in Frontend, Vue Router Navigation Guards.

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: Tại sao bạn lại cấu hình Proxy trong Vite thay vì cấu hình CORS trên Backend Spring Boot ở môi trường Development?
Trả lời:
Cấu hình CORS trên Backend đôi khi dẫn đến rủi ro bảo mật nếu vô tình đẩy cấu hình `allowedOrigins("*")` lên production. Việc sử dụng Vite Proxy giúp trình duyệt hiểu rằng Frontend và Backend đang chạy trên cùng một domain (localhost), từ đó "đánh lừa" trình duyệt và vượt qua lỗi CORS một cách an toàn mà không cần thay đổi bất kỳ code nào ở Backend.

#### Câu 2: Trong Frontend, bạn xử lý luồng cấp lại Token (Refresh Token) như thế nào để người dùng không bị văng ra trang Login khi đang thao tác?
Trả lời:
Em sử dụng Axios Interceptor (hàm chặn request/response). Tại `response interceptor`, nếu Backend trả về mã lỗi 401 (Unauthorized), em sẽ:
1. Đóng băng request hiện tại.
2. Gọi ngầm API `/api/auth/refresh` bằng Refresh Token lưu trong LocalStorage/Cookies.
3. Nếu lấy được Access Token mới, em cập nhật vào Pinia Store, thay thế header cũ và thực hiện lại (retry) request vừa bị đóng băng. 
Luồng này diễn ra hoàn toàn tĩnh (silent), người dùng sẽ không hề hay biết Token của họ vừa được làm mới.

## Frontend Authentication & Navigation Guards

### 1. Tóm tắt ngắn gọn
Xây dựng giao diện Đăng nhập/Đăng ký sử dụng Vue 3, thực hiện validate form ở phía Client, tích hợp RESTful API với Axios và bảo vệ các route nội bộ bằng Vue Router Guards.

### 2. Kiến thức phỏng vấn liên quan
Client-side Validation vs Server-side Validation, Vue Router Navigation Guards, JWT Storage (LocalStorage vs HttpOnly Cookies), XSS (Cross-Site Scripting).

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: Tại sao phải làm Validation ở Frontend (Client-side) trong khi Backend đã validate rất chặt chẽ rồi?
Trả lời: 
Làm validation ở Client-side chủ yếu để tối ưu hóa Trải nghiệm người dùng (UX) và tiết kiệm tài nguyên Server. Khi validate ở Frontend, người dùng nhận được phản hồi ngay lập tức (ví dụ: sai định dạng email, mật khẩu quá ngắn) mà không cần chờ dữ liệu gửi qua mạng. Việc này giúp giảm thiểu các request rác không hợp lệ (Bad Request) bắn lên Server, giúp hệ thống hoạt động hiệu quả hơn. Tuy nhiên, Client-side validation có thể bị bypass (vượt qua), nên Server-side validation vẫn là lớp bảo mật bắt buộc cuối cùng.

#### Câu 2: Navigation Guards trong Vue Router hoạt động như thế nào để bảo vệ ứng dụng?
Trả lời:
Navigation Guards giống như các trạm kiểm soát (checkpoints) trước khi ứng dụng chuyển từ trang này sang trang khác. Em sử dụng `beforeEach` guard để kiểm tra xem một route có yêu cầu xác thực (`requiresAuth`) hay không. Nếu có, guard sẽ kiểm tra xem Token đã tồn tại trong Pinia Store (hoặc LocalStorage) chưa. Nếu chưa có Token, hệ thống sẽ chặn hành động điều hướng và dùng lệnh `router.push()` để đẩy người dùng về trang `/login`. Nó cũng giúp ngăn người dùng đã login truy cập lại vào trang Đăng nhập bằng cách đẩy thẳng họ vào Dashboard.

## Tối ưu thời gian tải trang với Promise.all & Quản lý trạng thái UI

### 1. Tóm tắt ngắn gọn
Tích hợp API thống kê và danh sách khóa học vào Student Dashboard, sử dụng `Promise.all` để fetch dữ liệu song song và quản lý chi tiết các trạng thái hiển thị (Loading, Empty, Data, Error).

### 2. Kiến thức phỏng vấn liên quan
Concurrent API Fetching, Skeleton Loading UI, Vue Component Lifecycle (`onMounted`), Empty States.

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: Khi trang Dashboard cần gọi 2 API riêng biệt (lấy tiến độ học và lấy danh sách khóa học), bạn sẽ gọi chúng như thế nào để tối ưu hiệu năng?
Trả lời:
Em sử dụng `Promise.all()` để gọi cả 2 API song song thay vì gọi tuần tự (dùng `await` liên tiếp). Nếu API 1 mất 1s, API 2 mất 1.5s, việc gọi tuần tự sẽ tốn tổng cộng 2.5s. Với `Promise.all`, tổng thời gian chờ chỉ bằng thời gian của request lâu nhất là 1.5s. Điều này giúp tối ưu đáng kể tốc độ render lần đầu (First Paint) cho ứng dụng SPA.

#### Câu 2: Trải nghiệm người dùng (UX) sẽ bị ảnh hưởng thế nào nếu ta không xử lý trạng thái Loading và Empty State khi fetch data?
Trả lời:
Nếu không có Loading State, khi mạng chậm, màn hình sẽ trắng tinh hoặc hiển thị vỡ layout trong vài giây khiến người dùng tưởng web bị lỗi. Skeleton Loading hoặc Spinner giúp thông báo trực quan rằng hệ thống đang xử lý.
Nếu không có Empty State (khi mảng dữ liệu rỗng), UI sẽ hiển thị một khoảng trống khó hiểu. Việc có Empty State (ví dụ: "Bạn chưa có khóa học nào, hãy khám phá ngay") đóng vai trò điều hướng và giữ chân người dùng (Call-to-Action) rất hiệu quả.

## Admin Dashboard UI & API Integration

### 1. Tóm tắt ngắn gọn

Xây dựng màn hình Admin Dashboard bằng Vue 3, tách layout admin riêng, bảo vệ route bằng role `ADMIN`/`SUPER_ADMIN`, gọi dữ liệu qua `admin.service.js` và tạm dùng mock data khi backend dashboard API chưa có thật.

### 2. Kiến thức phỏng vấn liên quan

Vue Router Guard, Role-Based Access Control, Pinia auth store, service layer trong frontend, loading/error/empty state, mock API, dashboard data aggregation.

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: Vue Router Guard dùng để làm gì trong task Admin Dashboard?
Trả lời:
Router guard dùng để kiểm tra trước khi chuyển trang. Với `/admin/dashboard`, guard kiểm tra user đã đăng nhập chưa và có role `ADMIN` hoặc `SUPER_ADMIN` không. Nếu không đạt điều kiện thì điều hướng sang trang phù hợp.

#### Câu 2: Vì sao không nên chỉ dựa vào frontend route guard để bảo mật API admin?
Trả lời:
Vì frontend có thể bị bypass bằng Postman, DevTools hoặc gọi API trực tiếp. Route guard chỉ cải thiện trải nghiệm người dùng; backend vẫn phải dùng Spring Security như `@PreAuthorize` hoặc rule `/api/v1/admin/**` để chặn thật.

#### Câu 3: Tại sao role trong frontend nên xử lý như một mảng thay vì một chuỗi?
Trả lời:
Một user có thể có nhiều quyền, ví dụ `["ADMIN", "CONTENT_EDITOR"]`. Dùng mảng và kiểm tra bằng `includes()` giúp hệ thống linh hoạt hơn và tránh lỗi khi backend trả nhiều role.

#### Câu 4: Tại sao nên tách `admin.service.js` thay vì gọi Axios trực tiếp trong `AdminDashboardPage.vue`?
Trả lời:
Tách service giúp page chỉ tập trung render UI và quản lý state. Logic gọi API nằm riêng nên dễ tái sử dụng, dễ sửa endpoint, dễ mock data và dễ test hơn.

#### Câu 5: Mock data trong frontend có lợi ích gì khi backend chưa xong?
Trả lời:
Mock data giúp frontend vẫn hoàn thiện layout, component và trạng thái hiển thị mà không bị chờ backend. Tuy nhiên mock chỉ là tạm thời, sau đó phải có task backend để thay bằng API thật.

#### Câu 6: Loading, error và empty state khác nhau như thế nào?
Trả lời:
Loading hiển thị khi đang gọi API. Error hiển thị khi API lỗi hoặc user không có quyền. Empty state hiển thị khi gọi API thành công nhưng dữ liệu rỗng, ví dụ chưa có user mới hoặc khóa học mới.

#### Câu 7: Admin Dashboard thường nên dùng một API tổng hợp hay nhiều API nhỏ?
Trả lời:
Nên dùng một API tổng hợp cho màn dashboard, ví dụ `GET /api/v1/admin/dashboard`, vì frontend chỉ cần một request để lấy các chỉ số và danh sách mới nhất. Backend sẽ chịu trách nhiệm query nhiều bảng và đóng gói thành DTO.

#### Câu 8: Khi đăng nhập xong, frontend nên điều hướng theo role như thế nào?
Trả lời:
Sau khi login thành công và lấy profile user, frontend đọc `user.roles`. Nếu có `ADMIN` hoặc `SUPER_ADMIN` thì chuyển đến `/admin/dashboard`; nếu là `STUDENT` thì chuyển đến `/student/dashboard`.

## Backend Admin Dashboard API

### 1. Tóm tắt ngắn gọn

Triển khai API `GET /api/v1/admin/dashboard` trong Spring Boot để trả dữ liệu tổng quan cho admin, gồm các chỉ số count và danh sách user/course mới gần đây. API dùng DTO response, service layer riêng và phân quyền bằng `@PreAuthorize`.

### 2. Kiến thức phỏng vấn liên quan

Spring Boot REST API, Controller-Service-Repository, DTO response, Spring Security method-level authorization, Spring Data JPA derived query, `@EntityGraph`, dashboard data aggregation.

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: Vì sao API dashboard nên trả DTO thay vì trả Entity trực tiếp?
Trả lời:
DTO giúp kiểm soát field trả về, tránh lộ dữ liệu nhạy cảm như `passwordHash`, đồng thời giúp response ổn định hơn nếu Entity thay đổi.

#### Câu 2: Controller trong task này nên làm gì và không nên làm gì?
Trả lời:
Controller chỉ nhận request, kiểm tra quyền qua annotation và trả `ApiResponse`. Logic count, query recent data và map DTO phải nằm ở Service.

#### Câu 3: `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")` dùng để làm gì?
Trả lời:
Annotation này yêu cầu user hiện tại phải có role `ADMIN` hoặc `SUPER_ADMIN` mới được gọi API. Nếu không có quyền, Spring Security sẽ trả 403.

#### Câu 4: Spring Data JPA method `findTop5ByOrderByCreatedAtDesc()` hoạt động như thế nào?
Trả lời:
Spring Data JPA đọc tên method để tự sinh query: lấy tối đa 5 bản ghi và sắp xếp theo `createdAt` giảm dần.

#### Câu 5: `@EntityGraph(attributePaths = {"roles"})` có tác dụng gì khi lấy recent users?
Trả lời:
Nó yêu cầu JPA fetch sẵn quan hệ `roles` cùng user. Nhờ vậy khi map DTO không bị lazy loading bất ngờ và giảm nguy cơ N+1 query.

#### Câu 6: Dashboard API là dạng Data Aggregation như thế nào?
Trả lời:
Backend gom dữ liệu từ nhiều nguồn như user, course, lesson, enrollment rồi trả về một response duy nhất cho frontend. Frontend không cần gọi nhiều API nhỏ.

#### Câu 7: Khi user có nhiều role, tại sao cần xác định primary role?
Trả lời:
Dashboard chỉ cần hiển thị một role chính cho dễ đọc. Service chọn role theo thứ tự ưu tiên như `SUPER_ADMIN`, `ADMIN`, `TEACHER`, `CONTENT_EDITOR`, `STUDENT`.

#### Câu 8: Test bảo mật quan trọng nhất cho API admin dashboard là gì?
Trả lời:
Cần test 3 case: không có token thì 401, token `STUDENT` thì 403, token `ADMIN` hoặc `SUPER_ADMIN` thì gọi thành công.

## Backend Admin User Management API

### 1. Tóm tắt ngắn gọn

Triển khai nhóm API quản lý user cho admin bằng Spring Boot: danh sách user có phân trang/tìm kiếm/lọc, xem chi tiết user, khóa user và mở khóa user. API trả DTO an toàn, fetch roles hợp lý và được bảo vệ bằng `@PreAuthorize`.

### 2. Kiến thức phỏng vấn liên quan

Spring Boot REST API, DTO, Spring Security, `@PreAuthorize`, Spring Data JPA, JPQL, pagination, filtering, ManyToMany roles, account locking.

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: Vì sao API admin user không được trả trực tiếp Entity `User`?
Trả lời:
Vì Entity `User` có field nhạy cảm như `passwordHash`. Trả DTO giúp kiểm soát dữ liệu response và tránh lộ thông tin nội bộ.

#### Câu 2: Phân trang trong API danh sách user có tác dụng gì?
Trả lời:
Phân trang giúp server không trả quá nhiều user trong một lần gọi, giảm tải database, giảm dung lượng response và giúp frontend hiển thị bảng dữ liệu dễ hơn.

#### Câu 3: `Pageable` trong Spring Data JPA dùng để làm gì?
Trả lời:
`Pageable` chứa thông tin `page`, `size`, `sort`. Repository dùng nó để query đúng trang dữ liệu và trả về `Page<T>` có metadata như tổng số phần tử và tổng số trang.

#### Câu 4: Query `(:keyword IS NULL OR ... LIKE ...)` có ý nghĩa gì?
Trả lời:
Đây là cách viết filter động. Nếu `keyword` không truyền lên thì điều kiện đó được bỏ qua; nếu có keyword thì query lọc theo `fullName` hoặc `email`.

#### Câu 5: Vì sao lock user nên đổi status thành `LOCKED` thay vì xóa user?
Trả lời:
Đổi status giúp giữ lại dữ liệu lịch sử, enrollment, progress và audit. Xóa user có thể làm mất dữ liệu liên quan và gây lỗi quan hệ database.

#### Câu 6: Vì sao không cho admin tự khóa tài khoản của chính mình?
Trả lời:
Nếu admin tự khóa mình, họ có thể mất quyền truy cập hệ thống và cần can thiệp database để sửa. Đây là rule bảo vệ vận hành.

#### Câu 7: Vì sao không cho khóa tài khoản `SUPER_ADMIN` trong task này?
Trả lời:
`SUPER_ADMIN` là quyền cao nhất. Nếu admin thường khóa được `SUPER_ADMIN` thì hệ thống có rủi ro mất quyền quản trị cao nhất hoặc bị lạm quyền.

#### Câu 8: `@EntityGraph(attributePaths = {"roles"})` giúp gì khi list user?
Trả lời:
Nó fetch sẵn roles cùng user, giúp map DTO không phát sinh nhiều query nhỏ và giảm nguy cơ N+1 query.

#### Câu 9: Test security cơ bản cho API admin user gồm những case nào?
Trả lời:
Không có token phải bị 401, token `STUDENT` phải bị 403, token `ADMIN` hoặc `SUPER_ADMIN` mới gọi được API.
