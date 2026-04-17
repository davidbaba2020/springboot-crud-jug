# Spring Boot CRUD Demo — Annotations Explained

A complete Spring Boot CRUD application using H2 in-memory database,
built to clearly illustrate the role of every major Spring annotation.

---

## Project Structure

```
src/main/java/com/example/crud/
├── CrudDemoApplication.java        ← @SpringBootApplication entry point
├── controller/
│   └── ProductController.java      ← @RestController, @GetMapping, @PostMapping...
├── service/
│   ├── ProductService.java         ← Interface (loose coupling)
│   └── ProductServiceImpl.java     ← @Service, @Slf4j, @Transactional
├── repository/
│   └── ProductRepository.java      ← @Repository, JpaRepository
├── model/
│   └── Product.java                ← @Entity, @Table, @Column, @Data, validation
└── exception/
    ├── ResourceNotFoundException.java  ← @ResponseStatus
    └── GlobalExceptionHandler.java     ← @RestControllerAdvice, @ExceptionHandler, @Slf4j

src/main/resources/
├── application.properties          ← datasource, JPA, logging config
└── data.sql                        ← seed data (5 products loaded on startup)
```

---

## Running the Application

### Prerequisites
- Java 17+
- Maven 3.6+

### Start

```bash
mvn spring-boot:run
```

### Build executable JAR

```bash
mvn clean package
java -jar target/crud-demo-0.0.1-SNAPSHOT.jar
```

The app starts on **http://localhost:8080** and seeds 5 products automatically.

---

## Build Notes

### Lombok annotation processing

Lombok requires explicit registration as a Maven annotation processor — having it
as a plain `<dependency>` is not enough. The `pom.xml` registers it via
`maven-compiler-plugin`'s `annotationProcessorPaths` so `@Slf4j`, `@Data`,
`@Builder`, etc. are generated at compile time.

### Java version (terminal builds)

Maven must compile with **Java 17**. If your terminal `JAVA_HOME` points to a
newer JDK (e.g. Java 21+), Lombok's annotation processor will crash because
`TypeTag.UNKNOWN` was removed in later compiler internals.

The build is configured with `<fork>true</fork>` and `<executable>${env.JAVA17_HOME}/bin/javac</executable>`.
Set `JAVA17_HOME` before running Maven from the terminal:

```bash
export JAVA17_HOME=/Users/learning/Library/Java/JavaVirtualMachines/ms-17.0.18/Contents/Home
mvn clean compile
# or in one line:
JAVA17_HOME=/Users/learning/Library/Java/JavaVirtualMachines/ms-17.0.18/Contents/Home mvn spring-boot:run
```

**IntelliJ** handles this automatically — it uses the JDK configured in
*Project Structure → Project SDK* (set it to the Microsoft Java 17 SDK).

---

## Seed Data Fix (data.sql)

`created_at` and `updated_at` are `NOT NULL` columns. The `@PrePersist`
callback only fires through JPA — raw SQL in `data.sql` bypasses it.
The INSERT statements explicitly supply `NOW()` for both columns:

```sql
INSERT INTO products (name, description, price, stock, category, created_at, updated_at) VALUES
  ('Wireless Keyboard', '...', 89.99, 150, 'Electronics', NOW(), NOW()),
  ...
```

---

## Swagger / OpenAPI

| URL | Description |
|-----|-------------|
| http://localhost:8080/swagger-ui.html | Interactive Swagger UI |
| http://localhost:8080/v3/api-docs | Raw OpenAPI 3 JSON spec |

Powered by `springdoc-openapi-starter-webmvc-ui`. Every endpoint is annotated
with `@Operation`, `@ApiResponse`, and `@Parameter` so Swagger UI shows
summaries, response codes, and parameter descriptions.

---

## API Endpoints

| Method | URL                                     | Description                   |
|--------|-----------------------------------------|-------------------------------|
| GET    | /api/products                           | Get all products               |
| GET    | /api/products/{id}                      | Get product by ID              |
| POST   | /api/products                           | Create a product               |
| PUT    | /api/products/{id}                      | Update a product               |
| DELETE | /api/products/{id}                      | Delete a product               |
| GET    | /api/products/search?q={keyword}        | Search by name/desc/category   |
| GET    | /api/products/category/{category}       | Filter by category             |
| GET    | /api/products/price-range?min=&max=     | Filter by price range          |
| GET    | /api/products/low-stock?threshold=      | Find low-stock products        |
| GET    | /api/products/stats/categories          | Category aggregation stats     |

---

## Complete curl Reference

### Get all products
```bash
curl http://localhost:8080/api/products
```

### Get product by ID
```bash
curl http://localhost:8080/api/products/1
```

### Create a product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mechanical Keyboard",
    "description": "Tactile switches, RGB backlighting",
    "price": 129.99,
    "stock": 75,
    "category": "Electronics"
  }'
