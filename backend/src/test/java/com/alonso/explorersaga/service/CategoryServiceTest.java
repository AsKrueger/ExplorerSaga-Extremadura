package com.alonso.explorersaga.service;

import com.alonso.explorersaga.dto.category.CategoryCreateRequest;
import com.alonso.explorersaga.dto.category.CategoryResponse;
import com.alonso.explorersaga.entity.Category;
import com.alonso.explorersaga.exception.DuplicateResourceException;
import com.alonso.explorersaga.exception.InvalidDataException;
import com.alonso.explorersaga.exception.ResourceNotFoundException;
import com.alonso.explorersaga.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCategory_Success() {
        CategoryCreateRequest request = new CategoryCreateRequest("MONUMENT", "Desc");
        Category entity = new Category("MONUMENT", "Desc");
        entity.setId(1L);

        when(categoryRepository.findByName("MONUMENT")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(entity);

        CategoryResponse response = categoryService.createCategory(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("MONUMENT");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_EmptyName_ThrowsInvalidData() {
        CategoryCreateRequest request = new CategoryCreateRequest("", "Desc");
        assertThrows(InvalidDataException.class, () -> categoryService.createCategory(request));
    }

    @Test
    void createCategory_DuplicateName_ThrowsDuplicateResource() {
        CategoryCreateRequest request = new CategoryCreateRequest("MONUMENT", "Desc");
        when(categoryRepository.findByName("MONUMENT")).thenReturn(Optional.of(new Category()));

        assertThrows(DuplicateResourceException.class, () -> categoryService.createCategory(request));
    }

    @Test
    void getCategory_NotFound_ThrowsResourceNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategory(1L));
    }
}
