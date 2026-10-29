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

## 56. `.env.example` Whitelist trong Git

### Giải thích ngắn gọn
Khi project ignore các file môi trường như `.env`, đôi khi ta vẫn muốn commit `.env.example` để làm mẫu setup. Khi đó `.gitignore` cần rule phủ định để whitelist file mẫu.

### Ví dụ trong project này
`backend/.gitignore` có thể ignore `.env` nhưng cho phép track `.env.example`:

```gitignore
.env
!.env.example
```

### Câu hỏi phỏng vấn liên quan
Vì sao `.env.example` không nên bị ignore?

### Câu trả lời ngắn gọn
Vì `.env.example` là tài liệu setup quan trọng, giúp developer biết project cần biến môi trường nào mà không lộ secret thật.

---

## 57. Quiz Attempt

### Giải thích ngắn gọn
Quiz attempt là một lần làm bài quiz của user. Một user có thể làm cùng một quiz nhiều lần nếu hệ thống cho phép, và mỗi lần làm cần lưu điểm, thời gian, trạng thái và kết quả riêng.

### Ví dụ trong project này
Entity `QuizAttempt` lưu `user`, `quiz`, `startedAt`, `submittedAt`, `score`, `passed` và `status`. Khi student bấm bắt đầu quiz ở task sau, backend sẽ tạo một attempt mới.

### Câu hỏi phỏng vấn liên quan
Vì sao không lưu điểm trực tiếp vào bảng `quizzes`?

### Câu trả lời ngắn gọn
Vì `quizzes` là đề quiz dùng chung cho nhiều user. Điểm là kết quả của từng user trong từng lần làm, nên phải lưu ở bảng `quiz_attempts`.

---

## 58. EnumType.STRING trong JPA

### Giải thích ngắn gọn
`EnumType.STRING` là cách lưu enum vào database bằng tên enum thay vì số thứ tự. Nó giúp dữ liệu dễ đọc và an toàn hơn khi enum thay đổi thứ tự.

### Ví dụ trong project này
`QuizStatus.DRAFT` được lưu trong database là chuỗi `DRAFT`, không phải số `0`.

### Câu hỏi phỏng vấn liên quan
Rủi ro của `EnumType.ORDINAL` là gì?

### Câu trả lời ngắn gọn
Nếu đổi thứ tự enum trong code, số ordinal cũ trong database có thể trỏ sang ý nghĩa khác, gây lỗi dữ liệu nghiêm trọng.

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

---

## 59. Publish Workflow

### Giải thích ngắn gọn
Publish workflow là quy trình chuyển dữ liệu từ trạng thái nháp sang trạng thái được hiển thị cho user. Trước khi publish thường cần kiểm tra dữ liệu đã đủ điều kiện hay chưa.

### Ví dụ trong project này
Quiz mới tạo mặc định là `DRAFT`. Khi admin gọi publish, backend kiểm tra quiz đã có ít nhất một câu hỏi rồi mới đổi status thành `PUBLISHED`.

### Câu hỏi phỏng vấn liên quan
Vì sao không nên cho publish quiz rỗng?

### Câu trả lời ngắn gọn
Vì quiz rỗng không dùng được cho student và có thể làm hỏng flow start/submit quiz.

---

## 60. Historical Data Integrity trong Quiz

### Giải thích ngắn gọn
Historical Data Integrity là việc bảo vệ dữ liệu lịch sử để kết quả cũ không bị sai khi dữ liệu gốc thay đổi. Với quiz, nếu student đã làm bài, câu hỏi/đáp án liên quan cần được bảo vệ hoặc snapshot.

### Ví dụ trong project này
Backend chặn sửa/xóa question hoặc answer nếu đã có `QuizAttemptAnswer` liên quan, để lịch sử làm bài không bị biến dạng.

### Câu hỏi phỏng vấn liên quan
Vì sao sửa đáp án sau khi student đã làm quiz có thể nguy hiểm?

### Câu trả lời ngắn gọn
Vì kết quả cũ có thể dựa trên đáp án cũ. Nếu thay đổi đáp án gốc mà không snapshot/versioning, điểm và giải thích lịch sử có thể trở nên sai.

---

## 61. Answer Leakage Prevention

### Giải thích ngắn gọn
Answer Leakage Prevention là việc thiết kế API để không làm lộ đáp án đúng trước thời điểm người học nộp bài. Với quiz, backend có thể lưu `isCorrect`, nhưng response cho student khi đang làm bài phải loại bỏ field này.

### Ví dụ trong project này
`GET /api/v1/quizzes/{id}` trả danh sách câu hỏi và đáp án cho student, nhưng `AnswerLearningRes` chỉ chứa `id`, `content`, `sortOrder`, không trả `isCorrect`.

### Câu hỏi phỏng vấn liên quan
Vì sao không thể chỉ ẩn đáp án đúng bằng frontend?

### Câu trả lời ngắn gọn
Vì user có thể mở DevTools hoặc đọc network response. Nếu backend đã gửi `isCorrect`, dữ liệu coi như đã bị lộ.

---

## 62. Server-Side Quiz Scoring

### Giải thích ngắn gọn
Server-side scoring là việc backend tự tính điểm dựa trên đáp án gốc trong database, thay vì để frontend gửi điểm lên. Cách này bảo vệ tính toàn vẹn kết quả vì client luôn có thể bị sửa.

### Ví dụ trong project này
Khi student gọi `POST /api/v1/quizzes/{id}/submit`, backend lấy question/answer từ database, so sánh `answerId` đã chọn với answer có `isCorrect = true`, sau đó lưu score, correct count, wrong count và trạng thái passed vào `QuizAttempt`.

### Câu hỏi phỏng vấn liên quan
Vì sao frontend không nên tự tính điểm rồi gửi score lên backend?

### Câu trả lời ngắn gọn
Vì frontend chạy trên máy người dùng nên có thể bị chỉnh sửa. Backend phải là nơi quyết định điểm cuối cùng để tránh gian lận và giữ kết quả nhất quán.

---

## 63. Attempt Ownership Check

### Giải thích ngắn gọn
Attempt ownership check là bước xác minh phiên làm bài có thuộc về user hiện tại hay không. Đây là lớp bảo vệ bắt buộc cho submit/result API vì `attemptId` là dữ liệu có thể bị đoán hoặc thay đổi trên URL/request.

### Ví dụ trong project này
Khi submit quiz, `QuizLearningServiceImpl` lấy attempt theo `attemptId`, sau đó so sánh `attempt.getUser().getId()` với current user. Nếu không khớp, backend ném `QUIZ_ATTEMPT_FORBIDDEN`.

### Câu hỏi phỏng vấn liên quan
Vì sao kiểm tra JWT hợp lệ vẫn chưa đủ khi xem result của quiz attempt?

### Câu trả lời ngắn gọn
JWT chỉ chứng minh user đã đăng nhập. Backend vẫn phải kiểm tra resource-level permission để chắc chắn attempt đó thuộc về user hiện tại hoặc user có quyền admin.

---

## 64. Frontend API Service Layer

### Giải thích ngắn gọn
Frontend API Service Layer là lớp gom các lời gọi HTTP theo từng domain, ví dụ `quiz.service.js` cho toàn bộ API quiz. Component chỉ gọi hàm nghiệp vụ như `startQuiz()` hoặc `submitQuiz()` thay vì tự viết URL ở nhiều nơi.

### Ví dụ trong project này
`QuizTakingPage.vue`, `QuizResultPage.vue` và `StudentDashboardPage.vue` đều dùng `QuizService` để gọi backend quiz APIs.

### Câu hỏi phỏng vấn liên quan
Vì sao không nên gọi `api.get(...)` rải rác trực tiếp trong nhiều component?

### Câu trả lời ngắn gọn
Vì endpoint, payload và response contract sẽ bị lặp. Khi backend đổi API, ta phải sửa nhiều nơi và dễ tạo bug không nhất quán.

---

## 65. Attempt-Based Quiz UI State

### Giải thích ngắn gọn
Attempt-based UI state là cách frontend quản lý quiz theo phiên làm bài cụ thể. Trước khi start chỉ hiển thị thông tin quiz; sau khi backend trả `attemptId`, UI mới cho submit đáp án.

### Ví dụ trong project này
`QuizTakingPage.vue` lưu `attemptId` sau khi gọi `startQuiz()`. Khi submit, payload gửi cả `attemptId` và danh sách answer để backend kiểm tra phiên làm bài.

### Câu hỏi phỏng vấn liên quan
Vì sao không nên cho user submit quiz nếu chưa có `attemptId`?

### Câu trả lời ngắn gọn
Vì backend cần biết submit này thuộc phiên làm bài nào, của user nào và trạng thái attempt hiện tại có còn hợp lệ không.

---

## 66. Discoverability Gap trong User Flow

### Giải thích ngắn gọn
Discoverability gap là khoảng trống khi tính năng đã tồn tại nhưng user không có đường đi tự nhiên để tìm thấy nó. API và page có thể chạy đúng, nhưng flow vẫn chưa trọn nếu user phải biết URL hoặc ID thủ công.

### Ví dụ trong project này
Frontend đã có route `/student/quizzes/:quizId`, nhưng lesson learning page cần biết lesson hiện tại có quiz nào để hiển thị nút "Làm quiz". Vì vậy task tiếp theo nên bổ sung API discover quiz theo lesson/course hoặc tích hợp dữ liệu đó vào lesson response.

### Câu hỏi phỏng vấn liên quan
Vì sao tính năng đã code xong nhưng vẫn có thể chưa hoàn thiện về UX?

### Câu trả lời ngắn gọn
Vì user cần đường đi rõ ràng trong luồng sử dụng thật. Nếu chỉ truy cập được bằng URL thủ công, tính năng chưa thật sự được tích hợp vào sản phẩm.

---

## 67. Discovery API

### Giải thích ngắn gọn
Discovery API là endpoint giúp frontend tìm các tài nguyên liên quan trong một ngữ cảnh cụ thể, ví dụ lesson hiện tại có quiz nào. Nó thường trả metadata vừa đủ để render link, card hoặc CTA, không trả toàn bộ detail.

### Ví dụ trong project này
`GET /api/v1/lessons/{lessonId}/quizzes` trả danh sách quiz published gắn với lesson, gồm title, question count, latest attempt và remaining attempts để lesson page hiện nút "Làm quiz" hoặc "Xem kết quả".

### Câu hỏi phỏng vấn liên quan
Vì sao không bắt frontend tự đoán URL quiz bằng id hardcode?

### Câu trả lời ngắn gọn
Vì frontend không nên biết dữ liệu nghiệp vụ cố định. Backend phải cung cấp API discovery để UI hiển thị đúng theo dữ liệu thật.

---

## 68. Metadata Response Design

### Giải thích ngắn gọn
Metadata response design là cách thiết kế response chỉ gồm thông tin tóm tắt cần thiết cho một màn hình. Nó giúp API nhẹ hơn, bảo mật hơn và tránh frontend phụ thuộc vào dữ liệu chi tiết không cần dùng.

### Ví dụ trong project này
`QuizDiscoveryRes` trả `id`, `title`, `questionCount`, `latestAttemptId`, `latestScore`, `latestPassed`, `remainingAttempts`, nhưng không trả questions hoặc answers.

### Câu hỏi phỏng vấn liên quan
Vì sao discovery response không nên tái sử dụng luôn quiz detail response?

### Câu trả lời ngắn gọn
Vì detail response có thể quá nặng hoặc chứa dữ liệu không cần thiết. Discovery chỉ cần metadata để render entry point.

---

## 69. Contextual CTA

### Giải thích ngắn gọn
Contextual CTA là nút hành động xuất hiện đúng lúc, đúng nơi trong user flow. Thay vì bắt user tự tìm tính năng, giao diện đưa hành động phù hợp vào ngữ cảnh hiện tại.

### Ví dụ trong project này
Khi student đang học một lesson có quiz published, `LessonLearningPage.vue` hiển thị CTA "Làm bài", "Làm lại" hoặc "Xem kết quả" dựa trên discovery response.

### Câu hỏi phỏng vấn liên quan
Vì sao contextual CTA có thể quan trọng hơn việc chỉ tạo route mới?

### Câu trả lời ngắn gọn
Vì route mới chỉ làm tính năng tồn tại, còn contextual CTA giúp user thật sự tìm thấy và sử dụng tính năng trong luồng học tự nhiên.

---

## 70. Design Tokens - Token Thiết Kế

### Giải thích ngắn gọn
Design tokens là các giá trị thiết kế nguyên tử (atomic design values) được đặt tên và lưu trữ dưới dạng biến (CSS variables, JSON, YAML). Chúng đại diện cho các quyết định thiết kế cụ thể: màu sắc, font, spacing, border-radius, shadow... Thay vì hardcode giá trị hex khắp codebase, ta dùng token (`--stitch-primary`, `bg-stitch-card`) → thay đổi 1 nơi, cập nhật toàn bộ giao diện.

### Ví dụ trong project này
Stitch dùng `:root { --primary: #c1184a; }` rồi map vào Tailwind qua `@theme inline`. Dự án Vue chuyển đổi thành `--stitch-primary: #c1184a` trong `@layer base` và extend Tailwind config: `"stitch-primary": "var(--stitch-primary)"`. Prefix `stitch-` tránh xung đột với bảng màu Material Design đang dùng (`primary: #8f0020`).

### Câu hỏi phỏng vấn liên quan
Khi một dự án đã có hệ thống màu sắc, làm thế nào để thêm design tokens mới mà không phá vỡ giao diện hiện tại?

### Câu trả lời ngắn gọn
Dùng namespace/prefix riêng cho bộ tokens mới (ví dụ `stitch-*`), để hai hệ thống cùng tồn tại. Migrate từng page dần dần, chỉ xóa tokens cũ khi chắc chắn không còn reference nào.

---

## 71. Compound Component Pattern (Vue) - Mẫu Component Kết Hợp

### Giải thích ngắn gọn
Pattern chia một UI phức tạp thành nhiều sub-components có API riêng (props/slots) nhưng được thiết kế để dùng cùng nhau. Ví dụ: `<Card>` + slot nội dung, `<Modal>` + slot `footer`, `<EmptyState>` + slot `icon` và `action`. Mỗi sub-component tập trung một responsibility, nhưng khi kết hợp tạo thành UI phong phú.

### Ví dụ trong project này
`EmptyState.vue` có 3 slots: `icon` (custom SVG), default (mô tả), và `action` (nút CTA). Sử dụng:
```vue
<EmptyState title="Chưa có bài tập" description="Hãy tạo bài tập đầu tiên">
  <template #icon><MyIcon /></template>
  <template #action><Button>Tạo ngay</Button></template>
</EmptyState>
```

### Câu hỏi phỏng vấn liên quan
Slots trong Vue 3 khác gì với children trong React?

### Câu trả lời ngắn gọn
Vue slots hỗ trợ named slots natively (nhiều vùng nội dung trong 1 component), còn React dùng `children` cho 1 vùng và cần dùng props riêng hoặc compound pattern phức tạp hơn cho nhiều vùng.

---

## 72. Teleport/Portal Pattern - Kỹ Thuật Render Ngoài DOM Tree

### Giải thích ngắn gọn
`<Teleport>` (Vue 3) hay Portal (React) cho phép render một phần DOM vào vị trí khác trong DOM tree, thường là `<body>`, thay vì render tại vị trí khai báo trong component tree. Điều này giải quyết vấn đề stacking context (`z-index`, `overflow: hidden`) khiến overlay/modal bị cắt hoặc bị che.

### Ví dụ trong project này
`Modal.vue` dùng `<Teleport to="body">` để render backdrop và dialog ra ngoài `#app`. Nếu không dùng Teleport, Modal được render bên trong một container có `overflow: hidden` sẽ bị cắt mất phần nền mờ.

### Câu hỏi phỏng vấn liên quan
Tại sao Modal nên dùng Teleport thay vì render trực tiếp trong component cha?

### Câu trả lời ngắn gọn
Vì component cha có thể có `overflow: hidden`, `transform`, hoặc `z-index` tạo stacking context mới, khiến modal không hiển thị đúng trên toàn trang. Teleport đưa DOM node ra `<body>`, thoát khỏi mọi stacking context.


## 73. Component Extraction - Tách Component Dùng Chung

### Giải thích ngắn gọn
Component Extraction là kỹ thuật refactor đưa một đoạn UI lặp lại giữa nhiều file thành một component riêng biệt, import và tái sử dụng. Mục tiêu là Single Source of Truth cho phần UI đó: sửa 1 chỗ, cập nhật tất cả nơi sử dụng.

### Ví dụ trong project này
Navbar ban đầu được viết inline trong `MainLayout.vue` (~100 dòng), `StudentLayout.vue` có sidebar riêng (~90 dòng), mỗi nơi tự xử lý auth state, logout, navigation. Tách thành `Navbar.vue` component chung, cả 2 layouts chỉ cần `<Navbar />` → giảm duplicate, đảm bảo UX nhất quán.

### Câu hỏi phỏng vấn liên quan
Khi nào nên tách inline UI thành shared component, và khi nào nên giữ inline?

### Câu trả lời ngắn gọn
Tách khi: logic và visual lặp lại ≥ 2 nơi, hoặc component đủ phức tạp để có lifecycle/state riêng. Giữ inline khi: layout-specific (ví dụ AdminLayout sidebar chỉ dùng ở admin) và không có nhu cầu reuse.

---

## 74. Role-based UI Rendering - Hiển Thị Giao Diện Theo Vai Trò

### Giải thích ngắn gọn
Kỹ thuật thay đổi nội dung hiển thị trên UI dựa trên vai trò (role) của user đang đăng nhập. Khác với role-based access control (chặn ở router/backend), đây là việc ẩn/hiện các element trên cùng một component.

### Ví dụ trong project này
`Navbar.vue` sử dụng `computed` từ `auth.store` để xác định `isLoggedIn`, `isAdmin`, `dashboardRoute`. Guest thấy nút Đăng nhập/Đăng ký. Student thấy Dashboard + Profile + Avatar. Admin thấy link Admin panel. Tất cả trong cùng 1 component, không cần 3 navbar riêng.

### Câu hỏi phỏng vấn liên quan
Role-based UI rendering ở frontend có thay thế được backend authorization không?

### Câu trả lời ngắn gọn
Không. Frontend rendering chỉ là UX convenience (ẩn nút để user không nhầm lẫn). Backend authorization (router guards + API middleware) mới là security boundary thực sự. User có thể bypass frontend bằng DevTools hoặc gọi API trực tiếp.

---

## 75. Click Outside Pattern - Đóng Menu Khi Click Bên Ngoài

### Giải thích ngắn gọn
Pattern xử lý đóng dropdown/popover khi user click ra bên ngoài vùng menu. Thực hiện bằng cách đăng ký event listener trên `document` và kiểm tra `event.target` có nằm trong container menu không.

### Ví dụ trong project này
`Navbar.vue` dùng `@click.stop` trên mobile menu container để ngăn event bubbling, kết hợp `document.addEventListener('click', handleClickOutside)` để đóng menu khi click bất kỳ đâu ngoài menu. Phải cleanup listener trong `onUnmounted` để tránh memory leak.

### Câu hỏi phỏng vấn liên quan
Tại sao cần `@click.stop` trên menu container khi đã có `handleClickOutside`?

### Câu trả lời ngắn gọn
Vì click bên trong menu cũng bubble lên `document`, trigger `handleClickOutside` và đóng menu trước khi router-link kịp navigate. `.stop` ngăn event lan tới document, chỉ cho phép đóng khi click thật sự ở ngoài.

---

## 76. Vue Transition Component - Animation Enter/Leave

### Giải thích ngắn gọn
`<Transition>` là built-in component của Vue 3 cho phép thêm animation CSS khi element được insert (`v-if` true) hoặc remove (`v-if` false) khỏi DOM. Hỗ trợ class-based hoặc JavaScript hooks. Chỉ wrap được 1 child element trực tiếp.

### Ví dụ trong project này
Mobile menu trong `Navbar.vue` dùng `<Transition>` với `enter-from-class="opacity-0 -translate-y-4"` → `enter-to-class="opacity-100 translate-y-0"` tạo hiệu ứng slide-down mượt mà khi mở menu. Leave animation ngược lại: slide-up + fade-out.

### Câu hỏi phỏng vấn liên quan
`<Transition>` và `<TransitionGroup>` khác nhau thế nào?

### Câu trả lời ngắn gọn
`<Transition>` chỉ wrap 1 element, dùng cho toggle show/hide. `<TransitionGroup>` wrap danh sách (v-for), hỗ trợ thêm move transitions khi item thay đổi vị trí. TransitionGroup render wrapper element thật (mặc định `<span>`), Transition không render gì.


## 77. URL Query Sync - Đồng Bộ State Ứng Dụng Với URL

### Giải thích ngắn gọn
URL Query Sync là kỹ thuật lưu trạng thái giao diện (filters, search, page number, sort) vào URL query parameters (`?keyword=N5&page=2`). Khi user refresh trang, share link, hoặc bấm Back/Forward, ứng dụng khôi phục đúng trạng thái từ URL. Đây là yêu cầu cơ bản của bất kỳ trang có filter/search nào trong production.

### Ví dụ trong project này
`CourseListPage.vue` dùng `router.replace({ query })` để cập nhật URL mỗi khi user thay đổi filter, search, sort hoặc page. Kết hợp `watch(() => route.query)` để phản ứng khi user bấm Back/Forward. Khi mount, `syncFiltersFromUrl()` đọc `route.query` để khôi phục state → `fetchCourses()`.

### Câu hỏi phỏng vấn liên quan
Tại sao dùng `router.replace()` thay vì `router.push()` để sync filter?

### Câu trả lời ngắn gọn
`replace()` không tạo history entry mới, tránh user phải bấm Back nhiều lần khi chỉ đang thay đổi filter. `push()` sẽ tạo 1 entry cho mỗi lần click filter → Back button trở nên vô dụng vì chỉ quay lại filter trước đó thay vì trang trước đó.

---

## 78. Spring Data Pageable Sort Parameter - Tham Số Sắp Xếp

### Giải thích ngắn gọn
Spring Data Pageable nhận tham số `sort` từ query string dưới dạng `sort=field,direction` (ví dụ `sort=totalStudents,desc`). Spring tự parse string này thành `Sort` object. Có thể truyền nhiều sort: `sort=level,asc&sort=title,desc`. Frontend chỉ cần gửi đúng format, không cần tách field và direction thành 2 param riêng.

### Ví dụ trong project này
`CourseListPage.vue` có `<select>` với các option value như `id,desc`, `totalStudents,desc`, `originalPrice,asc`. Value này được truyền thẳng vào `params.sort` khi gọi `CourseService.getCourses(params)`. Axios serialize thành `?sort=totalStudents,desc`, Spring Boot Controller nhận qua `@PageableDefault Pageable pageable`.

### Câu hỏi phỏng vấn liên quan
Nếu frontend gửi sort field không tồn tại trong entity (ví dụ `sort=rating,desc` nhưng entity dùng `averageRating`), điều gì xảy ra?

### Câu trả lời ngắn gọn
Spring sẽ ném `PropertyReferenceException` hoặc bỏ qua tùy cấu hình. Đây là lý do frontend cần map đúng tên field trong entity (`averageRating,desc`) thay vì dùng tên UI (`rating`).

---

## 79. Static vs Dynamic Content Strategy - Chiến Lược Nội Dung Tĩnh và Động

### Giải thích ngắn gọn
Trên một landing page (HomePage), không phải mọi section đều cần gọi API. Marketing content (stats, features, testimonials) thường là static — thay đổi hiếm khi và không phụ thuộc user. Course listings là dynamic — phụ thuộc dữ liệu real-time từ database. Tách biệt giúp page render nhanh phần tĩnh trong khi API đang loading phần động.

### Ví dụ trong project này
`HomePage.vue` có 5 sections: Hero (static), Features (static), Courses Preview (dynamic — gọi API), Gamification (static), Testimonials (static), CTA (static). Chỉ section "Khóa học nổi bật" có loading spinner và error state. Các section khác render ngay lập tức từ `const` arrays.

### Câu hỏi phỏng vấn liên quan
Khi nào nên chuyển static content thành dynamic (từ CMS hoặc API)?

### Câu trả lời ngắn gọn
Khi: (1) content team cần thay đổi thường xuyên mà không deploy code, (2) content khác nhau theo locale/region, (3) cần A/B testing. Nếu content chỉ thay đổi mỗi vài tháng theo release cycle, giữ static trong code đơn giản và nhanh hơn.

---

## 80. Infinite Loop Prevention in Watchers - Tránh Vòng Lặp Vô Hạn Trong Watch

### Giải thích ngắn gọn
Khi component vừa `watch(route.query)` vừa gọi `router.replace({ query })` trong cùng logic, có nguy cơ tạo vòng lặp: replace → query thay đổi → watch fire → fetchCourses → replace lại → query thay đổi → ... Cần cơ chế so sánh (guard) để phát hiện query không thực sự thay đổi và skip re-fetch.

### Ví dụ trong project này
`CourseListPage.vue` dùng `JSON.stringify(newQuery) !== JSON.stringify(oldQuery)` trong callback của `watch(() => route.query, ...)` để chỉ chạy `syncFiltersFromUrl()` + `fetchCourses()` khi query thực sự khác. Nếu `router.replace()` set cùng query, watch fire nhưng stringify match → skip.

### Câu hỏi phỏng vấn liên quan
`JSON.stringify` compare có nhược điểm gì? Có cách nào tốt hơn không?

### Câu trả lời ngắn gọn
`JSON.stringify` phụ thuộc thứ tự key — `{a:1, b:2}` ≠ `{b:2, a:1}` dù logically equal. Trong Vue Router, `route.query` luôn giữ thứ tự key ổn định nên thực tế không gặp vấn đề. Cách tốt hơn: dùng flag boolean `isUpdatingUrl` set true trước `replace()`, check trong watch, reset sau.


## 81. Pinia Getter vs State trong Testing - Khác Biệt Khi Mock

### Giải thích ngắn gọn
Trong Pinia, `getters` là computed properties được tính từ `state`. Khi dùng `createTestingPinia({ initialState })`, chỉ có thể set **state** trực tiếp — getters sẽ tự động tính lại từ state đó. Nếu cố set getter trong `initialState`, nó sẽ bị ignore vì getter không phải state.

### Ví dụ trong project này
`useAuthStore` có getter `isAuthenticated: (state) => !!state.accessToken`. Trong test, phải set `initialState: { auth: { accessToken: 'token' } }` thay vì `{ auth: { isAuthenticated: true } }`. Getter `isAuthenticated` sẽ tự evaluate thành `true` khi `accessToken` có giá trị.

### Câu hỏi phỏng vấn liên quan
Làm sao override một Pinia getter trong unit test nếu cần giá trị khác với computed logic?

### Câu trả lời ngắn gọn
Sau khi mount component, lấy store instance bằng `useAuthStore()` và gán trực tiếp: `store.isAuthenticated = true` (trong testing mode, getters có thể writable). Hoặc set state sao cho getter trả về giá trị mong muốn — đây là cách an toàn hơn vì test luôn validate cả logic getter.

---

## 82. flushPromises vs nextTick vs setTimeout - Async Testing Strategies

### Giải thích ngắn gọn
Ba phương pháp đợi async operations trong Vue test utils:
- `await nextTick()`: Chỉ đợi DOM update cycle, không đợi Promises.
- `await flushPromises()`: Đợi tất cả pending Promises resolve (bao gồm chained `.then()` và `async/await`). Import từ `@vue/test-utils`.
- `await new Promise(r => setTimeout(r, 0))`: Đợi 1 macrotask, có thể miss promise chains phức tạp.

### Ví dụ trong project này
`CourseDetailPage.spec.js` ban đầu dùng `setTimeout(0)` nhưng fail vì `fetchCourseDetail()` chain thêm `checkEnrollmentStatus()` (2 API calls liên tiếp). Chuyển sang `flushPromises()` giải quyết vì nó drain toàn bộ microtask queue.

### Câu hỏi phỏng vấn liên quan
Khi nào dùng `nextTick()` và khi nào dùng `flushPromises()` trong Vue component test?

### Câu trả lời ngắn gọn
`nextTick()` khi chỉ cần đợi reactive DOM update (ví dụ sau `ref.value = x`, đợi template re-render). `flushPromises()` khi component có async operations (API calls, setTimeout callbacks) cần hoàn thành trước khi assert kết quả.

---

## 83. Duplicate Request Prevention Pattern - Ngăn Chặn Request Trùng Lặp

