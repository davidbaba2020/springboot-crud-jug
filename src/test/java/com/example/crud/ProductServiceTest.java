package com.example.crud;

import com.example.crud.model.Product;
import com.example.crud.repository.ProductRepository;
import com.example.crud.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ════════════════════════════════════════════════════════════
 *  @SpringBootTest
 * ════════════════════════════════════════════════════════════
 *
 * Loads the FULL application context for integration testing.
 * All beans are available, H2 database is initialized, etc.
 *
 * For unit tests (testing a single class in isolation), you'd
 * use @ExtendWith(MockitoExtension.class) and mock dependencies.
 *
 * ════════════════════════════════════════════════════════════
 *  @Transactional on Test Class
 * ════════════════════════════════════════════════════════════
 *
 * Each test method runs in a transaction that is ROLLED BACK
 * after the test. This keeps the database clean between tests
 * without needing to manually clean up data.
 */
@SpringBootTest
@Transactional
@DisplayName("Product Service Integration Tests")
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private Product sampleProduct;

    /**
     * @BeforeEach
     * Runs before EACH test method. Used to set up test data.
     * Other lifecycle annotations:
     *   @AfterEach  → runs after each test
     *   @BeforeAll  → runs once before all tests (must be static)
     *   @AfterAll   → runs once after all tests (must be static)
     */
    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .name("Test Product")
                .description("A product for testing")
                .price(new BigDecimal("29.99"))
                .stock(50)
                .category("Test")
                .build();
    }

    /**
     * @Test — marks this method as a test case.
     * @DisplayName — human-readable test name in reports.
     *
     * AssertJ's assertThat() provides fluent, readable assertions.
     */
    @Test
    @DisplayName("Should create a product successfully")
    void shouldCreateProduct() {
        Product created = productService.create(sampleProduct);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Test Product");
        assertThat(created.getPrice()).isEqualByComparingTo("29.99");
        assertThat(created.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find product by id")
    void shouldFindProductById() {
        Product created = productService.create(sampleProduct);
        Product found = productService.findById(created.getId());

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Test Product");
    }

    @Test
    @DisplayName("Should update product successfully")
    void shouldUpdateProduct() {
        Product created = productService.create(sampleProduct);

        Product update = Product.builder()
                .name("Updated Product")
                .description("Updated description")
                .price(new BigDecimal("49.99"))
                .stock(100)
                .category("Updated")
                .build();

        Product updated = productService.update(created.getId(), update);

        assertThat(updated.getName()).isEqualTo("Updated Product");
        assertThat(updated.getPrice()).isEqualByComparingTo("49.99");
        assertThat(updated.getId()).isEqualTo(created.getId()); // ID unchanged
    }

    @Test
    @DisplayName("Should delete product")
    void shouldDeleteProduct() {
        Product created = productService.create(sampleProduct);
        Long id = created.getId();

        productService.delete(id);

        assertThat(productRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Should search products by keyword")
    void shouldSearchProducts() {
        productService.create(sampleProduct);

        List<Product> results = productService.search("Test");

        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(p -> p.getName().contains("Test"));
    }

    @Test
    @DisplayName("Should throw exception for non-existent product")
    void shouldThrowExceptionForNonExistentProduct() {
        assertThatThrownBy(() -> productService.findById(9999L))
                .isInstanceOf(com.example.crud.exception.ResourceNotFoundException.class)
                .hasMessageContaining("9999");
    }
}
