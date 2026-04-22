package com.example.crud.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductDto(

        @NotBlank(message = "Product name must not be blank")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,

        @NotNull(message = "Stock is required")
        @Min(value = 0, message = "Stock cannot be negative")
        @Max(value = 100000, message = "Stock cannot exceed 100,000 units")
        Integer stock,

        @Size(max = 50, message = "Category cannot exceed 50 characters")
        String category

) {}