package com.example.crud.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDto {

    @NotBlank(message = "Product name must not be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Column(columnDefinition = "TEXT")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    @Max(value = 100000, message = "Stock cannot exceed 100,000 units")
    private Integer stock;

    @Column(length = 50)
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String  category;
}