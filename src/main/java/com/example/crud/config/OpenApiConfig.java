package com.example.crud.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ════════════════════════════════════════════════════════════
 *  OpenApiConfig — Swagger UI with JWT Bearer authentication
 * ════════════════════════════════════════════════════════════
 *
 * WHAT THIS DOES
 * ──────────────
 * 1. Registers a "bearerAuth" security scheme of type HTTP/Bearer.
 *    Swagger UI shows an "Authorize" button where you paste your JWT.
 *
 * 2. Applies that scheme globally via addSecurityItem().
 *    Every endpoint shows a lock icon in Swagger UI.
 *    Public endpoints (login, register) still work without a token —
 *    Spring Security permits them regardless; the lock icon just
 *    means "this scheme is available" not "this endpoint requires it".
 *
 * HOW TO USE IN SWAGGER UI
 * ─────────────────────────
 * 1. Call POST /api/auth/login to get your JWT.
 * 2. Click the "Authorize" button (top right).
 * 3. Paste the token into the "bearerAuth" field (no "Bearer " prefix —
 *    Swagger adds it automatically).
 * 4. Click "Authorize" then "Close".
 * 5. All subsequent requests from Swagger UI include the header:
 *      Authorization: Bearer <your-token>
 *
 * SECURITY SCHEME TYPES
 * ──────────────────────
 * HTTP scheme "bearer"
 *   → Swagger sends:  Authorization: Bearer <value>
 *   → bearerFormat "JWT" is a documentation hint only (not enforced).
 *
 * Other scheme types for reference:
 *   apiKey   → custom header or query param (e.g. X-API-Key)
 *   oauth2   → full OAuth2 flows (authCode, clientCredentials, etc.)
 *   openIdConnect → OIDC discovery URL
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8095")
                                .description("Local development server")
                ))
                .components(securityComponents())
                /*
                 * Global security requirement — applies bearerAuth to every
                 * endpoint by default. Individual operations can override this
                 * with @SecurityRequirements({}) to mark themselves as public.
                 */
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    // ── API metadata ─────────────────────────────────────────────

    private Info apiInfo() {
        return new Info()
                .title("Product CRUD API")
                .version("2.0.0")
                .description("""
                        Spring Boot 3 REST API with JWT authentication and \
                        database-driven role-based access control (RBAC).

                        ## Authentication

                        1. Call **POST /api/auth/login** with your credentials.
                        2. Copy the `token` from the response.
                        3. Click the **Authorize** button above and paste the token \
                        (without the "Bearer " prefix — Swagger adds it automatically).

                        ## Permission Matrix

                        | Permission       | Who has it                  | Protected endpoints          |
                        |------------------|-----------------------------|------------------------------|
                        | `PRODUCT_READ`   | ROLE_ADMIN, ROLE_USER       | GET /api/products/**         |
                        | `PRODUCT_WRITE`  | ROLE_ADMIN, ROLE_USER       | POST, PUT /api/products      |
                        | `PRODUCT_DELETE` | ROLE_ADMIN only             | DELETE /api/products/**      |
                        | `USER_READ`      | ROLE_ADMIN only             | GET /api/admin/**            |
                        | `USER_WRITE`     | ROLE_ADMIN only             | POST/PUT/DELETE /api/admin/**|

                        ## Seed Accounts (development only)

                        | Username | Password   | Role         |
                        |----------|------------|--------------|
                        | admin    | admin123   | ROLE_ADMIN   |
                        | user     | user123    | ROLE_USER    |
                        | newbie   | user123    | ROLE_USER (must change password) |
                        """)
                .contact(new Contact()
                        .name("David")
                        .email("admin@example.com"));
    }

    // ── Security components ──────────────────────────────────────

    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, jwtBearerScheme());
    }

    /**
     * Defines the Bearer JWT security scheme.
     *
     * type   = HTTP  → uses the standard Authorization header
     * scheme = bearer → Authorization: Bearer <token>
     * bearerFormat = JWT → documentation hint shown in Swagger UI
     */
    private SecurityScheme jwtBearerScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description(
                        "Paste your JWT token here (obtained from POST /api/auth/login). " +
                        "Do NOT include the 'Bearer ' prefix — Swagger UI adds it automatically.");
    }
}