### Giải thích ngắn gọn
Khi user click nhanh nhiều lần vào nút submit/enroll, nhiều request giống nhau sẽ gửi đến server, gây lỗi hoặc data inconsistency. Pattern phổ biến: dùng boolean flag (`isEnrolling`) set `true` trước khi gửi request, bind vào `:disabled` attribute của button, và set `false` trong `finally` block.

### Ví dụ trong project này
`CourseDetailPage.vue` dùng `isEnrolling` ref: button có `:disabled="isEnrolling || !course.id"`, text thay đổi thành "Đang xử lý..." khi loading, và style chuyển sang `cursor-not-allowed` với màu muted. Server-side cũng có thể trả lỗi "đã ghi danh" — client xử lý bằng cách set `isEnrolled = true` khi nhận error message chứa keyword này.

### Câu hỏi phỏng vấn liên quan
Ngoài disable button, còn cách nào khác để ngăn duplicate request ở frontend?

### Câu trả lời ngắn gọn
(1) Debounce/throttle function wrapper, (2) AbortController cancel request trước đó, (3) Request deduplication middleware trong axios interceptor (cache pending requests by URL+params, return same Promise), (4) Optimistic UI update — disable interaction ngay lập tức trước khi request gửi.

---

## 84. Return URL Pattern - Redirect Sau Đăng Nhập

### Giải thích ngắn gọn
Khi guest user cố truy cập feature cần auth (enroll khóa học), app redirect họ đến login page kèm `redirect` query param chứa URL hiện tại. Sau khi đăng nhập thành công, LoginPage đọc `redirect` param và navigate user về đúng trang họ đang xem, thay vì luôn redirect về homepage.

### Ví dụ trong project này
`CourseDetailPage.vue`: `router.push({ path: '/login', query: { redirect: route.fullPath } })`. URL sẽ thành `/login?redirect=/courses/tieng-nhat-n5`. LoginPage sau khi auth thành công đọc `route.query.redirect` và dùng `router.replace(redirect)` để quay lại.

### Câu hỏi phỏng vấn liên quan
Tại sao dùng `route.fullPath` thay vì `route.path` cho redirect URL?

### Câu trả lời ngắn gọn
`fullPath` bao gồm cả query params và hash (`/courses?level=N5#reviews`), `path` chỉ có pathname (`/courses`). Dùng `fullPath` giữ nguyên context mà user đang xem (bộ lọc, anchor position), mang lại UX tốt hơn khi quay lại.



---

## 85. Axios Error Handling Pattern - Xử Lý Lỗi API & Network

### Giải thích ngắn gọn
Khi gọi API với thư viện như Axios, lỗi có thể đến từ 3 nguồn chính:
1. **Lỗi từ backend (HTTP errors):** Server trả về status code >= 400 (VD: 401 Unauthorized, 422 Unprocessable Entity). Axios map object này vào `error.response`.
2. **Lỗi mạng (Network errors):** Mất mạng, server chết không phản hồi, bị block CORS. Axios gửi request nhưng không nhận được response, map vào `error.request`.
3. **Lỗi cấu hình (Setup errors):** Lỗi syntax trong code trước khi gọi request, timeout interceptor.

### Ví dụ trong project này
Trong `LoginPage.vue` và `RegisterPage.vue`, catch block được tổ chức chuẩn xác:
```javascript
} catch (error) {
  if (error.response) {
    // 401, 409, 422 (xử lý logic API nghiệp vụ, field validation)
  } else if (error.request) {
    // Network Error, Server Unreachable
    formErrorMsg.value = "Không thể kết nối đến máy chủ. Vui lòng kiểm tra mạng."
  } else {
    formErrorMsg.value = "Đã xảy ra lỗi không xác định."
  }
}
```

### Câu hỏi phỏng vấn liên quan
Tại sao khi testing network error mock, ta phải define `request: {}` object trong mock error?

### Câu trả lời ngắn gọn
Bởi vì cấu trúc của network error trong Axios là một object có thuộc tính `request` nhưng không có `response`. Nếu mock object thiếu property `request`, logic `else if (error.request)` sẽ là falsy, dẫn code chạy sai vào nhánh fallback "lỗi không xác định".


---

## 86. Promise.all với Partial Failure Handling

### Giải thích ngắn gọn
Khi một trang cần gọi nhiều API cùng lúc, `Promise.all([...])` giúp chạy song song và đợi tất cả hoàn thành. Tuy nhiên, nếu BẤT KỲ 1 promise nào reject, toàn bộ `Promise.all` sẽ reject — trang sẽ hiển thị error dù các API khác đã trả về dữ liệu hợp lệ.

Giải pháp: Với các API không critical (phụ trợ, có thể thiếu mà trang vẫn hoạt động), thêm `.catch(() => null)` để biến rejection thành resolved value `null`. Code sau đó kiểm tra `if (result?.data?.code === 1000)` trước khi sử dụng.

### Ví dụ trong project này
`StudentDashboardPage.vue` gọi 3 API cùng lúc:
```javascript
const [progressRes, coursesRes, attemptsRes] = await Promise.all([
  StudentService.getDashboardProgress(),   // critical — throw nếu lỗi
  StudentService.getMyCourses(),           // critical — throw nếu lỗi
  QuizService.getMyQuizAttempts().catch(() => null)  // optional — swallow lỗi
])
```
Nếu Quiz API chết, dashboard vẫn render progress + courses. Widget quiz đơn giản ẩn đi.

### Câu hỏi phỏng vấn liên quan
`Promise.all` khác `Promise.allSettled` như thế nào? Khi nào nên dùng cái nào?

### Câu trả lời ngắn gọn
`Promise.all` fail-fast: reject ngay khi 1 promise reject. `Promise.allSettled` chờ TẤT CẢ promises settle (dù fulfilled hay rejected), trả về array `{ status, value/reason }`. Dùng `allSettled` khi cần kết quả của mọi promise bất kể thành công hay thất bại. Dùng `all` + `.catch()` khi muốn kiểm soát chính xác promise nào được phép fail.

---

## 87. Selective Migration Pattern - Lọc Feature Khi Migrate Từ Prototype

### Giải thích ngắn gọn
Khi migrate từ design prototype (Stitch/Figma) sang production code, không nên port 1:1 mọi thứ. Mỗi UI element cần kiểm tra: (1) Backend API đã có chưa? (2) Dữ liệu là thật hay mock? (3) Nếu chưa có API, có timeline rõ ràng không? Nếu câu trả lời là "chưa có" và "không rõ timeline", loại bỏ element đó khỏi production. Giữ lại prototype code làm reference cho phase sau.

### Ví dụ trong project này
Stitch `DashboardPage.tsx` có: XP system, Streak counter, Badges grid, Weekly Activity Chart, Daily Missions. Backend chỉ có `GET /users/me/progress` (3 fields) và `GET /users/me/courses`. Kết quả: Vue `StudentDashboardPage.vue` chỉ port stat cards + course list + quiz list. Loại bỏ 5 widgets mock data.

Stitch `ProfilePage.tsx` có 4 tabs: Info, Password, Orders, Notifications. Backend chỉ có API cho Info và Password. Kết quả: Vue `ProfilePage.vue` chỉ giữ 2 tabs.

### Câu hỏi phỏng vấn liên quan
Tại sao không giữ UI placeholder "Coming soon" cho các feature chưa có API?

### Câu trả lời ngắn gọn
Placeholder "Coming soon" chấp nhận được cho feature đơn giản (1 badge nhỏ). Nhưng với feature phức tạp (Badges grid, Weekly Chart, Notification Settings), placeholder tạo kỳ vọng sai cho user, tốn effort maintain code dead, và làm UI rối. Tốt hơn là loại bỏ hoàn toàn, giữ reference trong prototype, và thêm vào production khi API sẵn sàng.

---

### Vue Router Navigation Guards (onBeforeRouteLeave) - Bảo vệ luồng điều hướng

**Rating:** 🟢

**Định nghĩa:**
Các hook (hàm) của Vue Router cho phép chặn (intercept) hoặc can thiệp vào quá trình chuyển trang (navigation). Dùng phổ biến để kiểm tra quyền truy cập (auth guard) hoặc cảnh báo trước khi rời trang có dữ liệu chưa lưu.

**Ví dụ:**
```javascript
import { onBeforeRouteLeave } from 'vue-router'

onBeforeRouteLeave((to, from, next) => {
  if (isTakingQuiz.value && !isFinished.value) {
    const answer = window.confirm('Bạn đang làm bài kiểm tra. Nếu thoát bây giờ, kết quả sẽ bị mất. Bạn có chắc chắn muốn thoát?')
    if (answer) {
      next() // Cho phép chuyển trang
    } else {
      next(false) // Hủy bỏ thao tác chuyển trang
    }
  } else {
    next()
  }
})
```

**Lợi ích/Khi nào dùng:**

- `onBeforeRouteLeave`: Cảnh báo khi rời trang chứa form chưa submit, đang làm quiz, hoặc tiến trình upload chưa xong.
- `beforeEach`: Kiểm tra user đã login chưa trước khi cho phép vào các private route.

**Misconception hay gặp:**

- ❌ "Chỉ cần dùng window.onbeforeunload là đủ" - Sai, sự kiện này chỉ chạy khi đóng/load lại browser, không bắt được các thao tác chuyển trang nội bộ bằng Vue Router.


---

## 88. Lazy-Loading Tree Pattern - Tải Dữ Liệu Cây Theo Cấp

### Giải thích ngắn gọn
Khi UI hiển thị dữ liệu có cấu trúc cây nhiều cấp (Course → Section → Lesson → Resource), fetch toàn bộ cây 1 lần sẽ rất chậm nếu dữ liệu lớn. Thay vào đó, chỉ fetch cấp gốc khi trang load, và fetch cấp con khi user mở rộng (expand) node cha. Pattern này gọi là lazy-loading tree.

### Ví dụ trong project này
`AdminCourseStructurePage.vue` implement:
```javascript
// Khi trang load: chỉ fetch sections
const sectionRes = await AdminService.getSectionsByCourse(courseId)
sections.value = sectionRes.data.result.map(sec => ({
  ...sec,
  isExpanded: false,      // chưa mở
  isLoadingLessons: false, // chưa đang tải
  lessons: []              // chưa có dữ liệu con
}))

// Khi user click expand section:
const toggleSection = async (section) => {
  section.isExpanded = !section.isExpanded
  if (section.isExpanded && section.lessons.length === 0) {
    section.isLoadingLessons = true
    const res = await AdminService.getLessonsBySection(section.id)
    section.lessons = res.data.result || []
    section.isLoadingLessons = false
  }
}
```
Tương tự, mỗi lesson có `showResources`, `isLoadingResources`, `resources: []` — chỉ fetch khi user click "Tài liệu".

### Câu hỏi phỏng vấn liên quan
Khi nào nên dùng lazy-loading tree thay vì fetch toàn bộ cây 1 lần?

### Câu trả lời ngắn gọn
Dùng lazy-loading khi: (1) Cây có nhiều cấp và mỗi cấp có nhiều node, (2) User thường chỉ xem 1-2 nhánh, không cần toàn bộ, (3) API backend đã tách endpoint theo cấp. Fetch toàn bộ khi: cây nhỏ (< 50 nodes), user cần search/filter across nodes, hoặc cần render tree view đầy đủ ngay lập tức.

---

## 89. Centralized Admin Service Pattern - Gom API Call Vào 1 File

### Giải thích ngắn gọn
Thay vì tạo nhiều service file riêng biệt cho từng domain admin (course-admin.service, quiz-admin.service, user-admin.service), gom tất cả vào 1 file `admin.service.js`. Mỗi method tương ứng 1 endpoint API. File export 1 object `AdminService` duy nhất.

### Ví dụ trong project này
`admin.service.js` chứa ~30 methods chia theo comment section:
```javascript
export const AdminService = {
  // Dashboard
  async getDashboardStats() { return api.get('/v1/admin/dashboard') },
  // User Management
  async getUsers(params) { return api.get('/v1/admin/users', { params }) },
  async lockUser(id) { return api.put(`/v1/admin/users/${id}/lock`) },
  // Course Management
  async getCourses(params) { ... },
  async createCourse(payload) { ... },
  // Section & Lesson
  async getSectionsByCourse(courseId) { ... },
  async createLesson(sectionId, payload) { ... },
  // Quiz, Question, Answer...
}
```

### Câu hỏi phỏng vấn liên quan
Khi nào nên tách service file thay vì gom chung?

### Câu trả lời ngắn gọn
Gom chung khi: (1) Tất cả endpoint dùng cùng base path (`/v1/admin/...`), (2) Quy mô MVP dưới 50 endpoints, (3) Team nhỏ không cần phân chia ownership file. Tách khi: (1) File vượt 500 dòng, (2) Các domain có logic interceptor riêng (retry, cache), (3) Team lớn cần tránh merge conflict. Nguyên tắc: bắt đầu gom, tách khi có lý do cụ thể.

---

## 90. Data Isolation Pattern - Backend Bảo Vệ Dữ Liệu Theo Ownership

### Giải thích ngắn gọn
Trong hệ thống multi-tenant hoặc multi-role, Data Isolation đảm bảo user chỉ thao tác được trên dữ liệu mình sở hữu. ADMIN/SUPER_ADMIN có thể truy cập tất cả, nhưng TEACHER chỉ có quyền trên course do mình tạo. Logic này **phải** nằm ở backend (server-side enforcement), không dựa vào frontend hide/show UI.

### Ví dụ trong project này
`LessonAdminServiceImpl.java` implement `checkDataIsolation()`:
```java
private void checkDataIsolation(Course course) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String currentUserEmail = auth.getName();
    
    boolean isAdminOrSuperAdmin = auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") 
                    || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    
    if (!isAdminOrSuperAdmin) {
        if (!course.getTeacher().getEmail().equals(currentUserEmail)) {
            throw new AppException(ErrorCode.DATA_ISOLATION_FORBIDDEN);
        }
    }
}
```
Frontend nhận HTTP 403 với error code `DATA_ISOLATION_FORBIDDEN` → hiển thị thông báo "Bạn không có quyền truy cập dữ liệu này."

### Câu hỏi phỏng vấn liên quan
Tại sao Data Isolation phải nằm ở backend thay vì chỉ ẩn UI ở frontend?

### Câu trả lời ngắn gọn
Frontend có thể bị bypass bằng DevTools, Postman, hoặc script tự động. Nếu backend không kiểm tra ownership, kẻ tấn công chỉ cần biết endpoint + ID là thao tác được trên dữ liệu của người khác. Backend check là lớp bảo vệ bắt buộc; frontend hide UI chỉ là UX convenience, không phải security measure.

---

## 91. Status Workflow Pattern (Publish/Hide/Archive) - Quản Lý Vòng Đời Entity

### Giải thích ngắn gọn
Nhiều entity trong CMS (Course, Quiz, Lesson, Section) có lifecycle status: `DRAFT → PUBLISHED → HIDDEN → ARCHIVED`. Mỗi transition được bảo vệ bởi 1 API endpoint riêng thay vì cho phép update status trực tiếp trong PUT request. Điều này đảm bảo backend validate điều kiện trước khi chuyển trạng thái (VD: quiz phải có ít nhất 1 câu hỏi mới được publish).

### Ví dụ trong project này
```javascript
// Frontend gọi endpoint riêng cho từng action:
await AdminService.publishQuiz(quiz.id)  // PUT /admin/quizzes/{id}/publish
await AdminService.hideQuiz(quiz.id)     // PUT /admin/quizzes/{id}/hide
await AdminService.deleteQuiz(quiz.id)   // DELETE /admin/quizzes/{id} → ARCHIVED

// Backend validation khi publish:
// - Quiz phải có >= 1 question
// - Tất cả question phải có >= 1 answer
// - Phải có ít nhất 1 correct answer
// Nếu không đạt → throw AppException → frontend hiển thị error
```

### Câu hỏi phỏng vấn liên quan
Tại sao dùng endpoint riêng (`PUT /publish`) thay vì cho phép update status trong `PUT /quizzes/{id}` body?

### Câu trả lời ngắn gọn
(1) Tách endpoint cho phép backend validate business rules riêng cho từng transition (publish cần check questions, hide cần check active enrollments). (2) Rõ ràng về intent: `PUT /publish` chỉ làm 1 việc, dễ audit log. (3) Tránh race condition: nếu 2 admin cùng PUT update quiz, 1 người gửi `status: PUBLISHED` xen lẫn với sửa title → kết quả khó đoán. Endpoint riêng đảm bảo atomicity.


---

## 92. Frontend Regression Testing - Kiểm Thử Hồi Quy Giao Diện

### Giải thích ngắn gọn
Regression testing là quá trình kiểm tra lại các tính năng hiện có sau khi thay đổi code (refactor, migrate UI, thêm feature mới) để đảm bảo không có gì bị hỏng. Trong context frontend Vue, regression tests sử dụng `@vue/test-utils` + Vitest để mount component, mock services, và assert DOM output.

### Ví dụ trong project này
```javascript
// Pattern: Test-proof selector — tìm button qua text thay vì class
const startBtn = wrapper.findAll('button')
  .find(b => b.text().includes('Bắt đầu làm bài'))
await startBtn.trigger('click')

// Pattern: Stub child component để isolate test
const wrapper = mount(LessonLearningPage, {
  global: {
    stubs: {
      'router-link': true,
      'LearningCurriculumSidebar': { template: '<div />' }
    }
  }
})

// Pattern: flushPromises thay vì setTimeout
import { flushPromises } from '@vue/test-utils'
await flushPromises() // ổn định hơn setTimeout(0)
```

### Câu hỏi phỏng vấn liên quan
Khi migrate UI (thay đổi CSS class, restructure DOM), tại sao test lại bị gãy? Làm sao viết test bền vững hơn?

### Câu trả lời ngắn gọn
Test dựa vào CSS class (`.btn-complete`, `.lesson-title`) bị gãy vì class thay đổi khi redesign. Test bền vững hơn nên dựa vào: (1) Text content (`wrapper.text().toContain()`), (2) Semantic HTML (`wrapper.find('h1')`), (3) ARIA attributes (`[aria-label="..."]`), (4) `data-testid` attributes (nếu team đồng ý convention). Ưu tiên test behavior (user nhấn nút → API được gọi) hơn test structure (DOM có class X).

---

## 93. Accessibility Testing trong Vue - Kiểm Thử Khả Năng Tiếp Cận

### Giải thích ngắn gọn
Accessibility (a11y) testing đảm bảo web app sử dụng được bởi mọi người, kể cả người dùng screen reader, keyboard-only, hoặc có vấn đề về thị lực. Trong test, kiểm tra: `aria-label` cho interactive elements, `alt` text cho images, `focus-visible` styling cho keyboard navigation.

### Ví dụ trong project này
```javascript
// Kiểm tra pagination buttons có aria-label
const prevBtn = wrapper.find('button[aria-label="Previous page"]')
expect(prevBtn.exists()).toBe(true)

// Kiểm tra image có alt text
const img = wrapper.find('img')
expect(img.attributes('alt')).toBe('Test')

// Kiểm tra keyboard focus styling
const courseLink = wrapper.find('.group')
expect(courseLink.classes()).toContain('focus-visible:ring-2')
```

### Câu hỏi phỏng vấn liên quan
Accessibility testing ở mức nào là đủ cho một web app MVP?

### Câu trả lời ngắn gọn
Ở mức MVP, tối thiểu cần: (1) `alt` text cho mọi `<img>`, (2) `aria-label` cho icon-only buttons, (3) `focus-visible` styling cho keyboard users, (4) Semantic HTML (h1 > h2 > h3, nav, main, aside), (5) Sufficient color contrast (WCAG AA). Không cần full WCAG AAA compliance nhưng baseline a11y là non-negotiable vì ảnh hưởng SEO và legal compliance ở nhiều quốc gia.

---

## 94. Hotlinking vs Self-Hosting Static Assets - Lưu Trữ Tài Nguyên Tĩnh

### Giải thích ngắn gọn
Hotlinking là việc dùng trực tiếp URL ảnh/font/tài nguyên từ server bên thứ ba (ví dụ `https://images.unsplash.com/...`) trong code production. Self-hosting là tải về, tối ưu, và phục vụ tài nguyên từ chính server hoặc CDN của project. Hotlinking gây rủi ro: ảnh bị xóa/thay đổi, thêm DNS lookup, vi phạm ToS của nguồn, không cache được ở build time.

### Ví dụ trong project này
```vue
<!-- ❌ Trước (Hotlinking Unsplash) — rủi ro production -->
<div style="background-image: url('https://images.unsplash.com/photo-1598957232485-fab51e0ed7e8?w=1600&h=900&fit=crop&auto=format')">

<!-- ✅ Sau (Self-hosted WebP) — ổn định, nhanh hơn, cache được -->
<script setup>
import heroBg from '@/assets/hero-bg.webp'
</script>
<div :style="{ backgroundImage: `url(${heroBg})` }">
```

### Câu hỏi phỏng vấn liên quan
Khi nào nên hotlink ảnh từ CDN bên ngoài vs self-host? Trade-off là gì?

### Câu trả lời ngắn gọn
Hotlink chỉ phù hợp khi: (1) nguồn có SLA đảm bảo uptime (ví dụ Google Fonts CDN), (2) ảnh thay đổi thường xuyên và cần luôn lấy bản mới nhất, (3) license cho phép. Self-host phù hợp khi: (1) ảnh tĩnh không thay đổi (hero, logo), (2) cần kiểm soát format/quality/kích thước, (3) cần cache ở build time (Vite hash asset → long-term caching), (4) cần hoạt động offline/trong mạng nội bộ. Trong production app, self-host là mặc định an toàn.

---

## 95. WebP Image Format & Responsive Image Strategy

### Giải thích ngắn gọn
WebP là định dạng ảnh do Google phát triển, hỗ trợ cả lossy và lossless compression. So với PNG, WebP thường nhỏ hơn 25-35%. So với JPEG, nhỏ hơn 25-34% ở cùng chất lượng (theo Google). Kết hợp với `loading="lazy"` và `decoding="async"` trên thẻ `<img>`, trang web giảm đáng kể thời gian tải ban đầu.

### Ví dụ trong project này
```bash
# Chuyển đổi logo từ PNG sang WebP (giảm 99% dung lượng)
# logo.png:  712 KB (1024×1024) → logo.webp: 5.3 KB (128×128)

# Lazy loading cho ảnh below-the-fold (dynamic content từ API)
<img :src="c.thumbnailUrl" :alt="c.title"
     loading="lazy" decoding="async"
     class="w-full h-full object-cover" />

# KHÔNG lazy load ảnh above-the-fold (hero, logo trên navbar)
<img src="@/assets/logo.webp" alt="BrianJP Logo" />
```

### Câu hỏi phỏng vấn liên quan
`loading="lazy"` hoạt động như thế nào? Khi nào KHÔNG nên dùng?

### Câu trả lời ngắn gọn
`loading="lazy"` ra lệnh cho browser trì hoãn tải ảnh cho đến khi ảnh sắp xuất hiện trong viewport (dựa trên Intersection Observer nội bộ). KHÔNG nên dùng cho ảnh above-the-fold (hero banner, logo navbar, LCP element) vì nó trì hoãn Largest Contentful Paint. Browser sẽ không tải ảnh lazy cho đến khi layout được tính toán xong, gây delay thêm ~200-500ms cho critical images.

---

## 96. Font Loading Strategy (`display=swap` vs `display=block`)

### Giải thích ngắn gọn
Khi load web font từ Google Fonts, tham số `display` kiểm soát hành vi hiển thị text trong lúc font đang tải. `display=block` gây FOIT (Flash of Invisible Text) — text biến mất hoàn toàn cho đến khi font tải xong, có thể kéo dài 3s trên mạng chậm. `display=swap` gây FOUT (Flash of Unstyled Text) — text hiển thị ngay bằng fallback font, rồi swap sang web font khi tải xong. FOUT tốt hơn cho UX vì user đọc được nội dung ngay.

### Ví dụ trong project này
```html
<!-- ❌ display=block: Icon biến mất 1-3s trên mạng chậm -->
<link href="...Material+Symbols+Outlined...&display=block" rel="stylesheet">

<!-- ✅ display=swap: Text fallback hiện ngay, icon swap vào khi sẵn -->
<link href="...Material+Symbols+Outlined...&display=swap" rel="stylesheet">
```

### Câu hỏi phỏng vấn liên quan
Giải thích sự khác biệt giữa `font-display: swap`, `block`, `fallback`, `optional`. Khi nào chọn cái nào?

### Câu trả lời ngắn gọn
`swap`: 0ms block period, infinite swap period — text hiện ngay bằng fallback, swap khi font ready. Tốt cho body text. `block`: 3s block period — text ẩn 3s, nếu font chưa tải xong thì fallback. Tốt cho icon fonts (nhưng UX kém). `fallback`: 100ms block, 3s swap — compromise giữa swap và block. `optional`: 100ms block, 0s swap — browser tự quyết định dùng font hay không dựa trên network. Tốt cho non-critical fonts khi ưu tiên performance tuyệt đối.

---

## 97. Spaced Repetition System (SRS) — Thuật toán lặp lại ngắt quãng

### Giải thích ngắn gọn
SRS là kỹ thuật quản lý bộ nhớ dài hạn bằng cách ôn tập thẻ (flashcard) đúng lúc sắp quên, thay vì ôn đều đặn mỗi ngày. Mỗi lần review, user đánh giá độ khó (EASY/MEDIUM/HARD), thuật toán sẽ tính khoảng thời gian (`interval_days`) và hệ số thuận lợi (`ease_factor`) để quyết định lần ôn tiếp theo. Thẻ dễ → interval tăng nhanh (3 → 7 → 18 ngày). Thẻ khó → interval về 0 (ôn lại ngay).

### Ví dụ trong project này
```java
// FlashcardServiceImpl.java — SRS logic
if (difficulty.equals("HARD")) {
    progress.setEaseFactor(Math.max(1.3, progress.getEaseFactor() - 0.2));
    progress.setIntervalDays(0); // Quay về ôn ngay
} else if (difficulty.equals("MEDIUM")) {
    if (progress.getIntervalDays() == 0) progress.setIntervalDays(1);
    else progress.setIntervalDays((int)(progress.getIntervalDays() * 1.2));
} else if (difficulty.equals("EASY")) {
    progress.setEaseFactor(progress.getEaseFactor() + 0.15);
    if (progress.getIntervalDays() == 0) progress.setIntervalDays(3);
    else progress.setIntervalDays((int)(progress.getIntervalDays() * progress.getEaseFactor()));
}
```

Bảng FlashcardProgress lưu trạng thái SRS riêng biệt cho từng cặp `(user_id, flashcard_id)`:
```java
@UniqueConstraint(columnNames = {"user_id", "flashcard_id"})
private Double easeFactor = 2.5;   // Hệ số dễ, giảm khi HARD, tăng khi EASY
private Integer intervalDays = 0;  // Khoảng ôn (ngày), 0 = ôn lại ngay
private LocalDateTime nextReviewTime; // Thời điểm ôn tiếp theo (UTC)
```

### Câu hỏi phỏng vấn liên quan
Giải thích thuật toán SRS cơ bản. Tại sao `ease_factor` có lower bound 1.3?

### Câu trả lời ngắn gọn
SRS hoạt động bằng cách nhân `interval_days` với `ease_factor` sau mỗi lần review thành công. Nếu user nhớ dễ, interval tăng theo cấp số nhân (giảm lượng ôn tập). Nếu user quên (HARD), interval reset về 0 để ôn lại ngay. `ease_factor` có lower bound 1.3 (thay vì 1.0 hoặc thấp hơn) để đảm bảo interval luôn tăng ít nhất 30% mỗi lần — nếu không, thẻ sẽ bị "kẹt" ở cùng interval mãi mãi. Đây là cải tiến so với thuật toán SM-2 gốc của Piotr Woźniak.

---

## 98. Idempotency Key — Ngăn chặn xử lý trùng lặp trong API

### Giải thích ngắn gọn
Idempotency key là một giá trị duy nhất do client tạo ra, gắn kèm với mỗi request mutate (POST/PUT). Server lưu key này lại và nếu nhận được request trùng key, server sẽ bỏ qua thay vì xử lý lại. Kỹ thuật này ngăn chặn tình huống: user click "Submit" 2 lần, mạng giật retry, hoặc frontend gọi API trùng.

### Ví dụ trong project này
```java
// Server-side: FlashcardServiceImpl.java
public void reviewCard(ReviewFlashcardReq req) {
    // Check trước khi xử lý — nếu key đã tồn tại, return ngay
    if (reviewLogRepository.findByIdempotencyKey(req.getIdempotencyKey()).isPresent()) {
        return; // Already processed, skip silently
    }
    // ... xử lý logic SRS ...
    FlashcardReviewLog log = FlashcardReviewLog.builder()
            .idempotencyKey(req.getIdempotencyKey()) // Lưu key vào DB
            .build();
    reviewLogRepository.save(log);
}
```

