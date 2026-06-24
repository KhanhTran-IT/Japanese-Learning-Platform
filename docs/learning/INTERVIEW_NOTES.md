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

## Login API + JWT Token Generation

### 1. Tóm tắt ngắn gọn

Login API dùng để xác thực user bằng email/password. Nếu thông tin hợp lệ, backend tạo access token để user gọi các API cần đăng nhập, đồng thời tạo refresh token để lấy access token mới khi access token hết hạn. Refresh token được lưu vào database để hệ thống có thể revoke khi logout hoặc khi cần vô hiệu hóa token.

### 2. Kiến thức phỏng vấn liên quan

* Authentication là quá trình xác minh user là ai.
* Authorization là quá trình kiểm tra user có quyền làm gì.
* BCrypt dùng để hash và verify password.
* JWT là token có thể chứa thông tin user và thời gian hết hạn.
* Access token nên ngắn hạn.
* Refresh token nên dài hạn và có thể revoke.
* DTO giúp response an toàn hơn, không lộ Entity.
* Không nên trả thông báo quá chi tiết khi login sai để tránh lộ thông tin tài khoản.

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: Login API hoạt động như thế nào?

Trả lời:
Frontend gửi email và password lên backend. Backend tìm user theo email, kiểm tra password bằng BCrypt, kiểm tra trạng thái tài khoản, sau đó tạo access token và refresh token. Cuối cùng backend trả token và thông tin user an toàn về frontend.

#### Câu 2: Vì sao không so sánh password trực tiếp với passwordHash?

Trả lời:
Vì password trong database đã được hash bằng BCrypt. Mỗi lần hash có thể sinh ra chuỗi khác nhau do salt, nên phải dùng `passwordEncoder.matches()` để kiểm tra raw password với passwordHash.

#### Câu 3: Access token là gì?

Trả lời:
Access token là token ngắn hạn dùng để xác thực các request sau khi user đăng nhập. Frontend gửi token này trong header `Authorization: Bearer <token>`.

#### Câu 4: Refresh token là gì?

Trả lời:
Refresh token là token dài hạn dùng để xin access token mới khi access token hết hạn. Refresh token thường được lưu database để có thể revoke khi logout hoặc khi phát hiện rủi ro bảo mật.

#### Câu 5: Vì sao cần cả access token và refresh token?

Trả lời:
Access token sống ngắn giúp bảo mật tốt hơn. Refresh token sống dài giúp user không phải đăng nhập lại liên tục. Kết hợp cả hai giúp cân bằng bảo mật và trải nghiệm người dùng.

#### Câu 6: JWT gồm những phần nào?

Trả lời:
JWT gồm 3 phần: header, payload và signature. Header mô tả thuật toán, payload chứa claims, signature dùng để kiểm tra token có bị sửa đổi hay không.

#### Câu 7: Có nên lưu access token vào database không?

Trả lời:
Thông thường không cần. Access token thường stateless, backend verify bằng secret key hoặc public key. Nhưng refresh token nên lưu database để hỗ trợ revoke.

#### Câu 8: Vì sao không trả User entity trực tiếp trong LoginResponse?

Trả lời:
User entity có thể chứa dữ liệu nhạy cảm như passwordHash, status nội bộ hoặc quan hệ database phức tạp. Dùng DTO giúp chỉ trả những field cần thiết và an toàn.

#### Câu 9: Khi login sai email hoặc password, nên trả lỗi thế nào?

Trả lời:
Nên trả lỗi chung như “Email hoặc mật khẩu không đúng”, không nên nói rõ email không tồn tại hay password sai để tránh lộ thông tin tài khoản.

#### Câu 10: Refresh token được dùng trong task tiếp theo như thế nào?

Trả lời:
Task tiếp theo sẽ tạo API refresh-token. Frontend gửi refresh token lên backend, backend kiểm tra token hợp lệ, chưa hết hạn, chưa revoked và tồn tại trong database. Nếu hợp lệ, backend cấp access token mới.

## Refresh Token API + Logout API

