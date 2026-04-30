package com.example.crud;

import com.example.crud.dto.ProductDto;
import com.example.crud.dto.ProductDtoResponse;
import com.example.crud.repository.ProductRepository;
import com.example.crud.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

    private ProductDto sampleProduct;

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
        sampleProduct = new ProductDto(
                "Test Product",
                "A product for testing",
                new BigDecimal("29.99"),
                50,
                "Test"
        );
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
        ProductDtoResponse created = productService.create(sampleProduct);

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Test Product");
        assertThat(created.price()).isEqualByComparingTo("29.99");
        assertThat(created.category()).isEqualTo("Test");
    }

    @Test
    @DisplayName("Should find product by id")
    void shouldFindProductById() {
        ProductDtoResponse created = productService.create(sampleProduct);
        ProductDtoResponse found = productService.findById(created.id());

        assertThat(found).isNotNull();
        assertThat(found.name()).isEqualTo("Test Product");
        assertThat(found.price()).isEqualByComparingTo("29.99");    }

    @Test
    @DisplayName("Should update product successfully")
    void shouldUpdateProduct() {
        ProductDtoResponse created = productService.create(sampleProduct);

        ProductDto updateRequest = new ProductDto(
                "Updated Product",
                "Updated description",
                new BigDecimal("49.99"),
                100,
                "Updated"
        );

        ProductDtoResponse updated = productService.update(created.id(), updateRequest);

        assertThat(updated.name()).isEqualTo("Updated Product");
        assertThat(updated.price()).isEqualByComparingTo("49.99");
        assertThat(updated.id()).isEqualTo(created.id()); // ID unchanged
    }

    @Test
    @DisplayName("Should delete product")
    void shouldDeleteProduct() {
        ProductDtoResponse created = productService.create(sampleProduct);
        Long id = created.id();

        productService.delete(id);

        assertThat(productRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Should search products by keyword")
    void shouldSearchProducts() {
        productService.create(sampleProduct);

        List<ProductDtoResponse> results = productService.search("Test");

        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(p -> p.name().contains("Test"));
    }

    @Test
    @DisplayName("Should throw exception for non-existent product")
    void shouldThrowExceptionForNonExistentProduct() {
        assertThatThrownBy(() -> productService.findById(9999L))
                .isInstanceOf(com.example.crud.exception.ResourceNotFoundException.class)
                .hasMessageContaining("9999");
    }
}