```javascript
// Client-side: FlashcardPage.vue
const generateIdempotencyKey = (cardId) => {
  return `review_${cardId}_${new Date().getTime()}_${Math.random().toString(36).substring(7)}`;
};
```

### Câu hỏi phỏng vấn liên quan
Tại sao cần idempotency key? Tại sao không chỉ dùng `unique(user_id, flashcard_id)` để ngăn duplicate?

### Câu trả lời ngắn gọn
`unique(user_id, flashcard_id)` chỉ ngăn được trường hợp review thẻ chỉ 1 lần duy nhất. Nhưng thực tế, cùng 1 thẻ sẽ được review nhiều lần (SRS yêu cầu ôn lại theo interval). Nên cần 1 key riêng biệt cho mỗi lần review để phân biệt "review lần thứ 3" vs "request retry của review lần thứ 3". Idempotency key sinh ra ở client đảm bảo mỗi user action (click button) chỉ tạo 1 key duy nhất, kể cả retry.

---

## 99. Tính toán thời gian due theo timezone người dùng

### Giải thích ngắn gọn
Server lưu tất cả thời gian dưới dạng UTC. Khi tính `nextReviewTime`, server nhận timezone string từ client (ví dụ `"Asia/Ho_Chi_Minh"`), chuyển thời điểm hiện tại sang local time, cộng interval rồi set đầu ngày (00:00) theo local, sau đó convert ngược về UTC để lưu. Điều này đảm bảo: thẻ "due ngày mai" luôn hiện ra vào 00:00 sáng theo giờ thực tế của user, không phải 00:00 UTC (tức 07:00 sáng giờ Việt Nam).

### Ví dụ trong project này
```java
// FlashcardServiceImpl.java — timezone-aware next review
ZoneId zoneId = ZoneId.of(req.getTimezone()); // e.g., "Asia/Ho_Chi_Minh"

LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
// Chuyển UTC → local
LocalDateTime localNow = nowUtc.atZone(ZoneOffset.UTC)
        .withZoneSameInstant(zoneId).toLocalDateTime();
// Cộng interval, đặt về đầu ngày local
LocalDateTime localNextDue = localNow.plusDays(progress.getIntervalDays())
        .withHour(0).withMinute(0).withSecond(0);
// Chuyển local → UTC để lưu
LocalDateTime nextDueUtc = localNextDue.atZone(zoneId)
        .withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
progress.setNextReviewTime(nextDueUtc);
```

### Câu hỏi phỏng vấn liên quan
Tại sao lưu UTC trong database thay vì local time? Giải thích luồng convert timezone.

### Câu trả lời ngắn gọn
Lưu UTC vì: (1) Tránh nhập nhằng khi user thay đổi timezone hoặc DST (Daylight Saving Time) chuyển mùa. (2) So sánh thời gian giữa các user ở timezone khác nhau chỉ cần 1 phép so sánh. (3) Database không cần biết timezone — chỉ lưu giá trị absolute. Luồng convert: Client gửi timezone string → Server tính "đầu ngày local" = `localNow + interval → .withHour(0)` → Convert ngược về UTC bằng `atZone(local).withZoneSameInstant(UTC)`. Khi query thẻ due, chỉ cần `WHERE next_review_time <= NOW()` (UTC).

---

## 100. Đếm "due cards" = total − notDue (phương pháp đếm bù)

### Giải thích ngắn gọn
Khi tính số thẻ cần ôn, ta KHÔNG đếm trực tiếp "thẻ nào due" vì phải xử lý 2 loại khác nhau: (1) thẻ mới chưa có progress, (2) thẻ cũ quá hạn. Thay vào đó, đếm bù: `dueCards = totalCards − notDueCount`. Trong đó `notDueCount` chỉ đếm thẻ CÓ progress VÀ `next_review_time > now` (chưa đến hạn). Mọi thứ còn lại (chưa từng học + quá hạn) tự động nằm trong `dueCards`.

### Ví dụ trong project này
```java
// FlashcardServiceImpl.java
Long totalCards = flashcardRepository.countByDeckId(deck.getId());
Long notDueCount = progressRepository.countNotDueCardsByDeckId(
    user.getId(), deck.getId(), currentUtc
);
Long dueCards = totalCards - notDueCount;
// Math.max(0, dueCards) — phòng trường hợp data inconsistency
```

### Câu hỏi phỏng vấn liên quan
Tại sao dùng phương pháp "đếm bù" thay vì query trực tiếp danh sách thẻ due?

### Câu trả lời ngắn gọn
Đếm trực tiếp yêu cầu LEFT JOIN giữa `flashcards` và `flashcard_progress` để tìm cả thẻ chưa có record (IS NULL) lẫn thẻ quá hạn (< now) — query phức tạp hơn. Đếm bù chỉ cần 2 query đơn giản: `COUNT(*)` tổng và `COUNT(*)` có điều kiện `next_review_time > now`. Ngoài ra, `Math.max(0, dueCards)` xử lý edge case khi data progress tồn tại nhưng flashcard bị xóa (orphan records).

---

## 101. Server-Authoritative Scoring — Chống gian lận điểm số

### Giải thích ngắn gọn
Trong hệ thống gamification (XP, điểm số, level), điểm PHẢI được tính và lưu hoàn toàn phía server. Client không bao giờ được phép gửi request kiểu `POST /api/scores { "xp": 1000 }` — vì bất kỳ ai biết dùng cURL/Postman đều có thể gửi request giả để cộng vô hạn điểm. Thay vào đó, server tự cộng điểm bên trong business logic khi hoàn thành hành động thực sự (lesson completed, quiz passed).

### Ví dụ trong project này
```java
// ✅ Server-Authoritative: Cộng XP bên trong service logic (client không biết)
// LearningServiceImpl.java
private void upsertLessonProgress(User user, Lesson lesson, ...) {
    // ... lưu progress ...
    if (Boolean.TRUE.equals(isCompleted)) {
        scoringService.addXp(user.getId(), "LESSON_COMPLETED", 100, lesson.getId(),
            "Hoàn thành bài học: " + lesson.getTitle());
    }
}

// QuizLearningServiceImpl.java
if (Boolean.TRUE.equals(attempt.getPassed())) {
    scoringService.addXp(user.getId(), "QUIZ_PASSED", 200, quiz.getId(),
        "Vượt qua bài kiểm tra: " + quiz.getTitle());
}

// FlashcardServiceImpl.java
scoringService.addXp(user.getId(), "FLASHCARD_REVIEWED", 10, null,
    "Ôn tập thẻ flashcard");
```

```java
// ❌ Client-Authoritative: TUYỆT ĐỐI KHÔNG LÀM NHƯ NÀY
@PostMapping("/api/scores")
public void addScore(@RequestBody ScoreReq req) {
    scoreService.addXp(getCurrentUser().getId(), req.getXp()); // User tự gửi XP!!!
}
```

### Câu hỏi phỏng vấn liên quan
Sự khác biệt giữa server-authoritative và client-authoritative trong game/gamification? Khi nào cần chọn cái nào?

### Câu trả lời ngắn gọn
**Server-authoritative**: Mọi quyết định về trạng thái (điểm số, tiến trình, inventory) đều do server tính toán và xác thực. Client chỉ gửi "hành động" (hoàn thành bài học, trả lời quiz), server tự quyết định kết quả. Ưu điểm: chống cheat hoàn toàn. Nhược điểm: latency cao hơn. **Client-authoritative**: Client tự tính toán trạng thái rồi gửi lên server lưu. Ưu điểm: nhanh, responsive. Nhược điểm: dễ bị hack/cheat. Trong web app có scoreboard/leaderboard, PHẢI dùng server-authoritative. Client-authoritative chỉ phù hợp cho single-player game offline hoặc những giá trị không ảnh hưởng cạnh tranh.

---

## 102. Pre-Aggregated Scores vs Real-Time Aggregation

### Giải thích ngắn gọn
Khi cần hiển thị bảng xếp hạng, có 2 cách tính tổng điểm: (1) Real-time: `SELECT user_id, SUM(xp_amount) FROM learning_activities GROUP BY user_id ORDER BY SUM(xp_amount) DESC` — tính lại mỗi lần query. (2) Pre-aggregated: Lưu sẵn tổng vào bảng riêng `user_scores`, mỗi lần có XP mới chỉ cần `UPDATE total_xp = total_xp + newXp`. Query leaderboard chỉ cần `SELECT * FROM user_scores ORDER BY total_xp DESC`.

### Ví dụ trong project này
```java
// ScoringService.java — Pre-aggregate vào 3 period cùng lúc
updateUserScore(user, "ALL_TIME", "ALL", xpAmount);      // Tổng mọi thời điểm
updateUserScore(user, "WEEKLY", currentWeek, xpAmount);   // Tổng tuần này: "2026-W41"
updateUserScore(user, "MONTHLY", currentMonth, xpAmount); // Tổng tháng này: "2026-10"

private void updateUserScore(User user, String periodType, String periodValue, Integer xp) {
    UserScore score = scoreRepository
        .findByUserIdAndPeriodTypeAndPeriodValue(user.getId(), periodType, periodValue)
        .orElse(UserScore.builder()
            .user(user).periodType(periodType).periodValue(periodValue).totalXp(0)
            .build());
    score.setTotalXp(score.getTotalXp() + xp); // Cộng dồn, không tính lại từ đầu
    scoreRepository.save(score);
}
```

```sql
-- Bảng user_scores: index tối ưu cho ORDER BY
CREATE INDEX idx_scores_ranking ON user_scores(period_type, period_value, total_xp DESC);
-- Query leaderboard chỉ cần scan index, không cần GROUP BY
```

### Câu hỏi phỏng vấn liên quan
So sánh pre-aggregation vs real-time aggregation cho leaderboard. Trade-off là gì?

### Câu trả lời ngắn gọn
**Real-time aggregation** (`SUM GROUP BY`): Luôn chính xác, nhưng query chậm dần theo số lượng activities — O(N) per query với N = tổng activities. Nếu có 1 triệu activities, mỗi lần load bảng xếp hạng phải scan 1 triệu rows. **Pre-aggregation**: Write thêm 1 bước (cộng dồn khi insert), nhưng read cực nhanh — query chỉ scan bảng `user_scores` với ~N users thay vì ~M activities. Trade-off: (1) Nếu cần recalculate (fix bug điểm), phải chạy batch job tính lại từ `learning_activities`. (2) Code write phức tạp hơn (phải maintain cả activity log + score table). Trong thực tế, mọi leaderboard ở quy mô > 1000 users đều dùng pre-aggregation.

---

## 103. ISO-8601 Week Numbering (`IsoFields.WEEK_OF_WEEK_BASED_YEAR`)

### Giải thích ngắn gọn
Java `IsoFields.WEEK_OF_WEEK_BASED_YEAR` tuân theo chuẩn ISO-8601: tuần bắt đầu từ thứ Hai, tuần 1 là tuần đầu tiên chứa ít nhất 4 ngày của năm mới. Điều này khác với `Calendar.WEEK_OF_YEAR` (Java cũ) bắt đầu từ Chủ nhật (kiểu Mỹ). Ví dụ: nếu 1/1/2027 là thứ Sáu, thì tuần ISO là W53 của 2026, nhưng Calendar.WEEK_OF_YEAR sẽ cho W1 của 2027.

### Ví dụ trong project này
```java
// ScoringService.java
LocalDate now = LocalDate.now(ZoneOffset.UTC);
String currentWeek = now.getYear() + "-W" + now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
// Kết quả: "2026-W41" (tuần 41 của năm 2026)
```

### Câu hỏi phỏng vấn liên quan
ISO week numbering khác gì so với WEEK_OF_YEAR? Tại sao quan trọng khi làm leaderboard theo tuần?

### Câu trả lời ngắn gọn
Nếu dùng `Calendar.WEEK_OF_YEAR` (locale-dependent), cùng một ngày có thể thuộc tuần khác nhau tùy theo locale setting (Mỹ: tuần bắt đầu Chủ nhật, châu Âu: thứ Hai). Trong leaderboard đa quốc gia, 2 user ở US và VN sẽ bị tính vào tuần khác nhau cho cùng 1 ngày → ranking sai. ISO-8601 chuẩn hóa toàn cầu: tuần luôn bắt đầu thứ Hai, tránh nhập nhằng. Ngoài ra, ISO week có thể thuộc năm trước/sau ở ranh giới (W52/W53 vs W01), cần dùng `getWeekBasedYear()` thay vì `getYear()` ở biên năm nếu muốn chính xác tuyệt đối.

---

## 104. Streak Tracking — Logic tính chuỗi ngày liên tục

### Giải thích ngắn gọn
Streak (chuỗi ngày liên tục) là cơ chế gamification khuyến khích user học mỗi ngày. Logic: nếu ngày hoạt động cuối = hôm qua → streak + 1. Nếu ngày hoạt động cuối < hôm qua → streak reset = 1. Nếu ngày hoạt động cuối = hôm nay → giữ nguyên (đã tính). Phải dùng `LocalDate` (không có time), so sánh theo ngày UTC để tránh timezone issues.

### Ví dụ trong project này
```java
// ScoringService.java
LocalDate now = LocalDate.now(ZoneOffset.UTC);

if (user.getLastActivityDate() == null || user.getLastActivityDate().isBefore(now)) {
    // Chưa hoạt động hôm nay
    if (user.getLastActivityDate() != null
        && user.getLastActivityDate().plusDays(1).equals(now)) {
        // Hôm qua có hoạt động → tiếp tục streak
        user.setCurrentStreak(user.getCurrentStreak() + 1);
    } else if (user.getLastActivityDate() == null
        || user.getLastActivityDate().isBefore(now.minusDays(1))) {
        // Đã nghỉ > 1 ngày → reset
        user.setCurrentStreak(1);
    }
    user.setLastActivityDate(now);
}
// Nếu lastActivityDate == now → không làm gì (hoạt động nhiều lần trong ngày)
```

### Câu hỏi phỏng vấn liên quan
Streak logic có edge case nào cần xử lý?

### Câu trả lời ngắn gọn
(1) **Timezone**: User ở UTC+7 hoạt động lúc 23:30 local (= 16:30 UTC), rồi 00:30 local hôm sau (= 17:30 UTC) — cùng ngày UTC nhưng 2 ngày local. Nếu streak dựa trên UTC thì mất streak. Giải pháp: dùng timezone của user để tính ngày, hoặc chấp nhận UTC (đơn giản hơn). (2) **DST**: Ngày chuyển giờ có thể dài 23 hoặc 25 giờ. `plusDays(1)` đúng vì nó cộng calendar day, không cộng 24h. (3) **Server downtime**: Nếu server chết 1 ngày, user không thể hoạt động nhưng bị mất streak. Giải pháp: "streak freeze" cho phép bỏ qua 1 ngày.

---

## 105. Privacy Filter trong Leaderboard (is_private_leaderboard)

### Giải thích ngắn gọn
Một số user không muốn tên mình xuất hiện trên bảng xếp hạng công khai (vì lý do riêng tư hoặc không muốn bị so sánh). Cột `is_private_leaderboard` trên bảng `users` cho phép user opt-out. Query leaderboard sẽ JOIN với users và lọc `WHERE u.isPrivateLeaderboard = false`. User private vẫn tính XP bình thường nhưng không hiển thị trên bảng.

### Ví dụ trong project này
```java
// UserScoreRepository.java — JOIN FETCH + filter private users
@Query("SELECT us FROM UserScore us JOIN FETCH us.user u "
     + "WHERE us.periodType = :periodType AND us.periodValue = :periodValue "
     + "AND u.isPrivateLeaderboard = false "
     + "ORDER BY us.totalXp DESC")
Page<UserScore> findTopByPeriod(...);

// LeaderboardServiceImpl.java — Không trả về myRanking nếu private
if (!currentUser.getIsPrivateLeaderboard()) {
    Long myRank = scoreRepository.getRankByUserIdAndPeriod(...);
    // ...
}
```

### Câu hỏi phỏng vấn liên quan
GDPR và privacy-by-design ảnh hưởng gì đến thiết kế leaderboard?

### Câu trả lời ngắn gọn
GDPR (General Data Protection Regulation) yêu cầu: (1) **Consent**: User phải opt-in hoặc có quyền opt-out khỏi việc hiển thị dữ liệu cá nhân công khai. Bảng xếp hạng hiển thị tên + avatar = dữ liệu cá nhân. (2) **Right to erasure**: User xóa tài khoản thì mọi dữ liệu trên leaderboard phải bị xóa (`ON DELETE CASCADE`). (3) **Data minimization**: Chỉ hiển thị thông tin cần thiết (tên, avatar, XP) — không hiển thị email, phone. (4) **Privacy by default**: Nên mặc định `is_private_leaderboard = false` (opt-out) hoặc `= true` (opt-in) tùy jurisdiction. Project này chọn `= false` (hiện mặc định, user tự ẩn).

---

## 106. Rank Calculation bằng COUNT Subquery

### Giải thích ngắn gọn
Để tính thứ hạng của 1 user cụ thể mà không cần load toàn bộ bảng xếp hạng, dùng công thức: `rank = (số người có điểm cao hơn mình) + 1`. Triển khai bằng `COUNT + 1` subquery. Đây là cách tối ưu hơn `ROW_NUMBER() OVER (ORDER BY total_xp DESC)` khi chỉ cần rank của 1 user, vì `ROW_NUMBER` phải sort toàn bộ bảng.

### Ví dụ trong project này
```java
// UserScoreRepository.java
@Query("SELECT COUNT(us) + 1 FROM UserScore us JOIN us.user u "
     + "WHERE us.periodType = :periodType AND us.periodValue = :periodValue "
     + "AND u.isPrivateLeaderboard = false "
     + "AND us.totalXp > ("
     + "  SELECT us2.totalXp FROM UserScore us2 "
     + "  WHERE us2.userId = :userId "
     + "  AND us2.periodType = :periodType AND us2.periodValue = :periodValue"
     + ")")
Long getRankByUserIdAndPeriod(@Param("userId") Long userId, ...);
```

### Câu hỏi phỏng vấn liên quan
So sánh `COUNT + 1 subquery` vs `ROW_NUMBER()` cho rank calculation. Khi nào chọn cái nào?

### Câu trả lời ngắn gọn
**COUNT + 1 subquery**: O(N) chỉ để tính rank 1 user. Tốt khi chỉ cần rank cá nhân ("Bạn đang xếp hạng #47"). Index trên `(period_type, period_value, total_xp DESC)` giúp COUNT chỉ scan ít rows. **ROW_NUMBER()**: Phải sort toàn bộ bảng, tạo window function result set. Tốt khi cần rank cho toàn bộ danh sách (hiển thị bảng xếp hạng top 50). Trong project này: dùng ROW_NUMBER implicitly qua `ORDER BY + LIMIT` cho danh sách, COUNT + 1 cho rank cá nhân. Lưu ý: nếu 2 user cùng điểm, COUNT + 1 cho cùng rank (dense rank), còn ROW_NUMBER cho rank khác nhau (phụ thuộc tie-breaking).

---

## 107. @Cacheable trong Spring — Cache layer cho leaderboard

### Giải thích ngắn gọn
`@Cacheable` là annotation của Spring Cache, lưu kết quả method vào cache (in-memory/Redis). Khi method được gọi lại với cùng key, Spring trả về kết quả cached thay vì chạy lại logic. Phù hợp cho leaderboard vì: (1) leaderboard không cần real-time tuyệt đối (delay 1-5 phút chấp nhận được), (2) nhiều user cùng xem cùng 1 bảng xếp hạng → giảm load DB.

### Ví dụ trong project này
```java
// LeaderboardServiceImpl.java
@Cacheable(value = "leaderboard", key = "#timeframe + '-' + #page + '-' + #size")
public LeaderboardRes getLeaderboard(String timeframe, int page, int size) {
    // ... query DB ...
}
```

### Câu hỏi phỏng vấn liên quan
`@Cacheable` có những pitfall nào cần chú ý?

### Câu trả lời ngắn gọn
(1) **Cache invalidation**: Khi user cộng XP, cache vẫn giữ bảng xếp hạng cũ. Cần `@CacheEvict` hoặc TTL (time-to-live). Trong project chưa implement TTL → leaderboard có thể stale cho đến khi cache bị evict. (2) **Self-invocation**: `@Cacheable` dùng AOP proxy — nếu gọi method từ bên trong cùng class (`this.getLeaderboard()`), cache bị bypass. Phải gọi qua injected bean. (3) **Key collision**: Key phải unique. Nếu quên tham số (vd chỉ dùng `#timeframe` mà quên `#page`), 2 request khác page sẽ trả cùng kết quả. (4) **Serialization**: Nếu dùng Redis cache, DTO phải Serializable. Default `ConcurrentMapCacheManager` (in-memory) không cần nhưng không share giữa instances. (5) **currentUser trong cache**: Method vừa dùng `getCurrentUser()` vừa `@Cacheable` → tất cả user sẽ thấy `isMe = true` cho cùng người. Cần tách logic current user ra khỏi cached method, hoặc thêm userId vào cache key.

---

## 108. API Response Shape Contract

### Giải thích ngắn gọn
API response shape contract là thỏa thuận chính xác giữa backend, service layer frontend và component: dữ liệu nằm ở đâu, field tên gì, đã unwrap hay chưa. Nếu mỗi layer hiểu khác nhau, UI có thể trắng dữ liệu dù API trả đúng.

### Ví dụ trong project này
Backend `GET /api/v1/leaderboards` trả `ApiResponse<LeaderboardRes>` với dữ liệu ở `data.result`. `leaderboard.service.js` unwrap `response.data.result`, nên `LeaderboardPage.vue` phải đọc `result.rankings`, không đọc `response.data.rankings`.

### Câu hỏi phỏng vấn liên quan
Vì sao unit test frontend có thể pass nhưng production vẫn lỗi khi gọi API thật?

### Câu trả lời ngắn gọn
Vì mock response trong test không giống response thật. Test phải mô phỏng đúng boundary contract, hoặc service phải unwrap response trước để component không biết chi tiết `ApiResponse`.

---

## 109. User-Controlled Privacy Preference

### Giải thích ngắn gọn
User-controlled privacy preference là thiết kế cho phép user tự quyết định dữ liệu cá nhân nào được hiển thị công khai. Backend lưu field, API expose field, và frontend cung cấp UI để chỉnh.

### Ví dụ trong project này
User có thể bật `isPrivateLeaderboard` trong Profile. Khi bật, leaderboard backend không đưa user đó vào danh sách ranking công khai, nhưng XP vẫn được tính ở server.

### Câu hỏi phỏng vấn liên quan
Vì sao chỉ thêm cột `is_private_leaderboard` trong database chưa đủ?

### Câu trả lời ngắn gọn
Vì user cần có cách đọc và thay đổi preference đó. Privacy setting phải đi trọn vòng: database, API response/update, frontend UI và test.

---

## 110. Cache Personalization Boundary

### Giải thích ngắn gọn
Cache personalization boundary là ranh giới giữa dữ liệu public có thể cache chung và dữ liệu cá nhân hóa phải tính riêng theo user. Nếu vượt ranh giới này, cache có thể làm user này thấy trạng thái của user khác.

### Ví dụ trong project này
Leaderboard public rankings có thể cache theo `timeframe/page/size`, nhưng các field như `isMe` và `currentUserRanking` phụ thuộc user hiện tại. Vì vậy `LeaderboardServiceImpl.getLeaderboard()` không được cache nguyên response personalized bằng key public.

### Câu hỏi phỏng vấn liên quan
Khi nào một API response không nên cache bằng key chung?

### Câu trả lời ngắn gọn
Khi response chứa dữ liệu phụ thuộc user hiện tại, quyền truy cập, session, locale riêng tư hoặc bất kỳ flag cá nhân hóa nào. Nếu muốn cache, cần tách phần public khỏi phần personalized.

---

## 111. Nested Route Guard Resolution trong SPA

### Giải thích ngắn gọn
Trong Single Page Application (đặc biệt là Vue Router), khi cấu hình route theo mô hình nested (cha bọc layout, con là page cụ thể), metadata bảo mật (như `meta: { role: 'ADMIN' }`) thường chỉ được đặt ở route cha. Để bảo vệ toàn diện các trang con, navigation guard không được chỉ kiểm tra `to.meta` của leaf route mà phải duyệt qua mảng `to.matched` theo thứ tự từ con lên cha hoặc ngược lại.

### Ví dụ trong project này
```javascript
// Thay vì chỉ: const requiredRole = to.meta.role (dễ bỏ lọt nếu leaf route không định nghĩa meta)
const requiredRole = to.matched?.slice().reverse().find(record => record.meta?.role)?.meta?.role || to.meta?.role
```
Học viên cố truy cập `/admin/courses` hay `/admin/quizzes/1/builder` đều bị guard phát hiện route cha yêu cầu role `ADMIN` và bị điều hướng tức thì về `/student/dashboard`.

### Câu hỏi phỏng vấn liên quan
Tại sao dùng `to.matched.some()` hoặc `to.matched.find()` trong router guard an toàn hơn đọc trực tiếp thuộc tính trên `to.meta`?

### Câu trả lời ngắn gọn
Bởi vì `to.matched` chứa danh sách đầy đủ tất cả các bản ghi route đã khớp từ gốc tới lá. Nếu một middleware/guard chỉ đọc thuộc tính trên leaf route, nó sẽ bỏ qua mọi quy tắc phân quyền được thiết lập ở các route cha và layout bao quanh.

---

## 112. DDL Portability trong Flyway Migration (MariaDB vs H2)

### Giải thích ngắn gọn
DDL Portability là nguyên tắc viết các script migration cơ sở dữ liệu sao cho chúng có thể thực thi chính xác trên cả hệ quản trị dữ liệu production (như MariaDB/MySQL) và hệ quản trị dữ liệu in-memory dùng cho testing (như H2). Cần tránh các cú pháp viết tắt hoặc dialect-specific extensions.

### Ví dụ trong project này
Trong `V6__create_leaderboard_tables.sql`, cú pháp MariaDB thêm nhiều cột trong 1 câu lệnh `ALTER TABLE users ADD COLUMN c1 ..., ADD COLUMN c2 ...` khiến H2 báo lỗi cú pháp khi chạy `mvn test`. Khi tách thành từng câu lệnh độc lập `ALTER TABLE users ADD COLUMN c1 ...; ALTER TABLE users ADD COLUMN c2 ...;`, migration chạy trơn tru trên cả 2 database.

### Câu hỏi phỏng vấn liên quan
Khi nào nên dùng Testcontainers thay vì H2 cho integration testing của Spring Boot?

### Câu trả lời ngắn gọn
Khi migration hoặc query sử dụng các tính năng đặc thù sâu của production database (như JSON functions, window functions nâng cao, full-text search, stored procedures, dialect-specific constraints) mà H2 không thể mô phỏng chính xác 100%, lúc đó Testcontainers (chạy container MariaDB thật trong Docker khi test) là giải pháp tiêu chuẩn.

---

## 113. Production Secret Hygiene

### Giải thích ngắn gọn
Production secret hygiene là nguyên tắc không để secret thật như database password, JWT secret, Redis password hoặc API key xuất hiện trong source code, git history, docs public hoặc image build context. Repo chỉ nên lưu template như `.env.example`, còn giá trị thật được inject qua CI/CD secret store, server environment hoặc secret manager.

### Ví dụ trong project này
Root `.gitignore` ignore `.env`, `.env.*`, `*.env` nhưng giữ lại các file mẫu `!.env*.example`. Nhờ vậy `.env.prod` hoặc `.env.local` không bị commit nhầm, trong khi `.env.prod.example` vẫn mô tả đầy đủ biến cần cấu hình.

### Câu hỏi phỏng vấn liên quan
Nếu lỡ commit secret vào git rồi xóa ở commit sau thì đã an toàn chưa?

### Câu trả lời ngắn gọn
Chưa. Secret vẫn tồn tại trong git history, fork, cache CI hoặc remote clone. Cần rotate secret ngay, sau đó mới cân nhắc rewrite history bằng công cụ phù hợp nếu repo yêu cầu.

---

## 114. Localhost Port Binding cho Internal Services

### Giải thích ngắn gọn
Khi chạy Docker Compose production-like, không phải service nào cũng cần public port. Database và Redis thường chỉ cần backend truy cập nội bộ. Bind port vào `127.0.0.1` hoặc bỏ publish port sẽ giảm bề mặt tấn công so với mở ra mọi interface `0.0.0.0`.

### Ví dụ trong project này
`docker-compose.prod.yml` bind MariaDB và Redis vào localhost để admin vẫn có thể debug từ host nếu cần, nhưng không public trực tiếp database/cache ra mạng ngoài.

