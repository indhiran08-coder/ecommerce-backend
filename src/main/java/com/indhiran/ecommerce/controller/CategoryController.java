package com.indhiran.ecommerce.controller;

import com.indhiran.ecommerce.dto.request.CategoryRequest;
import com.indhiran.ecommerce.dto.response.ApiResponse;
import com.indhiran.ecommerce.dto.response.CategoryResponse;
import com.indhiran.ecommerce.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Category created",
                        categoryService.createCategory(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>>
    getAllCategories() {

        return ResponseEntity.ok(ApiResponse.success(
                "Categories fetched",
                categoryService.getAllCategories()));
    }

}