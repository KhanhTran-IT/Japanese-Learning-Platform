# Giải Thích Các Khái Niệm

Tài liệu ghi lại các khái niệm, pattern, best practices mà bạn đã học được và muốn ghi nhớ lâu dài.

## Hướng dẫn ghi chép

- **Ghi tên concept rõ ràng** - tên tiếng Anh và tiếng Việt
- **Giải thích ngắn gọn** ý tưởng chính
- **Cho ví dụ cụ thể** từ project hoặc real-world
- **Ghi lợi ích/khi nào dùng**
- **Ghi các misconception** hay gặp phải
- **Link tới resources** nếu có
- **Rating hiểu biết**: 🔴 Chưa hiểu | 🟡 Hiểu một phần | 🟢 Hiểu rõ

## Template mẫu

```markdown
### [Concept Name] - [Tên tiếng Việt]

**Rating:** 🔴/🟡/🟢

**Định nghĩa:**
Giải thích ngắn gọn về concept

**Ví dụ:**
\`\`\`java
// Ví dụ code
\`\`\`

**Lợi ích/Khi nào dùng:**

- Lợi ích 1
- Lợi ích 2

**Misconception hay gặp:**

- Hiểu sai 1
- Hiểu sai 2

**Resources:** Link tới bài viết, video
```

## Concepts

### Dependency Injection (DI) - Tiêm Phụ Thuộc

**Rating:** 🟢

**Định nghĩa:**
Pattern mà các dependency (phụ thuộc) được truyền từ bên ngoài vào class, thay vì class tự tạo. Ở Spring Boot dùng `@Autowired` hoặc constructor injection.

**Ví dụ:**

```java
// ❌ Tự tạo dependency - khó test
public class UserService {
    private UserRepository repo = new UserRepository();
}

// ✅ Inject dependency - dễ test
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }
}
```

**Lợi ích/Khi nào dùng:**

- Dễ test (mock dependency)
- Giảm coupling giữa các class
- Dễ thay đổi implementation

**Misconception hay gặp:**

- DI chỉ là `@Autowired` - sai, constructor injection tốt hơn
- DI làm code phức tạp - sai, nó làm code dễ maintain

---

### REST API Design - Thiết Kế REST API

**Rating:** 🟢

**Định nghĩa:**
Cách thiết kế API theo REST principles: sử dụng HTTP methods (GET, POST, PUT, DELETE) đúng cách, tài nguyên được đại diện bởi URL.

**Ví dụ:**

```
GET    /api/users           - Lấy danh sách users
GET    /api/users/{id}      - Lấy user cụ thể
POST   /api/users           - Tạo user mới
PUT    /api/users/{id}      - Cập nhật user
DELETE /api/users/{id}      - Xóa user
```

**Lợi ích/Khi nào dùng:**

- API dễ hiểu, dễ sử dụng
- Standard, dễ collaborate với team
- Client có thể guess endpoint

**Misconception hay gặp:**

- POST dùng cho mọi thao tác - sai, PUT cho update
- URL nên có động từ `/api/users/create` - sai, dùng HTTP method

---

### Vue Composition API - API soạn thảo Vue

**Rating:** 🟡

**Định nghĩa:**
Cách viết Vue 3 component bằng function thay vì object. Giúp tổ chức logic tốt hơn và tái sử dụng code dễ hơn.

**Ví dụ:**

```javascript
// Vue 2 / Options API
export default {
  data() { return { count: 0 } },
  methods: { increment() { this.count++ } }
}

// Vue 3 / Composition API
export default {
  setup() {
    const count = ref(0)
    const increment = () => count.value++
    return { count, increment }
  }
}
```

**Lợi ích/Khi nào dùng:**

- Logic liên quan được group lại
- Dễ tái sử dụng logic qua composables
- Setup() dễ test

**Misconception hay gặp:**

- Phải dùng Composition API - sai, Options API cũng ok cho đơn giản
- `value` là lỗi - không, ref.value cần thiết

---

### N+1 Query Problem - Vấn đề Truy Vấn N+1

**Rating:** 🟡

**Định nghĩa:**
Khi query 1 user (1 query), sau đó lặp qua 100 user để query orders của mỗi user (100 queries), tổng cộng 101 queries. Cần dùng JOIN hoặc fetch strategy tốt.

**Ví dụ:**

```java
// ❌ N+1 problem
List<User> users = userRepository.findAll(); // 1 query
users.forEach(u -> {
    System.out.println(u.getOrders()); // 100 queries
});

// ✅ Fix với JOIN FETCH hoặc eager loading
@Query("SELECT u FROM User u LEFT JOIN FETCH u.orders")
List<User> findAllWithOrders();
```

**Lợi ích/Khi nào dùng:**

- Cải thiện performance đáng kể
- Đặc biệt quan trọng khi data lớn

**Misconception hay gặp:**

- Lazy loading luôn xấu - không, lazy loading ok nếu đúng juncture
- Phải eager load mọi thứ - không, chỉ eager load khi cần

---

## Spring Boot Backend Foundation Concepts

### 1. Spring Boot - Spring Boot Framework

**Rating:** 🟢

**Giải thích:**
Framework Java giúp tạo standalone, production-ready REST API server một cách nhanh chóng. Tự động setup configuration, không cần file XML phức tạp.

**Ví dụ trong project:**

- Backend folder của bạn dùng Spring Boot
- Chạy `mvn spring-boot:run` → Server chạy tại `localhost:8080`
- Swagger UI mở được → tất cả do Spring Boot

**Ưu điểm:**

- Setup nhanh, convention over configuration
- Tự động config Tomcat web server
- Dễ deploy (1 file JAR chứa tất cả)

**Câu hỏi phỏng vấn:**

> "Tại sao bạn chọn Spring Boot thay vì Spring Framework thuần?"

**Câu trả lời mẫu:**

> "Spring Boot nhanh hơn, ít boilerplate, tự động config mọi thứ. Spring Framework thuần cần config XML dài dòng. Spring Boot thích hợp hơn cho startup hoặc project cần nhanh."

---

### 2. REST API - RESTful API

**Rating:** 🟢

**Giải thích:**
Kiến trúc API dùng HTTP protocol để định nghĩa operations trên resources. Sử dụng standard HTTP methods (GET, POST, PUT, DELETE) và HTTP status codes.

**Ví dụ trong project:**

```
GET  http://localhost:8080/api/health        → Lấy health status
POST http://localhost:8080/api/users         → Tạo user mới
PUT  http://localhost:8080/api/users/{id}    → Cập nhật user
DELETE http://localhost:8080/api/users/{id}  → Xóa user
```

**Ưu điểm:**

- Standard, mọi developer đều hiểu
- Stateless, dễ scale
- Mỗi endpoint có trách nhiệm rõ ràng

**Câu hỏi phỏng vấn:**

> "REST API khác GraphQL ở điểm nào?"

**Câu trả lời mẫu:**

> "REST dùng URL + HTTP method để định nghĩa operation, GraphQL dùng single endpoint + query language. REST dễ caches và hiểu, GraphQL linh hoạt hơn nhưng phức tạp hơn."

---

### 3. Controller - HTTP Request Handler

**Rating:** 🟢

**Giải thích:**
Class trong Spring Boot xử lý HTTP requests từ client. Định nghĩa endpoints, nhận/trả dữ liệu, delegate logic cho Service layer.

**Ví dụ trong project:**

```java
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
```

**Ưu điểm:**

- Tách biệt HTTP handling từ business logic
- Dễ test (mock HTTP layer)
- Rõ ràng, mỗi method là một endpoint

**Câu hỏi phỏng vấn:**

> "Controller có nên chứa business logic không?"

**Câu trả lời mẫu:**

> "Không. Controller chỉ handle HTTP request/response và validation. Business logic nên ở Service layer. Này giúp code dễ test, tái sử dụng, maintain."

---

### 4. ApiResponse - Chuẩn hóa Response

**Rating:** 🟢

**Giải thích:**
Class wrapper để chuẩn hóa format response trả về từ tất cả API endpoints. Đảm bảo client luôn biết structure response.

**Ví dụ trong project:**

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "status": "UP",
    "timestamp": "2026-06-12T10:30:00"
  }
}
```

**Ưu điểm:**

- Frontend developer biết chính xác format
- Dễ xử lý lỗi (code, message chuẩn)
- Dễ thêm metadata (pagination, timestamp)

**Câu hỏi phỏng vấn:**

> "Tại sao cần ApiResponse wrapper? Không trả response object trực tiếp được?"

**Câu trả lời mẫu:**

> "Nếu không có wrapper: API này trả `{user: {...}}`, API khác trả `[{...}]`, client phải xử lý từng cách. Có wrapper: Tất cả trả `{code, message, data}`, client xử lý 1 cách duy nhất. Dễ maintain, dễ debug, dễ log."

---

### 5. GlobalExceptionHandler - Xử lý Lỗi Toàn Cục

**Rating:** 🟢

**Giải thích:**
Class Spring dùng `@RestControllerAdvice` bắt tất cả exceptions xảy ra trong application và trả về response chuẩn thay vì error 500 lộn xộn.

**Ví dụ trong project:**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse> handleNotFound(UserNotFoundException ex) {
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

**Ưu điểm:**

- Tất cả lỗi trả về format chuẩn
- Không expose sensitive information
- Dễ log và track lỗi

**Câu hỏi phỏng vấn:**

> "Nếu không có GlobalExceptionHandler thì sao?"

**Câu trả lời mẫu:**

> "Client nhận stack trace dài lộn xộn, không hiểu gì. Nên có GlobalExceptionHandler để catch lỗi, format chuẩn, trả message user-friendly."

---

### 6. Swagger/OpenAPI - API Documentation Tự Động

**Rating:** 🟢

**Giải thích:**
Tool tự động generate documentation cho API từ code annotations. Cho phép test API trực tiếp từ web UI.

**Ví dụ trong project:**

```java
@GetMapping("/health")
@Operation(summary = "Check API health status")
@ApiResponse(responseCode = "200", description = "API is UP")
public ResponseEntity<ApiResponse> health() {
    return ResponseEntity.ok(ApiResponse.success(Map.of("status", "UP")));
}
```

Mở tại `http://localhost:8080/swagger-ui/index.html` → thấy endpoint `/api/health` với description và response format.

**Ưu điểm:**

- Documentation luôn up-to-date (từ code)
- Frontend dev tự khám phá API
- Có thể test endpoint trực tiếp

**Câu hỏi phỏng vấn:**

> "Swagger documentation tự động cập nhật khi code thay đổi sao?"

**Câu trả lời mẫu:**

> "Vì Swagger annotations `@Operation`, `@ApiResponse` được viết trực tiếp trong code. Khi bạn deploy, Swagger generate documentation từ annotations này. Nên nó luôn match code hiện tại."

---

### 7. Health Check API - Kiểm Tra Server Sống

**Rating:** 🟢

**Giải thích:**
Endpoint đơn giản (thường GET /health) để kiểm tra xem backend server có đang chạy bình thường hay không. Dùng trong production monitoring.

**Ví dụ trong project:**

```
GET http://localhost:8080/api/health
Response: {
  "status": "UP",
  "timestamp": "2026-06-12T10:30:00",
  "database": "CONNECTED"
}
```

**Ưu điểm:**

- DevOps/Kubernetes dùng để biết server còn sống
- Load balancer biết route request vào server healthy
- Alert khi server fail

**Câu hỏi phỏng vấn:**

> "Health Check API dùng để làm gì? Tại sao quan trọng?"

**Câu trả lời mẫu:**

> "Health Check giúp monitoring system biết server còn sống hay chết. Kubernetes/Docker tự động restart container nếu health check fail. Load balancer tránh route request vào server dead. Quan trọng cho high availability."

---

### 8. application.yml - Cấu Hình Ứng Dụng

**Rating:** 🟢

**Giải thích:**
File YAML trong Spring Boot để cấu hình ứng dụng: server port, database connection, logging, profile (dev/prod).

**Ví dụ trong project:**

```yaml
# src/main/resources/application.yml
spring:
  application:
    name: japanese-learning-platform
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  datasource:
    url: jdbc:mysql://localhost:3306/jlp
    username: root
    password: password

server:
  port: 8080
  servlet:
    context-path: /
```

**Ưu điểm:**

- Externalize configuration (không hardcode)
- Dễ thay đổi giữa dev/prod environments
- Centralized configuration

**Câu hỏi phỏng vấn:**

> "Tại sao không hardcode database URL vào code?"

**Câu trả lời mẫu:**

> "Vì dev, staging, production cần database khác nhau. Hardcode sẽ phải rebuild code cho mỗi environment. Dùng application.yml cho phép set via environment variables, không cần rebuild."

---

### 9. Maven - Build Tool & Dependency Manager

**Rating:** 🟢

**Giải thích:**
Tool Java để build project, manage dependencies (libraries). File `pom.xml` định nghĩa project structure, dependencies, build configuration.

**Ví dụ trong project:**

```xml
<!-- pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    </dependency>
</dependencies>

<!-- Chạy command -->
mvn clean install      <!-- Download dependencies, compile, package -->
mvn spring-boot:run    <!-- Run Spring Boot application -->
```

**Ưu điểm:**

- Tập trung quản lý dependencies 1 chỗ
- Tự động download từ Maven repository
- Build reproducible (lần build nào cũng giống)

**Câu hỏi phỏng vấn:**

> "Maven là gì? Tại sao cần?"

**Câu trả lời mẫu:**

> "Maven là build tool + dependency manager cho Java. `pom.xml` define dependencies (Spring Boot, JPA, etc.), Maven tự động download từ repo, compile code, run tests, package. Không cần manually manage JAR files."

---

### 10. Dependency - Phụ Thuộc / Thư Viện

**Rating:** 🟢

**Giải thích:**
Một library/framework bên ngoài mà project cần để hoạt động. Ví dụ Spring Boot, JPA, MySQL Driver. Maven manage dependencies từ Maven Central Repository.

**Ví dụ trong project:**

```xml
<!-- Dependencies trong pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>3.1.0</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

**Ưu điểm:**

- Code reuse, không cần viết lại
- Tiết kiệm thời gian development
- Có version control (dễ update)

**Câu hỏi phỏng vấn:**

> "Dependency hell là gì? Làm sao tránh?"

**Câu trả lời mẫu:**

> "Dependency hell: Project A depend B v1.0, B depend C v2.0; Project A depend C v3.0 → Conflict. Tránh: Dùng dependency management tool (Maven), update dependencies định kỳ, test sau khi update, đừng dùng quá nhiều dependencies."

---

## Ghi chú

**Tổng kết Backend Foundation:**

- Spring Boot tự động setup, Maven manage dependencies
- Controller handle HTTP, Service handle logic, Repository handle database
- ApiResponse + GlobalExceptionHandler chuẩn hóa error handling
- Swagger tự động generate documentation
- Health Check + application.yml chuẩn bị cho production

**Phần cần ôn lại thêm:**

- Spring Security (authentication, authorization)
- JPA/Hibernate (database ORM)
- Unit testing với JUnit + Mockito
- Docker + Kubernetes deployment

---

## Auth/User Database - JPA & Entities Concepts

### JPA Entity - JPA Thực thể

**Rating:** 🟢

**Giải thích:**
Class Java được đánh dấu `@Entity` để Hibernate map với 1 table trong database. Mỗi field là 1 column, mỗi instance là 1 row.

**Ví dụ trong project:**

```java
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserStatus status;  // ACTIVE, INACTIVE, SUSPENDED, DELETED
}
```

**Ưu điểm:**

- Hibernate tự động tạo/update tables
- Code Java thay vì SQL thuần
- Dễ maintain, refactor

**Câu hỏi phỏng vấn:**

> "Vì sao phải dùng @Entity thay vì viết SQL CREATE TABLE?"

**Câu trả lời:**

> "Vì @Entity cho phép:
>
> - Write once, deploy everywhere (database agnostic)
> - Tự động create/update schema (Hibernate ddl-auto)
> - Type-safe queries (không string SQL dễ sai)
> - Easier refactoring (IDE can track references)."

---

### ManyToMany - Quan hệ Nhiều - Nhiều

**Rating:** 🟢

**Giải thích:**
Khi 1 entity có thể liên kết với nhiều entity khác, và entity kia cũng có thể liên kết với nhiều entity đầu tiên. Cần 1 join table ở giữa.

**Ví dụ trong project:**

```java
// User.java
@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
@JoinTable(
    name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id")
)
private Set<Role> roles = new HashSet<>();

// Role.java
@ManyToMany(mappedBy = "roles")  // Inverse side
private Set<User> users = new HashSet<>();
```

**Database structure:**

```
table users: id, email, password, ...
table roles: id, name, ...
table user_roles: user_id (FK), role_id (FK)  -- Join table
```

**Ưu điểm:**

- Hỗ trợ các relationship phức tạp
- Hibernate handle join logic

**Misconception hay gặp:**

- ❌ "Đặt Role đảo ngược là user_id, role_id" - sai, join table chứa 2 FKs
- ❌ "CascadeType.ALL là tốt nhất" - sai, dùng PERSIST + MERGE thường an toàn hơn

**Câu hỏi phỏng vấn:**

> "Tại sao ManyToMany cần join table? Không thể lưu trong 1 table được sao?"

**Câu trả lời:**

> "Vì nếu lưu trong 1 table (ví dụ users): users (id, email, role_id) thì:
>
> - 1 user có nhiều roles → phải có nhiều rows cho 1 user
> - Dữ liệu duplicate (email, password duplicate nhiều lần)
> - Waste storage, khó query
>
> Dùng join table:
>
> - users: id, email, password (1 row per user)
> - roles: id, name (1 row per role)
> - user_roles: user_id, role_id (many rows)
> - Dữ liệu normalized, efficient."

---

### FetchType.LAZY vs EAGER - Kiểu Tải Dữ Liệu

**Rating:** 🟢

**Giải thích:**
Kiểm soát khi nào related entities được load từ database.

**Ví dụ:**

```java
// LAZY: Roles không load khi query User
@ManyToMany(fetch = FetchType.LAZY)
private Set<Role> roles;

// EAGER: Roles tự động load khi query User (dùng JOIN)
@ManyToMany(fetch = FetchType.EAGER)
private Set<Role> roles;
```

**So sánh:**
| | LAZY | EAGER |
|---|---|---|
| **Query User** | 1 query | 1 query (+ JOIN) |
| **Access roles** | +1 query | 0 queries (đã loaded) |
| **100 users** | 1 + N queries (N+1) | 1 query (1 JOIN) |
| **Memory** | Thấp | Cao |
| **Network** | Bất ngờ delays | Có thể slow JOIN |

**Quy tắc:**

- Dùng **LAZY** mặc định (hầu hết trường hợp)
- Dùng **EAGER** chỉ khi chắc chắn cần data + acceptable JOINs

**Câu hỏi phỏng vấn:**

> "N+1 problem là gì? Làm sao fix?"

**Câu trả lời:**

> "N+1: Query 1 user → N+1 queries thay vì 1. Fix: Dùng FetchType.EAGER hoặc @Query với LEFT JOIN FETCH."

---

### Spring Data JPA Repository - Truy vấn Dữ liệu

**Rating:** 🟢

**Giải thích:**
Interface extends `JpaRepository<T, ID>` để query database mà không viết SQL.

**Ví dụ trong project:**

```java
public interface UserRepository extends JpaRepository<User, Long> {
    // Method name conventions
    User findByEmail(String email);
    List<User> findByStatus(UserStatus status);
    List<User> findByStatusAndCreatedDateAfter(UserStatus status, LocalDateTime date);

    // Custom query
    @Query("SELECT u FROM User u WHERE u.status = :status")
    List<User> findActiveUsers(@Param("status") UserStatus status);
}
```

**Ưu điểm:**

- Tự động generate implementations
- Type-safe queries
- Mixin methods từ JpaRepository (save, delete, findAll, etc.)

**Phương thức quy ước:**

- `findBy*` → SELECT ... WHERE
- `findBy*OrderBy*` → ORDER BY
- `countBy*` → COUNT
- `*And*`, `*Or*` → AND, OR

**Câu hỏi phỏng vấn:**

> "Spring Data tự động generate queries làm thế nào?"

**Câu trả lời:**

> "Spring dùng reflection để parse method name, generate SQL tương ứng. Ví dụ `findByEmail` → `SELECT * FROM users WHERE email = ?`. Nếu method complex quá, dùng @Query annotation viết SQL tay."

---

### Enum - Tập hợp Giá trị Cố định

**Rating:** 🟢

**Giải thích:**
Type Java với một tập các giá trị cố định (không thể add/remove). Dùng để ràng buộc valid values.

**Ví dụ trong project:**

```java
public enum UserStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended"),
    DELETED("Deleted");

    private final String displayName;
    UserStatus(String displayName) {
        this.displayName = displayName;
    }
}

public enum RoleName {
    ADMIN, TEACHER, STUDENT
}

// Sử dụng
@Enumerated(EnumType.STRING)  // Lưu tên enum ("ACTIVE")
private UserStatus status;
```

**Database:**

```sql
-- MySQL sử dụng ENUM type
ALTER TABLE users ADD COLUMN status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED');
```

**Ưu điểm:**

- Type-safe (không thể gán giá trị random)
- Compile-time checking
- Switch statements dễ

**Ví dụ sử dụng:**

```java
if (user.getStatus() == UserStatus.ACTIVE) {
    // ...
}
```

**Câu hỏi phỏng vấn:**

> "Enum ORDINAL vs STRING, cái nào tốt hơn?"

**Câu trả lời:**

> "STRING tốt hơn vì:
>
> - @Enumerated(EnumType.STRING): Lưu "ACTIVE" (readable, safe)
> - @Enumerated(EnumType.ORDINAL): Lưu 0, 1, 2 (compact, nhưng if reorder enum → data corrupt)
>
> Best practice: Dùng STRING."

---

### CascadeType - Lan truyền Hành động

**Rating:** 🟢

**Giải thích:**
Khi thực hiện hành động trên parent entity (persist, merge, remove), nó lan truyền tới child entities.

**Ví dụ:**

```java
// ❌ Nguy hiểm - dùng ALL
@OneToMany(cascade = CascadeType.ALL)
private Set<RefreshToken> tokens;  // Xóa user → xóa tokens OK

// ❌ Còn nguy hiểm hơn - dùng ALL cho ManyToMany
@ManyToMany(cascade = CascadeType.ALL)  // Xóa user → xóa roles! Sai!
private Set<Role> roles;

// ✅ Đúng - dùng PERSIST + MERGE cho ManyToMany
@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
private Set<Role> roles;
```

**Các CascadeType:**
| Type | Ý nghĩa |
|---|---|
| **PERSIST** | Khi persist parent → persist children |
| **MERGE** | Khi merge parent → merge children |
| **REMOVE** | Khi delete parent → delete children |
| **REFRESH** | Khi refresh parent → refresh children |
| **ALL** | Tất cả các hành động trên |

**Quy tắc:**

- OneToMany (User → RefreshTokens): `CascadeType.ALL` OK
- ManyToMany (User ↔ Roles): `CascadeType.PERSIST, MERGE` (never REMOVE)

**Câu hỏi phỏng vấn:**

> "Tại sao không nên dùng CascadeType.REMOVE cho ManyToMany?"

**Câu trả lời:**

> "Vì 1 role có thể thuộc nhiều users. Nếu xóa user với REMOVE cascade → xóa role luôn → tất cả users khác mất role. Disaster!"

---

### RefreshToken - Token Tái tạo

**Rating:** 🟢

**Giải thích:**
Token dài hạn dùng để request AccessToken mới khi token hiện tại hết hạn. Được lưu trong database để có thể revoke.

**Ví dụ trong project:**

```java
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime expiryDate;
    private LocalDateTime createdAt;
}