### Câu hỏi phỏng vấn liên quan
Sự khác nhau giữa `ports` và `expose` trong Docker Compose là gì?

### Câu trả lời ngắn gọn
`ports` publish container port ra host. `expose` chỉ document/mở port trong Docker network cho service khác truy cập, không publish ra host. Với DB/Redis production, ưu tiên Docker network hoặc localhost binding thay vì public host binding.

---

## 115. Fail-Open vs Fail-Closed Rate Limiter

### Giải thích ngắn gọn
Rate limiter dùng Redis có thể gặp lỗi khi Redis down, mất password hoặc network lỗi. Lúc đó hệ thống phải chọn policy: fail-open cho request đi qua, hoặc fail-closed chặn request. Đây là tradeoff giữa availability và security.

### Ví dụ trong project này
`RedisRateLimiterService` hỗ trợ cấu hình `APP_RATE_LIMIT_REDIS_FAILURE_POLICY` và normalize cả `fail-open` lẫn `fail_open`. Tài liệu vận hành giải thích khi nào chọn từng policy và cách debug Redis rate-limit key.

### Câu hỏi phỏng vấn liên quan
Khi nào nên dùng fail-closed thay vì fail-open?

### Câu trả lời ngắn gọn
Khi endpoint nhạy cảm với abuse, ví dụ login, password reset, payment hoặc admin mutation. Fail-closed có thể làm giảm availability nếu Redis lỗi, nhưng tránh việc rate limit bị bypass hoàn toàn.

---

## 116. Reproducible Builds & `npm ci` trong Continuous Integration

### Giải thích ngắn gọn
Trong môi trường Continuous Integration (CI), tính nhất quán và khả năng tái lập (reproducibility) của các bản build là yêu cầu sống còn. Nếu dùng `npm install`, npm có thể tự động nâng các dependency dạng caret (`^`) hoặc tilde (`~`) lên bản mới hơn vừa được publish trên registry, hoặc cố gắng bỏ qua cảnh báo xung đột peerDependency. Kết quả là code chạy được ở máy developer nhưng lại fail trên CI, hoặc ngược lại ("Works on my machine").
`npm ci` giải quyết vấn đề này bằng cách:
1. Đọc và thực thi cài đặt chính xác 100% dựa trên cây phụ thuộc được đóng băng trong `package-lock.json`.
2. Kiểm tra tính toàn vẹn (integrity hash) của từng package tarball.
3. Tuân thủ nghiêm ngặt peerDependencies. Nếu phát hiện xung đột (`ERESOLVE`), `npm ci` sẽ dừng ngay lập tức thay vì âm thầm cài đặt sai lệch.

### Ví dụ trong project này
Khi thiết lập GitHub Actions, `npm ci` ban đầu phát hiện xung đột peerDependency giữa `@pinia/testing@2.0.1` (đòi hỏi `pinia >=4.0.2`) và `pinia@3.0.4`. Thay vì lạm dụng flag `--legacy-peer-deps` để che giấu lỗi, chúng ta đã giải quyết tận gốc bằng cách chuẩn hóa `@pinia/testing` về phiên bản `^1.0.3` phù hợp với Pinia 3.x, giúp `package-lock.json` hoàn toàn tương thích và bản build CI chạy an toàn, nhất quán 100%.

### Câu hỏi phỏng vấn liên quan
Tại sao lệnh `npm ci` lại bắt buộc phải có file `package-lock.json`, trong khi `npm install` thì không?

### Câu trả lời ngắn gọn
Vì triết lý của `npm ci` là tái hiện lại chính xác cây thư mục `node_modules` đã được kiểm thử và xác nhận bởi developer thông qua `package-lock.json`. Nếu không có `package-lock.json`, npm không có cơ sở dữ liệu về phiên bản con (transitive dependencies) chính xác để cài đặt, do đó `npm ci` sẽ từ chối chạy. Ngược lại, `npm install` được thiết kế cho việc phát triển nên nếu thiếu `package-lock.json`, nó sẽ tự tính toán dependency tree mới và tự tạo ra lock file.

---

## 117. Static Dry-Run Validation cho Docker Compose trong CI

### Giải thích ngắn gọn
Trước khi đẩy code hoặc image lên môi trường Production, việc kiểm tra xem tệp cấu hình triển khai (`docker-compose.prod.yml`) có hợp lệ hay không là một bước phòng thủ thiết yếu. Tuy nhiên, trong môi trường CI tự động, việc khởi động thật các container (MySQL/MariaDB, Redis, Backend Spring Boot) tốn rất nhiều thời gian, tài nguyên RAM/CPU của runner và yêu cầu phải nạp secret production nhạy cảm.
Giải pháp là sử dụng chế độ Static Dry-Run thông qua lệnh:
```bash
docker compose -f docker-compose.prod.yml config
```
Lệnh này chỉ đọc cấu hình YAML, kiểm tra cú pháp schema chuẩn của Docker Compose, áp dụng các quy tắc nội suy biến môi trường (Environment Variable Interpolation), và in ra cấu hình chuẩn hóa mà không kéo image hay khởi động container.

### Ví dụ trong project này
Workflow GitHub Actions sử dụng tệp `.env.prod.example` (chứa các giá trị giả lập an toàn) để chạy `docker compose config`. Bước này giúp CI xác minh ngay:
- Không có lỗi chính tả tên biến môi trường.
- Các container tham chiếu đúng volume và network.
- Cấu hình port binding `127.0.0.1` hợp lệ.
Toàn bộ quá trình diễn ra trong 2-3 giây mà không cần bất kỳ mật khẩu thật nào.

### Câu hỏi phỏng vấn liên quan
Lệnh `docker compose config` có tác dụng gì trong pipeline CI/CD?

### Câu trả lời ngắn gọn
`docker compose config` đóng vai trò là một static linter và validator cho file Docker Compose. Nó kiểm tra cú pháp YAML, kiểm tra tính hợp lệ của schema phiên bản compose, kiểm tra sự tồn tại của các biến môi trường được nội suy và xác thực liên kết giữa các services/volumes/networks mà không cần khởi chạy container, giúp bắt lỗi cấu hình sớm trong pipeline CI.

---

## 118. Least Privilege Permissions & Concurrency Optimization trong GitHub Actions

### Giải thích ngắn gọn
GitHub Actions cung cấp môi trường tự động hóa mạnh mẽ nhưng cũng là mục tiêu hàng đầu của các cuộc tấn công chuỗi cung ứng (supply-chain attacks). Hai thực hành chuẩn (best practices) quan trọng nhất khi viết workflow:
1. **Nguyên tắc đặc quyền tối thiểu (Least Privilege Permissions):**
   Theo mặc định, `GITHUB_TOKEN` có thể được cấp quyền ghi (write) cho nhiều scope. Nếu một dependency độc hại hoặc script kiểm thử bên thứ ba bị tiêm nhiễm, kẻ tấn công có thể dùng token này để sửa đổi mã nguồn, tạo release giả mạo hoặc thay đổi PR. Bằng cách khai báo tường minh:
   ```yaml
   permissions:
     contents: read
   ```
   chúng ta tước bỏ mọi quyền ghi không cần thiết của runner.
2. **Tối ưu hóa hàng đợi với Concurrency Cancellation:**
   Khi lập trình viên liên tục push commit vào nhánh đang mở PR để sửa lỗi, nếu không cấu hình concurrency, mỗi commit sẽ kích hoạt một pipeline mới chạy song song, gây lãng phí runner phút của tổ chức. Khai báo:
   ```yaml
   concurrency:
     group: ${{ github.workflow }}-${{ github.ref }}
     cancel-in-progress: true
   ```
   sẽ tự động hủy bỏ ngay lập tức các build cũ chưa hoàn thành của nhánh đó để tập trung tài nguyên vào commit mới nhất.

### Ví dụ trong project này
Tệp `.github/workflows/ci.yml` áp dụng đầy đủ cả hai cơ chế trên:
- `permissions: contents: read` ở cấp root workflow.
- `concurrency` nhóm theo `${{ github.workflow }}-${{ github.ref }}` với `cancel-in-progress: true`.
- Tách 3 jobs `backend`, `frontend`, `docker-config` chạy song song (matrix/parallel execution) giúp giảm thời gian phản hồi toàn bộ pipeline xuống mức tối ưu.

### Câu hỏi phỏng vấn liên quan
Tại sao nên dùng `cancel-in-progress: true` trong cấu hình `concurrency` của GitHub Actions PR workflow?

### Câu trả lời ngắn gọn
Vì trên một Pull Request, kết quả kiểm thử của các commit cũ không còn giá trị ngay khi có commit mới được đẩy lên. Bằng cách tự động cancel các build đang chạy dở của commit cũ, hệ thống giải phóng ngay runner tài nguyên, tránh nghẽn hàng đợi CI và giúp developer nhận kết quả kiểm thử của commit mới nhất nhanh hơn.

---

## 119. Parent Search bằng `EXISTS` Subquery

### Giải thích ngắn gọn
Khi cần tìm parent entity dựa trên dữ liệu của child entity, `EXISTS` subquery là một cách an toàn để hỏi: "Có ít nhất một child thỏa điều kiện hay không?". Cách này tránh duplicate parent rows thường gặp khi join trực tiếp vào collection.

### Ví dụ trong project này
Public course search cần tìm course theo lesson title/content. Query dùng:
```sql
EXISTS (
  SELECT 1 FROM Lesson l
  WHERE l.course = c
  AND l.status = 'PUBLISHED'
  AND LOWER(l.title) LIKE ...
)
```
Nhờ vậy một course có 3 lesson match vẫn chỉ xuất hiện một lần trong danh sách khóa học.

### Câu hỏi phỏng vấn liên quan
Khi nào `EXISTS` tốt hơn `JOIN` trong truy vấn search?

### Câu trả lời ngắn gọn
Khi chỉ cần biết child entity có tồn tại hay không, không cần lấy dữ liệu child vào result. `EXISTS` giúp tránh nhân bản parent rows và thường diễn đạt đúng intent hơn.

---

## 120. Backward-Compatible Multi-Value Query Parameter

### Giải thích ngắn gọn
Backward-compatible API evolution là mở rộng khả năng API mà không phá client cũ. Với filter một giá trị, có thể giữ nguyên tên param nhưng cho phép định dạng nhiều giá trị phân tách bằng dấu phẩy.

### Ví dụ trong project này
Trước đây public course API nhận `level=N5`. Sau task này, API vẫn nhận `level=N5`, đồng thời nhận thêm `level=N5,N4`. Frontend multi-select gửi cùng param nên không cần tạo endpoint mới.

### Câu hỏi phỏng vấn liên quan
Ưu và nhược điểm của comma-separated query param so với repeated query param là gì?

### Câu trả lời ngắn gọn
Comma-separated dễ giữ backward-compatible và URL gọn (`level=N5,N4`). Repeated param (`level=N5&level=N4`) rõ semantic hơn với framework hỗ trợ collection trực tiếp. Chọn cách nào nên dựa trên contract hiện có và mức độ muốn tránh breaking change.

---

## 121. URL-Driven Filter State

### Giải thích ngắn gọn
URL-driven filter state là kỹ thuật đồng bộ trạng thái search/filter/pagination của UI vào query string. Nhờ đó URL trở thành source có thể share, refresh và back/forward được.

### Ví dụ trong project này
`CourseListPage.vue` đọc `route.query.keyword`, `route.query.level`, `route.query.courseType`, `route.query.sort`, `route.query.page` để khởi tạo state. Khi user chọn N5 và N4, page cập nhật URL thành `?level=N5,N4`.

### Câu hỏi phỏng vấn liên quan
Vì sao search page nên lưu filter vào URL thay vì chỉ giữ trong component state?

### Câu trả lời ngắn gọn
Vì search/filter là trạng thái điều hướng. User kỳ vọng copy link cho người khác, refresh trang hoặc quay lại bằng browser back mà vẫn thấy cùng kết quả.

---

## 122. Public API Enrichment via SecurityContextHolder

### Giải thích ngắn gọn
Public API Enrichment là kỹ thuật cho phép một public endpoint (không bắt buộc đăng nhập) tự động phát hiện danh tính người dùng nếu có token xác thực gửi kèm trong header `Authorization`. Dựa vào danh tính này, backend làm giàu thêm (enrich) dữ liệu cá nhân hóa (như `isEnrolled`, `lastLessonId`) vào response payload mà không phá vỡ khả năng truy cập tự do của khách vãng lai.

### Ví dụ trong project này
Tại `CoursePublicServiceImpl.java`:
```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
    String username = auth.getName();
    // Query User, CourseEnrollment, LessonProgress để gán isEnrolled và lastLessonId
}
```
Khách vãng lai nhận `isEnrolled = false, lastLessonId = null`, còn học viên nhận `isEnrolled = true, lastLessonId = 15`.

### Câu hỏi phỏng vấn liên quan
Tại sao không tách thành 2 endpoint: một endpoint public cho khóa học, và một endpoint private cho trạng thái của học viên?

### Câu trả lời ngắn gọn
Tách 2 endpoint đòi hỏi frontend phải gửi 2 request nối tiếp (waterfall requests), gây ra độ trễ hiển thị và hiện tượng nút bấm bị nhảy trạng thái (state flickering). Gộp (enrich) vào 1 response giúp client render ngay lập tức trạng thái chính xác nhất (zero-latency UI state transition).

---

## 123. Backend-Driven UI State vs Client-Side Async Resolution

### Giải thích ngắn gọn
Backend-Driven UI State là mô hình trong đó server tính toán và trả về cờ trạng thái giao diện (như quyền hành động, trạng thái ghi danh, bước học tiếp theo) ngay trong API response, thay vì bắt client phải tự tải nhiều tập dữ liệu về rồi tự suy luận logic hiển thị.

### Ví dụ trong project này
Thay vì frontend phải gọi `StudentService.getMyCourses()`, lặp qua danh sách so sánh `c.id === courseId` để suy ra học viên đã đăng ký hay chưa, backend kiểm tra trực tiếp qua index DB và trả về `isEnrolled: true` ngay trong `CourseDetailPublicRes`.

### Câu hỏi phỏng vấn liên quan
Khi nào nên dùng backend-driven UI state thay vì client-side resolution?

### Câu trả lời ngắn gọn
Khi trạng thái đó quyết định các CTA hành vi sống còn (nút Mua/Ghi danh/Học tiếp), hoặc khi logic kiểm tra trên client tốn kém tài nguyên mạng/bộ nhớ và dễ dẫn đến bất đồng bộ giữa các trang hoặc giữa mobile và desktop.

---

## 124. Context-Aware UX & State Flickering Prevention

### Giải thích ngắn gọn
Context-Aware UX là thiết kế giao diện tự động thích ứng với trạng thái và mục đích hiện tại của người dùng. State Flickering Prevention là tập hợp các kỹ thuật frontend nhằm ngăn chặn hiện tượng component hiển thị trạng thái A (mặc định) trong vài mili-giây rồi bất ngờ đổi sang trạng thái B (thực tế) khi dữ liệu bất đồng bộ trả về.

### Ví dụ trong project này
- **Context-Aware:** `Navbar.vue` hiển thị "Khóa học của tôi" khi đã đăng nhập; `HomePage.vue` đổi banner CTA từ "Đăng ký tài khoản" thành "Vào học ngay".
- **Flickering Prevention:** `CourseDetailPage.vue` khởi tạo và bind ngay `course.isEnrolled` từ response chi tiết khóa học, loại bỏ hoàn toàn việc nút nháy từ "Đăng ký khóa học ngay" sang "Tiếp tục học".

### Câu hỏi phỏng vấn liên quan
Làm thế nào để tránh UI flickering khi component phụ thuộc vào dữ liệu authentication bất đồng bộ?

### Câu trả lời ngắn gọn
1. Lưu cache/hydrate auth state trong persistent store (Pinia + LocalStorage) trước khi component render.
2. Dùng skeleton loading placeholder hoặc backend-enriched response thay vì render giao diện mặc định dạng "unauthenticated" trước khi fetch xong API.

---

## 125. HTML5 Content Model & Nested Interactive Controls Restriction

### Giải thích ngắn gọn
Chuẩn HTML5 (W3C Content Model) phân loại các phần tử HTML thành các nhóm: Flow Content, Phrasing Content, Interactive Content. Quy tắc cơ bản quy định rằng một phần tử thuộc loại "Interactive Content" (như `<a>`, `<button>`, `<input>`, `<select>`) **không được phép** chứa bất kỳ phần tử Interactive Content nào khác bên trong.

### Ví dụ trong project này
Tại `HomePage.vue` và `CourseListPage.vue`, các thẻ khóa học trước đây bọc toàn bộ khối bằng `<router-link to="...">` (thẻ `<a>`), nhưng bên trong lại đặt thêm `<button class="...">Mua ngay</button>`. Điều này được sửa lại bằng cách đổi nút bên trong thành `<span class="inline-flex items-center ...">Mua ngay</span>`, đảm bảo toàn bộ thẻ card là một liên kết hợp lệ duy nhất.

### Câu hỏi phỏng vấn liên quan
Hậu quả kỹ thuật khi vi phạm quy tắc Nested Interactive Content là gì?

### Câu trả lời ngắn gọn
Gây ra xung đột bubbling sự kiện click, hành vi không thể dự đoán giữa các trình duyệt (ví dụ: Safari kích hoạt cả hai handler), phá vỡ thứ tự phím Tab, và làm công cụ hỗ trợ người khiếm thị (Screen Reader) thông báo hai nút điều khiển lồng nhau gây nhầm lẫn nghiêm trọng.

---

## 126. Accessible Clickable Card Pattern (WCAG 2.1)

### Giải thích ngắn gọn
Accessible Clickable Card Pattern là kỹ thuật xây dựng các thẻ giao diện tổng hợp (card) có thể bấm vào toàn bộ vùng hiển thị nhưng vẫn đáp ứng đầy đủ tiêu chuẩn tiếp cận WCAG: có vai trò rõ ràng, nhận diện được bằng bàn phím, và cung cấp nhãn ngữ nghĩa đầy đủ.

### Ví dụ trong project này
Tại `MyCourseCard.vue`:
```html
<div 
  class="bg-stitch-card rounded-2xl cursor-pointer focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring"
  tabindex="0"
  role="button"
  :aria-label="`Tiếp tục học ${course.courseName}`"
  @click="$emit('continue', course)"
  @keydown.enter="$emit('continue', course)"
  @keydown.space.prevent="$emit('continue', course)"
>
```
Nút bên trong chỉ là thẻ `<span>` để hiển thị visual CTA `"Tiếp tục học →"`.

### Câu hỏi phỏng vấn liên quan
Tại sao sự kiện `@keydown.space` cần có modifier `.prevent`?

### Câu trả lời ngắn gọn
Vì trong trình duyệt, phím Space mặc định sẽ cuộn trang xuống một khoảng bằng chiều cao màn hình. Dùng `.prevent` sẽ ngăn chặn hành vi cuộn mặc định và chỉ thực thi hành động kích hoạt nút của card.

---

## 127. Mobile Navigation Shell & Viewport Drawer Traps

### Giải thích ngắn gọn
Mobile Navigation Shell là cấu trúc điều hướng thu gọn chuyên biệt cho thiết bị màn hình nhỏ (smartphones/tablets). Để đảm bảo trải nghiệm người dùng cao cấp, một Mobile Drawer cần xử lý đầy đủ các tương tác: lớp nền tối làm mờ (backdrop overlay), khả năng đóng linh hoạt (chạm ra ngoài, phím Escape, hoặc chọn menu item), và không được đẩy hay bóp méo nội dung chính bên dưới.

### Ví dụ trong project này
Tại `AdminLayout.vue`, sidebar desktop cố định `w-56` được ẩn trên mobile bằng `hidden md:flex`. Màn hình nhỏ dùng nút Hamburger trên top bar kích hoạt một drawer `fixed inset-0 z-50 md:hidden` trượt ra từ bên trái với backdrop `bg-black/70 backdrop-blur-sm`, đóng lại khi click backdrop hoặc nhấn Escape, giúp nội dung admin trang quản trị hiển thị đầy đủ và không bị tràn layout.

### Câu hỏi phỏng vấn liên quan
Khi nào nên dùng Navigation Drawer thay vì Bottom Navigation Bar trên mobile?

### Câu trả lời ngắn gọn
Khi menu có nhiều hơn 5 mục lựa chọn, hoặc khi menu phân cấp theo vai trò quản trị viên phức tạp có nhiều tác vụ phụ (như đăng xuất, chuyển sang trang công khai). Bottom Navigation chỉ phù hợp khi ứng dụng có 3-5 tác vụ cốt lõi lặp đi lặp lại hàng ngày.

---

## 128. Financial Data Snapshotting (Bất Biến Thông Tin Đơn Hàng)

### Giải thích ngắn gọn
Financial Data Snapshotting là nguyên tắc thiết kế cơ sở dữ liệu và domain logic trong e-commerce: khi một đơn hàng được tạo, các thông tin nhạy cảm về giá bán, tên mặt hàng, chiết khấu và thuế phải được sao chép (snapshot) trực tiếp vào bảng đơn hàng / chi tiết đơn hàng (`orders`, `order_items`), thay vì chỉ lưu khóa ngoại (`course_id`) trỏ sang bảng danh mục (`courses`).

### Ví dụ trong project này
Tại `OrderServiceImpl.java`:
```java
BigDecimal effectivePrice = (course.getSalePrice() != null && course.getSalePrice().compareTo(BigDecimal.ZERO) > 0)
        ? course.getSalePrice()
        : course.getOriginalPrice();

OrderItem item = OrderItem.builder()
        .course(course)
        .courseTitle(course.getTitle()) // Snapshot tên khóa học
        .price(effectivePrice)          // Snapshot giá bán hiệu lực
        .quantity(1)
        .build();
```
Nếu 6 tháng sau, admin tăng giá khóa học từ 350.000đ lên 500.000đ hoặc đổi tên thành "Khóa học N3 Pro", lịch sử đơn hàng của học viên vẫn hiển thị chính xác tên và số tiền 350.000đ mà họ đã mua tại thời điểm đó.

### Câu hỏi phỏng vấn liên quan
Tại sao không nên join trực tiếp bảng `courses` để lấy giá và tên khi hiển thị lịch sử đơn hàng?

### Câu trả lời ngắn gọn
Vì dữ liệu trong bảng `courses` là dữ liệu biến đổi theo thời gian (mutable catalog data). Nếu chỉ lưu ID và join khi query, giá của đơn hàng cũ sẽ bị biến động mỗi khi sản phẩm đổi giá, gây sai lệch báo cáo kế toán, tranh chấp pháp lý với khách hàng và vi phạm tính toàn vẹn kiểm toán (audit trail).

---

## 129. Defense-in-Depth Authorization (Bảo Mật Nhiều Lớp trong Spring Security)

### Giải thích ngắn gọn
Defense-in-depth (bảo vệ theo chiều sâu) là phương pháp bảo mật không phụ thuộc vào một tầng chốt chặn duy nhất. Trong backend Spring Boot, nó được hiện thực bằng cách kết hợp cả tầng lọc yêu cầu mạng (Filter Chain URL Matching) lẫn tầng phân quyền phương thức (Method Security with AOP).

### Ví dụ trong project này
1. **Lớp 1 - Web Security Filter Chain (`SecurityConfig.java`):**
   ```java
   .requestMatchers("/api/v1/orders", "/api/v1/orders/**").hasRole("STUDENT")
   ```
2. **Lớp 2 - Method Security (`OrderController.java`):**
   ```java
   @PostMapping
   @PreAuthorize("hasRole('STUDENT')")
   public ApiResponse<OrderRes> createOrder(@Valid @RequestBody OrderCreateReq req) { ... }
   ```
3. **Lớp 3 - Domain Ownership Check (`OrderServiceImpl.java`):**
   ```java
   if (!order.getUser().getId().equals(currentUser.getId())) {
       throw new AppException(ErrorCode.ORDER_ACCESS_DENIED);
   }
   ```
Dù developer có lỡ chỉnh sửa cấu hình endpoint ở `SecurityConfig`, tầng `@PreAuthorize` vẫn bảo vệ method. Và dù method security cho phép role `STUDENT`, tầng service vẫn kiểm tra người học chỉ được xem đúng đơn hàng do chính mình tạo ra (ngăn IDOR / cross-user data tampering).

### Câu hỏi phỏng vấn liên quan
Khi nào nên dùng URL pattern matching trong `SecurityFilterChain`, và khi nào nên dùng `@PreAuthorize`?

### Câu trả lời ngắn gọn
URL matching trong Filter Chain tốt cho việc phân vùng quyền diện rộng (ví dụ: `/api/admin/**`, `/api/auth/**`) chặn request ngay từ cửa ngõ trước khi vào DispatcherServlet. `@PreAuthorize` phù hợp cho phân quyền chi tiết (fine-grained authorization) tại từng hàm, hỗ trợ biểu thức SpEL phức tạp và kiểm tra thuộc tính của argument hoặc return object.

---

## 130. Duplicate Pending Order Prevention Policy trong E-Commerce

### Giải thích ngắn gọn
Duplicate Pending Order Prevention Policy là chính sách nghiệp vụ quy định hệ thống sẽ phản ứng ra sao khi một khách hàng gửi yêu cầu tạo đơn hàng mới cho một mặt hàng mà họ đã có một đơn hàng đang ở trạng thái chờ thanh toán (`PENDING`).

### Ví dụ trong project này
Tại `OrderServiceImpl.java`:
```java
boolean hasPendingOrder = orderRepository.existsByUserIdAndCourseIdAndStatus(
        currentUser.getId(),
        course.getId(),
        OrderStatus.PENDING
);
if (hasPendingOrder) {
    throw new AppException(ErrorCode.ORDER_DUPLICATE_PENDING);
}
```
Khi học viên bấm nút "Mua khóa học" nhiều lần hoặc mở nhiều tab, backend từ chối tạo đơn mới và trả về HTTP `409 CONFLICT` với mã lỗi `5004 ORDER_DUPLICATE_PENDING`.

### Câu hỏi phỏng vấn liên quan
Tại sao chọn trả về lỗi `409 Conflict` thay vì âm thầm trả về đơn hàng `PENDING` cũ (idempotent reuse)?

### Câu trả lời ngắn gọn
Trả về `409 Conflict` rõ ràng hơn về mặt ngữ nghĩa (explicit business contract): (1) Ngăn ngừa rủi ro sai lệch giá nếu giá khóa học thay đổi giữa 2 lần bấm, (2) Giúp frontend phân biệt rõ ràng giữa "vừa tạo mới thành công" và "đã có đơn hàng cũ cần tiếp tục thanh toán", từ đó hiển thị thông báo chính xác và chuyển hướng người dùng đến đúng hóa đơn đang chờ.

---

## 131. Token Hashing at Rest vs. Password Hashing (SHA-256 vs. BCrypt)

### Giải thích ngắn gọn
Mã hóa lưu trữ dữ liệu xác thực (At-Rest Security) chia làm hai trường phái hoàn toàn khác biệt tùy thuộc vào **Entropy (Độ ngẫu nhiên/Độ phức tạp)** của dữ liệu:
1. **Low-Entropy Data (Mật khẩu người dùng):** Người dùng thường đặt mật khẩu ngắn, dễ đoán, lặp lại. Bắt buộc phải sử dụng các thuật toán **Slow Hashing với Salt ngẫu nhiên và Memory-Hard** (như BCrypt, Argon2, PBKDF2). Mục tiêu là làm chậm tốc độ brute-force (ví dụ 1 hash mất 100-250ms).
2. **High-Entropy Data (Refresh Tokens, API Keys, Session Tokens):** Là các chuỗi ngẫu nhiên dài được sinh bằng thuật toán mật mã (cryptographically secure random) hoặc JWT ký chữ ký số 256/512-bit. Không gian mẫu là $2^{256}$, không thể bị brute-force hay tấn công rainbow table. Do đó, các token này **phải dùng Fast Deterministic Cryptographic Hash (như SHA-256)**.