### 1. Tóm tắt ngắn gọn

Refresh Token API cho phép backend cấp access token mới khi access token cũ hết hạn. Logout API dùng để revoke refresh token, giúp user kết thúc phiên đăng nhập. Trong project này, refresh token được lưu database nên backend có thể kiểm tra token còn hợp lệ, đã hết hạn hay đã bị thu hồi.

### 2. Kiến thức phỏng vấn liên quan

* Access token thường sống ngắn.
* Refresh token thường sống dài hơn.
* Refresh token nên lưu database để hỗ trợ revoke.
* Logout trong JWT system thường xử lý bằng cách revoke refresh token.
* Nếu refresh token đã revoked, backend không được cấp access token mới.
* Không nên chỉ kiểm tra chữ ký JWT mà bỏ qua database refresh token.
* Error code rõ ràng giúp frontend biết khi nào cần login lại.

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: Refresh token dùng để làm gì?

Trả lời:
Refresh token dùng để lấy access token mới khi access token hết hạn, giúp user không cần đăng nhập lại liên tục.

#### Câu 2: Vì sao refresh token nên lưu database?

Trả lời:
Vì lưu database giúp backend có thể revoke token khi user logout, đổi mật khẩu hoặc khi phát hiện rủi ro bảo mật.

#### Câu 3: Logout hoạt động thế nào trong hệ thống JWT?

Trả lời:
Với JWT stateless, access token thường vẫn hợp lệ đến khi hết hạn. Logout thường sẽ revoke refresh token để user không thể xin access token mới nữa.

#### Câu 4: Trường `revoked` trong refresh token dùng để làm gì?

Trả lời:
`revoked` đánh dấu token đã bị thu hồi. Nếu `revoked = true`, backend phải từ chối refresh token đó.

#### Câu 5: Khi refresh token hết hạn thì backend nên xử lý thế nào?

Trả lời:
Backend trả lỗi refresh token expired, ví dụ `AUTH_007`, và frontend nên yêu cầu user đăng nhập lại.

#### Câu 6: Nếu refresh token không tồn tại trong database thì sao?

Trả lời:
Backend nên xem token đó là không hợp lệ và trả lỗi `AUTH_006`.

#### Câu 7: Sau logout, dùng lại refresh token cũ thì chuyện gì xảy ra?

Trả lời:
Backend kiểm tra thấy token đã revoked và trả lỗi `AUTH_008`, không cấp access token mới.

#### Câu 8: Có nên tạo refresh token mới mỗi lần gọi refresh-token không?

Trả lời:
Đó là refresh token rotation. Nó bảo mật hơn nhưng phức tạp hơn. Với MVP có thể chưa cần, chỉ cần cấp access token mới và giữ refresh token cũ đến khi hết hạn hoặc logout.

#### Câu 9: Vì sao không chỉ dựa vào JWT signature để validate refresh token?

Trả lời:
Vì token có chữ ký hợp lệ vẫn có thể đã bị logout/revoked. Do đó cần kiểm tra thêm trong database.

#### Câu 10: Task tiếp theo sau refresh/logout là gì?

Trả lời:
Task tiếp theo là dùng access token để xác thực request, sau đó tạo API `GET /api/users/me` để lấy thông tin user hiện tại.

---

# INTERVIEW_NOTES - Access Token Authentication + GET /api/users/me

## Access Token Authentication + GET /api/users/me

### 1. Tóm tắt ngắn gọn

Task này xây dựng cơ chế để backend xác thực request bằng JWT access token. Sau khi user login và nhận access token, frontend gửi token trong header `Authorization: Bearer <token>`. Backend dùng `JwtAuthenticationFilter` để đọc token, validate token, load user từ database và set authentication vào `SecurityContextHolder`.

Sau đó API `GET /api/users/me` dùng authentication hiện tại để trả thông tin user đang đăng nhập.

### 2. Vì sao task này quan trọng?

Trước task này, hệ thống mới chỉ tạo được token. Sau task này, backend mới thật sự **dùng token để bảo vệ API**.

