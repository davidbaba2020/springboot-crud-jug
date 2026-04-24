package com.example.crud.service;

import com.example.crud.dto.ProductDto;
import com.example.crud.dto.ProductDtoResponse;
import com.example.crud.exception.ResourceNotFoundException;
import com.example.crud.model.Product;
import com.example.crud.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * Constructor injection — @Autowired is implicit for single constructors.
     * Fields are final (immutable, thread-safe), dependencies are explicit,
     * and testing is easier (just call new ProductServiceImpl(mockRepo)).
     */
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * readOnly=true is a performance optimization for read-only operations.
     * Hibernate won't track entity state changes, reducing overhead.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductDtoResponse> findAll(Pageable pageable) {
        log.debug("Fetching products — page {}, size {}", pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDtoResponse findById(Long id) {
        log.debug("Fetching product with id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return toResponse(product);
    }

    @Override
    public ProductDtoResponse create(ProductDto request) {
        log.info("Creating new product: {}", request.name());
        Product saved = productRepository.save(toEntity(request));
        log.info("Product created with id: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    public ProductDtoResponse update(Long id, ProductDto request) {
        log.info("Updating product with id: {}", id);
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Update fields — preserve id and createdAt
        existing.setName(request.name());
        existing.setDescription(request.description());
        existing.setPrice(request.price());
        existing.setStock(request.stock());
        existing.setCategory(request.category());

        return toResponse(productRepository.save(existing));
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting product with id: {}", id);
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        log.info("Product {} deleted successfully", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDtoResponse> findByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDtoResponse> search(String keyword) {
        return productRepository.searchProducts(keyword).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDtoResponse> findByPriceRange(BigDecimal min, BigDecimal max) {
        return productRepository.findByPriceBetween(min, max).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDtoResponse> findLowStock(Integer threshold) {
        return productRepository.findByStockLessThanEqualOrderByStockAsc(threshold).stream()
                .map(this::toResponse)
                .toList();
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

    // ── Mapping helpers ─────────────────────────────────────────

    private Product toEntity(ProductDto dto) {
        return Product.builder()
                .name(dto.name())
                .description(dto.description())
                .price(dto.price())
                .stock(dto.stock())
                .category(dto.category())
                .build();
    }

    private ProductDtoResponse toResponse(Product product) {
        return new ProductDtoResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory()
        );
    }
}