### Ví dụ trong project này
Tại `TokenHashUtil.java` và `AuthServiceImpl.java`:
```java
public static String hashToken(String token) {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(token.trim().getBytes(StandardCharsets.UTF_8));
    // convert to lowercase hex string
}

// Khi lưu CSDL:
RefreshToken entity = RefreshToken.builder()
    .user(user)
    .tokenHash(TokenHashUtil.hashToken(refreshTokenString))
    .token(null) // Tuyệt đối không lưu plaintext
    .build();
refreshTokenRepository.save(entity);

// Khi tra cứu:
String tokenHash = TokenHashUtil.hashToken(rawToken);
Optional<RefreshToken> tokenEntity = refreshTokenRepository.findByTokenHash(tokenHash);
```
Vì SHA-256 là **tất định (deterministic)** (cùng 1 input luôn ra đúng 1 chuỗi hash 64 hex), database có thể đánh index Unique trên `token_hash` và tìm kiếm bản ghi với độ phức tạp **$O(1)$** mà không tốn tải CPU. Nếu dùng BCrypt, do salt ngẫu nhiên mỗi lần hash, ta không thể `SELECT WHERE token = hash(token)` được, buộc phải load toàn bộ bảng ra kiểm tra `matches()`, gây sập hệ thống (Denial of Service).

---

## 132. Refresh Token Rotation (RTR) & Token Reuse Detection

### Giải thích ngắn gọn
**Refresh Token Rotation (RTR)** là kỹ thuật bảo mật quy định mỗi Refresh Token chỉ được phép sử dụng **một lần duy nhất**. Khi client gửi Refresh Token cũ lên để xin Access Token mới, backend sẽ thu hồi (revoke) token cũ và cấp phát một Refresh Token hoàn toàn mới.

**Reuse Detection (Phát hiện tái sử dụng)** là cơ chế phòng vệ tự động: Nếu một Refresh Token đã bị thu hồi (`revoked == true`) mà lại được gửi lên server một lần nữa, hệ thống kết luận ngay rằng token đã bị rò rỉ (Token Theft) và một trong hai bên (kẻ tấn công hoặc nạn nhân) đang cố gắng sử dụng token cũ. Hệ thống sẽ ngay lập tức **hủy toàn bộ họ token (Token Family Revocation)** của người dùng đó, buộc cả hai bên phải đăng nhập lại.

### Ví dụ trong project này
Tại `AuthServiceImpl.java`:
```java
// 1. Kiểm tra Token Reuse Detection
if (Boolean.TRUE.equals(refreshTokenEntity.getRevoked())) {
    log.warn("Token reuse detected! Revoking all tokens for user ID: {}", refreshTokenEntity.getUser().getId());
    refreshTokenRepository.revokeAllByUserId(refreshTokenEntity.getUser().getId());
    throw new AppException(ErrorCode.REFRESH_TOKEN_REVOKED);
}

// 2. Refresh Token Rotation: Hủy token cũ
refreshTokenEntity.setRevoked(true);
refreshTokenRepository.save(refreshTokenEntity);

// 3. Cấp phát token mới
String newAccessToken = jwtUtil.generateAccessToken(user);
String newRefreshToken = jwtUtil.generateRefreshToken(user);
RefreshToken newEntity = RefreshToken.builder()
    .user(user)
    .tokenHash(TokenHashUtil.hashToken(newRefreshToken))
    .token(null)
    .expiredAt(LocalDateTime.now().plusDays(7))
    .build();
refreshTokenRepository.save(newEntity);
```

---

## 133. Zero-Downtime Safe Migration Pattern cho Secret Data (Dual-Read & On-the-fly Upgrade)

### Giải thích ngắn gọn
Khi thay đổi cơ chế lưu trữ dữ liệu từ Plaintext sang Hashed trong một hệ thống lớn đang hoạt động liên tục (Live Production), không thể dừng hệ thống hoặc chạy một câu lệnh SQL Update khổng lồ khóa bảng (Table Lock). 

Chiến lược **Dual-Read & On-the-fly Upgrade (Lazy Migration)** giải quyết bài toán này:
1. **Step 1 - Non-blocking Schema Extension:** Thêm cột mới (`token_hash`) cho phép `NULL`.
2. **Step 2 - Dual-Read:**
   - Ưu tiên đọc bằng định dạng mới (`findByTokenHash`).
   - Nếu không thấy, fallback đọc định dạng cũ (`findByToken`).
3. **Step 3 - On-the-fly Upgrade:** Khi một bản ghi cũ được đọc qua fallback, hệ thống lập tức băm dữ liệu, cập nhật `tokenHash`, xóa trắng cột `token` plaintext về `null` và lưu lại.
4. **Step 4 - Natural Expiration Cleanup:** Các bản ghi cũ không được sử dụng sẽ tự hết hạn (TTL 7 ngày) và bị tiến trình dọn dẹp ban đêm (`TokenCleanupScheduler`) xóa vĩnh viễn.

### Ví dụ trong project này
```java
private Optional<RefreshToken> findRefreshToken(String rawToken) {
    if (rawToken == null || rawToken.isBlank()) return Optional.empty();
    String tokenHash = TokenHashUtil.hashToken(rawToken);
    
    // 1. Ưu tiên tìm qua token_hash
    Optional<RefreshToken> byHash = refreshTokenRepository.findByTokenHash(tokenHash);
    if (byHash.isPresent()) {
        return byHash;
    }
    
    // 2. Fallback tìm theo plaintext cho dữ liệu cũ (Legacy records)
    Optional<RefreshToken> legacy = refreshTokenRepository.findByToken(rawToken);
    if (legacy.isPresent()) {
        RefreshToken entity = legacy.get();
        // 3. Nâng cấp on-the-fly
        entity.setTokenHash(tokenHash);
        entity.setToken(null);
        return Optional.of(refreshTokenRepository.save(entity));
    }
    return Optional.empty();
}
```
Kết quả: 100% người dùng đang hoạt động không nhận thấy bất kỳ sự gián đoạn nào (Zero Downtime, No Force-Logout).

---

## 134. Cookie Security Attributes & The Least-Privilege Transport Principle

### Giải thích ngắn gọn
Cookie không chỉ là nơi lưu trữ key-value đơn thuần mà là một giao thức ủy quyền vận chuyển giữa Trình duyệt và Server. Để bảo vệ các bí mật nhạy cảm (như Refresh Token), việc thiết lập đúng các cờ bảo vệ (Cookie Flags) là bắt buộc:

1. **`HttpOnly = true`:**
   - **Tác dụng:** Cấm hoàn toàn JavaScript truy cập cookie qua thuộc tính `document.cookie`.
   - **Mục tiêu:** Miễn nhiễm trước các cuộc tấn công **XSS (Cross-Site Scripting)**. Ngay cả khi hacker chèn được script độc hại vào trang web, script đó cũng không thể đọc hoặc gửi Refresh Token về server của hacker.
2. **`Secure = true`:**
   - **Tác dụng:** Yêu cầu trình duyệt chỉ được phép gửi cookie qua các kết nối được mã hóa bằng **HTTPS / TLS**.
   - **Mục tiêu:** Ngăn chặn tấn công nghe lén đường truyền mạng **Man-in-the-Middle (MitM)** hoặc rò rỉ token qua mạng Wi-Fi công cộng không an toàn.
3. **`SameSite` (Strict vs. Lax vs. None):**
   - **`Strict`:** Cookie chỉ được gửi đi khi request bắt nguồn từ chính domain đó (First-party context). Nếu user click một đường link từ email hoặc Facebook sang web của bạn, cookie sẽ KHÔNG được gửi kèm.
   - **`Lax` (Khuyến nghị cho Authentication):** Cho phép gửi cookie khi user điều hướng cấp cao nhất (Top-level GET navigation như click link), nhưng **chặn tuyệt đối** khi request được kích hoạt chéo domain từ các thẻ `<img>`, `<iframe>`, hoặc form POST ngầm.
   - **`None`:** Cho phép gửi xuyên domain (Cross-site). Bắt buộc phải đi kèm `Secure = true`.
4. **`Path` (Nguyên tắc Đặc quyền Tối thiểu - Least Privilege):**
   - Thay vì để `Path=/` (mọi request tới mọi endpoint của server đều đính kèm Refresh Token), ta cấu hình `Path=/api/auth`.
   - Chỉ khi trình duyệt gọi các endpoint như `/api/auth/refresh-token` hay `/api/auth/logout`, cookie mới được gửi. Các request học tập, xem video, làm quiz thông thường sẽ không mang theo Refresh Token, giảm thiểu tối đa diện tích tấn công (Attack Surface).

### Ví dụ trong project này
Tại `CookieUtil.java`:
```java
ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(isSecure)
        .sameSite("Lax")
        .path("/api/auth")
        .maxAge(7 * 24 * 60 * 60) // 7 ngày
        .build();
response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
```

---

## 135. Ma Trận Mô Hình Đe Dọa (Threat Modeling Matrix) Cho Kiến Trúc Dual-Token

| Vector Tấn Công | Điểm Yếu Thường Gặp | Cơ Chế Phòng Thủ Đã Triển Khai Trong Dự Án |
| :--- | :--- | :--- |
| **XSS (Cross-Site Scripting)** | Lưu Refresh Token trong `localStorage` hoặc SessionStorage. | **`HttpOnly` Cookie:** JS không thể đọc được token. Đồng thời DTO Response đánh dấu `@JsonIgnore` loại bỏ hoàn toàn token khỏi JSON payload. |
| **CSRF (Cross-Site Request Forgery)** | Cookie gửi tự động trong các request cross-origin độc hại. | **`SameSite=Lax`:** Trình duyệt chặn gửi cookie từ các domain lạ. Ngoài ra các API thay đổi trạng thái đều dùng phương thức `POST` và xác thực qua CSRF-safe context. |
| **Rò rỉ Database / Backup Leak (SQL Injection / Dump Leak)** | Lưu token dạng plaintext trong bảng `refresh_tokens`. Kẻ tấn công trích xuất và chiếm quyền phiên hàng loạt. | **SHA-256 Hashing At Rest:** CSDL chỉ lưu chuỗi băm hex 64 ký tự. Không thể dịch ngược về JWT gốc để sử dụng. |
| **Token Theft & Replay Attack (Đánh cắp Refresh Token)** | Token dùng được nhiều lần vô hạn, không phát hiện được khi bị đánh cắp. | **Refresh Token Rotation (RTR) + Reuse Detection:** Token chỉ dùng 1 lần. Nếu token cũ đã bị thu hồi mà tái xuất hiện, hệ thống lập tức kích hoạt hủy toàn bộ họ token (`revokeAllByUserId`), ép logout cả 2 bên. |
| **Man-in-the-Middle (MitM)** | Truyền tải qua HTTP không mã hóa. | **`Secure = true` Flag:** Trình duyệt chỉ gửi cookie qua kết nối mã hóa SSL/TLS. |
| **Over-Exposure (Thừa đặc quyền truyền dẫn)** | Cookie gửi kèm cho tất cả static assets và API không liên quan. | **Scoped `Path=/api/auth`:** Giới hạn cookie chỉ gửi cho các route xác thực cần thiết. |

---

## 136. Idempotent Payment Completion

### Giải thích ngắn gọn

Idempotent Payment Completion nghĩa là request hoàn tất thanh toán có thể đến nhiều lần do double-click, timeout hoặc provider retry, nhưng hệ thống chỉ ghi nhận một giao dịch thành công và một lần cấp quyền học.

### Ví dụ trong project này

`PaymentServiceImpl.simulatePayment()` khóa order trước. Nếu order đã là `PAID`, service lấy `PaymentStatus.SUCCESS` đã tồn tại và trả response; không save thêm `Payment`, không tạo enrollment mới, không tăng `Course.totalStudents`.

### Câu hỏi phỏng vấn liên quan

Vì sao payment callback cần idempotent ngay cả khi frontend đã disable nút thanh toán?

### Câu trả lời ngắn gọn

Disable nút chỉ cải thiện UX và không kiểm soát được retry mạng, nhiều tab, hay callback từ provider. Backend phải tự bảo vệ dữ liệu để một giao dịch chỉ được ghi nhận đúng một lần.

---

## 137. Pessimistic Locking cho Checkout

### Giải thích ngắn gọn

Pessimistic locking là việc database khóa bản ghi khi transaction đọc để ghi. Transaction khác muốn cập nhật cùng bản ghi phải chờ tới khi lock được giải phóng.

### Ví dụ trong project này

`OrderRepository.findByIdForUpdate()` dùng `@Lock(LockModeType.PESSIMISTIC_WRITE)`. Trong `simulatePayment`, request đầu tiên khóa order `PENDING`, ghi payment và chuyển nó thành `PAID`; request thứ hai sau đó đọc trạng thái mới và trả kết quả idempotent.

### Câu hỏi phỏng vấn liên quan

Khi nào cân nhắc pessimistic lock thay vì optimistic lock?

### Câu trả lời ngắn gọn

Dùng pessimistic lock khi xung đột ghi cần được tuần tự hóa ngay từ đầu và side effect rất nhạy cảm, ví dụ payment. Optimistic lock phù hợp hơn với luồng đọc nhiều, xung đột ít và có thể retry an toàn.

---

## 138. Server-Authoritative Pricing

### Giải thích ngắn gọn

Server-authoritative pricing là nguyên tắc backend là nguồn duy nhất quyết định giá, khuyến mãi và số tiền phải trả. Frontend chỉ gửi định danh sản phẩm và hiển thị snapshot do backend trả về.

### Ví dụ trong project này

`CourseDetailPage.vue` gọi `OrderService.createOrder(courseId)` mà không gửi `price`. `CheckoutPage.vue` hiển thị `order.finalAmount`, `order.totalAmount` và item snapshot từ `GET /api/v1/orders/{id}`.

### Câu hỏi phỏng vấn liên quan

Vì sao không nên tin giá tiền do frontend gửi lên API checkout?

### Câu trả lời ngắn gọn

Người dùng có thể sửa request bằng DevTools hoặc proxy. Nếu server tin giá client gửi, họ có thể mua khóa học với giá 0. Backend phải tự tính và snapshot giá từ dữ liệu tin cậy.

---

## 139. Purchase Recovery Flow

### Giải thích ngắn gọn

Purchase recovery flow là luồng giúp người dùng quay lại giao dịch đang dở dang thay vì tạo order mới hoặc nhận một lỗi kỹ thuật khó hiểu.

### Ví dụ trong project này

Khi tạo order cho cùng course nhận `ORDER_DUPLICATE_PENDING` (5004), frontend gọi `GET /api/v1/orders/me`, tìm order `PENDING` có `item.courseId` phù hợp, rồi điều hướng tới `/checkout/:orderId`.

### Câu hỏi phỏng vấn liên quan

Tại sao không chỉ hiển thị lỗi 409 khi user đã có pending order?

### Câu trả lời ngắn gọn

409 đúng về nghiệp vụ, nhưng không đủ tốt về trải nghiệm. Frontend nên hiểu error code đặc thù và hướng user đến hành động tiếp theo an toàn là tiếp tục thanh toán order hiện có.

---

## 140. Fail-Fast Startup Validation với `@PostConstruct`

### Giải thích ngắn gọn

Fail-fast startup validation là pattern kiểm tra tính hợp lệ của cấu hình ngay tại thời điểm ứng dụng khởi động. Nếu phát hiện cấu hình không an toàn (secret yếu, placeholder chưa thay, mật khẩu mặc định), ứng dụng ném exception và từ chối khởi động hoàn toàn.

Nguyên tắc cốt lõi: **Phát hiện lỗi ở deployment, không phải sau incident.** Một hệ thống chạy thành công với JWT secret 6 bytes sẽ hoạt động bình thường cho tới khi bị brute-force — và lúc đó thì đã quá muộn.

### Ví dụ trong project này

`ProductionSecurityValidator` sử dụng `@PostConstruct` để gọi `validate()` ngay sau khi Spring inject xong tất cả `@Value`. Nếu danh sách `errors` không rỗng, method gọi `throw new IllegalStateException(errorMessage)` buộc Spring Context khởi tạo thất bại và application process thoát. Bean này chỉ tồn tại khi `@Profile("prod")`, không ảnh hưởng development.

```java
@PostConstruct
public void validateOnStartup() {
    List<String> errors = validate();
    if (!errors.isEmpty()) {
        throw new IllegalStateException(
            "Production configuration validation failed with " + errors.size() + " violation(s):\n" + ...
        );
    }
}
```

### Câu hỏi phỏng vấn liên quan

Tại sao không để ứng dụng chạy rồi kiểm tra cấu hình sau?

### Câu trả lời ngắn gọn

Nếu chạy trước rồi kiểm tra sau, trong khoảng thời gian đó hệ thống phục vụ traffic với cấu hình không an toàn. Với JWT secret yếu, token đã ký trong giai đoạn này bị compromise vĩnh viễn. Fail-fast đảm bảo không có traffic nào được phục vụ với cấu hình lỗi.

---

## 141. Aggregate Error Reporting — Gom lỗi thay vì Fail-on-First

### Giải thích ngắn gọn

Aggregate error reporting là kỹ thuật thu thập tất cả vi phạm vào một collection rồi báo cáo toàn bộ trong một lần duy nhất, thay vì ném exception ngay khi gặp lỗi đầu tiên.

Trong ngữ cảnh configuration validation: nếu có 5 biến environment chưa được cung cấp, DevOps cần biết tất cả 5 biến đó trong một lần deploy thất bại thay vì phải deploy → fail → fix → deploy → fail → fix lặp đi lặp lại 5 lần.

### Ví dụ trong project này

`ProductionSecurityValidator.validate()` tạo một `List<String> errors = new ArrayList<>()`, truyền vào 8 phương thức validate con (`validateJwtSecrets(errors)`, `validateDatabase(errors)`, `validateRedis(errors)`, `validateCors(errors)`...). Mỗi phương thức chỉ `add()` mô tả lỗi vào list mà không throw. Sau khi tất cả đã chạy xong, `validateOnStartup()` kiểm tra list và ném một exception duy nhất chứa toàn bộ vi phạm.

```java
public List<String> validate() {
    List<String> errors = new ArrayList<>();
    validateJwtSecrets(errors);
    validateAdminPassword(errors);
    validateDatabase(errors);
    validateRedis(errors);
    validateCors(errors);
    validateCookies(errors);
    validateRateLimitPolicy(errors);
    validatePaymentSimulation(errors);
    return errors;
}
```

### Câu hỏi phỏng vấn liên quan

Aggregate error reporting có ưu điểm gì so với fail-on-first khi validate configuration?

### Câu trả lời ngắn gọn

Fail-on-first buộc operator phải fix từng lỗi một qua nhiều lần deploy. Aggregate cho phép fix tất cả trong một lần, tiết kiệm thời gian deployment đáng kể. Phương thức `validate()` trả về `List<String>` cũng dễ dàng unit test từng rule riêng lẻ mà không cần bắt exception.

---

## 142. Profile-Based Configuration Separation (`@Profile`)

### Giải thích ngắn gọn

Spring `@Profile` annotation giới hạn một bean chỉ được đăng ký vào ApplicationContext khi profile tương ứng đang active. Đây là cơ chế tách biệt cấu hình development và production, đảm bảo các ràng buộc nghiêm ngặt của production không gây phiền hà cho developer khi phát triển local.

**Nguyên tắc:** Development profile cho phép mọi thứ hoạt động nhanh (localhost, H2, JWT ngắn). Production profile bắt buộc mọi thứ phải an toàn. Hai thế giới này không bao giờ được trộn lẫn.

### Ví dụ trong project này

`ProductionSecurityValidator` đánh dấu `@Profile("prod")`. Khi developer chạy `mvn spring-boot:run` mà không set profile (mặc định là default profile), bean này hoàn toàn không tồn tại — developer thoải mái dùng `jwt.secret: myDevSecret`, `spring.datasource.url: jdbc:h2:mem:testdb` mà không bị chặn.

Khi deploy lên server với `SPRING_PROFILES_ACTIVE=prod`, bean được kích hoạt và fail-fast validation chạy.

### Câu hỏi phỏng vấn liên quan

Làm sao đảm bảo production security rules không ảnh hưởng developer workflow?

### Câu trả lời ngắn gọn

Đặt security validator bean dưới `@Profile("prod")` để nó chỉ tồn tại khi production profile active. Development chạy với default hoặc dev profile, không load bean. Cấu hình chia thành `application.yml` (development defaults) và `application-prod.yml` (chỉ environment variable, không fallback cho secrets).

---

## 143. Placeholder & Weak-Secret Detection bằng Regex + Blacklist

### Giải thích ngắn gọn

Placeholder detection là kỹ thuật phát hiện các giá trị cấu hình chưa được thay thế từ template (ví dụ `CHANGE_ME`, `YOUR_STRONG_PASSWORD_HERE`). Weak-secret detection là phát hiện các mật khẩu phổ biến quá đơn giản (`password`, `admin`, `123456`). Kết hợp cả hai tạo thành lớp bảo vệ ngăn production deploy với credential không an toàn.

**Hai chiến lược bổ trợ:**
- **Regex pattern:** Bắt các biến thể placeholder (case-insensitive) dù có suffix khác nhau (`CHANGE_ME_NOW`, `YOUR_STRONG_DB_PASSWORD_HERE_2026`).
- **Exact-match blacklist:** Bắt các mật khẩu yếu không chứa substring placeholder nhưng vẫn nguy hiểm (`password`, `root`, `admin`).

### Ví dụ trong project này

```java
private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(
    "(?i).*(CHANGE_ME|YOUR_STRONG_|SECRET_HERE|PASSWORD_HERE|PLACEHOLDER|GENERATE_WITH|DEFAULT_PASSWORD).*");

private static final Set<String> FORBIDDEN_WEAK_PASSWORDS = Set.of(
    "password", "admin", "root", "123456", "password@123", "admin_password", "db_password", "redis_password");
```

Mỗi validation method gọi `isPlaceholder(value)` (regex) và kiểm tra `FORBIDDEN_WEAK_PASSWORDS.contains(value.toLowerCase())` (blacklist) trước khi validate độ dài.

### Câu hỏi phỏng vấn liên quan

Tại sao cần cả regex lẫn blacklist thay vì chỉ dùng một?

### Câu trả lời ngắn gọn

Regex bắt các template placeholder với nhiều biến thể suffix/prefix, nhưng không bắt được `password` hay `admin` vì chúng không chứa substring `CHANGE_ME`. Blacklist bắt các weak password phổ biến nhưng không cover hết mọi biến thể placeholder. Hai chiến lược bổ trợ nhau để giảm false-negative.

---

## 144. Docker Port Binding Security — `127.0.0.1` vs `0.0.0.0`

### Giải thích ngắn gọn

Khi Docker Compose map port với cú pháp `ports: - "3306:3306"`, Docker mặc định bind lên `0.0.0.0` — tức tất cả network interfaces của host machine. Trên VPS public, điều này có nghĩa cổng database (3306) và Redis (6379) mở ra internet cho mọi IP kết nối trực tiếp, hoàn toàn bypass ứng dụng backend.

Thay bằng `127.0.0.1:3306:3306` giới hạn kết nối chỉ từ localhost trên host machine. Các container trong cùng Docker network vẫn giao tiếp qua tên service (ví dụ `jdbc:mariadb://db:3306/...`) mà không cần port mapping ra host.

**Defense-in-depth:** Port binding + password authentication + firewall rules. Không có lớp nào đứng một mình; tất cả cùng bảo vệ.

### Ví dụ trong project này

`docker-compose.prod.yml` bind database và Redis chỉ ra localhost:

```yaml
services:
  db:
    ports:
      - "127.0.0.1:3306:3306"  # Chỉ host machine truy cập được
  redis:
    ports:
      - "127.0.0.1:6379:6379"  # Không public ra internet
    command: redis-server --requirepass "${REDIS_PASSWORD}"
```

Backend container kết nối qua Docker internal network: `DB_URL=jdbc:mariadb://db:3306/...`, `REDIS_HOST=redis`. Không đi qua port mapping của host.

### Câu hỏi phỏng vấn liên quan

Docker port mapping `0.0.0.0` có bypass firewall (iptables/ufw) không?

### Câu trả lời ngắn gọn

Có. Docker tự thêm iptables rules cho port mapping, có thể bypass ufw rules đã cấu hình. Bind `127.0.0.1` ngăn Docker tạo rule mở public. Với production, nên audit Docker iptables rules hoặc dùng `--iptables=false` kết hợp firewall thủ công.

---

## 145. Rate Limiter Failure Policy — FAIL_CLOSED vs FAIL_OPEN

### Giải thích ngắn gọn

Failure policy quyết định hành vi của rate limiter khi backend storage (Redis) không khả dụng. Đây là quyết định kiến trúc phản ánh sự đánh đổi giữa **bảo mật** (security) và **khả dụng** (availability) — một trong những trade-off cơ bản nhất trong thiết kế hệ thống.

- **FAIL_CLOSED:** Redis down → chặn tất cả request → bảo mật tối đa, nhưng có thể gây outage toàn bộ authentication nếu Redis offline lâu.
- **FAIL_OPEN:** Redis down → cho tất cả request đi qua → ưu tiên availability, nhưng rate limiting tạm thời bị vô hiệu, cho phép brute-force attack trong thời gian Redis offline.

### Ví dụ trong project này

`application-prod.yml` mặc định `app.rate-limit.redis-failure-policy: FAIL_CLOSED`. `ProductionSecurityValidator` kiểm tra giá trị phải thuộc `{FAIL_CLOSED, FAIL_OPEN}`, reject bất kỳ giá trị nào khác.

Quyết định chọn `FAIL_CLOSED` cho authentication endpoints vì nếu Redis down mà rate limiting bị tắt, kẻ tấn công có thể brute-force hàng triệu password combinations mỗi giây mà không bị chặn.

### Câu hỏi phỏng vấn liên quan

Trong CAP theorem, FAIL_CLOSED và FAIL_OPEN phản ánh sự đánh đổi nào?

### Câu trả lời ngắn gọn

FAIL_CLOSED ưu tiên Consistency/Safety (đảm bảo rate limit luôn được áp dụng) nhưng hy sinh Availability. FAIL_OPEN ưu tiên Availability nhưng hy sinh invariant rate limit. Với authentication, FAIL_CLOSED an toàn hơn vì hậu quả của brute-force thành công (account takeover) nghiêm trọng hơn downtime tạm thời.

---

## 146. Hibernate `ddl-auto` Strategies & Production Safety

### Giải thích ngắn gọn

`spring.jpa.hibernate.ddl-auto` kiểm soát cách Hibernate xử lý schema database so với JPA Entity mapping:

| Giá trị | Hành vi | Phù hợp với |
|---------|---------|-------------|
| `create` | Xóa toàn bộ schema, tạo lại từ đầu | Test chạy xong bỏ |
| `create-drop` | Tạo khi start, xóa khi shutdown | Test ngắn hạn |
| `update` | So sánh entity với schema, thêm cột/bảng thiếu (không xóa cột cũ) | Development nhanh |
| `validate` | Chỉ kiểm tra schema khớp entity, không sửa đổi gì | **Production** |
| `none` | Không làm gì | Khi dùng tool khác (Flyway) |

**Nguy hiểm của `update` trong production:** Hibernate không bao giờ xóa cột hoặc index đã tồn tại, dẫn đến schema drift tích lũy. Nó cũng không thể rename column, split table, hay thực hiện data migration phức tạp. Và quan trọng nhất: không có cách rollback.

### Ví dụ trong project này

`application-prod.yml` set `hibernate.ddl-auto: validate`. Nếu Entity thêm field mới mà chưa có Flyway migration tạo cột tương ứng, application fail-fast ngay tại startup thay vì chạy rồi crash khi query trúng cột thiếu.

`application.yml` (development) dùng `ddl-auto: update` để developer nhanh chóng thêm field mà không cần viết migration cho mỗi lần thay đổi nhỏ.

### Câu hỏi phỏng vấn liên quan

Tại sao `ddl-auto: update` nguy hiểm trong production?

### Câu trả lời ngắn gọn

`update` không xóa cột cũ nên schema drift tích lũy theo thời gian. Nó không hỗ trợ rollback, rename, hay data migration phức tạp. Với production, mọi schema change phải đi qua Flyway versioned migration để có audit trail, checksum verification, và khả năng review/rollback.

---

## 147. Secret Masking trong Error Logging

### Giải thích ngắn gọn

Secret masking là kỹ thuật chỉ hiển thị một phần nhỏ của giá trị bí mật (đủ để nhận diện) khi ghi log hoặc hiển thị trong error message, ngăn việc lộ toàn bộ secret ra log file, monitoring system, hay terminal output.

**Quy tắc:** Log file thường lưu trữ lâu dài, được backup, và có thể nhiều người có quyền đọc. Nếu log chứa toàn bộ JWT secret, bất kỳ ai có quyền đọc log đều có thể giả mạo token.

### Ví dụ trong project này

```java
private String maskSecret(String secret) {
    if (secret == null) return "null";
    if (secret.length() <= 8) return "***";
    return secret.substring(0, 4) + "..." + secret.substring(secret.length() - 4);
}
```

Error message hiển thị: `[JWT] 'jwt.secret.access' contains forbidden placeholder: 'CHAN...SE64'` thay vì hiện toàn bộ `CHANGE_ME_GENERATE_WITH_OPENSSL_RAND_BASE64_32`.

