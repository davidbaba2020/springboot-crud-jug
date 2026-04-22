package com.example.crud.service;

import com.example.crud.dto.ProductDto;
import com.example.crud.dto.ProductDtoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * ════════════════════════════════════════════════════════════
 *  SERVICE INTERFACE — Why use an interface here?
 * ════════════════════════════════════════════════════════════
 *
 * Best practice: define a service interface, then implement it.
 *
 * Benefits:
 *  1. LOOSE COUPLING — Controller depends on the interface, not
 *     the concrete class. Easy to swap implementations.
 *
 *  2. TESTABILITY — You can mock/stub this interface in tests
 *     without spinning up the real service.
 *
 *  3. SPRING AOP — Spring's AOP proxies (used by @Transactional,
 *     @Cacheable, security, etc.) work seamlessly with interfaces.
 *
 *  4. MULTIPLE IMPLEMENTATIONS — Swap between ProductServiceImpl
 *     and a CachedProductService without touching the controller.
 */
public interface ProductService {

    Page<ProductDtoResponse> findAll(Pageable pageable);

    ProductDtoResponse findById(Long id);

    ProductDtoResponse create(ProductDto request);

    ProductDtoResponse update(Long id, ProductDto request);

    void delete(Long id);

    List<ProductDtoResponse> findByCategory(String category);

    List<ProductDtoResponse> search(String keyword);

    List<ProductDtoResponse> findByPriceRange(BigDecimal min, BigDecimal max);

    List<ProductDtoResponse> findLowStock(Integer threshold);

    List<Map<String, Object>> getCategoryStats();
}