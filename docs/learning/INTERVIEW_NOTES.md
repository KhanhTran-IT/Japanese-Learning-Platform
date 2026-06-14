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
