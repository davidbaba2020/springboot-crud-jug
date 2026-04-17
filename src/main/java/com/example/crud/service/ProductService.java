package com.example.crud.service;

import com.example.crud.dto.ProductDto;
import com.example.crud.model.Product;

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

    List<Product> findAll();

    Product findById(Long id);

    Product create(Product product);

    Product create2(ProductDto product);

    Product update(Long id, Product product);

    void delete(Long id);

    List<Product> findByCategory(String category);

    List<Product> search(String keyword);

    List<Product> findByPriceRange(BigDecimal min, BigDecimal max);

    List<Product> findLowStock(Integer threshold);

    List<Map<String, Object>> getCategoryStats();
}