// Flow:
// 1. User login → sinh RefreshToken, lưu DB, trả cho client
// 2. AccessToken expire → client gửi RefreshToken
// 3. Server check RefreshToken còn valid không, sinh AccessToken mới
// 4. Logout → xóa RefreshToken từ DB (revoke)
```

**Ưu điểm:**

- Tăng security (short-lived AccessToken)
- Có thể logout (revoke token)
- Có thể track user sessions

**Câu hỏi phỏng vấn:**

> "Tại sao cần RefreshToken? Không thể dùng long-lived AccessToken?"

**Câu trả lời:**

> "Nếu AccessToken bị steal:
>
> - Long-lived (7 ngày): Attacker dùng được 7 ngày
> - Short-lived (15 phút): Attacker chỉ dùng được 15 phút
>
> RefreshToken:
>
> - Long-lived, nhưng lưu backend DB
> - Backend có thể track, revoke khi nghi ngờ
> - Frontend không thể revoke AccessToken, chỉ backend revoke RefreshToken."

---

### @JoinTable - Bảng Kết Nối

**Rating:** 🟢

**Giải thích:**
Annotation định nghĩa tên và structure của join table cho ManyToMany relationships.

**Ví dụ:**

```java
@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
@JoinTable(
    name = "user_roles",  // Tên join table
    joinColumns = @JoinColumn(name = "user_id"),  // FK column pointing to User
    inverseJoinColumns = @JoinColumn(name = "role_id")  // FK column pointing to Role
)
private Set<Role> roles = new HashSet<>();
```

**Generated table:**

```sql
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

**Câu hỏi phỏng vấn:**

> "Tại sao phải dùng @JoinTable? Hibernate không thể tự generate?"

**Câu trả lời:**

> "Hibernate có thể tự generate, nhưng tên column/table có thể không đẹp (ví dụ user_roles_id, user_roles_role_id). @JoinTable cho phép customize:
>
> - Tên join table
> - Tên FK columns
> - Indexes, constraints
>
> Best practice: Luôn customize thay vì dùng default."

---

### @GeneratedValue - Tự động Sinh Khóa Chính

**Rating:** 🟢

**Giải thích:**
Annotation xác định cách sinh giá trị khóa chính (ID). Có nhiều chiến lược:

**Ví dụ:**

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)  // DB auto-increment
private Long id;

// Strategies:
// IDENTITY: DB auto-increment (auto_increment trong MySQL)
// SEQUENCE: DB sequence (Oracle, PostgreSQL)
// AUTO: Spring tự chọn based on database
// UUID: Dùng UUID thay vì numeric
```

**Ưu điểm:**

- ID tự động sinh, không cần set tay
- Database level guarantee unique

**Câu hỏi phỏng vấn:**

> "Generationtype IDENTITY vs SEQUENCE khác gì?"

**Câu trả lời:**

> "IDENTITY (MySQL): Auto-increment column (1, 2, 3, ...). Sau khi insert, get ID từ DB.
> SEQUENCE (Oracle): DB có sequence object, Spring call để get next ID trước insert.
>
> IDENTITY dễ hơn (1 step), SEQUENCE linh hoạt hơn (batch operations)."

---

## Register API - DTO & Validation Concepts

### DTO (Data Transfer Object) - Đối Tượng Truyền Tải Dữ Liệu

**Rating:** 🟢

**Giải thích:**
DTO là class riêng dùng để nhận/trả dữ liệu từ HTTP request/response, không phải Entity database. Tách biệt Entity khỏi API giúp tăng bảo mật, linh hoạt, và tránh lỗi.

**Ví dụ:**

```java
// DTO nhận từ client
@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private String confirmPassword;
}

// Entity database (không return trực tiếp)
@Entity
public class User {
    private String passwordHash;  // Nhạy cảm, không expose
    private LocalDateTime createdAt;
    private Boolean emailVerified;
}

// DTO trả về client
@Data
public class RegisterResponse {
    private Long id;
    private String fullName;
    private String email;
    private List<String> roles;  // Chỉ role name, không toàn bộ Role entity
}
```

**Ưu điểm:**

- **Bảo mật**: Không expose passwordHash, createdAt, hay internal fields
- **Flexibility**: Frontend schema khác DB schema, DTO là bridge
- **Tránh Infinite Recursion**: User ↔ Role ManyToMany, nếu trả Entity → JSON serializer bị vòng lặp
- **API Versioning**: Tạo RegisterRequestV2, RegisterResponseV2 mà không thay Entity
- **Validation**: DTO chứa annotation validate, Entity không cần

**Misconception:**

- ❌ "Dùng Entity trực tiếp làm DTO cũng được" → Sai, rủi ro bảo mật & vòng lặp JSON
- ❌ "DTO chỉ dùng khi phức tạp" → Sai, nên dùng từ đầu dù đơn giản

**Câu hỏi phỏng vấn:**

> "Nếu Entity có 30 field, nhưng API chỉ cần 5 field, cách nào tốt nhất?"

**Câu trả lời:**

> "Tạo DTO chỉ với 5 field cần thiết. Không trả toàn bộ Entity. Lợi ích:
>
> - API response nhẹ hơn
> - Frontend biết rõ cấu trúc
> - DB schema thay đổi không ảnh hưởng API
> - Bảo mật: không expose internal fields"

---

### Bean Validation - Validate Dữ Liệu Tự Động

**Rating:** 🟢

**Giải thích:**
Chuẩn Java (JSR-303/JSR-380) để validate dữ liệu thông qua annotation. Spring Boot auto-enable Bean Validation.

**Ví dụ:**

```java
@Data
public class RegisterRequest {
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 150, message = "Họ tên ≤ 150 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, max = 100, message = "Mật khẩu 8-100 ký tự")
    private String password;

    @NotBlank
    private String confirmPassword;
}

@RestController
public class AuthController {
    @PostMapping("/register")
    // @Valid ← Trigger Bean Validation
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Nếu request sai format → MethodArgumentNotValidException
        // → GlobalExceptionHandler xử lý
        // → Không chạy đến đây
    }
}
```

**Common annotations:**

- `@NotNull` / `@NotBlank`: Field không được null/blank
- `@Size(min, max)`: String/Collection size
- `@Email`: Email format
- `@Min` / `@Max`: Number range
- `@Pattern`: Regex matching
- `@Valid`: Trigger validation cho nested objects

**Ưu điểm:**

- Không cần `if-else` checks trong Controller
- Declarative (nói LÀ CÁI GÌ, không nói LÀM GÌ)
- Reusable: Cùng DTO dùng ở nhiều Controller
- Lỗi tập trung xử lý trong GlobalExceptionHandler

**Misconception:**

- ❌ "Validate trong Controller trước @Valid" → Không cần, @Valid đã làm
- ❌ "Quên @Valid, validation không chạy" → Sai, validation chỉ chạy nếu có @Valid

**Câu hỏi phỏng vấn:**

> "Nếu quên `@Valid` trước `@RequestBody`, điều gì sẽ xảy ra?"

**Câu trả lời:**

> "Bean Validation không chạy. Request có thể chứa null/blank values, trực tiếp vào Service. Hậu quả:
>
> - NullPointerException khi access field
> - Invalid data vào database
> - Khó debug
>
> Best practice: **Luôn** thêm @Valid."

---

### @Transactional - Quản Lý Transaction

**Rating:** 🟢

**Giải thích:**
Annotation định nghĩa transaction boundary. Spring wrap method trong 1 transaction - hoặc tất cả commit, hoặc tất cả rollback.

**Ví dụ:**

```java
@Service
public class AuthServiceImpl {

    @Transactional  // ← Transaction wraps toàn bộ method
    public RegisterResponse register(RegisterRequest request) {
        // Step 1: Validate
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);  // ← Rollback ngay
        }

        // Step 2: Get role
        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        // Step 3: Create user
        User user = new User(...);
        userRepository.save(user);  // ← INSERT

        // Step 4: Return response
        return toResponse(user);  // ← Nếu đến đây thành công → COMMIT
    }
}
```

**ACID guarantees:**

- **Atomicity**: All-or-nothing (tất cả commit hoặc tất cả rollback)
- **Consistency**: DB từ consistent state này sang consistent state khác
- **Isolation**: Transactions không ảnh hưởng lẫn nhau
- **Durability**: Commit thành công, data không bao giờ mất

**Khi nào commit/rollback:**

- **Commit**: Method kết thúc bình thường (return)
- **Rollback**: Exception ném ra (mặc định RuntimeException, hoặc custom exception)

**Ưu điểm:**

- Tự động rollback nếu có lỗi → không orphan data
- Consistent state, không partial update
- Dễ sử dụng: chỉ cần 1 annotation

**Misconception:**

- ❌ "Không cần @Transactional, database tự quản lý" → Sai, cần để wrap multiple operations
- ❌ "@Transactional bắt tất cả exception" → Sai, mặc định chỉ RuntimeException

**Câu hỏi phỏng vấn:**

> "Nếu `userRepository.save()` thành công, nhưng `roleRepository.findByName()` throw exception, điều gì xảy ra?"

**Câu trả lời:**

> "Với @Transactional: ROLLBACK toàn bộ, userRepository.save() được undo.
> Không @Transactional: User được lưu, exception ném ra → orphan data."

---

### ErrorCode Enum - Chuẩn Hóa Lỗi

**Rating:** 🟢

**Giải thích:**
Enum tập trung định nghĩa lỗi, bao gồm code (số), HTTP status, message. Thay vì throw Exception với string message tùy tiện.

**Ví dụ:**

```java
public enum ErrorCode {
    // 2xxx: Auth errors
    EMAIL_ALREADY_EXISTS(2001, HttpStatus.CONFLICT, "Email đã tồn tại"),
    PASSWORD_CONFIRM_NOT_MATCH(2010, HttpStatus.BAD_REQUEST, "Mật khẩu xác nhận không khớp"),

    // 3xxx: User/Role errors
    ROLE_NOT_FOUND(3002, HttpStatus.INTERNAL_SERVER_ERROR, "Không tìm thấy role"),
}

// Throw AppException với ErrorCode
if (userRepository.existsByEmail(email)) {
    throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);  // 409 Conflict
}

// API response
{
    "success": false,
    "code": 2001,
    "message": "Email đã tồn tại",
    "data": null
}
```

**Ưu điểm:**

- **Tập trung định nghĩa**: Tất cả error ở 1 nơi, dễ bảo trì
- **Chuẩn hóa**: Mỗi error có code, status, message duy nhất
- **Frontend parse**: Biết code 2001 = email conflict, code 2010 = password mismatch
- **I18n (Internationalization)**: Code không thay đổi, message dịch
- **IDE support**: Autocomplete, tránh typo

**Quy tắc đặt code:**

```
2xxx: Auth (2001, 2002, ..., 2010, 2020, ...)
3xxx: User/Role
4xxx: Course
5xxx: Payment
```

**Misconception:**

- ❌ "Throw Exception(\"Email already exists\")" → Sai, frontend khó parse
- ❌ "Mỗi file Exception riêng" → Sai, tập trung ErrorCode enum

**Câu hỏi phỏng vấn:**

> "Tại sao dùng ErrorCode enum thay vì throw Exception với message?"

**Câu trả lời:**

> "ErrorCode enum tập trung, chuẩn hóa, dễ maintain:
>
> - 1 nơi định nghĩa → dễ bảo trì
> - Frontend parse code (không parse string) → logic linh hoạt
> - Tránh typo bằng autocomplete
> - I18n: code cố định, message dịch"

---

### PasswordEncoder - Mã Hóa Mật Khẩu An Toàn

**Rating:** 🟢

**Giải thích:**
Spring Security interface để hash/verify password an toàn. BCryptPasswordEncoder là implementation được recommend.

**Ví dụ:**

```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // ← Recommend implementation
    }
}

// Trong Service
@Service
public class AuthServiceImpl {
    private final PasswordEncoder passwordEncoder;

    public RegisterResponse register(RegisterRequest request) {
        // Hash password
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        // hashedPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMye..."

        User user = new User();
        user.setPasswordHash(hashedPassword);  // Lưu hash, không lưu plain-text
        userRepository.save(user);
    }

    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
}
```

**Hash vs Encryption:**

| Aspect          | Hash (BCrypt)            | Encryption                    |
| --------------- | ------------------------ | ----------------------------- |
| **Reversible?** | ❌ No                    | ✅ Yes                        |
| **Use for**     | Password, sensitive data | Credit card, encrypted fields |
| **Security**    | ✅ Nên dùng              | ❌ Không cho password         |
| **Speed**       | Chậm (intentional)       | Nhanh                         |

**Tại sao BCrypt an toàn hơn MD5:**

- **Speed**: BCrypt chậm (~1,000 guesses/s), MD5 nhanh (1 tỷ guesses/s)
- **Salt**: BCrypt tự động add salt, MD5 không
- **Rainbow table**: MD5 có pre-computed tables, BCrypt không

**Ưu điểm:**

- Auto-salt: Mỗi hash khác nhau mặc dù cùng password
- Slow-by-design: Chặn brute-force
- Industry standard: OWASP recommend

**Misconception:**

- ❌ "MD5 nhanh nên tốt hơn" → Sai, BCrypt chậm là feature
- ❌ "Hash password là encryption" → Sai, hash không thể reverse
- ❌ "Lưu plain-text password" → Sai, rủi ro bảo mật cao

**Câu hỏi phỏng vấn:**

> "Làm sao verify login nếu password không thể reverse-hash?"

**Câu trả lời:**

> "Dùng `passwordEncoder.matches(rawPassword, hashedPassword)`:
>
> 1. Take raw password từ login request
> 2. Hash nó cùng công thức
> 3. Compare với stored hash
> 4. Nếu match → password đúng"

## Stateless JWT Architecture (Kiến trúc Xác thực Phi trạng thái)

### 1. Bản chất vấn đề

Trong mô hình Session truyền thống, Server cấp cho Client một `session_id` (Cookie) và Server phải duy trì một bảng băm (Hash Table) trên RAM (hoặc Redis) để nhớ xem `session_id` này thuộc về ai. Khi hệ thống mở rộng (Scale Out) lên nhiều server chạy song song, việc đồng bộ RAM giữa các server trở thành nút thắt cổ chai (Bottleneck).
Kiến trúc **Stateless JWT (JSON Web Token)** giải quyết triệt để bài toán này. Server không cần nhớ ai đã đăng nhập. Mọi thông tin (Email, Role, Expiration Time) đều được đóng gói trực tiếp vào chính Token. Khi Request bay đến bất kỳ Server nào, Server đó chỉ cần dùng Secret Key để giải mã và xác thực tính vẹn toàn (Integrity) của Token mà không cần query Database.

### 2. Triển khai trong Dự án

Hệ thống sử dụng cơ chế Token Kép (Dual-Token Mechanism):

- **Access Token (Ngắn hạn - 15 phút):** Chỉ chứa các Claim cơ bản, bay đi bay về liên tục trong Header của mọi Request.
- **Refresh Token (Dài hạn - 7 ngày):** Lưu ngầm trong Database (`refresh_tokens` table) và Cookie/LocalStorage. Chỉ bay lên Server 1 lần duy nhất khi Access Token hết hạn để xin cấp mới. Nó cung cấp cơ chế **Revocation (Thu hồi)**: Admin có thể khóa tài khoản, ép `revoked = true`, lập tức Refresh Token vô tác dụng, kẻ gian không thể xin thêm Access Token mới.

---

## Spring Security Filter Chain (Chuỗi màng lọc Bảo mật)

### 1. Khái niệm

Spring Security không can thiệp trực tiếp vào Controller của bạn. Thay vào đó, nó giăng ra một bức tường gồm nhiều lớp lưới lọc (Filters) đứng trước DispatcherServlet. Mọi Request từ ngoài Internet đi vào đều phải đi qua hệ thống ống nước (Pipeline) này. Nếu một Filter phát hiện dấu hiệu xâm nhập hoặc thiếu quyền, nó sẽ đánh bật Request ra ngoài ngay lập tức (Ném Exception) trước khi Request kịp chạm vào Code nghiệp vụ (Controller/Service).

### 2. Tùy chỉnh Filter trong Dự án (JwtAuthenticationFilter)

Dự án chèn thêm `JwtAuthenticationFilter` vào trước `UsernamePasswordAuthenticationFilter` mặc định của Spring.

```java
// Logic cốt lõi của JwtAuthenticationFilter
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        if (jwtUtil.validateToken(token)) {
            String email = jwtUtil.extractEmail(token);
            // Query DB lấy Role mới nhất của User (Chống lộ lọt quyền cũ)
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

            // Ép thẻ hành nghề (Authentication) vào tay Request hiện tại
            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // Lưu vào Context của luồng (ThreadLocal)
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }
    filterChain.doFilter(request, response); // Chuyển cho Filter tiếp theo
}
```

---

## SecurityContextHolder & ThreadLocal

### 1. Bản chất vấn đề

Trong môi trường Web (Servlet), mỗi Request của User (Client) bay tới sẽ được Tomcat cấp phát một Luồng xử lý độc lập (Thread). Hàng ngàn Request bay tới cùng lúc là hàng ngàn Threads chạy song song.
Câu hỏi: Làm sao các class ở tầng sâu (Service, Repository) có thể biết được Thread hiện tại đang chạy đại diện cho User nào, mà không cần phải truyền biến `User` lằng nhằng qua từng tham số hàm (`method(User u, String param)`)?

### 2. ThreadLocal và SecurityContext

Spring Security giải bài toán này bằng **ThreadLocal** - Một kho lưu trữ bộ nhớ đặc biệt, nơi dữ liệu chỉ có thể được nhìn thấy và truy cập bởi chính Thread đã tạo ra nó.
Khi `JwtAuthenticationFilter` (chạy trên Thread A) xác thực Token thành công, nó cất thẻ định danh vào két sắt của Thread A thông qua: `SecurityContextHolder.getContext().setAuthentication(auth)`.
Khi code chạy sâu xuống `LearningServiceImpl` (vẫn đang ở Thread A), ta chỉ cần gõ:

```java
String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
```

Spring tự động mở két sắt của Thread A, lấy ra email. Thread B kế bên gọi dòng code y hệt sẽ lấy ra két sắt của Thread B, dữ liệu hoàn toàn cô lập (Thread-Safe).

---

## Bắt Lỗi Bảo Mật Khéo Léo (Security Exception Handling)

### 1. Sự khác biệt giữa 401 và 403

- **401 Unauthorized (Chưa định danh):** Xảy ra ở vòng gửi xe. Hệ thống từ chối vì bạn không có Thẻ ra vào (Token), hoặc Thẻ đã hết hạn, hoặc Thẻ giả.
- **403 Forbidden (Sai thẩm quyền):** Xảy ra khi đã qua vòng gửi xe. Hệ thống nhận diện đúng bạn là học sinh (Student), nhưng bạn lại cố tình đá cửa xông vào phòng họp của Hội đồng Quản trị (Admin API). Hệ thống chặn lại vì bạn không đủ thẩm quyền (Authority).

### 2. Triển khai trong Dự án

Vì Spring Security Filter chạy _trước_ Controller, nếu xảy ra lỗi 401/403 ở đây, lỗi này sẽ không bị tóm bởi `@RestControllerAdvice` (GlobalExceptionHandler) như các lỗi Logic thông thường. Nếu để yên, Spring sẽ trả về một file HTML báo lỗi rất xấu xí.
Dự án đã giải quyết bằng cách định nghĩa 2 điểm đánh chặn (Interceptors):

- **JwtAuthenticationEntryPoint:** Chặn và ép kiểu lỗi 401 về định dạng JSON `ApiResponse`.
- **CustomAccessDeniedHandler:** Chặn và ép kiểu lỗi 403 về định dạng JSON `ApiResponse`.

```java
// Bơm vào cấu hình SecurityConfig
http.exceptionHandling(ex -> ex
    .authenticationEntryPoint(jwtAuthenticationEntryPoint) // Bắt lỗi 401 (Chưa đăng nhập)
    .accessDeniedHandler(customAccessDeniedHandler)        // Bắt lỗi 403 (Sai phân quyền)
);
```

---

## Method Security (@PreAuthorize) vs HttpSecurity Config

### 1. Khái niệm

Phân quyền là việc lập rào chắn bảo vệ API. Spring Security cung cấp 2 cách để đặt rào:

- **HttpSecurity (Bảo vệ theo URL Path):** Đặt rào ngay từ cổng Filter. `requestMatchers("/api/admin/**").hasRole("ADMIN")`. Ưu điểm: Tập trung, dễ nhìn, hiệu suất cao (Request bị đá văng từ ngoài ngõ).
- **Method Security (Bảo vệ theo Hành vi):** Đặt rào ngay trên đỉnh của Method bằng `@PreAuthorize`. Ưu điểm: Phân quyền cực mịn, có thể dùng biểu thức SpEL (Sping Expression Language) để kiểm tra logic phức tạp.

### 2. Ứng dụng nâng cao bằng SpEL

Trong khi HttpSecurity chỉ chặn cứng đường dẫn, `@PreAuthorize` cho phép đọc ngược thông số từ tham số truyền vào hàm (Method Arguments) để quyết định cho qua hay không.

```java
// Ví dụ: Chỉ cho phép người dùng xem thông tin Đơn hàng CỦA CHÍNH HỌ
@PreAuthorize("hasRole('ADMIN') or #orderUserId == authentication.principal.id")
@GetMapping("/orders/{orderUserId}")
public OrderDTO getOrderDetails(@PathVariable Long orderUserId) {
    ...
}
```

Nhờ SpEL, rào chắn này trở nên "thông minh": Admin thì qua tự do, nhưng User thường thì chỉ qua được nếu `orderUserId` trên URL trùng khớp với `id` của Token đang nắm giữ (Tuyệt chiêu chống IDOR ngay tại Controller).

## JPA Cascade & Orphan Removal

### Giải thích ngắn gọn

Là các cấu hình trong Spring Data JPA giúp tự động hóa việc đồng bộ dữ liệu giữa bảng cha và bảng con. Cascade giúp thao tác (Thêm/Sửa/Xóa) lan truyền từ cha xuống con. Orphan Removal dọn dẹp các dữ liệu rác không còn được tham chiếu.

### Ví dụ trong project này

Entity `Course` (Cha) có danh sách `CourseSection` (Con). Khi cấu hình `cascade = CascadeType.ALL, orphanRemoval = true`: Nếu Admin xóa 1 Course, tất cả Section của nó bị xóa. Nếu Admin xóa 1 phần tử Section ra khỏi `List<CourseSection>`, Section đó cũng tự bay khỏi database.

### Câu hỏi phỏng vấn liên quan

Phân biệt CascadeType.REMOVE và orphanRemoval = true?

### Câu trả lời ngắn gọn

`CascadeType.REMOVE` chỉ xóa con khi cha bị xóa. `orphanRemoval = true` bao gồm cả tính năng trên, và còn xóa luôn con nếu con bị gỡ khỏi danh sách của cha (bị cắt đứt quan hệ).

---

## Lombok Infinite Recursion (Lỗi đệ quy Lombok)

### Giải thích ngắn gọn

Lỗi xảy ra khi hai Object tham chiếu chéo nhau (Quan hệ 2 chiều) và cố gắng in nội dung của nhau ra (thông qua hàm `toString()` hoặc `equals()/hashCode()`), tạo thành vòng lặp vô hạn gây tràn bộ nhớ (StackOverflow).

### Ví dụ trong project này

`Lesson` mapping đến `Course`. `Course` lại chứa `List<Lesson>`. Khi in `Course`, Lombok gọi in `Lesson`, `Lesson` lại gọi in `Course`... Để tránh, ta dùng `@ToString.Exclude` ở thuộc tính `course` trong entity `Lesson`.

### Câu hỏi phỏng vấn liên quan

Tại sao lại bị StackOverflow khi dùng @Data của Lombok trong entity có quan hệ `@OneToMany`?

### Câu trả lời ngắn gọn

Vì `@Data` tự động generate `@ToString` và `@EqualsAndHashCode`. Hai entity cha con gọi qua lại các hàm này tạo thành vòng lặp vô hạn. Cần đổi sang dùng `@Getter`, `@Setter` hoặc dùng `@ToString.Exclude` để ngắt vòng lặp.

---

## Spring SecurityContextHolder

### Giải thích ngắn gọn

Là nơi lưu trữ trung tâm của Spring Security, chứa thông tin chi tiết về ngữ cảnh bảo mật hiện tại của ứng dụng, bao gồm cả thông tin về người dùng (Principal) đang tương tác với hệ thống trong luồng xử lý request hiện tại (ThreadLocal).

### Ví dụ trong project này

Khi Admin gửi một request kèm JWT Token hợp lệ đến endpoint `POST /api/v1/admin/courses`, tầng Filter sẽ xác thực token và lưu thông tin Admin đó vào `SecurityContextHolder`. Tại tầng Service, ta gọi `SecurityContextHolder.getContext().getAuthentication().getPrincipal()` để lấy ra ID của Admin và gán vào trường `teacher` của khóa học một cách tự động.

### Câu hỏi phỏng vấn liên quan

Làm sao ứng dụng phân biệt được dữ liệu Security của các request chạy đồng thời?

### Câu trả lời ngắn gọn

Mặc định Spring Security sử dụng chiến lược lưu trữ `MODE_THREADLOCAL`. Nghĩa là mỗi request đi vào hệ thống sẽ được xử lý trên một Thread riêng biệt, và thông tin bảo mật trong `SecurityContextHolder` được gắn chặt vào Thread đó, đảm bảo dữ liệu cô lập tuyệt đối giữa các người dùng.

---

## JPA @EntityGraph

### Giải thích ngắn gọn

Là một tính năng của JPA (được Spring Data hỗ trợ qua annotation) giúp định nghĩa một giải pháp nạp dữ liệu một cách linh hoạt tại thời điểm chạy (runtime), chỉ định chính xác các thuộc tính liên kết nào cần được nạp ngay lập tức (`FETCH`) bằng cách sinh câu lệnh SQL `LEFT JOIN`.

### Ví dụ trong project này

Trong `CourseRepository`, ta khai báo:

```java
@EntityGraph(attributePaths = {"teacher"})
Page<Course> findAll(Pageable pageable);
```

Khi gọi hàm này, thay vì chạy 1 câu lệnh select lấy danh sách Course rồi lặp qua từng phần tử chạy tiếp N câu lệnh để lấy thông tin Giáo viên, Hibernate sẽ sinh ra duy nhất 1 lệnh SQL JOIN giữa bảng courses và users để kéo toàn bộ dữ liệu về cùng một lúc.

### Câu hỏi phỏng vấn liên quan

Sự khác biệt giữa @EntityGraph và từ khóa FETCH JOIN trong JPQL là gì?

### Câu trả lời ngắn gọn

Cả hai đều giải quyết lỗi N+1 Query thông qua SQL JOIN. Tuy nhiên, FETCH JOIN yêu cầu viết truy vấn tĩnh bằng chuỗi JPQL (@Query), còn @EntityGraph linh hoạt hơn, có thể khai báo đè trực tiếp lên các phương thức có sẵn của Spring Data JPA (như findAll, findById) mà không cần viết lại câu truy vấn.

---

## Multi-level Data Isolation (Cô lập dữ liệu đa cấp)

### 1. Định nghĩa và Bản chất

Data Isolation trong ứng dụng đa khách hàng (multi-tenant) hoặc đa người dùng (như nền tảng giáo dục) là kỹ thuật phân chia ranh giới vật lý hoặc logic, đảm bảo Dữ liệu của thực thể A không thể bị truy xuất hoặc chỉnh sửa bởi thực thể B nếu không có quyền.
Trong kiến trúc phần mềm, điều này bảo vệ hệ thống khỏi lỗ hổng IDOR (Insecure Direct Object References).

### 2. Triển khai trong Dự án (Code Thực Tế)

Trong `LessonAdminServiceImpl`, hệ thống không bao giờ tin tưởng `id` truyền từ Frontend. Khi nhận một request sửa/xóa bài học, thay vì chỉ tìm `Lesson` và xóa, nó thực thi chuỗi truy ngược phân cấp (Upward Traversal):

```java
// Trong LessonAdminServiceImpl
private void checkDataIsolation(Course course) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String currentUserEmail = auth.getName();

    // Kiểm tra role: Nếu là ADMIN/SUPER_ADMIN thì được bypass
    boolean isAdminOrSuperAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));

    if (!isAdminOrSuperAdmin) {
        // Lấy Teacher sở hữu khóa học và đối chiếu email với Token hiện tại
        if (!course.getTeacher().getEmail().equals(currentUserEmail)) {
            throw new AppException(ErrorCode.DATA_ISOLATION_FORBIDDEN); // Bắn lỗi 403
        }
    }
}
```

**Luồng đi:** `Lesson` -> `.getSection()` -> `.getCourse()` -> `.getTeacher()` -> `.getEmail()` === `currentUserEmail`. Bất cứ mắt xích nào đứt gãy hoặc sai lệch, hành động sẽ bị chặn đứng (403 Forbidden).

---

## MultipleBagFetchException (Lỗi Nạp Nhiều Túi Dữ Liệu)

### 1. Bản chất vấn đề

Đây là cơn ác mộng thường gặp nhất khi tối ưu hóa hiệu suất truy vấn bằng Hibernate. Khi bạn thiết kế các quan hệ `@OneToMany` lồng nhau (VD: 1 Course có nhiều Sections, 1 Section có nhiều Lessons) và sử dụng kiểu `java.util.List`.
Khái niệm `Bag` trong Hibernate ám chỉ một tập hợp (Collection) không có trật tự và cho phép phần tử trùng lặp (giống y hệt `List` của Java).
Nếu dùng `@EntityGraph` hoặc `FETCH JOIN` để kéo cả Course, Section, và Lesson trong cùng 1 câu lệnh SQL, DB sẽ sinh ra Tích Đề-các (Cartesian Product). Hibernate nhận về một bảng kết quả khổng lồ chứa hàng trăm dòng lặp lại của cùng 1 Course. Vì `Bag` cho phép trùng lặp, Hibernate sợ rằng nếu nó tự ý lọc đi các phần tử trùng lặp, nó sẽ làm mất dữ liệu của lập trình viên, nên nó chọn cách ném Exception: `MultipleBagFetchException`.

### 2. Cách giải quyết trong Dự án

Sử dụng `java.util.Set` (cụ thể là `LinkedHashSet`) để thay thế cho `List`.

```java
// Trong entity Course
@OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
private Set<CourseSection> sections = new LinkedHashSet<>(); // Set giải quyết Cartesian Product

