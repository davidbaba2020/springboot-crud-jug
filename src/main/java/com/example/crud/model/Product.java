package com.example.crud.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ════════════════════════════════════════════════════════════
 *  JPA ENTITY ANNOTATIONS
 * ════════════════════════════════════════════════════════════
 *
 * @Entity
 *   Marks this class as a JPA entity — it maps to a database table.
 *   Hibernate (JPA provider) will manage its lifecycle: creating,
 *   reading, updating, and deleting rows.
 *
 * @Table(name = "products")
 *   (Optional) Explicitly names the table. Without it, Hibernate
 *   defaults to the class name ("Product"). Good practice to be explicit.
 *
 * ════════════════════════════════════════════════════════════
 *  LOMBOK ANNOTATIONS
 * ════════════════════════════════════════════════════════════
 *
 * @Data         → generates getters, setters, toString, equals, hashCode
 * @Builder      → enables the Builder pattern: Product.builder().name("X").build()
 * @NoArgsConstructor → generates a no-arg constructor (required by JPA!)
 * @AllArgsConstructor → generates constructor with all fields (used by @Builder)
 */
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    /**
     * @Id
     *   Marks this field as the Primary Key of the entity.
     *   Every JPA entity MUST have exactly one @Id field.
     *
     * @GeneratedValue(strategy = GenerationType.IDENTITY)
     *   Tells JPA how to auto-generate the ID value.
     *   IDENTITY → delegates to the DB's auto-increment column (e.g., H2's IDENTITY).
     *
     *   Other strategies:
     *     AUTO     → JPA picks the strategy based on DB
     *     SEQUENCE → uses a DB sequence (preferred for PostgreSQL)
     *     TABLE    → uses a separate DB table to track IDs (portable but slow)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @Column
     *   Maps this field to a specific column in the table.
     *   nullable=false → adds a NOT NULL constraint in the DB schema.
     *   length=100     → sets VARCHAR(100) for String columns.
     *
     * @NotBlank (Bean Validation)
     *   Validates that the field is not null, not empty, and not just whitespace.
     *   Triggered by @Valid in the controller before the method runs.
     *
     * @Size
     *   Validates string length range. Works alongside @Column(length)
     *   but at the application level (before it ever hits the DB).
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Product name must not be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    /**
     * @Column(columnDefinition = "TEXT")
     *   Overrides the default column type. TEXT allows unlimited length.
     *   Without this, a String defaults to VARCHAR(255).
     */
    @Column(columnDefinition = "TEXT")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    /**
     * @Column(precision = 10, scale = 2)
     *   For BigDecimal fields:
     *     precision = total digits allowed (10 digits total)
     *     scale     = digits after decimal point (2 → store as 99999999.99)
     *
     * @NotNull      → field cannot be null (different from @NotBlank — works on any type)
     * @DecimalMin   → value must be >= 0.0 (inclusive=true by default)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    /**
     * @Min / @Max (Bean Validation)
     *   Validates numeric range for integer/long types.
     */
    @Column(nullable = false)
    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    @Max(value = 100000, message = "Stock cannot exceed 100,000 units")
    private Integer stock;

    @Column(length = 50)
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;

    /**
     * @Column(updatable = false)
     *   This column is set once and never updated by JPA.
     *   Perfect for "created at" timestamps.
     *
     * @PrePersist
     *   A JPA lifecycle callback. This method runs automatically
     *   BEFORE the entity is first saved (INSERT) to the DB.
     *   Other lifecycle callbacks:
     *     @PostPersist  → after INSERT
     *     @PreUpdate    → before UPDATE
     *     @PostUpdate   → after UPDATE
     *     @PreRemove    → before DELETE
     *     @PostRemove   → after DELETE
     *     @PostLoad     → after SELECT (entity loaded from DB)
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
