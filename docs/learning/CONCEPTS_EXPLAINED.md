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