Nếu không có task này:

* Access token tạo ra nhưng chưa được dùng.
* Backend không biết request hiện tại là của user nào.
* Không thể làm `/api/users/me`.
* Không thể phân quyền Admin/Student.
* Không thể bảo vệ các API như học bài, mua khóa học, quản lý admin.

### 3. Luồng xử lý chính

```text
Client gọi API protected
→ Gửi header Authorization: Bearer <accessToken>
→ JwtAuthenticationFilter đọc token
→ JwtUtil validate token
→ Extract email/userId từ token
→ CustomUserDetailsService load user từ database
→ Tạo Authentication object
→ Set vào SecurityContextHolder
→ Request đi tiếp vào Controller
→ Controller/Service lấy user hiện tại
→ Trả response
```

### 4. Các class quan trọng

#### JwtAuthenticationFilter

Dùng để chặn request, lấy JWT từ header, validate token và set authentication.

#### JwtUtil

Dùng để generate, validate và extract claims từ JWT.

#### CustomUserDetails

Đại diện cho user theo chuẩn Spring Security.

#### CustomUserDetailsService

Load user từ database và chuyển thành `CustomUserDetails`.

#### SecurityConfig

Cấu hình endpoint nào public, endpoint nào cần authentication và gắn JWT filter vào filter chain.

#### JwtAuthenticationEntryPoint

Xử lý lỗi khi request chưa authenticated hoặc token không hợp lệ.

#### CurrentUserResponse

DTO dùng để trả thông tin user hiện tại, không trả passwordHash.

---

# Câu hỏi phỏng vấn và câu trả lời mẫu

## Câu 1: Authentication và Authorization khác nhau thế nào?

Trả lời:
Authentication là xác minh user là ai, ví dụ login bằng email/password hoặc JWT. Authorization là kiểm tra user đó có quyền làm gì, ví dụ chỉ ADMIN được vào API admin.

---

## Câu 2: JWT access token được gửi từ frontend lên backend như thế nào?

Trả lời:
Frontend gửi access token trong HTTP header:

```http
Authorization: Bearer <accessToken>
```

Backend đọc header này, lấy token và validate token.

---

## Câu 3: JwtAuthenticationFilter dùng để làm gì?

Trả lời:
`JwtAuthenticationFilter` dùng để đọc JWT từ request, kiểm tra token hợp lệ, load user từ database và set authentication vào `SecurityContextHolder`.

---

## Câu 4: Vì sao cần filter thay vì xử lý token trong từng controller?

Trả lời:
Nếu xử lý token trong từng controller thì code bị lặp và khó bảo trì. Filter giúp xử lý authentication tập trung trước khi request vào controller.

---

## Câu 5: SecurityContextHolder là gì?

Trả lời:
`SecurityContextHolder` là nơi Spring Security lưu thông tin authentication của request hiện tại. Sau khi filter xác thực token thành công, user được set vào context này.

---

## Câu 6: CustomUserDetails là gì?

Trả lời:
`CustomUserDetails` là object đại diện cho user theo chuẩn Spring Security. Nó thường implement `UserDetails` và chứa email, password, roles/authorities.

---

## Câu 7: Vì sao không dùng trực tiếp User entity làm principal?

Trả lời:
User entity là object database, có thể chứa nhiều field nhạy cảm hoặc quan hệ phức tạp. `CustomUserDetails` giúp chỉ đưa những thông tin cần thiết vào Spring Security.

---

## Câu 8: CustomUserDetailsService dùng để làm gì?

Trả lời:
Nó load user từ database, thường bằng email hoặc username, sau đó trả về `CustomUserDetails` để Spring Security sử dụng.

---

## Câu 9: AuthenticationEntryPoint dùng khi nào?

Trả lời:
Nó được gọi khi request chưa được xác thực hoặc token không hợp lệ nhưng lại truy cập API cần authentication. Thường trả HTTP 401.

---

## Câu 10: SessionCreationPolicy.STATELESS nghĩa là gì?

