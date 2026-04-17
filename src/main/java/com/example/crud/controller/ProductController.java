package com.example.crud.controller;

import com.example.crud.dto.ProductDto;
import com.example.crud.model.Product;
import com.example.crud.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * ════════════════════════════════════════════════════════════
 *  @RestController
 * ════════════════════════════════════════════════════════════
 *
 * A meta-annotation combining:
 *  1. @Controller  → registers this as a Spring MVC controller (a Bean)
 *  2. @ResponseBody → every method return value is serialised directly
 *                     to the HTTP response body (as JSON via Jackson),
 *                     instead of being interpreted as a view name.
 *
 * Without @ResponseBody, returning "products" would look for a
 * Thymeleaf/JSP template called "products". With it, the List<Product>
 * is converted to a JSON array automatically.
 *
 * ════════════════════════════════════════════════════════════
 *  @RequestMapping("/api/products")
 * ════════════════════════════════════════════════════════════
 *
 * Sets the BASE URL prefix for all endpoints in this controller.
 * Every method URL is relative to this base path.
 * Final URLs: GET /api/products, POST /api/products, etc.
 */
@RestController
@RequestMapping("/api/products")
@Slf4j
@Tag(name = "Products", description = "CRUD operations and queries for the Product resource")
public class ProductController {

    /**
     * Constructor injection — cleaner than @Autowired on field.
     * The final keyword guarantees the dependency can't be replaced.
     */
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ════════════════════════════════════════════════════════
    //  C R U D   E N D P O I N T S
    // ════════════════════════════════════════════════════════

    /**
     * @GetMapping
     *   Shortcut for @RequestMapping(method = RequestMethod.GET)
     *   Handles HTTP GET requests to /api/products
     *
     * ResponseEntity<T>
     *   Gives full control over the HTTP response:
     *     - Status code (200, 201, 404, etc.)
     *     - Headers
     *     - Body
     *
     *   ResponseEntity.ok(body) → 200 OK with body
     */
    @Operation(summary = "Get all products")
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        log.debug("GET /api/products");
        return ResponseEntity.ok(productService.findAll());
    }

    /**
     * @GetMapping("/{id}")
     *   Path variable placeholder — matches any value in that URL segment.
     *
     * @PathVariable
     *   Extracts the value from the URL path and binds it to the parameter.
     *   GET /api/products/42 → id = 42
     */
    @Operation(summary = "Get product by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@Parameter(description = "Product ID") @PathVariable Long id) {
        log.debug("GET /api/products/{}", id);
        return ResponseEntity.ok(productService.findById(id));
    }

    /**
     * @PostMapping
     *   Handles HTTP POST requests to /api/products
     *   Used for creating new resources (C in CRUD)
     *
     * @RequestBody
     *   Deserialises the HTTP request body (JSON) into a Java object.
     *   Jackson handles the conversion: {"name":"X",...} → Product object
     *
     * @Valid
     *   Triggers Bean Validation on the @RequestBody object.
     *   Checks all annotations on Product fields (@NotBlank, @Size, etc.).
     *   If validation fails → 400 Bad Request with validation errors.
     *   Without @Valid, validation annotations are ignored!
     *
     * HttpStatus.CREATED (201)
     *   REST convention: return 201 when a resource is successfully created.
     *   201 vs 200: 201 explicitly tells the client "a new resource was created".
     */
    @Operation(summary = "Create a new product")
    @ApiResponse(responseCode = "201", description = "Product created")
    @PostMapping
//    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductDto req) {
        log.info("POST /api/products — Creating: {}", req.getName());
        Product created = productService.create2(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * @PutMapping("/{id}")
     *   Handles HTTP PUT to /api/products/{id}
     *   PUT = full replacement of the resource (all fields must be sent)
     *   PATCH would be partial update (only send changed fields)
     */
    @Operation(summary = "Update a product (full replacement)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product updated"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Valid @RequestBody Product product) {
        log.info("PUT /api/products/{}", id);
        return ResponseEntity.ok(productService.update(id, product));
    }

    /**
     * @DeleteMapping("/{id}")
     *   Handles HTTP DELETE to /api/products/{id}
     *
     * ResponseEntity<Void>
     *   A Void body (no response body). We only return 204 No Content.
     *   REST convention: DELETE returns 204 (success, no body).
     */
    @Operation(summary = "Delete a product")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Product deleted"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@Parameter(description = "Product ID") @PathVariable Long id) {
        log.info("DELETE /api/products/{}", id);
        productService.delete(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // ════════════════════════════════════════════════════════
    //  A D D I T I O N A L   Q U E R Y   E N D P O I N T S
    // ════════════════════════════════════════════════════════

    /**
     * @RequestParam
     *   Extracts a query string parameter from the URL.
     *   GET /api/products/search?q=keyboard → keyword = "keyboard"
     *
     *   required=false → parameter is optional (won't throw if missing)
     *   defaultValue   → used when parameter is absent
     */
    @Operation(summary = "Search products by keyword (name, description, or category)")
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(
            @Parameter(description = "Search keyword") @RequestParam(required = false, defaultValue = "") String q) {
        log.debug("GET /api/products/search?q={}", q);
        return ResponseEntity.ok(productService.search(q));
    }

    /**
     * GET /api/products/category/Electronics
     * Combines @PathVariable (from URL) with business logic filtering.
     */
    @Operation(summary = "Get products by category")
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getByCategory(@Parameter(description = "Category name") @PathVariable String category) {
        return ResponseEntity.ok(productService.findByCategory(category));
    }

    /**
     * GET /api/products/price-range?min=10&max=50
     * Multiple @RequestParam parameters on one method.
     */
    @Operation(summary = "Get products within a price range")
    @GetMapping("/price-range")
    public ResponseEntity<List<Product>> getByPriceRange(
            @Parameter(description = "Minimum price") @RequestParam BigDecimal min,
            @Parameter(description = "Maximum price") @RequestParam BigDecimal max) {
        return ResponseEntity.ok(productService.findByPriceRange(min, max));
    }

    /**
     * GET /api/products/low-stock?threshold=20
     * Finds products where stock <= threshold. Defaults to 10.
     */
    @Operation(summary = "Get products with stock at or below threshold")
    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStock(
            @Parameter(description = "Stock threshold (default 10)") @RequestParam(defaultValue = "10") Integer threshold) {
        return ResponseEntity.ok(productService.findLowStock(threshold));
    }

    /**
     * GET /api/products/stats/categories
     * Returns aggregated data from a native SQL query.
     */
    @Operation(summary = "Get aggregated stats per category (count, avg price)")
    @GetMapping("/stats/categories")
    public ResponseEntity<List<Map<String, Object>>> getCategoryStats() {
        return ResponseEntity.ok(productService.getCategoryStats());
    }
}
