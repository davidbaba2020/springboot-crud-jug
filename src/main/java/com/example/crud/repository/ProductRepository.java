package com.example.crud.repository;

import com.example.crud.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════════
 *  @Repository
 * ════════════════════════════════════════════════════════════
 *
 * Marks this interface as a Spring Data Repository — a component
 * responsible for data access logic (the "persistence layer").
 *
 * Spring will:
 *  1. Detect it via @ComponentScan
 *  2. Auto-generate a proxy implementation at runtime
 *  3. Register it as a Spring Bean so it can be @Autowired
 *
 * It also enables Spring's exception translation, converting
 * low-level JPA/SQL exceptions into Spring's unified
 * DataAccessException hierarchy.
 *
 * ════════════════════════════════════════════════════════════
 *  JpaRepository<Product, Long>
 * ════════════════════════════════════════════════════════════
 *
 * By extending JpaRepository, we get DOZENS of methods for FREE:
 *
 *  CREATE/UPDATE:
 *    save(entity)           → INSERT or UPDATE (upsert based on ID)
 *    saveAll(entities)      → batch save
 *
 *  READ:
 *    findById(id)           → Optional<Product>
 *    findAll()              → List<Product>
 *    findAll(Pageable)      → Page<Product> (pagination + sorting)
 *    existsById(id)         → boolean
 *    count()                → long
 *
 *  DELETE:
 *    deleteById(id)         → void
 *    delete(entity)         → void
 *    deleteAll()            → void
 *
 * Generic params:
 *   Product → the entity type this repository manages
 *   Long    → the type of the entity's @Id field
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * ════════════════════════════════════════════════════════════
     *  DERIVED QUERY METHODS (Spring Data Magic!)
     * ════════════════════════════════════════════════════════════
     *
     * Spring Data JPA can generate SQL automatically by PARSING
     * the method name. No implementation needed!
     *
     * Naming rules:
     *   findBy{Field}             → WHERE field = ?
     *   findBy{Field}Containing   → WHERE field LIKE '%?%'
     *   findBy{Field}Between      → WHERE field BETWEEN ? AND ?
     *   findBy{Field}LessThan     → WHERE field < ?
     *   findBy{A}And{B}           → WHERE a = ? AND b = ?
     *   findBy{A}Or{B}            → WHERE a = ? OR b = ?
     *   findBy{Field}OrderBy{F}   → ... ORDER BY f ASC
     *   countBy{Field}            → SELECT COUNT(*)
     *   existsBy{Field}           → SELECT COUNT(*) > 0
     *   deleteBy{Field}           → DELETE WHERE field = ?
     */

    // SELECT * FROM products WHERE category = ?
    List<Product> findByCategory(String category);

    // SELECT * FROM products WHERE LOWER(name) LIKE LOWER('%keyword%')
    List<Product> findByNameContainingIgnoreCase(String keyword);

    // SELECT * FROM products WHERE price BETWEEN ? AND ?
    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);

    // SELECT * FROM products WHERE stock <= ? ORDER BY stock ASC
    List<Product> findByStockLessThanEqualOrderByStockAsc(Integer threshold);

    // SELECT * FROM products WHERE category = ? ORDER BY price ASC
    List<Product> findByCategoryOrderByPriceAsc(String category);

    /**
     * ════════════════════════════════════════════════════════════
     *  @Query — Custom JPQL Queries
     * ════════════════════════════════════════════════════════════
     *
     * JPQL (Java Persistence Query Language) is SQL-like but
     * operates on ENTITY NAMES and FIELD NAMES, not table/column names.
     *
     * @Param binds the method parameter to the named :param in the query.
     *
     * Use @Query when:
     *  - The derived method name would be too long/complex
     *  - You need JOINs, subqueries, or aggregate functions
     *  - You need native SQL (set nativeQuery = true)
     */
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.category) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);

    /**
     * nativeQuery = true → write plain SQL (not JPQL).
     * Use sparingly — ties your code to a specific DB dialect.
     * Great for DB-specific features or complex aggregations.
     */
    @Query(value = "SELECT category, COUNT(*) as count, AVG(price) as avg_price " +
                   "FROM products GROUP BY category ORDER BY count DESC",
           nativeQuery = true)
    List<Object[]> getCategoryStats();
}