### Câu hỏi phỏng vấn liên quan

Tại sao không nên log toàn bộ giá trị secret khi validation thất bại?

### Câu trả lời ngắn gọn

Log files được lưu trữ, backup, và có thể nhiều người đọc. Nếu secret xuất hiện đầy đủ trong log, mọi người có quyền đọc log (developer, ops, monitoring tool) đều biết secret. Masking chỉ hiện đủ để nhận diện mà không lộ giá trị hoàn chỉnh.

---

## 148. Server-Authoritative Quiz Evaluation & Partial-Credit Scoring

### Giải thích ngắn gọn

Server-Authoritative Evaluation là nguyên tắc trong đó toàn bộ logic tính điểm, kiểm tra đúng/sai, cộng trừ điểm thành phần, và quyết định kết quả bài thi được thực hiện 100% trên server. Client chỉ đóng vai trò gửi lên danh sách lựa chọn của người dùng (`answerId`, `answerIds`, `userAnswerText`).

Đối với câu hỏi trắc nghiệm nhiều lựa chọn (`MULTIPLE_CHOICE`), thuật toán **Partial Credit with Penalty** giúp chấm điểm công bằng cho thí sinh chọn đúng một phần, đồng thời ngăn chặn tuyệt đối hành vi đoán mò (chọn tất cả các ô):

$$\text{ratio} = \max\left(0, \frac{C_{\text{selected}} - W_{\text{selected}}}{C_{\text{total}}}\right)$$
$$\text{pointsEarned} = \text{round}(\text{ratio} \times \text{maxPoints}, 2)$$

- $C_{\text{selected}}$: Số đáp án đúng mà thí sinh đã chọn.
- $W_{\text{selected}}$: Số đáp án sai mà thí sinh đã chọn (penalty).
- $C_{\text{total}}$: Tổng số đáp án đúng của câu hỏi ($C_{\text{total}} \ge 1$).
- Thí sinh chọn đúng toàn bộ và không chọn đáp án sai nào mới đạt full credit (`isCorrect = true`).

### Ví dụ trong project này

Trong [QuizLearningServiceImpl.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_quiz/service/QuizLearningServiceImpl.java):

```java
long correctSelected = selectedIds.stream().filter(correctIds::contains).count();
long wrongSelected = selectedIds.stream().filter(wrongIds::contains).count();

if (correctSelected == correctIds.size() && wrongSelected == 0) {
    isCorrect = true;
    pointsEarned = maxPoints;
} else if (correctSelected > 0) {
    double ratio = Math.max(0.0, (double) (correctSelected - wrongSelected) / (double) correctIds.size());
    pointsEarned = maxPoints.multiply(BigDecimal.valueOf(ratio))
            .setScale(2, RoundingMode.HALF_UP);
    isCorrect = pointsEarned.compareTo(maxPoints) == 0;
}
```

### Câu hỏi phỏng vấn liên quan

Tại sao cần phạt điểm khi thí sinh chọn đáp án sai trong câu hỏi MULTIPLE_CHOICE?

### Câu trả lời ngắn gọn

Nếu chỉ tính điểm theo tỉ lệ đáp án đúng mà không phạt đáp án sai, thí sinh có thể gian lận bằng cách tích chọn tất cả các phương án để luôn đạt 100% điểm. Cơ chế trừ điểm đáp án sai triệt tiêu điểm số đoán mò, đảm bảo chỉ những thí sinh thực sự hiểu bài mới được điểm.

---

## 149. Duplicate Payload Attack Vector & In-Memory Deduplication Guard (`Set.add()`)

### Giải thích ngắn gọn

Duplicate Payload Attack Vector xảy ra khi một client độc hại gửi một danh sách chứa các phần tử trùng lặp khóa định danh (ví dụ nhiều phần tử cùng chứa `questionId = 50`) trong cùng một HTTP request body. 

Nếu backend xử lý theo dạng stream/for-loop ngây thơ hoặc dùng `Collectors.toMap(..., (a, b) -> b)`:
1. Đối tượng sau sẽ âm thầm ghi đè đối tượng trước, làm sai lệch trạng thái câu trả lời đã lưu.
2. Hoặc nếu có tích lũy điểm trong vòng lặp, kẻ tấn công có thể gửi lặp lại câu hỏi đã trả lời đúng hàng trăm lần để nhân số điểm nhận được lên gấp bội.

Cơ chế phòng thủ tối ưu là sử dụng cấu trúc dữ liệu `Set<Long>` và kiểm tra kết quả boolean của `set.add(key)`. Nếu phần tử đã tồn tại, `set.add()` trả về `false` với độ phức tạp $O(1)$, cho phép ném ngoại lệ `INVALID_REQUEST` ngay lập tức để từ chối toàn bộ request.

### Ví dụ trong project này

Trong [QuizLearningServiceImpl.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_quiz/service/QuizLearningServiceImpl.java):

```java
Set<Long> seenQuestionIds = new HashSet<>();
for (QuizSubmitAnswerReq ansReq : req.getAnswers()) {
    if (ansReq.getQuestionId() == null) {
        throw new AppException(ErrorCode.INVALID_REQUEST, "questionId không được để trống");
    }
    if (!quizQuestionIds.contains(ansReq.getQuestionId())) {
        throw new AppException(ErrorCode.INVALID_REQUEST,
                "Câu hỏi không thuộc bài kiểm tra này: " + ansReq.getQuestionId());
    }
    if (!seenQuestionIds.add(ansReq.getQuestionId())) {
        throw new AppException(ErrorCode.INVALID_REQUEST,
                "Dữ liệu nộp bài chứa câu hỏi trùng lặp: " + ansReq.getQuestionId());
    }
}
```

### Câu hỏi phỏng vấn liên quan

Tại sao nên kiểm tra trùng lặp bằng `Set.add()` thay vì `List.contains()`?

### Câu trả lời ngắn gọn

`List.contains()` có độ phức tạp thời gian là $O(n)$, nếu kiểm tra trong vòng lặp duyệt $n$ phần tử thì tổng độ phức tạp sẽ là $O(n^2)$. Ngược lại, `HashSet.add()` có độ phức tạp trung bình là $O(1)$, giúp kiểm tra toàn bộ danh sách trong thời gian tuyến tính $O(n)$ mà không gây nghẽn CPU khi payload lớn.

---

## 150. Jackson SetterlessProperty Gotcha with Java Unmodifiable Collections

### Giải thích ngắn gọn

Trong thư viện Jackson (JSON ObjectMapper của Spring Boot), khi deserialize JSON thành Java Object, Jackson tìm kiếm cả Setter lẫn Getter. Nếu một class có một getter trả về Collection (ví dụ `public List<Long> getEffectiveAnswerIds()`) nhưng **không có setter** tương ứng, Jackson kích hoạt tính năng **SetterlessProperty**:
1. Jackson gọi getter để lấy đối tượng collection hiện tại (`getEffectiveAnswerIds()`).
2. Jackson cố gắng gọi phương thức `.add(...)` trên collection vừa lấy được để nhét các phần tử từ JSON vào.

**Cạm bẫy nghiêm trọng:** Nếu getter trả về một unmodifiable collection (ví dụ `List.of(...)` trong Java 9+ thuộc lớp `ImmutableCollections`), phương thức `.add()` sẽ ném ra `UnsupportedOperationException`. Ngoại lệ này bị wrap thành `JsonMappingException` và trả về mã lỗi HTTP 500 (Internal Server Error) cho client.

**Quy tắc:** Mọi helper method trong DTO có tiền tố `get...` hoặc `is...` mà không đại diện cho một JSON field thực sự cần nhận dữ liệu từ client đều **bắt buộc phải được đánh dấu `@JsonIgnore`**.

### Ví dụ trong project này

Trong [QuizSubmitAnswerReq.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_quiz/dto/QuizSubmitAnswerReq.java):

```java
/**
 * Resolves effective selected answer IDs from either answerIds or answerId.
 */
@com.fasterxml.jackson.annotation.JsonIgnore
public List<Long> getEffectiveAnswerIds() {
    if (answerIds != null && !answerIds.isEmpty()) {
        return answerIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
    if (answerId != null) {
        return List.of(answerId);
    }
    return List.of();
}
```

### Câu hỏi phỏng vấn liên quan

Làm thế nào để tránh Jackson tự động serialize/deserialize các helper getter method trong DTO?

### Câu trả lời ngắn gọn

Đánh dấu `@JsonIgnore` trên method đó, hoặc đổi tên method không bắt đầu bằng tiền tố JavaBean (`get`/`is`), ví dụ `resolveEffectiveAnswerIds()`. Đánh dấu `@JsonIgnore` là giải pháp chuẩn mực và tường minh nhất trong Spring Boot.

---

## 151. Security Defense Persistence & Transaction Rollback Gotcha (`noRollbackFor`)

### Giải thích ngắn gọn

Trong Spring Boot, khi một phương thức được đánh dấu `@Transactional`, bất kỳ ngoại lệ nào kế thừa từ `RuntimeException` (ví dụ `AppException`) bị ném ra khỏi phương thức sẽ kích hoạt cơ chế tự động **Rollback** toàn bộ transaction về trạng thái ban đầu trước khi bắt đầu phương thức.

**Lỗ hổng bảo mật tiềm ẩn (Security Defense Rollback):**
Trong kịch bản phát hiện tấn công tái sử dụng refresh token cũ (Token Reuse / Replay Attack), nghiệp vụ yêu cầu server phải ngay lập tức **thu hồi toàn bộ phiên hoạt động** của người dùng đó (`revokeAllByUserId`) và ném ra ngoại lệ `AppException(ErrorCode.REFRESH_TOKEN_REVOKED)`.
Nếu method ném `AppException` trong transaction mặc định:
1. Spring rollback toàn bộ transaction!
2. Câu lệnh `UPDATE refresh_tokens SET revoked = true WHERE user_id = :userId` vừa thực thi bị hủy bỏ!
3. Phiên hoạt động của nạn nhân hoặc kẻ tấn công **vẫn tiếp tục tồn tại hợp lệ trong database**!

**Giải pháp:**
Sử dụng `@Transactional(noRollbackFor = AppException.class)` trên phương thức phòng thủ hoặc tách tác vụ thu hồi phiên sang một transaction độc lập (`@Transactional(propagation = Propagation.REQUIRES_NEW)`). Khi đó, các câu lệnh thu hồi token sẽ được `COMMIT` bền vững vào CSDL bất chấp việc phương thức kết thúc bằng ngoại lệ lỗi HTTP 401.

### Ví dụ trong project này

Trong [AuthServiceImpl.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_auth/service/AuthServiceImpl.java):

```java
@Override
@Transactional(noRollbackFor = AppException.class)
public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
    ...
    // Token Reuse Detection: thu hồi ngay lập tức và commit xuống DB
    if (Boolean.TRUE.equals(refreshTokenEntity.getRevoked())) {
        log.warn("Token reuse detected! Revoking all tokens for user ID: {}", refreshTokenEntity.getUser().getId());
        revokeUserSessions(refreshTokenEntity.getUser().getId());
        throw new AppException(ErrorCode.REFRESH_TOKEN_REVOKED);
    }
    ...
}
```

### Câu hỏi phỏng vấn liên quan

Tại sao cần cấu hình `noRollbackFor` khi ném ngoại lệ trong logic phát hiện tấn công token reuse?

### Câu trả lời ngắn gọn

Mặc định Spring sẽ rollback toàn bộ transaction khi có `RuntimeException`. Nếu không dùng `noRollbackFor`, lệnh thu hồi token family sẽ bị rollback và vô hiệu hóa, khiến các phiên của người dùng vẫn hoạt động sau cuộc tấn công.

---

## 152. RFC 7519 `jti` Claim & Unique Token Hash at Rest

### Giải thích ngắn gọn

Khi lưu trữ refresh token dưới dạng chuỗi băm an toàn (ví dụ SHA-256 hex digest) với ràng buộc duy nhất (`UNIQUE`), nếu hai token được cấp cho cùng một user trong cùng một millisecond (ví dụ khi user đăng nhập nhanh trên 2 tab hoặc chạy kiểm thử song song), các claims mặc định (`id`, `sub`, `iat`, `exp`) hoàn toàn giống nhau byte-for-byte. Kết quả là 2 chuỗi JWT giống hệt nhau và sinh ra SHA-256 hash giống hệt nhau, gây lỗi vi phạm khóa duy nhất `Unique index or primary key violation` trong cơ sở dữ liệu.

Theo tiêu chuẩn **RFC 7519 Section 4.1.7**, claim `jti` (JWT ID) cung cấp một mã định danh duy nhất cho JWT:
- Luôn bổ sung `"jti": UUID.randomUUID().toString()` vào claims khi tạo token.
- Đảm bảo tính duy nhất tuyệt đối (cryptographically unique) cho từng token kể cả khi được tạo cùng một thời điểm cho cùng một chủ thể.
- Ngăn chặn hoàn toàn việc đụng độ token hash trong CSDL.

### Ví dụ trong project này

Trong [JwtUtil.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_auth/util/JwtUtil.java):

```java
private String buildToken(Map<String, Object> extraClaims, String subject, long expiration, SecretKey key) {
    Map<String, Object> allClaims = new HashMap<>();
    if (extraClaims != null) {
        allClaims.putAll(extraClaims);
    }
    allClaims.putIfAbsent("jti", java.util.UUID.randomUUID().toString());

    return Jwts.builder()
            .claims(allClaims)
            .subject(subject)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(key)
            .compact();
}
```

### Câu hỏi phỏng vấn liên quan

Claim `jti` trong JWT là gì và mang lại lợi ích gì cho hệ thống lưu trữ token dạng hash?

### Câu trả lời ngắn gọn

`jti` (JWT ID) là định danh duy nhất ngẫu nhiên (UUID) cho từng token. Nó giúp tránh va chạm chuỗi JWT khi sinh cùng millisecond, đảm bảo mỗi token luôn sinh ra một SHA-256 hash độc nhất, không bị lỗi trùng khóa unique trong database.

---

## 153. Repeatable Pruning Query & Active Session Protection

### Giải thích ngắn gọn

Trong kiến trúc bảo mật session dạng token xoay vòng (rotating refresh tokens), các bản ghi trong bảng `refresh_tokens` sẽ tăng dần theo thời gian (token cũ bị revoke, token hết hạn). Một tiến trình dọn dẹp định kỳ (scheduled cleanup) cần đáp ứng 3 tiêu chí:
1. **Bảo vệ tuyệt đối phiên hoạt động (Active Session Protection):** Không bao giờ xóa hoặc thu hồi nhầm các token đang active (`revoked = false AND expiredAt >= now`).
2. **Tính lặp lại và lũy thừa (Repeatable / Idempotent):** Quét nhiều lần liên tiếp không gây lỗi, không làm xáo trộn dữ liệu hiện có; đợt quét thứ hai trả về 0 bản ghi xóa.
3. **Hiệu năng cao nhờ Index:** Các câu lệnh bulk delete phải được hỗ trợ bởi chỉ mục trên `expired_at` và `revoked` để tránh table lock trên bảng lớn.

### Ví dụ trong project này

Trong [RefreshTokenRepository.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_auth/repository/RefreshTokenRepository.java):

```java
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("DELETE FROM RefreshToken r WHERE r.expiredAt < :now OR r.revoked = true")
int deleteExpiredOrRevoked(@Param("now") LocalDateTime now);
```

- Điều kiện `expiredAt < now OR revoked = true` loại trừ hoàn toàn các token có `revoked = false AND expiredAt >= now`.
- Đi kèm chỉ mục `idx_refresh_tokens_expired_at` và `idx_refresh_tokens_revoked` trong migration `V10`.

### Câu hỏi phỏng vấn liên quan

Làm thế nào để đảm bảo job scheduled cleanup xóa token rác mà không xóa nhầm phiên đăng nhập của người dùng đang online?

### Câu trả lời ngắn gọn

Phân tách ranh giới rõ ràng trong câu query: chỉ xóa các token thỏa mãn `expiredAt < now` hoặc `revoked = true`. Các session hợp lệ đang hoạt động luôn có `revoked = false` và `expiredAt >= now`, do đó không bao giờ rơi vào tập điều kiện xóa.

---

## 154. Production Structured Logging & High-Performance Log Masking

### Giải thích ngắn gọn

Trong môi trường phân tán (Distributed / Microservices / Cloud-native), log dạng văn bản tự do (plain text) gây khó khăn rất lớn cho việc thu thập, phân tích cú pháp (parsing) và tìm kiếm tự động trên ELK (Elasticsearch/Logstash/Kibana), Loki hay CloudWatch.

**Structured Logging** chuyển đổi mọi dòng log thành một đối tượng JSON có schema chuẩn:
- Trường thời gian `@timestamp` ISO-8601.
- Mức độ `level` (INFO, WARN, ERROR).
- Tên logger `logger_name`, luồng xử lý `thread_name`.
- Metadata ngữ cảnh: `correlationId`, `requestMethod`, `requestUri`, `clientIp`, `service`, `environment`.

Tuy nhiên, việc in log ra JSON tiềm ẩn nguy cơ **rò rỉ dữ liệu nhạy cảm (PII & Credentials Leakage)** như mật khẩu, access token, cookie hoặc JWT. Kỹ thuật **Log Masking** giải quyết bài toán này bằng cách:
1. Xác định tập các trường nhạy cảm (`password`, `refreshToken`, `token`, `Bearer`, `Cookie`).
2. **Fast-path Optimization:** Kiểm tra sơ bộ bằng `indexOf` trước khi áp dụng Regex để tránh ngốn CPU (ReDoS và CPU spikes khi log tải lớn).
3. Thay thế giá trị nhạy cảm bằng chuỗi `***REDACTED***`.

### Ví dụ trong project này

Trong [LogMaskingConverter.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/common/logging/LogMaskingConverter.java):
```java
public static String maskSensitiveData(String message) {
    if (message == null || message.isEmpty()) return message;
    if (!quickCheck(message)) return message; // Fast-path: 95% log thường không cần regex

    String masked = message;
    for (Pattern pattern : SENSITIVE_PATTERNS) {
        masked = pattern.matcher(masked).replaceAll(REPLACEMENT);
    }
    return masked;
}
```

Và cấu hình [logback-spring.xml](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/resources/logback-spring.xml) tích hợp `LogstashEncoder` cùng `MaskedMessageProvider` cho môi trường production.

### Câu hỏi phỏng vấn liên quan

Tại sao việc che mờ log (Log Masking) bằng Regex có thể làm giảm hiệu năng hệ thống và cách khắc phục trong thiết kế là gì?

### Câu trả lời ngắn gọn

Áp dụng nhiều regex phức tạp trên mọi chuỗi log có thể gây ra CPU spike và nghẽn luồng I/O. Khắc phục bằng cách áp dụng **Fast-path check** (kiểm tra nhanh chuỗi con bằng `indexOf` với chi phí $O(N)$), chỉ khi log có chứa từ khóa nhạy cảm (`password`, `token`, `Bearer`) mới chạy qua pipeline Regex.

---

## 155. Distributed Request Tracing with MDC & Correlation ID Filter Lifecycle

### Giải thích ngắn gọn

Khi một hệ thống phục vụ hàng ngàn request đồng thời hoặc giao tiếp qua nhiều dịch vụ, việc theo dõi xem một chuỗi log thuộc về request hay người dùng nào trở thành thách thức lớn nếu chỉ dựa vào thread name.

**Correlation ID (Trace ID)** là một mã định danh duy nhất (thường là UUID v4) được gắn vào request ngay từ khi bước vào gateway hoặc controller:
1. **Tiếp nhận / Khởi tạo:** Đọc từ HTTP header `X-Correlation-ID`. Nếu client chưa gửi, bộ lọc sẽ tự sinh một UUID mới.
2. **Ngữ cảnh luồng (MDC - Mapped Diagnostic Context):** Lưu trữ Correlation ID vào `ThreadLocal` của SLF4J MDC. Nhờ đó, bất kỳ câu lệnh `log.info()`, `log.error()` nào trong toàn bộ tầng logic (Service, Repository, Helper) đều tự động mang theo Correlation ID mà không cần truyền biến thủ công qua từng tham số method.
3. **Phản hồi Client:** Trả lại header `X-Correlation-ID` trong HTTP response để người dùng hoặc frontend gửi mã này khi báo lỗi kỹ thuật (Ticket / Support).
4. **Bắt buộc dọn dẹp (ThreadLocal Cleanup):** Vì Servlet Container (Tomcat) tái sử dụng worker thread từ ThreadPool, bắt buộc phải gọi `MDC.clear()` trong khối `finally` của filter để ngăn ngừa rò rỉ dữ liệu sang request tiếp theo.

### Ví dụ trong project này

Trong [CorrelationIdFilter.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/common/logging/CorrelationIdFilter.java):
```java
try {
    String correlationId = request.getHeader("X-Correlation-ID");
    if (correlationId == null || correlationId.isBlank()) {
        correlationId = UUID.randomUUID().toString();
    }
    MDC.put("correlationId", correlationId);
    MDC.put("clientIp", extractClientIp(request));
    response.setHeader("X-Correlation-ID", correlationId);

    filterChain.doFilter(request, response);
} finally {
    MDC.clear(); // Ngăn ngừa ThreadLocal leak khi thread quay lại pool
}
```

### Câu hỏi phỏng vấn liên quan

Điều gì sẽ xảy ra nếu lập trình viên quên gọi `MDC.clear()` trong một ứng dụng Spring Boot chạy trên Tomcat?

### Câu trả lời ngắn gọn

Tomcat tái sử dụng các worker thread trong pool. Nếu không gọi `MDC.clear()`, Correlation ID và Client IP của request trước sẽ tồn tại trong `ThreadLocal`, khiến log của các request sau (có thể từ user khác) bị dán nhầm Correlation ID cũ, làm sai lệch hoàn toàn dữ liệu điều tra sự cố (audit & tracing).

---

## 156. Kubernetes Probes Architecture: Liveness vs Readiness & Component Health Aggregation

### Giải thích ngắn gọn

Trong kiến trúc containerized orchestration (Kubernetes / Docker Swarm), bộ điều phối cần phân biệt rõ giữa việc **tiến trình còn sống** và việc **ứng dụng đã sẵn sàng xử lý dữ liệu**:

1. **Liveness Probe (`/actuator/health/liveness`):**
   - **Mục đích:** Kiểm tra tiến trình JVM có bị treo cứng, deadlock hay rò rỉ bộ nhớ nghiêm trọng hay không.
   - **Hành động khi fail:** Kubernetes sẽ **KILL & RESTART** container.
   - **Quy tắc vàng:** Chỉ kiểm tra trạng thái nội bộ của container (`livenessState`). **Tuyệt đối KHÔNG** đưa CSDL bên ngoài hay Redis vào liveness probe, vì nếu CSDL quá tải chậm phản hồi, liveness fail sẽ khiến toàn bộ cluster pod bị restart đồng loạt, dẫn đến sập toàn bộ hệ thống (cascading failure).
2. **Readiness Probe (`/actuator/health/readiness`):**
   - **Mục đích:** Kiểm tra ứng dụng và các tài nguyên phụ thuộc (Database connection pool, Redis cache) có sẵn sàng nhận traffic người dùng hay không.
   - **Hành động khi fail:** Kubernetes sẽ **NGẮT ĐIỀU PHỐI TRAFFIC** (gỡ pod khỏi danh sách Service endpoints) nhưng **KHÔNG RESTART** pod. Khi database hoặc redis hồi phục, readiness probe pass trở lại và traffic tự động được định tuyến tới pod.
3. **Information Disclosure Prevention:**
   - Public health check endpoint (`/actuator/health`) tại production phải luôn cấu hình `show-details: never`. Nó chỉ trả về `{"status":"UP"}` mà không để lộ chi tiết cấu hình database, dung lượng ổ đĩa hay phiên bản Redis cho kẻ tấn công bên ngoài.

### Ví dụ trong project này

Trong [application-prod.yml](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/resources/application-prod.yml):
```yaml
management:
  endpoint:
    health:
      show-details: never # Chỉ trả về {"status":"UP"}
      probes:
        enabled: true
      group:
        liveness:
          include: livenessState
        readiness:
          include: readinessState,db,redisCustomHealthIndicator
```

### Câu hỏi phỏng vấn liên quan

Tại sao việc đưa kiểm tra kết nối CSDL (Database Ping) vào Kubernetes Liveness Probe là một sai lầm chết người trong kiến trúc Production?

### Câu trả lời ngắn gọn

Khi database bị nghẽn tải hoặc bảo trì ngắn, database ping sẽ timeout làm Liveness probe fail. Kubernetes sẽ restart hàng loạt container cùng lúc. Khi các pod khởi động lại đồng loạt, chúng sẽ ồ ạt mở lại connection pool vào database, làm database càng sụp đổ nặng hơn (thảm họa thundering herd & cascading restart loop).

---

## 157. Metric Instrumentation with Micrometer & Dimensional Tagging for Prometheus

### Giải thích ngắn gọn

Giám sát thụ động qua file log chỉ cho biết những gì đã xảy ra trong quá khứ. Giám sát chủ động theo thời gian thực (Real-time Observability) đòi hỏi ứng dụng phải phát ra các **Metrics (Chỉ số)** có thể tổng hợp và cảnh báo:

**Micrometer** đóng vai trò là "SLF4J dành cho Metrics", cung cấp giao diện trừu tượng để ứng dụng gửi chỉ số tới Prometheus, Datadog hay InfluxDB:
- **Counter:** Đo lường các sự kiện tăng dần đều (monotonically increasing): số lần đăng nhập sai, số lần chạm rate limit, số đơn hàng thành công.
- **Timer / Histogram:** Đo lường độ trễ và phân phối thời gian (latency percentiles: P50, P95, P99).
- **Gauge:** Đo lường giá trị có thể tăng hoặc giảm tại một thời điểm: số kết nối database đang active, dung lượng heap memory đang dùng.

**Dimensional Tagging (Gắn nhãn theo chiều đo):**
Thay vì tạo ra hàng loạt metric tĩnh với tên khác nhau (ví dụ: `auth_fail_wrong_pass`, `auth_fail_locked_acc`), Prometheus khuyến nghị sử dụng **một metric duy nhất kèm các nhãn (tags)**:
- Tên metric: `app_auth_failures_total`
- Tag: `reason="invalid_credentials"`, `reason="account_locked"`, `reason="expired_token"`
Cách thiết kế này cho phép truy vấn PromQL linh hoạt:
- Tổng số lỗi auth: `sum(rate(app_auth_failures_total[5m]))`
- Lỗi do dò pass: `rate(app_auth_failures_total{reason="invalid_credentials"}[5m])`

### Ví dụ trong project này

Trong [ObservabilityMetrics.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/common/observability/ObservabilityMetrics.java):
```java
Counter authFailureInvalidCredentials = Counter.builder("app.auth.failures")
        .description("Authentication failures")
        .tag("reason", "invalid_credentials")
        .register(meterRegistry);

public void recordQuizSubmission(Long quizId, boolean passed) {
    Counter.builder("app.quiz.submissions")
            .tag("quiz_id", String.valueOf(quizId))
            .tag("passed", String.valueOf(passed))
            .register(meterRegistry)
            .increment();
}
```

### Câu hỏi phỏng vấn liên quan

### Câu trả lời ngắn gọn

Counter chỉ tăng (reset về 0 khi app restart), dùng để đếm sự kiện (request, lỗi auth, nộp bài). Gauge theo dõi giá trị tức thời biến thiên hai chiều (số kết nối DB, CPU, RAM). Trong Prometheus, luôn áp dụng hàm `rate()` hoặc `increase()` trên Counter, còn Gauge được đọc trực tiếp giá trị.

---

## 158. Non-blocking Consistent Snapshot with InnoDB MVCC (`--single-transaction`)

### Giải thích ngắn gọn

Trong môi trường Production có người dùng truy cập liên tục 24/7, việc sao lưu cơ sở dữ liệu nếu sử dụng cờ `--lock-all-tables` (khóa toàn bộ bảng) sẽ chặn đứng mọi câu lệnh `INSERT`, `UPDATE`, `DELETE` của học viên (ví dụ nộp bài quiz, tiến độ học, tạo đơn hàng thanh toán), gây ra downtime nghiêm trọng.

