# Spring Boot CRUD — Spring Security + DB-Driven RBAC

A complete Spring Boot REST API demonstrating:
- Full CRUD with pagination
- JWT authentication (JJWT 0.12)
- **DB-driven Role-Based Access Control** — roles and permissions live in the database, not in code
- First-login forced password change
- Custom security filters

---

## Architecture Overview

```
app_permission          app_role
┌───────────────┐      ┌─────────────┐      ┌──────────────┐
│ id            │      │ id          │      │ id           │
│ name          │◄─────│ name        │◄─────│ username     │
│ description   │  M:M │ description │  M:1 │ email        │
└───────────────┘      └─────────────┘      │ password     │
     role_permissions                       │ role_id (FK) │
                                            │ mustChange.. │
                                            │ enabled      │
                                            └──────────────┘
                                               app_user
```

**Seeded roles and permissions:**

| Role        | Permissions                                                                 |
|-------------|-----------------------------------------------------------------------------|
| `ROLE_ADMIN`| `PRODUCT_READ`, `PRODUCT_WRITE`, `PRODUCT_DELETE`, `USER_READ`, `USER_WRITE`|
| `ROLE_USER` | `PRODUCT_READ`, `PRODUCT_WRITE`                                             |

---

## Project Structure

```
src/main/java/com/example/crud/
├── controller/
│   ├── AuthController.java        login, register, change-password, /me
│   ├── AdminController.java       user management (USER_READ / USER_WRITE)
│   ├── ProductController.java     product CRUD (PRODUCT_* permissions)
│   └── TestController.java        public / user / admin test endpoints
├── service/
│   ├── ProductService.java
│   └── ProductServiceImpl.java
├── repository/
│   ├── ProductRepository.java
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   └── PermissionRepository.java
├── model/
│   ├── Product.java               JPA entity
│   ├── User.java                  JPA entity (app_user table)
│   ├── Role.java                  JPA entity (app_role table) — NOT an enum
│   └── Permission.java            JPA entity (app_permission table)
├── security/
│   ├── Permissions.java           Compile-time permission name constants
│   ├── JwtUtils.java              Token generation & validation (JJWT 0.12)
│   ├── JwtAuthenticationFilter.java   Custom filter #1 — reads Bearer token
│   ├── ForcePasswordChangeFilter.java Custom filter #2 — enforces password reset
│   ├── UserDetailsServiceImpl.java    Loads user + builds authorities from DB
│   └── SecurityConfig.java            SecurityFilterChain, permission rules
├── dto/                           Java records (no Lombok needed)
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── AuthResponse.java          includes permissions list
│   ├── ChangePasswordRequest.java
│   ├── CreateUserRequest.java     admin creates user with roleName string
│   ├── UserSummary.java           safe user view (no password)
│   ├── ProductDto.java            product request record
│   └── ProductDtoResponse.java    product response record
└── exception/
    ├── ResourceNotFoundException.java
    └── GlobalExceptionHandler.java    handles security + validation exceptions
```

---

## Running the Application

### Prerequisites
- Java 17+
- Maven 3.6+

### Start

```bash
JAVA17_HOME=/path/to/jdk-21 mvn spring-boot:run
```

App starts on **http://localhost:8095**. H2 console: **http://localhost:8095/h2-console**

---

## Seed Accounts

| Username | Password   | Role         | mustChangePassword |
|----------|------------|--------------|--------------------|
| `admin`  | `admin123` | `ROLE_ADMIN` | false              |
| `user`   | `user123`  | `ROLE_USER`  | false              |
| `newbie` | `user123`  | `ROLE_USER`  | **true**           |

---

## Authentication Flow

```
POST /api/auth/login
  body: { "username": "admin", "password": "admin123" }

Response:
  {
    "token": "eyJhbGci...",
    "username": "admin",
    "role": "ROLE_ADMIN",
    "permissions": ["PRODUCT_DELETE","PRODUCT_READ","PRODUCT_WRITE","USER_READ","USER_WRITE"],
    "mustChangePassword": false
  }

Use the token in subsequent requests:
  Authorization: Bearer eyJhbGci...
```

---

## API Endpoints

### Authentication — `/api/auth`

| Method | Path                      | Auth Required   | Description                          |
|--------|---------------------------|-----------------|--------------------------------------|
| POST   | `/api/auth/login`         | Public          | Login — returns JWT + permissions    |
| POST   | `/api/auth/register`      | Public          | Self-service register (ROLE_USER)    |
| POST   | `/api/auth/change-password` | Any JWT       | Change password / clear force flag   |
| GET    | `/api/auth/me`            | Any JWT         | Current user profile                 |