Trả lời:
Nó nghĩa là backend không lưu session đăng nhập trên server. Mỗi request phải gửi token để backend xác thực user.

---

## Câu 11: Vì sao JWT authentication thường dùng STATELESS?

Trả lời:
Vì JWT chứa thông tin xác thực trong token, backend có thể validate token mà không cần session server-side. Điều này phù hợp với REST API và dễ mở rộng hơn.

---

## Câu 12: API GET /api/users/me dùng để làm gì?

Trả lời:
API này trả thông tin user hiện tại đang đăng nhập. Frontend dùng nó để biết user là ai, có role gì và điều hướng giao diện phù hợp.

---

## Câu 13: Vì sao không trả passwordHash trong /api/users/me?

Trả lời:
`passwordHash` là dữ liệu nhạy cảm. API chỉ nên trả thông tin cần thiết như id, fullName, email, status và roles.

---

## Câu 14: Nếu gọi /api/users/me không có token thì sao?

Trả lời:
Backend phải trả HTTP 401 vì request chưa được xác thực.

---

## Câu 15: Nếu token sai hoặc bị chỉnh sửa thì sao?

Trả lời:
Backend validate signature thất bại và trả HTTP 401 với lỗi token không hợp lệ.

---

## Câu 16: Nếu token hết hạn thì sao?

Trả lời:
Backend trả HTTP 401, frontend nên dùng refresh token để gọi API refresh-token lấy access token mới.

---

## Câu 17: Làm sao backend biết user có role gì?

Trả lời:
Backend load user từ database cùng roles của user, sau đó chuyển roles thành authorities trong `CustomUserDetails`.

---

## Câu 18: Role và Authority khác nhau thế nào?

Trả lời:
Role thường là vai trò lớn như ADMIN, STUDENT. Authority là quyền mà Spring Security dùng để kiểm tra truy cập. Trong Spring Security, role thường được map thành authority dạng `ROLE_ADMIN`, `ROLE_STUDENT`.

---

## Câu 19: Vì sao các endpoint login/register phải permitAll?

Trả lời:
Vì user chưa có token trước khi đăng nhập hoặc đăng ký. Nếu login/register bị yêu cầu authentication thì user không thể vào hệ thống.

---

## Câu 20: Tại sao cần kiểm tra user còn ACTIVE khi xác thực token?

Trả lời:
Vì token có thể vẫn còn hạn nhưng tài khoản user đã bị khóa hoặc vô hiệu hóa. Backend cần kiểm tra trạng thái user để chặn truy cập nếu tài khoản không còn hợp lệ.

---

## Câu 21: Nếu user bị LOCKED sau khi đã login thì token cũ có dùng được không?

Trả lời:
Nếu filter luôn load user từ database và kiểm tra status, token cũ sẽ bị từ chối vì user đã bị LOCKED. Đây là lý do nên kiểm tra user status trong quá trình authentication.

---

## Câu 22: JwtAuthenticationFilter nên đặt trước filter nào?

Trả lời:
Thường đặt trước `UsernamePasswordAuthenticationFilter` để xử lý JWT trước khi Spring Security xử lý authentication mặc định.

---

## Câu 23: API protected và public khác nhau thế nào?

Trả lời:
Public API không cần token, ví dụ login/register. Protected API cần token hợp lệ, ví dụ `/api/users/me`.

---

## Câu 24: Vì sao task này là nền tảng cho role-based authorization?

Trả lời:
Vì muốn phân quyền theo role, backend trước tiên phải xác định được user hiện tại là ai và user đó có những role nào. Task này đã đưa user và roles vào SecurityContext.

---

## Câu 25: Sau task này nên làm gì tiếp?

Trả lời:
Nên làm Basic Role-Based Authorization để cấu hình endpoint nào chỉ ADMIN/SUPER_ADMIN được truy cập, endpoint nào chỉ cần authenticated, endpoint nào public.

# INTERVIEW_NOTES - Basic Role-Based Authorization + Security Rules

