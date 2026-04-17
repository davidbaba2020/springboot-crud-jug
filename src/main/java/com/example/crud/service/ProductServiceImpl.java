package com.example.crud.service;

import com.example.crud.dto.ProductDto;
import com.example.crud.exception.ResourceNotFoundException;
import com.example.crud.model.Product;
import com.example.crud.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * ════════════════════════════════════════════════════════════
 *  @Service
 * ════════════════════════════════════════════════════════════
 *
 * A specialization of @Component. Marks this class as a
 * "service" — the business logic layer. Functionally identical
 * to @Component, but conveys intent and enables future
 * AOP pointcuts targeting services specifically.
 *
 * Spring registers this as a Bean named "productServiceImpl"
 * (lowercase first letter of class name by default).
 *
 * ════════════════════════════════════════════════════════════
 *  @Slf4j (Lombok)
 * ════════════════════════════════════════════════════════════
 *
 * Generates a static final Logger field:
 *   private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
 *
 * So you can write: log.debug("..."), log.info("..."), log.error("...")
 *
 * ════════════════════════════════════════════════════════════
 *  @Transactional
 * ════════════════════════════════════════════════════════════
 *
 * Applied at class level → all public methods run in a transaction.
 *
 * What a transaction guarantees:
 *  - ATOMICITY: All DB operations in a method succeed or all roll back.
 *  - Spring starts a transaction before the method, commits after,
 *    and rolls back if a RuntimeException is thrown.
 *
 * At method level, you can override:
 *  @Transactional(readOnly = true) → optimisation hint for SELECT-only methods.
 *    Hibernate skips dirty checking (no need to track changes), uses
 *    read replicas if configured, and prevents accidental writes.
 *
 * Key attributes:
 *  propagation  → how transaction boundaries interact (REQUIRED is default)
 *  isolation    → database isolation level
 *  rollbackFor  → which exceptions trigger rollback (RuntimeException by default)
 *  readOnly     → optimisation for read operations
 */
@Service
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    /**
     * ════════════════════════════════════════════════════════════
     *  @Autowired — Dependency Injection
     * ════════════════════════════════════════════════════════════
     *
     * Tells Spring to inject a bean of type ProductRepository here.
     * Spring finds the bean it created for ProductRepository (the
     * auto-generated JPA proxy) and injects it.
     *
     * CONSTRUCTOR INJECTION (preferred, shown below in comment):
     *   When there's only ONE constructor, @Autowired is optional —
     *   Spring injects automatically. Constructor injection is preferred
     *   because:
     *     - Fields are final (immutable, thread-safe)
     *     - Dependencies are explicit
     *     - Easier to test (just call new ProductServiceImpl(mockRepo))
     *
     * FIELD INJECTION (@Autowired on field) — works but discouraged:
     *   - Makes testing harder (can't inject mock without reflection)
     *   - Hides dependencies
     *   - Can't use final fields
     */
    private final ProductRepository productRepository;

    // Constructor injection — @Autowired is implicit for single constructors
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * readOnly=true is a performance optimization for read-only operations.
     * Hibernate won't track entity state changes, reducing overhead.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        log.debug("Fetching all products");
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        log.debug("Fetching product with id: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }


    @Override
    public Product create(Product product) {
        log.info("Creating new product: {}", product.getName());
        Product saved = productRepository.save(product);
        log.info("Product created with id: {}", saved.getId());
        return saved;
    }

    @Override
    public Product create2(ProductDto product) {
        Product product1 = maptToProduct(product);
        log.info("Creating new product: {}", product.getName());
        Product saved = productRepository.save(product1);
        log.info("Product created with id: {}", saved.getId());
        return saved;
    }

    @Override
    public Product update(Long id, Product updatedProduct) {
        log.info("Updating product with id: {}", id);
        // findById throws ResourceNotFoundException if not found
        Product existing = findById(id);

        // Update fields — we don't replace the entity to preserve id and createdAt
        existing.setName(updatedProduct.getName());
        existing.setDescription(updatedProduct.getDescription());
        existing.setPrice(updatedProduct.getPrice());
        existing.setStock(updatedProduct.getStock());
        existing.setCategory(updatedProduct.getCategory());

        // save() here does an UPDATE because existing.getId() is set
        return productRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting product with id: {}", id);
        // Verify it exists before deleting
        findById(id);
        productRepository.deleteById(id);
        log.info("Product {} deleted successfully", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> search(String keyword) {
        return productRepository.searchProducts(keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findByPriceRange(BigDecimal min, BigDecimal max) {
        return productRepository.findByPriceBetween(min, max);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findLowStock(Integer threshold) {
        return productRepository.findByStockLessThanEqualOrderByStockAsc(threshold);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCategoryStats() {
        List<Object[]> raw = productRepository.getCategoryStats();
        List<Map<String, Object>> stats = new ArrayList<>();
        for (Object[] row : raw) {
            Map<String, Object> stat = new LinkedHashMap<>();
            stat.put("category", row[0]);
            stat.put("count", row[1]);
            stat.put("avgPrice", row[2]);
            stats.add(stat);
        }
        return stats;
    }


    public Product getProduct(Long id) {
        return productRepository.findById(id).get();
    }

    private Product maptToProduct(ProductDto product) {
        return Product.builder()
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .stock(product.getStock())
                .build();
    }
}
