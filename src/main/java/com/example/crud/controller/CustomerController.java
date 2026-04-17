package com.example.crud.controller;


import com.example.crud.model.Product;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@Slf4j
@Tag(name = "Customer", description = "CRUD operations and queries for the custer resource")
public class CustomerController {

    @Operation(summary = "Get all products")
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        log.debug("GET /api/products");
        return null;
    }
}