## Basic Role-Based Authorization + Security Rules

### 1. Tóm tắt ngắn gọn

Task này thiết lập phân quyền cơ bản cho backend bằng Spring Security. Sau khi backend đã xác thực được user bằng JWT, hệ thống cần kiểm tra user đó có quyền truy cập API hay không.

Trong project này:

* Public API không cần token.
* `/api/users/me` cần user đã đăng nhập.
* `/api/admin/**` chỉ cho phép `ADMIN` hoặc `SUPER_ADMIN`.
* `/api/student/**` cho phép `STUDENT`, `ADMIN`, `SUPER_ADMIN`.
* User không có token sẽ nhận 401.
* User có token nhưng không đủ quyền sẽ nhận 403.

### 2. Vì sao task này quan trọng?

Task này là nền tảng để bảo vệ hệ thống thật sự. Nếu chỉ có login/JWT mà không có phân quyền, mọi user đăng nhập đều có thể gọi các API quan trọng. Với role-based authorization, backend có thể bảo vệ API admin, API student, API teacher và các module khác sau này.

### 3. Luồng xử lý chính

```text
Client gọi API
→ JwtAuthenticationFilter xác thực access token
→ SecurityContextHolder có thông tin user + authorities
→ SecurityConfig kiểm tra endpoint cần quyền gì
→ Nếu chưa login: 401
→ Nếu đã login nhưng thiếu quyền: 403
→ Nếu đủ quyền: request đi vào Controller
```

---

# Câu hỏi phỏng vấn và câu trả lời mẫu

## Câu 1: Authentication và Authorization khác nhau thế nào?

Trả lời:
Authentication là xác minh user là ai. Authorization là kiểm tra user đó có quyền làm gì. Ví dụ login bằng JWT là authentication, còn kiểm tra chỉ ADMIN được vào `/api/admin/**` là authorization.

---

## Câu 2: Role-Based Authorization là gì?

Trả lời:
Role-Based Authorization là cơ chế phân quyền dựa trên vai trò của user, ví dụ `ADMIN`, `STUDENT`, `TEACHER`. Mỗi role được phép truy cập một nhóm chức năng khác nhau.

---

## Câu 3: Vì sao backend phải kiểm tra quyền, dù frontend đã ẩn nút admin?

Trả lời:
Frontend chỉ giúp tăng trải nghiệm người dùng, không bảo mật thật sự. User có thể gọi API trực tiếp bằng Postman. Vì vậy backend phải là nơi kiểm tra quyền cuối cùng.

---

## Câu 4: HTTP 401 là gì?

Trả lời:
HTTP 401 nghĩa là request chưa được xác thực hoặc token không hợp lệ. Ví dụ gọi API protected mà không gửi access token sẽ nhận 401.

---

## Câu 5: HTTP 403 là gì?

Trả lời:
HTTP 403 nghĩa là user đã được xác thực nhưng không có đủ quyền. Ví dụ user STUDENT gọi `/api/admin/**` sẽ bị 403.

---

## Câu 6: 401 và 403 khác nhau thế nào?

Trả lời:
401 là chưa đăng nhập hoặc token sai. 403 là đã đăng nhập nhưng không đủ quyền.

---

## Câu 7: AuthenticationEntryPoint dùng để làm gì?

Trả lời:
`AuthenticationEntryPoint` xử lý lỗi 401 khi user chưa authenticated hoặc token không hợp lệ.

---

## Câu 8: AccessDeniedHandler dùng để làm gì?

Trả lời:
`AccessDeniedHandler` xử lý lỗi 403 khi user đã authenticated nhưng không đủ quyền truy cập tài nguyên.

---

## Câu 9: Vì sao cần CustomAccessDeniedHandler?

Trả lời:
Để trả response lỗi 403 theo format chuẩn của project, ví dụ `ApiResponse`, thay vì response mặc định của Spring Security.

---

## Câu 10: SecurityConfig dùng để làm gì?