// Trong entity CourseSection
@OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
private Set<Lesson> lessons = new LinkedHashSet<>();
```

- **Tại sao lại là Set?** Theo toán học, Set KHÔNG cho phép phần tử trùng lặp. Hibernate dựa vào điều này để dùng hàm `.equals()` lọc bỏ hoàn toàn các dòng lặp dư thừa sinh ra từ SQL JOIN, ánh xạ hoàn hảo thành một Object Tree sạch sẽ trên RAM.
- **Tại sao lại là LinkedHashSet?** Nếu dùng `HashSet` thường, thứ tự Section/Lesson sẽ lộn xộn. `LinkedHashSet` bảo tồn thứ tự chèn dữ liệu, kết hợp với trường `sortOrder` giúp API trả về danh sách bài học có thứ tự chính xác.

---

## Fail-Fast Principle (Nguyên tắc Thất bại nhanh)

### 1. Khái niệm

Fail-Fast là thiết kế hàm/thuật toán báo lỗi và ngắt luồng (throw Exception) ngay từ những dòng code đầu tiên khi phát hiện một điều kiện đầu vào không hợp lệ. Điều này ngăn chặn việc hệ thống tiếp tục chạy những đoạn code tiêu tốn tài nguyên (CPU, RAM, Network, Database) một cách vô ích.

### 2. Ứng dụng trong Ghi Danh Khóa Học (Course Enrollment)

Thay vì load hết thông tin, query kiểm tra đủ kiểu rồi mới gộp lại xử lý, hàm `enrollFreeCourse` từ chối phục vụ từng bước một (từ rẻ tới đắt):

```java
// 1. Kiểm tra RAM (Rẻ) - Ném lỗi ngay nếu khóa học chưa xuất bản
if (course.getStatus() != CourseStatus.PUBLISHED) {
    throw new AppException(ErrorCode.COURSE_NOT_AVAILABLE_FOR_ENROLLMENT);
}

// 2. Kiểm tra RAM (Rẻ) - Ném lỗi ngay nếu là khóa học trả phí
if (course.getCourseType() != CourseType.FREE) {
    throw new AppException(ErrorCode.COURSE_CANNOT_ENROLL_PAID);
}

// 3. Gọi Database (Đắt) - Ném lỗi nếu đã tồn tại bản ghi Ghi danh
if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
    throw new AppException(ErrorCode.USER_ALREADY_ENROLLED);
}
```

---

## Check-Then-Act & Race Condition (Xung đột trạng thái đa luồng)

### 1. Bản chất rủi ro

Trong các hệ thống phân tán, nếu nhiều request (vd: người dùng bấm liên tục nút Ghi danh 10 lần) chạy song song qua đoạn code `Check-Then-Act` (Kiểm tra xem chưa có -> Mới thêm vào), tất cả 10 luồng đều vượt qua vòng kiểm tra (vì tại tích tắc đó DB chưa kịp lưu). Hậu quả: 10 bản ghi Ghi danh được sinh ra cho cùng 1 user và 1 course, gây rác DB và lỗi logic sau này.

### 2. Giải pháp kiên cố ở tầng Database

Không bao giờ giao phó toàn bộ niềm tin cho code Java (Application Layer). Trách nhiệm giữ gìn sự toàn vẹn dữ liệu (Data Integrity) phải được ủy thác xuống mức thấp nhất: Database Layer thông qua Unique Constraint.

```java
// Entity CourseEnrollment
@Table(name = "course_enrollments", uniqueConstraints = {
    // Composite Unique Key chặn đứng Race Condition
    @UniqueConstraint(columnNames = {"user_id", "course_id"})
})
public class CourseEnrollment { ... }
```

Nhờ constraint này, DB Engine sẽ khóa (lock) các thao tác insert trùng lặp. Cho dù 10 luồng Java cùng gọi `.save(enrollment)`, chỉ 1 luồng thành công, 9 luồng còn lại sẽ bị ném `DataIntegrityViolationException` (Mã HTTP 500 hoặc 409).

---

## Upsert Pattern (Cập nhật hoặc Thêm mới)

### 1. Định nghĩa Upsert

Upsert = Update + Insert. Là mô hình xử lý một cục dữ liệu được gửi tới: Hệ thống tự đánh giá xem cần Ghi đè (Update) lên bản ghi cũ hay Tạo mới (Insert) bản ghi đầu tiên, giúp Frontend không cần phải gọi 2 API riêng biệt (API POST để tạo, API PUT để sửa).

### 2. Code Pattern trong JPA (Sử dụng Optional)

Trong Spring Data JPA, `save()` tự động đóng vai trò Upsert. Tuy nhiên để làm mịn luồng logic, chúng ta kết hợp nó với `.orElse()` của Optional:

```java
// Trong LearningServiceImpl.java (updateProgress)
// Lấy ra bản ghi tiến độ CŨ, hoặc TẠO MỚI bản ghi RỖNG nếu chưa có
LessonProgress progress = progressRepository.findByUserIdAndLessonId(user.getId(), lessonId)
        .orElse(LessonProgress.builder() // <-- Nếu không tìm thấy, tạo Entity trên RAM
                .user(user)
                .lesson(lesson)
                .watchedPercent(0.0)
                .isCompleted(false)
                .build());

// Cập nhật giá trị mới lên Entity
progress.setWatchedPercent(newPercent);

// Lưu xuống DB. JPA tự phân giải: Entity cũ -> UPDATE, Entity mới -> INSERT
progressRepository.save(progress);
```

---

## Anti-Downgrade Algorithm (High-water mark / Bảo toàn đỉnh)

### 1. Ngữ cảnh

Một học viên đang xem video đến phút 10 (tương đương 50% tiến độ). Do chưa hiểu, họ kéo thanh timeline ngược lại phút thứ 2. Lúc này Frontend báo về API tiến độ hiện tại là `10%`.
Nếu Backend dùng toán tử gán `=`, tiến độ của học viên sẽ bị tụt dốc thê thảm từ 50% về 10%, gây ức chế trải nghiệm học tập và đánh dấu sai tiến trình.

### 2. Thuật toán xử lý

Thuật toán Anti-Downgrade chỉ chấp nhận việc ghi đè trạng thái nếu trạng thái mới mang giá trị TÍCH CỰC HƠN (lớn hơn) trạng thái đang có.

```java
// Code bảo vệ trong LearningServiceImpl
// Chỉ cập nhật WatchedPercent nếu giá trị mới LỚN HƠN giá trị đang lưu trong DB
if (req.getWatchedPercent() != null && req.getWatchedPercent() > progress.getWatchedPercent()) {
    progress.setWatchedPercent(req.getWatchedPercent());
}
```

Nhờ lớp giáp logic này, dù học viên tua đi xem lại hàng ngàn lần ở các mốc thời gian cũ, tiến độ cao nhất (High-water mark) luôn được đóng băng bảo vệ.

## IDOR (Insecure Direct Object Reference)

### Giải thích ngắn gọn

Là một lỗ hổng bảo mật kiểm soát truy cập (Access Control Vulnerability). Xảy ra khi ứng dụng cung cấp quyền truy cập trực tiếp vào các đối tượng (như database records, files) dựa trên dữ liệu đầu vào của người dùng cung cấp (thường là các ID) mà không có cơ chế xác thực quyền sở hữu hợp lệ.

### Ví dụ trong project này

Thay vì thiết kế API `GET /users/{id}/progress`, ta dùng `GET /users/me/progress`. Chữ `me` được phân giải an toàn ở tầng Server thông qua JWT Token thay vì tin tưởng vào dữ liệu Client gửi lên.

### Ví dụ code dễ nhớ

```java
// Rủi ro IDOR: user có thể thử đổi id trên URL để xem dữ liệu người khác
@GetMapping("/users/{userId}/progress")
public List<ProgressRes> getProgress(@PathVariable Long userId) {
    return progressService.getProgressByUserId(userId);
}

// An toàn hơn: backend lấy user hiện tại từ SecurityContext/JWT
@GetMapping("/users/me/progress")
public List<ProgressRes> getMyProgress(Authentication authentication) {
    String email = authentication.getName();
    return progressService.getProgressByEmail(email);
}
```

### Điểm cần nhớ khi phỏng vấn

IDOR không phải lỗi "không login", mà là lỗi "login rồi nhưng truy cập nhầm dữ liệu của người khác". Vì vậy chỉ kiểm tra user đã đăng nhập là chưa đủ. Backend phải kiểm tra quyền sở hữu object, ví dụ progress này có thuộc về user hiện tại không, enrollment này có thuộc về student hiện tại không, hoặc user hiện tại có role admin không.

### Misconception hay gặp

- Nghĩ rằng ẩn nút trên frontend là đủ bảo mật. Sai, vì attacker có thể gọi API trực tiếp bằng Postman hoặc DevTools.
- Nghĩ rằng dùng UUID thay vì id số là hết IDOR. UUID chỉ làm khó đoán hơn, nhưng backend vẫn phải kiểm tra quyền.
- Nghĩ endpoint admin và endpoint student có thể dùng chung nếu frontend tự lọc. Sai, dữ liệu nhạy cảm phải được lọc ở backend.

---

## Data Aggregation (Tổng hợp dữ liệu / BFF Pattern)

### Giải thích ngắn gọn

Là quá trình thu thập, xử lý và tóm tắt dữ liệu từ nhiều bảng hoặc nguồn khác nhau trên Server, sau đó đóng gói lại thành một cục dữ liệu (JSON) duy nhất và tối ưu nhất để trả về cho Frontend hiển thị. Tránh việc Client phải thực hiện nhiều lời gọi mạng lẻ tẻ.

### Ví dụ trong project này

Màn Admin Dashboard cần nhiều dữ liệu cùng lúc: tổng số user, tổng số khóa học, tổng số lesson, tổng enrollment, danh sách user mới và khóa học mới. Nếu frontend tự gọi 5-6 API riêng rồi ghép lại, component sẽ phình to và thời gian tải dễ bị kéo dài. Backend có thể gom các query này trong `AdminDashboardServiceImpl` rồi trả về một DTO như `AdminDashboardRes`.

```java
public AdminDashboardRes getDashboardStats() {
    long totalUsers = userRepository.count();
    long totalCourses = courseRepository.count();
    long totalLessons = lessonRepository.count();
    long totalEnrollments = enrollmentRepository.count();

    return AdminDashboardRes.builder()
            .totalUsers(totalUsers)
            .totalCourses(totalCourses)
            .totalLessons(totalLessons)
            .totalEnrollments(totalEnrollments)
            .recentUsers(getRecentUsers())
            .recentCourses(getRecentCourses())
            .build();
}
```

### Lợi ích/Khi nào dùng

- Dùng cho các màn dashboard, profile summary, homepage, course detail, nơi UI cần nhiều mảnh dữ liệu để render một màn hình.
- Giảm round-trip network từ frontend tới backend.
- Giữ business rule tính toán ở backend, ví dụ "course published là gì", "progress tính thế nào".
- Giúp frontend đơn giản hơn vì chỉ nhận một response đã đúng shape để hiển thị.

### Lưu ý

Data Aggregation không có nghĩa là nhét mọi thứ vào một API khổng lồ. Nếu response quá nặng hoặc dữ liệu không phải lúc nào cũng cần, nên tách phần ít dùng thành API riêng hoặc lazy load.

---

## Frontend Foundation

### Giải thích ngắn gọn

Là quá trình dựng nền tảng frontend sao cho ứng dụng có thể chạy ổn định, route hoạt động đúng, state được tách rõ, và API client dễ mở rộng cho các màn hình sau này.

### Ví dụ trong project này

Trong dự án này, frontend foundation bao gồm việc tạo cấu trúc thư mục theo kiểu `src/pages`, `src/router`, `src/stores`, `src/services`, và dùng Axios để gọi `http://localhost:8080/api`.

### Câu hỏi phỏng vấn liên quan

- Tại sao cần tách router, store và service ra riêng?
- Axios interceptor có lợi ích gì?
- Pinia khác gì so với Vuex?

### Câu trả lời ngắn gọn

Router, store và service nên tách riêng để dễ bảo trì, dễ test và dễ mở rộng. Interceptor giúp gom logic token và lỗi API ở một chỗ. Pinia là cách quản lý state hiện đại và dễ dùng hơn trong Vue 3.

### Cấu trúc nên nhớ

```text
src/
  pages/       // Màn hình lớn theo route: LoginPage, AdminDashboardPage
  components/  // UI nhỏ có thể tái sử dụng: CourseCard, Modal, Button
  router/      // Khai báo route và navigation guards
  stores/      // Pinia store: auth, user, cart, learning
  services/    // Gọi API: auth.service.js, course.service.js
  utils/       // Hàm tiện ích thuần: formatDate, formatCurrency
```

### Điểm cần nhớ khi phỏng vấn

Frontend foundation tốt là nền để project lớn không bị rối. Page nên tập trung vào điều phối UI, service chịu trách nhiệm giao tiếp API, store giữ state dùng chung, router quyết định điều hướng. Khi chia đúng trách nhiệm, việc debug sẽ nhanh hơn: lỗi route thì tìm router, lỗi token thì tìm store/interceptor, lỗi endpoint thì tìm service.

## Axios Interceptors (Bộ đánh chặn Axios)

### Giải thích ngắn gọn
Là các hàm được chạy ngầm trước khi một request được gửi đi (Request Interceptor) hoặc trước khi hàm `.then()/.catch()` nhận được dữ liệu trả về (Response Interceptor).

### Ví dụ trong project này
- **Request Interceptor:** Trước khi Frontend gọi API lấy danh sách khóa học, hệ thống tự động móc Access Token từ Pinia và gắn vào header `Authorization: Bearer <token>`.
- **Response Interceptor:** Chặn lỗi 401 để tự động gọi API Refresh Token và retry request ban đầu.

### Ví dụ code dễ nhớ

```javascript
api.interceptors.request.use((config) => {
  const token = authStore.accessToken

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      authStore.logout()
      router.push('/login')
    }

    return Promise.reject(error)
  }
)
```

### Điểm cần nhớ khi phỏng vấn

Interceptor giúp gom logic lặp lại ở một chỗ. Nếu không có interceptor, mỗi service phải tự gắn token và tự xử lý lỗi 401, rất dễ thiếu sót. Tuy nhiên, interceptor cũng cần cẩn thận để tránh vòng lặp vô hạn khi refresh token fail hoặc khi chính request refresh token cũng bị 401.

---

## Vite Proxy

### Giải thích ngắn gọn
Là một tính năng của Vite Dev Server, hoạt động như một trạm trung chuyển (Reverse Proxy). Nó nhận request từ trình duyệt (ví dụ: `http://localhost:5173/api/users`), và chuyển tiếp nó đến Backend (ví dụ: `http://localhost:8080/api/users`). 

### Lợi ích
Giúp giải quyết triệt để lỗi CORS (Cross-Origin Resource Sharing) ở môi trường phát triển (Development) mà không cần cấu hình trên Backend.

### Ví dụ cấu hình

```javascript
// vite.config.js
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

### Điểm cần nhớ khi phỏng vấn

Proxy của Vite chỉ chạy ở môi trường development. Khi deploy production, request sẽ đi qua domain thật, reverse proxy như Nginx, hoặc cấu hình CORS/backend tương ứng. Vì vậy không nên hiểu Vite proxy là giải pháp bảo mật; nó chủ yếu giúp quá trình dev frontend-backend mượt hơn.

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

### Ví dụ code dễ nhớ

```javascript
router.beforeEach((to) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return {
      path: '/login',
      query: { redirect: to.fullPath }
    }
  }

  if (to.meta.role && !authStore.hasRole(to.meta.role)) {
    return '/403'
  }
})
```

### Điểm cần nhớ khi phỏng vấn

Navigation guard giúp bảo vệ trải nghiệm điều hướng trên frontend, nhưng không phải lớp bảo mật cuối cùng. Backend vẫn phải validate token, role và quyền sở hữu dữ liệu. Câu trả lời tốt khi phỏng vấn là: "Em dùng guard để user không vào nhầm màn hình, còn API thật vẫn được bảo vệ bằng Spring Security ở backend."

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

### Ví dụ code dễ nhớ

```javascript
const isLoading = ref(false)
const errorMessage = ref('')

onMounted(async () => {
  isLoading.value = true
  errorMessage.value = ''

  try {
    const [statsRes, coursesRes] = await Promise.all([
      StudentService.getDashboardStats(),
      StudentService.getMyCourses()
    ])

    stats.value = statsRes.data.result
    courses.value = coursesRes.data.result
  } catch (error) {
    errorMessage.value = 'Không thể tải dữ liệu dashboard'
  } finally {
    isLoading.value = false
  }
})
```

### Lưu ý

`Promise.all()` phù hợp khi các request độc lập nhau. Nếu request thứ hai cần dữ liệu từ request thứ nhất, ví dụ cần `courseId` lấy từ API đầu tiên, thì phải gọi tuần tự. Ngoài ra, `Promise.all()` sẽ reject ngay nếu một promise fail; nếu muốn API nào thành công vẫn hiển thị phần đó, có thể dùng `Promise.allSettled()`.

## Role-Based Access Control ở Frontend

### Giải thích ngắn gọn

Role-Based Access Control (RBAC) ở frontend là cách kiểm tra role của user để quyết định họ được vào route nào hoặc nhìn thấy menu nào. Trong Vue, phần này thường được đặt trong router guard.

### Ví dụ trong project này

Route `/admin/dashboard` có `meta: { requiresAuth: true, role: 'ADMIN' }`. Guard đọc `authStore.user.roles` và chỉ cho user có `ADMIN` hoặc `SUPER_ADMIN` đi tiếp.

### Câu hỏi phỏng vấn liên quan

Frontend route guard có đủ để bảo mật API admin không?

### Câu trả lời ngắn gọn

Không đủ. Frontend chỉ chặn điều hướng ở trình duyệt. Backend vẫn phải kiểm tra quyền bằng Spring Security vì API có thể bị gọi trực tiếp.

### Ví dụ code dễ nhớ

```javascript
const routes = [
  {
    path: '/admin/users',
    component: AdminUserManagementPage,
    meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] }
  }
]

