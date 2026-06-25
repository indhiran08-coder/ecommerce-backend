package com.indhiran.ecommerce.service;

import com.indhiran.ecommerce.dto.request.CategoryRequest;
import com.indhiran.ecommerce.dto.response.CategoryResponse;
import com.indhiran.ecommerce.entity.Category;
import com.indhiran.ecommerce.exception.ResourceNotFoundException;
import com.indhiran.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {

        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(request.getSlug() != null
                ? request.getSlug()
                : request.getName().toLowerCase()
                .replaceAll("\\s+", "-"));

        if (request.getParentId() != null) {
            Category parent = categoryRepository
                    .findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category", "id", request.getParentId()));
            category.setParent(parent);
        }

        return mapToResponse(categoryRepository.save(category));
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(category.getParent() != null
                        ? category.getParent().getId() : null)
                .parentName(category.getParent() != null
                        ? category.getParent().getName() : null)
                .build();
    }

}