Trả lời:
`SecurityConfig` cấu hình bảo mật cho ứng dụng, bao gồm endpoint nào public, endpoint nào cần login, endpoint nào cần role cụ thể, filter nào được dùng và session policy.

---

## Câu 11: Vì sao cần SessionCreationPolicy.STATELESS?

Trả lời:
Vì hệ thống dùng JWT, mỗi request tự mang token để xác thực. Backend không cần lưu session đăng nhập server-side.

---

## Câu 12: hasRole('ADMIN') hoạt động thế nào?

Trả lời:
Trong Spring Security, `hasRole('ADMIN')` thường kiểm tra authority `ROLE_ADMIN`. Spring tự thêm prefix `ROLE_`.

---

## Câu 13: hasRole và hasAuthority khác nhau thế nào?

Trả lời:
`hasRole('ADMIN')` thường tương đương kiểm tra `ROLE_ADMIN`, còn `hasAuthority('ROLE_ADMIN')` kiểm tra chính xác authority được truyền vào.

---

## Câu 14: Vì sao role trong database cần map đúng sang authority?

Trả lời:
Vì Spring Security kiểm tra quyền dựa trên authorities. Nếu role map sai format, user có role đúng trong database nhưng vẫn bị 403.

---

## Câu 15: @EnableMethodSecurity dùng để làm gì?

Trả lời:
Nó cho phép dùng các annotation như `@PreAuthorize` để phân quyền ở cấp method, ví dụ chỉ ADMIN được gọi một service hoặc controller method.

---

## Câu 16: Khi nào nên dùng SecurityConfig, khi nào nên dùng @PreAuthorize?

Trả lời:
SecurityConfig phù hợp để phân quyền theo pattern endpoint, ví dụ `/api/admin/**`. `@PreAuthorize` phù hợp khi cần phân quyền chi tiết ở từng method hoặc theo logic nghiệp vụ.

---

## Câu 17: Vì sao /api/auth/login phải permitAll?

Trả lời:
Vì user chưa có token trước khi login. Nếu login yêu cầu authentication thì user sẽ không thể đăng nhập.

---

## Câu 18: Vì sao /api/admin/** cần ADMIN hoặc SUPER_ADMIN?

Trả lời:
Vì các API admin thường liên quan đến quản lý dữ liệu hệ thống như user, course, order, payment. Nếu user thường truy cập được sẽ gây rủi ro bảo mật.

---

## Câu 19: Nếu STUDENT gọi /api/admin/test thì backend nên trả gì?

Trả lời:
Backend nên trả HTTP 403 vì user đã đăng nhập nhưng không đủ quyền.

---

## Câu 20: Nếu gọi /api/admin/test không có token thì backend nên trả gì?

Trả lời:
Backend nên trả HTTP 401 vì request chưa được xác thực.

---

## Câu 21: Tại sao cần test cả 401, 403 và 200?

Trả lời:
Vì đây là ba trạng thái quan trọng của bảo mật API: chưa đăng nhập, không đủ quyền và đủ quyền truy cập thành công.

---

## Câu 22: CustomAccessDeniedHandler nên trả dữ liệu gì?

Trả lời:
Nên trả response chuẩn của hệ thống, gồm success=false, error code, message và timestamp nếu project có.

---

## Câu 23: Role SUPER_ADMIN khác ADMIN thế nào?

Trả lời:
SUPER_ADMIN thường có toàn quyền hệ thống, bao gồm quản lý admin khác và cấu hình nhạy cảm. ADMIN có quyền quản lý thông thường nhưng có thể bị giới hạn một số phần.

---

## Câu 24: Có nên chỉ phân quyền ở frontend không?

Trả lời:
Không. Frontend có thể bị bypass. Backend bắt buộc phải kiểm tra quyền ở API.

---

## Câu 25: Sau khi có phân quyền cơ bản, nên làm gì tiếp?

Trả lời:
Nên bắt đầu module chính của hệ thống. Với website học tiếng Nhật, bước tiếp theo là Course/Lesson Database Foundation để chuẩn bị cho admin CRUD khóa học và bài học.

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