router.beforeEach((to) => {
  const authStore = useAuthStore()
  const requiredRoles = to.meta.roles

  if (requiredRoles && !requiredRoles.some(role => authStore.hasRole(role))) {
    return '/403'
  }
})
```

### Điểm cần nhớ khi phỏng vấn

RBAC ở frontend có 2 nhiệm vụ: điều hướng đúng và hiển thị UI đúng. Nó không thay thế RBAC ở backend. Một câu trả lời chắc là: "Frontend guard giúp user thường không thấy màn admin, nhưng backend vẫn là nơi quyết định request có được phép chạy không."

## Mock API Data

### Giải thích ngắn gọn

Mock API data là dữ liệu giả được dùng tạm khi API thật chưa sẵn sàng. Nó giúp frontend tiếp tục dựng UI, kiểm tra layout và xử lý state mà không phải chờ backend.

### Ví dụ trong project này

`admin.service.js` gọi `GET /api/v1/admin/dashboard`. Nếu backend trả 404 vì API chưa được triển khai, service trả mock data gồm `totalUsers`, `totalCourses`, `totalLessons`, `totalEnrollments`, `recentUsers` và `recentCourses`.

### Câu hỏi phỏng vấn liên quan

Vì sao nên đặt mock data ở service thay vì viết trực tiếp trong page component?

### Câu trả lời ngắn gọn

Vì service là nơi quản lý API call. Đặt mock ở service giúp page component không bị trộn logic dữ liệu, sau này thay mock bằng API thật cũng ít phải sửa UI.

### Ví dụ code dễ nhớ

```javascript
async function getDashboardStats() {
  try {
    return await api.get('/api/v1/admin/dashboard')
  } catch (error) {
    if (error.response?.status === 404) {
      return {
        data: {
          result: {
            totalUsers: 0,
            totalCourses: 0,
            totalLessons: 0,
            totalEnrollments: 0,
            recentUsers: [],
            recentCourses: []
          }
        }
      }
    }

    throw error
  }
}
```

### Lưu ý

Mock data chỉ nên là giải pháp tạm thời trong giai đoạn frontend đi trước backend. Khi API thật đã có, cần bỏ mock hoặc giới hạn mock theo môi trường development để tránh production che giấu lỗi backend thật.

## Frontend Service Layer

### Giải thích ngắn gọn

Frontend service layer là lớp file chuyên phụ trách gọi API, ví dụ `auth.service.js`, `student.service.js`, `admin.service.js`. Page/component sẽ gọi service thay vì dùng Axios trực tiếp ở nhiều nơi.

### Ví dụ trong project này

`AdminDashboardPage.vue` gọi `AdminService.getDashboardStats()` để lấy dữ liệu dashboard. Component không cần biết chi tiết endpoint, base URL hay cách xử lý 404 mock data.

### Câu hỏi phỏng vấn liên quan

Lợi ích của việc tách service layer trong frontend là gì?

### Câu trả lời ngắn gọn

Giúp code dễ đọc, dễ tái sử dụng, dễ đổi endpoint, dễ test và tránh việc logic gọi API bị lặp trong nhiều component.

### Ví dụ code dễ nhớ

```javascript
// services/admin.service.js
export const AdminService = {
  getUsers(params) {
    return api.get('/api/v1/admin/users', { params })
  },

  lockUser(id) {
    return api.put(`/api/v1/admin/users/${id}/lock`)
  }
}
```

### Điểm cần nhớ khi phỏng vấn

Component không nên biết quá nhiều về endpoint. Component chỉ cần gọi `AdminService.lockUser(id)`, còn URL, method, params và cách unwrap response nên được gom ở service. Cách này giúp khi backend đổi endpoint, ta sửa một nơi thay vì tìm trong nhiều component.

## Backend Dashboard Aggregation

### Giải thích ngắn gọn

Backend Dashboard Aggregation là cách backend gom nhiều chỉ số và danh sách dữ liệu từ nhiều bảng khác nhau, sau đó trả về một response duy nhất cho màn hình dashboard.

### Ví dụ trong project này

`AdminDashboardServiceImpl` lấy tổng user, course, lesson, enrollment và lấy thêm 5 user/khóa học mới nhất để trả về `AdminDashboardRes`.

### Câu hỏi phỏng vấn liên quan

Tại sao dashboard nên dùng một API tổng hợp thay vì frontend gọi nhiều API lẻ?

### Câu trả lời ngắn gọn

Một API tổng hợp giúp giảm số request, giảm logic ghép dữ liệu ở frontend và giữ cách tính số liệu tập trung ở backend.

### Ví dụ response DTO

```java
public record AdminDashboardRes(
        long totalUsers,
        long totalCourses,
        long totalLessons,
        long totalEnrollments,
        List<RecentUserRes> recentUsers,
        List<RecentCourseRes> recentCourses
) {}
```

### Lưu ý performance

Dashboard thường gọi nhiều query count. Với data nhỏ thì đơn giản, nhưng khi dữ liệu lớn cần cân nhắc index, cache, materialized view hoặc bảng thống kê riêng. Không nên join quá nhiều bảng nặng chỉ để render một dashboard nếu các số liệu có thể tính riêng hiệu quả hơn.

## Spring Data JPA Derived Query Method

### Giải thích ngắn gọn

Derived query method là cách đặt tên method trong Repository để Spring Data JPA tự sinh query dựa trên tên method.

### Ví dụ trong project này

`findTop5ByOrderByCreatedAtDesc()` giúp lấy 5 bản ghi mới nhất theo `createdAt` giảm dần mà không cần viết SQL thủ công.

### Câu hỏi phỏng vấn liên quan

Khi nào nên dùng derived query method?

### Câu trả lời ngắn gọn

Nên dùng khi query đơn giản, dễ đọc qua tên method. Nếu query phức tạp, nhiều điều kiện động hoặc cần tối ưu riêng thì nên dùng `@Query`, Specification hoặc QueryDSL.

### Ví dụ code dễ nhớ

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findTop5ByOrderByCreatedAtDesc();

    Page<User> findByStatus(UserStatus status, Pageable pageable);
}
```

### Misconception hay gặp

- Tên method càng dài càng tốt. Sai, method quá dài như `findByStatusAndRoleNameAndEmailContainingAndCreatedAtBetween...` rất khó đọc.
- Derived query luôn tối ưu. Sai, nó tiện nhưng vẫn cần xem SQL sinh ra khi query ảnh hưởng performance.
- Không cần `@Query`. Sai, `@Query` vẫn cần khi query có join, aggregate, điều kiện động hoặc tối ưu riêng.

## EntityGraph

### Giải thích ngắn gọn

`@EntityGraph` cho phép khai báo các quan hệ cần fetch kèm khi query Entity, giúp tránh việc load quan hệ quá muộn hoặc phát sinh nhiều query nhỏ.

### Ví dụ trong project này

`UserRepository.findTop5ByOrderByCreatedAtDesc()` dùng `@EntityGraph(attributePaths = {"roles"})` để lấy sẵn role của user khi map sang `RecentUserRes`.

### Câu hỏi phỏng vấn liên quan

`@EntityGraph` giúp tránh vấn đề gì trong JPA?

### Câu trả lời ngắn gọn

Nó giúp giảm rủi ro N+1 query và lỗi lazy loading khi service cần dùng dữ liệu từ quan hệ như `roles` hoặc `teacher`.

### Ví dụ code dễ nhớ

```java
public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"roles"})
    List<User> findTop5ByOrderByCreatedAtDesc();
}
```

### Khi nào dùng

`@EntityGraph` hợp khi mặc định quan hệ là `LAZY`, nhưng ở một query cụ thể ta biết chắc cần fetch thêm quan hệ đó. Nó gọn hơn việc đổi entity sang `EAGER`, vì `EAGER` áp dụng mọi lúc và có thể làm query nặng ngoài ý muốn.

## Pagination

### Giải thích ngắn gọn

Pagination là kỹ thuật chia danh sách dữ liệu lớn thành nhiều trang nhỏ. Client gửi `page` và `size`, backend chỉ trả đúng phần dữ liệu cần hiển thị.

### Ví dụ trong project này

API `GET /api/v1/admin/users?page=0&size=10` trả 10 user đầu tiên cùng metadata như `currentPage`, `totalPages`, `totalElements`.

### Câu hỏi phỏng vấn liên quan

Vì sao danh sách user nên dùng pagination?

### Câu trả lời ngắn gọn

Vì số lượng user có thể tăng rất nhiều. Pagination giúp giảm tải database, giảm dung lượng response và giúp UI table chạy mượt hơn.

### Ví dụ code dễ nhớ

```java
@GetMapping("/admin/users")
public Page<UserRes> getUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    return userService.getUsers(pageable);
}
```

### Điểm cần nhớ khi phỏng vấn

Spring Data dùng page index bắt đầu từ `0`, trong khi UI thường hiển thị từ trang `1`. Frontend cần map đúng: khi user bấm trang 1 thì gửi `page=0`. Nếu không thống nhất, UI rất dễ lệch một trang.

## Dynamic Filtering

### Giải thích ngắn gọn

Dynamic filtering là cách cho phép API lọc dữ liệu theo các tham số tùy chọn. Tham số nào không truyền thì bỏ qua điều kiện đó.

### Ví dụ trong project này

`UserRepository.findUsersByCriteria(keyword, status, role, pageable)` cho phép admin lọc user theo keyword, trạng thái và role. Nếu `status` là `null`, query không lọc theo status.

`CourseRepository.searchPublishedCourses(level, courseType, keyword, pageable)` cho phép public course list lọc theo level, loại khóa học và keyword. Query vẫn luôn giữ điều kiện `status = PUBLISHED`.

### Câu hỏi phỏng vấn liên quan

Vì sao cần filter động thay vì tạo nhiều endpoint riêng?

### Câu trả lời ngắn gọn

Filter động giúp API gọn hơn, frontend linh hoạt hơn và tránh phải tạo nhiều endpoint như `/users/by-role`, `/users/by-status`, `/users/search`.

### Ví dụ query dễ nhớ

```java
@Query("""
    SELECT u FROM User u
    WHERE (:keyword IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:status IS NULL OR u.status = :status)
      AND (:role IS NULL OR EXISTS (
          SELECT r FROM u.roles r WHERE r.name = :role
      ))
""")
Page<User> findUsersByCriteria(
        String keyword,
        UserStatus status,
        RoleName role,
        Pageable pageable
);
```

### Lưu ý

Filter động cần thống nhất cách hiểu giá trị rỗng. Frontend nên không gửi param hoặc gửi `null`/chuỗi rỗng theo contract đã định; backend nên parse rõ ràng để tránh lỗi enum, lỗi query hoặc filter sai.

## Enum Query Param Parsing

### Giải thích ngắn gọn

Enum Query Param Parsing là việc nhận query param dạng text từ URL rồi chuyển sang enum backend đang dùng. Nếu giá trị không hợp lệ, backend nên trả lỗi 400 rõ ràng.

### Ví dụ trong project này

`CoursePublicServiceImpl` nhận `level` và `courseType` dạng String, sau đó parse sang `CourseLevel` và `CourseType`. Nếu client gửi `level=INVALID`, service throw `AppException(ErrorCode.INVALID_REQUEST)`.

### Câu hỏi phỏng vấn liên quan

Vì sao không nên để lỗi enum sai trở thành lỗi 500?

### Câu trả lời ngắn gọn

Vì enum sai là lỗi input từ client, không phải lỗi hệ thống. Backend nên trả 400 để frontend biết cần sửa request hoặc hiển thị thông báo hợp lệ.

### Ví dụ code dễ nhớ

```java
private CourseLevel parseLevel(String level) {
    if (level == null || level.isBlank()) {
        return null;
    }

    try {
        return CourseLevel.valueOf(level.toUpperCase());
    } catch (IllegalArgumentException ex) {
        throw new AppException(ErrorCode.INVALID_REQUEST);
    }
}
```

### Điểm cần nhớ khi phỏng vấn

Enum từ URL là dữ liệu bên ngoài hệ thống, nên phải coi là input không tin cậy. Backend nên trả lỗi rõ ràng như `400 Bad Request`, thay vì để exception thô làm thành `500 Internal Server Error`. Lỗi 500 khiến người đọc hiểu nhầm là server hỏng, trong khi thực tế client gửi sai giá trị.

## Public Data Protection

### Giải thích ngắn gọn

Public Data Protection là nguyên tắc backend chỉ trả dữ liệu được phép hiển thị công khai, dù client có truyền query param như thế nào.

### Ví dụ trong project này

API public `GET /api/v1/courses` luôn lọc `CourseStatus.PUBLISHED`, nên các khóa học `DRAFT`, `HIDDEN`, `ARCHIVED` không bị lộ ra ngoài.

### Câu hỏi phỏng vấn liên quan

Vì sao không nên chỉ ẩn course chưa publish ở frontend?

### Câu trả lời ngắn gọn

Vì frontend có thể bị bypass. Điều kiện bảo vệ dữ liệu phải nằm ở backend query để mọi client đều chỉ nhận dữ liệu public hợp lệ.

### Ví dụ code dễ nhớ

```java
@Query("""
    SELECT c FROM Course c
    WHERE c.status = 'PUBLISHED'
      AND (:level IS NULL OR c.level = :level)
      AND (:courseType IS NULL OR c.courseType = :courseType)
""")
Page<Course> searchPublishedCourses(
        CourseLevel level,
        CourseType courseType,
        Pageable pageable
);
```

### Điểm cần nhớ khi phỏng vấn

Public API phải tự bảo vệ dữ liệu public. Dù frontend không hiển thị course `DRAFT`, attacker vẫn có thể gọi endpoint trực tiếp. Vì vậy điều kiện `status = PUBLISHED` nên nằm trong query/service backend, không phụ thuộc vào UI.

## Account Locking

### Giải thích ngắn gọn

Account locking là cơ chế khóa tài khoản bằng cách đổi trạng thái user sang `LOCKED`, thường dùng khi user vi phạm hoặc cần tạm ngưng truy cập.

### Ví dụ trong project này

API `PUT /api/v1/admin/users/{id}/lock` đổi `User.status` thành `LOCKED`. API `unlock` đổi lại thành `ACTIVE`.

### Câu hỏi phỏng vấn liên quan

Tại sao nên lock account bằng status thay vì xóa user?

### Câu trả lời ngắn gọn

Vì lock giữ lại dữ liệu lịch sử và quan hệ database. Xóa user có thể làm mất enrollment, progress hoặc gây lỗi dữ liệu liên quan.

### Ví dụ code dễ nhớ

```java
@Transactional
public UserRes lockUser(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    if (user.hasRole(RoleName.SUPER_ADMIN)) {
        throw new AppException(ErrorCode.CANNOT_LOCK_SUPER_ADMIN);
    }

    user.setStatus(UserStatus.LOCKED);
    return userMapper.toUserRes(user);
}
```

### Lưu ý bảo mật

Lock account nên đi kèm kiểm tra quyền: ai được lock, có được lock admin khác không, có được tự lock chính mình không. Ngoài ra backend login cũng phải kiểm tra `status`; nếu chỉ đổi status trong database nhưng login không kiểm tra thì tài khoản bị lock vẫn có thể đăng nhập.

## Admin Data Table

### Giải thích ngắn gọn

Admin Data Table là bảng dữ liệu dành cho màn quản trị, thường có search, filter, pagination, trạng thái loading/error/empty và các nút thao tác trên từng dòng.

### Ví dụ trong project này

`AdminUserManagementPage.vue` hiển thị bảng user với role, status, email verified, ngày tạo, đăng nhập cuối và nút khóa/mở khóa.

### Câu hỏi phỏng vấn liên quan

Một bảng admin tốt nên có những trạng thái UI nào?

### Câu trả lời ngắn gọn

Nên có loading, error, empty, data state và trạng thái đang xử lý action để user biết hệ thống đang làm gì.

### Các phần thường có trong admin table

- Search keyword để tìm nhanh theo email, tên khóa học hoặc title.
- Filter theo status, role, level hoặc course type.
- Pagination để không tải toàn bộ dữ liệu.
- Sort theo ngày tạo, tên hoặc trạng thái nếu nghiệp vụ cần.
- Action per row như lock/unlock, publish/hide, edit/delete.
- Loading, error, empty state và disabled state cho action đang chạy.

### Điểm cần nhớ khi phỏng vấn

Bảng admin là UI thao tác dữ liệu thật, nên cần rõ ràng hơn trang public. Khi action đang chạy, nên disable nút của dòng đó để tránh double click. Sau khi action thành công, UI nên phản ánh kết quả ngay; nếu thất bại, hiển thị lỗi rõ và không tự ý đổi state.

## Optimistic Row Update

### Giải thích ngắn gọn

Optimistic Row Update là cách cập nhật ngay một dòng trong UI sau khi action thành công, thay vì tải lại toàn bộ danh sách.

### Ví dụ trong project này

Sau khi gọi `AdminService.lockUser(user.id)` thành công, frontend tìm user trong `users` và đổi `status` của user đó thành kết quả backend trả về.

### Câu hỏi phỏng vấn liên quan

Khi nào nên cập nhật row thay vì reload cả table?

### Câu trả lời ngắn gọn

Nên dùng khi action chỉ thay đổi một phần nhỏ dữ liệu và backend đã trả kết quả mới đáng tin cậy. Cách này giúp UI phản hồi nhanh hơn.

### Ví dụ code dễ nhớ

```javascript
async function lockUser(user) {
  processingUserId.value = user.id

  try {
    const res = await AdminService.lockUser(user.id)
    const updatedUser = res.data.result
    const index = users.value.findIndex(item => item.id === user.id)

    if (index !== -1) {
      users.value[index] = updatedUser
    }
  } finally {
    processingUserId.value = null
  }
}
```

### Lưu ý

Optimistic Row Update ở đây thực chất là cập nhật UI sau khi API thành công. Nếu cập nhật trước khi API thành công thì gọi là optimistic UI đúng nghĩa, nhưng phải có rollback khi API fail. Với admin action quan trọng, cập nhật sau response thành công thường an toàn và dễ debug hơn.

## Role-Based UI Guard

### Giải thích ngắn gọn

Role-Based UI Guard là việc ẩn, hiện hoặc disable các nút/chức năng trên frontend dựa theo role của user hoặc role của đối tượng đang được thao tác.

### Ví dụ trong project này

Trang quản lý user không hiển thị nút khóa/mở khóa với tài khoản có role `SUPER_ADMIN`.

### Câu hỏi phỏng vấn liên quan

Role-Based UI Guard có thay thế được kiểm tra quyền ở backend không?

### Câu trả lời ngắn gọn

Không. UI guard chỉ giúp trải nghiệm rõ ràng hơn. Backend vẫn phải kiểm tra quyền thật vì API có thể bị gọi trực tiếp.

### Ví dụ code dễ nhớ

```vue
<button
  v-if="!user.roles.includes('SUPER_ADMIN')"
  :disabled="processingUserId === user.id"
  @click="lockUser(user)"
>
  Khóa
</button>
```

### Điểm cần nhớ khi phỏng vấn

Ẩn nút không phải bảo mật, nhưng vẫn rất cần cho UX. User thường không nên nhìn thấy action mà họ không được phép dùng. Tuy nhiên backend vẫn phải trả `403 Forbidden` nếu request không đủ quyền.

## State Transition

### Giải thích ngắn gọn

State Transition là việc chuyển một đối tượng từ trạng thái này sang trạng thái khác theo rule nghiệp vụ rõ ràng.

### Ví dụ trong project này

Course có thể chuyển từ `DRAFT` hoặc `HIDDEN` sang `PUBLISHED` khi admin gọi API publish, và chuyển sang `HIDDEN` khi admin gọi API hide.

### Câu hỏi phỏng vấn liên quan

Vì sao cần kiểm soát state transition ở backend?

### Câu trả lời ngắn gọn

Vì frontend có thể bị bypass. Backend phải là nơi quyết định trạng thái nào được chuyển, khi nào được chuyển và ai được phép chuyển.

### Ví dụ rule

```text
DRAFT  -> PUBLISHED  // được, nếu course có lesson
HIDDEN -> PUBLISHED  // được, nếu course có lesson
PUBLISHED -> HIDDEN  // được, để tạm ẩn khỏi public
ARCHIVED -> PUBLISHED // thường không nên, nếu archived nghĩa là đã đóng vĩnh viễn
```

### Ví dụ code dễ nhớ

```java
private void ensureCanPublish(Course course) {
    if (course.getStatus() == CourseStatus.ARCHIVED) {
        throw new AppException(ErrorCode.INVALID_COURSE_STATUS);
    }

    if (lessonRepository.countByCourseId(course.getId()) == 0) {
        throw new AppException(ErrorCode.COURSE_CANNOT_PUBLISH_EMPTY);
    }
}
```

### Lưu ý

State transition nên được đặt ở service/backend, không rải ở frontend. Khi rule thay đổi, ví dụ sau này chỉ teacher của course mới được publish, ta sửa ở một nơi và mọi client đều được bảo vệ.

## Publish Validation

### Giải thích ngắn gọn

Publish Validation là các điều kiện cần kiểm tra trước khi cho một nội dung xuất hiện public.

### Ví dụ trong project này

Trước khi publish course, backend kiểm tra khóa học có ít nhất một bài học. Nếu không có bài học, ném lỗi `COURSE_CANNOT_PUBLISH_EMPTY`.

### Câu hỏi phỏng vấn liên quan

Vì sao không nên cho publish khóa học rỗng?

### Câu trả lời ngắn gọn

Vì khóa học public phải có nội dung học tối thiểu. Publish khóa học rỗng làm hỏng trải nghiệm người dùng và giảm chất lượng dữ liệu public.

### Các điều kiện publish thường gặp

- Course phải có title, slug, description hợp lệ.
- Course phải có ít nhất một section hoặc lesson.
- Lesson public phải có nội dung học, video URL hoặc tài liệu cần thiết.
- Giá phải hợp lệ nếu là course trả phí.
- Teacher/owner phải tồn tại và đang active.

### Điểm cần nhớ khi phỏng vấn

Publish là ranh giới giữa dữ liệu nháp và dữ liệu public. Vì vậy validation khi publish thường chặt hơn validation khi save draft. Draft có thể thiếu nội dung để người tạo chỉnh dần, nhưng published content phải đủ chất lượng tối thiểu cho người học.

## Soft Visibility Control

### Giải thích ngắn gọn

Soft Visibility Control là cách ẩn/hiện dữ liệu bằng trạng thái thay vì xóa dữ liệu khỏi database.

### Ví dụ trong project này

API hide course đổi `Course.status` thành `HIDDEN`, nhờ vậy khóa học không hiện public nhưng dữ liệu course, lessons và enrollment vẫn được giữ.

### Câu hỏi phỏng vấn liên quan

Ẩn course khác gì xóa course?

### Câu trả lời ngắn gọn

Ẩn course chỉ thay đổi khả năng hiển thị. Xóa course có thể ảnh hưởng dữ liệu liên quan và khó khôi phục hơn.

### Ví dụ code dễ nhớ

```java
@Transactional
public CourseRes hideCourse(Long courseId) {
    Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

    course.setStatus(CourseStatus.HIDDEN);
    return courseMapper.toCourseRes(course);
}
```

### Khi nào dùng

Soft visibility phù hợp khi dữ liệu vẫn có giá trị lịch sử hoặc có quan hệ với dữ liệu khác. Ví dụ course đã có enrollment thì xóa cứng có thể làm mất lịch sử học tập. Ẩn course giúp public list sạch hơn nhưng admin vẫn quản lý và khôi phục được.

## Spring Page Response Mapping

### Giải thích ngắn gọn

Spring Page Response Mapping là việc frontend đọc đúng cấu trúc phân trang mà Spring Data trả về, thường gồm `content`, `number`, `size`, `totalPages`, `totalElements`.

### Ví dụ trong project này

`AdminCourseManagementPage.vue` lấy danh sách khóa học từ `res.data.result.content` và lấy thông tin phân trang từ `res.data.result.number`, `totalPages`, `totalElements`.

### Câu hỏi phỏng vấn liên quan

Điểm khác nhau giữa `PageResponse` custom và Spring `Page` là gì?