```

### Update a product (full replacement)
```bash
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mechanical Keyboard Pro",
    "description": "Updated description, tactile switches",
    "price": 149.99,
    "stock": 60,
    "category": "Electronics"
  }'
```

### Delete a product
```bash
curl -X DELETE http://localhost:8080/api/products/1
```

### Search by keyword (name, description, or category)
```bash
curl "http://localhost:8080/api/products/search?q=keyboard"
```

### Filter by category
```bash
curl http://localhost:8080/api/products/category/Electronics
```

### Filter by price range
```bash
curl "http://localhost:8080/api/products/price-range?min=10&max=50"
```

### Find low-stock products (default threshold: 10)
```bash
curl "http://localhost:8080/api/products/low-stock?threshold=20"
```

### Category aggregation stats
```bash
curl http://localhost:8080/api/products/stats/categories
```

---

## Validation Rules

Sent in the request body for POST and PUT:

| Field         | Rule                                      |
|---------------|-------------------------------------------|
| `name`        | Required, 2–100 characters                |
| `description` | Optional, max 1000 characters             |
| `price`       | Required, greater than 0                  |
| `stock`       | Required, 0–100,000                       |
| `category`    | Optional, max 50 characters               |

Validation failures return **400 Bad Request** with a `fieldErrors` map:

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "One or more fields have invalid values",
  "fieldErrors": {
    "name": "Product name must not be blank",
    "price": "Price must be greater than 0"
  }
}
```

---

## H2 Console

Browse the in-memory database at: **http://localhost:8080/h2-console**

| Field    | Value                  |
|----------|------------------------|
| JDBC URL | `jdbc:h2:mem:cruddb`   |
| Username | `sa`                   |
| Password | *(leave empty)*        |

---

## Annotation Cheatsheet

| Annotation               | Layer      | Purpose                                                        |
|--------------------------|------------|----------------------------------------------------------------|
| `@SpringBootApplication` | App        | Meta: @Configuration + @EnableAutoConfiguration + @ComponentScan |
| `@RestController`        | Controller | @Controller + @ResponseBody — returns JSON                     |
| `@RequestMapping`        | Controller | Base URL prefix for all methods                                |
| `@GetMapping`            | Controller | Maps HTTP GET to a method                                      |
| `@PostMapping`           | Controller | Maps HTTP POST to a method                                     |
| `@PutMapping`            | Controller | Maps HTTP PUT to a method                                      |
| `@DeleteMapping`         | Controller | Maps HTTP DELETE to a method                                   |
| `@PathVariable`          | Controller | Extracts `{id}` from the URL path                              |
| `@RequestParam`          | Controller | Extracts `?param=value` from the query string                  |
| `@RequestBody`           | Controller | Deserialises JSON request body into a Java object              |
| `@Valid`                 | Controller | Triggers Bean Validation on the annotated parameter            |
| `@Service`               | Service    | Marks as business logic bean                                   |
| `@Slf4j`                 | Service    | Generates `private static final Logger log` via Lombok         |
| `@Transactional`         | Service    | Wraps method in a DB transaction; rollback on error            |
| `@Repository`            | Repository | Marks as data-access bean; enables exception translation       |
| `@Query`                 | Repository | Custom JPQL or native SQL query                                |
| `@Entity`                | Model      | Maps this class to a DB table                                  |
| `@Table`                 | Model      | Specifies the table name                                       |
| `@Id`                    | Model      | Marks the primary key field                                    |
| `@GeneratedValue`        | Model      | Auto-generates the PK value                                    |
| `@Column`                | Model      | Column constraints (nullable, length, etc.)                    |
| `@Data`                  | Model      | Lombok: generates getters, setters, toString, equals, hashCode |
| `@Builder`               | Model      | Lombok: enables builder pattern                                |
| `@NoArgsConstructor`     | Model      | Lombok: generates no-arg constructor (required by JPA)         |
| `@AllArgsConstructor`    | Model      | Lombok: generates all-args constructor (used by @Builder)      |
| `@PrePersist`            | Model      | JPA lifecycle callback: runs before INSERT                     |
| `@PreUpdate`             | Model      | JPA lifecycle callback: runs before UPDATE                     |
| `@NotBlank`              | Model      | Validates field is not blank (Bean Validation)                 |
| `@NotNull`               | Model      | Validates field is not null                                    |
| `@Size`                  | Model      | Validates string length range                                  |
| `@Min` / `@Max`          | Model      | Validates numeric range                                        |
| `@DecimalMin`            | Model      | Validates decimal minimum value                                |
| `@RestControllerAdvice`  | Exception  | Global exception handler for all controllers                   |
| `@ExceptionHandler`      | Exception  | Maps exception type to a handler method                        |
| `@ResponseStatus`        | Exception  | Sets HTTP status code for an exception                         |

---

## Running Tests

```bash
mvn test
```
