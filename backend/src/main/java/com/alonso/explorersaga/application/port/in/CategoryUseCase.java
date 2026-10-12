package com.alonso.explorersaga.application.port.in;

import com.alonso.explorersaga.dto.category.CategoryCreateRequest;
import com.alonso.explorersaga.dto.category.CategoryResponse;

import java.util.List;

public interface CategoryUseCase {
    CategoryResponse createCategory(CategoryCreateRequest request);
    CategoryResponse getCategory(Long id);
    List<CategoryResponse> listCategories();
}
