package com.alonso.explorersaga.controller;

import com.alonso.explorersaga.dto.category.CategoryCreateRequest;
import com.alonso.explorersaga.dto.category.CategoryResponse;
import com.alonso.explorersaga.exception.DuplicateResourceException;
import com.alonso.explorersaga.exception.ResourceNotFoundException;
import com.alonso.explorersaga.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @Test
    void createCategory_Success() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest("MONUMENT", "Desc");
        CategoryResponse response = new CategoryResponse(1L, "MONUMENT", "Desc");

        when(categoryService.createCategory(any(CategoryCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("MONUMENT"));
    }

    @Test
    void createCategory_Invalid_BadRequest() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest("", "Desc"); // Blank name

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategory_Duplicate_Conflict() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest("DUP", "Desc");
        when(categoryService.createCategory(any())).thenThrow(new DuplicateResourceException("Already exists"));

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getCategory_Success() throws Exception {
        when(categoryService.getCategory(1L)).thenReturn(new CategoryResponse(1L, "CAT", "Desc"));

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("CAT"));
    }

    @Test
    void getCategory_NotFound_404() throws Exception {
        when(categoryService.getCategory(1L)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listCategories_Success() throws Exception {
        when(categoryService.listCategories()).thenReturn(List.of(new CategoryResponse(1L, "C1", "D1")));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("C1"));
    }
}