### Access Control Tests — `/api/test`

| Method | Path               | Required Permission | Description              |
|--------|--------------------|---------------------|--------------------------|
| GET    | `/api/test/public` | None                | Open to everyone         |
| GET    | `/api/test/user`   | `PRODUCT_READ`      | Accessible to USER+ADMIN |
| GET    | `/api/test/admin`  | `USER_READ`         | Accessible to ADMIN only |

### Products — `/api/products`

| Method | Path                                    | Required Permission  | Description           |
|--------|-----------------------------------------|----------------------|-----------------------|
| GET    | `/api/products`                         | `PRODUCT_READ`       | Paginated list        |
| GET    | `/api/products/{id}`                    | `PRODUCT_READ`       | Get by ID             |
| POST   | `/api/products`                         | `PRODUCT_WRITE`      | Create product        |
| PUT    | `/api/products/{id}`                    | `PRODUCT_WRITE`      | Update product        |
| DELETE | `/api/products/{id}`                    | `PRODUCT_DELETE`     | Delete product        |
| GET    | `/api/products/search?q=`               | `PRODUCT_READ`       | Keyword search        |
| GET    | `/api/products/category/{cat}`          | `PRODUCT_READ`       | By category           |
| GET    | `/api/products/price-range?min=&max=`   | `PRODUCT_READ`       | By price range        |
| GET    | `/api/products/low-stock?threshold=`    | `PRODUCT_READ`       | Low-stock alert       |
| GET    | `/api/products/stats/categories`        | `PRODUCT_READ`       | Category stats        |

### Admin — `/api/admin` (all require `USER_READ`; mutations require `USER_WRITE`)

| Method | Path                                        | Permission   | Description                   |
|--------|---------------------------------------------|--------------|-------------------------------|
| GET    | `/api/admin/users`                          | `USER_READ`  | List all users                |
| GET    | `/api/admin/users/{id}`                     | `USER_READ`  | Get user by ID                |
| GET    | `/api/admin/roles`                          | `USER_READ`  | List roles + their permissions|
| POST   | `/api/admin/users`                          | `USER_WRITE` | Create user account           |
| PUT    | `/api/admin/users/{id}/role?roleName=`      | `USER_WRITE` | Change user's role            |
| PUT    | `/api/admin/users/{id}/disable`             | `USER_WRITE` | Disable account               |
| PUT    | `/api/admin/users/{id}/enable`              | `USER_WRITE` | Re-enable account             |
| PUT    | `/api/admin/users/{id}/force-password-change`| `USER_WRITE`| Force reset on next login     |
| DELETE | `/api/admin/users/{id}`                     | `USER_WRITE` | Permanently delete            |

---

## curl Quick Reference

### Login as admin
```bash
TOKEN=$(curl -s -X POST http://localhost:8095/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")
echo $TOKEN
```

### Access a protected product endpoint
```bash
curl http://localhost:8095/api/products \
  -H "Authorization: Bearer $TOKEN"
```

### Create a product (PRODUCT_WRITE required)
```bash
curl -X POST http://localhost:8095/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop Stand","description":"Aluminium adjustable","price":49.99,"stock":80,"category":"Electronics"}'
```