### Câu trả lời ngắn gọn

`PageResponse` custom có field do mình tự định nghĩa như `data`, `currentPage`. Spring `Page` thường dùng `content`, `number`, `totalPages`, `totalElements`. Frontend phải map đúng cấu trúc backend trả.

### Ví dụ frontend mapping

```javascript
const result = res.data.result

courses.value = result.content
pagination.value = {
  currentPage: result.number,
  pageSize: result.size,
  totalPages: result.totalPages,
  totalElements: result.totalElements
}
```

### Lưu ý

Đây là API contract. Nếu backend trả Spring `Page` nhưng frontend lại đọc `result.data`, UI sẽ rỗng dù API thành công. Khi debug lỗi "API có data nhưng UI không hiện", nên kiểm tra response shape đầu tiên.

## Status Badge

### Giải thích ngắn gọn

Status Badge là nhãn UI ngắn, thường có màu, dùng để hiển thị trạng thái của một object trong bảng quản trị.

### Ví dụ trong project này

Course status `DRAFT`, `PUBLISHED`, `HIDDEN`, `ARCHIVED` được hiển thị bằng badge màu khác nhau trong `AdminCourseManagementPage.vue`.

### Câu hỏi phỏng vấn liên quan

Vì sao nên dùng badge cho trạng thái trong admin table?

### Câu trả lời ngắn gọn

Badge giúp người quản trị đọc bảng nhanh hơn, dễ phát hiện object nào đang published, hidden hoặc archived.

### Ví dụ mapping dễ nhớ

```javascript
const statusBadgeClass = {
  DRAFT: 'badge-gray',
  PUBLISHED: 'badge-green',
  HIDDEN: 'badge-yellow',
  ARCHIVED: 'badge-red'
}
```

### Lưu ý UI

Badge nên dùng màu nhất quán trên toàn app. Nếu `PUBLISHED` ở course list là màu xanh thì ở course detail/admin table cũng nên là xanh. Điều này giúp người dùng học được ngôn ngữ giao diện và scan bảng nhanh hơn.

## Feature Scoping

### Giải thích ngắn gọn

Feature Scoping là việc giới hạn phạm vi một task để task đủ nhỏ, dễ code, dễ test và ít rủi ro.

### Ví dụ trong project này

Task Course Management chỉ làm list, pagination, publish/hide/delete. Form create/update được để placeholder "Đang phát triển" và tách sang task riêng vì form khóa học có nhiều field.

### Câu hỏi phỏng vấn liên quan

Vì sao không nên nhồi quá nhiều chức năng vào một task frontend?

### Câu trả lời ngắn gọn

Vì task quá lớn sẽ khó review, khó test, dễ bug và dễ vượt phạm vi. Tách task giúp từng phần rõ ràng và ổn định hơn.

### Ví dụ cách scope hợp lý

```text
Task 1: Admin course list + pagination + publish/hide
Task 2: Course create/update form
Task 3: Course structure: sections + lessons
Task 4: Student course detail + enroll
Task 5: Learning page + progress tracking
```

### Điểm cần nhớ khi phỏng vấn

Feature scoping thể hiện tư duy làm việc thực tế. Không phải làm ít, mà là chia việc sao cho mỗi phần có thể code, review, test và rollback độc lập. Với dự án cá nhân, đây cũng là cách giúp mình không bị đuối khi feature ngày càng lớn.

## Reusable Form Modal

### Giải thích ngắn gọn

Reusable Form Modal là component form dạng popup có thể dùng lại cho nhiều chế độ, thường là tạo mới và cập nhật.

### Ví dụ trong project này

`CourseFormModal.vue` dùng `editingCourse` để biết đang create hay update. Nếu có `editingCourse`, form pre-fill dữ liệu cũ và gọi update API; nếu không có thì gọi create API.

### Câu hỏi phỏng vấn liên quan

Khi nào nên tách form thành component riêng?

### Câu trả lời ngắn gọn

Nên tách khi form có nhiều field, cần dùng lại cho create/update hoặc làm page component quá dài và khó đọc.

### Ví dụ logic create/update

```javascript
const isEditMode = computed(() => Boolean(props.editingCourse))

async function submitForm() {
  const payload = buildCoursePayload()

  if (isEditMode.value) {
    await AdminCourseService.updateCourse(props.editingCourse.id, payload)
  } else {
    await AdminCourseService.createCourse(payload)
  }

  emit('saved')
}
```

### Lưu ý

Reusable không có nghĩa là component phải xử lý mọi trường hợp. Nếu create và update khác nhau quá nhiều, tách thành hai component có thể dễ đọc hơn. Reusable tốt là khi hai mode chia sẻ phần lớn field, validation và layout.

## Props và Emits trong Vue

### Giải thích ngắn gọn

Props là dữ liệu component cha truyền xuống component con. Emits là sự kiện component con gửi ngược lên component cha.

### Ví dụ trong project này

`AdminCourseManagementPage.vue` truyền `editingCourse` vào `CourseFormModal.vue`. Khi lưu thành công, modal emit `saved` để page cha đóng modal và reload danh sách.

### Câu hỏi phỏng vấn liên quan

Vì sao không nên để component con tự reload page cha?

### Câu trả lời ngắn gọn

Vì component con nên độc lập và chỉ báo sự kiện. Component cha mới là nơi sở hữu danh sách course, nên cha quyết định reload hoặc cập nhật dữ liệu.

### Ví dụ code dễ nhớ

```vue
<!-- Parent -->
<CourseFormModal
  :editing-course="editingCourse"
  @saved="handleCourseSaved"
  @close="closeModal"
/>
```

```javascript
// Child
const props = defineProps({
  editingCourse: Object
})

const emit = defineEmits(['saved', 'close'])
```

### Điểm cần nhớ khi phỏng vấn

Props đi từ cha xuống con, emits đi từ con lên cha. Đây là one-way data flow của Vue. Component con không nên tự sửa trực tiếp prop vì prop thuộc quyền sở hữu của cha; nếu cần thay đổi, con emit event để cha quyết định.

## Frontend DTO Mapping

### Giải thích ngắn gọn

Frontend DTO Mapping là việc biến dữ liệu form thành payload đúng cấu trúc backend request DTO yêu cầu.

### Ví dụ trong project này

`CourseFormModal.vue` build payload cho create gồm title, slug, description, level, courseType và price. Khi update thì thêm `status` để khớp `CourseUpdateReq`.

### Câu hỏi phỏng vấn liên quan

Vì sao create payload và update payload không phải lúc nào cũng giống nhau?

### Câu trả lời ngắn gọn

Vì backend có thể đặt rule khác nhau cho create và update. Ví dụ create course mặc định status là `DRAFT`, còn update yêu cầu gửi `status`.

### Ví dụ code dễ nhớ

```javascript
function buildCreatePayload(form) {
  return {
    title: form.title.trim(),
    slug: form.slug.trim(),
    description: form.description.trim(),
    level: form.level,
    courseType: form.courseType,
    price: Number(form.price || 0)
  }
}

function buildUpdatePayload(form) {
  return {
    ...buildCreatePayload(form),
    status: form.status
  }
}
```

### Lưu ý

DTO mapping là nơi chuyển dữ liệu UI thành dữ liệu backend hiểu được. Không nên gửi nguyên object form nếu form có field chỉ phục vụ UI như `isSubmitting`, `previewImage`, `confirmDelete`, vì backend không cần và có thể reject request.

## Dynamic Route Params

### Giải thích ngắn gọn

Dynamic Route Params là tham số động trong URL, dùng để xác định dữ liệu cụ thể cần hiển thị.

### Ví dụ trong project này

Route `/admin/courses/:id/structure` dùng `:id` để biết admin đang quản lý cấu trúc của course nào.

### Câu hỏi phỏng vấn liên quan

Khi nào nên dùng dynamic route params?

### Câu trả lời ngắn gọn

Nên dùng khi một page cần hiển thị dữ liệu theo một object cụ thể như course id, lesson id hoặc user id.

### Ví dụ code dễ nhớ

```javascript
const route = useRoute()
const courseId = computed(() => route.params.id)

onMounted(() => {
  loadCourseStructure(courseId.value)
})
```

### Lưu ý

Dynamic param nên thể hiện đúng định danh mà backend cần. Nếu route dùng `:slug` nhưng API cần `id`, frontend sẽ phải có thêm bước chuyển slug sang id. Điều này không sai, nhưng phải được thiết kế rõ để tránh route đẹp ở frontend nhưng tích hợp API lại rối.

## Lazy Loading Child Data

### Giải thích ngắn gọn

Lazy loading child data là cách chỉ tải dữ liệu con khi người dùng thật sự cần xem, thay vì tải tất cả ngay khi mở page.

### Ví dụ trong project này

`AdminCourseStructurePage.vue` chỉ gọi API lấy lessons khi admin mở một section.

### Câu hỏi phỏng vấn liên quan

Lazy loading lessons có lợi ích gì?

### Câu trả lời ngắn gọn

Nó giảm số request ban đầu, giúp trang tải nhanh hơn và tránh tải dữ liệu không cần thiết nếu admin không mở section đó.

### Ví dụ code dễ nhớ

```javascript
async function toggleSection(section) {
  section.isExpanded = !section.isExpanded

  if (section.isExpanded && !section.lessonsLoaded) {
    section.isLoadingLessons = true

    try {
      const res = await AdminCourseService.getLessons(section.id)
      section.lessons = res.data.result
      section.lessonsLoaded = true
    } finally {
      section.isLoadingLessons = false
    }
  }
}
```

### Điểm cần nhớ khi phỏng vấn

Lazy loading giúp tối ưu initial load, nhưng cần cache trạng thái đã load để không gọi lại API vô ích mỗi lần mở/đóng. Nếu dữ liệu có thể thay đổi sau action create/update/delete lesson, cần invalidate hoặc reload lại section đó.

## Parent-Child UI State

### Giải thích ngắn gọn

Parent-Child UI State là cách quản lý trạng thái dữ liệu cha-con trong frontend, ví dụ course chứa sections, section chứa lessons.

### Ví dụ trong project này

Mỗi section trong `sections` có thêm state UI như `isExpanded`, `isLoadingLessons` và `lessons` để quản lý việc mở/đóng và tải bài học.

### Câu hỏi phỏng vấn liên quan

Vì sao cần thêm state UI như `isExpanded` vào dữ liệu section?

### Câu trả lời ngắn gọn

Vì backend chỉ trả dữ liệu nghiệp vụ, còn frontend cần thêm trạng thái hiển thị để biết section nào đang mở và section nào đang tải lessons.

### Ví dụ state mở rộng ở frontend

```javascript
sections.value = apiSections.map(section => ({
  ...section,
  isExpanded: false,
  isLoadingLessons: false,
  lessonsLoaded: false,
  lessons: []
}))
```

### Lưu ý

Không nên gửi các field UI-only như `isExpanded` hoặc `isLoadingLessons` ngược về backend. Đây là trạng thái hiển thị của trình duyệt, không phải dữ liệu nghiệp vụ. Khi build payload update section, cần chỉ lấy các field backend cần như `title`, `orderIndex`.

## API Response Mapping

### Giải thích ngắn gọn

API Response Mapping là việc frontend đọc đúng cấu trúc response từ backend rồi đưa dữ liệu vào state phù hợp để render UI.

### Ví dụ trong project này

`CourseListPage.vue` lấy danh sách khóa học từ `res.data.result.content`, lấy phân trang từ `res.data.result.number`, `totalPages` và `totalElements`.

### Câu hỏi phỏng vấn liên quan

Điều gì xảy ra nếu frontend map sai response backend?

### Câu trả lời ngắn gọn

UI có thể không hiển thị dữ liệu, pagination sai hoặc báo lỗi runtime. Vì vậy frontend phải bám sát API contract.

### Ví dụ lỗi thường gặp

```javascript
// Sai nếu backend trả Spring Page
courses.value = res.data.result.data

// Đúng nếu backend trả Spring Page
courses.value = res.data.result.content
```

### Cách debug nhanh

Khi UI không hiện data, bước đầu tiên nên `console.log(res.data)` hoặc xem tab Network để kiểm tra response thật. Đừng đoán field theo trí nhớ, vì mỗi backend có thể wrap response khác nhau: `data`, `result`, `content`, `items`, `records`.

## Frontend Filter State

### Giải thích ngắn gọn

Frontend Filter State là trạng thái người dùng chọn trên UI như keyword, level, course type. State này được chuyển thành query params khi gọi API.

### Ví dụ trong project này

`CourseListPage.vue` lưu `filters.keyword`, `filters.level`, `filters.courseType`; khi gọi API thì chỉ gửi param nào có giá trị.

### Câu hỏi phỏng vấn liên quan

Vì sao filter "Tất cả" nên gửi param rỗng hoặc không gửi param?

### Câu trả lời ngắn gọn

Vì backend hiểu param rỗng/null là bỏ qua filter. Nếu gửi một giá trị giả như `ALL_LEVELS` mà backend không hỗ trợ, request sẽ lỗi.

### Ví dụ code dễ nhớ

```javascript
function buildCourseParams() {
  const params = {
    page: currentPage.value,
    size: pageSize.value
  }

  if (filters.keyword) params.keyword = filters.keyword
  if (filters.level) params.level = filters.level
  if (filters.courseType) params.courseType = filters.courseType

  return params
}
```

### Lưu ý UX

Khi filter thay đổi, thường nên reset page về `0`. Nếu user đang ở trang 5 rồi đổi filter hẹp hơn, backend có thể không còn trang 5 và UI sẽ rỗng dù filter có kết quả ở trang đầu.

## UI Async States

### Giải thích ngắn gọn

UI Async States là các trạng thái giao diện khi làm việc với API bất đồng bộ: loading, success, error và empty.

### Ví dụ trong project này

`CourseListPage.vue` hiển thị spinner khi đang tải khóa học, message lỗi khi API fail và empty state khi API thành công nhưng không có course nào.

### Câu hỏi phỏng vấn liên quan

Vì sao cần phân biệt error state và empty state?

### Câu trả lời ngắn gọn

Error nghĩa là hệ thống chưa lấy được dữ liệu, còn empty nghĩa là lấy dữ liệu thành công nhưng không có kết quả. Hai tình huống cần thông báo và hành động khác nhau.

### Cách nghĩ đơn giản

```text
loading: đang chờ API
error: API fail hoặc không parse được dữ liệu
empty: API thành công nhưng danh sách rỗng
data: API thành công và có dữ liệu
```

### Điểm cần nhớ khi phỏng vấn

UI async state tốt giúp sản phẩm có cảm giác ổn định. Người dùng luôn biết hệ thống đang tải, đã lỗi, hay thật sự không có dữ liệu. Đây là khác biệt giữa "code chạy được" và "UI dùng được".

## Route Param Watcher

### Giải thích ngắn gọn

Route Param Watcher là việc theo dõi thay đổi của tham số trên URL để component gọi lại API hoặc cập nhật state khi tham số đổi.

### Ví dụ trong project này

`CourseDetailPage.vue` theo dõi `route.params.slug`. Nếu slug thay đổi, page gọi lại `CourseService.getCourseBySlug(slug)` để hiển thị đúng khóa học mới.

### Câu hỏi phỏng vấn liên quan

Vì sao `onMounted()` đôi khi chưa đủ khi dùng dynamic route?

### Câu trả lời ngắn gọn

Vì Vue Router có thể tái sử dụng cùng component khi chỉ param thay đổi. `watch` giúp component phản ứng với param mới mà không cần remount.

### Ví dụ code dễ nhớ

```javascript
watch(
  () => route.params.slug,
  (newSlug) => {
    if (newSlug) {
      loadCourseDetail(newSlug)
    }
  },
  { immediate: true }
)
```

### Lưu ý

`immediate: true` giúp watcher chạy ngay lần đầu component mount, nên có thể thay cho việc viết thêm `onMounted()`. Nếu API call phụ thuộc nhiều param, nên watch một object hoặc array param để tránh quên reload khi một param khác thay đổi.

## XSS Awareness

### Giải thích ngắn gọn

XSS Awareness là ý thức phòng tránh việc render HTML/script không tin cậy từ backend hoặc người dùng lên giao diện.

### Ví dụ trong project này

Course description được render bằng text thường với `white-space: pre-line` thay vì `v-html`, vì task này chưa cần rich text HTML.

### Câu hỏi phỏng vấn liên quan

Khi nào có thể dùng `v-html` trong Vue?

### Câu trả lời ngắn gọn

Chỉ nên dùng khi thật sự cần render HTML và dữ liệu đã được sanitize hoặc đến từ nguồn tin cậy. Nếu chỉ là text, interpolation an toàn hơn.

### Ví dụ dễ nhớ

```vue
<!-- An toàn hơn cho text thường -->
<p>{{ course.description }}</p>

<!-- Chỉ dùng khi HTML đã sanitize -->
<div v-html="sanitizedDescription"></div>
```

### Điểm cần nhớ khi phỏng vấn

XSS xảy ra khi app render nội dung không tin cậy như HTML/script và trình duyệt thực thi nó. Vue interpolation `{{ }}` tự escape text, nên an toàn hơn cho mô tả, comment, title. `v-html` bỏ qua cơ chế escape này, vì vậy phải sanitize trước.

## Placeholder CTA

### Giải thích ngắn gọn

Placeholder CTA là nút hành động được hiển thị để định hướng luồng sản phẩm nhưng chưa kích hoạt nghiệp vụ thật.

### Ví dụ trong project này

`CourseDetailPage.vue` hiển thị nút "Đăng ký học miễn phí" hoặc "Mua khóa học" nhưng để disabled vì task hiện tại chưa làm enrollment/payment UI.

### Câu hỏi phỏng vấn liên quan

Vì sao CTA chưa hoàn thiện nên disabled?

### Câu trả lời ngắn gọn

Vì người dùng không nên bấm vào một hành động chưa có xử lý thật. Disabled CTA giúp UI rõ phạm vi và tránh lỗi trải nghiệm.

### Lưu ý sản phẩm

Placeholder CTA hữu ích khi muốn giữ layout và định hướng flow tương lai, nhưng nên tránh tạo kỳ vọng sai. Nếu nút disabled, UI nên có style rõ là chưa thao tác được. Trong môi trường production, không nên để quá nhiều CTA giả vì người dùng sẽ mất niềm tin vào sản phẩm.

## Protected Action

### Giải thích ngắn gọn

Protected Action là hành động trên UI chỉ cho phép user đã đăng nhập hoặc có role phù hợp thực hiện.

### Ví dụ trong project này

Nút "Đăng ký học miễn phí" trên `CourseDetailPage.vue` yêu cầu user đăng nhập. Nếu chưa đăng nhập, frontend chuyển sang `/login` trước khi gọi API enroll.

### Câu hỏi phỏng vấn liên quan

Frontend check đăng nhập có thay thế backend security được không?

### Câu trả lời ngắn gọn

Không. Frontend check giúp UX tốt hơn, còn backend security mới là lớp bắt buộc để bảo vệ dữ liệu và nghiệp vụ.

### Ví dụ code dễ nhớ

```javascript
async function enrollFreeCourse(courseId) {
  if (!authStore.isAuthenticated) {
    router.push({
      path: '/login',
      query: { redirect: route.fullPath }
    })
    return
  }

  await CourseService.enrollFreeCourse(courseId)
}
```

### Lưu ý

Protected action khác với protected route. Có những page public ai cũng xem được, nhưng một hành động cụ thể trong page như enroll, favorite, comment, checkout lại yêu cầu đăng nhập. Vì vậy guard ở route chưa đủ; button handler cũng cần kiểm tra trạng thái auth để điều hướng UX đúng.

## Safe Redirect

### Giải thích ngắn gọn

Safe Redirect là cách chỉ cho phép điều hướng sau login tới đường dẫn nội bộ hợp lệ, tránh chuyển user sang URL lạ.

### Ví dụ trong project này

`LoginPage.vue` chỉ dùng `redirect` query nếu path bắt đầu bằng `/` và không bắt đầu bằng `//`. Nếu không hợp lệ, user được đưa về `/student/dashboard`.

### Câu hỏi phỏng vấn liên quan

Open redirect nguy hiểm ở điểm nào?

### Câu trả lời ngắn gọn

Kẻ xấu có thể lợi dụng redirect để đưa user sang website giả mạo sau khi đăng nhập. Vì vậy app chỉ nên redirect nội bộ.

### Ví dụ code dễ nhớ

```javascript
function getSafeRedirect(redirect) {
  if (!redirect) return '/student/dashboard'

  const isInternalPath = redirect.startsWith('/') && !redirect.startsWith('//')
  return isInternalPath ? redirect : '/student/dashboard'
}
```

### Misconception hay gặp

- Nghĩ redirect chỉ là UX nên không liên quan bảo mật. Sai, open redirect thường được dùng trong phishing.
- Chỉ check `startsWith('/')` là đủ. Chưa đủ, vì `//evil.com` cũng có thể được trình duyệt hiểu là external URL.
- Tin tuyệt đối vào query param `redirect`. Sai, query param là input từ client và phải validate.

## Idempotency Awareness

### Giải thích ngắn gọn

Idempotency Awareness là ý thức xử lý các hành động có thể bị gọi nhiều lần, ví dụ user bấm nút liên tục hoặc gửi lại request.

### Ví dụ trong project này

Frontend disable nút enroll khi `isEnrolling`, còn backend có unique constraint và xử lý `USER_ALREADY_ENROLLED` để chống ghi danh trùng.

### Câu hỏi phỏng vấn liên quan

Vì sao vừa cần disable nút ở frontend vừa cần chống trùng ở backend?

### Câu trả lời ngắn gọn

Disable nút chỉ giảm lỗi thao tác. Backend vẫn phải chống trùng vì request có thể được gửi từ tab khác, Postman hoặc do race condition.

### Ví dụ nhiều lớp bảo vệ

```javascript
// Frontend: giảm double click
if (isEnrolling.value) return
isEnrolling.value = true
```

```java
// Backend/database: bảo vệ dữ liệu thật
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "course_id"})
})
public class CourseEnrollment {
}
```

### Điểm cần nhớ khi phỏng vấn

Idempotency nghĩa là gọi một hành động nhiều lần nhưng kết quả cuối cùng vẫn không bị sai. Với enroll course, user bấm 10 lần thì kết quả đúng vẫn chỉ là một enrollment. Frontend giảm thao tác lặp, backend và database mới là lớp đảm bảo cuối cùng.

## Reusable Course Card

### Giải thích ngắn gọn

Reusable Course Card là component card khóa học có thể dùng ở nhiều page, nhận dữ liệu qua props và báo hành động qua emits.

### Ví dụ trong project này

`MyCourseCard.vue` nhận `course` qua props và emit `continue` khi user bấm nút học tiếp. `StudentDashboardPage` và `MyCoursesPage` có thể cùng dùng component này.

### Câu hỏi phỏng vấn liên quan

Vì sao component card không nên tự quyết định toàn bộ điều hướng?

### Câu trả lời ngắn gọn

Vì component card nên dễ tái sử dụng. Page cha có context đầy đủ hơn để quyết định route nào phù hợp.

### Ví dụ code dễ nhớ

```vue
<MyCourseCard
  v-for="course in courses"
  :key="course.id"
  :course="course"
  @continue="goToLearning"
/>
```

```javascript
const emit = defineEmits(['continue'])

function handleContinue() {
  emit('continue', props.course)
}
```

### Lưu ý

Card nên nhận dữ liệu qua props và phát event khi user tương tác. Nếu card tự gọi router hoặc API quá nhiều, nó sẽ khó dùng lại ở page khác. Ví dụ dashboard có thể muốn đi tới lesson gần nhất, còn course list có thể muốn đi tới course detail.

## Progress Mapping

### Giải thích ngắn gọn

Progress Mapping là việc frontend đọc dữ liệu tiến độ từ backend và hiển thị thành progress bar, phần trăm hoặc số bài đã học.

### Ví dụ trong project này

`MyCourseCard.vue` ưu tiên dùng `course.progressPercent`. Nếu field này không có, component fallback tính từ `completedLessons / totalLessons`.

### Câu hỏi phỏng vấn liên quan

