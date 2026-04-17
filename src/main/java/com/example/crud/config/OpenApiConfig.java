package com.example.crud.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ════════════════════════════════════════════════════════════
 *  @Configuration
 * ════════════════════════════════════════════════════════════
 *
 * Marks this class as a source of @Bean definitions.
 * Spring processes it at startup and registers the returned
 * objects as beans in the application context.
 *
 * ════════════════════════════════════════════════════════════
 *  OpenAPI Bean
 * ════════════════════════════════════════════════════════════
 *
 * Customises the metadata shown at the top of Swagger UI:
 * title, version, and description.
 *
 * springdoc-openapi picks this bean up automatically — no
 * extra wiring needed.
 *
 * UI:       http://localhost:8080/swagger-ui.html
 * JSON spec: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productApiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product CRUD API")
                        .version("1.0.0")
                        .description("Spring Boot CRUD demo — H2 in-memory database"));
    }
}