Engine **InnoDB** của MariaDB/MySQL giải quyết bài toán này nhờ kiến trúc **MVCC (Multi-Version Concurrency Control)** và cờ `--single-transaction`:
1. Khi bắt đầu dump, client gửi lệnh `START TRANSACTION WITH CONSISTENT SNAPSHOT` với transaction isolation level là `REPEATABLE READ`.
2. InnoDB tạo một snapshot nhất quán tại thời điểm giao dịch bắt đầu bằng cách sử dụng Undo Logs.
3. Trong suốt quá trình dump, các câu lệnh `SELECT` của `mariadb-dump` chỉ đọc dữ liệu từ snapshot đó.
4. Các ứng dụng khác vẫn tự do thực hiện ghi/sửa dữ liệu vào bảng mà không hề bị khóa (lock-free).
5. Kết hợp với `--quick` (đọc từng hàng thay vì tải cả bảng vào RAM) và `--hex-blob` (tránh lỗi ký tự nhị phân), hệ thống đạt được bản sao lưu nhất quán 100% với chi phí tài nguyên tối thiểu.

### Ví dụ trong project này

Trong [scripts/backup/backup.sh](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/scripts/backup/backup.sh):
```bash
DUMP_FLAGS=(
    "--single-transaction"
    "--quick"
    "--routines"
    "--triggers"
    "--events"
    "--default-character-set=utf8mb4"
    "--hex-blob"
    "--no-autocommit"
)
${DUMP_CMD} -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${DB_PASSWORD}" "${DUMP_FLAGS[@]}" "${DB_NAME}"
```

### Câu hỏi phỏng vấn liên quan

Tại sao `--single-transaction` chỉ đảm bảo tính nhất quán cho các bảng InnoDB mà không áp dụng được cho MyISAM?

### Câu trả lời ngắn gọn

Vì MyISAM không hỗ trợ giao dịch (transactions) và không có kiến trúc MVCC / Undo logs. Nếu dump bảng MyISAM với `--single-transaction`, các thao tác ghi đồng thời vẫn làm sai lệch dữ liệu; MyISAM bắt buộc phải dùng `--lock-tables`. Do đó, toàn bộ bảng nghiệp vụ production bắt buộc phải dùng engine InnoDB.

---

## 159. Ephemeral Cache vs Persistent State: The Redis Zero-Backup Strategy

### Giải thích ngắn gọn

Trong thiết kế kiến trúc hệ thống, nguyên tắc **Phân định Trạng thái (State Classification)** phân tách tài nguyên thành hai nhóm:
1. **Trạng thái Cốt lõi (Persistent State):** Dữ liệu không thể tái tạo nếu bị mất (tài khoản người dùng, đơn hàng, thanh toán, tiến độ học, token gia đình). Nhóm này **bắt buộc** phải được sao lưu định kỳ, mã hóa và lưu trữ ngoại biên (MariaDB).
2. **Dữ liệu Tạm thời (Ephemeral State / Volatile Cache):** Dữ liệu phái sinh, có thời gian sống ngắn (TTL), hoặc có thể tự động tính toán lại mà không ảnh hưởng tới tính toàn vẹn nghiệp vụ (bộ đếm rate limit, cache danh mục).

Trong Japanese Learning Platform:
- Redis chỉ được sử dụng cho `RedisRateLimiterService` với cấu trúc ZSET trượt cửa sổ **15 phút** và `RedisHealthIndicator` cho readiness probe.
- Toàn bộ phiên đăng nhập, JWT stateless và Refresh Token đều nằm trong bảng MariaDB `refresh_tokens`.
- **Zero-Backup Strategy:** Dữ liệu rate limit 15 phút không mang giá trị lịch sử và việc sao lưu định kỳ là hoàn toàn lãng phí tài nguyên. Nếu Redis bị restart hoặc sập, bộ đếm reset về 0 và tiếp tục phục vụ cửa sổ 15 phút tiếp theo mà không gây gián đoạn phiên người dùng hay thất thoát doanh thu.

### Ví dụ trong project này

Tại [docker-compose.prod.yml](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/docker-compose.prod.yml), Redis chỉ gắn volume `redis_data_prod:/data` để phục vụ khởi động lại nhanh cục bộ, nhưng hoàn toàn được miễn trừ khỏi pipeline sao lưu ngoại biên trong [DATABASE_BACKUP_RECOVERY.md](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/docs/DATABASE_BACKUP_RECOVERY.md).

### Câu hỏi phỏng vấn liên quan

Khi nào Redis cần được cấu hình RDB/AOF snapshot và backup định kỳ? Khi nào có thể áp dụng chiến lược Zero-Backup?

### Câu trả lời ngắn gọn

Nếu Redis được sử dụng làm Primary Data Store (ví dụ: lưu Session người dùng, Job Queue Celery/BullMQ, hay Shopping Cart chưa thanh toán), Redis bắt buộc phải được backup bền vững. Nếu Redis chỉ đóng vai trò L2 Cache hoặc Rate Limiter tạm thời có thể tái tạo tức thì, ta áp dụng Zero-Backup để tối ưu chi phí hạ tầng.

---

## 160. Cryptographic Checksum Integrity & Tamper Detection in Backup Pipelines

### Giải thích ngắn gọn

Một nguy cơ lớn trong việc lưu trữ sao lưu lâu dài là **Bit Rot (suy thoái dữ liệu âm thầm)** và **Tampering (bị mã độc tống tiền hoặc kẻ tấn công can thiệp sửa đổi)**. Nếu khôi phục một file sao lưu đã bị biến đổi cấu trúc, cơ sở dữ liệu có thể rơi vào trạng thái corrupt không thể cứu vãn.

Quy trình bảo vệ tính toàn vẹn đa lớp (Integrity Multi-layering):
1. **Mã băm SHA-256 độc lập:** Mỗi file sao lưu đã mã hóa `.sql.gz.enc` luôn được tính toán mã băm SHA-256 ngay sau khi xuất xưởng và lưu vào file companion `.sha256`.
2. **Pre-flight Integrity Verification:** Trước khi bất kỳ thao tác giải mã hay nạp dữ liệu nào được thực hiện, script `restore.sh` và `verify-restore.sh` bắt buộc tính lại mã SHA-256 của file hiện tại và so sánh với mã gốc. Nếu có sai lệch dù chỉ 1 bit (1 byte flipped), tiến trình lập tức dừng với mã lỗi `INTEGRITY_VIOLATION`.
3. **Mã hóa AES-256-CBC PBKDF2:** Khóa dẫn xuất qua 100,000 vòng lặp kèm Salt ngẫu nhiên ngăn ngừa hoàn toàn các cuộc tấn công Rainbow Table và bảo vệ bí mật dữ liệu khi lưu trữ tại S3 / Cloud Storage (Encryption at Rest).

### Ví dụ trong project này

Trong [scripts/backup/restore.sh](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/scripts/backup/restore.sh):
```bash
EXPECTED_HASH=$(awk '{print $1}' "${SHA_FILE}")
ACTUAL_HASH=$(sha256sum "${BACKUP_FILE}" | awk '{print $1}')
if [[ "${EXPECTED_HASH}" != "${ACTUAL_HASH}" ]]; then
    echo "[ERROR] CHECKSUM MISMATCH! Backup file is corrupted or tampered with." >&2
    exit 2
fi
```

### Câu hỏi phỏng vấn liên quan

Tại sao nên lưu mã băm SHA-256 của file đã mã hóa (`.enc.sha256`) thay vì lưu mã băm của file SQL gốc chưa mã hóa?

### Câu trả lời ngắn gọn

Bởi vì việc kiểm tra mã băm của file mã hóa có thể thực hiện ngay lập tức (Pre-flight check) trước khi giải mã. Nếu file bị hỏng hoặc bị giả mạo trên đường truyền/S3, hệ thống từ chối ngay mà không cần tốn tài nguyên CPU để giải mã một file rác, đồng thời tránh kích hoạt các lỗ hổng parser khi giải mã dữ liệu độc hại.

---

## 161. Isolated Restore Verification & Ephemeral Container Teardown

### Giải thích ngắn gọn

Nguyên tắc vàng của SRE: **"Một bản sao lưu chưa từng được khôi phục thử nghiệm thì không được coi là một bản sao lưu an toàn."**

Tuy nhiên, việc khôi phục thử nghiệm trực tiếp trên máy chủ Production là điều tối kỵ vì có thể gây gián đoạn dịch vụ hoặc ghi đè dữ liệu đang hoạt động. Giải pháp chuẩn là **Kiểm tra Phục hồi Cách ly (Isolated Verification)**:
1. **Container Tạm thời (Ephemeral Container):** Sử dụng Docker API để khởi tạo một container MariaDB độc lập kèm một Docker Network và Volume tạm thời ngẫu nhiên (`verify_$(date +%s)_$$`).
2. **Nạp & Thực thi Stream:** Giải mã và nạp bản backup vào container tạm này.
3. **Kiểm tra Tính toàn vẹn Ngữ nghĩa (Semantic Sanity Checks):**
   - Kiểm tra `flyway_schema_history` có đầy đủ các migration thành công hay không.
   - Kiểm tra sự hiện diện của đủ 13 bảng cốt lõi của ứng dụng.
   - Kiểm tra đếm dòng (`COUNT(*)`) trên các bảng quan trọng (`users`, `courses`, `lessons`).
   - Kiểm tra hiển thị đúng bảng mã UTF-8 tiếng Nhật (Kanji/Kana).
4. **Tự động Dọn dẹp Sạch sẽ (Auto Teardown):** Lệnh `trap cleanup EXIT` đảm bảo container và volume thử nghiệm bị tiêu hủy hoàn toàn, không để lại rác tài nguyên trên máy chủ.

### Ví dụ trong project này

Trong [scripts/backup/verify-restore.sh](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/scripts/backup/verify-restore.sh):
```bash
docker run -d --name "${VERIFY_CONTAINER}" --network "${VERIFY_NET}" \
    -e MARIADB_ROOT_PASSWORD="${TEMP_PASS}" -e MARIADB_DATABASE="japanese_learning_db" mariadb:10.11

# Stream giải mã trực tiếp vào container tạm
openssl enc -d -aes-256-cbc -pbkdf2 -iter 100000 "${OPENSSL_PASS_ARG[@]}" -in "${BACKUP_FILE}" \
    | gzip -d \
    | docker exec -i "${VERIFY_CONTAINER}" mariadb -uroot -p"${TEMP_PASS}" japanese_learning_db
```

### Câu hỏi phỏng vấn liên quan

Tại sao việc kiểm tra cú pháp file dump bằng lệnh `grep` hay `head/tail` là chưa đủ để khẳng định bản backup hợp lệ?

### Câu trả lời ngắn gọn

Kiểm tra chuỗi chỉ xác nhận file có header/footer hợp lệ, nhưng không phát hiện được các lỗi ngữ nghĩa nghiêm trọng như: vi phạm khóa ngoại (foreign key constraint failure), kiểu dữ liệu không tương thích giữa các phiên bản MariaDB, hoặc dữ liệu bị lỗi encoding UTF-8. Chỉ có việc nạp thực tế vào một instance database độc lập mới chứng minh được bản backup sẵn sàng hoạt động 100%.

---

## 162. Flyway Migration Checksum Mismatch & Schema Repair Mechanics

### Giải thích ngắn gọn

Flyway là công cụ quản lý tiến hóa cấu trúc cơ sở dữ liệu (Database Schema Migration). Để đảm bảo tính bất biến (Immutability), Flyway tính toán mã băm **CRC32** cho mỗi file SQL migration (ví dụ `V8__hash_refresh_tokens.sql`) và lưu vào cột `checksum` của bảng `flyway_schema_history`.

**Nguyên nhân Checksum Mismatch:**
Khi một file migration đã từng được áp dụng vào database của lập trình viên, nếu sau đó file SQL đó bị sửa đổi nội dung (thêm comment, sửa câu lệnh, hoặc git revert giữa các branch), mã băm nội dung của file local sẽ thay đổi. Khi ứng dụng khởi động lại, Flyway so sánh checksum của file local với checksum trong CSDL; nếu khác nhau, Flyway lập tức ném ngoại lệ `FlywayValidateException` và dừng khởi động ứng dụng (Fail-fast).

**Cơ chế Sửa chữa (Repair Mechanics):**
1. **Lệnh `flyway:repair`:** Flyway cung cấp lệnh repair để căn chỉnh lại bảng `flyway_schema_history`, cập nhật lại checksum của các migration đã áp dụng cho khớp với file mã nguồn hiện tại.
2. **Cập nhật thủ công trong môi trường Dev:** Trong trường hợp không tích hợp Flyway CLI, lập trình viên có thể cập nhật trực tiếp checksum mới vào bảng:
   `UPDATE flyway_schema_history SET checksum = :newChecksum WHERE version = :version;`
3. **Nguyên tắc Production:** Tuyệt đối không bao giờ sửa đổi các file migration đã merge vào nhánh chính (`main`/`develop`) trên môi trường Production. Mọi thay đổi cấu trúc đều phải được thực hiện bằng một migration phiên bản mới (ví dụ `V11__...sql`).

### Ví dụ trong project này

Khi file `V8__hash_refresh_tokens.sql` bị lệch checksum giữa `802283801` (bản có `IF NOT EXISTS`) và `1094426290` (bản 24 dòng), bảng `flyway_schema_history` trên local dev được sửa chữa:
```sql
UPDATE japanese_learning_db.flyway_schema_history 
SET checksum = 1094426290 
WHERE version = '8';
```

### Câu hỏi phỏng vấn liên quan

Tại sao Flyway mặc định bật tính năng Validate Checksum và nghiêm cấm sửa đổi các migration đã chạy trong quá khứ?

### Câu trả lời ngắn gọn

Để bảo đảm tính tất định (determinism) của cơ sở dữ liệu. Nếu một file migration cũ bị thay đổi, các môi trường (Staging, Production, Dev của thành viên khác) sẽ có cấu trúc CSDL không đồng nhất, dẫn đến những lỗi dữ liệu tiềm ẩn cực kỳ khó debug. Thay vào đó, Flyway yêu cầu luôn tiến về phía trước (Forward-only migrations).

---

## 163. Least-Privilege Database User Architecture in Production

### Giải thích ngắn gọn

Nguyên tắc Phân quyền Tối thiểu (Principle of Least Privilege - PoLP) quy định rằng một đối tượng (user/tiến trình) chỉ được cấp chính xác các quyền hạn cần thiết để hoàn thành nhiệm vụ của nó, không thừa một quyền nào.

Trong kiến trúc CSDL Production:
- **Tài khoản Root/DBA (`root`):** Có toàn quyền tối cao trên toàn bộ máy chủ (`ALL PRIVILEGES ON *.* WITH GRANT OPTION`). Tài khoản này có thể drop database, shutdown server, đọc ghi file hệ điều hành (`LOAD_FILE`, `INTO OUTFILE`), và xem mọi schema hệ thống (`mysql`, `information_schema`).
- **Tài khoản Ứng dụng (`japanese_learning_user`):** Chỉ được cấp quyền trong phạm vi database của ứng dụng (`japanese_learning_db.*`):
  1. **Quyền DML (Data Manipulation Language):** `SELECT`, `INSERT`, `UPDATE`, `DELETE` để JPA/Hibernate thực hiện CRUD dữ liệu học viên, khóa học, đơn hàng.
  2. **Quyền DDL (Data Definition Language):** `CREATE`, `ALTER`, `INDEX`, `DROP`, `REFERENCES` để Flyway Migration tự động cập nhật bảng khi deploy phiên bản mới.
  3. **Không có quyền:** `SUPER`, `PROCESS`, `RELOAD`, `SHUTDOWN`, `FILE`, `GRANT OPTION`.

### Ví dụ trong project này

1. Cấp quyền trong MariaDB cho application user:
```sql
CREATE USER IF NOT EXISTS 'japanese_learning_user'@'%' IDENTIFIED BY 'YOUR_STRONG_PASSWORD';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES 
ON japanese_learning_db.* TO 'japanese_learning_user'@'%';
FLUSH PRIVILEGES;
```
2. Bộ kiểm tra `ProductionSecurityValidator.java` từ chối khởi động nếu phát hiện backend dùng tài khoản root:
```java
if ("root".equalsIgnoreCase(datasourceUsername.trim())) {
    errors.add("[Database] 'spring.datasource.username' must not be 'root' in production. Use a dedicated least-privilege application user.");
}
```

### Câu hỏi phỏng vấn liên quan

Tại sao không nên để ứng dụng web dùng tài khoản root kết nối CSDL, kể cả khi database chỉ nằm trong mạng nội bộ Docker?

### Câu trả lời ngắn gọn

Nếu ứng dụng gặp lỗ hổng SQL Injection hoặc thư viện bên thứ 3 bị dính Remote Code Execution (RCE), kẻ tấn công với quyền root có thể chiếm toàn quyền kiểm soát database server, đọc ghi file nhạy cảm trên host (như `/etc/passwd`), cài webshell, hoặc xóa sạch toàn bộ các CSDL khác trên cùng instance. Tài khoản phân quyền tối thiểu sẽ giới hạn phạm vi thiệt hại nghiêm ngặt trong một database duy nhất.

---

## 164. Container Non-Root Security (Container Breakout Mitigation)

### Giải thích ngắn gọn

Mặc định khi build Dockerfile, nếu không khai báo chỉ thị `USER`, tiến trình bên trong container sẽ chạy dưới tài khoản `root` (UID 0). Do container sử dụng chung nhân (Linux Kernel) với máy chủ host, UID 0 bên trong container ánh xạ trực tiếp tới UID 0 trên host (trừ khi bật User Namespaces).

**Rủi ro của Root Container:**
1. **Container Breakout:** Nếu kẻ tấn công khai thác được một lỗ hổng trong runtime (runc, containerd, kernel exploit) hoặc container mount nhầm Docker socket (`/var/run/docker.sock`), kẻ tấn công sẽ lập tức có toàn quyền root trên máy chủ vật lý bên ngoài.
2. **File Permission Tampering:** Tiến trình root có thể vô tình hoặc cố ý ghi đè các tệp nhạy cảm của hệ thống.

**Giải pháp Non-Root:**
Tạo một user và group chuyên biệt với UID/GID cố định không đặc quyền (ví dụ UID 10001), phân quyền sở hữu file jar cho user này, và kích hoạt `USER appuser:appgroup`.

### Ví dụ trong project này

Trong `backend/Dockerfile`:
```dockerfile
# Tạo non-root user và group (UID/GID 10001)
RUN apk add --no-cache curl && \
    addgroup -g 10001 -S appgroup && \
    adduser -u 10001 -S appuser -G appgroup

# Phân quyền tệp jar cho appuser
COPY --chown=appuser:appgroup ${JAR_FILE} app.jar

# Chuyển ngữ cảnh thực thi sang non-root
USER appuser:appgroup

# Tiến trình Java chạy dưới quyền UID 10001
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
```

### Câu hỏi phỏng vấn liên quan

Làm thế nào để kiểm tra một Docker container đang chạy trên server có thực sự an toàn dưới quyền non-root hay không?

### Câu trả lời ngắn gọn

Sử dụng lệnh `docker inspect --format '{{.Config.User}}' <container_name>` để xem user được khai báo trong image, hoặc dùng lệnh `docker top <container_name>` trên host để xem cột UID của tiến trình đang chạy. Nếu UID khác 0 (ví dụ 10001 hoặc `appuser`), tiến trình đang chạy an toàn ở chế độ non-root.

---

## 165. Network Exposure Reduction & Reverse Proxy Binding Strategy

### Giải thích ngắn gọn

Trong kiến trúc triển khai microservices/web app nhiều tầng:
1. **Giảm thiểu bề mặt tấn công (Attack Surface Reduction):** Những dịch vụ thuần túy nội bộ như Database (MariaDB 3306) và Caching (Redis 6379) tuyệt đối không được publish cổng ra ngoài host vật lý (không dùng `ports: - "3306:3306"`). Thay vào đó, chúng chỉ cần kết nối qua mạng nội bộ Docker (`networks: - app-network-prod`) và dùng chỉ thị `expose: - "3306"` để backend trong cùng mạng truy cập được qua tên miền nội bộ `db:3306`.
2. **Chiến lược Ràng buộc Localhost (Localhost Binding Strategy):**
   - Nếu bind cổng backend ở dạng `ports: - "8080:8080"`, Docker mặc định sẽ lắng nghe trên mọi network interface (`0.0.0.0:8080`). Bất kỳ ai trên Internet đều có thể gửi request trực tiếp tới port 8080 qua HTTP plain-text, bỏ qua hoàn toàn tầng SSL/TLS, bộ lọc WAF và Rate Limiting của Nginx/Caddy.
   - Khi cấu hình `ports: - "127.0.0.1:8080:8080"`, chỉ có các tiến trình cục bộ trên cùng máy chủ (chính là Reverse Proxy Nginx/Caddy) mới có thể chuyển tiếp request vào backend. Toàn bộ người dùng ngoài Internet bắt buộc phải đi qua Reverse Proxy trên cổng 443 (HTTPS).

### Ví dụ trong project này

Trong `docker-compose.prod.yml`:
```yaml
services:
  db:
    expose:
      - "3306" # Không mở cổng ra host, chỉ nội bộ app-network-prod
      
  redis:
    expose:
      - "6379" # Không mở cổng ra host
      
  backend:
    ports:
      - "${BACKEND_BIND_IP:-127.0.0.1}:8080:8080" # Chỉ bind localhost
```

### Câu hỏi phỏng vấn liên quan

Tại sao việc bỏ publish port 3306 của MariaDB ra host lại bảo vệ hệ thống tốt hơn việc chỉ đặt mật khẩu root thật mạnh?

### Câu trả lời ngắn gọn

Theo nguyên lý Phòng thủ theo chiều sâu (Defense-in-Depth): Mật khẩu mạnh chỉ là một lớp bảo vệ. Nếu mở cổng 3306 ra ngoài, database sẽ liên tục bị botnet quét cổng, tấn công brute-force mật khẩu làm nghẽn CPU/băng thông, hoặc bị khai thác các lỗ hổng zero-day trong giao thức MariaDB handshake trước cả khi xác thực. Khi không publish port ra host, kẻ tấn công hoàn toàn không có cách nào gửi gói tin TCP tới cổng 3306 từ Internet.

---

## 166. Container Resource Limits: Limits vs Reservations in Production Orchestration

### Giải thích ngắn gọn

Khi chạy nhiều container trên cùng một máy chủ VPS mà không đặt giới hạn tài nguyên, một container bị rò rỉ bộ nhớ (Memory Leak) hoặc bị vòng lặp vô tận (Infinite Loop) có thể ngốn 100% CPU và RAM của cả máy chủ, khiến hệ điều hành kích hoạt Linux Kernel OOM Killer tiêu diệt ngẫu nhiên các tiến trình quan trọng khác (bao gồm cả SSH daemon hoặc database).

Docker Compose hỗ trợ khai báo tài nguyên theo chuẩn Compose Specification:
- **`reservations` (Mức bảo đảm / Requests):** Mức tài nguyên tối thiểu mà máy chủ cam kết dành riêng cho container. Nếu máy chủ không đủ dung lượng này, container sẽ không được cấp phát khởi động.
- **`limits` (Trần tối đa / Limits):** Mức tài nguyên tối đa mà container được phép sử dụng:
  - **CPU Limit:** Sử dụng cgroups CFS (Completely Fair Scheduler) quota để giới hạn chu kỳ CPU. Nếu container vượt quá limit, CPU sẽ bị điều tiết (throttling) chậm lại chứ không bị kill.
  - **Memory Limit:** Sử dụng cgroups memory limit. Nếu tiến trình bên trong container vượt quá giới hạn RAM này, kernel sẽ gửi tín hiệu `SIGKILL` tiêu diệt tiến trình (OOMKilled) để bảo vệ toàn bộ phần còn lại của hệ thống.

### Ví dụ trong project này

Trong `docker-compose.prod.yml`:
```yaml
backend:
  deploy:
    resources:
      limits:
        cpus: '1.5'
        memory: 1024M
      reservations:
        cpus: '0.5'
        memory: 512M

db:
  deploy:
    resources:
      limits:
        cpus: '1.0'
        memory: 512M
      reservations:
        cpus: '0.25'
        memory: 256M
```

### Câu hỏi phỏng vấn liên quan

Java 21 trong container nhận diện giới hạn bộ nhớ (Memory Limit) của Docker như thế nào để tính toán kích thước Heap (`-Xmx`)?

### Câu trả lời ngắn gọn

Từ Java 10 trở đi và hoàn thiện ở Java 21, JVM mặc định kích hoạt cờ `-XX:+UseContainerSupport`. JVM tự động đọc các file cgroups của Linux (`/sys/fs/cgroup/memory.max` trên cgroups v2) để biết trần RAM của container thay vì đọc tổng RAM của máy chủ host. Từ đó, JVM tự động thiết lập kích thước Max Heap mặc định (khoảng 25% đến 50% memory limit của container) mà không cần cấu hình cờ `-Xmx` thủ công.

---

## 167. Secret Scanning & Allowlist Strategy (Gitleaks in CI/CD Pipelines)

### Giải thích ngắn gọn

Trong quy trình phát triển phần mềm, việc vô tình đẩy (commit/push) thông tin nhạy cảm (API keys, Private Keys, Database Passwords, JWT Secrets) lên Git repository là một trong những nguyên nhân hàng đầu dẫn đến vi phạm bảo mật dữ liệu. Một khi secret đã vào git history, việc xóa bằng `git commit` mới là hoàn toàn vô hiệu vì git tree vẫn lưu giữ commit cũ.

**Gitleaks** là công cụ phân tích tĩnh chuyên dụng phân tích cú pháp regex và entropy để phát hiện các secret bị rò rỉ:
1. **Quét tự động trong CI:** Chạy trên mỗi Pull Request và commit vào nhánh chính. Nếu phát hiện rò rỉ, pipeline lập tức đánh fail để chặn code không được merge.
2. **Chiến lược Allowlist an toàn (`.gitleaks.toml`):**
   - Trong quá trình viết unit test và integration test, lập trình viên bắt buộc phải dùng các chuỗi giả lập (mock secret, test tokens).
   - Nếu không có cấu hình allowlist, scanner sẽ liên tục báo động giả (False Positive) và chặn pipeline.
   - File `.gitleaks.toml` cho phép cấu hình loại trừ cụ thể theo đường dẫn file kiểm thử (`backend/src/test/**`, `*.spec.ts`, `docs/**`) hoặc theo giá trị cụ thể, trong khi vẫn áp dụng 100% quy tắc rà soát cho mã nguồn production.
3. **Log Redaction:** Luôn chạy gitleaks với cờ `--redact` để nếu phát hiện chuỗi secret vi phạm, công cụ sẽ ẩn đi các ký tự nhạy cảm, tránh việc màn hình CI log công khai lại trở thành nguồn rò rỉ secret.

### Ví dụ trong project này

Trong `.gitleaks.toml`:
```toml
title = "Japanese Learning Platform Gitleaks Configuration"

[extend]
useDefault = true

[allowlist]
description = "Ignore mock secrets and fixture credentials in tests, docs, and templates"
paths = [
  '''(?i)backend/src/test/.*''',
  '''(?i)frontend/src/.*\.spec\.(js|ts)''',
  '''(?i)docs/.*''',
  '''(?i)\.env\..*\.example$'''
]
```

Trong `.github/workflows/ci.yml`:
```yaml
- name: Run Gitleaks Secret Scan
  uses: gitleaks/gitleaks-action@v2
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
    GITLEAKS_CONFIG: .gitleaks.toml
    GITLEAKS_ARGS: --verbose --redact
```

### Câu hỏi phỏng vấn liên quan

Tại sao cần chạy Secret Scanning cả ở local (Pre-commit Hook) và trên GitHub Actions CI Runner?

### Câu trả lời ngắn gọn

Theo nguyên tắc Defense-in-Depth: Pre-commit hook chạy trên máy lập trình viên giúp ngăn chặn secret ngay trước khi tạo commit (Shift Left Security). Tuy nhiên, pre-commit hook có thể bị lập trình viên bỏ qua bằng cờ `--no-verify`, hoặc máy trạm chưa cài đặt công cụ. Do đó, CI runner đóng vai trò người gác cổng tối cao không thể bị can thiệp (Authoritative Gatekeeper), đảm bảo 100% commit đẩy lên kho lưu trữ chung đều phải trải qua kiểm tra tự động.

---

## 168. Software Composition Analysis (SCA) & CVE Exception Management (.trivyignore with Compensating Controls)

### Giải thích ngắn gọn

Khoảng 80-90% mã nguồn trong một ứng dụng doanh nghiệp hiện đại thực chất là các thư viện nguồn mở bên thứ ba (Third-party dependencies). **Software Composition Analysis (SCA)** là quá trình tự động kiểm tra danh mục các thư viện phụ thuộc (pom.xml, package.json, Docker Base Images) đối chiếu với các cơ sở dữ liệu lỗ hổng bảo mật toàn cầu (NVD, GitHub Advisory Database) để tìm ra các mã **CVE (Common Vulnerabilities and Exposures)**.