Vì sao nên ưu tiên progress do backend tính?

### Câu trả lời ngắn gọn

Backend là nơi biết rule nghiệp vụ đầy đủ nhất. Frontend chỉ nên hiển thị hoặc fallback đơn giản khi cần.

### Ví dụ code dễ nhớ

```javascript
const progressPercent = computed(() => {
  if (props.course.progressPercent != null) {
    return props.course.progressPercent
  }

  if (!props.course.totalLessons) {
    return 0
  }

  return Math.round(
    (props.course.completedLessons / props.course.totalLessons) * 100
  )
})
```

### Lưu ý

Fallback ở frontend chỉ nên dùng để hiển thị. Nếu dữ liệu progress ảnh hưởng nghiệp vụ như cấp chứng chỉ, mở khóa bài học tiếp theo, hoặc tính hoàn thành course, backend phải là nơi tính chính thức.

## API Contract Alignment

### Giải thích ngắn gọn

API Contract Alignment là việc đảm bảo frontend route, service và backend endpoint dùng cùng kiểu định danh, cùng request/response.

### Ví dụ trong project này

Frontend đang điều hướng `/student/lessons/:slug`, nhưng backend learning API hiện là `GET /api/v1/lessons/{id}`. Task LearningPage cần xử lý rõ dùng `lessonId` hay thêm API lấy bài học theo slug trong course.

### Câu hỏi phỏng vấn liên quan

Điều gì xảy ra nếu frontend dùng slug còn backend chỉ nhận id?

### Câu trả lời ngắn gọn

Frontend sẽ không gọi được API đúng, dễ gây 404 hoặc phải viết logic tạm. Cần thống nhất contract trước khi tích hợp UI.

### Ví dụ vấn đề thường gặp

```text
Frontend route:
/student/lessons/n5-greetings

Backend endpoint:
GET /api/v1/lessons/15
```

Trong ví dụ này, frontend đang có `slug`, còn backend cần `id`. Có 3 cách xử lý rõ ràng:

- Đổi route frontend sang `/student/lessons/:id`.
- Thêm API backend `GET /api/v1/lessons/slug/{slug}` nếu slug unique.
- Dùng route có cả context như `/courses/:courseSlug/lessons/:lessonSlug` nếu lesson slug chỉ unique trong course.

### Điểm cần nhớ khi phỏng vấn

API contract là "hợp đồng" giữa frontend và backend. Contract không chỉ là URL, mà còn gồm method, auth requirement, request body, response shape, error code và kiểu định danh. Khi contract lệch, frontend và backend đều có thể đúng riêng lẻ nhưng app vẫn không chạy được.

## Lesson ID Routing

### Giải thích ngắn gọn

Lesson ID Routing là cách dùng id của bài học trong route/frontend để gọi đúng backend API học bài.

### Ví dụ trong project này

Route student learning dùng `/student/lessons/:id`, lấy id này để gọi backend `GET /api/v1/lessons/{id}` và `POST /api/v1/lessons/{id}/progress`.

### Câu hỏi phỏng vấn liên quan

Khi nào nên dùng id thay vì slug cho route?

### Câu trả lời ngắn gọn

Nên dùng id khi backend API đã định danh bằng id, hoặc khi slug không unique toàn hệ thống. Nếu muốn URL đẹp bằng slug, cần đảm bảo slug đủ unique hoặc đi kèm context như course slug.

### Ví dụ code dễ nhớ

```javascript
const route = useRoute()
const lessonId = computed(() => Number(route.params.id))

watch(
  lessonId,
  (id) => {
    if (id) {
      loadLesson(id)
    }
  },
  { immediate: true }
)
```

### Lưu ý

Route param luôn đi vào frontend dưới dạng string. Nếu backend/service cần number, nên convert rõ ràng bằng `Number()` và xử lý trường hợp invalid. Không nên truyền bừa string `"abc"` vào API id vì lỗi sẽ khó hiểu hơn.

## DTO Evolution

### Giải thích ngắn gọn

DTO Evolution là việc thay đổi DTO theo nhu cầu mới của client mà vẫn giữ contract rõ ràng và hạn chế phá vỡ code cũ.

### Ví dụ trong project này

`MyCourseRes` được bổ sung `lastLessonId` để frontend có thể điều hướng tới learning route đúng với backend API hiện có.

### Câu hỏi phỏng vấn liên quan

Thêm field mới vào response DTO có thường phá frontend cũ không?

### Câu trả lời ngắn gọn

Thường không, nếu chỉ thêm field optional và giữ các field cũ. Rủi ro lớn hơn là đổi tên hoặc xóa field đang được frontend sử dụng.

### Ví dụ dễ nhớ

```java
public record MyCourseRes(
        Long id,
        String title,
        String slug,
        Double progressPercent,
        Long lastLessonId // field mới để frontend điều hướng học tiếp
) {}
```

### Nguyên tắc khi evolve DTO

- Thêm field mới thường an toàn hơn đổi tên field cũ.
- Xóa field hoặc đổi kiểu dữ liệu là breaking change.
- Nếu cần đổi lớn, nên version API hoặc hỗ trợ song song trong một thời gian.
- Field mới nên có giá trị `null` hợp lý nếu dữ liệu cũ chưa có.

### Điểm cần nhớ khi phỏng vấn

DTO không phải entity. DTO là contract phục vụ client. Khi UI có nhu cầu hợp lý như "học tiếp bài gần nhất", backend có thể bổ sung `lastLessonId` vào DTO thay vì bắt frontend tự suy luận từ nhiều API khác nhau.

## Monotonic Progress

### Giải thích ngắn gọn

Monotonic Progress là rule tiến độ chỉ tăng hoặc giữ nguyên, không bị giảm xuống khi client gửi watchedPercent thấp hơn.

### Ví dụ trong project này

Backend update `LessonProgress.watchedPercent` bằng logic chỉ nhận giá trị mới nếu nó lớn hơn giá trị hiện tại. Frontend cũng dùng `Math.max()` để giữ UI cùng rule.

### Câu hỏi phỏng vấn liên quan

Vì sao progress không nên giảm khi client gửi giá trị thấp hơn?

### Câu trả lời ngắn gọn

Vì tiến độ học thường biểu diễn mức cao nhất user đã đạt được. Nếu cho giảm tùy ý, user có thể mất tiến độ hoặc UI bị nhảy lùi khó hiểu.

### Ví dụ code dễ nhớ

```java
double currentPercent = progress.getWatchedPercent();
double incomingPercent = req.getWatchedPercent();

progress.setWatchedPercent(Math.max(currentPercent, incomingPercent));

if (progress.getWatchedPercent() >= 90) {
    progress.setCompleted(true);
}
```

### Lưu ý

Monotonic progress phù hợp với "mức cao nhất đã học". Nhưng không phải mọi trạng thái đều monotonic. Ví dụ volume video, playback speed, hoặc câu trả lời quiz hiện tại có thể tăng giảm bình thường. Cần hiểu ý nghĩa nghiệp vụ trước khi áp dụng rule chỉ tăng.

## Learning API Service

### Giải thích ngắn gọn

Learning API Service là lớp frontend service gom các API liên quan tới học bài và cập nhật tiến độ.

### Ví dụ trong project này

`learning.service.js` có `getLessonDetail(id)` và `updateProgress(id, req)`, giúp `LessonLearningPage.vue` không phải biết chi tiết endpoint ở nhiều chỗ.

### Câu hỏi phỏng vấn liên quan

Vì sao nên tách learning API thành service riêng?

### Câu trả lời ngắn gọn

Vì page component sẽ gọn hơn, API dễ tái sử dụng và khi endpoint thay đổi chỉ cần sửa ở service.

### Ví dụ code dễ nhớ

```javascript
export const LearningService = {
  getLessonDetail(id) {
    return api.get(`/api/v1/lessons/${id}`)
  },

  updateProgress(id, payload) {
    return api.post(`/api/v1/lessons/${id}/progress`, payload)
  }
}
```

### Lưu ý

Learning page thường có nhiều interaction nhỏ như load lesson, update progress, mark completed, chuyển bài tiếp theo. Nếu gọi Axios trực tiếp trong component cho từng việc, component sẽ rất nhanh rối. Service layer giúp gom endpoint và giữ component tập trung vào UI/state.

## Derived Data

### Giải thích ngắn gọn

Derived Data là dữ liệu được tính ra từ dữ liệu khác, thường dùng để tối ưu hiển thị hoặc truy vấn.

### Ví dụ trong project này

`CourseEnrollment.progressPercent` được tính từ số lesson đã hoàn thành trong `lesson_progress` chia cho tổng số lesson của course.

### Câu hỏi phỏng vấn liên quan

Nhược điểm của việc lưu derived data là gì?

### Câu trả lời ngắn gọn

Nó có thể bị lệch nếu không được cập nhật khi dữ liệu nguồn thay đổi. Vì vậy cần cập nhật trong cùng nghiệp vụ hoặc transaction.

### Ví dụ dễ nhớ

```text
completedLessons = 7
totalLessons = 10
progressPercent = 70
```

`progressPercent` là derived data vì nó được tính từ `completedLessons` và `totalLessons`. Nếu lưu nó vào `course_enrollments`, việc đọc dashboard sẽ nhanh hơn vì không cần tính lại liên tục. Nhưng khi một lesson progress thay đổi, backend phải cập nhật lại field này.

### Khi nào nên lưu derived data

- Khi dữ liệu được đọc rất thường xuyên và tính toán lại tốn chi phí.
- Khi màn hình cần render nhanh, ví dụ dashboard hoặc my courses.
- Khi có thể cập nhật dữ liệu dẫn xuất trong cùng transaction với dữ liệu nguồn.

### Khi nào không nên lưu

- Khi công thức đơn giản và dữ liệu nhỏ.
- Khi dữ liệu nguồn thay đổi liên tục nhưng khó đảm bảo cập nhật đồng bộ.
- Khi sai lệch dữ liệu gây hậu quả nghiệp vụ nghiêm trọng.

## Enrollment Progress Recalculation

### Giải thích ngắn gọn

Enrollment Progress Recalculation là việc tính lại tiến độ khóa học của một student sau khi progress của lesson thay đổi.

### Ví dụ trong project này

Sau khi `LearningServiceImpl.updateProgress()` lưu lesson progress, backend đếm số bài đã hoàn thành và update `course_enrollments.progress_percent`.

### Câu hỏi phỏng vấn liên quan

Vì sao recalculation nên nằm ở backend?

### Câu trả lời ngắn gọn

Vì backend là nguồn sự thật của dữ liệu học tập. Nếu để frontend tự tính và gửi lên, user có thể gửi dữ liệu sai hoặc giả mạo.

### Ví dụ code dễ nhớ

```java
@Transactional
public LessonProgressRes updateProgress(Long lessonId, UpdateProgressReq req) {
    LessonProgress progress = upsertLessonProgress(lessonId, req);

    CourseEnrollment enrollment = enrollmentRepository
            .findByUserIdAndCourseId(progress.getUser().getId(), progress.getLesson().getCourse().getId())
            .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

    long totalLessons = lessonRepository.countByCourseId(enrollment.getCourse().getId());
    long completedLessons = progressRepository.countCompletedLessons(
            enrollment.getUser().getId(),
            enrollment.getCourse().getId()
    );

    double courseProgress = totalLessons == 0
            ? 0
            : (completedLessons * 100.0 / totalLessons);

    enrollment.setProgressPercent(courseProgress);
    return progressMapper.toLessonProgressRes(progress);
}
```

### Điểm cần nhớ khi phỏng vấn

Recalculation nên nằm trong transaction để lesson progress và enrollment progress không bị lệch. Nếu update lesson progress thành công nhưng update course progress fail, dữ liệu sẽ không nhất quán. Với hệ thống lớn hơn, có thể dùng event/background job, nhưng vẫn phải có chiến lược đảm bảo eventual consistency.

### Misconception hay gặp

- Nghĩ frontend tính progress rồi gửi lên backend là đủ. Sai, client không đáng tin và có thể gửi số giả.
- Nghĩ lưu derived data là luôn xấu. Không hẳn, nó tốt nếu giúp đọc nhanh và có cơ chế cập nhật đồng bộ.
- Nghĩ chỉ cần update bài hiện tại. Chưa đủ, vì thay đổi bài học có thể ảnh hưởng tiến độ tổng của cả khóa.

### Virtual Threads - Luồng Ảo (Java 21)

**Rating:** 🔴

**Định nghĩa:**
Là tính năng cốt lõi của Java 21, cho phép tạo ra hàng triệu luồng (threads) với chi phí bộ nhớ cực kỳ rẻ (vài KB một thread), giúp giải quyết bài toán nghẽn luồng khi có lượng truy cập lớn (I/O-heavy operations).

**Ví dụ:**
```java
// Tạo một Executor sử dụng Virtual Threads
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
executor.submit(() -> {
    // Xử lý đọc file từ vựng hoặc gọi API tiếng Nhật
});
```

**Lợi ích/Khi nào dùng:**
- Tăng throughput (băng thông xử lý) của server Spring Boot lên gấp nhiều lần.
- Không cần cấu hình Thread Pool phức tạp.

---

### Database Index - Chỉ Mục Dữ Liệu

**Rating:** 🔴

**Định nghĩa:**
Là một cấu trúc dữ liệu (thường là B-Tree) giúp Database tăng tốc độ tìm kiếm các bản ghi, tương tự như mục lục của một cuốn sách.

**Ví dụ:**
```java
// Cấu hình trong Entity để Hibernate tự sinh Index trong DB
@Table(name = "lessons", indexes = {
    @Index(name = "idx_lesson_slug", columnList = "slug")
})
public class Lesson { ... }
```

**Lợi ích/Khi nào dùng:**
- Tăng tốc tối đa cho các câu lệnh tìm kiếm theo điều kiện (`WHERE slug = ...`).
- Rất cần thiết cho các trường dữ liệu dùng để Search hoặc Filter ở Frontend.

**Misconception hay gặp:**
- Tạo Index cho mọi cột là tốt: Sai, Index làm chậm câu lệnh INSERT/UPDATE vì DB phải cập nhật lại cây B-Tree. Chỉ tạo cho cột nào thường xuyên xuất hiện trong mệnh đề WHERE.

## Integration Testing (Failsafe Plugin)

### Giải thích ngắn gọn

Integration Testing (Kiểm thử tích hợp) là việc kiểm tra xem các thành phần của hệ thống (như Controller, Service, Repository và Database) có hoạt động trơn tru với nhau hay không. Trong Spring Boot, Maven chia test làm hai pha: `test` (dành cho Unit Test qua plugin `surefire`) và `verify` (dành cho Integration Test qua plugin `failsafe`).

### Ví dụ trong project này

Các file kết thúc bằng `*IT.java` như `StudentDashboardIT.java` được cấu hình để chạy bởi `maven-failsafe-plugin` ở giai đoạn `verify`, giúp nạp toàn bộ `ApplicationContext` cùng database ảo H2 để test API từ ngoài vào trong.

### Câu hỏi phỏng vấn liên quan

Tại sao không dùng luôn `surefire` để chạy tất cả các bài test?

### Câu trả lời ngắn gọn

Vì Integration Test thường tốn nhiều thời gian và yêu cầu ứng dụng phải được đóng gói (package) hoặc dựng container (database, cache) trước khi chạy. Tách nó sang giai đoạn `verify` (sau giai đoạn `package`) giúp quy trình CI/CD fail fast: nếu unit test lỗi thì không cần mất công build package và chạy DB ảo.

### Điểm cần nhớ khi phỏng vấn

Khi nạp `ApplicationContext` bằng `@SpringBootTest`, Spring sẽ tự động tìm và khởi chạy các bean cấu hình như `CommandLineRunner` (ví dụ `DatabaseSeeder`). Nếu các thành phần này phụ thuộc vào dữ liệu thật nhưng lại bị `@MockBean` ghi đè, hệ thống sẽ báo lỗi. Việc sử dụng `@ConditionalOnProperty` để vô hiệu hóa các bean này ở môi trường test là giải pháp tốt nhất.

## Lombok Builder.Default

### Giải thích ngắn gọn

`@Builder.Default` là một annotation của Lombok đi kèm với `@Builder`, giúp giữ nguyên giá trị khởi tạo ban đầu của một trường dữ liệu (field) khi đối tượng được tạo thông qua pattern Builder.

### Ví dụ trong project này

Trong entity `Lesson`, trường `sortOrder` có giá trị khởi tạo `private Integer sortOrder = 0;`. Nếu không có `@Builder.Default`, khi tạo đối tượng bằng `Lesson.builder().title("A").build()`, trường `sortOrder` sẽ bị Lombok ghi đè thành `null`.

### Câu hỏi phỏng vấn liên quan

Vì sao Lombok cảnh báo `@Builder will ignore the initializing expression entirely`?

### Câu trả lời ngắn gọn

Vì nguyên tắc của Builder pattern là chỉ set các giá trị được khai báo rõ ràng. Để bảo toàn giá trị khởi tạo ở khai báo field, Lombok yêu cầu lập trình viên phải chủ động đánh dấu bằng `@Builder.Default`.

### Ví dụ code dễ nhớ

```java
@Builder
public class Course {
    // Không có @Builder.Default -> Nếu builder không gọi .status(), status sẽ = null
    private CourseStatus oldStatus = CourseStatus.DRAFT;

    // Có @Builder.Default -> Nếu builder không gọi .status(), status sẽ = DRAFT
    @Builder.Default
    private CourseStatus status = CourseStatus.DRAFT;
}
```

### Misconception hay gặp

- Nghĩ rằng Java gán `0` hoặc `DRAFT` nên object luôn có giá trị đó. Sai, vì Lombok Builder can thiệp vào quá trình tạo constructor, bỏ qua phép gán ban đầu nếu không có `Default`.

## @DataJpaTest vs @SpringBootTest

### Giải thích ngắn gọn

`@DataJpaTest` là một annotation dùng để test tầng Repository (JPA slice). Khác với `@SpringBootTest` (nạp toàn bộ cấu hình, controller, service, security), `@DataJpaTest` chỉ nạp các `@Repository`, `@Entity` và các cấu hình liên quan đến database.

### Ví dụ trong project này

Các file test cho repository như `LessonProgressRepositoryTest` và `CourseRepositoryTest` dùng `@DataJpaTest` kết hợp với H2 In-Memory Database để kiểm thử các query phức tạp như `@Query("UPDATE ...")` hay phân trang `Page<Course>`.

### Câu hỏi phỏng vấn liên quan

Vì sao nên dùng `@DataJpaTest` thay vì dùng Mockito (`@Mock`) để test các custom query trong Repository?

### Câu trả lời ngắn gọn

Vì Mockito chỉ giả lập hành vi (trả về kết quả cho trước), nó không thể kiểm chứng được câu lệnh SQL sinh ra có đúng cú pháp hay logic hay không. `@DataJpaTest` sử dụng database thực (hoặc H2) để chạy lệnh SQL, giúp phát hiện lỗi mapping và lỗi SQL.

### Ví dụ code dễ nhớ

```java
@DataJpaTest
@ActiveProfiles("test") // Đọc file application-test.yml (thường trỏ vào H2)
class CourseRepositoryTest {
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private EntityManager em;

    @Test
    void testCustomQuery() {
        // 1. Seed dữ liệu thật vào DB In-memory
        Course course = new Course(...);
        em.persist(course);
        em.flush();
        em.clear(); // Xóa cache để ép query chạy xuống DB

        // 2. Gọi Repository method cần test
        Optional<Course> found = courseRepository.findBySlug("test-slug");
        
        // 3. Assert kết quả
        assertThat(found).isPresent();
    }
}
```

### Điểm cần nhớ khi phỏng vấn

1. **Tốc độ**: Nhanh hơn `@SpringBootTest` rất nhiều.
2. **Transaction**: `@DataJpaTest` mặc định là `@Transactional`, tức là nó sẽ tự động **rollback** sau mỗi hàm `@Test`, giúp các test case hoàn toàn độc lập và không bị rác dữ liệu.
3. **Hibernate Cache**: Khi insert dữ liệu mẫu, thường phải gọi `em.flush()` và `em.clear()` để đảm bảo dữ liệu đã được đẩy vào DB thật, tránh trường hợp query đọc nhầm từ Hibernate L1 Cache, làm sai lệch kết quả test.

---

## 30. Cơ chế bảo mật Session: Refresh Token Rotation & Token Reuse Detection

Khi làm việc với các ứng dụng Single Page Application (React, Vue) hoặc Mobile App, việc lưu trữ Access Token và Refresh Token phía client tiềm ẩn rủi ro bảo mật (như XSS trộm token từ LocalStorage). Để giải quyết triệt để rủi ro này, dự án áp dụng **Refresh Token Rotation**.

### 1. Refresh Token Rotation là gì?
Thay vì cấp phát một Refresh Token có thời hạn sống rất lâu (ví dụ 1 tháng) và dùng đi dùng lại nhiều lần, **Rotation** hoạt động theo nguyên tắc:
- Mỗi khi client sử dụng Refresh Token để lấy Access Token mới, Backend sẽ **hủy luôn (revoke)** Refresh Token cũ đó.
- Sau đó, Backend sẽ cấp phát một cặp (Access Token MỚI + Refresh Token MỚI).
- **Lợi ích:** Token trở thành dạng *sử dụng 1 lần (One-time use)*. Nếu hacker đánh cắp được token, chúng chỉ có một cơ hội duy nhất để sử dụng. Và nếu client hợp pháp đã dùng nó trước, token đó đã bị vô hiệu hóa.

### 2. Token Reuse Detection (Phát hiện đánh cắp Session)
Mặc dù Token Rotation rất mạnh, nhưng điều gì sẽ xảy ra nếu Hacker dùng token đánh cắp được *trước* khi chủ nhân thật sự dùng nó? (Hacker dùng thành công và lấy được cặp token mới, đẩy chủ nhân ra ngoài).
Khi chủ nhân thật sự quay lại và cố gắng dùng Refresh Token cũ của họ (đã bị Backend đánh dấu là *Revoked* do Hacker đã dùng trước đó), Backend sẽ nhận diện được hành vi **Reuse (Dùng lại)**.

**Xử lý Token Reuse Detection:**
- Nếu Backend nhận được một yêu cầu sử dụng Refresh Token mà token đó có trạng thái `revoked = true` trong database.
- Backend hiểu rằng: "Oh, token này đáng lẽ không được dùng nữa, vậy mà có người đang cố dùng nó. Chắc chắn session này đã bị đánh cắp hoặc có xung đột nghiêm trọng!"
- **Hành động bảo vệ:** Backend lập tức tìm ra User ID sở hữu token đó và **REVOKE (Thu hồi) TOÀN BỘ CÁC TOKEN HIỆN CÓ** của user này. 
- Hậu quả: Cả Hacker và User thật đều bị đăng xuất. User thật sẽ phải nhập lại Username và Password để tạo một Session hoàn toàn mới và an toàn.

### 3. Database Cleanup (Dọn dẹp rác)
Vì Refresh Token Rotation tạo ra rất nhiều bản ghi token (cứ mỗi lần refresh là sinh ra 1 token mới và bỏ 1 token cũ), Database sẽ nhanh chóng phình to (Database Bloat).
**Giải pháp:** Sử dụng Spring Boot `@Scheduled(cron = "0 0 2 * * ?")` để tạo một **Job Cron** chạy ngầm vào 2h sáng mỗi ngày. Job này sẽ càn quét Database và xóa cứng (`DELETE`) tất cả các token đã quá hạn (`expiredAt < now`) hoặc đã bị revoke (`revoked = true`).

### Điểm cần nhớ khi phỏng vấn
- **Refresh Token Rotation**: Trả lời phỏng vấn viên rằng đây là kỹ thuật an toàn nhất cho SPA khi không thể dùng HttpOnly Cookies.
- **Reuse Detection**: Là "lá chắn cuối cùng" giúp cô lập hoàn toàn thiệt hại, buộc kẻ gian phải ngừng kết nối khi phát hiện sự bất thường.
- **Idempotency**: Chú ý xử lý các trường hợp Race Condition trên Frontend (ví dụ 2 request cùng gọi API refresh lúc token hết hạn) bằng cách dùng Mutex hoặc Promise Queue ở Frontend Axios Interceptor để tránh Backend hiểu lầm là bị đánh cắp.

