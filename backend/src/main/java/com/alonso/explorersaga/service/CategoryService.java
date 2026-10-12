package com.alonso.explorersaga.service;

import com.alonso.explorersaga.application.port.in.CategoryUseCase;
import com.alonso.explorersaga.application.port.out.CategoryRepositoryPort;
import com.alonso.explorersaga.dto.category.CategoryCreateRequest;
import com.alonso.explorersaga.dto.category.CategoryResponse;
import com.alonso.explorersaga.entity.Category;
import com.alonso.explorersaga.exception.DuplicateResourceException;
import com.alonso.explorersaga.exception.InvalidDataException;
import com.alonso.explorersaga.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService implements CategoryUseCase {

    private final CategoryRepositoryPort categoryRepositoryPort;

    public CategoryService(CategoryRepositoryPort categoryRepositoryPort) {
        this.categoryRepositoryPort = categoryRepositoryPort;
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidDataException("Category name is required");
        }

        if (categoryRepositoryPort.findByName(request.getName()).isPresent()) {
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
        }

        Category category = new Category(request.getName(), request.getDescription());
        Category saved = categoryRepositoryPort.save(category);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {
        return categoryRepositoryPort.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Category with ID " + id + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepositoryPort.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CategoryResponse mapToResponse(Category entity) {
        return new CategoryResponse(entity.getId(), entity.getName(), entity.getDescription());
    }
}
