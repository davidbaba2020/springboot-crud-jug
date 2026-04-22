package com.example.crud.dto;

import java.math.BigDecimal;

public record ProductDtoResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String category
) {}