---

## 31. Bảo mật Session với HttpOnly Cookies trong kiến trúc SPA

Trong kiến trúc Single Page Application (SPA), nơi lưu trữ thông tin xác thực đóng vai trò cực kỳ quan trọng. Việc chuyển đổi từ LocalStorage sang HttpOnly Cookie cho Refresh Token mang lại sự nâng cấp bảo mật đáng kể.

### 1. Tại sao LocalStorage lại nguy hiểm?
`localStorage` và `sessionStorage` trong trình duyệt là những nơi lưu trữ có thể dễ dàng truy cập bởi bất kỳ đoạn mã JavaScript nào chạy trên cùng domain thông qua biến toàn cục `window.localStorage`.
Nếu trang web dính lỗi **XSS (Cross-Site Scripting)**, mã độc của hacker có thể dễ dàng đọc được các Token lưu tại đây và gửi về máy chủ của chúng. Hậu quả là hacker có thể mạo danh người dùng (Account Takeover).

### 2. Sức mạnh của HttpOnly Cookie
**HttpOnly Cookie** là một giải pháp thiết kế chuyên biệt để chống lại XSS:
- Khi một Cookie được server thiết lập kèm cờ `HttpOnly=true`, trình duyệt sẽ chặn hoàn toàn mọi lệnh JavaScript (như `document.cookie`) muốn đọc hoặc ghi đè cookie này.
- Nghĩa là cho dù hacker có khai thác được lỗ hổng XSS, chúng cũng **không thể lấy trộm** được Refresh Token.
- Trình duyệt sẽ tự động đính kèm Cookie này một cách "vô hình" vào các request HTTP gửi đến đúng Domain và Path đã chỉ định.

### 3. Các thuộc tính Cookie quan trọng đi kèm
- **`Secure`**: Yêu cầu Cookie chỉ được gửi đi trên các kết nối HTTPS được mã hóa. Điều này chống lại kĩ thuật nghe lén mạng (Man-In-The-Middle attacks).
- **`SameSite`**: Dùng để chống lại tấn công **CSRF (Cross-Site Request Forgery)**.
  - `Strict`: Chỉ gửi cookie nếu người dùng đang ở chính xác domain đó. Tối ưu cho bảo mật nhưng có thể làm giảm trải nghiệm (ví dụ bấm link từ email vào sẽ không có cookie).
  - `Lax`: Gửi cookie khi điều hướng qua liên kết (navigate) nhưng không gửi trong các cross-origin POST/PUT request (như iframe, form ẩn). Đây là chuẩn mặc định an toàn cho hầu hết SPA.

### 4. Triển khai trong thực tế (Kỹ thuật "Silent Session Restore")
Lưu Refresh Token vào Cookie đồng nghĩa với việc Frontend sẽ không bao giờ nhìn thấy nó. Vậy làm sao ứng dụng biết người dùng đã đăng nhập khi họ tải lại trang?
- Access Token (sống ngắn hạn) sẽ được lưu trong RAM (biến JS / Pinia State).
- Khi người dùng tải lại trang (F5), Access Token trong RAM sẽ bị xóa sạch.
- **Silent Restore**: Ngay trước khi mount giao diện (ví dụ trong Vue Router `beforeEach`), Frontend sẽ tự động tạo một HTTP POST request ngầm gọi lên API `/api/auth/refresh-token`.
- Nhờ cấu hình Axios `{ withCredentials: true }`, trình duyệt tự động móc Refresh Token Cookie ra và đính kèm vào request.
- Backend kiểm tra Cookie hợp lệ, trả về một Access Token hoàn toàn mới và gán một Refresh Token Cookie mới (nếu có Rotation).
- Ứng dụng "sống lại" (Hydrated) với session mới mà người dùng không hề hay biết!

### Điểm cần nhớ khi phỏng vấn
- Luôn khẳng định `HttpOnly Cookie` là tiêu chuẩn vàng để lưu Refresh Token thay cho LocalStorage.
- Trình bày được luồng đi của "Silent Restore Session" để thuyết phục rằng dù giấu token đi, hệ thống vẫn duy trì được trải nghiệm mượt mà.
- Phân biệt rõ `HttpOnly` (chống XSS) và `SameSite` (chống CSRF).

---

## 32. Bảo mật tham số phân trang (Pagination Hardening)

Phân trang (Pagination) là một tính năng phổ biến trong các API RESTful để trả về một lượng nhỏ dữ liệu từ một tập dữ liệu lớn. Tuy nhiên, nếu không được kiểm soát kỹ lưỡng, nó có thể trở thành lỗ hổng để tấn công Từ chối dịch vụ (DoS).

### 1. Rủi ro của phân trang không kiểm soát
Mặc định, các framework như Spring Data cho phép client truyền lên `page` và `size`. Nếu developer chỉ gán giá trị mặc định (`defaultValue = "10"`) mà không Validate đầu vào:
- Client có thể truyền `size = 1000000`. Khi đó, câu lệnh SQL sẽ cố gắng query và load 1 triệu bản ghi vào RAM của server.
- Hành vi này có thể làm tràn bộ nhớ (Out of Memory - OOM) và làm sập ứng dụng.
- Client cũng có thể truyền `page = -1` hoặc `size = 0`, gây ra lỗi Runtime Exception hoặc SQL Exception không mong muốn (500 Internal Server Error).

### 2. Kỹ thuật củng cố (Hardening)
Việc củng cố (Hardening) đồng nghĩa với việc đưa hệ thống vào trạng thái an toàn nhất quán (Predictable State) thông qua Validation chặt chẽ:
- **Ngăn chặn số âm/không**: `page >= 0` và `size > 0`.
- **Giới hạn cận trên (Clamping/Max Size)**: Đặt ra một con số tối đa hợp lý cho ứng dụng (ví dụ: `size <= 100`). Bất kỳ Request nào vượt quá mức này sẽ bị chặn ngay tại cửa (tầng Controller) trước khi xuống Database.

### 3. Xử lý lỗi chuẩn mực (Standardized Error Response)
Khi phát hiện tham số không hợp lệ, hệ thống không nên tự động thay đổi giá trị (silently clamp) mà nên phản hồi rõ ràng cho Frontend biết họ đang truyền sai thông số.
- Ném ra một ngoại lệ có kiểm soát (như `AppException(ErrorCode.VALIDATION_ERROR)`).
- `GlobalExceptionHandler` sẽ chặn ngoại lệ này và trả về HTTP 400 Bad Request kèm mã lỗi nội bộ (ví dụ: `1005`) và thông báo rõ ràng: `"Page size must not be greater than 100"`.

### Điểm cần nhớ khi phỏng vấn
- Luôn khẳng định việc validate `page` và `size` là bắt buộc đối với các API public hoặc API có dữ liệu lớn để chống DoS.
- Giải thích được rằng Validation Exception ném ra từ Controller sẽ được gom lại và xử lý tại `GlobalExceptionHandler` (bằng `@RestControllerAdvice`), giúp Backend trả về các cấu trúc lỗi (`ApiResponse`) hoàn toàn đồng nhất.

---

## Idempotent Completion API

### Giải thích ngắn gọn

Idempotent Completion API là endpoint đánh dấu một tài nguyên là hoàn thành, nhưng gọi nhiều lần vẫn cho kết quả cuối cùng giống nhau. API không tạo dữ liệu trùng, không làm giảm progress và không khiến hệ thống rơi vào trạng thái sai.

### Ví dụ trong project này

Endpoint `POST /api/v1/lessons/{id}/complete` đánh dấu lesson là đã hoàn thành:
- `watchedPercent = 100`
- `isCompleted = true`
- `completedAt` có giá trị
- `course_enrollments.progress_percent` được tính lại

Nếu student bấm nút "Đánh dấu hoàn thành" nhiều lần, lesson vẫn chỉ ở trạng thái completed và progress vẫn là 100%.

### Câu hỏi phỏng vấn liên quan

Vì sao endpoint complete lesson nên được thiết kế idempotent?

### Câu trả lời ngắn gọn

Vì frontend có thể gửi request lặp lại do user bấm nhiều lần, mạng retry hoặc tab bị refresh. Idempotent giúp backend xử lý an toàn và giữ dữ liệu nhất quán.

---

## Service Helper Refactor

### Giải thích ngắn gọn

Service Helper Refactor là việc tách các đoạn logic dùng chung trong service thành method nhỏ có tên rõ ràng. Cách này giúp code ít duplicate, dễ đọc và giảm rủi ro hai API xử lý cùng nghiệp vụ nhưng lệch rule.

### Ví dụ trong project này

`LearningServiceImpl` tách helper cho các bước dùng chung giữa update progress và complete lesson:
- `validateAndGetLessonAccess()` kiểm tra current user, lesson, course published và enrollment.
- `upsertLessonProgress()` update hoặc tạo mới lesson progress.
- `recalculateEnrollmentProgress()` tính lại progress tổng của khóa học.

Nhờ vậy `updateProgress()` và `completeLesson()` cùng đi qua một bộ rule nghiệp vụ thống nhất.

### Câu hỏi phỏng vấn liên quan

Khi nào nên tách helper method trong service layer?

### Câu trả lời ngắn gọn

Khi nhiều method service dùng chung các bước nghiệp vụ quan trọng, nên tách helper để tránh duplicate, dễ test và đảm bảo các flow xử lý nhất quán.

---

## Frontend API Service Layer

### Giải thích ngắn gọn

Frontend API Service Layer là lớp gom các hàm gọi API vào một file/service riêng. Component Vue không gọi Axios trực tiếp ở nhiều nơi, mà gọi các method có tên nghiệp vụ như `getLessonDetail()`, `updateProgress()` hoặc `completeLesson()`.

### Ví dụ trong project này

`LearningService.completeLesson(id)` gọi:

```javascript
return api.post(`/v1/lessons/${id}/complete`)
```

`LessonLearningPage.vue` chỉ cần gọi `LearningService.completeLesson(lesson.value.id)`, không cần biết chi tiết base URL hay endpoint đầy đủ.

### Câu hỏi phỏng vấn liên quan

Vì sao nên tách API call ra service layer trong Vue?

### Câu trả lời ngắn gọn

Vì service layer giúp code dễ maintain, dễ đổi endpoint, dễ tái sử dụng và component tập trung vào UI logic thay vì chi tiết HTTP.

---

## Local State Synchronization After Mutation

### Giải thích ngắn gọn

Sau khi frontend gọi một API làm thay đổi dữ liệu, UI cần đồng bộ lại state local để hiển thị kết quả mới. Có hai cách phổ biến: tự cập nhật local state nếu dữ liệu đơn giản, hoặc fetch lại từ backend nếu cần chính xác toàn bộ.

### Ví dụ trong project này

Sau khi `completeLesson()` thành công, `LessonLearningPage.vue` cập nhật:
- `lesson.isCompleted = true`
- `lesson.watchedPercent = 100`
- `progressForm.isCompleted = true`
- `progressForm.watchedPercent = 100`

Nhờ vậy người học thấy bài học đã hoàn thành ngay lập tức.

### Câu hỏi phỏng vấn liên quan

Khi nào nên update local state, khi nào nên refetch dữ liệu?

### Câu trả lời ngắn gọn

Nếu thay đổi đơn giản và biết chắc kết quả, update local state là đủ và nhanh. Nếu backend trả thêm nhiều dữ liệu mới hoặc có logic phức tạp, nên fetch lại để tránh UI lệch dữ liệu thật.

---

## 34. Quản lý cấu hình & The Twelve-Factor App (Environment Variables)

### Giải thích ngắn gọn
Trong phát triển phần mềm hiện đại, có một triết lý thiết kế ứng dụng tên là **"The Twelve-Factor App"**. Yếu tố thứ 3 trong 12 nguyên tắc này quy định: **"Store config in the environment"** (Lưu trữ cấu hình trong biến môi trường). Nghĩa là mọi thông tin có thể thay đổi giữa các môi trường (Dev, Staging, Prod) — đặc biệt là các secret/password — phải được tách biệt hoàn toàn khỏi mã nguồn.

### Ví dụ trong project này
Thay vì code cứng mật khẩu Database vào file `application-dev.yml`:
```yaml
# XẤU (Lộ lọt thông tin nhạy cảm)
spring:
  datasource:
    password: "0209"
```

Chúng ta sử dụng cú pháp `${VAR_NAME}` của Spring Boot để đọc từ biến môi trường:
```yaml
# TỐT (An toàn và linh hoạt)
spring:
  datasource:
    password: ${DB_PASSWORD}
```

Và tạo một file `.env.example` chứa dữ liệu mẫu để các developer khác biết cách thiết lập:
```bash
# .env.example
DB_PASSWORD=0209
```
Mỗi developer sẽ copy file `.env.example` thành `.env` (file này đã được đưa vào `.gitignore` để không bị commit lên Git).

### Tại sao không nên dùng giá trị mặc định thẳng trong file (VD: `${DB_PASSWORD:0209}`)?
Nếu bạn thiết lập giá trị mặc định là một mật khẩu thực tế (ví dụ: `0209`), mật khẩu đó vẫn nằm trong code base và được đưa lên Git. Mục tiêu của chúng ta là xóa sổ hoàn toàn mọi vết tích của "Secret thật" trên hệ thống quản lý mã nguồn.

### Câu hỏi phỏng vấn liên quan
Tại sao chúng ta phải lưu secret trong biến môi trường thay vì config file?

### Câu trả lời ngắn gọn
Để đảm bảo **Bảo mật** (không vô tình commit password lên public repo như GitHub) và **Linh hoạt** (dễ dàng deploy cùng một build artifact lên nhiều môi trường khác nhau chỉ bằng cách truyền tập biến môi trường khác nhau mà không cần sửa code).

---

## 35. Kiểm thử Frontend (Frontend Testing) & Mocking trong Vue

### Giải thích ngắn gọn
Trong môi trường Frontend, có hai loại kiểm thử chính:
1. **Component / Integration Testing**: Render một Component duy nhất (bằng jsdom), làm giả (mock) mọi kết nối ra bên ngoài như gọi API hoặc điều hướng URL. Mục tiêu là kiểm tra xem Component có xử lý đúng state và render UI ra chính xác không. Công cụ: `Vitest`, `@vue/test-utils`.
2. **End-to-End (E2E) Testing**: Khởi động toàn bộ ứng dụng trên một trình duyệt thật (như Chrome), mô phỏng cú click chuột thật của người dùng và gọi đến Database thật. Mục tiêu là kiểm tra toàn bộ luồng. Công cụ: `Playwright`, `Cypress`.

### Ví dụ trong project này
Khi test `RegisterPage.vue`, nếu ta để nguyên nó gọi API thật, test sẽ rất chậm và có thể thất bại nếu mạng yếu hoặc Database chưa bật. Thay vào đó, ta sử dụng **Mocking** (làm giả):
```javascript
// Thay thế module auth.service.js bằng một object giả mạo
vi.mock('@/services/auth.service', () => ({
  AuthService: {
    register: vi.fn() // Tạo ra một hàm gián điệp (spy function)
  }
}))

// Giả lập tình huống API trả về lỗi
AuthService.register.mockRejectedValue({
  isAxiosError: true,
  response: { data: { message: 'Email đã được sử dụng' } }
})
```
Bằng cách này, Component vẫn nghĩ rằng nó đang nói chuyện với Backend thật, nhưng thực ra nó nhận được dữ liệu do ta "mớm" sẵn. Điều này giúp test chạy trong vài mili-giây và độc lập hoàn toàn.

### Câu hỏi phỏng vấn liên quan
Tại sao lại phải dùng `vi.mock()` để giả lập API khi viết component test? Sao không dùng API thật cho chính xác?

### Câu trả lời ngắn gọn
Bởi vì mục tiêu của component test là kiểm tra **logic giao diện** (ví dụ: hiển thị thông báo lỗi màu đỏ nếu request thất bại), chứ không phải kiểm tra backend. Nếu dùng API thật, bài test sẽ chậm, phụ thuộc vào môi trường (mạng/database), vi phạm tính cô lập của Unit Test và khó tạo ra các kịch bản lỗi (edge cases) một cách đáng tin cậy.

---

## 36. DTO Contract Alignment trong Frontend Form

### Giải thích ngắn gọn
DTO Contract Alignment nghĩa là frontend form phải gửi dữ liệu đúng với DTO mà backend định nghĩa. Tên field, kiểu dữ liệu, enum value và field bắt buộc phải khớp để API xử lý ổn định.

### Ví dụ trong project này
`CourseCreateReq` cần:
- `title`
- `slug`
- `shortDescription`
- `description`
- `thumbnailUrl`
- `level`
- `courseType`
- `originalPrice`
- `salePrice`

`CourseUpdateReq` giống create nhưng có thêm `status`. Vì vậy `CourseFormModal.vue` chỉ thêm `status` vào payload khi đang ở edit mode.

### Câu hỏi phỏng vấn liên quan
Vì sao frontend không nên tự đặt tên field khác backend DTO?

### Câu trả lời ngắn gọn
Vì backend bind JSON vào DTO theo tên field. Nếu frontend gửi sai tên field, backend có thể nhận `null`, trả validation error hoặc lưu dữ liệu thiếu.

---

## 37. Modal Form Create/Edit Mode

### Giải thích ngắn gọn
Một modal form có thể dùng chung cho cả tạo mới và chỉnh sửa bằng cách xác định mode dựa trên dữ liệu truyền vào. Nếu có object đang edit thì là update mode, nếu không có thì là create mode.

### Ví dụ trong project này
`CourseFormModal.vue` dùng `editingCourse`:
- `editingCourse = null` nghĩa là tạo khóa học mới.
- `editingCourse` có dữ liệu nghĩa là cập nhật khóa học.

Form từ đó quyết định:
- Tiêu đề modal là "Tạo Khóa học mới" hoặc "Cập nhật Khóa học".
- Submit gọi `AdminService.createCourse()` hoặc `AdminService.updateCourse()`.
- Update mode có field `status`, create mode không cần gửi `status`.

### Câu hỏi phỏng vấn liên quan
Lợi ích của việc dùng chung một modal cho create và update là gì?

### Câu trả lời ngắn gọn
Giúp tái sử dụng UI và validation, giảm duplicate code. Nhưng phải tách rõ logic mode để không gửi nhầm payload giữa create và update.

---

## 38. Nested Resource API

### Giải thích ngắn gọn
Nested Resource API là cách thiết kế endpoint thể hiện quan hệ cha-con giữa các tài nguyên. Ví dụ resource thuộc lesson, lesson thuộc section, section thuộc course.

### Ví dụ trong project này
Admin tạo tài liệu cho một lesson bằng endpoint:

```http
POST /api/v1/admin/lessons/{lessonId}/resources
```

URL này cho thấy resource mới sẽ được gắn với lesson có `lessonId`.

### Câu hỏi phỏng vấn liên quan
Khi nào nên dùng nested URL như `/lessons/{id}/resources`?

### Câu trả lời ngắn gọn
Khi tài nguyên con chỉ có ý nghĩa trong ngữ cảnh tài nguyên cha. Lesson resource luôn thuộc một lesson, nên nested URL giúp API rõ nghĩa hơn.

---

## 39. Data Isolation cho Teacher-Owned Content

### Giải thích ngắn gọn
Data isolation là rule đảm bảo user chỉ thao tác được dữ liệu thuộc phạm vi của mình. Với role teacher, điều này thường nghĩa là teacher chỉ được quản lý course/lesson/resource do chính họ sở hữu.

### Ví dụ trong project này
`LessonResourceAdminServiceImpl` kiểm tra course chứa lesson resource. Nếu user không phải `ADMIN` hoặc `SUPER_ADMIN`, hệ thống kiểm tra email teacher của course có khớp user hiện tại không. Nếu không khớp thì ném `DATA_ISOLATION_FORBIDDEN`.

### Câu hỏi phỏng vấn liên quan
Vì sao không chỉ dựa vào frontend để ẩn nút sửa/xóa resource?

### Câu trả lời ngắn gọn
Vì frontend có thể bị bypass bằng Postman hoặc script. Backend phải tự kiểm tra quyền để bảo vệ dữ liệu thật.

---

## 40. Isolated Auxiliary Error Handling

### Giải thích ngắn gọn
Isolated Auxiliary Error Handling là cách xử lý lỗi của các phần phụ trợ sao cho lỗi đó không làm hỏng luồng chính của màn hình. Một UI có thể có nội dung chính và nhiều panel phụ, mỗi phần nên có error state riêng khi hợp lý.

### Ví dụ trong project này
Trong `LessonLearningPage.vue`, nếu API lấy tài liệu đính kèm bị lỗi, trang chỉ hiển thị lỗi trong panel tài liệu. Nội dung bài học, video và progress vẫn hoạt động bình thường.

### Câu hỏi phỏng vấn liên quan
Vì sao không nên đưa user ra khỏi trang học bài khi load resource lỗi?

### Câu trả lời ngắn gọn
Vì resource chỉ là phần bổ trợ. Nếu phần học bài chính vẫn tải được, user vẫn nên tiếp tục học thay vì bị chặn bởi lỗi của panel phụ.

---

## 41. Safe External Links

### Giải thích ngắn gọn
Khi frontend mở link bên ngoài ở tab mới bằng `target="_blank"`, nên thêm `rel="noopener noreferrer"` để giảm rủi ro bảo mật liên quan tới `window.opener`.

### Ví dụ trong project này
Resource link trong trang học bài mở `fileUrl` ở tab mới:

```html
<a :href="res.fileUrl" target="_blank" rel="noopener noreferrer">
  {{ res.title }}
</a>
```

### Câu hỏi phỏng vấn liên quan
`rel="noopener noreferrer"` dùng để làm gì?

### Câu trả lời ngắn gọn
Nó ngăn tab mới truy cập lại tab gốc qua `window.opener`, giúp giảm rủi ro tabnabbing và một số hành vi không an toàn khi mở link bên ngoài.

---

## 42. Curriculum Context Endpoint

### Giải thích ngắn gọn
Curriculum context endpoint là API trả dữ liệu ngữ cảnh chương trình học xung quanh một lesson hiện tại. Dữ liệu này thường gồm course, sections, lessons, trạng thái tiến độ và bài trước/bài sau.

### Ví dụ trong project này
Trang học bài gọi:

```http
GET /api/v1/lessons/{lessonId}/curriculum
```

Backend trả về course title, danh sách section/lesson, `previousLessonId` và `nextLessonId` để frontend render sidebar học tập.

### Câu hỏi phỏng vấn liên quan
Vì sao không để frontend tự lấy toàn bộ course detail rồi tự tính previous/next lesson?

### Câu trả lời ngắn gọn
Vì backend là nơi nắm rule nghiệp vụ như lesson published, enrollment và sort order. Backend tính sẵn giúp frontend đơn giản hơn và tránh lộ dữ liệu không nên hiển thị.

---

## 43. Route Param Watcher trong Vue

### Giải thích ngắn gọn
Route param watcher là cách theo dõi thay đổi của tham số trên URL để component load lại dữ liệu khi route đổi nhưng component không bị destroy/recreate.

### Ví dụ trong project này
`LessonLearningPage.vue` dùng route `/student/lessons/:id`. Khi student click lesson khác trong sidebar, route vẫn dùng cùng component nhưng `id` thay đổi. Component cần watch `route.params.id` để fetch lại lesson, resources và curriculum.

### Câu hỏi phỏng vấn liên quan
Vì sao chuyển từ `/student/lessons/1` sang `/student/lessons/2` đôi khi không tự chạy lại `onMounted()`?

### Câu trả lời ngắn gọn
Vì Vue Router có thể tái sử dụng cùng component cho cùng route pattern. `onMounted()` chỉ chạy khi component mount, nên cần watch route param để xử lý thay đổi dữ liệu.

---

## 44. Current User Endpoint Pattern

### Giải thích ngắn gọn
Current User Endpoint Pattern là cách thiết kế API dùng `/me` để đại diện cho user hiện tại. Backend xác định user bằng token/session thay vì nhận userId từ client.

