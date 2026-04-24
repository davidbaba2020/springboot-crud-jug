package com.example.crud.security;

/**
 * Compile-time constants for permission names.
 *
 * Use these in @PreAuthorize so the strings are verified at compile
 * time rather than silently mismatched at runtime:
 *
 *   import static com.example.crud.security.Permissions.*;
 *
 *   @PreAuthorize("hasAuthority('" + PRODUCT_DELETE + "')")
 *
 * This compiles to @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
 * — a compile-time string constant, so it is legal in an annotation.
 *
 * The corresponding rows must exist in the app_permission table
 * (seeded by data.sql) for the authorities to be granted.
 */
public final class Permissions {

    private Permissions() {}

    // ── Product permissions ──────────────────────────────────────
    public static final String PRODUCT_READ   = "PRODUCT_READ";
    public static final String PRODUCT_WRITE  = "PRODUCT_WRITE";
    public static final String PRODUCT_DELETE = "PRODUCT_DELETE";

    // ── User / admin permissions ─────────────────────────────────
    public static final String USER_READ  = "USER_READ";
    public static final String USER_WRITE = "USER_WRITE";
}