### Delete a product (PRODUCT_DELETE — admin only)
```bash
curl -X DELETE http://localhost:8095/api/products/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Try delete as regular user (expect 403)
```bash
USER_TOKEN=$(curl -s -X POST http://localhost:8095/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

curl -X DELETE http://localhost:8095/api/products/1 \
  -H "Authorization: Bearer $USER_TOKEN"
# -> 403: user has PRODUCT_READ + PRODUCT_WRITE but not PRODUCT_DELETE
```

### First-login forced password change
```bash
# 1 — login as newbie (mustChangePassword=true in response)
NEWBIE=$(curl -s -X POST http://localhost:8095/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"newbie","password":"user123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

# 2 — any endpoint is blocked (ForcePasswordChangeFilter returns 403)
curl http://localhost:8095/api/products \
  -H "Authorization: Bearer $NEWBIE"

# 3 — change the password
curl -X POST http://localhost:8095/api/auth/change-password \
  -H "Authorization: Bearer $NEWBIE" \
  -H "Content-Type: application/json" \
  -d '{"currentPassword":"user123","newPassword":"NewPass@99"}'
# response contains a new token with mustChangePassword: false

# 4 — full access now works
```

### Admin creates a user with forced password change
```bash
curl -X POST http://localhost:8095/api/admin/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "temporaryPassword": "Temp@123",
    "roleName": "ROLE_USER",
    "mustChangePassword": true
  }'
```

### View all roles and their permissions
```bash
curl http://localhost:8095/api/admin/roles \
  -H "Authorization: Bearer $TOKEN"
```

---

## How DB-Driven RBAC Works

1. **Permissions** are rows in `app_permission` (e.g. `PRODUCT_DELETE`).
2. **Roles** are rows in `app_role`, joined to permissions via `role_permissions`.
3. **Users** reference one role via a foreign key (`role_id`).
4. On every request, `UserDetailsServiceImpl` loads the user's role and all its permissions from the DB and constructs the Spring Security authority list:

```
authorities = ["ROLE_ADMIN", "PRODUCT_READ", "PRODUCT_WRITE", "PRODUCT_DELETE", "USER_READ", "USER_WRITE"]
```

5. `SecurityConfig` and `@PreAuthorize` check against this authority list.

To grant a new permission to `ROLE_USER`, just insert a row in `role_permissions` — no code changes required:

```sql
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM app_role r, app_permission p
WHERE r.name = 'ROLE_USER' AND p.name = 'PRODUCT_DELETE';
```

---

## Security Filter Order

```
Request
  |
  v  JwtAuthenticationFilter        (OncePerRequestFilter)
  |   - extracts Bearer token
  |   - validates JWT signature + expiry
  |   - loads user + authorities from DB
  |   - sets SecurityContext
  |
  v  ForcePasswordChangeFilter      (OncePerRequestFilter)
  |   - reads mustChangePassword claim from JWT
  |   - if true: blocks all endpoints except /api/auth/change-password
  |
  v  FilterSecurityInterceptor      (Spring built-in)
  |   - checks requestMatchers rules (hasAuthority, permitAll, etc.)
  |   - calls @PreAuthorize SpEL if present
  |
  v  Controller method
```

---

## Swagger / OpenAPI

| URL | Description |
|-----|-------------|
| http://localhost:8095/swagger-ui.html | Interactive Swagger UI |
| http://localhost:8095/v3/api-docs | Raw OpenAPI 3 JSON |

---

## H2 Console

Browse the in-memory database at: **http://localhost:8095/h2-console**

| Field    | Value                |
|----------|----------------------|
| JDBC URL | `jdbc:h2:mem:cruddb` |
| Username | `sa`                 |
| Password | *(empty)*            |

Useful tables: `app_user`, `app_role`, `app_permission`, `role_permissions`, `products`

---

## Annotation Cheatsheet

| Annotation                  | Layer    | Purpose                                                        |
|-----------------------------|----------|----------------------------------------------------------------|
| `@SpringBootApplication`    | App      | @Configuration + @EnableAutoConfiguration + @ComponentScan    |
| `@EnableWebSecurity`        | Security | Activates Spring Security web support                         |
| `@EnableMethodSecurity`     | Security | Enables @PreAuthorize / @PostAuthorize on methods             |
| `@PreAuthorize`             | Security | SpEL expression checked before method runs                    |
| `@RestController`           | Web      | @Controller + @ResponseBody — returns JSON                    |
| `@GetMapping` etc.          | Web      | Maps HTTP method to a handler                                  |
| `@RequestBody`              | Web      | Deserialises JSON into a Java record                          |
| `@Valid`                    | Web      | Triggers Bean Validation                                      |
| `@Service`                  | Service  | Business logic bean                                           |
| `@Transactional`            | Service  | DB transaction boundary                                       |
| `@Repository`               | Data     | Data access bean + exception translation                      |
| `@Entity`                   | Model    | Maps class to a DB table                                      |
| `@ManyToOne`                | Model    | FK relationship (User -> Role)                                |
| `@ManyToMany`               | Model    | Join-table relationship (Role -> Permission)                  |
| `@JoinTable`                | Model    | Configures the join table for @ManyToMany                     |
| `@PrePersist` / `@PreUpdate`| Model    | JPA lifecycle callbacks for timestamps                        |
| `@Data` / `@Builder`        | Lombok   | Generates boilerplate                                         |
| `@RestControllerAdvice`     | Error    | Global exception handler                                      |

---

## Running Tests

```bash
JAVA17_HOME=/path/to/jdk-21 mvn test
```

---

## Lecture PDF

A detailed step-by-step lecture guide is included:

```
Spring_Security_JWT_Lecture.pdf
```

Covers: authentication vs authorisation, the security filter chain, JWT anatomy,
custom filters line-by-line, DB-driven RBAC, UserDetailsService, SecurityConfig,
first-login password change, full API reference, and curl testing guide.