### Ví dụ trong project này
Student cập nhật profile bằng:

```http
PUT /api/users/me
```

Backend lấy email/user hiện tại từ `SecurityContextHolder`, sau đó query `UserRepository.findByEmail()`.

### Câu hỏi phỏng vấn liên quan
Vì sao `/users/me` thường an toàn hơn `/users/{id}` cho màn hình profile cá nhân?

### Câu trả lời ngắn gọn
Vì client không được chọn userId cần sửa. Backend tự xác định user từ token đã xác thực, giúp tránh lỗi user sửa dữ liệu của người khác.

---

## 45. BCrypt Password Verification

### Giải thích ngắn gọn
BCrypt Password Verification là quá trình kiểm tra mật khẩu người dùng nhập với password hash đã lưu trong database bằng `PasswordEncoder.matches()`.

### Ví dụ trong project này
Khi đổi mật khẩu, `UserServiceImpl` kiểm tra `currentPassword` trước. Nếu đúng, backend encode `newPassword` rồi lưu vào `passwordHash`.

### Câu hỏi phỏng vấn liên quan
Vì sao không so sánh trực tiếp `currentPassword.equals(user.getPasswordHash())`?

### Câu trả lời ngắn gọn
Vì database lưu hash, không lưu mật khẩu gốc. BCrypt còn có salt nên mỗi lần hash có thể khác nhau; phải dùng `PasswordEncoder.matches()` để xác thực đúng cách.

---

## 46. Smoke Test

### Giải thích ngắn gọn
Smoke test là kiểm thử nhanh các luồng chính để chắc rằng hệ thống không bị lỗi nghiêm trọng sau khi build hoặc sau một loạt thay đổi.

### Ví dụ trong project này
Luồng smoke test P0 gồm: guest xem khóa học, student đăng ký/đăng nhập/enroll/học bài/cập nhật profile, admin quản lý user và course structure.

### Câu hỏi phỏng vấn liên quan
Smoke test có thay thế unit test hoặc integration test không?

### Câu trả lời ngắn gọn
Không. Smoke test chỉ kiểm tra hệ thống có chạy được các luồng chính hay không. Unit/integration test vẫn cần để kiểm tra logic chi tiết và tự động hóa hồi quy.

---

## 47. Environment Variable và Secret Management

### Giải thích ngắn gọn
Environment variable là biến cấu hình được truyền từ môi trường chạy app, ví dụ database password hoặc JWT secret. Secret management là cách quản lý các giá trị nhạy cảm đó để không lộ ra source code.

### Ví dụ trong project này
Backend dùng `.env` để cấu hình database password, admin password và JWT secret cho local development.

### Câu hỏi phỏng vấn liên quan
Vì sao nên commit `.env.example` thay vì commit `.env` thật?

### Câu trả lời ngắn gọn
`.env.example` cho người khác biết cần cấu hình key nào nhưng không chứa secret thật. `.env` thật thường chứa password/token nên cần để ngoài git.

---

## 50. Enrollment-Driven CTA

### Giải thích ngắn gọn
Enrollment-Driven CTA là cách render nút hành động chính dựa trên trạng thái ghi danh thật của user với course. CTA không chỉ phụ thuộc course miễn phí/trả phí mà còn phụ thuộc user đã enroll hay chưa.

### Ví dụ trong project này
Ở `CourseDetailPage.vue`, nếu student đã enroll course, nút chính phải là "Tiếp tục học". Nếu chưa enroll và course free, nút mới là "Đăng ký học miễn phí".

### Câu hỏi phỏng vấn liên quan
Vì sao không nên luôn hiển thị nút "Đăng ký" trên course detail?

### Câu trả lời ngắn gọn
Vì user đã enroll sẽ bị hiểu nhầm và có thể bấm đăng ký lại. UI nên phản ánh trạng thái nghiệp vụ hiện tại để flow tự nhiên hơn.

---

## 51. Route Layout Separation

### Giải thích ngắn gọn
Route Layout Separation là cách tách các nhóm route vào layout khác nhau tùy mục tiêu sử dụng. Một route quản lý/dashboard không nhất thiết dùng chung layout với route học bài.

### Ví dụ trong project này
`/student/dashboard` và `/student/my-courses` có thể dùng `StudentLayout`, nhưng `/student/lessons/:id` nên dùng `LearningLayout` để tạo không gian học tập riêng.

### Câu hỏi phỏng vấn liên quan
Vì sao tách `LearningLayout` khỏi `StudentLayout` có thể cải thiện UX?

### Câu trả lời ngắn gọn
Vì trang học cần tập trung vào lesson content và curriculum. Dashboard sidebar hoặc mobile bottom nav có thể chiếm chỗ và gây nhiễu trong lúc học.

---

## 48. Design Token

### Giải thích ngắn gọn
Design token là các giá trị thiết kế dùng chung như màu sắc, font size, spacing, shadow và border radius. Thay vì mỗi page tự chọn style riêng, toàn bộ app dùng chung một bộ token.

### Ví dụ trong project này
Frontend dùng Tailwind config và global CSS để định nghĩa các màu như `primary`, `background`, `surface`, `secondary` và áp dụng lại trên home page, course list, course detail và student pages.

### Câu hỏi phỏng vấn liên quan
Design token giúp gì khi redesign nhiều màn hình cùng lúc?

### Câu trả lời ngắn gọn
Nó giúp UI nhất quán và dễ chỉnh về sau. Nếu đổi màu brand hoặc spacing, ta chỉnh token thay vì sửa thủ công ở nhiều page.

---

## 49. UI Regression Sau Redesign

### Giải thích ngắn gọn
UI regression là lỗi hành vi hoặc hiển thị phát sinh sau khi sửa giao diện. Redesign có thể làm mất event handler, sai điều kiện render hoặc làm layout vỡ ở một trạng thái cụ thể.

### Ví dụ trong project này
Sau khi redesign, cần test lại các flow như login xong quay về trang chủ, course detail hiển thị đúng trạng thái đã ghi danh, và lesson learning không bị nhốt trong dashboard layout.

### Câu hỏi phỏng vấn liên quan
Vì sao giao diện nhìn đẹp hơn nhưng vẫn có thể là regression?

### Câu trả lời ngắn gọn
Vì UI không chỉ là hình ảnh. Nếu nút, route, state hoặc API call bị sai thì trải nghiệm người dùng vẫn hỏng dù màn hình nhìn đẹp.

---

## 50. Mock Data Context & Lỗi 404 Ẩn Trong Integration Test

### Giải thích ngắn gọn
Trong Spring Boot Integration Test (với `@SpringBootTest` và `@AutoConfigureMockMvc`), test case sẽ khởi động toàn bộ ngữ cảnh ứng dụng (Application Context). Điều này có nghĩa là các tầng Filter, Interceptor, và Service Validation đều sẽ chạy y như môi trường production. 
Nếu dữ liệu mock (làm giả) bị thiếu một thuộc tính cốt lõi nào đó mà Business Logic yêu cầu, thay vì trả về HTTP 500 hay báo lỗi Null, ứng dụng có thể trả về HTTP 404 (do Global Exception Handler map `AppException(NOT_FOUND)` sang HTTP 404).

### Ví dụ trong project này
Lỗi build do `LessonProgressIT` mong đợi kết quả trả về `200 OK` nhưng lại nhận được `404 Not Found`. Nguyên nhân không phải do URL gõ sai, mà do `Course` mock data quên khởi tạo field `status`. 
Khi request đi vào `LearningService`, nó bắt gặp validation logic:
```java
if (course.getStatus() != CourseStatus.PUBLISHED) {
    throw new AppException(ErrorCode.LESSON_NOT_FOUND); 
}
```
Và ném ra lỗi dẫn đến kết quả HTTP Response là 404, khiến Integration Test fail. Việc bổ sung `status(CourseStatus.PUBLISHED)` cho mock data đã khắc phục được hiện tượng này.

### Câu hỏi phỏng vấn liên quan
Tại sao khi viết Integration Test với Spring Boot MockMvc, gọi đúng URL nhưng lại bị báo lỗi 404?

### Câu trả lời ngắn gọn
Ngoài khả năng URL chưa tồn tại, lỗi 404 phổ biến trong `@SpringBootTest` thường do dữ liệu mock (Mock Data) hoặc cơ sở dữ liệu test (Test DB) không thỏa mãn các điều kiện business logic (như check trạng thái active/published). Khi các điều kiện này vi phạm, tầng Service thường ném ra lỗi kiểu `NotFoundException` và ExceptionHandler toàn cục sẽ map exception đó thành mã HTTP 404.

---

## 51. Auth Flow "Escape Hatch" (Lối thoát điều hướng) & UI Testing Resilience

### Giải thích ngắn gọn
- **Auth Flow "Escape Hatch"**: Khi thiết kế màn hình Đăng nhập/Đăng ký, không bao giờ được phép dồn người dùng vào một "ngõ cụt" (nơi họ chỉ có 2 lựa chọn: Đăng nhập hoặc Tắt tab). Cần luôn cung cấp các lối thoát như nút "Quay lại" hoặc liên kết về "Trang chủ" để đảm bảo trải nghiệm người dùng (UX) tự nhiên.
- **UI Testing Resilience**: Khi đập đi xây lại giao diện (Redesign) từ Vanilla CSS sang Tailwind CSS, các file kiểm thử tự động (Unit Tests / Component Tests) rất dễ bị gãy (fail) nếu chúng phụ thuộc quá nhiều vào class CSS hoặc cấu trúc DOM. Để giữ cho Test bền vững, ta cần bảo lưu các class cốt lõi mà Test đang query (ví dụ: `.btn-submit`, `.error-alert`) hoặc sử dụng các attribute độc lập như `data-testid` hoặc `role`.

### Ví dụ trong project này
Trong Layout xác thực mới (`AuthLayout.vue`), logic fallback được áp dụng bằng cách kiểm tra `window.history.length > 2`. Nếu có lịch sử duyệt, nút "Quay lại" xuất hiện; nếu không (ví dụ user copy link gửi cho nhau mở tab mới), nút fallback "Khóa học" sẽ hiển thị.
Khi viết lại giao diện Login/Register bằng Tailwind, mặc dù loại bỏ file `main.css` cũ, nhưng các template class như `.error-alert` và tag `h2` vẫn được giữ nguyên để các file `LoginPage.spec.js` và `RegisterPage.spec.js` (dùng thư viện `@vue/test-utils`) vẫn có thể tìm thấy phần tử DOM và verify text lỗi thành công.

### Câu hỏi phỏng vấn liên quan
Khi bạn được giao nhiệm vụ đập đi xây lại (redesign) một tính năng lớn trên Frontend, bạn làm gì để đảm bảo các bài Automated Test cũ không bị phá hỏng?

### Câu trả lời ngắn gọn
Tôi sẽ ưu tiên tách biệt (decouple) các Test Selectors ra khỏi Styling. Ví dụ, nếu các bài Test cũ đang query DOM thông qua các CSS Class thuần túy (ví dụ: `wrapper.find('.error-alert')`), tôi sẽ bảo lưu chính xác class name đó trong bộ code Tailwind mới, hoặc tốt hơn nữa, tôi sẽ refactor bài Test sang việc sử dụng `data-testid="..."` hoặc `role="..."` để đảm bảo UI/UX Design có thay đổi thế nào thì Logic Test vẫn hoàn toàn ổn định.

---

## 52. Database Migration (Flyway) vs Hibernate `ddl-auto`

### Giải thích ngắn gọn
- **`ddl-auto=update`**: Là cơ chế tự động đồng bộ Schema của Hibernate dựa vào JPA `@Entity`. Rất tiện lợi ở môi trường Dev, nhưng cực kỳ nguy hiểm ở Production. Hibernate không giỏi trong việc dự đoán ý định (VD: Nếu bạn đổi tên một cột `price` thành `sale_price`, Hibernate có thể tạo cột mới `sale_price` và bỏ qua/xóa dữ liệu ở cột `price` cũ).
- **Flyway**: Là công cụ quản lý Database Migration chuyên dụng. Mọi thay đổi về cấu trúc Database (Schema) đều phải được viết thành các file SQL đánh số phiên bản rõ ràng (VD: `V1__init.sql`, `V2__add_index.sql`). 
- Khi dùng Flyway, ta cài đặt Hibernate về chế độ `ddl-auto=validate`. Lúc này, Hibernate không tự sửa Database nữa, nó chỉ so sánh xem Schema hiện tại (do Flyway tạo) có khớp 100% với `@Entity` hay không. Nếu lệch, app sẽ báo lỗi và từ chối khởi động, giúp phát hiện lỗi cấu trúc sớm nhất có thể.

### Ví dụ trong project này
Trong `application-dev.yml`, cấu hình đã được đổi từ `update` thành `validate`.
Cùng với đó, một script thuần MariaDB `V1__init_schema.sql` đã được tạo ra trong thư mục `src/main/resources/db/migration/`. Nó chứa chính xác các câu lệnh `CREATE TABLE` kèm theo các Index tối ưu hóa (như `CREATE INDEX idx_course_status ON courses(status);`).
Khi hệ thống chạy (kể cả lúc test bằng `mvn clean verify`), Flyway sẽ quét file `V1`, tạo bảng, sau đó Spring Data JPA sẽ nhảy vào kiểm chứng. 

### Câu hỏi phỏng vấn liên quan
Tại sao người ta khuyên không bao giờ dùng `spring.jpa.hibernate.ddl-auto=update` ở môi trường Production?

Thay vào đó, ở Production ta sử dụng công cụ như Flyway hoặc Liquibase để kiểm soát từng thay đổi bằng mã SQL tường minh, và chỉ dùng `ddl-auto=validate` để Hibernate kiểm chứng lại xem DB Schema và Java Entities đã khớp nhau hoàn toàn hay chưa. Việc này đảm bảo tính ACID và an toàn dữ liệu tuyệt đối.

## Concept 53: API Abuse Protection & Dual-Key Rate Limiting

### Giải thích ngắn gọn
- **Rate Limiting (Giới hạn tỷ lệ)**: Là kỹ thuật giới hạn số lượng request mà một client có thể gửi đến API trong một khoảng thời gian nhất định (ví dụ: 20 request / 15 phút). Mục đích là để chống spam, chống tấn công từ chối dịch vụ (DDoS) và bảo vệ hệ thống khỏi bị lạm dụng (Abuse).
- **Dual-Key Throttling (Throttling Kép)**: Thay vì chỉ giới hạn theo IP, ta kết hợp thêm một khóa thứ hai (ví dụ: Email/Username). Việc này giải quyết 2 bài toán tấn công Authentication khét tiếng:
  1. **Brute Force (Botnet)**: Kẻ tấn công dùng hàng nghìn IP khác nhau để thử mật khẩu vào CÙNG MỘT tài khoản email. Giới hạn theo IP vô dụng, nên ta cần giới hạn theo **Email** (VD: 1 email chỉ được nhập sai 10 lần / 15 phút).
  2. **Credential Stuffing**: Kẻ tấn công có 1 danh sách hàng triệu email/password rò rỉ, dùng 1 IP để thử đăng nhập tuần tự vào TẤT CẢ các email đó. Giới hạn theo Email vô dụng, nên ta cần giới hạn theo **IP** (VD: 1 IP chỉ được thử 20 lần / 15 phút).
- **In-Memory Sliding Window**: Thuật toán lưu lại thời điểm (timestamp) của mỗi request. Các timestamp cũ hơn khung thời gian cho phép (window) sẽ bị xóa (evict). Ta có thể dùng cấu trúc dữ liệu cơ bản như `ConcurrentHashMap<String, Deque<Instant>>` trong RAM thay vì phải setup nguyên một hệ thống Redis cồng kềnh, lý tưởng cho các ứng dụng nhỏ đến vừa (monolith).
- **Generic 429 Response**: Mã lỗi `429 Too Many Requests` trả về khi bị Rate Limit phải chung chung, tuyệt đối không tiết lộ thông tin kiểu *"Email này đang bị khóa tạm thời"* vì hacker có thể dựa vào đó để khẳng định email có tồn tại (Account Enumeration).

### Ví dụ trong project này
Class `RateLimiterService` lưu cache các mốc thời gian truy cập. Trong `AuthController`, hàm `login()` sẽ kiểm tra song song IP (`X-Forwarded-For`) và Email người dùng truyền vào. Nếu bất kỳ chỉ số nào vượt ngưỡng (`>20 lần/IP` hoặc `>10 lần/Email`), lập tức ném ra lỗi `AppException(ErrorCode.TOO_MANY_REQUESTS)` làm cho API trả về mã lỗi 429 chuẩn xác.

### Câu hỏi phỏng vấn liên quan
Làm thế nào để bảo vệ API Đăng nhập khỏi các cuộc tấn công Brute-force và Credential Stuffing? Tại sao chỉ giới hạn theo IP là không đủ?

### Câu trả lời ngắn gọn
Chỉ giới hạn theo IP là không đủ vì kẻ tấn công có thể sử dụng mạng Botnet (hàng chục nghìn IP ẩn danh) để tấn công tập trung vào một tài khoản duy nhất (Brute-force). 
Để phòng thủ toàn diện, hệ thống cần thiết lập "Dual-Key Rate Limiting": Giới hạn cả số lượng request trên mỗi IP (chống Credential Stuffing - quét diện rộng) VÀ giới hạn số lượng request trên mỗi Email (chống Botnet Brute-force - tấn công tập trung). Đồng thời, thông báo lỗi luôn dùng mã HTTP 429 generic để ngăn ngừa kỹ thuật rà quét tài khoản (Account Enumeration).

## Concept 54: CORS & Cookie SameSite in Production

### Giải thích ngắn gọn
- **CORS (Cross-Origin Resource Sharing)**: Là cơ chế bảo mật của trình duyệt, ngăn không cho trang web ở domain A (ví dụ `app.com`) gọi API ngầm sang domain B (ví dụ `api.com`). Trừ khi backend ở domain B cấu hình header `Access-Control-Allow-Origin: app.com` cho phép. 
  - Lưu ý: Trình duyệt luôn gửi một request thăm dò gọi là **Pre-flight request (OPTIONS)** trước khi gửi request thật. Nếu Spring Security chặn mất request `OPTIONS` này (trả về 401), luồng CORS của trình duyệt sẽ chết cứng. Do đó phải tích hợp CORS vào thẳng Spring Security (`.cors()`).
- **Cookie SameSite**: Cookie dùng để lưu Refresh Token (hoặc Session). Trình duyệt có quy định gắt gao về việc gửi Cookie xuyên miền (Cross-site):
  - **Lax**: (Mặc định) Cookie chỉ được gửi đi nếu request diễn ra trên cùng một Site (cùng domain mẹ, ví dụ `app.example.com` gọi `api.example.com`).
  - **None**: Cho phép gửi Cookie xuyên miền (ví dụ `my-frontend.vercel.app` gọi API `my-backend.herokuapp.com`). TUY NHIÊN, để chống CSRF, trình duyệt **bắt buộc** Cookie `SameSite=None` phải đi kèm cờ `Secure=true` (chỉ chạy trên HTTPS). Nếu set `None` mà quên `Secure`, trình duyệt sẽ lẳng lặng vứt Cookie đó đi, gây ra lỗi đăng nhập ngầm rất khó debug.

### Ví dụ trong project này
Biến môi trường `app.cors.allowed-origins` được đưa vào để cấu hình nhiều domain tĩnh ở môi trường Production.
Class `CookieUtil` được trang bị thêm cơ chế phòng vệ **Fast-Fail** (`@PostConstruct`). Nó sẽ kiểm tra lúc khởi động: Nếu lập trình viên cấu hình `same-site: None` nhưng biến `secure: false`, Spring Boot sẽ văng exception và **từ chối khởi động (Crash)** ngay lập tức, ngăn ngừa việc đẩy code lỗi lên Production.

### Câu hỏi phỏng vấn liên quan
Khi deploy ứng dụng Frontend và Backend ở hai domain hoàn toàn khác nhau, tại sao user đăng nhập thành công, Server có trả về Set-Cookie Header chứa Refresh Token, nhưng các request API sau đó trình duyệt lại không chịu gửi Cookie đi?

### Câu trả lời ngắn gọn
Đó là do vi phạm chính sách SameSite của trình duyệt. Khi Frontend và Backend khác domain (Cross-site request), trình duyệt sẽ từ chối gửi Cookie trừ khi Cookie đó được gán 2 cờ: `SameSite=None` VÀ `Secure=true`. Nếu Server trả về `SameSite=None` nhưng lại quên bật cờ `Secure`, hoặc Server đang chạy trên HTTP (không mã hóa), trình duyệt (như Chrome/Firefox) sẽ chủ động block và loại bỏ Cookie đó để đảm bảo an toàn bảo mật, dẫn đến hiện tượng lỗi ngầm (silent failure).

## Concept 55: Giả lập (Mocking) Lỗi Thực Tế trong Frontend Unit Test

### Giải thích ngắn gọn
- Khi viết Unit Test cho luồng gọi API (ví dụ bằng Axios), một sai lầm cực kỳ phổ biến của Junior Developer là giả lập (mock) lỗi quá hời hợt. Ví dụ: `mockRejectedValue(new Error('Lỗi'))` hoặc `mockRejectedValue({ message: 'Lỗi' })`.
- Trong thực tế, các thư viện HTTP client như Axios trả về một cấu trúc lỗi rất phức tạp (Error Schema). Một lỗi Axios chuẩn (AxiosError) thường có `error.isAxiosError = true`, `error.response.data`, `error.response.status`, và `error.code`.
- Nếu Frontend code của bạn cố gắng bóc tách lỗi bằng cách đọc `error.response.data.message`, nhưng trong Unit Test bạn chỉ trả về `{ message: 'Lỗi' }`, test có thể PASS (nếu bạn không assert kỹ), nhưng ra thực tế Production, khi mạng rớt (Network Error), biến `error.response` sẽ bị `undefined`, dẫn đến ứng dụng bị crash trắng trang với lỗi `Cannot read properties of undefined (reading 'data')`.
- **Giải pháp**: Xây dựng các hàm Helper để giả lập chính xác 100% cấu trúc lỗi (Realistic Error Shapes) bao phủ đủ các trường hợp: Lỗi do Backend (4xx, 5xx có response), Lỗi mất kết nối mạng (Network Error không có response), Lỗi Timeout, v.v.

### Ví dụ trong project này
Trong `LoginPage.spec.js` và `RegisterPage.spec.js`, thay vì mock đơn giản, chúng ta đã tạo ra các helper:
1. `makeAxiosApiError(status, code, message)`: Giả lập lỗi từ Backend trả về đúng cấu trúc chuẩn `ApiResponse` (`response.data.code`, `response.data.message`).
2. `makeAxiosNetworkError()`: Giả lập rớt mạng. Biến `isAxiosError: true` nhưng `response: undefined`.
3. `makeAxiosTimeoutError()`: Giả lập lỗi timeout (`code: 'ECONNABORTED'`).
Nhờ vậy, Unit Test của chúng ta bao phủ (cover) toàn bộ các nhánh logic trong hàm `getApiErrorMessage()`, đảm bảo app không bao giờ bị crash vì lỗi chưa lường trước.

### Câu hỏi phỏng vấn liên quan
Tại sao khi viết Unit test xử lý lỗi API (Axios), ứng dụng test vẫn Pass nhưng khi mang lên môi trường thực tế gặp lỗi rớt mạng thì app lại bị crash (trắng màn hình)?

### Câu trả lời ngắn gọn
Nguyên nhân thường do lập trình viên giả lập (mock) cấu trúc lỗi không sát thực tế (Shallow Mocking). Khi test, dev thường mock lỗi có chứa object `response.data`. Nhưng trên thực tế, khi gặp Network Error (rớt mạng) hoặc Timeout, Axios không nhận được phản hồi từ server nên object `error.response` sẽ là `undefined`. Nếu Frontend cố truy cập `error.response.data.message` mà không kiểm tra trước, JavaScript sẽ quăng lỗi `TypeError: Cannot read properties of undefined` gây crash toàn bộ ứng dụng (White screen of death). Để khắc phục, cần viết test mô phỏng chính xác các Error Shapes khác nhau (có response và không có response).