**Quản lý ngoại lệ với Cơ chế bù đắp (Compensating Controls):**
- Trong thực tế, khi một CVE mới được công bố trên một thư viện bắc cầu (transitive dependency) như Tomcat hay Netty, Spring Boot có thể chưa phát hành bản vá ngay lập tức.
- Nếu pipeline CI cấu hình chặn cứng mọi CVE, toàn bộ quy trình release sản phẩm sẽ bị tê liệt hoàn toàn (Build Block).
- Kỹ sư không được phép tắt scanner hay hạ mức severity xuống thấp. Giải pháp chuẩn mực doanh nghiệp là lập **Tệp phê duyệt ngoại lệ (`.trivyignore`)**:
  - Ghi rõ mã CVE, ngày phê duyệt, và tài liệu phân tích chi tiết.
  - Trình bày **Biện pháp Kiểm soát Bù đắp (Compensating Control)** chứng minh rằng luồng thực thi của ứng dụng không kích hoạt đoạn code bị lỗi (Unreachable vulnerable path), ví dụ: Tomcat bị lỗi Form Auth nhưng hệ thống dùng Stateless JWT, hoặc Netty bị lỗi TLS nhưng chỉ giao tiếp Redis nội bộ.

### Ví dụ trong project này

Trong `.trivyignore`:
```text
# CVE-2026-43512: Apache Tomcat Digest Authentication timing attack
# Approved: 2026-10-28
# Compensating Control: System uses stateless JWT authentication via Spring Security FilterChain.
# Tomcat's built-in Digest Authenticator is never invoked or configured.
CVE-2026-43512

# CVE-2026-75595: Netty SNI hostname validation bypass
# Approved: 2026-10-28
# Compensating Control: Netty is used strictly internally by Lettuce Redis client.
# No public-facing TLS listeners terminate on Netty; public TLS is terminated at Nginx.
CVE-2026-75595
```

### Câu hỏi phỏng vấn liên quan

Tại sao việc phân biệt giữa Direct Dependency và Transitive Dependency lại quan trọng trong việc khắc phục lỗ hổng SCA?

### Câu trả lời ngắn gọn

- **Direct Dependency:** Là thư viện ứng dụng trực tiếp khai báo (ví dụ `spring-boot-starter-web`). Ta có thể sửa bằng cách nâng phiên bản trong `pom.xml`.
- **Transitive Dependency:** Là thư viện được kéo theo gián tiếp bởi thư viện cha (ví dụ `tomcat-embed-core`). Việc tự ý ghi đè phiên bản transitive dependency (qua `<dependencyManagement>`) có thể phá vỡ tính tương thích nhị phân (Binary Incompatibility) và gây lỗi runtime (`NoSuchMethodError`, `ClassNotFoundException`). Do đó cần đánh giá kỹ lưỡng và chỉ áp dụng exception khi có biện pháp bù đắp an toàn.

---

## 169. GitHub Actions Service Containers for Real Database Integration Tests (MariaDB vs H2 InMemory)

### Giải thích ngắn gọn

Trong kiểm thử phần mềm, có sự đánh đổi lớn giữa **Tốc độ (Speed)** và **Độ tin cậy thực tế (Fidelity)**:
- **H2 In-Memory Database:** Rất nhanh, khởi tạo trong tích tắc, thích hợp cho Unit Tests chạy liên tục. Nhưng H2 không tương thích 100% với MariaDB/MySQL về:
  - Cú pháp Migration nâng cao (DDL statements, character set collations).
  - Khả năng xử lý chuỗi Unicode 4-byte (`utf8mb4_unicode_ci`) lưu trữ emoji hoặc Hán tự hiếm.
  - Các hàm đặc thù của MariaDB (`JSON_EXTRACT`, `REGEXP_SUBSTR`) và cách đánh chỉ mục (Index storage engines).
- **GitHub Actions Service Containers:**
  - Cho phép GitHub Actions runner tự động khởi chạy một container Docker độc lập chạy song song với job kiểm thử (ví dụ: `image: mariadb:10.11`).
  - Cung cấp cơ sở dữ liệu thật với cấu hình production chính xác cho các bài kiểm thử tích hợp (Integration Tests).

**Chiến lược Test Isolation bằng Biến Môi Trường:**
- Bộ test tích hợp MariaDB thật (`RealMariaDbIntegrationIT`) cần truy cập container MariaDB đang chạy.
- Để bài test không làm sập quy trình build cục bộ của dev (khi chưa bật Docker container), sử dụng chú thích `@EnabledIfEnvironmentVariable(named = "RUN_MARIADB_IT", matches = "true")`.
- Trong pipeline CI, biến này được thiết lập `true`, kích hoạt kiểm thử toàn diện 10 bản Flyway migrations và encoding tiếng Nhật.

### Ví dụ trong project này

Trong `.github/workflows/ci.yml`:
```yaml
mariadb-integration:
  runs-on: ubuntu-latest
  services:
    mariadb:
      image: mariadb:10.11
      env:
        MARIADB_ROOT_PASSWORD: it_root_password
        MARIADB_DATABASE: japanese_learning_it_db
        MARIADB_USER: it_user
        MARIADB_PASSWORD: it_password
      ports:
        - 3306:3306
      options: --health-cmd="healthcheck.sh --connect --innodb_initialized" --health-interval=10s ...
  steps:
    - name: Run Real MariaDB Integration Tests
      env:
        RUN_MARIADB_IT: "true"
        DB_HOST: 127.0.0.1
        DB_PORT: "3306"
      run: mvn -B test -Dtest=RealMariaDbIntegrationIT
```

Trong Java test:
```java
@SpringBootTest
@ActiveProfiles("mariadb-it")
@EnabledIfEnvironmentVariable(named = "RUN_MARIADB_IT", matches = "true")
class RealMariaDbIntegrationIT {
    @Test
    void flywayMigrations_AreAllSuccessfullyAppliedOnMariaDb() { ... }
}
```

### Câu hỏi phỏng vấn liên quan

Testcontainers trong Java khác gì so với GitHub Actions Service Containers? Khi nào nên chọn giải pháp nào?

### Câu trả lời ngắn gọn

- **Testcontainers:** Thư viện Java tự động điều khiển Docker daemon từ bên trong mã test code (`@Testcontainers`). Ưu điểm: Lập trình viên chạy lệnh `mvn test` trên máy cá nhân sẽ tự động pull và bật container nếu máy có Docker, tính đóng gói cao. Nhược điểm: Cần quyền truy cập Docker socket (`/var/run/docker.sock`), có thể gây phức tạp trong môi trường container-in-container hoặc Podman không có root socket.
- **GitHub Actions Service Containers:** Được quản lý trực tiếp bởi hạ tầng GitHub Actions Workflow (`services:` block). Ưu điểm: Tận dụng cơ chế network bridge nguyên bản của CI, nhẹ hơn, không phụ thuộc vào Docker socket bên trong Java tiến trình, cực kỳ ổn định và tương thích tốt trên mọi runner Linux.

---

## 170. Automated API Smoke Testing & High-Traffic Load Gates (p95/p99 Latency SLAs & Concurrency Control)

### Giải thích ngắn gọn

Sau khi mã nguồn đã vượt qua Unit Test và Build artifact thành công, hệ thống vẫn có thể gặp lỗi khi chạy thực tế (ví dụ: xung đột cấu hình YAML, lỗi kết nối cơ sở dữ liệu thật, lỗi serialization Jackson DTO, hoặc sụt giảm hiệu năng nghiêm trọng do N+1 query).

1. **API Smoke Testing (Kiểm thử Khói Tự động):**
   - Chạy một tập hợp nhỏ các HTTP requests nhanh nhằm xác nhận các tính năng cốt lõi nhất của ứng dụng đang sống và hoạt động đúng trước khi tiến hành các bước kiểm thử sâu hơn.
   - Kiểm tra: Actuator Health (`/actuator/health`), Liveness/Readiness Probes, OpenAPI Documentation (`/api-docs`), Public Read Endpoints (`/api/v1/courses`), và cấu trúc JSON chuẩn của Error Envelope khi gửi sai thông tin đăng nhập (HTTP 401).
2. **Controlled Load Testing Gate (Cổng Kiểm soát Tải):**
   - Thay vì chờ đến khi triển khai lên production mới phát hiện endpoint bị nghẽn, pipeline CI kích hoạt một bài kiểm tra chịu tải nhỏ có kiểm soát (ví dụ: 10 concurrent clients, 200 requests) nhắm vào các read-endpoint tần suất cao.
   - **Quality Gate SLA Thresholds:**
     - Tỷ lệ lỗi (Error Rate): Bắt buộc `0.00%`.
     - Phân vị 95 (p95 Latency): `≤ 300ms`.
     - Phân vị 99 (p99 Latency): `≤ 600ms`.
   - Nếu bất kỳ chỉ số nào vượt ngưỡng, CI sẽ fail ngay lập tức, ngăn chặn các đoạn mã gây suy thoái hiệu năng (Performance Regression) đi vào nhánh chính.

### Ví dụ trong project này

Trong `scripts/ci/load_test.mjs`:
```javascript
const SLA_MAX_ERROR_RATE = 0.00;
const SLA_MAX_P95_MS = 300;
const SLA_MAX_P99_MS = 600;

const p95 = sortedLatencies[Math.floor(sortedLatencies.length * 0.95)];
const p99 = sortedLatencies[Math.floor(sortedLatencies.length * 0.99)];

if (errorRate > SLA_MAX_ERROR_RATE || p95 > SLA_MAX_P95_MS || p99 > SLA_MAX_P99_MS) {
  console.error("❌ SLA QUALITY GATE BREACHED");
  process.exit(1);
}
```

### Câu hỏi phỏng vấn liên quan

Tại sao trong kiểm thử hiệu năng API, độ trễ Percentile (p95, p99) lại có giá trị phát hiện lỗi cao hơn nhiều so với độ trễ Trung bình (Mean/Average Latency)?

### Câu trả lời ngắn gọn

Giá trị trung bình (Average) có xu hướng làm phẳng và che giấu các bất thường: Nếu 99 request phản hồi trong 10ms nhưng 1 request bị khóa database (Deadlock) mất 10,000ms, độ trễ trung bình chỉ là ~110ms (trông vẫn chấp nhận được). Tuy nhiên, đối với 1% người dùng gặp độ trễ 10s, đó là một trải nghiệm ứng dụng bị treo. Phân vị p95 và p99 phản ánh chính xác các điểm nghẽn nghiêm trọng nhất (Outliers) gây ra bởi GC pauses, Thread pool starvation, hoặc Database connection pool exhaustion.

---

## 171. JPA/Hibernate N+1 Query Problem & Batch Retrieval Architectures (@EntityGraph vs Batch IN Queries)

### Giải thích ngắn gọn

Vấn đề N+1 Query là một trong những nguyên nhân phổ biến nhất gây sụp đổ hiệu năng trong các ứng dụng sử dụng ORM (Hibernate/Spring Data JPA).

1. **Bản chất của N+1 Query:**
   - **Lazy Loading N+1:** Khi truy vấn danh sách $N$ đối tượng cha (ví dụ: $N$ bản ghi `QuizAttempt`), sau đó trong quá trình tuần tự hóa DTO hoặc logic xử lý, mã nguồn truy cập vào thực thể con lười (`a.getQuiz().getTitle()`). Hibernate sẽ phát sinh thêm $N$ câu truy vấn `SELECT * FROM quizzes WHERE id = ?` độc lập. Tổng số truy vấn gửi tới cơ sở dữ liệu là $1 + N$.
   - **Loop Repository N+1 ($1 + 3N$):** Lập trình viên truy vấn danh sách $N$ bài quiz của một bài học, sau đó dùng vòng lặp `for (Quiz q : quizzes)` để gọi các hàm repository đơn lẻ: `questionRepo.countByQuizId(q.getId())`, `attemptRepo.findLatest(...)`, `attemptRepo.count(...)`. Hệ thống phải thực thi $1 + 3N$ truy vấn mạng riêng biệt. Với một bài học có 5 quiz, backend thực thi tới 16 truy vấn SQL liên tiếp.

2. **Chiến lược xử lý triệt để:**
   - **`@EntityGraph`:** Khai báo danh sách các trường cần nạp sẵn (`attributePaths = {"quiz"}`) ngay trên phương thức Spring Data JPA. Hibernate tự động tạo câu lệnh SQL với `LEFT OUTER JOIN`, nạp toàn bộ thực thể liên kết chỉ trong **1 câu truy vấn duy nhất** mà vẫn giữ cấu trúc quan hệ JPA sạch sẽ.
   - **Batch Aggregations với Mệnh đề `IN`:** Với các nhu cầu thống kê (như đếm số câu hỏi, đếm số lượt thi), thay vì gọi đếm trong vòng lặp, ta viết các truy vấn gộp lô:
     ```sql
     SELECT q.quiz.id, COUNT(q) FROM Question q WHERE q.quiz.id IN :quizIds GROUP BY q.quiz.id
     ```
     Kết hợp với Java Stream API (`Collectors.toMap`), ta ánh xạ kết quả vào bộ nhớ (`Map<Long, Long>`), giảm số lượng round-trip từ $1 + 3N$ xuống còn **4 truy vấn cố định $O(1)$** trên toàn bộ request.

### Ví dụ trong project này

Trong `QuestionRepository.java` và `QuizLearningServiceImpl.java` (#382):
```java
// 1. Repository: Truy vấn gộp nhóm bằng IN clause
@Query("SELECT q.quiz.id, COUNT(q) FROM Question q WHERE q.quiz.id IN :quizIds GROUP BY q.quiz.id")
List<Object[]> countQuestionsByQuizIds(@Param("quizIds") Collection<Long> quizIds);

// 2. Service: Thực thi 4 truy vấn cố định O(1) thay vì 1 + 3N trong vòng lặp
List<Quiz> quizzes = quizRepository.findByLessonIdAndStatus(lessonId, QuizStatus.PUBLISHED);
List<Long> quizIds = quizzes.stream().map(Quiz::getId).toList();

// Tải hàng loạt số câu hỏi và attempts trong 3 batch queries
Map<Long, Long> questionCounts = questionRepository.countQuestionsByQuizIds(quizIds).stream()
    .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));

Map<Long, Long> attemptCounts = quizAttemptRepository.countAttemptsByUserIdAndQuizIds(user.getId(), quizIds).stream()
    .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));

Map<Long, QuizAttempt> latestAttempts = quizAttemptRepository.findByUserIdAndQuizIdInOrderByStartedAtDesc(user.getId(), quizIds).stream()
    .collect(Collectors.groupingBy(a -> a.getQuiz().getId(), Collectors.collectingAndThen(Collectors.toList(), list -> list.get(0))));
```

### Câu hỏi phỏng vấn liên quan

Sự khác biệt giữa `@EntityGraph` và `JOIN FETCH` trong JPQL là gì? Khi nào nên dùng giải pháp nào?

### Câu trả lời ngắn gọn

- **`JOIN FETCH`:** Được viết trực tiếp trong câu lệnh JPQL (ví dụ: `SELECT a FROM QuizAttempt a JOIN FETCH a.quiz`). Mặc định tạo phép `INNER JOIN`, ghi đè hoàn toàn chiến lược fetch type của entity và bắt buộc định nghĩa câu truy vấn tĩnh.
- **`@EntityGraph`:** Được Spring Data JPA cung cấp dưới dạng annotation đặt trên repository method (ví dụ: `@EntityGraph(attributePaths = {"quiz"})`). Hibernate tự động sinh `LEFT OUTER JOIN`, cho phép tái sử dụng các derived query methods có sẵn (`findByUserIdOrderByStartedAtDesc`) mà không cần viết lại toàn bộ câu lệnh JPQL, đồng thời hỗ trợ nạp nhiều nhánh quan hệ một cách linh hoạt.

---

## 172. Compound Indexing Architecture & Eliminating Database 'Using filesort' (MySQL/MariaDB B-Tree Optimization)

### Giải thích ngắn gọn

Trong MySQL/MariaDB, cây chỉ mục B-Tree lưu trữ các khóa theo thứ tự sắp xếp sẵn. Khi một câu lệnh truy vấn có cả mệnh đề lọc (`WHERE`) và mệnh đề sắp xếp (`ORDER BY`), kiến trúc chỉ mục quyết định trực tiếp việc database có thể đọc luồng dữ liệu ngay hay phải dừng lại để sắp xếp trên bộ nhớ đệm (`Using filesort`).

1. **Hiện tượng `Using filesort`:**
   - Giả sử bảng `quiz_attempts` có index trên cột `user_id`.
   - Khi chạy câu lệnh:
     ```sql
     SELECT * FROM quiz_attempts WHERE user_id = 10 ORDER BY started_at DESC;
     ```
   - Cơ sở dữ liệu dùng index `user_id` để lọc ra tất cả các bản ghi của user 10. Nhưng các bản ghi này không được sắp xếp theo `started_at`.
   - Động cơ InnoDB buộc phải đọc các dòng này vào một vùng nhớ tạm gọi là **`sort_buffer`** và chạy thuật toán QuickSort/MergeSort để sắp xếp lại kết quả trước khi trả về. Nếu tập dữ liệu lớn vượt quá `sort_buffer_size`, nó phải ghi các file tạm xuống ổ cứng (Disk I/O filesort), làm CPU tăng đột biến và độ trễ tăng vọt.

2. **Giải pháp Chỉ mục Hỗn hợp (Composite Index):**
   - Đánh chỉ mục hỗn hợp đa cột theo quy tắc Left-to-Right: Cột lọc trước, cột sắp xếp sau:
     ```sql
     CREATE INDEX idx_quiz_attempts_user_started ON quiz_attempts(user_id, started_at DESC);
     ```
   - Lúc này, với mỗi giá trị `user_id`, các nút con trong cây B-Tree đã được sắp xếp sẵn theo thứ tự giảm dần của `started_at`.
   - Cơ sở dữ liệu chỉ việc nhảy tới nhánh `user_id = 10` và duyệt tuần tự các nút từ trên xuống dưới (Index Range Scan), trả về kết quả ngay lập tức mà **hoàn toàn không cần filesort**.

### Ví dụ trong project này

Trong Flyway migration `V11__performance_indexes.sql`:
```sql
-- Khử filesort cho lịch sử làm bài kiểm tra học viên
CREATE INDEX idx_quiz_attempts_user_started ON quiz_attempts(user_id, started_at DESC);

-- Khử filesort khi tải cây giáo trình theo thứ tự chương
CREATE INDEX idx_course_sections_course_sort ON course_sections(course_id, sort_order);

-- Khử filesort cho dòng thời gian hoạt động học tập
CREATE INDEX idx_learning_activities_user_created ON learning_activities(user_id, created_at DESC);

-- Khử filesort cho lịch sử đơn hàng của học viên
CREATE INDEX idx_orders_user_created ON orders(user_id, created_at DESC);
```

### Câu hỏi phỏng vấn liên quan

Tại sao thứ tự các cột trong Composite Index (Chỉ mục kết hợp) lại mang tính quyết định sống còn đến hiệu quả của câu truy vấn?

### Câu trả lời ngắn gọn

Composite Index tuân thủ **Quy tắc Tiền tố Trái nhất (Leftmost Prefix Rule)**. Một chỉ mục trên `(A, B)` có thể phục vụ cho truy vấn `WHERE A = ?` và `WHERE A = ? ORDER BY B`, nhưng **hoàn toàn vô dụng** đối với truy vấn chỉ tìm kiếm theo `WHERE B = ?`. Do đó, nguyên tắc vàng khi thiết kế chỉ mục kết hợp là: Cột có độ chọn lọc cao (Equality Filters) đặt ở vị trí đầu tiên, tiếp theo là cột dùng để sắp xếp (`ORDER BY`) hoặc điều kiện dải (`Range Filters > < BETWEEN`).

---

## 173. HikariCP Connection Pool Sizing Methodology & Leak Detection Mechanics

### Giải thích ngắn gọn

Database Connection Pool quản lý tập hợp các kết nối JDBC được mở sẵn để ứng dụng tái sử dụng thay vì phải tốn chi phí đắt đỏ của việc bắt tay 3 bước TCP và xác thực bảo mật mỗi khi có request tới.

1. **Công thức Định cỡ Connection Pool (HikariCP / PostgreSQL Formula):**
   $$\text{pool\_size} = (\text{core\_count} \times 2) + \text{effective\_spindle\_count}$$
   - Trong môi trường container 2 vCPU gắn ổ cứng thể rắn SSD (NVMe), công thức cơ bản cho ra: $(2 \times 2) + 1 = 5$ kết nối đồng thời trên mỗi instance.
   - Để dự phòng đột biến lưu lượng (Spikes) cho hệ thống vừa đọc vừa làm bài kiểm tra (Write-burst), kích thước pool chuẩn cho mỗi API pod được định mức ở **15 - 20 kết nối**.
   - Cấu hình pool quá lớn (ví dụ 100 kết nối) không làm ứng dụng nhanh hơn mà gây nghẽn phần cứng (Hardware Throttling) do tranh chấp CPU Context Switch và nghẽn Disk I/O.

2. **Các Tham số Cốt lõi & Quản lý Vòng đời Kết nối:**
   - **`maximum-pool-size: 20`**: Số lượng kết nối tối đa có thể mở tới MariaDB.
   - **`minimum-idle: 10`**: Số kết nối nhàn rỗi luôn được duy trì sẵn sàng nhận việc.
   - **`connection-timeout: 20000ms` (20s)**: Thời gian tối đa một thread chờ lấy kết nối từ pool trước khi ném ngoại lệ `SQLException`, ngăn chặn tình trạng treo thread vô thời hạn.
   - **`max-lifetime: 1200000ms` (20 phút)**: Tuổi thọ tối đa của một kết nối. Giá trị này **bắt buộc phải nhỏ hơn `wait_timeout` của MariaDB** (thường là 8 giờ hoặc 30 phút trong cloud DB) từ 2-5 phút để đảm bảo HikariCP chủ động đóng và tạo mới kết nối trước khi server tự ngắt, triệt tiêu lỗi "Communications link failure".
   - **`leak-detection-threshold: 5000ms` (5s)**: Nếu một luồng mượn kết nối quá 5 giây mà chưa hoàn trả (chưa commit/rollback), HikariCP lập tức ghi log `WARN` kèm theo toàn bộ Stacktrace nơi đã mượn kết nối.

3. **Nguyên tắc `open-in-view: false`:**
   - Khi bật `OSIV = true` (mặc định của Spring Boot), Hibernate Session được giữ mở xuyên suốt quá trình Controller render JSON cho client. Nếu việc chuyển đổi JSON hoặc mạng client bị chậm, kết nối DB bị chiếm giữ vô ích.
   - Tắt `open-in-view: false` đảm bảo kết nối DB được giải phóng ngay khi Service method kết thúc transaction, giải phóng tài nguyên tức thì cho các request khác.

### Ví dụ trong project này

Trong `application-prod.yml` (#382):
```yaml
spring:
  jpa:
    open-in-view: false
  datasource:
    hikari:
      pool-name: JapaneseLearningProdPool
      maximum-pool-size: ${HIKARI_MAX_POOL_SIZE:20}
      minimum-idle: ${HIKARI_MIN_IDLE:10}
      connection-timeout: ${HIKARI_CONNECTION_TIMEOUT:20000}
      idle-timeout: ${HIKARI_IDLE_TIMEOUT:300000}
      max-lifetime: ${HIKARI_MAX_LIFETIME:1200000}
      leak-detection-threshold: ${HIKARI_LEAK_DETECTION:5000}
```

### Câu hỏi phỏng vấn liên quan

Tại sao việc thiết lập `max-lifetime` của HikariCP lớn hơn `wait_timeout` của MariaDB/MySQL lại là một sai lầm chết người trong production?

### Câu trả lời ngắn gọn

Nếu `wait_timeout` của MariaDB là 600s mà `max-lifetime` của HikariCP là 900s, sau 600s không có truy vấn, MariaDB server sẽ đơn phương ngắt kết nối socket ở phía nó (TCP FIN). Tuy nhiên, pool HikariCP vẫn tưởng kết nối đó đang sống. Khi có một request của người dùng mượn đúng kết nối này để thực thi truy vấn, driver sẽ ném ra lỗi `Communications link failure / Connection closed by foreign host`, làm người dùng bị lỗi 500. `max-lifetime` phải luôn ngắn hơn `wait_timeout` ít nhất vài phút để HikariCP chủ động thu hồi và tái tạo kết nối sạch.

---

## 174. Spring Transactional Read-Only Optimization & Hibernate Dirty Checking Bypass

### Giải thích ngắn gọn

Trong Spring Boot, việc gắn chú thích `@Transactional(readOnly = true)` trên các phương thức chỉ đọc dữ liệu (như hiển thị Dashboard, tìm kiếm giáo trình, báo cáo thống kê) mang lại những lợi ích tối ưu hóa hiệu năng vượt bậc ở cả 3 tầng: Spring Framework, Hibernate ORM, và JDBC Driver.

1. **Tối ưu hóa tầng Hibernate (Bỏ qua Dirty Checking):**
   - Khi một transaction thông thường diễn ra, Hibernate nạp các thực thể vào Persistence Context và tạo một bản sao lưu trạng thái ban đầu (Hydrated Snapshot). Lúc transaction commit, Hibernate phải lặp qua toàn bộ thực thể trong context để so sánh từng thuộc tính xem có bị thay đổi không (Dirty Checking).
   - Với `readOnly = true`, Spring chỉ thị cho Hibernate chuyển `FlushMode` sang `FlushMode.MANUAL`. Hibernate sẽ **hoàn toàn không lưu bản snapshot và không chạy dirty checking**, giúp tiết kiệm từ 30% đến 50% mức tiêu thụ bộ nhớ Heap và giải phóng đáng kể chu kỳ CPU khi truy vấn hàng ngàn dòng dữ liệu.

2. **Tối ưu hóa tầng JDBC Driver:**
   - Spring gọi phương thức `connection.setReadOnly(true)` trên JDBC connection.
   - Một số cơ sở dữ liệu (như MySQL/MariaDB hoặc Oracle) có thể kích hoạt các đường dẫn thực thi tối ưu hóa riêng cho truy vấn đọc (Read-only Transactions) mà không cần cấp phát Transaction ID (XID) hoặc ghi nhật ký Undo/Redo Log.

3. **Định tuyến Bản sao Đọc (Read-Replica Routing):**
   - Trong kiến trúc cơ sở dữ liệu có Master-Slave (hoặc Primary-Replica), lớp phân tuyến `AbstractRoutingDataSource` dựa vào cờ `TransactionSynchronizationManager.isCurrentTransactionReadOnly()` để tự động chuyển toàn bộ câu lệnh `SELECT` sang Replica, giải tỏa tải cho nút Primary mà không cần can thiệp vào mã nguồn nghiệp vụ.

### Ví dụ trong project này

Trong `AdminDashboardServiceImpl.java` (#382):
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardServiceImpl implements AdminDashboardService {
    // ...

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalCourses = courseRepository.count();
        long totalLessons = lessonRepository.count();
        long totalEnrollments = enrollmentRepository.count();

        List<RecentRegistrationDto> recentUsers = userRepository.findTop5ByOrderByCreatedAtDesc() ...;
        List<RecentOrderDto> recentOrders = orderRepository.findTop5ByOrderByCreatedAtDesc() ...;

        return AdminDashboardResponse.builder()
            .totalUsers(totalUsers)
            .totalCourses(totalCourses)
            .totalLessons(totalLessons)
            .totalEnrollments(totalEnrollments)
            .recentRegistrations(recentUsers)
            .recentOrders(recentOrders)
            .build();
    }
}
```

### Câu hỏi phỏng vấn liên quan

Điều gì sẽ xảy ra nếu ta vô tình gọi lệnh `repository.save()` hoặc sửa đổi thuộc tính của một Managed Entity bên trong một phương thức được đánh dấu `@Transactional(readOnly = true)`?

### Câu trả lời ngắn gọn

Do `readOnly = true` chuyển `FlushMode` thành `MANUAL`, Hibernate sẽ không bao giờ tự động gọi `flush()` khi commit transaction. Do đó, các thay đổi trên Managed Entity sẽ **âm thầm bị bỏ qua (Silently ignored)** và không được ghi xuống database. Nếu lập trình viên cố tình gọi `saveAndFlush()` hoặc thực hiện một câu lệnh Native SQL `UPDATE/INSERT`, tùy thuộc vào loại cơ sở dữ liệu và JDBC driver, hệ thống sẽ ném ra ngoại lệ `SQLException: Connection is read-only` hoặc `TransientDataAccessException`.









